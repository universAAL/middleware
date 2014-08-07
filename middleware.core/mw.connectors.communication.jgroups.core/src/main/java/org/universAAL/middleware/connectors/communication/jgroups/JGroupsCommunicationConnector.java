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
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.Message.Flag;
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

/**
 * JGroup communication connector implementation
 * 
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:filippo.palumbo@isti.cnr.it">Filippo Palumbo</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 */
public class JGroupsCommunicationConnector implements CommunicationConnector,
Receiver, RequestHandler, MembershipListener{

    private String name;
    private String version;
    private String description;
    private String provider;
    private boolean enableRemoteChannelConfigurarion;
    private ModuleContext context;
    private CommunicationModule communicationModule;
    // maps the channel name with the channel instance
    // The prefix of the channel name is in the XXX.space configuration file
    // (ex. Home.space)
    private final Map<String, JChannel> channelMap = new HashMap<String, JChannel>();
    private MessageDispatcher disp;
    // Security stuff
    private boolean security = false;
    private String key;
    private String enableRemoteChannelURL = null;

    public JGroupsCommunicationConnector(ModuleContext context)
	    throws Exception {
	this.context = context;

	security = Boolean.parseBoolean(System.getProperty(
		"universaal.security.enabled", "false"));

	if (security == false) {
	    return;
	} else {
	    initializeSecurity();
	}




    }

    private void initializeSecurity() throws Exception {
	final String METHOD = "initializeSecurity";

	LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
		"JGroupsCommunicationConnector",
		new Object[] { "Security enabled" + security }, null);

	String fileName = System.getProperty("bouncycastle.key");

	LogUtils.logDebug(context, JGroupsCommunicationConnector.class, METHOD,
		new Object[] { "Security key file : ", fileName,
	" bouncycastle.key" }, null);

	File file = new File(fileName, "bouncycastle.key");
	boolean exists = file.exists();
	if (!exists) {
	    // disable security
	    LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
		    METHOD, "Security disabled, key file not found");
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
	    } catch (Exception ex) {
		LogUtils.logError(
			context,
			JGroupsCommunicationConnector.class,
			METHOD,
			new Object[] {
			    "Error while initializing the CryptoUtil: ",
			    ex.toString() }, ex);
		throw new Exception("Security disabled. Key file not found.");
	    }

	    LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
		    METHOD, "Connector Up");
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


			//the name of the cluster is composed by: name+AALSpaceID
			ch.connect( element.getChannelName());
			// associates the channel name with the channel instance
			channelMap.put( element.getChannelName(), ch);
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
     * 
     * @param element
     * @return
     * @throws Exception
     */
    private JChannel configureJChannel(ChannelDescriptor element)
	    throws Exception {
	final String METHOD = "configureJChannel";
	JChannel ch = null;
	if (enableRemoteChannelConfigurarion == false) {
	    LogUtils.logDebug(
		    context,
		    JGroupsCommunicationConnector.class,
		    METHOD,
		    "Remote channel configuration disabled using default JGroup cluster configuration for channel "
			    + element.getChannelName());
	    return new JChannel();
	}
	URL urlConfig = null;
	if (enableRemoteChannelConfigurarion && enableRemoteChannelURL != null) {
	    urlConfig = new URL(enableRemoteChannelURL);
	}
	if (enableRemoteChannelConfigurarion && urlConfig == null
		&& element.getChannelDescriptorFileURL() != null) {
	    urlConfig = new URL(element.getChannelDescriptorFileURL());
	}
	// Set up the jChannel from the URL or the value
	if (urlConfig != null) {
	    try {
		LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
			METHOD, new Object[] {
		    "Loading JGroups communication channel from ",
		    urlConfig }, null);
		ch = createSharedChannel(urlConfig);
	    } catch (Exception e) {
		LogUtils.logError(context, JGroupsCommunicationConnector.class,
			METHOD, new Object[] {
		    "Failed to load remote configuration for ",
		    element.getChannelName(), " from URL -> ",
		    urlConfig, " due to internal exception ",
		    ExceptionUtils.stackTraceAsString(e), "\n",
		"Trying to initializee the channels locally" },
		e);
	    }
	}
	if (enableRemoteChannelConfigurarion && ch == null
		&& element.getChannelValue() == null) {
	    LogUtils.logDebug(
		    context,
		    JGroupsCommunicationConnector.class,
		    METHOD,
		    new Object[] { "No local configuration for ",
			element.getChannelName(),
		    "\nFalling back to JGroup default cluster configuration" },
		    null);
	    return new JChannel();
	}
	if (enableRemoteChannelConfigurarion && ch == null
		&& element.getChannelValue() != null) {
	    // Try from the InputStream
	    InputStream channelValue = new ByteArrayInputStream(element
		    .getChannelValue().getBytes());
	    try {
		LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
			METHOD,
			"Loading JChannel configuration from <channelValue> tag");
		ch = createSharedChannel(channelValue);
	    } catch (Exception e) {
		LogUtils.logError(
			context,
			JGroupsCommunicationConnector.class,
			METHOD,
			new Object[] {
			    "Failed to load local configuration for ",
			    element.getChannelName(),
			    " due to internal exception ",
			    ExceptionUtils.stackTraceAsString(e), "\n",
			"Falling back to JGroup default cluster configuration" },
			e);
	    }

	}
	if (ch != null) {
	    return ch;
	} else {
	    throw new CommunicationConnectorException(
		    CommunicationConnectorErrorCode.CHANNEL_INIT_ERROR,
		    "Unable to load channel configuration from anysource");
	}
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
		    new Object[] {
			"Unable to initialize the JGroup channel with URL: ",
			channelURL }, e);
	    throw new CommunicationConnectorException(
		    CommunicationConnectorErrorCode.CHANNEL_INIT_ERROR,
		    "Unable to initialize the JGroup channel with URL: "
			    + channelURL, e);
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
     * This method selects the channels to which to send the message. For every
     * destinationChannelNames I check if the name of one of the communcationChannels contains the destinationChannelName
     * 
     * @param message
     * @return
     */
    private List<JChannel> selectJChannels(List destinationChannelNames) {
	List<JChannel> selectedJChannels = new ArrayList<JChannel>();
	for (int i = 0; i < destinationChannelNames.size(); i++) {
	    String destinaltionChannelName = (String) destinationChannelNames
		    .get(i);

	    for (String channelName : channelMap.keySet()) {
		if (channelName.contains(destinaltionChannelName)) {
		    selectedJChannels.add(channelMap.get(channelName));
		}
	    }
	}
	return selectedJChannels;
    }

    public synchronized void unicast(ChannelMessage message, String receiver)
	    throws CommunicationConnectorException {
	final String METHOD = "unicast";
	if (message.getChannelNames() == null
		|| message.getChannelNames().isEmpty()) {
	    logAndThrowComExec(METHOD,
		    CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED,
		    "No channel name specified");
	    return;
	}
	if (message.getChannelNames().size() > 1) {
	    logAndThrowComExec(METHOD,
		    CommunicationConnectorErrorCode.MULTIPLE_RECEIVERS,
		    "Too much receivers specified for unicast");
	    return;
	}
	// retrieve the channel to which send the message


	JChannel ch = selectJChannels(message.getChannelNames()).get(0);
	if (ch == null) {
	    logAndThrowComExec(
		    METHOD,
		    CommunicationConnectorErrorCode.CHANNEL_NOT_FOUND,
		    "The channel name:"
			    + ch.getName()
			    + " was not found. It is either not configured or it has been deleted");
	    return;
	}
	View view = ch.getView();
	if (view == null) {
	    logAndThrowComExec(METHOD,
		    CommunicationConnectorErrorCode.NOT_CONNECTED_TO_CHANNEL,
		    "Unable to get the View on the channel " + ch.getName()
		    + " We may not be connected to it");
	    return;
	}
	Address dst = null;
	final Message msg;
	/*
	 * //FIX The android peer is not shown as member of the channel thus
	 * joining fails
	 */
	for (Address address : view.getMembers()) {
	    if (receiver.equals(ch.getName(address))) {
		dst = address;
		break;
	    }
	}
	if (dst == null) {
	    logAndThrowComExec(METHOD,
		    CommunicationConnectorErrorCode.RECEIVER_NOT_EXISTS,
		    "Trying to send message to " + receiver
		    + " but it is not a memeber of " + ch.getName()
		    + "/" + ch.getClusterName());
	    return;
	}
	if (security) {
	    try {
		msg = new Message(dst, null, CryptUtil.encrypt(message
			.toString()));
	    } catch (Throwable t) {
		logAndThrowComExec(
			METHOD,
			CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
			"Failed to encrypt the message due to internal exception",
			t);
		return;
	    }
	} else {
	    msg = new Message(dst, null, message.toString());
	}

	try {
	    ch.send(msg);
	} catch (Exception t) {
	    logAndThrowComExec(METHOD,
		    CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
		    "Error sending unicast message " + message
		    + " due to internal exception", t);
	    return;
	}
    }

    private void logAndThrowComExec(String method,
	    CommunicationConnectorErrorCode code, String msg, Throwable t) {
	LogUtils.logError(context, JGroupsCommunicationConnector.class, method,
		new Object[] { msg }, t);
	throw new CommunicationConnectorException(
		CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED, msg, t);

    }

    private void logAndThrowComExec(String method,
	    CommunicationConnectorErrorCode code, String msg) {
	LogUtils.logError(context, JGroupsCommunicationConnector.class, method,
		msg);
	throw new CommunicationConnectorException(
		CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED, msg);
    }

    public synchronized void multicast(ChannelMessage message)
	    throws CommunicationConnectorException {
	final String METHOD = "multicast";

	if (message.getChannelNames() == null) {
	    logAndThrowComExec(METHOD,
		    CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED,
		    "No channel name specified");
	    return;
	}
	// send message to all brokers of any kind
	List selectedChannel = selectJChannels(message.getChannelNames());
	for (int i = 0; i < selectedChannel.size(); i++) {
	    JChannel channel = (JChannel) selectedChannel.get(i);
	    Message msg = null;
	    String s = message.toString();

            try {
                if (security) {
                    msg = new Message(null, null, CryptUtil.encrypt(message
                            .toString()));
                } else {
                    msg = new Message(null, null, message.toString());
                }
            } catch (Throwable e) {
                logAndThrowComExec(METHOD,
                        CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                        "Error during cretaion of multicast message", e);
                return;
            }
            try {
                channel.send(msg);
            } catch (Throwable e) {
                logAndThrowComExec(METHOD,
                        CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                        "Sending broadcast message " + msg.toString(), e);
                return;
            }
        }


    }

    public synchronized void multicast(ChannelMessage message,
	    List<PeerCard> receivers) throws CommunicationConnectorException {
	final String METHOD = "multicast";
	final List channels = message.getChannelNames();
	if (channels == null) {
	    logAndThrowComExec(METHOD,
		    CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED,
		    "No channel name specified");
	    return;
	}

	List selectedChannel = selectJChannels(channels);

	if (selectedChannel == null || selectedChannel.isEmpty()) {
	    logAndThrowComExec(
		    METHOD,
		    CommunicationConnectorErrorCode.CHANNEL_NOT_FOUND,
		    "No destination channel found among the list :"
			    + Arrays.toString(channels.toArray())
			    + "They were either not configured or deleted");
	    return;
	}

	// TODO Add log message or error if some of the destination does not
	// exists on the channels

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
		    if (channel.getName(address).equals(removeAdressString))
			removeAddressList.add(address);
		}
	    }
	    //
	    if (security) {
		try {
		    msg = new Message(null, null, CryptUtil.encrypt(message
			    .toString()));
		} catch (Throwable t) {
		    logAndThrowComExec(
			    METHOD,
			    CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
			    "Failed to encrypt the message due to internal exception",
			    t);
		    return;
		}
	    } else {
		msg = new Message(null, null, message.toString());
	    }

	    RequestOptions opts = new RequestOptions();
	    opts.setExclusionList((Address[]) removeAddressList
		    .toArray(new Address[removeAddressList.size()]));
	    disp = new MessageDispatcher(channel, null, null, this);

	    try {
		disp.sendMessage(msg, opts);
	    } catch (Throwable e) {
		logAndThrowComExec(
			METHOD,
			CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
			"Unable to broadcast the message:" + message.toString(),
			e);
		return;
	    }
	}

    }

    public void loadConfigurations(Dictionary configurations) {
	final String METHOD = "loadConfigurations";
	LogUtils.logDebug(context, JGroupsCommunicationConnector.class, METHOD,
		"updating JGroups Connector properties");
	if (configurations == null) {
	    // TODO We should reset the configuration to the default properties
	    LogUtils.logDebug(context, JGroupsCommunicationConnector.class,
		    METHOD, "JGroups Connector properties are null");
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
		    .valueOf((String) configurations
			    .get(Consts.ENABLE_REMOTE_CHANNEL_CONFIG));
	    this.enableRemoteChannelURL = (String) configurations
		    .get(Consts.ENABLE_REMOTE_CHANNEL_URL_CONFIG);
	} catch (Throwable t) {
	    LogUtils.logError(context, JGroupsCommunicationConnector.class,
		    METHOD,
		    new Object[] { "Error during JGroups properties update" },
		    t);
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
	final String METHOD = "receive";
	try {
	    /*
	     * JGroups 2.2 only if (msg.isFlagSet(Flag.INTERNAL)) {
	     * LogUtils.logWarn(context, JGroupsCommunicationConnector.class,
	     * METHOD, "Skipping internal JGroups packet"); return; }
	     */
	    String msgBuffer = (String) msg.getObject();

	    if (security) {
		msgBuffer = CryptUtil.decrypt((String) msg.getObject());
	    }
	    ChannelMessage channelMessage = ChannelMessage
		    .unmarshall(msgBuffer);
	    communicationModule.messageReceived(channelMessage);
	} catch (Exception ex) {
	    LogUtils.logDebug(
		    context,
		    JGroupsCommunicationConnector.class,
		    METHOD,
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
	ChannelMessage channelMessage = ChannelMessage.unmarshall(new String(
		msg.getBuffer()));
	communicationModule.messageReceived(channelMessage);
	return null;
    }

    public boolean init() {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * Remember that group is the name of the broker, but the name of the channel is X+Y where:
     * X :brokerName
     * Y: AALSpaceID
     */
    public List<String> getGroupMembers(String groupName) {
	List<String> members = new ArrayList<String>();

	for(String channelName: channelMap.keySet()){
	    if(channelName.contains(groupName) && channelMap.get(channelName).getView() != null){
		List<Address> addresses = channelMap.get(channelName).getView()
			.getMembers();
		for (Address address : addresses) {
		    members.add(channelMap.get(channelName).getName(address));
		}
	    }	
	}

	return members;

    }

    public boolean hasChannel(String channelName) {
	
	for(String configuredChannelName: channelMap.keySet()){
	    if(configuredChannelName.contains(channelName))
		return true;
	}
	return false;
    }


}
