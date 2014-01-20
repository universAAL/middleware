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
package org.universAAL.middleware.modules.aalspace;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.brokers.Broker;
import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessage.BrokerMessageTypes;
import org.universAAL.middleware.brokers.message.BrokerMessageFields;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage.AALSpaceMessageTypes;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessageException;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessageFields;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceType;
import org.universAAL.middleware.modules.AALSpaceModule;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.ConfigurableCommunicationModule;
import org.universAAL.middleware.modules.exception.AALSpaceModuleErrorCode;
import org.universAAL.middleware.modules.exception.AALSpaceModuleException;
import org.universAAL.middleware.modules.listener.MessageListener;

import com.google.gson.Gson;

/**
 * Implementation of the AALSpaceModule
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public class AALSpaceModuleImpl implements AALSpaceModule, MessageListener,
        SharedObjectListener, ServiceListener, Broker {

    private static final String AALSpaceMessage = null;
    private String name;
    private String provider;
    private String version;
    private String description;
    private ModuleContext context;
    // Discovery Connectors
    private List<DiscoveryConnector> discoveryConnectors;
    // Communication Module
    private CommunicationModule communicationModule;
    // ControlBroler
    private ControlBroker controlBoker;
    private boolean initialized = false;
    // the Broker Name to use in order to send the messages with the correct
    // channel
    private String brokerName;

    /**
     * This method configures the AALSpaceModule: -to obtain the reference to
     * all the DiscoveryConnector present in the fw -to obtain the reference to
     * the CommunicationModdule -to obtain the reference to the ControlBroker
     *
     * @return true if initialized with the connectors and the module, false
     *         otherwise
     */
    public boolean init() {
        if (!initialized) {
            try {
                if (discoveryConnectors == null
                        || discoveryConnectors.isEmpty()) {

                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Fetching the DiscoveryConnector..." },
                            null);
                    Object[] dConnectors = context.getContainer()
                            .fetchSharedObject(
                                    context,
                                    new Object[] { DiscoveryConnector.class
                                            .getName() }, this);
                    if (dConnectors != null && dConnectors.length > 0) {
                        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl", new Object[] { "Found: "
                                        + dConnectors.length
                                        + " DiscoveryConnector" }, null);
                        // clear or init the list of connectors

                        discoveryConnectors = new ArrayList<DiscoveryConnector>();
                        for (Object ref : dConnectors) {
                            DiscoveryConnector dConnector = (DiscoveryConnector) ref;
                            dConnector.addAALSpaceListener(this);
                            discoveryConnectors.add((DiscoveryConnector) ref);
                        }
                        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl",
                                new Object[] { "DiscoveryConnectors fetched" },
                                null);
                    } else {
                        LogUtils.logWarn(context, AALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl",
                                new Object[] { "No DiscoveryConnector found" },
                                null);
                        initialized = false;
                        return initialized;
                    }
                }
                if (communicationModule == null) {
                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Fetching the CommunicationModule..." },
                            null);
                    Object cModule = context.getContainer().fetchSharedObject(
                            context,
                            new Object[] { CommunicationModule.class.getName()
                                    .toString() });
                    if (cModule != null) {
                        communicationModule = (CommunicationModule) cModule;
                        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl",
                                new Object[] { "CommunicationModule fetched" },
                                null);
                    } else {
                        LogUtils.logWarn(
                                context,
                                AALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl",
                                new Object[] { "No CommunicationModule found" },
                                null);
                        initialized = false;
                        return initialized;
                    }
                }

                if (controlBoker == null) {
                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Fetching the ControlBroker..." },
                            null);
                    Object cBroker = context.getContainer().fetchSharedObject(
                            context,
                            new Object[] { ControlBroker.class.getName()
                                    .toString() });
                    if (cBroker != null) {
                        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl",
                                new Object[] { "Found a ControlBroker" }, null);
                        controlBoker = (ControlBroker) cBroker;
                        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl",
                                new Object[] { "ControlBroker fetched" }, null);
                    } else {
                        LogUtils.logWarn(context, AALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl",
                                new Object[] { "No ControlBroker found" }, null);
                        initialized = false;
                        return initialized;
                    }
                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "ControlBroker found" }, null);
                    // DiscoveryConnector, CommunicationModule and ControlBroker
                    // have been found

                }
                initialized = true;
            } catch (NullPointerException e) {
                LogUtils.logError(
                        context,
                        AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Error while initializing the AALSpaceModule: "
                                + e }, null);
                initialized = false;
            } catch (ClassCastException e) {
                LogUtils.logError(
                        context,
                        AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Error while casting CommunicationConnector and CommunicationModule: "
                                + e }, null);
                initialized = false;
            }
        }
        if (initialized)
            LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "AALSpaceModule initialized" }, null);
        return initialized;
    }

    public AALSpaceModuleImpl(ModuleContext context) {
        this.context = context;
        discoveryConnectors = new ArrayList<DiscoveryConnector>();
    }

    public List<AALSpaceCard> getAALSpaces() {
        return this.getAALSpaces(null);
    }

    public List<AALSpaceCard> getAALSpaces(Dictionary<String, String> filters)
            throws AALSpaceModuleException {
        List<AALSpaceCard> spaces = new ArrayList<AALSpaceCard>();
        if (init()) {
            LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Searching for the AALSpace with filters: "
                            + filters.toString() + "..." }, null);

            try {
                for (DiscoveryConnector dConnector : discoveryConnectors) {
                    if (filters != null && filters.size() > 0)
                        spaces.addAll(dConnector.findAALSpace(filters));
                    else
                        spaces.addAll(dConnector.findAALSpace());
                }
            } catch (DiscoveryConnectorException e) {
                LogUtils.logError(
                        context,
                        AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Error during the AALSPace search:"
                                + e.toString() }, null);
                throw new AALSpaceModuleException(
                        AALSpaceModuleErrorCode.ERROR_INTERACTING_DISCOVERY_CONNECTORS,
                        e.toString());
            }
            LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Found: " + spaces.size() + " AALSpaces." },
                    null);

        } else {
            LogUtils.logWarn(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "AALSpaceModule cannot be initialized. Returning no AALSpaces" },
                    null);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.NO_DISCOVERY_CONNECTORS,
                    "AALSpaceModule cannot be initialized. Returning no AALSpaces");
        }
        return spaces;
    }

    public synchronized void newAALSpace(AALSpaceCard aalSpaceCard)
            throws AALSpaceModuleException {
        if (init()) {
            LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Creating a new AALSpace..." }, null);
            for (DiscoveryConnector connector : discoveryConnectors) {
                try {
                    connector.announceAALSpace(aalSpaceCard);

                } catch (DiscoveryConnectorException e) {
                    LogUtils.logError(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Error creating the AALSpace: "
                                    + aalSpaceCard.toString() + " due to: "
                                    + e.toString() }, null);
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_INTERACTING_DISCOVERY_CONNECTORS,
                            "Error creating the AALSpace: "
                                    + aalSpaceCard.toString() + " due to: "
                                    + e.toString());
                }
            }
        } else {
            LogUtils.logWarn(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "AALSpaceModule cannot be initialized. Returning no AALSpace" },
                    null);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.NO_DISCOVERY_CONNECTORS,
                    "AALSpaceModule cannot be initialized. Returning no AALSpaces");
        }
    }

    public String getDescription() {
        return description;
    }

    public String getProvider() {
        return provider;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void loadConfigurations(Dictionary configurations) {
        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                "AALSpaceModuleImpl",
                new Object[] { "updating AALSpaceModule properties" }, null);
        if (configurations == null) {
            LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "AALSpaceModule properties are null" }, null);
            return;
        }
        try {

            this.name = (String) configurations
                    .get(org.universAAL.middleware.modules.util.Consts.MODULE_NAME);
            this.version = (String) configurations
                    .get(org.universAAL.middleware.modules.util.Consts.MODULE_VERSION);
            this.description = (String) configurations
                    .get(org.universAAL.middleware.modules.util.Consts.MODULE_DESCRIPTION);
            this.provider = (String) configurations
                    .get(org.universAAL.middleware.modules.util.Consts.MODULE_PROVIDER);
        } catch (NumberFormatException e) {
            LogUtils.logError(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Error during AALSpaceModule properties update" },
                    null);
        } catch (NullPointerException e) {
            LogUtils.logError(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Error during AALSpaceModule properties update" },
                    null);
        } catch (Exception e) {
            LogUtils.logError(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Error during AALSpaceModule properties update" },
                    null);
        }
        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                "AALSpaceModuleImpl",
                new Object[] { "AALSpaceModule properties updated" }, null);

    }

    public void leaveAALSpace(PeerCard spaceCoordinator, AALSpaceCard spaceCard) {
        try {
            if (init()) {
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Creating a new AALSpaceMessage" }, null);
                // prepare the AALSpace Message...
                AALSpaceMessage spaceMessage = new AALSpaceMessage(
                        new AALSpaceDescriptor(spaceCard,
                                new ArrayList<ChannelDescriptor>()),
                        AALSpaceMessageTypes.LEAVE);
                // ...and wraps it as ChannelMessage
                List<String> channelName = new ArrayList<String>();
                channelName.add(getBrokerName());
                ChannelMessage channelMessage = new ChannelMessage(
                        controlBoker.getmyPeerCard(), spaceMessage.toString(),
                        channelName);

                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Sending Leave Request message..." },
                        null);
                communicationModule
                        .send(channelMessage, this, spaceCoordinator);
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Leave Request message sent." }, null);

            }
        } catch (CommunicationConnectorException e) {
            LogUtils.logError(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Error during the unicast send: " + e },
                    null);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                    "Error during the unicast send: " + e);
        }

    }

    public void requestToLeave(AALSpaceDescriptor spaceDescriptor) {
        try {
            if (init()) {
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Creating a new AALSpaceMessage" }, null);

                // prepare the AALSpace Message
                AALSpaceMessage spaceMessage = new AALSpaceMessage(
                        spaceDescriptor, AALSpaceMessageTypes.REQUEST_TO_LEAVE);
                // ...and wrap it as ChannelMessage
                List<String> channelName = new ArrayList<String>();
                channelName.add(getBrokerName());
                ChannelMessage channelMessage = new ChannelMessage(
                        controlBoker.getmyPeerCard(), spaceMessage.toString(),
                        channelName);

                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Sending Leave Request message..." },
                        null);
                communicationModule.sendAll(channelMessage, this);
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Leave Request message sent." }, null);

            }
        } catch (CommunicationConnectorException e) {
            LogUtils.logError(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Error during the unicast send: " + e },
                    null);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                    "Error during the unicast send: " + e);
        }

    }

    public void requestPeerCard(AALSpaceDescriptor spaceDescriptor,
            String peerAddress) {
        try {
            if (init()) {
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Creating a new AALSpaceMessage" }, null);

                // prepare the AALSpace Message
                AALSpaceMessage spaceMessage = new AALSpaceMessage(
                        spaceDescriptor, AALSpaceMessageTypes.REQUEST_PEERCARD);
                // ...and wrap it as ChannelMessage
                List<String> channelName = new ArrayList<String>();
                channelName.add(getBrokerName());
                ChannelMessage channelMessage = new ChannelMessage(
                        controlBoker.getmyPeerCard(), spaceMessage.toString(),
                        channelName);

                LogUtils.logDebug(
                        context,
                        AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Sending Request Peer Card message..." },
                        null);
                communicationModule.send(channelMessage, this, new PeerCard(
                        peerAddress, PeerRole.PEER));
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Request Peer Card sent." }, null);

            }
        } catch (CommunicationConnectorException e) {
            LogUtils.logError(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Error during the unicast send: " + e },
                    null);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                    "Error during the unicast send: " + e);
        }

    }

    public void newAALSpacesFound(Set<AALSpaceCard> spaceCards) {
        if (spaceCards != null) {
            if (controlBoker != null)
                controlBoker.newAALSpaceFound(spaceCards);
            else
                LogUtils.logWarn(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "NO control Broker found" }, null);
        } else
            LogUtils.logWarn(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "AALSpace card is null" }, null);
    }

    public synchronized void joinAALSpace(PeerCard spaceCoordinator,
            AALSpaceCard spaceCard) {

        if (spaceCoordinator != null && spaceCard != null) {
            LogUtils.logDebug(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Peer: " + spaceCoordinator.toString()
                            + " is joining the spaceCard: "
                            + spaceCard.toString() }, null);
            try {
                if (init()) {
                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Creating a new AALSpaceMessage" },
                            null);
                    // prepare the AALSpace Message

                    AALSpaceDescriptor spaceDesc = new AALSpaceDescriptor(
                            spaceCard, new ArrayList<ChannelDescriptor>());
                    AALSpaceMessage spaceMessage = new AALSpaceMessage(
                            spaceDesc, AALSpaceMessageTypes.JOIN_REQUEST);

                    // ...and wrap it as ChannelMessage
                    List<String> channelName = new ArrayList<String>();
                    channelName.add(getBrokerName());
                    ChannelMessage channelMessage = new ChannelMessage(
                            controlBoker.getmyPeerCard(),
                            spaceMessage.toString(), channelName);

                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Sending Join Request message..." },
                            null);
                    communicationModule.send(channelMessage, this,
                            spaceCoordinator);
                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Join Request message sent." }, null);

                }
            } catch (CommunicationConnectorException e) {
                LogUtils.logError(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Error during the unicast send: " + e },
                        null);
                throw new AALSpaceModuleException(
                        AALSpaceModuleErrorCode.AALSPACE_JOIN_ERROR,
                        "Error during the unicast send: " + e);
            }
        }
        // invalid parameters
        else {
            LogUtils.logError(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "PeerCard and/or SpaceCard are null" }, null);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_JOIN_WRONG_PARAMETERS,
                    "PeerCard and/or SpaceCard are null");
        }

    }

    public void addPeer(AALSpaceDescriptor spaceDescriptor, PeerCard newPeer) {
        if (spaceDescriptor != null && newPeer != null) {
            LogUtils.logDebug(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Peer: " + newPeer.toString()
                            + " is joining the spaceCard: "
                            + spaceDescriptor.toString() }, null);
            try {
                if (init()) {
                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Creating a new AALSpaceMessage" },
                            null);
                    // prepare the AALSpace Message

                    AALSpaceMessage spaceMessage = new AALSpaceMessage(
                            spaceDescriptor, AALSpaceMessageTypes.JOIN_RESPONSE);

                    // ...and wrap it as ChannelMessage
                    List<String> channelName = new ArrayList<String>();
                    channelName.add(getBrokerName());
                    ChannelMessage channelMessage = new ChannelMessage(
                            controlBoker.getmyPeerCard(),
                            spaceMessage.toString(), channelName);

                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Sending Join Request message..." },
                            null);
                    communicationModule.send(channelMessage, this, newPeer);
                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Join Request message queued." },
                            null);

                }
            } catch (CommunicationConnectorException e) {
                LogUtils.logError(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Error during the unicast send: " + e },
                        null);
                throw new AALSpaceModuleException(
                        AALSpaceModuleErrorCode.AALSPACE_JOIN_ERROR,
                        "Error during the unicast send: " + e);
            }
        }
        // invalid parameters
        else {
            LogUtils.logWarn(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "PeerCard and/or SpaceCard are null" }, null);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_JOIN_WRONG_PARAMETERS,
                    "PeerCard and/or SpaceCard are null");
        }

    }

    public void announceNewPeer(AALSpaceCard spaceCard, PeerCard peerCard) {
        if (spaceCard != null && peerCard != null) {
            LogUtils.logDebug(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Announcing new Peer: "
                            + peerCard.toString() + " into the AASpace: "
                            + spaceCard.toString() }, null);

            AALSpaceMessage spaceMessage = new AALSpaceMessage(
                    new AALSpaceDescriptor(spaceCard,
                            new ArrayList<ChannelDescriptor>()),
                    AALSpaceMessageTypes.NEW_PEER);

            // ...and wrap it as ChannelMessage
            List<String> channelName = new ArrayList<String>();
            channelName.add(getBrokerName());
            ChannelMessage channelMessage = new ChannelMessage(
                    controlBoker.getmyPeerCard(), spaceMessage.toString(),
                    channelName);

            LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Sending New Peer added message..." }, null);
            communicationModule.sendAll(channelMessage, this);
            LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "New Peer added message queued" }, null);

        } else {
            LogUtils.logWarn(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Event propagation failed! PeerCard and/or AALSpaceCard are not valid" },
                    null);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_NEW_PEER_ADDED_ERROR,
                    "Event propagation failed! PeerCard and/or AALSpaceCard are not valid");
        }

    }

    public void messageFromSpace(AALSpaceMessage message, PeerCard sender)
            throws AALSpaceModuleException {
        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                "AALSpaceModuleImpl",
                new Object[] { "AALSpaceMessage arrived...queuing" }, null);
        try {
            AALSpaceMessageTypes messageType = message.getMessageType();
            switch (messageType) {
            case JOIN_REQUEST: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() }, null);
                    return;
                }
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl", new Object[] { "Join request: "
                                + message.toString() }, null);
                controlBoker.joinRequest(message.getSpaceDescriptor()
                        .getSpaceCard(), sender);

            }
                break;
            case JOIN_RESPONSE: {

                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl", new Object[] { "Join response: "
                                + message.toString() }, null);
                if (message.getSpaceDescriptor() != null) {
                    controlBoker.aalSpaceJoined(message.getSpaceDescriptor());
                } else {
                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "The Join Response is not valid" },
                            null);
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.AALSPACE_JOIN_RESPONSE_WRONG_PARAMETERS,
                            "The Join Response is not valid");
                }

            }
                break;
            case NEW_PEER: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() }, null);
                    return;
                }
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl", new Object[] { "New Peer added: "
                                + message.toString() }, null);
                if (sender != null) {
                    controlBoker.peerFound(sender);
                } else {
                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "The New Peer added has not a valid PeerCard" },
                            null);
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.AALSPACE_NEW_PEER_ERROR,
                            "The New Peer added has not a valid PeerCard");
                }

            }
                break;
            case LEAVE: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() }, null);
                    return;
                }
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl", new Object[] { "Leave request: "
                                + message.toString() }, null);
                if (sender != null) {
                    controlBoker.peerLost(sender);
                } else {
                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Leaving Peer  has not a valid PeerCard" },
                            null);
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                            "The leaving Peer has not a valid PeerCard");
                }
            }
                break;
            case REQUEST_TO_LEAVE: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() }, null);
                    return;
                }
                LogUtils.logDebug(
                        context,
                        AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Request to leave: "
                                + message.toString() }, null);
                if (message.getSpaceDescriptor() != null) {
                    controlBoker.leaveRequest(message.getSpaceDescriptor());
                } else {
                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Not a valid AALSpace descriptor" },
                            null);
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                            "No valid space descritpro");
                }

            }
                break;
            case REQUEST_PEERCARD: {
                LogUtils.logDebug(
                        context,
                        AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Request PeerCard: "
                                + message.toString() }, null);
                // check if everything is correct
                if (controlBoker.getmyAALSpaceDescriptor() == null) {
                    LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "AALSpaceDescritor not yet ready.. "
                                    + message.toString() }, null);
                    return;

                }

                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Not part of the AALSpace requester... "
                                    + message.toString() }, null);
                    return;

                }

                // send my PeerCard
                AALSpaceMessage spaceMessage = new AALSpaceMessage(
                        controlBoker.getmyAALSpaceDescriptor(),
                        AALSpaceMessageTypes.PEERCARD);

                // ...and wrap it as ChannelMessage
                List<String> channelName = new ArrayList<String>();
                channelName.add(getBrokerName());
                ChannelMessage channelMessage = new ChannelMessage(
                        controlBoker.getmyPeerCard(), spaceMessage.toString(),
                        channelName);

                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Sending Peer Card ..." }, null);
                communicationModule.send(channelMessage, this, sender);
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Peer Card message sent." }, null);
            }
                break;
            case PEERCARD: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    LogUtils.logDebug(
                            context,
                            AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() }, null);
                    return;
                }
                LogUtils.logDebug(
                        context,
                        AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "PeerCard received: "
                                + message.toString() }, null);
                // check if it is part of my aalspacde

                controlBoker.peerFound(sender);

            }
            default:
                break;
            }

        } catch (ClassCastException e) {
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.ERROR_MANAGING_AALSPACE_MESSAGE,
                    "The message body is not valid: " + e);
        }

        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                "AALSpaceModuleImpl",
                new Object[] { "AALSpaceMessage queued" }, null);
    }

    public void configureAALSpaceChannel(String group) {
        LogUtils.logDebug(
                context,
                AALSpaceModuleImpl.class,
                "AALSpaceModuleImpl",
                new Object[] { "Setting the broker group for the AALSpaceModule..."
                        + group }, null);
        this.brokerName = group;
        /*
         * Register me as MessageListener for messages to the channel associated
         * to my broker group
         */
        communicationModule.addMessageListener(this, group);

    }

    public void messageReceived(ChannelMessage message) {
        if (message == null || message.getContent() == null) {
            LogUtils.logDebug(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "The message received is not valid...dropping it." },
                    null);
            return;
        }
        try {

            BrokerMessage brokerMessage = null;
            try {
                brokerMessage = unmarshall(message.getContent());
            } catch (Exception e) {
                LogUtils.logError(
                        context,
                        AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "The message received is not valid...dropping it." },
                        null);
            }

            switch (brokerMessage.getMType()) {
            case AALSpaceMessage:
                AALSpaceMessage spaceMessage = (AALSpaceMessage) unmarshall(message
                        .getContent());
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "AALSpace message arrived" }, null);
                messageFromSpace(spaceMessage, message.getSender());
                break;

            }
        } catch (Exception e) {
            LogUtils.logError(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Error during message receive: "
                            + e.toString() }, null);
        }

    }

    public void handleSendError(ChannelMessage message,
            CommunicationConnectorException exception)
            throws AALSpaceModuleException {
        try {
            LogUtils.logWarn(context, AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "Error during message queuing for message: "
                            + message.toString() }, null);
            AALSpaceModuleException spaceException;

            if (message != null && message.getContent() != null) {
                // initialize the exception and throw it
                AALSpaceMessage aMessage = (AALSpaceMessage) unmarshall(message
                        .getContent());
                switch (aMessage.getMessageType()) {
                case JOIN_REQUEST:
                    spaceException = new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_SENDING_JOIN_REQUEST,
                            exception.toString());
                    break;
                case JOIN_RESPONSE:
                    spaceException = new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_SENDING_JOIN_RESPONSE,
                            exception.toString());
                    break;
                case NEW_PEER:
                    spaceException = new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_SENDING_NEW_PEER_ADDED,
                            exception.toString());
                    break;
                default:
                    spaceException = new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_MANAGING_AALSPACE_MESSAGE,
                            exception.toString());
                    break;
                }
                throw spaceException;
            }
        } catch (AALSpaceMessageException e) {
            LogUtils.logError(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "The message received is not valid...dropping it."
                            + e.toString() }, null);
        } catch (Exception e2) {

            LogUtils.logError(
                    context,
                    AALSpaceModuleImpl.class,
                    "AALSpaceModuleImpl",
                    new Object[] { "The message received is not valid...dropping it."
                            + e2.toString() }, null);
        }
    }

    public void destroyAALSpace(AALSpaceCard spaceCard) {
        LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                "AALSpaceModuleImpl", new Object[] { "Destroy the AALSpace: "
                        + spaceCard.toString() }, null);
        // to de-register the AALSpace
        for (DiscoveryConnector dConnector : discoveryConnectors) {
            try {
                dConnector.deregisterAALSpace(spaceCard);
            } catch (Exception e) {
                LogUtils.logError(
                        context,
                        AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Error during destroy AALSpace: "
                                + e.toString() }, null);
            }
        }

    }

    public void aalSpaceLost(AALSpaceCard spaceCard) {
        // TODO Auto-generated method stub

    }

    public void sharedObjectAdded(Object arg0, Object arg1) {
        if (arg0 != null) {
            if (arg0 instanceof DiscoveryConnector) {

                DiscoveryConnector connector = (DiscoveryConnector) arg0;
                // check if I already have the same connector
                if (!discoveryConnectors.contains(connector)) {
                    connector.addAALSpaceListener(this);
                    discoveryConnectors.add(connector);
                }
            } else if (arg0 instanceof CommunicationModule) {
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "New CommunicationModule added..." },
                        null);
                communicationModule = (CommunicationModule) arg0;
            } else if (arg0 instanceof ControlBroker) {
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "ControkBroker added..." }, null);
                controlBoker = (ControlBroker) arg0;
            }
        }
    }

    public void sharedObjectRemoved(Object arg0) {
        if (arg0 != null) {
            if (arg0 instanceof DiscoveryConnector) {
                DiscoveryConnector connector = (DiscoveryConnector) arg0;
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Removing the DiscoveryConnector" },
                        null);
                discoveryConnectors.remove(connector);
                if (discoveryConnectors.size() == 0) {
                    initialized = false;
                }
            } else if (arg0 instanceof CommunicationModule) {
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "CommunicationModule removed..." }, null);
                communicationModule = null;
                initialized = false;
            } else if (arg0 instanceof ControlBroker) {
                LogUtils.logDebug(context, AALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "ControlBroker removed..." }, null);
                controlBoker = null;
                initialized = false;
            }
        }

    }

    public String getBrokerName() {
        return context.getID();
    }

    public void dispose() {
        // remove me as listener
        context.getContainer().removeSharedObjectListener(this);
        if (communicationModule != null)
            communicationModule.removeMessageListener(this, brokerName);
        if (discoveryConnectors != null && discoveryConnectors.size() > 0) {
            for (DiscoveryConnector dConnector : discoveryConnectors) {
                dConnector.removeAALSpaceListener(this);
            }
        }

    }

    public List<String> getPeersAddress() {
        if (communicationModule instanceof ConfigurableCommunicationModule) {
            ConfigurableCommunicationModule cCommMode = (ConfigurableCommunicationModule) communicationModule;
            Map<String, PeerCard> checkedPeer = new HashMap<String, PeerCard>();
            List<String> members = cCommMode.getGroupMembers(brokerName);
            return members;
        }
        return null;
    }

    public void renewAALSpace(AALSpaceCard spaceCard) {
        if (spaceCard != null) {
            for (DiscoveryConnector discoveryConnector : discoveryConnectors) {
                try {
                    discoveryConnector.announceAALSpace(spaceCard);
                } catch (DiscoveryConnectorException e) {
                    LogUtils.logError(context, AALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "error during AALSpace renew: "
                                    + spaceCard.toString() }, null);
                }
            }
        }

    }

    public BrokerMessage unmarshall(String message) {
        try {

            return GsonParserBuilder.getInstance().buildGson()
                    .fromJson(message, AALSpaceMessage.class);

        } catch (Exception e) {

            throw new AALSpaceMessageException(
                    "Unable to unmashall AALSpaceMessage. Original message: "
                            + message + ". Full Stack: " + e.toString());
        }

    }

}
