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
package org.universAAL.middleware.brokers.control;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.brokers.Broker;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessageFields;

import org.universAAL.middleware.brokers.message.configuration.ConfigurationMessage;
import org.universAAL.middleware.brokers.message.configuration.ConfigurationMessage.ConfigurationMessageType;
import org.universAAL.middleware.brokers.message.control.ControlMessage;
import org.universAAL.middleware.brokers.message.deploy.DeployMessage;
import org.universAAL.middleware.brokers.message.deploy.DeployMessage.DeployMessageType;
import org.universAAL.middleware.brokers.message.deploy.DeployMessageException;

import org.universAAL.middleware.brokers.message.deploy.DeployNotificationPayload;
import org.universAAL.middleware.brokers.message.deploy.DeployPayload;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;

import org.universAAL.middleware.connectors.DeployConnector;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceStatus;

import org.universAAL.middleware.interfaces.mpa.UAPPCard;
import org.universAAL.middleware.interfaces.mpa.UAPPPartStatus;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.ConfigurationManagerConnector;
import org.universAAL.middleware.managers.api.DeployManager;
import org.universAAL.middleware.managers.api.DeployManagerEventHandler;
import org.universAAL.middleware.modules.AALSpaceModule;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.ConfigurableCommunicationModule;
import org.universAAL.middleware.modules.listener.MessageListener;

import com.google.gson.Gson;

/**
 * The Control Broker
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class ControlBroker implements SharedObjectListener, Broker,
	MessageListener {

    private ModuleContext context;
    private AALSpaceModule aalSpaceModule;
    private ConfigurableCommunicationModule communicationModule;
    private AALSpaceEventHandler aalSpaceEventHandler;
    private AALSpaceManager aalSpaceManager;
    private DeployManager deployManager;
    private DeployConnector deployConnector;
    private ConfigurationManagerConnector configConnector;
    private boolean initialized = false;
    private HashMap<String, WaitForResponse> openTransaction = new HashMap<String, WaitForResponse>();

    private static String TMP_DEPLOY_FOLDER = "etc" + File.separatorChar
	    + "tmp" + File.separatorChar + "installations" + File.separatorChar;

    private class Response {
	ControlMessage msg;
	PeerCard sender;
    }

    public ControlBroker(ModuleContext context) {
	this.context = context;
	init();

    }

    private CommunicationModule getCommunicationModule() {
	LogUtils.logDebug(context, ControlBroker.class,
		"getCommunicationModule",
		new Object[] { "Fetching the CommunicationModule..." }, null);
	Object[] refs = context.getContainer()
		.fetchSharedObject(
			context,
			new Object[] { CommunicationModule.class.getName()
				.toString() }, this);
	if (refs != null && refs[0] instanceof ConfigurableCommunicationModule) {
	    communicationModule = (ConfigurableCommunicationModule) refs[0];
	    LogUtils.logDebug(context, ControlBroker.class,
		    "getCommunicationModule",
		    new Object[] { "CommunicationModule fetched" }, null);
	} else {
	    LogUtils.logWarn(context, ControlBroker.class,
		    "getCommunicationModule",
		    new Object[] { "No CommunicationModule found" }, null);
	}
	return communicationModule;
    }

    private AALSpaceEventHandler getAALSpaceEventHandler() {
	LogUtils.logTrace(context, ControlBroker.class,
		"getAALSpaceEventHandler",
		new Object[] { "Fetching the AALSpaceEventHandler..." }, null);
	Object[] refs = context.getContainer()
		.fetchSharedObject(
			context,
			new Object[] { AALSpaceEventHandler.class.getName()
				.toString() }, this);
	if (refs != null) {
	    LogUtils.logTrace(context, ControlBroker.class,
		    "getAALSpaceEventHandler",
		    new Object[] { "AALSpaceEventHandler found!" }, null);
	    aalSpaceEventHandler = (AALSpaceEventHandler) refs[0];

	} else {
	    LogUtils.logDebug(context, ControlBroker.class,
		    "getAALSpaceEventHandler",
		    new Object[] { "No AALSpaceEventHandler found" }, null);
	    return null;
	}
	return aalSpaceEventHandler;
    }

    private AALSpaceManager getAALSpaceManager() {
	LogUtils.logDebug(context, ControlBroker.class, "getAALSpaceManager",
		new Object[] { "Fetching the AALSpaceManager..." }, null);
	Object[] refs = context.getContainer().fetchSharedObject(context,
		new Object[] { AALSpaceManager.class.getName().toString() },
		this);
	if (refs != null) {
	    LogUtils.logDebug(context, ControlBroker.class,
		    "getAALSpaceManager",
		    new Object[] { "AALSpaceManager found!" }, null);
	    aalSpaceManager = (AALSpaceManager) refs[0];

	} else {
	    LogUtils.logDebug(context, ControlBroker.class,
		    "getAALSpaceManager",
		    new Object[] { "No AALSpaceManager found" }, null);
	}
	return aalSpaceManager;
    }

    private AALSpaceModule getAALSpaceModule() {
	LogUtils.logDebug(context, ControlBroker.class, "getAALSpaceModule",
		new Object[] { "Fetching the AALSpaceModule..." }, null);
	Object[] refs = context.getContainer().fetchSharedObject(context,
		new Object[] { AALSpaceModule.class.getName().toString() },
		this);
	if (refs != null) {

	    LogUtils.logDebug(context, ControlBroker.class,
		    "getAALSpaceModule",
		    new Object[] { "AALSpaceModule found!" }, null);
	    aalSpaceModule = (AALSpaceModule) refs[0];

	} else {
	    LogUtils.logWarn(context, ControlBroker.class, "getAALSpaceModule",
		    new Object[] { "No AALSpaceModule found" }, null);
	}
	return aalSpaceModule;

    }

    private ConfigurationManagerConnector getConfiguratorManagerConnector() {
	LogUtils.logDebug(
		context,
		ControlBroker.class,
		"getConfiguratorManagerConnector",
		new Object[] { "Fetching the ConfigurationManagerConnector..." },
		null);
	Object[] refs = context.getContainer().fetchSharedObject(
		context,
		new Object[] { ConfigurationManagerConnector.class.getName()
			.toString() }, this);
	if (refs != null) {

	    LogUtils.logDebug(context, ControlBroker.class,
		    "getConfiguratorManagerConnector",
		    new Object[] { "ConfigurationManagerConnector found!" },
		    null);
	    configConnector = (ConfigurationManagerConnector) refs[0];

	} else {
	    LogUtils.logWarn(context, ControlBroker.class,
		    "getConfiguratorManagerConnector",
		    new Object[] { "No ConfigurationManagerConnector found" },
		    null);
	}
	return configConnector;

    }

    public boolean init() {
	if (!initialized) {

	    /*
	     * TODO: why not use the getAALSpaceModule() and the rest of
	     * methods? something like: if (getAALSpaceModule() == null ||
	     * getAALSpaceEventHandler() == null || getAALSpaceManager() == null
	     * || getCommunicationModule() == null ||
	     * getConfigurationManagerConnector == null){ initialized = false;
	     * return initialized; } getDeployManager(); getDeployConnector();
	     */

	    LogUtils.logDebug(context, ControlBroker.class, "init",
		    new Object[] { "Fetching the AALSpaceModule..." }, null);
	    Object[] refs = context.getContainer().fetchSharedObject(context,
		    new Object[] { AALSpaceModule.class.getName().toString() },
		    this);
	    if (refs != null) {

		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "AALSpaceModule found!" }, null);
		aalSpaceModule = (AALSpaceModule) refs[0];
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "AALSpaceModule fetched" }, null);
	    } else {
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "No AALSpaceModule found" }, null);
		initialized = false;
		return initialized;
	    }

	    LogUtils.logDebug(context, ControlBroker.class, "init",
		    new Object[] { "Fetching the AALSpaceEventHandler..." },
		    null);
	    refs = context.getContainer().fetchSharedObject(
		    context,
		    new Object[] { AALSpaceEventHandler.class.getName()
			    .toString() }, this);
	    if (refs != null) {

		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "AALSpaceEventHandler found!" }, null);
		aalSpaceEventHandler = (AALSpaceEventHandler) refs[0];

	    } else {
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "No AALSpaceEventHandler" }, null);
		initialized = false;
		return initialized;
	    }

	    LogUtils.logDebug(context, ControlBroker.class, "init",
		    new Object[] { "Fetching the AALSpaceManager..." }, null);
	    refs = context.getContainer()
		    .fetchSharedObject(
			    context,
			    new Object[] { AALSpaceManager.class.getName()
				    .toString() }, this);
	    if (refs != null) {

		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "AALSpaceManager found!" }, null);
		aalSpaceManager = (AALSpaceManager) refs[0];

	    } else {
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "No AALSpaceManager" }, null);
		initialized = false;
		return initialized;
	    }

	    LogUtils.logDebug(context, ControlBroker.class, "init",
		    new Object[] { "Fetching the CommunicationModule..." },
		    null);
	    refs = context.getContainer().fetchSharedObject(
		    context,
		    new Object[] { CommunicationModule.class.getName()
			    .toString() }, this);
	    if (refs != null
		    && refs[0] instanceof ConfigurableCommunicationModule) {
		communicationModule = (ConfigurableCommunicationModule) refs[0];
		communicationModule.addMessageListener(this, getBrokerName());
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "CommunicationModule fetched" }, null);
	    } else {
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "No CommunicationModule found" }, null);
		initialized = false;
		return initialized;
	    }

	    LogUtils.logDebug(
		    context,
		    ControlBroker.class,
		    "init",
		    new Object[] { "Fetching the ConfigurationManagerConnector..." },
		    null);
	    refs = context.getContainer().fetchSharedObject(
		    context,
		    new Object[] { ConfigurationManagerConnector.class
			    .getName().toString() }, this);
	    if (refs != null
		    && refs[0] instanceof ConfigurationManagerConnector) {
		configConnector = (ConfigurationManagerConnector) refs[0];
		LogUtils.logDebug(
			context,
			ControlBroker.class,
			"init",
			new Object[] { "ConfigurationManagerConnector fetched" },
			null);
	    } else {
		LogUtils.logDebug(
			context,
			ControlBroker.class,
			"init",
			new Object[] { "No ConfigurationManagerConnector found" },
			null);
		initialized = false;
		return initialized;
	    }

	    LogUtils.logDebug(context, ControlBroker.class, "init",
		    new Object[] { "Fetching the DeployManager..." }, null);
	    refs = context.getContainer().fetchSharedObject(context,
		    new Object[] { DeployManager.class.getName().toString() },
		    this);
	    if (refs != null) {

		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "DeployManager found!" }, null);
		deployManager = (DeployManager) refs[0];
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "DeployManager fetched" }, null);
	    } else {
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "No DeployManager found" }, null);
	    }

	    LogUtils.logDebug(context, ControlBroker.class, "init",
		    new Object[] { "Fetching the DeployConnector..." }, null);
	    refs = context.getContainer()
		    .fetchSharedObject(
			    context,
			    new Object[] { DeployConnector.class.getName()
				    .toString() }, this);
	    if (refs != null) {

		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "DeployConnector found!" }, null);
		deployConnector = (DeployConnector) refs[0];
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "DeployConnector fetched" }, null);
	    } else {
		LogUtils.logDebug(context, ControlBroker.class, "init",
			new Object[] { "No DeployConnector found" }, null);
	    }

	}
	initialized = true;
	return initialized;

    }

    public List<AALSpaceCard> discoverAALSpace(
	    Dictionary<String, String> filters) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "discoverAALSpace",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return null;
	}
	return aalSpaceModule.getAALSpaces(filters);
    }

    public void buildAALSpace(AALSpaceCard aalSpaceCard) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "buildAALSpace",
		    new Object[] { "ControlBroker not initialized. " }, null);
	    return;
	}
	aalSpaceModule.newAALSpace(aalSpaceCard);
    }

    public void sharedObjectAdded(Object arg0, Object arg1) {
	if (arg0 != null && arg0 instanceof AALSpaceModule) {
	    LogUtils.logDebug(context, ControlBroker.class,
		    "sharedObjectAdded",
		    new Object[] { "AALSpaceModule registered..." }, null);
	    aalSpaceModule = (AALSpaceModule) arg0;
	}
	if (arg0 != null && arg0 instanceof CommunicationModule) {
	    LogUtils.logDebug(context, ControlBroker.class,
		    "sharedObjectAdded",
		    new Object[] { "CommunicationModule registered..." }, null);
	    if (communicationModule instanceof ConfigurableCommunicationModule)
		communicationModule = (ConfigurableCommunicationModule) arg0;
	    communicationModule.addMessageListener(this, getBrokerName());

	}

	if (arg0 != null && arg0 instanceof AALSpaceManager) {
	    LogUtils.logDebug(context, ControlBroker.class,
		    "sharedObjectAdded",
		    new Object[] { "AALSpaceManager registered..." }, null);
	    aalSpaceManager = (AALSpaceManager) arg0;

	}

	if (arg0 != null && arg0 instanceof AALSpaceEventHandler) {
	    LogUtils.logDebug(context, ControlBroker.class,
		    "sharedObjectAdded",
		    new Object[] { "AALSpaceEventHandler registered..." }, null);
	    aalSpaceEventHandler = (AALSpaceEventHandler) arg0;
	}
	if (arg0 != null && arg0 instanceof DeployManager) {
	    LogUtils.logDebug(context, ControlBroker.class,
		    "sharedObjectAdded",
		    new Object[] { "DeployManager registered..." }, null);
	    deployManager = (DeployManager) arg0;
	}
	if (arg0 != null && arg0 instanceof DeployConnector) {
	    LogUtils.logDebug(context, ControlBroker.class,
		    "sharedObjectAdded",
		    new Object[] { "DeployConnector registered..." }, null);
	    deployConnector = (DeployConnector) arg0;
	}
	if (arg0 != null && arg0 instanceof ConfigurationManagerConnector) {
	    LogUtils.logDebug(
		    context,
		    ControlBroker.class,
		    "sharedObjectAdded",
		    new Object[] { "ConfigurationManagerConnector registered..." },
		    null);
	    configConnector = (ConfigurationManagerConnector) arg0;
	}
    }

    public void sharedObjectRemoved(Object arg0) {
	if (arg0 instanceof AALSpaceEventHandler) {
	    LogUtils.logInfo(context, ControlBroker.class,
		    "sharedObjectRemoved",
		    new Object[] { "AALSpaceEventHandler unregistered!" }, null);
	    aalSpaceEventHandler = null;
	    initialized = false;
	} else if (arg0 instanceof AALSpaceManager) {
	    LogUtils.logInfo(context, ControlBroker.class,
		    "sharedObjectRemoved",
		    new Object[] { "AALSpaceManager unregistered!" }, null);
	    aalSpaceManager = null;
	    initialized = false;
	} else if (arg0 instanceof DeployManager) {
	    LogUtils.logInfo(context, ControlBroker.class,
		    "sharedObjectRemoved",
		    new Object[] { "DeployManager unregistered!" }, null);
	    deployManager = null;
	    initialized = false;

	} else if (arg0 instanceof AALSpaceModule) {
	    LogUtils.logInfo(context, ControlBroker.class,
		    "sharedObjectRemoved",
		    new Object[] { "AALSpaceModule unregistered!" }, null);
	    aalSpaceModule = null;
	    initialized = false;

	} else if (arg0 instanceof CommunicationModule) {
	    LogUtils.logInfo(context, ControlBroker.class,
		    "sharedObjectRemoved",
		    new Object[] { "CommunicationModule unregistered!" }, null);
	    try {
		communicationModule
			.removeMessageListener(this, getBrokerName());
	    } catch (Exception e) {

	    }
	    communicationModule = null;
	    initialized = false;
	} else if (arg0 instanceof DeployConnector) {
	    LogUtils.logInfo(context, ControlBroker.class,
		    "sharedObjectRemoved",
		    new Object[] { "DeployConnector unregistered!" }, null);
	    deployConnector = null;
	    initialized = false;
	}

    }

    public void joinRequest(AALSpaceCard spaceCard, PeerCard sender) {
	if (getAALSpaceEventHandler() == null) {
	    LogUtils.logWarn(
		    context,
		    ControlBroker.class,
		    "joinRequest",
		    new Object[] { "ControlBroker not initialized. Join aborted" },
		    null);
	    return;
	}
	aalSpaceEventHandler.joinRequest(spaceCard, sender);

    }

    public void leaveRequest(AALSpaceDescriptor spaceDescriptor) {
	if (getAALSpaceEventHandler() == null) {
	    LogUtils.logWarn(
		    context,
		    ControlBroker.class,
		    "leaveRequest",
		    new Object[] { "ControlBroker not initialized. Leave aborted" },
		    null);
	    return;
	}
	aalSpaceEventHandler.leaveRequest(spaceDescriptor);
    }

    public void requestToLeave(AALSpaceDescriptor spaceDescriptor) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(
		    context,
		    ControlBroker.class,
		    "requestToLeave",
		    new Object[] { "ControlBroker not initialized. Request to leave aborted" },
		    null);
	    return;
	}
	aalSpaceModule.requestToLeave(spaceDescriptor);
    }

    public void peerLost(PeerCard sender) {
	if (getAALSpaceEventHandler() == null) {
	    LogUtils.logWarn(
		    context,
		    ControlBroker.class,
		    "peerLost",
		    new Object[] { "ControlBroker not initialized. Peer Lost message aborted" },
		    null);
	    return;
	}
	aalSpaceEventHandler.peerLost(sender);

    }

    public void join(PeerCard spaceCoordinator, AALSpaceCard spaceCard) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(
		    context,
		    ControlBroker.class,
		    "join",
		    new Object[] { "ControlBroker not initialized. Join message aborted" },
		    null);
	    return;
	}
	aalSpaceModule.joinAALSpace(spaceCoordinator, spaceCard);
    }

    /**
     * This method returns the PeerCard of the current MW instance
     * 
     * @return PeerCard
     */
    public PeerCard getmyPeerCard() {
	if (getAALSpaceManager() == null) {
	    LogUtils.logWarn(
		    context,
		    ControlBroker.class,
		    "getmyPeerCard",
		    new Object[] { "ControlBroker not initialized. Fetching the PeerCard aborted" },
		    null);
	    return null;
	}
	return aalSpaceManager.getMyPeerCard();
    }

    /**
     * This method returns the AALSpaceDescriptor of my AALSpace
     * 
     * @return
     */
    public AALSpaceDescriptor getmyAALSpaceDescriptor() {
	if (getAALSpaceManager() == null) {
	    LogUtils.logWarn(
		    context,
		    ControlBroker.class,
		    "getmyAALSpaceDescriptor",
		    new Object[] { "ControlBroker not initialized. Fetching the PeerCard aborted" },
		    null);
	    return null;
	}
	return aalSpaceManager.getAALSpaceDescriptor();
    }

    public void aalSpaceJoined(AALSpaceDescriptor descriptor) {
	if (getAALSpaceEventHandler() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "aalSpaceJoined",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	aalSpaceEventHandler.aalSpaceJoined(descriptor);
    }

    public void peerFound(PeerCard peer) {
	if (getAALSpaceEventHandler() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "peerFound",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	aalSpaceEventHandler.peerFound(peer);
    }

    public void newAALSpaceFound(Set<AALSpaceCard> spaceCards) {
	if (getAALSpaceEventHandler() == null) {
	    return;
	}
	aalSpaceEventHandler.newAALSpacesFound(spaceCards);

    }

    /**
     * Only configures the communication channels by creating a list of channels
     * for the Communication Module
     * 
     * @param communicationChannels
     */
    public void configureChannels(
	    List<ChannelDescriptor> communicationChannels, String peerName) {
	if (getCommunicationModule() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "configureChannels",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	communicationModule.addMessageListener(this, this.getBrokerName());
	communicationModule.configureChannels(communicationChannels, peerName);

    }

    /**
     * Configures the peering channel by configuring the AALSpaceModule and by
     * creating a new channel for the Communication Module
     * 
     * @param peeringChannel
     */
    public void configurePeeringChannel(ChannelDescriptor peeringChannel,
	    String peerName) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(context, ControlBroker.class,
		    "configurePeeringChannel",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	aalSpaceModule
		.configureAALSpaceChannel(peeringChannel.getChannelName());
	List<ChannelDescriptor> channel = new ArrayList<ChannelDescriptor>();
	channel.add(peeringChannel);
	configureChannels(channel, peerName);

    }

    public void resetModule(List<ChannelDescriptor> channels) {
	if (getCommunicationModule() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "resetModule",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	communicationModule.dispose(channels);
    }

    public void destroyAALSpace(AALSpaceCard spaceCard) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "destroyAALSpace",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	aalSpaceModule.destroyAALSpace(spaceCard);
    }

    public void leaveAALSpace(PeerCard spaceCoordinator, AALSpaceCard spaceCard) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "leaveAALSpace",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	aalSpaceModule.leaveAALSpace(spaceCoordinator, spaceCard);
    }

    public void addNewPeer(AALSpaceDescriptor spaceDescriptor, PeerCard peer) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "addNewPeer",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	aalSpaceModule.addPeer(spaceDescriptor, peer);
    }

    public void newPeerAdded(AALSpaceCard spaceCard, PeerCard peer) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(context, ControlBroker.class, "newPeerAdded",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	aalSpaceModule.announceNewPeer(spaceCard, peer);
    }

    /**
     * This method allows to request the installation of an uApp part to a
     * target node
     * 
     * @param target
     *            The node into which to install the part
     * @param card
     *            The reference information of the part of the application
     *            within a service to install
     */
    public void requestToUninstallPart(PeerCard target, UAPPCard card) {
	final String METHOD = "requestToUninstallPart";
	if (!init()) {
	    LogUtils.logWarn(context, ControlBroker.class, METHOD,
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}

	// I'm the target node install the part locally
	if (target.getPeerID().equals(
		aalSpaceManager.getMyPeerCard().getPeerID())) {
	    // TODO Handle local uninstallation
	    deployConnector.uninstallPart(card);
	} else {
	    // Send the message to the target node

	    DeployPayload payload = new DeployPayload(new byte[] {}, card);
	    DeployMessage deployMessage = new DeployMessage(
		    DeployMessageType.REQUEST_TO_UNINSTALL_PART, payload);

	    // ...and wrap it as ChannelMessage
	    List<String> channelName = new ArrayList<String>();
	    channelName.add(getBrokerName());
	    ChannelMessage channelMessage = new ChannelMessage(getmyPeerCard(),
		    deployMessage.toString(), channelName);
	    communicationModule.send(channelMessage, this, target);
	}
    }

    /**
     * This method allows to request the installation of an uApp part to a
     * target node
     * 
     * @param partAsZip
     *            The part serialized as a String. The payload of the
     *            DeployMessage has to be a string
     * @param target
     *            The node into which to install the part
     * @param card
     *            The reference information of the part of the application
     *            within a service to install
     */
    public void requestToInstallPart(byte[] partAsZip, PeerCard target,
	    UAPPCard card) {
	final String METHOD = "requestToInstallPart";
	if (!init()) {
	    LogUtils.logWarn(context, ControlBroker.class, METHOD,
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}

	// I'm the target node install the part locally
	if (target.getPeerID().equals(
		aalSpaceManager.getMyPeerCard().getPeerID())) {
	    File file = null, root = null;
	    root = new File(TMP_DEPLOY_FOLDER);
	    String dst = null;
	    try {
		file = File.createTempFile("part", "install.zip", root);
		dst = file.getAbsolutePath();
	    } catch (IOException e) {
		LogUtils.logError(
			context,
			ControlBroker.class,
			METHOD,
			new Object[] { "Unable to generate a valid tmp filename with createTempFile() method using timestamp" },
			null);
		dst = TMP_DEPLOY_FOLDER + System.currentTimeMillis() + ".part";
	    }
	    file = FileUtils.createFileFromByte(context, partAsZip, dst, true);
	    if (file == null) {
		LogUtils.logError(
			context,
			ControlBroker.class,
			METHOD,
			new Object[] { "Error while installing artifact locally: unable to create file" },
			null);
		return;
	    }
	    deployConnector.installPart(file, card);
	    file.delete();
	    file.deleteOnExit();
	    file = null;
	} else {
	    // Send the message to the target node

	    DeployPayload payload = new DeployPayload(partAsZip, card);
	    DeployMessage deployMessage = new DeployMessage(
		    DeployMessageType.REQUEST_TO_INSTALL_PART, payload);

	    // ...and wrap it as ChannelMessage
	    List<String> channelName = new ArrayList<String>();
	    channelName.add(getBrokerName());
	    // TODO we should send a part in small pieces or by means of
	    // out-of-band protocol which support streaming
	    ChannelMessage channelMessage = new ChannelMessage(getmyPeerCard(),
		    deployMessage.toString(), channelName);
	    communicationModule.send(channelMessage, this, target);
	}
    }

    /**
     * 
     * @param mpaCard
     * @param partID
     * @param peer
     *            The peer notifying the staus of the part
     * @param partStatus
     */
    public void notifyRequestToInstallPart(UAPPCard mpaCard, String partID,
	    UAPPPartStatus partStatus) {
	if (!init()) {
	    LogUtils.logWarn(context, ControlBroker.class,
		    "notifyRequestToInstallPart",
		    new Object[] { "ControlBroker not initialized." }, null);
	    return;
	}
	if (deployManager.isDeployCoordinator()) {
	    // notify the local deploy manager
	    if (deployManager instanceof DeployManagerEventHandler)
		((DeployManagerEventHandler) deployManager)
			.installationPartNotification(mpaCard, partID,
				aalSpaceManager.getMyPeerCard(), partStatus);

	} else {
	    // send the message to the remote DeployManager
	    DeployNotificationPayload notificationPayload = new DeployNotificationPayload(
		    null, mpaCard, partID, partStatus);
	    DeployMessage deployMessage = new DeployMessage(
		    DeployMessageType.PART_NOTIFICATION, notificationPayload);

	    // ...and wrap it as ChannelMessage
	    List<String> channelName = new ArrayList<String>();
	    channelName.add(getBrokerName());
	    ChannelMessage channelMessage = new ChannelMessage(getmyPeerCard(),
		    deployMessage.toString(), channelName);
	    communicationModule.send(channelMessage, this, aalSpaceManager
		    .getAALSpaceDescriptor().getDeployManager());
	}
    }

    public String getBrokerName() {
	return context.getID();
    }

    public void handleSendError(ChannelMessage message,
	    CommunicationConnectorException e) {
	LogUtils.logError(
		context,
		ControlBroker.class,
		"handleSendError",
		new Object[] { "Error while sending the message: "
			+ message.toString() + " error: " + e.toString() },
		null);
    }

    public void messageReceived(ChannelMessage message) {
	if (!init()) {
	    LogUtils.logWarn(
		    context,
		    ControlBroker.class,
		    "messageReceived",
		    new Object[] { "ControlBroker not initialized. Dropping the message" },
		    null);
	    return;
	}
	deployManager = getDeployManager();
	deployConnector = getDeployConnector();
	BrokerMessage cm = null;
	if (message != null) {
	    try {
		Gson gson = GsonParserBuilder.getInstance().buildGson();
		cm = gson.fromJson(message.getContent(), BrokerMessage.class);
	    } catch (Exception e) {
		LogUtils.logError(
			context,
			ControlBroker.class,
			"messageReceived",
			new Object[] {
				"Error during message receive: ",
				e.toString(),
				"\nUnable to unmashall ControlMessage. Original message: ",
				message }, null);
	    }
	}

	if (cm instanceof ControlMessage) {
	    handleControlMessage(message.getSender(), (ControlMessage) cm);
	} else if (cm instanceof DeployMessage) {
	    handleDeployMessage(message.getSender(), (DeployMessage) cm);
	} else if (cm instanceof ConfigurationMessage) {
	    handleConfigurationMessage((ConfigurationMessage) cm);
	}
    }

    private void handleControlMessage(PeerCard sender, ControlMessage msg) {
	switch (msg.getMessageType()) {
	case GET_ATTRIBUTES: {
	    handleGetAttributes(sender, msg.getTransactionId(),
		    msg.getAttributes());
	}
	    break;
	case GET_ATTRIBUTES_RESPONSE: {
	    WaitForResponse req = openTransaction.get(msg.getTransactionId());
	    if (req != null) {
		req.addResponse(msg);
	    }
	}
	    break;
	case MATCH_ATTRIBUTES: {
	    handleMatchAttributes(sender, msg.getTransactionId(),
		    msg.getAttributeFilter());
	}
	    break;
	case MATCH_ATTRIBUTES_RESPONSE: {
	    WaitForResponse req = openTransaction.get(msg.getTransactionId());
	    if (req != null) {
		Response response = new Response();
		response.msg = msg;
		response.sender = sender;
		req.addResponse(response);
	    }
	}
	    break;

	default:
	    throw new UnsupportedOperationException(
		    "Unable to handle Control Message of type: "
			    + msg.getMessageType());
	}
    }

    private void handleMatchAttributes(PeerCard sender, String transactionId,
	    Map<String, Serializable> attributeValues) {

	ControlMessage controlMsg = prepareMatchingResponse(transactionId,
		attributeValues);
	CommunicationModule bus = getCommunicationModule();
	List<String> chName = new ArrayList<String>();
	chName.add(getBrokerName());
	ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
		controlMsg.toString(), chName);
	bus.send(chMsg, this, sender);
    }

    private ControlMessage prepareMatchingResponse(String transactionId,
	    Map<String, Serializable> attributeValues) {
	boolean match = true;

	HashMap<String, Serializable> attributes = new HashMap<String, Serializable>();
	Set<String> names = attributeValues.keySet();
	for (String name : names) {
	    Object value = context.getProperty(name);
	    if (value == null) {
		match = false;
		break;
	    }
	    if (attributeValues.get(name) != null
		    && value.equals(attributeValues.get(name)) == false) {
		match = false;
		break;
	    }
	    if (value instanceof Serializable) {
		attributes.put(name, (Serializable) value);
	    } else {
		attributes.put(name, value.toString());
	    }
	}
	return new ControlMessage(aalSpaceManager.getAALSpaceDescriptor(),
		transactionId, attributes, match);
    }

    private void handleGetAttributes(PeerCard sender, String transactionId,
	    List<String> requestedAttributes) {

	ControlMessage controlMsg = prepareGetAttributesResponse(transactionId,
		requestedAttributes);

	CommunicationModule bus = getCommunicationModule();
	List<String> chName = new ArrayList<String>();
	chName.add(getBrokerName());
	ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
		controlMsg.toString(), chName);
	bus.send(chMsg, this, sender);
    }

    private ControlMessage prepareGetAttributesResponse(String transactionId,
	    List<String> requestedAttributes) {
	HashMap<String, Serializable> attributes = new HashMap<String, Serializable>();
	for (String name : requestedAttributes) {
	    Object value = context.getProperty(name);
	    if (value == null)
		continue;
	    if (value instanceof Serializable) {
		attributes.put(name, (Serializable) value);
	    } else {
		attributes.put(name, value.toString());
	    }
	}
	return new ControlMessage(aalSpaceManager.getAALSpaceDescriptor(),
		transactionId, attributes);

    }

    private void handleDeployMessage(PeerCard sender, DeployMessage msg) {
	final String METHOD = "handleDeployMessage";
	switch (msg.getMessageType()) {

	case REQUEST_TO_INSTALL_PART:
	    LogUtils.logDebug(
		    context,
		    ControlBroker.class,
		    "handleDeployMessage",
		    new Object[] { "Request to install artefact. Passig it to the DeployConnector" },
		    null);
	    if (msg.getPayload() != null && msg.getPayload().getPart() != null) {
		File file = FileUtils.createFileFromByte(context, msg
			.getPayload().getPart(), TMP_DEPLOY_FOLDER + "part",
			true);
		if (file == null) {
		    LogUtils.logError(
			    context,
			    ControlBroker.class,
			    METHOD,
			    new Object[] { "Error while extracing artifact from message: unable to create file" },
			    null);
		}
		deployConnector.installPart(file, msg.getPayload()
			.getuappCard());
	    }
	    break; // TODO Ask michele if it was missing by reason

	case PART_NOTIFICATION:
	    LogUtils.logDebug(
		    context,
		    ControlBroker.class,
		    "handleDeployMessage",
		    new Object[] { "Notification of mpa appllication part. Notify the DeployManager" },
		    null);
	    if (msg.getPayload() != null
		    && msg.getPayload() instanceof DeployNotificationPayload) {
		DeployNotificationPayload payload = (DeployNotificationPayload) msg
			.getPayload();
		// pass it to the DeployManager
		if (deployManager instanceof DeployManagerEventHandler) {
		    ((DeployManagerEventHandler) deployManager)
			    .installationPartNotification(
				    payload.getuappCard(), payload.getPartID(),
				    sender, payload.getMpaPartStatus());
		}
	    }

	    break;

	default:
	    break;
	}
    }

    /**
     * @param cm
     */
    private void handleConfigurationMessage(ConfigurationMessage cm) {
	if (getConfiguratorManagerConnector() == null) {
	    LogUtils.logError(context, ControlBroker.class,
		    "handleConfigurationMessage",
		    "ConfigurationManagerConnector not available, unable to handle.");
	    return;
	}
	if (cm.getMessageType().equals(ConfigurationMessageType.PROPAGATE)) {
	    configConnector.processPropagation(cm);
	} else if (cm.isRequest()) {
	    configConnector.processRequest(cm);
	} else {
	    configConnector.processResponse(cm);
	}
    }

    public void installArtefactLocally(String serializedPart) {
	// deployConnector.installPart(serializedPart);
    }

    private DeployManager getDeployManager() {
	LogUtils.logDebug(context, ControlBroker.class, "getDeployManager",
		new Object[] { "Fetching the DeployManager..." }, null);
	if (deployManager == null) {
	    Object[] refs = context.getContainer().fetchSharedObject(context,
		    new Object[] { DeployManager.class.getName().toString() },
		    this);
	    if (refs != null) {

		LogUtils.logDebug(context, ControlBroker.class,
			"getDeployManager",
			new Object[] { "DeployManager found!" }, null);
		deployManager = (DeployManager) refs[0];
		LogUtils.logDebug(context, ControlBroker.class,
			"getDeployManager",
			new Object[] { "DeployManager fetched" }, null);
		return deployManager;
	    } else {
		LogUtils.logWarn(context, ControlBroker.class,
			"getDeployManager",
			new Object[] { "No DeployManager found" }, null);
		return null;
	    }
	} else
	    return deployManager;

    }

    private DeployConnector getDeployConnector() {
	LogUtils.logDebug(context, ControlBroker.class, "getDeployConnector",
		new Object[] { "Fetching the DeployConnector..." }, null);
	if (deployConnector == null) {
	    Object[] refs = context.getContainer()
		    .fetchSharedObject(
			    context,
			    new Object[] { DeployConnector.class.getName()
				    .toString() }, this);
	    if (refs != null) {

		LogUtils.logDebug(context, ControlBroker.class,
			"getDeployConnector",
			new Object[] { "DeployConnector found!" }, null);
		deployConnector = (DeployConnector) refs[0];
		LogUtils.logDebug(context, ControlBroker.class,
			"getDeployConnector",
			new Object[] { "DeployConnector fetched" }, null);
		return deployConnector;
	    } else {
		LogUtils.logWarn(context, ControlBroker.class,
			"getDeployConnector",
			new Object[] { "No DeployConnector found" }, null);
		return null;
	    }
	} else
	    return deployConnector;

    }

    public List<String> getPeersAddress() {
	return aalSpaceModule.getPeersAddress();
    }

    public void requestPeerCard(String peerAddress) {
	if (getAALSpaceModule() == null) {
	    LogUtils.logWarn(
		    context,
		    ControlBroker.class,
		    "requestPeerCard",
		    new Object[] { "ControlBroker not initialized. Request to leave aborted" },
		    null);
	    return;
	}
	aalSpaceModule.requestPeerCard(aalSpaceManager.getAALSpaceDescriptor(),
		peerAddress);
    }

    /*
     * public void configureDeployMessage() { if (getCommunicationModule() ==
     * null) { return; } communicationModule.addMessageListener(this,
     * aalSpaceManager .getGroupName(this)); }
     */

    public void dispose() {
	context.getContainer().removeSharedObjectListener(this);
	if (communicationModule == null)
	    return;
	communicationModule.removeMessageListener(this, getBrokerName());

    }

    public void renewAALSpace(AALSpaceCard spaceCard) {
	aalSpaceModule.renewAALSpace(spaceCard);
    }

    public void signalAALSpaceStatus(AALSpaceStatus status,
	    AALSpaceDescriptor spaceDescriptor) {

	// ControlPayload payload = new ControlPayload(getBrokerName(),
	// UUID.randomUUID().toString(), "", status);
	// ControlMessage message = new
	// ControlMessage(aalSpaceManager.getmyPeerCard(), getBrokerName(),
	// null, payload, ControlMessageType.AALSPACE_EVENT);
	// communicationModule.sendAll(message, this);
    }

    public BrokerMessage unmarshall(String message) {
	try {
	    Gson gson = GsonParserBuilder.getInstance().buildGson();
	    return gson.fromJson(message, ControlMessage.class);
	} catch (Exception e) {
	    final String MSG = "Unable to unmarshall message due to JSON parsing issue for "
		    + BrokerMessageFields.BROKER_MESSAGE_TYPE + ":";
	    LogUtils.logDebug(context, ControlBroker.class, "unmarshall",
		    new Object[] { MSG, e }, e);
	    LogUtils.logDebug(
		    context,
		    ControlBroker.class,
		    "unmarshall",
		    new Object[] { MSG + ExceptionUtils.stackTraceAsString(e) },
		    e);
	    throw new DeployMessageException(MSG + e.toString(), e);
	}
    }

    public Map<String, Serializable> requestPeerAttributes(
	    List<String> attributes, PeerCard target, int limit, int timeout) {

	if (target.equals(aalSpaceManager.getMyPeerCard())) {
	    ControlMessage response = prepareGetAttributesResponse("local",
		    attributes);
	    return response.getAttributeValues();
	}

	CommunicationModule bus = getCommunicationModule();
	ControlMessage controlMsg = new ControlMessage(
		aalSpaceManager.getAALSpaceDescriptor(), attributes);
	List<String> chName = new ArrayList<String>();
	chName.add(getBrokerName());
	ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
		controlMsg.toString(), chName);
	WaitForResponse<ControlMessage> waiter = new WaitForResponse<ControlMessage>(
		limit, timeout);
	openTransaction.put(controlMsg.getTransactionId(), waiter);
	bus.send(chMsg, this, target);
	ControlMessage response = waiter.getFirstReponse();
	openTransaction.remove(controlMsg.getTransactionId());
	return response.getAttributeValues();
    }

    public Map<PeerCard, Map<String, Serializable>> findMatchingPeers(
	    Map<String, Serializable> filter, int limit, int timeout) {
	CommunicationModule bus = getCommunicationModule();
	ControlMessage controlMsg = new ControlMessage(
		aalSpaceManager.getAALSpaceDescriptor(), filter);
	List<String> chName = new ArrayList<String>();
	chName.add(getBrokerName());
	ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
		controlMsg.toString(), chName);
	WaitForResponse<Response> waiter = new WaitForResponse<Response>(limit,
		timeout);
	openTransaction.put(controlMsg.getTransactionId(), waiter);
	bus.sendAll(chMsg, this);
	List<Response> responses = new ArrayList<ControlBroker.Response>();
	handleLocalMatchingPeers(controlMsg, responses);
	responses.addAll(waiter.getReponses());
	HashMap<PeerCard, Map<String, Serializable>> results = new HashMap<PeerCard, Map<String, Serializable>>();
	for (Response response : responses) {
	    if (response.msg.getMatchFilter() == false) {
		continue;
	    }
	    Map<String, Serializable> values = response.msg
		    .getAttributeValues();
	    results.put(response.sender, values);
	}
	openTransaction.remove(controlMsg.getTransactionId());
	return results;
    }

    private void handleLocalMatchingPeers(ControlMessage controlMsg,
	    List<Response> responses) {

	Response r = new Response();
	r.msg = prepareMatchingResponse(controlMsg.getTransactionId(),
		controlMsg.getAttributeFilter());
	r.sender = aalSpaceManager.getMyPeerCard();
	responses.add(r);
    }

    /**
     * Send a Configuration Message.
     * 
     * @param cm
     */
    public void sendConfigurationMessage(ConfigurationMessage cm) {
	if (getCommunicationModule() == null)
	    return;
	if (cm.getMessageType().equals(ConfigurationMessageType.PROPAGATE)) {
	    List<String> chName = new ArrayList<String>();
	    chName.add(getBrokerName());
	    ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
		    cm.toString(), chName);
	    communicationModule.sendAll(chMsg);
	} else {
	    List<String> chName = new ArrayList<String>();
	    chName.add(getBrokerName());
	    ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
		    cm.toString(), chName);
	    communicationModule.send(chMsg,
		    (PeerCard) Arrays.asList(cm.getReceivers()));
	}
    }

}
