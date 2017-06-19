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
package org.universAAL.middleware.modules.space;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;

import org.universAAL.middleware.brokers.Broker;
import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.brokers.message.space.SpaceMessage;
import org.universAAL.middleware.brokers.message.space.SpaceMessageException;
import org.universAAL.middleware.brokers.message.space.SpaceMessage.SpaceMessageTypes;
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
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.modules.SpaceModule;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.ConfigurableCommunicationModule;
import org.universAAL.middleware.modules.exception.SpaceModuleErrorCode;
import org.universAAL.middleware.modules.exception.SpaceModuleException;
import org.universAAL.middleware.modules.listener.MessageListener;

/**
 * Implementation of the SpaceModule
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public class SpaceModuleImpl
		implements SpaceModule, MessageListener, SharedObjectListener, ServiceListener, Broker {

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

	/**
	 * This method configures the SpaceModule: -to obtain the reference to
	 * all the DiscoveryConnector present in the fw -to obtain the reference to
	 * the CommunicationModdule -to obtain the reference to the ControlBroker
	 *
	 * @return true if initialized with the connectors and the module, false
	 *         otherwise
	 */
	public boolean init() {
		if (!initialized) {
			try {
				if (discoveryConnectors == null || discoveryConnectors.isEmpty()) {

					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Fetching the DiscoveryConnector..." }, null);
					Object[] dConnectors = context.getContainer().fetchSharedObject(context,
							new Object[] { DiscoveryConnector.class.getName() }, this);
					if (dConnectors != null && dConnectors.length > 0) {
						LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
								new Object[] { "Found: " + dConnectors.length + " DiscoveryConnector" }, null);
						// clear or init the list of connectors

						discoveryConnectors = new ArrayList<DiscoveryConnector>();
						for (Object ref : dConnectors) {
							DiscoveryConnector dConnector = (DiscoveryConnector) ref;
							dConnector.addSpaceListener(this);
							discoveryConnectors.add((DiscoveryConnector) ref);
						}
						LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
								new Object[] { "DiscoveryConnectors fetched" }, null);
					} else {
						LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
								new Object[] { "No DiscoveryConnector found" }, null);
						initialized = false;
						return initialized;
					}
				}
				if (communicationModule == null) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Fetching the CommunicationModule..." }, null);
					Object cModule = context.getContainer().fetchSharedObject(context,
							new Object[] { CommunicationModule.class.getName().toString() });
					if (cModule != null) {
						communicationModule = (CommunicationModule) cModule;
						LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
								new Object[] { "CommunicationModule fetched" }, null);
					} else {
						LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
								new Object[] { "No CommunicationModule found" }, null);
						initialized = false;
						return initialized;
					}
				}

				if (controlBoker == null) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Fetching the ControlBroker..." }, null);
					Object cBroker = context.getContainer().fetchSharedObject(context,
							new Object[] { ControlBroker.class.getName().toString() });
					if (cBroker != null) {
						LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
								new Object[] { "Found a ControlBroker" }, null);
						controlBoker = (ControlBroker) cBroker;
						LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
								new Object[] { "ControlBroker fetched" }, null);
					} else {
						LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
								new Object[] { "No ControlBroker found" }, null);
						initialized = false;
						return initialized;
					}
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "ControlBroker found" }, null);
					// DiscoveryConnector, CommunicationModule and ControlBroker
					// have been found

				}
				initialized = true;
			} catch (NullPointerException e) {
				LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Error while initializing the SpaceModule: " + e }, null);
				initialized = false;
			} catch (ClassCastException e) {
				LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Error while casting CommunicationConnector and CommunicationModule: " + e },
						null);
				initialized = false;
			}
		}
		if (initialized)
			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "SpaceModule initialized" }, null);
		return initialized;
	}

	public SpaceModuleImpl(ModuleContext context) {
		this.context = context;
		discoveryConnectors = new ArrayList<DiscoveryConnector>();
	}

	public List<SpaceCard> getSpaces() {
		return this.getSpaces(null);
	}

	public List<SpaceCard> getSpaces(Dictionary<String, String> filters) throws SpaceModuleException {
		List<SpaceCard> spaces = new ArrayList<SpaceCard>();
		if (init()) {
			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Searching for the Space with filters: " + filters.toString() + "..." }, null);

			try {
				for (DiscoveryConnector dConnector : discoveryConnectors) {
					if (filters != null && filters.size() > 0)
						spaces.addAll(dConnector.findSpace(filters));
					else
						spaces.addAll(dConnector.findSpace());
				}
			} catch (DiscoveryConnectorException e) {
				LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Error during the SPace search:" + e.toString() }, null);
				throw new SpaceModuleException(SpaceModuleErrorCode.ERROR_INTERACTING_DISCOVERY_CONNECTORS,
						e.toString());
			}
			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Found: " + spaces.size() + " Spaces." }, null);

		} else {
			LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "SpaceModule cannot be initialized. Returning no Spaces" }, null);
			throw new SpaceModuleException(SpaceModuleErrorCode.NO_DISCOVERY_CONNECTORS,
					"SpaceModule cannot be initialized. Returning no Spaces");
		}
		return spaces;
	}

	public synchronized void newSpace(SpaceCard spaceCard) throws SpaceModuleException {
		if (init()) {
			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Creating a new Space..." }, null);
			for (DiscoveryConnector connector : discoveryConnectors) {
				try {
					connector.announceSpace(spaceCard);

				} catch (DiscoveryConnectorException e) {
					LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl", new Object[] {
							"Error creating the Space: " + spaceCard.toString() + " due to: " + e.toString() },
							null);
					throw new SpaceModuleException(SpaceModuleErrorCode.ERROR_INTERACTING_DISCOVERY_CONNECTORS,
							"Error creating the Space: " + spaceCard.toString() + " due to: " + e.toString());
				}
			}
		} else {
			LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "SpaceModule cannot be initialized. Returning no Space" }, null);
			throw new SpaceModuleException(SpaceModuleErrorCode.NO_DISCOVERY_CONNECTORS,
					"SpaceModule cannot be initialized. Returning no Spaces");
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
		LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
				new Object[] { "updating SpaceModule properties" }, null);
		if (configurations == null) {
			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "SpaceModule properties are null" }, null);
			return;
		}
		try {

			this.name = (String) configurations.get(org.universAAL.middleware.modules.util.Consts.MODULE_NAME);
			this.version = (String) configurations.get(org.universAAL.middleware.modules.util.Consts.MODULE_VERSION);
			this.description = (String) configurations
					.get(org.universAAL.middleware.modules.util.Consts.MODULE_DESCRIPTION);
			this.provider = (String) configurations.get(org.universAAL.middleware.modules.util.Consts.MODULE_PROVIDER);
		} catch (NumberFormatException e) {
			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Error during SpaceModule properties update" }, null);
		} catch (NullPointerException e) {
			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Error during SpaceModule properties update" }, null);
		} catch (Exception e) {
			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Error during SpaceModule properties update" }, null);
		}
		LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
				new Object[] { "SpaceModule properties updated" }, null);
	}

	public void leaveSpace(PeerCard spaceCoordinator, SpaceCard spaceCard) {
		try {
			if (init()) {
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Creating a new SpaceMessage" }, null);
				// prepare the Space Message...
				SpaceMessage spaceMessage = new SpaceMessage(
						new SpaceDescriptor(spaceCard, new ArrayList<ChannelDescriptor>()),
						SpaceMessageTypes.LEAVE);
				// ...and wraps it as ChannelMessage
				List<String> channelName = new ArrayList<String>();
				channelName.add(getBrokerName());
				ChannelMessage channelMessage = new ChannelMessage(controlBoker.getmyPeerCard(),
						spaceMessage.toString(), channelName);

				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Sending Leave Request message..." }, null);
				communicationModule.send(channelMessage, this, spaceCoordinator);
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Leave Request message sent." }, null);

			}
		} catch (CommunicationConnectorException e) {
			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Error during the unicast send: " + e }, null);
			throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_LEAVE_ERROR,
					"Error during the unicast send: " + e);
		}
	}

	public void requestToLeave(SpaceDescriptor spaceDescriptor) {
		try {
			if (init()) {
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Creating a new SpaceMessage" }, null);

				// prepare the Space Message
				SpaceMessage spaceMessage = new SpaceMessage(spaceDescriptor,
						SpaceMessageTypes.REQUEST_TO_LEAVE);
				// ...and wrap it as ChannelMessage
				List<String> channelName = new ArrayList<String>();
				channelName.add(getBrokerName());
				ChannelMessage channelMessage = new ChannelMessage(controlBoker.getmyPeerCard(),
						spaceMessage.toString(), channelName);

				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Sending Leave Request message..." }, null);
				communicationModule.sendAll(channelMessage, this);
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Leave Request message sent." }, null);

			}
		} catch (CommunicationConnectorException e) {
			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Error during the unicast send: " + e }, null);
			throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_LEAVE_ERROR,
					"Error during the unicast send: " + e);
		}
	}

	public void requestPeerCard(SpaceDescriptor spaceDescriptor, String peerAddress) {
		try {
			if (init()) {
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Creating a new SpaceMessage" }, null);

				// prepare the Space Message
				SpaceMessage spaceMessage = new SpaceMessage(spaceDescriptor,
						SpaceMessageTypes.REQUEST_PEERCARD);
				// ...and wrap it as ChannelMessage
				List<String> channelName = new ArrayList<String>();
				channelName.add(getBrokerName());
				ChannelMessage channelMessage = new ChannelMessage(controlBoker.getmyPeerCard(),
						spaceMessage.toString(), channelName);

				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Sending Request Peer Card message..." }, null);
				communicationModule.send(channelMessage, this, new PeerCard(peerAddress, PeerRole.PEER));
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Request Peer Card sent." }, null);

			}
		} catch (CommunicationConnectorException e) {
			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Error during the unicast send: " + e }, null);
			throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_LEAVE_ERROR,
					"Error during the unicast send: " + e);
		}
	}

	public void newSpacesFound(Set<SpaceCard> spaceCards) {
		if (spaceCards != null) {
			if (controlBoker != null)
				controlBoker.newSpaceFound(spaceCards);
			else
				LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "NO control Broker found" }, null);
		} else
			LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Space card is null" }, null);
	}

	public synchronized void joinSpace(PeerCard spaceCoordinator, SpaceCard spaceCard) {

		if (spaceCoordinator != null && spaceCard != null) {
			LogUtils.logDebug(context,
					SpaceModuleImpl.class, "SpaceModuleImpl", new Object[] { "Peer: "
							+ spaceCoordinator.toString() + " is joining the spaceCard: " + spaceCard.toString() },
					null);
			try {
				if (init()) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Creating a new SpaceMessage" }, null);
					// prepare the Space Message

					SpaceDescriptor spaceDesc = new SpaceDescriptor(spaceCard,
							new ArrayList<ChannelDescriptor>());
					SpaceMessage spaceMessage = new SpaceMessage(spaceDesc, SpaceMessageTypes.JOIN_REQUEST);

					// ...and wrap it as ChannelMessage
					List<String> channelName = new ArrayList<String>();
					channelName.add(getBrokerName());
					ChannelMessage channelMessage = new ChannelMessage(controlBoker.getmyPeerCard(),
							spaceMessage.toString(), channelName);

					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Sending Join Request message..." }, null);
					communicationModule.send(channelMessage, this, spaceCoordinator);
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Join Request message sent." }, null);

				}
			} catch (CommunicationConnectorException e) {
				LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Error during the unicast send: " + e }, null);
				throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_JOIN_ERROR,
						"Error during the unicast send: " + e);
			}
		}
		// invalid parameters
		else {
			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "PeerCard and/or SpaceCard are null" }, null);
			throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_JOIN_WRONG_PARAMETERS,
					"PeerCard and/or SpaceCard are null");
		}
	}

	public void addPeer(SpaceDescriptor spaceDescriptor, PeerCard newPeer) {
		if (spaceDescriptor != null && newPeer != null) {
			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl", new Object[] {
					"Peer: " + newPeer.toString() + " is joining the spaceCard: " + spaceDescriptor.toString() }, null);
			try {
				if (init()) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Creating a new SpaceMessage" }, null);
					// prepare the Space Message

					SpaceMessage spaceMessage = new SpaceMessage(spaceDescriptor,
							SpaceMessageTypes.JOIN_RESPONSE);

					// ...and wrap it as ChannelMessage
					List<String> channelName = new ArrayList<String>();
					channelName.add(getBrokerName());
					ChannelMessage channelMessage = new ChannelMessage(controlBoker.getmyPeerCard(),
							spaceMessage.toString(), channelName);

					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Sending Join Request message..." }, null);
					communicationModule.send(channelMessage, this, newPeer);
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Join Request message queued." }, null);

				}
			} catch (CommunicationConnectorException e) {
				LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Error during the unicast send: " + e }, null);
				throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_JOIN_ERROR,
						"Error during the unicast send: " + e);
			}
		}
		// invalid parameters
		else {
			LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "PeerCard and/or SpaceCard are null" }, null);
			throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_JOIN_WRONG_PARAMETERS,
					"PeerCard and/or SpaceCard are null");
		}
	}

	public void announceNewPeer(SpaceCard spaceCard, PeerCard peerCard) {
		if (spaceCard != null && peerCard != null) {
			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl", new Object[] {
					"Announcing new Peer: " + peerCard.toString() + " into the AASpace: " + spaceCard.toString() },
					null);

			SpaceMessage spaceMessage = new SpaceMessage(
					new SpaceDescriptor(spaceCard, new ArrayList<ChannelDescriptor>()),
					SpaceMessageTypes.NEW_PEER);

			// ...and wrap it as ChannelMessage
			List<String> channelName = new ArrayList<String>();
			channelName.add(getBrokerName());
			ChannelMessage channelMessage = new ChannelMessage(controlBoker.getmyPeerCard(), spaceMessage.toString(),
					channelName);

			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Sending New Peer added message..." }, null);
			communicationModule.sendAll(channelMessage, this);
			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "New Peer added message queued" }, null);

		} else {
			LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Event propagation failed! PeerCard and/or SpaceCard are not valid" }, null);
			throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_NEW_PEER_ADDED_ERROR,
					"Event propagation failed! PeerCard and/or SpaceCard are not valid");
		}
	}

	public void messageFromSpace(SpaceMessage message, PeerCard sender) throws SpaceModuleException {
		LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
				new Object[] { "SpaceMessage arrived...queuing" }, null);
		try {
			SpaceMessageTypes messageType = message.getMessageType();
			switch (messageType) {
			case JOIN_REQUEST: {
				// check if the peer belongs to by Space
				if (!message.getSpaceDescriptor().getSpaceCard().getSpaceID()
						.equals(controlBoker.getMySpaceDescriptor().getSpaceCard().getSpaceID())) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Peer is not part of my Space..dropping it" + message.toString() }, null);
					return;
				}
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Join request: " + message.toString() }, null);
				controlBoker.joinRequest(message.getSpaceDescriptor().getSpaceCard(), sender);

			}
				break;
			case JOIN_RESPONSE: {

				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Join response: " + message.toString() }, null);
				if (message.getSpaceDescriptor() != null) {
					controlBoker.spaceJoined(message.getSpaceDescriptor());
				} else {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "The Join Response is not valid" }, null);
					throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_JOIN_RESPONSE_WRONG_PARAMETERS,
							"The Join Response is not valid");
				}

			}
				break;
			case NEW_PEER: {
				// check if the peer belongs to by Space
				if (!message.getSpaceDescriptor().getSpaceCard().getSpaceID()
						.equals(controlBoker.getMySpaceDescriptor().getSpaceCard().getSpaceID())) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Peer is not part of my Space..dropping it" + message.toString() }, null);
					return;
				}
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "New Peer added: " + message.toString() }, null);
				if (sender != null) {
					controlBoker.peerFound(sender);
				} else {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "The New Peer added has not a valid PeerCard" }, null);
					throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_NEW_PEER_ERROR,
							"The New Peer added has not a valid PeerCard");
				}

			}
				break;
			case LEAVE: {
				// check if the peer belongs to by Space
				if (!message.getSpaceDescriptor().getSpaceCard().getSpaceID()
						.equals(controlBoker.getMySpaceDescriptor().getSpaceCard().getSpaceID())) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Peer is not part of my Space..dropping it" + message.toString() }, null);
					return;
				}
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Leave request: " + message.toString() }, null);
				if (sender != null) {
					controlBoker.peerLost(sender);
				} else {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Leaving Peer  has not a valid PeerCard" }, null);
					throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_LEAVE_ERROR,
							"The leaving Peer has not a valid PeerCard");
				}
			}
				break;
			case REQUEST_TO_LEAVE: {
				// check if the peer belongs to by Space
				if (!message.getSpaceDescriptor().getSpaceCard().getSpaceID()
						.equals(controlBoker.getMySpaceDescriptor().getSpaceCard().getSpaceID())) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Peer is not part of my Space..dropping it" + message.toString() }, null);
					return;
				}
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Request to leave: " + message.toString() }, null);
				if (message.getSpaceDescriptor() != null) {
					controlBoker.leaveRequest(message.getSpaceDescriptor());
				} else {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Not a valid Space descriptor" }, null);
					throw new SpaceModuleException(SpaceModuleErrorCode.SPACE_LEAVE_ERROR,
							"No valid space descritpro");
				}

			}
				break;
			case REQUEST_PEERCARD: {
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Request PeerCard: " + message.toString() }, null);
				// check if everything is correct
				if (controlBoker.getMySpaceDescriptor() == null) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "SpaceDescritor not yet ready.. " + message.toString() }, null);
					return;

				}

				if (!message.getSpaceDescriptor().getSpaceCard().getSpaceID()
						.equals(controlBoker.getMySpaceDescriptor().getSpaceCard().getSpaceID())) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Not part of the Space requester... " + message.toString() }, null);
					return;

				}

				// send my PeerCard
				SpaceMessage spaceMessage = new SpaceMessage(controlBoker.getMySpaceDescriptor(),
						SpaceMessageTypes.PEERCARD);

				// ...and wrap it as ChannelMessage
				List<String> channelName = new ArrayList<String>();
				channelName.add(getBrokerName());
				ChannelMessage channelMessage = new ChannelMessage(controlBoker.getmyPeerCard(),
						spaceMessage.toString(), channelName);

				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Sending Peer Card ..." }, null);
				communicationModule.send(channelMessage, this, sender);
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Peer Card message sent." }, null);
			}
				break;
			case PEERCARD: {
				// check if the peer belongs to by Space
				if (!message.getSpaceDescriptor().getSpaceCard().getSpaceID()
						.equals(controlBoker.getMySpaceDescriptor().getSpaceCard().getSpaceID())) {
					LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "Peer is not part of my Space..dropping it" + message.toString() }, null);
					return;
				}
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "PeerCard received: " + message.toString() }, null);
				// check if it is part of my space

				controlBoker.peerFound(sender);

			}
			default:
				break;
			}

		} catch (ClassCastException e) {
			throw new SpaceModuleException(SpaceModuleErrorCode.ERROR_MANAGING_SPACE_MESSAGE,
					"The message body is not valid: " + e);
		}

		LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
				new Object[] { "SpaceMessage queued" }, null);
	}

	public void configureSpaceChannel() {
		LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
				new Object[] { "Setting the broker group for the SpaceModule..." + getBrokerName() }, null);

		/*
		 * Register me as MessageListener for messages to the channel associated
		 * to my broker group
		 */
		communicationModule.addMessageListener(this, getBrokerName());
	}

	public void messageReceived(ChannelMessage message) {
		if (message == null || message.getContent() == null) {
			LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "The message received is not valid...dropping it." }, null);
			return;
		}
		try {

			BrokerMessage brokerMessage = null;
			try {
				brokerMessage = unmarshall(message.getContent());
			} catch (Exception e) {
				LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "The message received is not valid...dropping it." }, null);
			}

			switch (brokerMessage.getMType()) {
			case SpaceMessage:
				SpaceMessage spaceMessage = (SpaceMessage) unmarshall(message.getContent());
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Space message arrived" }, null);
				messageFromSpace(spaceMessage, message.getSender());
				break;

			}
		} catch (Exception e) {
			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Error during message receive: " + e.toString() }, null);
		}
	}

	public void handleSendError(ChannelMessage message, CommunicationConnectorException exception)
			throws SpaceModuleException {
		try {
			LogUtils.logWarn(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "Error during message queuing for message: " + message.toString() }, null);
			SpaceModuleException spaceException;

			if (message != null && message.getContent() != null) {
				// initialize the exception and throw it
				SpaceMessage aMessage = (SpaceMessage) unmarshall(message.getContent());
				switch (aMessage.getMessageType()) {
				case JOIN_REQUEST:
					spaceException = new SpaceModuleException(SpaceModuleErrorCode.ERROR_SENDING_JOIN_REQUEST,
							exception.toString());
					break;
				case JOIN_RESPONSE:
					spaceException = new SpaceModuleException(SpaceModuleErrorCode.ERROR_SENDING_JOIN_RESPONSE,
							exception.toString());
					break;
				case NEW_PEER:
					spaceException = new SpaceModuleException(SpaceModuleErrorCode.ERROR_SENDING_NEW_PEER_ADDED,
							exception.toString());
					break;
				default:
					spaceException = new SpaceModuleException(
							SpaceModuleErrorCode.ERROR_MANAGING_SPACE_MESSAGE, exception.toString());
					break;
				}
				throw spaceException;
			}
		} catch (SpaceMessageException e) {
			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "The message received is not valid...dropping it." + e.toString() }, null);
		} catch (Exception e2) {

			LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
					new Object[] { "The message received is not valid...dropping it." + e2.toString() }, null);
		}
	}

	public void destroySpace(SpaceCard spaceCard) {
		LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
				new Object[] { "Destroy the Space: " + spaceCard.toString() }, null);
		// to de-register the Space
		for (DiscoveryConnector dConnector : discoveryConnectors) {
			try {
				dConnector.deregisterSpace(spaceCard);
			} catch (Exception e) {
				LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Error during destroy Space: " + e.toString() }, null);
			}
		}
	}

	public void spaceLost(SpaceCard spaceCard) {
	}

	public void sharedObjectAdded(Object arg0, Object arg1) {
		if (arg0 != null) {
			if (arg0 instanceof DiscoveryConnector) {

				DiscoveryConnector connector = (DiscoveryConnector) arg0;
				// check if I already have the same connector
				if (!discoveryConnectors.contains(connector)) {
					connector.addSpaceListener(this);
					discoveryConnectors.add(connector);
				}
			} else if (arg0 instanceof CommunicationModule) {
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "New CommunicationModule added..." }, null);
				communicationModule = (CommunicationModule) arg0;
			} else if (arg0 instanceof ControlBroker) {
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "ControkBroker added..." }, null);
				controlBoker = (ControlBroker) arg0;
			}
		}
	}

	public void sharedObjectRemoved(Object arg0) {
		if (arg0 != null) {
			if (arg0 instanceof DiscoveryConnector) {
				DiscoveryConnector connector = (DiscoveryConnector) arg0;
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "Removing the DiscoveryConnector" }, null);
				discoveryConnectors.remove(connector);
				if (discoveryConnectors.size() == 0) {
					initialized = false;
				}
			} else if (arg0 instanceof CommunicationModule) {
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
						new Object[] { "CommunicationModule removed..." }, null);
				communicationModule = null;
				initialized = false;
			} else if (arg0 instanceof ControlBroker) {
				LogUtils.logDebug(context, SpaceModuleImpl.class, "SpaceModuleImpl",
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
			communicationModule.removeMessageListener(this, getBrokerName());
		if (discoveryConnectors != null && discoveryConnectors.size() > 0) {
			for (DiscoveryConnector dConnector : discoveryConnectors) {
				dConnector.removeSpaceListener(this);
			}
		}
	}

	public List<String> getPeersAddress() {
		if (communicationModule instanceof ConfigurableCommunicationModule) {
			ConfigurableCommunicationModule cCommMode = (ConfigurableCommunicationModule) communicationModule;
			List<String> members = cCommMode.getGroupMembers(getBrokerName());
			return members;
		}
		return null;
	}

	public void renewSpace(SpaceCard spaceCard) {
		if (spaceCard != null) {
			for (DiscoveryConnector discoveryConnector : discoveryConnectors) {
				try {
					discoveryConnector.announceSpace(spaceCard);
				} catch (DiscoveryConnectorException e) {
					LogUtils.logError(context, SpaceModuleImpl.class, "SpaceModuleImpl",
							new Object[] { "error during Space renew: " + spaceCard.toString() }, null);
				}
			}
		}
	}

	public BrokerMessage unmarshall(String message) {
		try {
			return GsonParserBuilder.getInstance().fromJson(message, SpaceMessage.class);
		} catch (Exception e) {

			throw new SpaceMessageException("Unable to unmashall SpaceMessage. Original message: " + message
					+ ". Full Stack: " + e.toString());
		}
	}
}
