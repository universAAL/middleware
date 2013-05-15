/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */
package org.universAAL.middleware.connectors.communication.jgroups;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolConfiguration;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.util.UUID;
import org.jgroups.util.Util;
import org.universAAL.middleware.connectors.CommunicationConnector;
import org.universAAL.middleware.connectors.communication.jgroups.util.Codec;
import org.universAAL.middleware.connectors.communication.jgroups.util.Consts;
import org.universAAL.middleware.connectors.communication.jgroups.util.CryptUtil;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorErrorCode;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.connectors.util.ExceptionUtils;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.communication.CommunicationModuleImpl;

/**
 * JGroup communication connector implementation
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:filippo.palumbo@isti.cnr.it">Filippo Palumbo</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 */
public class JGroupsCommunicationConnector implements CommunicationConnector,
        Receiver, RequestHandler, MembershipListener {

    private String name;
    private String version;
    private String description;
    private String provider;
    private boolean enableRemoteChannelConfigurarion;
    private ModuleContext context;
    private CommunicationModule communicationModule;
    // maps the channel name with the channel instance
    // The channel name is in the XXX.space configuration file (ex. Home.space)
    private final Map<String, JChannel> channelMap = new HashMap<String, JChannel>();
    private MessageDispatcher disp;
    // Security stuff
    private boolean security = false;
    private String key;

    public JGroupsCommunicationConnector(ModuleContext context)
            throws Exception {
        this.context = context;

        security = Boolean.parseBoolean(System.getProperty(
                "universaal.security.enabled", "false"));

        if( security == false ){
            return;
        }

        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "Security enabled" + security }, null);

        String fileName = System.getProperty("bouncycastle.key");

        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "Security key file : " + fileName
                        + " bouncycastle.key" }, null);

        File file = new File(fileName, "bouncycastle.key");
        boolean exists = file.exists();
        if (!exists) {
            // disable security
            LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Security disabled, key file not found" },
                    null);
            throw new Exception("Security disabled. Key file not found.");

        } else {
            // It returns true if File or directory exists
            // init the cryptoUtil
            try {
                CryptUtil.init(fileName, new Codec() {
                    public byte[] encode(byte[] data) {
                        return org.bouncycastle.util.encoders.Base64
                                .encode(data);
                    }

                    public byte[] decode(String data) {
                        return org.bouncycastle.util.encoders.Base64
                                .decode(data);
                    }
                });
            } catch (Exception e1) {
                LogUtils.logError(
                        context,
                        CommunicationModuleImpl.class,
                        "CommunicationModuleImpl",
                        new Object[] { "Error while initializing the CryptoUtil: "
                                + e1.toString() }, null);
                throw new Exception("Security disabled. Key file not found.");
            }

            LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Connector Up" }, null);
        }

    }

    public void configureConnector(List<ChannelDescriptor> channels,
            String peerName) throws CommunicationConnectorException {
        LogUtils.logDebug(
                context,
                JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "Configuring the JChannel and the ReceiverAdapter..." },
                null);
        communicationModule = (CommunicationModule) context.getContainer()
                .fetchSharedObject(context,
                        new Object[] { CommunicationModule.class.getName() });
        for (final ChannelDescriptor element : channels) {
            try {
                JChannel ch = null;
                if (channelMap.containsKey(element.getChannelName())) {
                    LogUtils.logWarn(
                            context,
                            JGroupsCommunicationConnector.class,
                            "JGroupsCommunicationConnector",
                            new Object[] { "The channel: "
                                    + element.getChannelName()
                                    + " is already configured" }, null);
                } else {
                    try {
                        ch = configureJChannel(element);
                    } catch (Exception e) {
                        LogUtils.logError(
                                context,
                                JGroupsCommunicationConnector.class,
                                "JGroupsCommunicationConnector",
                                new Object[] { "Error configuringing the JChannel"
                                        + e }, null);
                        throw new CommunicationConnectorException(
                                CommunicationConnectorErrorCode.NEW_CHANNEL_ERROR,
                                "Error configuringing the JChannel: " + e);

                    }

                    if (ch != null) {
                        ch.setDiscardOwnMessages(true);
                        ch.setReceiver(this);
                        // nome logico del peer che esegue la join. Il nome
                        // logico � associato ad un UUID

                        ch.setName(peerName);
                        // ch.setAddressGenerator(new AddressGenerator() {
                        // public Address generateAddress() {
                        // return PayloadUUID.randomUUID("prova");
                        // }
                        // });
                        ch.connect(element.getChannelName());
                        // associates the channel name with the channel instance
                        channelMap.put(element.getChannelName(), ch);
                    }
                }
            } catch (Exception e) {
                LogUtils.logError(
                        context,
                        JGroupsCommunicationConnector.class,
                        "JGroupsCommunicationConnector",
                        new Object[] { "Error configuringing the JChannel and the ReceiverAdapter: "
                                + e }, null);
                throw new CommunicationConnectorException(
                        CommunicationConnectorErrorCode.NEW_CHANNEL_ERROR,
                        "Error configuringing the JChannel and the ReceiverAdapter: "
                                + e);
            }
        }
        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "JChannel and ReceiverAdapter configured." },
                null);

    }

    /**
     * Strategy for initializing the jGroups Communication channel: -if
     * enableRemoteChannelConfiguration is true -> init jGroups channel with
     * default constructor -if enableRemoteChannelConfiguration is false and and
     * URL for jGroups conf. channel is provided -> init with URL -if
     * enableRemoteChannelConfiguration is false and a configuration XML is
     * provided -> init with XML
     *
     * @param element
     * @return
     * @throws Exception
     */
    private JChannel configureJChannel(ChannelDescriptor element)
            throws Exception {
        JChannel ch = null;
        if (enableRemoteChannelConfigurarion == false) {
            ch = new JChannel();
        } else if (element.getChannelDescriptorFileURL() != null) {
            // Set up the jChannel from the URL or the value
            URL channelURL = new URL(element.getChannelDescriptorFileURL());
            try {
                ch = createSharedChannel(channelURL);
            } catch (Exception e) {
                LogUtils.logInfo(
                        context,
                        JGroupsCommunicationConnector.class,
                        "JGroupsCommunicationConnector",
                        new Object[] { "Trying to initializee the channels locally" },
                        null);
            }
        } else if (ch == null && element.getChannelValue() != null) {
            // Try from the InputStream
            InputStream channelValue = new ByteArrayInputStream(element
                    .getChannelValue().getBytes());
            try {
                ch = createSharedChannel(channelValue);
            } catch (Exception e) {
                LogUtils.logError(context, JGroupsCommunicationConnector.class,
                        "JGroupsCommunicationConnector",
                        new Object[] { "Unable to initialize the channel: "
                                + element.toString() }, null);
            }

        }
        return ch;

    }

    public void dispose(List<ChannelDescriptor> channels) {
        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "Reset the JGroupCommunicationConnector..." },
                null);
        try {
            if (channelMap != null) {
                for (ChannelDescriptor channel : channels) {
                    Util.close(channelMap.get(channel.getChannelName()));
                }
            }
        } catch (Exception e) {
            LogUtils.logError(
                    context,
                    JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Error while resetting the Communication connector: "
                            + e.toString() }, null);
        } finally {
            Set<String> keys = channelMap.keySet();
            // check if to close some channels
            for (String key : keys) {
                if (channelMap.get(key).isOpen()) {
                    Util.close(channelMap.get(key));
                }
            }
            for (ChannelDescriptor channelDesc : channels) {
                channelMap.remove(channelDesc.getChannelName());
            }
        }
        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "JGroupCommunicationConnector reset" }, null);

    }

    public void dispose() {
        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "Reset the JGroupCommunicationConnector..." },
                null);

        try {
            if (channelMap != null) {
                Set<String> keys = channelMap.keySet();
                for (String key : keys) {
                    Util.close(channelMap.get(key));
                }
            }
        } catch (Exception e) {
            LogUtils.logError(
                    context,
                    JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Error while resetting the Communication connector: "
                            + e.toString() }, null);
        } finally {
            channelMap.clear();
        }
        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "JGroupCommunicationConnector reset" }, null);

    }

    private JChannel createSharedChannel(URL channelURL) throws Exception {
        try {
            ProtocolStackConfigurator config = ConfiguratorFactory
                    .getStackConfigurator(channelURL);
            List<ProtocolConfiguration> protocols = config.getProtocolStack();
            // ProtocolConfiguration transport = protocols.get(0);
            // transport.getProperties().put(Global.SINGLETON_NAME,
            // transport.getProtocolName());
            return new JChannel(config);
        } catch (Exception e) {
            LogUtils.logError(
                    context,
                    JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Unable to initialize the JGroup channel with URL: "
                            + channelURL }, null);
            throw new CommunicationConnectorException(
                    CommunicationConnectorErrorCode.CHANNEL_INIT_ERROR,
                    "Unable to initialize the JGroup channel with URL: "
                            + channelURL);
        }

    }

    private JChannel createSharedChannel(InputStream channelValue)
            throws Exception {
        try {
            ProtocolStackConfigurator config = ConfiguratorFactory
                    .getStackConfigurator(channelValue);
            List<ProtocolConfiguration> protocols = config.getProtocolStack();
            // ProtocolConfiguration transport = protocols.get(0);
            // transport.getProperties().put(Global.SINGLETON_NAME,
            // transport.getProtocolName());
            return new JChannel(config);

        } catch (Exception e) {
            LogUtils.logError(
                    context,
                    JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Unable to initialize the JGroup channel with URL: "
                            + channelValue.toString() + " -> " + e.toString() },
                    null);
            throw new CommunicationConnectorException(
                    CommunicationConnectorErrorCode.CHANNEL_INIT_ERROR,
                    "Unable to initialize the JGroup channel with URL: "
                            + channelValue.toString() + " -> " + e.toString());
        }
    }

    /**
     * This method selects the channels to which to send the message
     *
     * @param message
     * @return
     */
    private List selectJChannels(List channelNames) {
        List selectedJChannels = new ArrayList();
        for (int i = 0; i < channelNames.size(); i++) {
            String channel = (String) channelNames.get(i);
            if (channelMap.containsKey(channel))
                selectedJChannels.add(channelMap.get(channel));
        }
        return selectedJChannels;
    }

    public synchronized void unicast(ChannelMessage message, String receiver)
            throws CommunicationConnectorException {
        if (message.getChannelNames() == null
                || message.getChannelNames().isEmpty()) {
            LogUtils.logError(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "No channel name specified..." }, null);
        } else if (message.getChannelNames().size() > 1) {
            LogUtils.logError(
                    context,
                    JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Too much receivers specified for unicast..." },
                    null);
        } else {
            // get the first and only channel
            JChannel ch = channelMap.get(message.getChannelNames().get(0));
            if (ch == null) {
                LogUtils.logWarn(context, JGroupsCommunicationConnector.class,
                        "JGroupsCommunicationConnector",
                        new Object[] { "The channel name:"
                                + message.getChannelNames().get(0)
                                + " is not configured" }, null);
            } else {
                View view = ch.getView();
                if (view == null) {
                    LogUtils.logError(context,
                            JGroupsCommunicationConnector.class,
                            "JGroupsCommunicationConnector",
                            new Object[] { "Not able to find the receiver: "
                                    + message.toString() }, null);
                    return;
                }
                List<Address> list = view.getMembers();
                Message msg = null;
                for (Address address : list) {
                    if (receiver.equals(ch.getName(address))) {
                        if (security) {
                            try {
                                msg = new Message(address, null,
                                        CryptUtil.encrypt(message.toString()));
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                LogUtils.logError(
                                        context,
                                        JGroupsCommunicationConnector.class,
                                        "JGroupsCommunicationConnector",
                                        new Object[] { "Error during unicast with message: "
                                                + e.toString() }, null);
                            }
                        } else {
                            msg = new Message(address, null, message.toString());
                        }
                    }
                }
                if (msg != null) {
                    try {
                        ch.send(msg);
                    } catch (Exception e) {
                        LogUtils.logError(
                                context,
                                JGroupsCommunicationConnector.class,
                                "JGroupsCommunicationConnector",
                                new Object[] { "Error during unicast with message: "
                                        + message.toString() }, null);
                        throw new CommunicationConnectorException(
                                CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                                e.toString());
                    }
                } else {
                    LogUtils.logError(
                            context,
                            JGroupsCommunicationConnector.class,
                            "JGroupsCommunicationConnector",
                            new Object[] { "The JChannel cannot be created with ChannelMessage:"
                                    + message.toString() }, null);
                }
            }
        }
    }

    public synchronized void multicast(ChannelMessage message)
            throws CommunicationConnectorException {
        if (message.getChannelNames() != null) {
            // send message to all brokers of any kind
            List selectedChannel = selectJChannels(message.getChannelNames());
            for (int i = 0; i < selectedChannel.size(); i++) {
                JChannel channel = (JChannel) selectedChannel.get(i);
                Message msg = null;

                if (security) {
                    try {
                        msg = new Message(null, null, CryptUtil.encrypt(message
                                .toString()));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LogUtils.logError(
                                context,
                                JGroupsCommunicationConnector.class,
                                "JGroupsCommunicationConnector",
                                new Object[] { "Error during unicast with message: "
                                        + e.toString() }, null);
                    }
                } else {
                    msg = new Message(null, null, message.toString());
                }
                try {
                    channel.send(msg);
                } catch (Exception e) {
                    LogUtils.logError(context,
                            JGroupsCommunicationConnector.class,
                            "JGroupsCommunicationConnector",
                            new Object[] { "Sending broadcast message: "
                                    + message.toString() }, null);
                    throw new CommunicationConnectorException(
                            CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                            e.toString());
                }
            }
        }
    }

    public synchronized void multicast(ChannelMessage message,
            List<PeerCard> receivers) throws CommunicationConnectorException {
        if (message.getChannelNames() == null) {
            LogUtils.logError(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Unable to select the channel to use..." },
                    null);
        } else {
            List selectedChannel = selectJChannels(message.getChannelNames());

            if (selectedChannel == null || selectedChannel.isEmpty()) {
                LogUtils.logWarn(
                        context,
                        JGroupsCommunicationConnector.class,
                        "JGroupsCommunicationConnector",
                        new Object[] { "Unable to select the channels to use with message: "
                                + message.toString() }, null);
            } else {

                // Send the message to the selected channels and selected
                // receivers
                for (int i = 0; i < selectedChannel.size(); i++) {
                    JChannel channel = (JChannel) selectedChannel.get(i);
                    Message msg = null;
                    View view = channel.getView();
                    List<Address> list = view.getMembers();

                    // creation of the list of address to be excluded from
                    // broadcast
                    List<String> removeList = new ArrayList<String>();
                    for (Address address : list) {
                        removeList.add(channel.getName(address));
                    }
                    List<String> nodeIDsAsString = new ArrayList<String>();
                    for (PeerCard nodeID : receivers) {
                        nodeIDsAsString.add(nodeID.getPeerID());
                    }
                    removeList.removeAll(nodeIDsAsString);
                    List<Address> removeAddressList = new ArrayList<Address>();
                    for (Address address : list) {
                        for (String removeAdressString : removeList) {
                            if (channel.getName(address).equals(
                                    removeAdressString))
                                removeAddressList.add(address);
                        }
                    }
                    //
                    if (security) {
                        try {
                            msg = new Message(null, null,
                                    CryptUtil.encrypt(message.toString()));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            LogUtils.logError(
                                    context,
                                    JGroupsCommunicationConnector.class,
                                    "JGroupsCommunicationConnector",
                                    new Object[] { "Error during unicast with message: "
                                            + e.toString() }, null);
                        }
                    } else {
                        msg = new Message(null, null, message.toString());
                    }

                    RequestOptions opts = new RequestOptions();
                    opts.setExclusionList((Address[]) removeAddressList
                            .toArray());
                    disp = new MessageDispatcher(channel, null, null, this);

                    try {
                        disp.sendMessage(msg, opts);
                    } catch (Exception e) {
                        LogUtils.logError(
                                context,
                                JGroupsCommunicationConnector.class,
                                "JGroupsCommunicationConnector",
                                new Object[] { "Unable to broadcast the message:"
                                        + message.toString() }, null);
                        throw new CommunicationConnectorException(
                                CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                                e.toString());
                    }
                }
            }
        }
    }

    public void loadConfigurations(Dictionary configurations) {
        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "updating JGroups Connector properties" }, null);
        if (configurations == null) {
            LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "JGroups Connector properties are null" },
                    null);
            return;
        }
        try {
            this.name = (String) configurations
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_NAME);
            this.version = (String) configurations
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_VERSION);
            this.description = (String) configurations
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_DESCRIPTION);
            this.provider = (String) configurations
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_PROVIDER);
            this.enableRemoteChannelConfigurarion = Boolean
                    .getBoolean(((String) configurations
                            .get(Consts.ENABLE_REMOTE_CHANNEL_CONFIG)));
        } catch (NumberFormatException e) {
            LogUtils.logError(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Error during JGroups properties update" },
                    null);
        } catch (NullPointerException e) {
            LogUtils.logError(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Error during JGroups properties update" },
                    null);
        } catch (Exception e) {
            LogUtils.logError(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Error during JGroups properties update" },
                    null);
        }
        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "JGroups Connector properties updated" }, null);
    }

    public void loadProperties(Dictionary properties) {
        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "updating JGroups Connector properties" }, null);
        if (properties == null) {
            LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "JGroups Connector properties are null" },
                    null);
            return;
        }
        try {
            this.name = (String) properties
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_NAME);
            this.version = (String) properties
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_VERSION);
            this.description = (String) properties
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_DESCRIPTION);
            this.provider = (String) properties
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_PROVIDER);
        } catch (NumberFormatException e) {
            LogUtils.logError(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Error during JGroups properties update" },
                    null);
        } catch (NullPointerException e) {
            LogUtils.logError(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Error during JGroups properties update" },
                    null);
        } catch (Exception e) {
            LogUtils.logError(context, JGroupsCommunicationConnector.class,
                    "JGroupsCommunicationConnector",
                    new Object[] { "Error during JGroups properties update" },
                    null);
        }
        LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
                "JGroupsCommunicationConnector",
                new Object[] { "JGroups Connector properties updated" }, null);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getProvider() {
        return provider;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void receive(Message msg) {
        try {
            String msgBuffer = new String(msg.getBuffer());
            if (security) {
                msgBuffer = CryptUtil.decrypt((String) msg.getObject());
            }
            ChannelMessage channelMessage = ChannelMessage.unmarhall(msgBuffer);
            communicationModule.messageReceived(channelMessage);
        } catch (Exception ex) {
            LogUtils.logDebug(
                    context,
                    JGroupsCommunicationConnector.class,
                    "receive",
                    new Object[] { "Failed to unmarhall message due to exception "
                            + ExceptionUtils.stackTraceAsString(ex) }, ex);
        }
    }

    public void getState(OutputStream output) throws Exception {
        // TODO Auto-generated method stub

    }

    public void setState(InputStream input) throws Exception {
        // TODO Auto-generated method stub

    }

    public void block() {
        // TODO Auto-generated method stub

    }

    public void suspect(Address suspectedMbr) {

    }

    public void unblock() {
        // TODO Auto-generated method stub

    }

    public void viewAccepted(View newView) {

    }

    public String toString() {
        return "Name: " + this.name + " Version: " + this.version
                + " Description: " + this.description + " Provider: "
                + this.provider;
    }

    public Object handle(Message msg) throws Exception {
        ChannelMessage channelMessage = ChannelMessage.unmarhall(new String(msg
                .getBuffer()));
        communicationModule.messageReceived(channelMessage);
        return null;
    }

    public boolean init() {
        // TODO Auto-generated method stub
        return false;
    }

    public List<String> getGroupMembers(String groupName) {
        List<String> members = new ArrayList<String>();
        if (channelMap.get(groupName) != null
                && channelMap.get(groupName).getView() != null) {
            List<Address> addresses = channelMap.get(groupName).getView()
                    .getMembers();
            for (Address address : addresses) {
                members.add(channelMap.get(groupName).getName(address));
            }
        }
        return members;

    }

    public static void main(String[] args) {
        JChannel c;
        try {

            ProtocolStackConfigurator config = ConfiguratorFactory
                    .getStackConfigurator(new URL(
                            "http://aaloa.isti.cnr.it/udp.xml"));
            List<ProtocolConfiguration> protocols = config.getProtocolStack();
            // ProtocolConfiguration transport = protocols.get(0);
            // transport.getProperties().put(Global.SINGLETON_NAME,
            // transport.getProtocolName());
            c = new JChannel(config);
            c.setDiscardOwnMessages(true);
            c.setReceiver(new JGroupsCommunicationConnector(null));
            c.setName(UUID.randomUUID().toString());

            c.connect("i");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int a = 2;

    }

    public boolean hasChannel(String channelName) {
        return channelMap.containsKey(channelName);
    }
}
