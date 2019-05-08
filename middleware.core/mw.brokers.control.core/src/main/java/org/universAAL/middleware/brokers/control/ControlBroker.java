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
import org.universAAL.middleware.brokers.message.distributedmw.DistributedMWMessage;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;

import org.universAAL.middleware.connectors.DeployConnector;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.mpa.UAPPCard;
import org.universAAL.middleware.interfaces.mpa.UAPPPartStatus;
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.interfaces.space.SpaceStatus;
import org.universAAL.middleware.managers.api.SpaceEventHandler;
import org.universAAL.middleware.managers.api.SpaceManager;
import org.universAAL.middleware.managers.api.ConfigurationManagerConnector;
import org.universAAL.middleware.managers.api.DeployManager;
import org.universAAL.middleware.managers.api.DeployManagerEventHandler;
import org.universAAL.middleware.managers.api.DistributedMWEventHandler;
import org.universAAL.middleware.modules.SpaceModule;
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
public class ControlBroker implements SharedObjectListener, Broker, MessageListener {

	private ModuleContext context;
	private SpaceModule spaceModule;
	private CommunicationModule communicationModule;
	private SpaceEventHandler spaceEventHandler;
	private SpaceManager spaceManager;
	private DeployManager deployManager;
	private DeployConnector deployConnector;
	private ConfigurationManagerConnector configConnector;
	private DistributedMWEventHandler distributedMWEventHandler;
	private boolean initialized = false;
	private HashMap<String, WaitForResponse> openTransaction = new HashMap<String, WaitForResponse>();
	private List<ChannelMessage> cachedMessages = new ArrayList<ChannelMessage>();

	private static final String TMP_DEPLOY_FOLDER = "etc" + File.separatorChar + "tmp" + File.separatorChar + "installations"
			+ File.separatorChar;

	private class Response {
		ControlMessage msg;
		PeerCard sender;
	}

	public ControlBroker(ModuleContext context) {
		this.context = context;
		init();
	}

	private CommunicationModule getCommunicationModule() {
		return communicationModule = (ConfigurableCommunicationModule) getSharedObject(
				ConfigurableCommunicationModule.class, new Object[] { CommunicationModule.class.getName() });
	}

	private SpaceEventHandler getSpaceEventHandler() {
		return spaceEventHandler = (SpaceEventHandler) getSharedObject(SpaceEventHandler.class,
				new Object[] { SpaceEventHandler.class.getName() });
	}

	private SpaceManager getSpaceManager() {
		return spaceManager = (SpaceManager) getSharedObject(SpaceManager.class,
				new Object[] { SpaceManager.class.getName() });
	}

	private SpaceModule getSpaceModule() {
		return spaceModule = (SpaceModule) getSharedObject(SpaceModule.class,
				new Object[] { SpaceModule.class.getName() });
	}

	private DeployManager getDeployManager() {
		return deployManager = (DeployManager) getSharedObject(DeployManager.class,
				new Object[] { DeployManager.class.getName() });
	}

	private DeployConnector getDeployConnector() {
		return deployConnector = (DeployConnector) getSharedObject(DeployConnector.class,
				new Object[] { DeployConnector.class.getName() });
	}

	private ConfigurationManagerConnector getConfiguratorManagerConnector() {
		return configConnector = (ConfigurationManagerConnector) getSharedObject(ConfigurationManagerConnector.class,
				new Object[] { ConfigurationManagerConnector.class.getName() });
	}

	private DistributedMWEventHandler getDistributedMWEventHandler() {
		return distributedMWEventHandler = (DistributedMWEventHandler) getSharedObject(DistributedMWEventHandler.class,
				new Object[] { DistributedMWEventHandler.class.getName() });
	}

	private Object getSharedObject(Class<?> cls, Object[] params) {
		return getSharedObject(cls.getSimpleName(), params);
	}

	private Object getSharedObject(String clsName, Object[] params) {
		LogUtils.logTrace(context, ControlBroker.class, "getSharedObject",
				new Object[] { "Fetching the " + clsName + "..." }, null);
		Object[] refs = context.getContainer().fetchSharedObject(context, params, this);
		if (refs != null) {
			LogUtils.logTrace(context, ControlBroker.class, "getSharedObject", new Object[] { clsName + " found!" },
					null);
			return refs[0];
		} else {
			LogUtils.logWarn(context, ControlBroker.class, "getSharedObject",
					new Object[] { "No " + clsName + " found" }, null);
			return null;
		}
	}

	public boolean init() {
		if (!initialized) {
			if (getSpaceModule() == null || getSpaceEventHandler() == null || getSpaceManager() == null
					|| getCommunicationModule() == null || getConfiguratorManagerConnector() == null
					|| getDeployManager() == null
					// TODO: check this, the deploy connector is currently only
					// available in Karaf
					// || getDeployConnector() == null
					|| getDistributedMWEventHandler() == null) {
				return initialized = false;
			}
			communicationModule.addMessageListener(this, getBrokerName());

			initialized = true;
			// process cached messages
			synchronized (cachedMessages) {
				if (cachedMessages.size() != 0) {
					LogUtils.logDebug(context, ControlBroker.class, "init", new Object[] {
							"ControlBroker fully initialized, processing " + cachedMessages.size() + " messages" },
							null);
					for (ChannelMessage message : cachedMessages) {
						messageReceived(message);
					}
					cachedMessages.clear();
				}
			}
		}

		return initialized = true;
	}

	public List<SpaceCard> discoverSpace(Dictionary<String, String> filters) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "discoverSpace",
					new Object[] { "ControlBroker not initialized." }, null);
			return null;
		}
		return spaceModule.getSpaces(filters);
	}

	public void buildSpace(SpaceCard spaceCard) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "buildSpace",
					new Object[] { "ControlBroker not initialized. " }, null);
			return;
		}
		spaceModule.newSpace(spaceCard);
	}

	public void sharedObjectAdded(Object sharedObj, Object removeHook) {
		if (sharedObj == null)
			return;

		if (sharedObj instanceof SpaceModule) {
			LogUtils.logDebug(context, ControlBroker.class, "sharedObjectAdded",
					new Object[] { "SpaceModule registered..." }, null);
			spaceModule = (SpaceModule) sharedObj;
		}
		if (sharedObj instanceof CommunicationModule) {
			LogUtils.logDebug(context, ControlBroker.class, "sharedObjectAdded",
					new Object[] { "CommunicationModule registered..." }, null);
			//if (communicationModule instanceof ConfigurableCommunicationModule)
			//	communicationModule = (ConfigurableCommunicationModule) sharedObj;
			communicationModule = (CommunicationModule) sharedObj;
			communicationModule.addMessageListener(this, getBrokerName());
		}
		if (sharedObj instanceof SpaceManager) {
			LogUtils.logDebug(context, ControlBroker.class, "sharedObjectAdded",
					new Object[] { "SpaceManager registered..." }, null);
			spaceManager = (SpaceManager) sharedObj;
		}
		if (sharedObj instanceof SpaceEventHandler) {
			LogUtils.logDebug(context, ControlBroker.class, "sharedObjectAdded",
					new Object[] { "SpaceEventHandler registered..." }, null);
			spaceEventHandler = (SpaceEventHandler) sharedObj;
		}
		if (sharedObj instanceof DeployManager) {
			LogUtils.logDebug(context, ControlBroker.class, "sharedObjectAdded",
					new Object[] { "DeployManager registered..." }, null);
			deployManager = (DeployManager) sharedObj;
		}
		if (sharedObj instanceof DeployConnector) {
			LogUtils.logDebug(context, ControlBroker.class, "sharedObjectAdded",
					new Object[] { "DeployConnector registered..." }, null);
			deployConnector = (DeployConnector) sharedObj;
		}
		if (sharedObj instanceof ConfigurationManagerConnector) {
			LogUtils.logDebug(context, ControlBroker.class, "sharedObjectAdded",
					new Object[] { "ConfigurationManagerConnector registered..." }, null);
			configConnector = (ConfigurationManagerConnector) sharedObj;
		}
		if (sharedObj instanceof DistributedMWEventHandler) {
			LogUtils.logDebug(context, ControlBroker.class, "sharedObjectAdded",
					new Object[] { "DistributedMWEventHandler registered..." }, null);
			distributedMWEventHandler = (DistributedMWEventHandler) sharedObj;
		}

		init();
	}

	public void sharedObjectRemoved(Object arg0) {
		// TODO: handle ConfigurationManagerConnector?
		if (arg0 instanceof SpaceEventHandler) {
			LogUtils.logInfo(context, ControlBroker.class, "sharedObjectRemoved",
					new Object[] { "SpaceEventHandler unregistered!" }, null);
			spaceEventHandler = null;
			initialized = false;
		} else if (arg0 instanceof SpaceManager) {
			LogUtils.logInfo(context, ControlBroker.class, "sharedObjectRemoved",
					new Object[] { "SpaceManager unregistered!" }, null);
			spaceManager = null;
			initialized = false;
		} else if (arg0 instanceof DeployManager) {
			LogUtils.logInfo(context, ControlBroker.class, "sharedObjectRemoved",
					new Object[] { "DeployManager unregistered!" }, null);
			deployManager = null;
			initialized = false;

		} else if (arg0 instanceof SpaceModule) {
			LogUtils.logInfo(context, ControlBroker.class, "sharedObjectRemoved",
					new Object[] { "SpaceModule unregistered!" }, null);
			spaceModule = null;
			initialized = false;

		} else if (arg0 instanceof CommunicationModule) {
			LogUtils.logInfo(context, ControlBroker.class, "sharedObjectRemoved",
					new Object[] { "CommunicationModule unregistered!" }, null);
			try {
				communicationModule.removeMessageListener(this, getBrokerName());
			} catch (Exception e) {
			}
			communicationModule = null;
			initialized = false;
		} else if (arg0 instanceof DeployConnector) {
			LogUtils.logInfo(context, ControlBroker.class, "sharedObjectRemoved",
					new Object[] { "DeployConnector unregistered!" }, null);
			deployConnector = null;
			initialized = false;
		} else if (arg0 instanceof DistributedMWEventHandler) {
			LogUtils.logInfo(context, ControlBroker.class, "sharedObjectRemoved",
					new Object[] { "DistributedMWEventHandler unregistered!" }, null);
			distributedMWEventHandler = null;
			initialized = false;
		}
	}

	public void joinRequest(SpaceCard spaceCard, PeerCard sender) {
		if (getSpaceEventHandler() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "joinRequest",
					new Object[] { "ControlBroker not initialized. Join aborted" }, null);
			return;
		}
		spaceEventHandler.joinRequest(spaceCard, sender);
	}

	public void leaveRequest(SpaceDescriptor spaceDescriptor) {
		if (getSpaceEventHandler() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "leaveRequest",
					new Object[] { "ControlBroker not initialized. Leave aborted" }, null);
			return;
		}
		spaceEventHandler.leaveRequest(spaceDescriptor);
	}

	public void requestToLeave(SpaceDescriptor spaceDescriptor) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "requestToLeave",
					new Object[] { "ControlBroker not initialized. Request to leave aborted" }, null);
			return;
		}
		spaceModule.requestToLeave(spaceDescriptor);
	}

	public void peerLost(PeerCard sender) {
		if (getSpaceEventHandler() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "peerLost",
					new Object[] { "ControlBroker not initialized. Peer Lost message aborted" }, null);
			return;
		}
		spaceEventHandler.peerLost(sender);
	}

	public void join(PeerCard spaceCoordinator, SpaceCard spaceCard) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "join",
					new Object[] { "ControlBroker not initialized. Join message aborted" }, null);
			return;
		}
		spaceModule.joinSpace(spaceCoordinator, spaceCard);
	}

	/**
	 * This method returns the PeerCard of the current MW instance
	 *
	 * @return PeerCard
	 */
	public PeerCard getmyPeerCard() {
		if (getSpaceManager() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "getmyPeerCard",
					new Object[] { "ControlBroker not initialized. Fetching the PeerCard aborted" }, null);
			return null;
		}
		return spaceManager.getMyPeerCard();
	}

	/**
	 * This method returns the SpaceDescriptor of my Space
	 *
	 * @return
	 */
	public SpaceDescriptor getMySpaceDescriptor() {
		if (getSpaceManager() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "getMySpaceDescriptor",
					new Object[] { "ControlBroker not initialized. Fetching the PeerCard aborted" }, null);
			return null;
		}
		return spaceManager.getSpaceDescriptor();
	}

	public void spaceJoined(SpaceDescriptor descriptor) {
		if (getSpaceEventHandler() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "spaceJoined",
					new Object[] { "ControlBroker not initialized." }, null);
			return;
		}
		spaceEventHandler.spaceJoined(descriptor);
	}

	public void peerFound(PeerCard peer) {
		if (getSpaceEventHandler() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "peerFound",
					new Object[] { "ControlBroker not initialized." }, null);
			return;
		}
		spaceEventHandler.peerFound(peer);
	}

	public void newSpaceFound(Set<SpaceCard> spaceCards) {
		if (getSpaceEventHandler() == null) {
			return;
		}
		spaceEventHandler.newSpacesFound(spaceCards);
	}

	/**
	 * Only configures the communication channels by creating a list of channels
	 * for the Communication Module
	 *
	 * @param communicationChannels
	 */
	public void configureChannels(List<ChannelDescriptor> communicationChannels, String peerName) {
		if (getCommunicationModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "configureChannels",
					new Object[] { "ControlBroker not initialized." }, null);
			return;
		}
		communicationModule.addMessageListener(this, this.getBrokerName());
		try {
			((ConfigurableCommunicationModule) communicationModule).configureChannels(communicationChannels, peerName);
		} catch (Exception e) {
			LogUtils.logError(context, ControlBroker.class, "configureChannels",
					new Object[] { "unable to configure Channels of Comunication Module." }, e);
		}
	}

	/**
	 * Configures the peering channel by configuring the SpaceModule and by
	 * creating a new channel for the Communication Module
	 *
	 * @param peeringChannel
	 */
	public void configurePeeringChannel(ChannelDescriptor peeringChannel, String peerName) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "configurePeeringChannel",
					new Object[] { "ControlBroker not initialized." }, null);
			return;
		}
		spaceModule.configureSpaceChannel();
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
		if (communicationModule instanceof ConfigurableCommunicationModule)
			((ConfigurableCommunicationModule)communicationModule).dispose(channels);
		else
			LogUtils.logWarn(context, ControlBroker.class, "resetModule",
					new Object[] { "Communication module is not configurable." }, null);
	}

	public void destroySpace(SpaceCard spaceCard) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "destroySpace",
					new Object[] { "ControlBroker not initialized." }, null);
			return;
		}
		spaceModule.destroySpace(spaceCard);
	}

	public void leaveSpace(PeerCard spaceCoordinator, SpaceCard spaceCard) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "leaveSpace",
					new Object[] { "ControlBroker not initialized." }, null);
			return;
		}
		spaceModule.leaveSpace(spaceCoordinator, spaceCard);
	}

	public void addNewPeer(SpaceDescriptor spaceDescriptor, PeerCard peer) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "addNewPeer",
					new Object[] { "ControlBroker not initialized." }, null);
			return;
		}
		spaceModule.addPeer(spaceDescriptor, peer);
	}

	public void newPeerAdded(SpaceCard spaceCard, PeerCard peer) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "newPeerAdded",
					new Object[] { "ControlBroker not initialized." }, null);
			return;
		}
		spaceModule.announceNewPeer(spaceCard, peer);
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
			LogUtils.logWarn(context, ControlBroker.class, METHOD, new Object[] { "ControlBroker not initialized." },
					null);
			return;
		}

		// I'm the target node install the part locally
		if (target.getPeerID().equals(spaceManager.getMyPeerCard().getPeerID())) {
			// TODO Handle local uninstallation
			deployConnector.uninstallPart(card);
		} else {
			// Send the message to the target node

			DeployPayload payload = new DeployPayload(new byte[] {}, card);
			DeployMessage deployMessage = new DeployMessage(DeployMessageType.REQUEST_TO_UNINSTALL_PART, payload);

			// ...and wrap it as ChannelMessage
			List<String> channelName = new ArrayList<String>();
			channelName.add(getBrokerName());
			ChannelMessage channelMessage = new ChannelMessage(getmyPeerCard(), deployMessage.toString(), channelName);
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
	public void requestToInstallPart(byte[] partAsZip, PeerCard target, UAPPCard card) {
		final String METHOD = "requestToInstallPart";
		if (!init()) {
			LogUtils.logWarn(context, ControlBroker.class, METHOD, new Object[] { "ControlBroker not initialized." },
					null);
			return;
		}

		// I'm the target node install the part locally
		if (target.getPeerID().equals(spaceManager.getMyPeerCard().getPeerID())) {
			File file = null, root = null;
			root = new File(TMP_DEPLOY_FOLDER);
			String dst = null;
			try {
				file = File.createTempFile("part", "install.zip", root);
				dst = file.getAbsolutePath();
			} catch (IOException e) {
				LogUtils.logError(context, ControlBroker.class, METHOD,
						new Object[] {
								"Unable to generate a valid tmp filename with createTempFile() method using timestamp" },
						null);
				dst = TMP_DEPLOY_FOLDER + System.currentTimeMillis() + ".part";
			}
			file = FileUtils.createFileFromByte(context, partAsZip, dst, true);
			if (file == null) {
				LogUtils.logError(context, ControlBroker.class, METHOD,
						new Object[] { "Error while installing artifact locally: unable to create file" }, null);
				return;
			}
			deployConnector.installPart(file, card);
			file.delete();
			file.deleteOnExit();
			file = null;
		} else {
			// Send the message to the target node

			DeployPayload payload = new DeployPayload(partAsZip, card);
			DeployMessage deployMessage = new DeployMessage(DeployMessageType.REQUEST_TO_INSTALL_PART, payload);

			// ...and wrap it as ChannelMessage
			List<String> channelName = new ArrayList<String>();
			channelName.add(getBrokerName());
			// TODO we should send a part in small pieces or by means of
			// out-of-band protocol which support streaming
			ChannelMessage channelMessage = new ChannelMessage(getmyPeerCard(), deployMessage.toString(), channelName);
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
	public void notifyRequestToInstallPart(UAPPCard mpaCard, String partID, UAPPPartStatus partStatus) {
		if (!init()) {
			LogUtils.logWarn(context, ControlBroker.class, "notifyRequestToInstallPart",
					new Object[] { "ControlBroker not initialized." }, null);
			return;
		}
		if (deployManager.isDeployCoordinator()) {
			// notify the local deploy manager
			if (deployManager instanceof DeployManagerEventHandler)
				((DeployManagerEventHandler) deployManager).installationPartNotification(mpaCard, partID,
						spaceManager.getMyPeerCard(), partStatus);

		} else {
			// send the message to the remote DeployManager
			DeployNotificationPayload notificationPayload = new DeployNotificationPayload(null, mpaCard, partID,
					partStatus);
			DeployMessage deployMessage = new DeployMessage(DeployMessageType.PART_NOTIFICATION, notificationPayload);

			// ...and wrap it as ChannelMessage
			List<String> channelName = new ArrayList<String>();
			channelName.add(getBrokerName());
			ChannelMessage channelMessage = new ChannelMessage(getmyPeerCard(), deployMessage.toString(), channelName);
			communicationModule.send(channelMessage, this, spaceManager.getSpaceDescriptor().getDeployManager());
		}
	}

	public String getBrokerName() {
		return context.getID();
	}

	public void handleSendError(ChannelMessage message, CommunicationConnectorException e) {
		LogUtils.logError(context, ControlBroker.class, "handleSendError",
				new Object[] { "Error while sending the message: " + message.toString() + " error: " + e.toString() },
				null);
	}

	public void messageReceived(ChannelMessage message) {
		if (!init()) {
			LogUtils.logWarn(context, ControlBroker.class, "messageReceived",
					new Object[] { "ControlBroker not initialized. Caching the message for later processing" }, null);
			synchronized (cachedMessages) {
				cachedMessages.add(message);
			}
			return;
		}
		// CS: removed, this is done in init and sharedObjectAdded
		// deployManager = getDeployManager();
		// deployConnector = getDeployConnector();
		BrokerMessage cm = null;
		if (message != null) {
			try {
				Gson gson = GsonParserBuilder.getInstance();
				cm = gson.fromJson(message.getContent(), BrokerMessage.class);
				if (cm == null)
					cm = gson.fromJson(message.getContent(), DistributedMWMessage.class);
			} catch (Exception e) {
				LogUtils.logError(context,
						ControlBroker.class, "messageReceived", new Object[] { "Error during message receive: ",
								e.toString(), "\nUnable to unmashall ControlMessage. Original message: ", message },
						null);
			}
		}

		if (cm instanceof ControlMessage) {
			handleControlMessage(message.getSender(), (ControlMessage) cm);
		} else if (cm instanceof DeployMessage) {
			handleDeployMessage(message.getSender(), (DeployMessage) cm);
		} else if (cm instanceof ConfigurationMessage) {
			handleConfigurationMessage((ConfigurationMessage) cm);
		} else if (cm instanceof DistributedMWMessage) {
			handleDistributedMWMessage(message.getSender(), (DistributedMWMessage) cm);
		} else {
			String s = cm == null ? "null" : cm.getClass().getName();
			LogUtils.logError(context, ControlBroker.class, "messageReceived",
					"Message type unknown. Dropping message of type " + s);
		}
	}

	private void handleDistributedMWMessage(PeerCard sender, DistributedMWMessage msg) {
		if (getDistributedMWEventHandler() == null) {
			LogUtils.logError(context, ControlBroker.class, "handleDistributedMWMessage",
					"DistributedMWEventHandler not available, unable to handle.");
			return;
		}
		distributedMWEventHandler.handleMessage(sender, msg);
	}

	private void handleControlMessage(PeerCard sender, ControlMessage msg) {
		switch (msg.getMessageType()) {
		case GET_ATTRIBUTES: {
			handleGetAttributes(sender, msg.getTransactionId(), msg.getAttributes());
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
			handleMatchAttributes(sender, msg.getTransactionId(), msg.getAttributeFilter());
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
					"Unable to handle Control Message of type: " + msg.getMessageType());
		}
	}

	private void handleMatchAttributes(PeerCard sender, String transactionId,
			Map<String, Serializable> attributeValues) {

		ControlMessage controlMsg = prepareMatchingResponse(transactionId, attributeValues);
		CommunicationModule bus = getCommunicationModule();
		List<String> chName = new ArrayList<String>();
		chName.add(getBrokerName());
		ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(), controlMsg.toString(), chName);
		bus.send(chMsg, this, sender);
	}

	private ControlMessage prepareMatchingResponse(String transactionId, Map<String, Serializable> attributeValues) {
		boolean match = true;

		HashMap<String, Serializable> attributes = new HashMap<String, Serializable>();
		Set<String> names = attributeValues.keySet();
		for (String name : names) {
			Object value = context.getProperty(name);
			if (value == null) {
				match = false;
				break;
			}
			if (attributeValues.get(name) != null && value.equals(attributeValues.get(name)) == false) {
				match = false;
				break;
			}
			if (value instanceof Serializable) {
				attributes.put(name, (Serializable) value);
			} else {
				attributes.put(name, value.toString());
			}
		}
		return new ControlMessage(spaceManager.getSpaceDescriptor(), transactionId, attributes, match);
	}

	private void handleGetAttributes(PeerCard sender, String transactionId, List<String> requestedAttributes) {

		ControlMessage controlMsg = prepareGetAttributesResponse(transactionId, requestedAttributes);

		CommunicationModule bus = getCommunicationModule();
		List<String> chName = new ArrayList<String>();
		chName.add(getBrokerName());
		ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(), controlMsg.toString(), chName);
		bus.send(chMsg, this, sender);
	}

	private ControlMessage prepareGetAttributesResponse(String transactionId, List<String> requestedAttributes) {
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
		return new ControlMessage(spaceManager.getSpaceDescriptor(), transactionId, attributes);

	}

	private void handleDeployMessage(PeerCard sender, DeployMessage msg) {
		final String METHOD = "handleDeployMessage";
		switch (msg.getMessageType()) {

		case REQUEST_TO_INSTALL_PART:
			LogUtils.logDebug(context, ControlBroker.class, "handleDeployMessage",
					new Object[] { "Request to install artefact. Passig it to the DeployConnector" }, null);
			if (msg.getPayload() != null && msg.getPayload().getPart() != null) {
				File file = FileUtils.createFileFromByte(context, msg.getPayload().getPart(),
						TMP_DEPLOY_FOLDER + "part", true);
				if (file == null) {
					LogUtils.logError(context, ControlBroker.class, METHOD,
							new Object[] { "Error while extracing artifact from message: unable to create file" },
							null);
				}
				deployConnector.installPart(file, msg.getPayload().getuappCard());
			}
			break; // TODO Ask michele if it was missing by reason

		case PART_NOTIFICATION:
			LogUtils.logDebug(context, ControlBroker.class, "handleDeployMessage",
					new Object[] { "Notification of mpa appllication part. Notify the DeployManager" }, null);
			if (msg.getPayload() != null && msg.getPayload() instanceof DeployNotificationPayload) {
				DeployNotificationPayload payload = (DeployNotificationPayload) msg.getPayload();
				// pass it to the DeployManager
				if (deployManager instanceof DeployManagerEventHandler) {
					((DeployManagerEventHandler) deployManager).installationPartNotification(payload.getuappCard(),
							payload.getPartID(), sender, payload.getMpaPartStatus());
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
			LogUtils.logError(context, ControlBroker.class, "handleConfigurationMessage",
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

	public List<String> getPeersAddress() {
		return spaceModule.getPeersAddress();
	}

	public void requestPeerCard(String peerAddress) {
		if (getSpaceModule() == null) {
			LogUtils.logWarn(context, ControlBroker.class, "requestPeerCard",
					new Object[] { "ControlBroker not initialized. Request to leave aborted" }, null);
			return;
		}
		spaceModule.requestPeerCard(spaceManager.getSpaceDescriptor(), peerAddress);
	}

	/*
	 * public void configureDeployMessage() { if (getCommunicationModule() ==
	 * null) { return; } communicationModule.addMessageListener(this,
	 * SpaceManager .getGroupName(this)); }
	 */

	public void dispose() {
		context.getContainer().removeSharedObjectListener(this);
		if (communicationModule == null)
			return;
		communicationModule.removeMessageListener(this, getBrokerName());
	}

	public void renewSpace(SpaceCard spaceCard) {
		spaceModule.renewSpace(spaceCard);
	}

	public void signalSpaceStatus(SpaceStatus status, SpaceDescriptor spaceDescriptor) {

		// ControlPayload payload = new ControlPayload(getBrokerName(),
		// UUID.randomUUID().toString(), "", status);
		// ControlMessage message = new
		// ControlMessage(SpaceManager.getmyPeerCard(), getBrokerName(),
		// null, payload, ControlMessageType.SPACE_EVENT);
		// communicationModule.sendAll(message, this);
	}

	public BrokerMessage unmarshall(String message) {
		try {
			Gson gson = GsonParserBuilder.getInstance();
			return gson.fromJson(message, ControlMessage.class);
		} catch (Exception e) {
			final String MSG = "Unable to unmarshall message due to JSON parsing issue for "
					+ BrokerMessageFields.BROKER_MESSAGE_TYPE + ":";
			LogUtils.logDebug(context, ControlBroker.class, "unmarshall", new Object[] { MSG, e }, e);
			LogUtils.logDebug(context, ControlBroker.class, "unmarshall",
					new Object[] { MSG + ExceptionUtils.stackTraceAsString(e) }, e);
			throw new DeployMessageException(MSG + e.toString(), e);
		}
	}

	public Map<String, Serializable> requestPeerAttributes(List<String> attributes, PeerCard target, int limit,
			int timeout) {

		if (target.equals(spaceManager.getMyPeerCard())) {
			ControlMessage response = prepareGetAttributesResponse("local", attributes);
			return response.getAttributeValues();
		}

		CommunicationModule bus = getCommunicationModule();
		ControlMessage controlMsg = new ControlMessage(spaceManager.getSpaceDescriptor(), attributes);
		List<String> chName = new ArrayList<String>();
		chName.add(getBrokerName());
		ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(), controlMsg.toString(), chName);
		WaitForResponse<ControlMessage> waiter = new WaitForResponse<ControlMessage>(limit, timeout);
		openTransaction.put(controlMsg.getTransactionId(), waiter);
		bus.send(chMsg, this, target);
		ControlMessage response = waiter.getFirstReponse();
		openTransaction.remove(controlMsg.getTransactionId());
		return response.getAttributeValues();
	}

	public Map<PeerCard, Map<String, Serializable>> findMatchingPeers(Map<String, Serializable> filter, int limit,
			int timeout) {
		CommunicationModule bus = getCommunicationModule();
		ControlMessage controlMsg = new ControlMessage(spaceManager.getSpaceDescriptor(), filter);
		List<String> chName = new ArrayList<String>();
		chName.add(getBrokerName());
		ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(), controlMsg.toString(), chName);
		WaitForResponse<Response> waiter = new WaitForResponse<Response>(limit, timeout);
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
			Map<String, Serializable> values = response.msg.getAttributeValues();
			results.put(response.sender, values);
		}
		openTransaction.remove(controlMsg.getTransactionId());
		return results;
	}

	private void handleLocalMatchingPeers(ControlMessage controlMsg, List<Response> responses) {

		Response r = new Response();
		r.msg = prepareMatchingResponse(controlMsg.getTransactionId(), controlMsg.getAttributeFilter());
		r.sender = spaceManager.getMyPeerCard();
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
			ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(), cm.toString(), chName);
			communicationModule.sendAll(chMsg);
		} else {
			List<String> chName = new ArrayList<String>();
			chName.add(getBrokerName());
			ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(), cm.toString(), chName);
			ArrayList receivers = new ArrayList();
			PeerCard[] array = cm.getReceivers();
			for (int i = 0; i < array.length; i++) {
				receivers.add(array[i]);
			}
			communicationModule.sendAll(chMsg, receivers);
		}
	}

	/**
	 * Send a message.
	 */
	public void sendMessage(BrokerMessage msg, List<PeerCard> receivers) {
		if (getCommunicationModule() == null || receivers.size() == 0)
			return;
		List<String> chName = new ArrayList<String>();
		chName.add(getBrokerName());
		ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(), msg.toString(), chName);
		if (receivers.size() == 1) {
			communicationModule.send(chMsg, receivers.get(0));
		} else {
			communicationModule.sendAll(chMsg, receivers);
		}
	}
}
