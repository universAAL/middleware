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
package org.universAAL.middleware.managers.space;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.interfaces.space.Consts;
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.interfaces.space.SpaceStatus;
import org.universAAL.middleware.interfaces.space.model.IChannelDescriptor;
import org.universAAL.middleware.interfaces.space.model.ICommunicationChannels;
import org.universAAL.middleware.interfaces.space.model.IPeeringChannel;
import org.universAAL.middleware.interfaces.space.model.ISpace;
import org.universAAL.middleware.interfaces.space.model.ISpaceDescriptor;
import org.universAAL.middleware.interfaces.space.xml.model.ObjectFactory;
import org.universAAL.middleware.interfaces.space.xml.model.Space;
import org.universAAL.middleware.managers.api.MatchingResult;
import org.universAAL.middleware.managers.api.SpaceEventHandler;
import org.universAAL.middleware.managers.api.SpaceListener;
import org.universAAL.middleware.managers.api.SpaceManager;
import org.universAAL.middleware.managers.space.util.CheckPeerThread;
import org.universAAL.middleware.managers.space.util.Joiner;
import org.universAAL.middleware.managers.space.util.RefreshSpaceThread;
import org.universAAL.middleware.managers.space.util.SpaceSchemaEventHandler;

/**
 * The implementation of the SpaceManager and SpaceEventHandler
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @author Carsten Stockloew
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
@SuppressWarnings({ "rawtypes" })
public class SpaceManagerImpl implements SpaceEventHandler, SpaceManager,
		SharedObjectListener {

	private ModuleContext context;
	private ControlBroker controlBroker;
	private boolean initialized = false;
	// data structure for the MW
	/**
	 * The Space to which the MW is connected. Currently the MW can join to only
	 * one space
	 */
	private SpaceDescriptor currentSpace;
	private PeerCard myPeerCard = null;

	private PeerRole peerRole = DEFAULT_PEER_ROLE;
	private ChannelDescriptor peeringChannel;
	/**
	 * The list of Space discovered by the MW
	 */
	private Set<SpaceCard> foundSpaces;

	/**
	 * The set of peers joining to my Space
	 */
	private Map<String, PeerCard> peers;

	/**
	 * A map of Spaces managed from this MW instance
	 */
	private Map<String, SpaceDescriptor> managedSpaces;
	private Boolean pendingSpace = Boolean.FALSE;
	private Object spaceLock = new Object();
	private String spaceExtension;
	private ISpace spaceDefaultConfiguration;

	// thread
	private Joiner joiner;
	private ScheduledFuture joinerFuture;

	private CheckPeerThread checkPeerThread;
	private ScheduledFuture checkerFuture;

	private RefreshSpaceThread refreshSpaceThread;
	private ScheduledFuture refreshFuture;

	private String spaceConfigurationPath;
//	private JAXBContext jc;
//	private Unmarshaller unmarshaller;
	private boolean spaceValidation;
	private String spaceSchemaURL;
	private String spaceSchemaName;
	private int spaceLifeTime;
	private long waitBeforeClosingChannels;
	private long waitAfterJoinRequest;
	private String altConfigDir;

	private List<SpaceListener> listeners;

	private long TIMEOUT;

	// scheduler
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(10);

	public SpaceManagerImpl(ModuleContext context, String altDir) {
		this.context = context;
		this.altConfigDir = altDir;
		managedSpaces = new Hashtable<String, SpaceDescriptor>();
		foundSpaces = Collections.synchronizedSet(new HashSet<SpaceCard>());
		peers = new HashMap<String, PeerCard>();
		listeners = new ArrayList<SpaceListener>();
		try {
			TIMEOUT = Long.parseLong(System.getProperty(
					SpaceManager.COMUNICATION_TIMEOUT_KEY,
					SpaceManager.COMUNICATION_TIMEOUT_VALUE));
		} catch (Exception ex) {
			LogUtils.logError(
					context,
					SpaceManagerImpl.class,
					"SpaceManagerImpl",
					new Object[] { "intalization timeout, falling back to default value: "
							+ SpaceManager.COMUNICATION_TIMEOUT_VALUE }, null);
			TIMEOUT = Long.parseLong(SpaceManager.COMUNICATION_TIMEOUT_VALUE);
		}
	}

	public Map<String, SpaceDescriptor> getManagedSpaces() {
		return managedSpaces;
	}

	public Map<String, PeerCard> getPeers() {
		return peers;
	}

	public ISpace getSpaceDefaultConfiguration() {
		if (spaceDefaultConfiguration == null) {
			spaceDefaultConfiguration = readSpaceDefaultConfigurations();
		}
		return spaceDefaultConfiguration;
	}

	public Boolean getPendingSpace() {
		return this.pendingSpace;
	}

	public long getWaitAfterJoinRequest() {
		return waitAfterJoinRequest;
	}

	public SpaceDescriptor getSpaceDescriptor() {
		return currentSpace;
	}

	public PeerCard getMyPeerCard() {
		synchronized (this) {
			if (myPeerCard != null) {
				return myPeerCard;
			}
			loadPeerCard();
		}
		return myPeerCard;
	}

	public Set<SpaceCard> getSpaces() {
		synchronized (foundSpaces) {
			return foundSpaces;
		}
	}

	private void loadPeerCard() {
		final String METHOD = "loadPeerCard";
		// try to load peer ID
		String peerId = System.getProperty(
			org.universAAL.middleware.managers.space.util.Consts.PEER_ID);
		if (peerId == null) {
		    LogUtils.logDebug(context, SpaceManagerImpl.class, METHOD,
			    new Object[] {
				    "No PeerID specified as System Properties " },
			    null);
		}
		if (peerId == null) {
		    LogUtils.logDebug(context, SpaceManagerImpl.class, METHOD,
			    new Object[] {
				    "No PeerID specified as System Properties " },
			    null);
		}
		try {
		    Properties props = new Properties();
		    File f = new File(altConfigDir, PEER_ID_FILE);
		    if (!f.exists())
			f.createNewFile();
		    props.load(new FileInputStream(f));
		    peerId = props.getProperty("default");
		} catch (Exception e) {
		    LogUtils.logInfo(context, SpaceManagerImpl.class, METHOD,
			    new Object[] { "Failed to loead peerId from file: ",
				    PEER_ID_FILE, " in folder ", altConfigDir },
			    e);
		}
		// create PeerCard
		synchronized (this) {
		    if (peerId == null) {
			LogUtils.logDebug(context, SpaceManagerImpl.class, METHOD,
				new Object[] { "Creating the PeerCard..." }, null);
			myPeerCard = new PeerCard(peerRole, "", "");
			LogUtils.logInfo(context, SpaceManagerImpl.class, "init",
				new Object[] { "--->PeerCard created: "
					+ myPeerCard.toString() },
				null);

		    } else {
			myPeerCard = new PeerCard(peerId, peerRole);
		    }
		}
		// save peer ID
		try {
		    Properties props = new Properties();
		    props.setProperty("default", myPeerCard.getPeerID());
		    File f = new File(altConfigDir, PEER_ID_FILE);
		    if (!f.exists())
			f.createNewFile();
		    props.store(new FileOutputStream(f),
			    "Properties files that contains the peerId used by this peer");
		} catch (Exception e) {
		    LogUtils.logError(context, SpaceManagerImpl.class, METHOD,
			    new Object[] { "Failed to save peerId from file: ",
				    PEER_ID_FILE, " in folder ", altConfigDir },
			    e);
		}
	}

	public synchronized boolean init() {
		if (!initialized) {
			loadPeerCard();

			// fetching the services
			LogUtils.logDebug(context, SpaceManagerImpl.class, "init",
					new Object[] { "Fetching the ContextBroker..." }, null);
			Object[] cBrokers = context.getContainer().fetchSharedObject(
					context,
					new Object[] { ControlBroker.class.getName().toString() },
					this);
			if (cBrokers != null) {
				LogUtils.logDebug(context, SpaceManagerImpl.class, "init",
						new Object[] { "Found  ContextBrokers..." }, null);
				if (cBrokers[0] instanceof ControlBroker)
					controlBroker = (ControlBroker) cBrokers[0];
				else {
					initialized = false;
					return initialized;
				}
			} else {
				LogUtils.logWarn(context, SpaceManagerImpl.class, "init",
						new Object[] { "No ContextBroker found" }, null);
				initialized = false;
				return initialized;
			}

			// start the threads
			// Joiner -> Space joiner
			joiner = new Joiner(this, context);
			joinerFuture = scheduler.scheduleAtFixedRate(joiner, 0, 1,
					TimeUnit.SECONDS);

			// Configure the Space
			if (spaceConfigurationPath == null
					|| spaceConfigurationPath.length() == 0) {
				LogUtils.logWarn(
						context,
						SpaceManagerImpl.class,
						"init",
						new Object[] { "Space default configurations are null" },
						null);
				initialized = true;
			} else {
				LogUtils.logDebug(
						context,
						SpaceManagerImpl.class,
						"init",
						new Object[] { "Parse the Space default configurations" },
						null);
				spaceDefaultConfiguration = readSpaceDefaultConfigurations();
				initSpace(spaceDefaultConfiguration);
				initialized = true;
			}
		}
		return initialized;
	}

	/**
	 * Private method to manage the creation of a new Space starting from the
	 * default configurations
	 * 
	 * @param spaceDefaultConfiguration
	 *            Default Space configurations
	 * @return true if the creation succeeded, false otherwise
	 */
	public synchronized void initSpace(ISpace spaceDefaultConfiguration) {
		// configure the MW with the space configurations
		if (currentSpace != null) {
			// EXPLAIN Space has been already configured
			LogUtils.logDebug(context, SpaceManagerImpl.class, "initSpace",
					new Object[] { "The MW belongs to: "
							+ currentSpace.getSpaceCard().toString() }, null);
			return;
		}
		if (spaceDefaultConfiguration == null) {
			// EXPLAIN no configuration path given so we cannot initialize
			LogUtils.logDebug(
					context,
					SpaceManagerImpl.class,
					"initSpace",
					new Object[] {
							"No Space default configuration found on the path: ",
							spaceConfigurationPath, " giving up" }, null);
			return;
		}
		try {
			LogUtils.logDebug(context, SpaceManagerImpl.class, "initSpace",
					new Object[] { "Space default configuration found" }, null);
			// first look for existing Space with the same name as the
			// one reported in the default config.file
			List<SpaceCard> spaceCards = controlBroker
					.discoverSpace(buildSpaceFilter(spaceDefaultConfiguration));
			if (spaceCards != null && spaceCards.size() > 0) {
				LogUtils.logDebug(context, SpaceManagerImpl.class, "initSpace",
						new Object[] { "Default Space found" }, null);
				synchronized (foundSpaces) {
					this.foundSpaces.addAll(spaceCards);
				}
			} else {
				if (getMyPeerCard().getRole().equals(PeerRole.COORDINATOR)) {

					LogUtils.logInfo(
							context,
							SpaceManagerImpl.class,
							"initSpace",
							new Object[] { "No default Space found...creating it " },
							null);

					List<ChannelDescriptor> communicationChannels = new ArrayList<ChannelDescriptor>();
					// fetch the communication channels
					communicationChannels = getChannels(spaceDefaultConfiguration
							.getCommunicationChannels().getChannelDescriptor());
					// fetch the peering channel
					ChannelDescriptor peeringChannel = getChannel(spaceDefaultConfiguration
							.getPeeringChannel().getChannelDescriptor());
					// configure the MW channels
					if (controlBroker != null) {
						controlBroker.configurePeeringChannel(peeringChannel,
								getMyPeerCard().getPeerID());
						controlBroker.configureChannels(communicationChannels,
								getMyPeerCard().getPeerID());

						// create the new Space
						SpaceCard mySpace = new SpaceCard(
								getSpaceProperties(spaceDefaultConfiguration));
						mySpace.setSpaceLifeTime(spaceLifeTime);
						currentSpace = new SpaceDescriptor(mySpace,
								communicationChannels);
						// since coordinator and deployCoordinator matches,
						// configure the space Descriptor
						currentSpace.setDeployManager(getMyPeerCard());

						// announce the Space
						controlBroker.buildSpace(mySpace);

						// start threads
						refreshSpaceThread = new RefreshSpaceThread(context);
						refreshFuture = scheduler.scheduleAtFixedRate(
								refreshSpaceThread, 0, spaceLifeTime - 1,
								TimeUnit.SECONDS);

						// start the thread for management of Space
						checkPeerThread = new CheckPeerThread(context);
						checkerFuture = scheduler.scheduleAtFixedRate(
								checkPeerThread, 0, 1, TimeUnit.SECONDS);

						// add the Space created to the list of managed
						// spaces
						managedSpaces.put(mySpace.getSpaceID(), currentSpace);

						// notify to all the listeners a new Space has
						// been joined
						for (SpaceListener spaceListener : listeners) {
							spaceListener.spaceJoined(currentSpace);
						}
						peers.put(myPeerCard.getPeerID(), myPeerCard);

						// init the control broker
						LogUtils.logInfo(context, SpaceManagerImpl.class,
								"initSpace",
								new Object[] { "New Space created!" }, null);

					} else {
						LogUtils.logWarn(
								context,
								SpaceManagerImpl.class,
								"initSpace",
								new Object[] { "Control Broker is not initialize" },
								null);
					}

				} else {
					LogUtils.logInfo(
							context,
							SpaceManagerImpl.class,
							"initSpace",
							new Object[] {
									"No default Space found...waiting to join an Space as :",
									getMyPeerCard().getRole() }, null);
				}
			}
		} catch (Exception e) {
			LogUtils.logError(
					context,
					SpaceManagerImpl.class,
					"initSpace",
					new Object[] { "Error during Space initialization: ",
							e.toString() }, null);
		}
	}

	public void join(SpaceCard spaceCard) {
		if (currentSpace != null) {
			LogUtils.logWarn(
					context,
					SpaceManagerImpl.class,
					"join",
					new Object[] { "Cannot join to multiple Space. First leave the current Space " },
					null);
		}
		if (init()) {
			synchronized (spaceLock) {
				pendingSpace = true;
				LogUtils.logInfo(context, SpaceManagerImpl.class, "join",
						new Object[] { "--->Start the join phase to Space: "
								+ spaceCard.toString() }, null);
				LogUtils.logDebug(context, SpaceManagerImpl.class, "join",
						new Object[] { "Configure the peering channel..." },
						null);

				// fetch the default peering channel
				ChannelDescriptor defaultPeeringChannel = getChannel(spaceDefaultConfiguration
						.getPeeringChannel().getChannelDescriptor());
				// fetch the default peering channel URL
				String peeringChannelSerialized = spaceCard.getPeeringChannel();

				// If the default peering channel URL from the SpaceCard matches
				// with the default peering channel URL from the local
				// configuration file, then I use the defaultPeeringChannel
				// channel descriptor
				if (defaultPeeringChannel != null
						&& peeringChannelSerialized
								.equals(defaultPeeringChannel
										.getChannelDescriptorFileURL())) {
					peeringChannel = defaultPeeringChannel;
				} else {
					peeringChannel = new ChannelDescriptor(
							spaceCard.getPeeringChannelName(), "", null);
					peeringChannel
							.setChannelDescriptorFileURL(peeringChannelSerialized);
				}
				try {

					if (peeringChannelSerialized != null) {
						// InputStream channelValue = new
						// ByteArrayInputStream(peeringChannelSerialized.getBytes());
						// ChannelDescriptor peeringChannelD =
						// (ChannelDescriptor)unmarshaller.unmarshal(channelValue);
						// org.universAAL.middleware.interfaces.ChannelDescriptor
						// peeringChannel = getChannel(peeringChannelD);

						controlBroker.configurePeeringChannel(peeringChannel,
								getMyPeerCard().getPeerID());
						LogUtils.logInfo(
								context,
								SpaceManagerImpl.class,
								"join",
								new Object[] { "--->Peering channel configured!" },
								null);
					} else {
						LogUtils.logWarn(
								context,
								SpaceManagerImpl.class,
								"join",
								new Object[] { "Peering channel is null not able to join the Space" },
								null);
					}
					LogUtils.logInfo(context, SpaceManagerImpl.class, "join",
							new Object[] { "--->Sending join request..." },
							null);
					PeerCard spaceCoordinator = new PeerCard(
							spaceCard.getCoordinatorID(), PeerRole.COORDINATOR);
					controlBroker.join(spaceCoordinator, spaceCard);

				} catch (Exception e) {
					LogUtils.logError(context, SpaceManagerImpl.class, "join",
							new Object[] { "Error during Space join: "
									+ spaceCard.toString() }, null);
					pendingSpace = false;
				}
			}
		} else {
			LogUtils.logWarn(context, SpaceManagerImpl.class, "join",
					new Object[] { "Space Manager not initialized" }, null);
		}
	}

	public void cleanUpJoinRequest() {
		synchronized (spaceLock) {
			List<ChannelDescriptor> pendingPC = new ArrayList<ChannelDescriptor>();
			pendingPC.add(peeringChannel);
			controlBroker.resetModule(pendingPC);
			pendingSpace = false;
		}
	}

	public void spaceJoined(SpaceDescriptor descriptor) {
		if (init()) {
			LogUtils.logDebug(context, SpaceManagerImpl.class, "spaceJoined",
					new Object[] { "Joining to Space: "
							+ descriptor.getSpaceCard().toString() }, null);

			synchronized (spaceLock) {
				currentSpace = descriptor;
				pendingSpace = false;

				LogUtils.logInfo(context, SpaceManagerImpl.class,
						"spaceJoined", new Object[] { "--->Space Joined!" },
						null);
				try {
					spaceLock.notifyAll();
				} catch (Exception e) {
					LogUtils.logError(
							context,
							SpaceManagerImpl.class,
							"spaceJoined",
							new Object[] { "Error during notify: "
									+ e.toString() }, null);
				}
			}
			final PeerCard joinedPeer = getMyPeerCard();
			// creating Space channels
			List<ChannelDescriptor> communicationChannels = currentSpace
					.getBrokerChannels();
			if (communicationChannels != null) {
				controlBroker.configureChannels(communicationChannels,
						joinedPeer.getPeerID());
			}
			// start checking for members peers in the Space
			checkPeerThread = new CheckPeerThread(context);
			checkerFuture = scheduler.scheduleAtFixedRate(checkPeerThread, 0,
					1, TimeUnit.SECONDS);
			// add myself to the list of peers
			peerFound(joinedPeer);
			controlBroker.newPeerAdded(currentSpace.getSpaceCard(), joinedPeer);

			LogUtils.logInfo(context, SpaceManagerImpl.class, "spaceJoined",
					new Object[] { "--->Announced my presence!" }, null);

			for (SpaceListener spaceListener : listeners) {
				spaceListener.spaceJoined(currentSpace);
			}
		} else {
			LogUtils.logWarn(
					context,
					SpaceManagerImpl.class,
					"spaceJoined",
					new Object[] { "Space Manager is not initialized aborting." },
					null);
			pendingSpace = false;
		}
	}

	private Dictionary<String, String> buildSpaceFilter(ISpace space) {
		Dictionary<String, String> filters = new Hashtable<String, String>();
		if (space != null) {
			try {
				filters.put(Consts.SPACE_ID, space.getSpaceDescriptor()
						.getSpaceId());
				LogUtils.logDebug(context, SpaceManagerImpl.class,
						"buildSpaceFilter", new Object[] { "Filter created" },
						null);
			} catch (NullPointerException e) {
				LogUtils.logError(
						context,
						SpaceManagerImpl.class,
						"buildSpaceFilter",
						new Object[] { "Error while building Space filter...returning empty filter"
								+ e.toString() }, null);
				return filters;
			} catch (Exception e) {
				LogUtils.logError(
						context,
						SpaceManagerImpl.class,
						"buildSpaceFilter",
						new Object[] { "Error while building Space filter...returning empty filter"
								+ e.toString() }, null);
				return filters;
			}
		}
		return filters;
	}

	private List<ChannelDescriptor> getChannels(
			List<IChannelDescriptor> channels) {
		List<ChannelDescriptor> theChannels = new ArrayList<ChannelDescriptor>();

		for (IChannelDescriptor channel : channels) {
			ChannelDescriptor singleChannel = new ChannelDescriptor(
					channel.getChannelName(), channel.getChannelURL(),
					channel.getChannelValue());
			theChannels.add(singleChannel);
		}
		return theChannels;
	}

	private ChannelDescriptor getChannel(IChannelDescriptor channel) {
		ChannelDescriptor singleChannel = new ChannelDescriptor(
				channel.getChannelName(), channel.getChannelURL(),
				channel.getChannelValue());
		return singleChannel;
	}

	/**
	 * This method collects in a dictionary the properties associated with a new
	 * Space in order to announce them. The properties are read from the data
	 * structure Space. The properties added to the Space card are the
	 * name,id,description and coordinator ID and the peering channel serialized
	 * as XML string
	 * 
	 * @param space
	 * @return
	 */
	private Dictionary<String, String> getSpaceProperties(ISpace space) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		try {

			// general purpose properties
			properties.put(Consts.SPACE_NAME, space.getSpaceDescriptor()
					.getSpaceName());
			properties.put(Consts.SPACE_ID, space.getSpaceDescriptor()
					.getSpaceId());
			properties.put(Consts.SPACE_DESCRIPTION, space.getSpaceDescriptor()
					.getSpaceDescription());
			properties.put(Consts.SPACE_COORDINATOR, getMyPeerCard()
					.getPeerID());
			// URL where to fetch the peering channel
			properties
					.put(Consts.SPACE_PEERING_CHANNEL_URL, space
							.getPeeringChannel().getChannelDescriptor()
							.getChannelURL());
			properties.put(Consts.SPACE_PEERING_CHANNEL_NAME, space
					.getPeeringChannel().getChannelDescriptor()
					.getChannelName());
			properties.put(Consts.SPACE_PROFILE, space.getSpaceDescriptor()
					.getProfile());

		} catch (NullPointerException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
		return properties;
	}

	private File[] getReadbleFileList(String spaceConfigurationPath,
			final String[] extensions) {
		File spaceConfigDirectory = new File(spaceConfigurationPath);
		if (spaceConfigDirectory.canRead() == false) {
			LogUtils.logWarn(context, SpaceManagerImpl.class, "getFileList",
					new Object[] { "File: " + spaceConfigurationPath
							+ " cannot be read." }, null);
			return null;
		}
		File[] spaces = spaceConfigDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (new File(dir, name).canRead() == false) {
					return false;
				}
				for (int i = 0; i < extensions.length; i++) {
					if (name.endsWith(extensions[i]))
						return true;
				}
				return false;
			}
		});
		return spaces;
	}

	public ISpace readSpaceDefaultConfigurations() {
		final String METHOD = "readSpaceDefaultConfigurations";
		LogUtils.logDebug(context, SpaceManagerImpl.class, METHOD,
				new Object[] { "Reading Space configuration." }, null);
		String spaceConfigurationPath = this.spaceConfigurationPath;
		File spaceConfigDirectory = new File(spaceConfigurationPath);

		// debug output: log the current path
		String currPath = "";
		try {
			currPath = new java.io.File(".").getCanonicalPath();
		} catch (IOException e) {
		}
		LogUtils.logDebug(context, SpaceManagerImpl.class, METHOD,
				new Object[] { "Reading Space configuration from directory: ",
						spaceConfigDirectory.toString(),
						" The current path is: ", currPath }, null);

		// get the list of config files
		File[] spaces = getReadbleFileList(spaceConfigurationPath,
				new String[] { ".space" });
		if (spaces == null || spaces.length == 0) {
			LogUtils.logWarn(
					context,
					SpaceManagerImpl.class,
					METHOD,
					new Object[] {
							"File: ",
							spaceConfigurationPath,
							" cannot be read or it does not containing any configuration files. Trying alternative: ",
							altConfigDir }, null);

			spaceConfigurationPath = altConfigDir;
			spaces = getReadbleFileList(spaceConfigurationPath,
					new String[] { ".space" });
		}

		if (spaces == null || spaces.length == 0) {
			LogUtils.logError(
					context,
					SpaceManagerImpl.class,
					METHOD,
					new Object[] {
							"Both directory ",
							spaceConfigurationPath,
							" and ",
							altConfigDir,
							" are empty or unreadable. No Space configuration found", },
					null);
			return null;
		}

		ISpace space = null;
		String value = "<![CDATA[<config xmlns=\"urn:org:jgroups\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:org:jgroups http://www.jgroups.org/schema/JGroups-3.0.xsd\"> <UDP mcast_port=\"${jgroups.udp.mcast_port:45588}\" tos=\"8\" ucast_recv_buf_size=\"20M\" ucast_send_buf_size=\"640K\" mcast_recv_buf_size=\"25M\" mcast_send_buf_size=\"640K\" loopback=\"true\" discard_incompatible_packets=\"true\" max_bundle_size=\"64K\" max_bundle_timeout=\"30\" ip_ttl=\"${jgroups.udp.ip_ttl:8}\" enable_bundling=\"true\" enable_diagnostics=\"false\" thread_naming_pattern=\"cl\" timer_type=\"new\" timer.min_threads=\"4\" timer.max_threads=\"10\" timer.keep_alive_time=\"3000\" timer.queue_max_size=\"500\" thread_pool.enabled=\"true\" thread_pool.min_threads=\"2\" thread_pool.max_threads=\"8\" thread_pool.keep_alive_time=\"5000\" thread_pool.queue_enabled=\"true\" thread_pool.queue_max_size=\"10000\" thread_pool.rejection_policy=\"discard\" oob_thread_pool.enabled=\"true\" oob_thread_pool.min_threads=\"1\" oob_thread_pool.max_threads=\"8\" oob_thread_pool.keep_alive_time=\"5000\" oob_thread_pool.queue_enabled=\"false\" oob_thread_pool.queue_max_size=\"100\" oob_thread_pool.rejection_policy=\"Run\"/> <PING timeout=\"2000\" num_initial_members=\"3\"/> <MERGE2 max_interval=\"30000\" min_interval=\"10000\"/> <FD_SOCK/> <FD_ALL/> <VERIFY_SUSPECT timeout=\"1500\" /> <BARRIER /> <pbcast.NAKACK exponential_backoff=\"300\" xmit_stagger_timeout=\"200\" use_mcast_xmit=\"false\" discard_delivered_msgs=\"true\"/> <UNICAST /> <pbcast.STABLE stability_delay=\"1000\" desired_avg_gossip=\"50000\" max_bytes=\"4M\"/> <pbcast.GMS print_local_addr=\"true\" join_timeout=\"3000\" view_bundling=\"true\"/> <UFC max_credits=\"2M\" min_threshold=\"0.4\"/> <MFC max_credits=\"2M\" min_threshold=\"0.4\"/> <FRAG2 frag_size=\"60K\" /> <pbcast.STATE_TRANSFER /> <pbcast.FLUSH /> </config>]]>";
		String url = "file:/mnt/sdcard/data/felix-conf-1.3.3/conf/etc/udp.xml";

		space = new Space();
		// space.setAdmin("admin");
		// space.setOwner("owner");
		space.setSecurity("security");
		ISpaceDescriptor sd = new Space.SpaceDescriptor();
		sd.setSpaceName("myHome3");
		sd.setProfile("HomeSpace");
		sd.setSpaceId("8888");
		sd.setSpaceDescription("Super Domestic Home");
		space.setSpaceDescriptor(sd);
		IPeeringChannel pc = new Space.PeeringChannel();
		IChannelDescriptor cd = new org.universAAL.middleware.interfaces.space.xml.model.ChannelDescriptor();
		cd.setChannelName("mw.modules.aalspace.osgi");
		cd.setChannelURL(url);
		cd.setChannelValue(value);
		pc.setChannelDescriptor(cd);
		space.setPeeringChannel(pc);
		ICommunicationChannels ccs = new Space.CommunicationChannels();
		IChannelDescriptor cd1 = new org.universAAL.middleware.interfaces.space.xml.model.ChannelDescriptor();
		cd1.setChannelName("mw.brokers.control.osgi"); // ONLY NAMES
		// ARE NEEDED
		cd1.setChannelURL(url);
		cd1.setChannelValue(value);
		IChannelDescriptor cd2 = new org.universAAL.middleware.interfaces.space.xml.model.ChannelDescriptor();
		cd2.setChannelName("mw.bus.context.osgi");
		cd2.setChannelURL(url);
		cd2.setChannelValue(value);
		IChannelDescriptor cd3 = new org.universAAL.middleware.interfaces.space.xml.model.ChannelDescriptor();
		cd3.setChannelName("mw.bus.service.osgi");
		cd3.setChannelURL(url);
		cd3.setChannelValue(value);
		IChannelDescriptor cd4 = new org.universAAL.middleware.interfaces.space.xml.model.ChannelDescriptor();
		cd4.setChannelName("mw.bus.ui.osgi");
		cd4.setChannelURL(url);
		cd.setChannelValue(value);
		// ccs.getChannelDescriptor().add(cd1);
		// ccs.getChannelDescriptor().add(cd2);
		// ccs.getChannelDescriptor().add(cd3);
		// ccs.getChannelDescriptor().add(cd4);
		ccs.addChannelDescriptor(cd1);
		ccs.addChannelDescriptor(cd2);
		ccs.addChannelDescriptor(cd3);
		ccs.addChannelDescriptor(cd4);
		space.setCommunicationChannels(ccs);
		if (space != null) {
		    return parametrizeChannelNames(space);
		} else {
		    LogUtils.logWarn(context, SpaceManagerImpl.class,
			    "readAALSpaceDefaultConfigurations",
			    new Object[] {
				    "Unable to parse default AALSpace configuration" },
			    null);
		    return null;
		}
	}

//	private ISpace loadConfigurationFromJSON(File[] spaces) {
//		return null;
//	}

//	private ISpace loadConfigurationFromXML(File[] spaces) {
//		final String METHOD = "loadConfigurationFromXML";
//		File xml = spaces[0];
//
//		if (spaces.length > 1) {
//			LogUtils.logWarn(
//					context,
//					SpaceManagerImpl.class,
//					METHOD,
//					new Object[] {
//							"Multiple Space configuration found but only using the file ",
//							xml.getAbsolutePath() }, null);
//		}
//		LogUtils.logDebug(context, SpaceManagerImpl.class, METHOD,
//				new Object[] { "Loading Space configuration from the file ",
//						xml.getAbsolutePath() }, null);
//
//		ISpace space = null;
//		try {
//			loadXMLParser();
//			space = (ISpace) unmarshaller.unmarshal(xml);
//			// parametrize the channels
//			space = parametrizeChannelNames(space);
//		} catch (Exception ex) {
//			LogUtils.logError(context, SpaceManagerImpl.class, METHOD,
//					new Object[] { "Failed to parse Space configuration from ",
//							xml.getAbsolutePath() }, ex);
//			return null;
//		}
//
//		return space;
//	}

	/**
	 * This methods modifies the name of the peering channel and of the
	 * communication channels, it adds the suffix SpaceID to the end of the
	 * channel name E.g. x.y where x = name of the broker, y = SpaceID
	 * 
	 * @param space
	 * @return
	 */
	private ISpace parametrizeChannelNames(ISpace space) {
		// change the peering channel
		String peeringChannelName = space.getPeeringChannel()
				.getChannelDescriptor().getChannelName();
		String spaceID = space.getSpaceDescriptor().getSpaceId();
		space.getPeeringChannel().getChannelDescriptor()
				.setChannelName(peeringChannelName + spaceID);

		for (IChannelDescriptor channelDescriptor : space
				.getCommunicationChannels().getChannelDescriptor()) {
			channelDescriptor.setChannelName(channelDescriptor.getChannelName()
					+ spaceID);
		}
		return space;
	}

//	private void loadXMLParser() throws Exception {
//		if (jc != null)
//			return;
//
//		jc = JAXBContext.newInstance(ObjectFactory.class);
//		unmarshaller = jc.createUnmarshaller();
//		// XML Schema validation
//		if (spaceValidation && spaceConfigurationPath != null
//				&& spaceSchemaName != null) {
//			loadXMLValidation();
//		}
//
//	}

//	private void loadXMLValidation() {
//		final String METHOD = "loadXMLValidation";
//		LogUtils.logDebug(context, SpaceManagerImpl.class, METHOD,
//				new Object[] { "Initialize Space schema validation" }, null);
//		SchemaFactory sf = SchemaFactory
//				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//		File spaceSchemaFile = new File(spaceSchemaURL + File.separatorChar
//				+ spaceSchemaName);
//		Schema spaceSchema = null;
//		if (spaceSchemaFile.canRead() == false) {
//			LogUtils.logWarn(context, SpaceManagerImpl.class, METHOD,
//					new Object[] { "Unable to read Space Scham from path: "
//							+ spaceSchemaFile.getAbsolutePath() }, null);
//			return;
//		}
//		try {
//			spaceSchema = sf.newSchema(spaceSchemaFile);
//			unmarshaller.setSchema(spaceSchema);
//			unmarshaller.setEventHandler(new SpaceSchemaEventHandler(context));
//
//		} catch (Exception ex) {
//			LogUtils.logError(
//					context,
//					SpaceManagerImpl.class,
//					METHOD,
//					new Object[] { "Error during Space schema initialization for validatin XML, it will not be used" },
//					ex);
//		}
//	}

	public void loadConfigurations(Dictionary configurations) {
		LogUtils.logDebug(context, SpaceManagerImpl.class,
				"loadConfigurations",
				new Object[] { "Updating SpaceManager properties" }, null);
		if (configurations == null) {
			LogUtils.logWarn(context, SpaceManagerImpl.class,
					"loadConfigurations",
					new Object[] { "SpaceManager properties are null!!!" },
					null);
			return;
		}
		LogUtils.logDebug(context, SpaceManagerImpl.class,
				"loadConfigurations", new Object[] { "Fetching the PeerRole" },
				null);
		String role = (String) configurations
				.get(org.universAAL.middleware.managers.space.util.Consts.PEER_ROLE);
		String roleOverride = System
				.getProperty(org.universAAL.middleware.managers.space.util.Consts.PEER_ROLE);
		if (roleOverride != null) {
			role = roleOverride;
		}
		if (role == null) {
			role = DEFAULT_PEER_ROLE.toString();
			LogUtils.logWarn(
					context,
					SpaceManagerImpl.class,
					"loadConfigurations",
					new Object[] {
							"No role found in the configuration: The role is null, configuring as default role (",
							role, ")" }, null);
		}
		try {
			peerRole = PeerRole.valueOf(role);
		} catch (Exception e) {
			LogUtils.logError(context, SpaceManagerImpl.class,
					"loadConfigurations", new Object[] {
							"Unable to initialize the peer with the role: ",
							role }, e);
			LogUtils.logInfo(
					context,
					SpaceManagerImpl.class,
					"loadConfigurations",
					new Object[] {
							"Failed to load role from configuration, using default role...configuring as regular (",
							DEFAULT_PEER_ROLE, ")" }, null);
			peerRole = DEFAULT_PEER_ROLE;
		}

		LogUtils.logDebug(context, SpaceManagerImpl.class,
				"loadConfigurations",
				new Object[] { "Fetching Space default configurations" }, null);
		spaceConfigurationPath = (String) configurations
				.get(org.universAAL.middleware.managers.space.util.Consts.SPACE_CONFIGURATION_PATH);
		try {
			if (spaceConfigurationPath == null) {
				LogUtils.logWarn(
						context,
						SpaceManagerImpl.class,
						"loadConfigurations",
						new Object[] { "Space default configurations are null!" },
						null);
			} else {
				// Resolving the relative path to absolute path
				File config = new File(spaceConfigurationPath);
				spaceConfigurationPath = config.getCanonicalPath();
				if (config.isDirectory() == false) {
					LogUtils.logWarn(
							context,
							SpaceManagerImpl.class,
							"loadConfigurations",
							new Object[] { "Space default configurations ",
									spaceConfigurationPath,
									" does not point to a directory or is not readable" },
							null);
				} else {
					LogUtils.logInfo(context, SpaceManagerImpl.class,
							"loadConfigurations", new Object[] {
									"Space default configurations fetched: ",
									spaceConfigurationPath }, null);
				}
			}
		} catch (IOException e) {
			LogUtils.logError(
					context,
					SpaceManagerImpl.class,
					"loadConfigurations",
					new Object[] {
							"Space default configurations is set by property \"spaceConfigurationPath\" but it points to invalid location ",
							spaceConfigurationPath }, null);
			spaceConfigurationPath = null;
		}
		LogUtils.logDebug(context, SpaceManagerImpl.class,
				"loadConfigurations",
				new Object[] { "Fetching Space extension" }, null);
		spaceExtension = (String) configurations
				.get(org.universAAL.middleware.managers.space.util.Consts.SPACE_EXTENSION);
		spaceValidation = Boolean
				.parseBoolean((String) configurations
						.get(org.universAAL.middleware.managers.space.util.Consts.SPACE_VALIDATION));
		spaceSchemaURL = (String) configurations
				.get(org.universAAL.middleware.managers.space.util.Consts.SPACE_SCHEMA_URL);
		spaceLifeTime = Integer
				.parseInt((String) configurations
						.get(org.universAAL.middleware.managers.space.util.Consts.SPACE_LIFETIME));

		spaceSchemaName = (String) configurations
				.get(org.universAAL.middleware.managers.space.util.Consts.SPACE_SCHEMA_NAME);
		waitBeforeClosingChannels = Long
				.parseLong((String) configurations
						.get(org.universAAL.middleware.managers.space.util.Consts.WAIT_BEFEORE_CLOSING_CHANNEL));
		waitAfterJoinRequest = Long
				.parseLong((String) configurations
						.get(org.universAAL.middleware.managers.space.util.Consts.WAIT_BEFEORE_CLOSING_CHANNEL));
	}

	public void joinRequest(SpaceCard spaceCard, PeerCard peer) {
		if (init()) {
			if (spaceCard != null && peer != null
					&& spaceCard.getSpaceID() != null) {
				LogUtils.logInfo(context, SpaceManagerImpl.class,
						"joinRequest", new Object[] { "---> Peer:"
								+ peer.getPeerID().toString()
								+ " requests to join to the Space: " }, null);

				if (peers.containsKey(peer.getPeerID())) {
					LogUtils.logError(
							context,
							SpaceManagerImpl.class,
							"joinRequest",
							new Object[] { "A peer with this ID is already available in the space. Ignoring request" },
							null);
					return;
				}

				if (!managedSpaces.containsKey(spaceCard.getSpaceID())) {
					LogUtils.logWarn(
							context,
							SpaceManagerImpl.class,
							"joinRequest",
							new Object[] { "Received a join request to an Space not managed: my Space: "
									+ currentSpace.getSpaceCard().getSpaceID()
									+ " while received: "
									+ spaceCard.getSpaceID() }, null);
				} else {
					// send unicast message to the peer with the space
					// descriptor
					LogUtils.logDebug(context, SpaceManagerImpl.class,
							"joinRequest",
							new Object[] { "Sending the space descriptor..." },
							null);
					// update the peers
					// add the new peer to the map of peers
					peerFound(peer);

					controlBroker.addNewPeer(currentSpace, peer);
					LogUtils.logDebug(context, SpaceManagerImpl.class,
							"joinRequest",
							new Object[] { "Space descriptor sent!" }, null);
					// newPeerJoined(peer)

					// announce bcast the new peer
					LogUtils.logDebug(context, SpaceManagerImpl.class,
							"joinRequest",
							new Object[] { "Announcing the new peer..." }, null);
				}
			} else
				LogUtils.logDebug(context, SpaceManagerImpl.class,
						"joinRequest",
						new Object[] { "Invalid join request parameter" }, null);
		} else {
			LogUtils.logWarn(context, SpaceManagerImpl.class, "joinRequest",
					new Object[] { "Space Manager not initialized" }, null);
		}
	}

	public synchronized void newSpacesFound(Set<SpaceCard> spaceCards) {
		if (spaceCards != null) {
			synchronized (foundSpaces) {
				foundSpaces = spaceCards;
			}
			if (foundSpaces.size() > 0) {
				LogUtils.logTrace(
						context,
						SpaceManagerImpl.class,
						"newSpacesFound",
						new Object[] { "--->The list of Spaces has been updated:"
								+ foundSpaces.toString() }, null);
			}
		}
	}

	public synchronized void peerFound(PeerCard peer) {
		if (peer != null && !peers.containsKey(peer.getPeerID())) {
			LogUtils.logInfo(context, SpaceManagerImpl.class, "peerFound",
					new Object[] { "--->The Peer: "
							+ peer.getPeerID().toString()
							+ " joins the Space: " }, null);
			peers.put(peer.getPeerID(), peer);
			for (SpaceListener list : listeners) {
				list.peerJoined(peer);
			}
		}
	}

	public synchronized void peerLost(PeerCard peer) {
		if (peer != null) {
			LogUtils.logInfo(context, SpaceManagerImpl.class, "peerLost",
					new Object[] { "--->Peer +" + peer.getPeerID()
							+ " left the Space" }, null);
			peers.remove(peer.getPeerID());
			for (SpaceListener list : listeners) {
				list.peerLost(peer);
			}
		}
	}

	public void sharedObjectAdded(Object sharedObj, Object removeHook) {
		if (sharedObj instanceof ControlBroker) {
			LogUtils.logDebug(context, SpaceManagerImpl.class,
					"sharedObjectAdded",
					new Object[] { "ControlBroker service added" }, null);
			this.controlBroker = (ControlBroker) sharedObj;
		}
	}

	public void sharedObjectRemoved(Object removeHook) {
		if (removeHook instanceof ControlBroker) {
			LogUtils.logDebug(context, SpaceManagerImpl.class,
					"sharedObjectRemoved",
					new Object[] { "ControlBroker service removed" }, null);
			this.controlBroker = null;
			initialized = false;
		}
	}

	public synchronized void leaveRequest(SpaceDescriptor spaceDescriptor) {
		if (spaceDescriptor != null) {
			// stop the management thread
			checkerFuture.cancel(true);
			if (refreshFuture != null)
				refreshFuture.cancel(true);

			ChannelDescriptor peeringChannel = new ChannelDescriptor(
					spaceDescriptor.getSpaceCard().getPeeringChannelName(), "",
					null);
			List<ChannelDescriptor> channels = new ArrayList<ChannelDescriptor>();
			channels.add(peeringChannel);
			channels.addAll(spaceDescriptor.getBrokerChannels());
			LogUtils.logInfo(context, SpaceManagerImpl.class, "leaveRequest",
					new Object[] { "--->Leaving the Space: "
							+ spaceDescriptor.getSpaceCard().getSpaceName() },
					null);
			controlBroker.resetModule(channels);
			// we assume the current space is the only one
			currentSpace = null;
			peers.clear();
		}
	}

	public void leaveSpace(SpaceDescriptor spaceDescriptor) {
		if (init()) {
			if (spaceDescriptor != null
					&& managedSpaces.containsKey(spaceDescriptor.getSpaceCard()
							.getSpaceID())) {
				LogUtils.logInfo(
						context,
						SpaceManagerImpl.class,
						"leaveSpace",
						new Object[] { "--->Leaving a managed Space: "
								+ spaceDescriptor.getSpaceCard().getSpaceName() },
						null);
				closeManagedSpace(spaceDescriptor);
			} else if (spaceDescriptor.getSpaceCard().getSpaceID()
					.equals(currentSpace.getSpaceCard().getSpaceID())) {
				// send a leave message
				LogUtils.logInfo(
						context,
						SpaceManagerImpl.class,
						"leaveSpace",
						new Object[] { "--->Leaving the Space: "
								+ spaceDescriptor.getSpaceCard().getSpaceName() },
						null);
				PeerCard spaceCoordinator = new PeerCard(spaceDescriptor
						.getSpaceCard().getCoordinatorID(),
						PeerRole.COORDINATOR);
				controlBroker.leaveSpace(spaceCoordinator,
						spaceDescriptor.getSpaceCard());
				LogUtils.logDebug(context, SpaceManagerImpl.class,
						"leaveSpace", new Object[] { "Leave message sent!" },
						null);

				// stop the management thread
				checkerFuture.cancel(true);
				if (refreshFuture != null)
					refreshFuture.cancel(true);

				ChannelDescriptor peeringChannel = new ChannelDescriptor(
						spaceDescriptor.getSpaceCard().getPeeringChannelName(),
						"", null);
				List<ChannelDescriptor> channels = new ArrayList<ChannelDescriptor>();
				channels.add(peeringChannel);
				channels.addAll(spaceDescriptor.getBrokerChannels());
				controlBroker.resetModule(channels);
				// we assume the current space is the only one
				currentSpace = null;
				// reset list of peers
				peers.clear();

			}

			for (SpaceListener elem : listeners) {

				elem.spaceLost(spaceDescriptor);
			}

		} else {
			LogUtils.logWarn(context, SpaceManagerImpl.class, "leaveSpace",
					new Object[] { "Space Manager not initialized" }, null);
		}
	}

	public void dispose() {
		// remove me as listener
		context.getContainer().removeSharedObjectListener(this);
		// workaround waiting for
		// http://forge.universaal.org/gf/project/middleware/tracker/?action=TrackerItemEdit&tracker_item_id=270
		if (controlBroker != null) {
			controlBroker.sharedObjectRemoved(this);
		}
		scheduler.shutdownNow();
		scheduler.shutdown();
		if (init()) {

			if (!managedSpaces.isEmpty()) {
				LogUtils.logInfo(context, SpaceManagerImpl.class, "dispose",
						new Object[] { "Closing all the managed Spaces" }, null);
				try {
					for (String spaceID : managedSpaces.keySet()) {
						closeManagedSpace(managedSpaces.get(spaceID));
					}
				} catch (Exception e) {
					LogUtils.logError(context, SpaceManagerImpl.class,
							"dispose", new Object[] { "Error during dispose: "
									+ e.toString() }, null);
				}
			} else {
				if (currentSpace != null)
					leaveSpace(currentSpace);

			}
		} else {
			LogUtils.logWarn(context, SpaceManagerImpl.class, "dispose",
					new Object[] { "Space Manager not initialized" }, null);
		}
		currentSpace = null;
		managedSpaces.clear();
		synchronized (foundSpaces) {
			foundSpaces.clear();
		}
		initialized = false;
	}

	/**
	 * Destroy all the managed Space
	 * 
	 * @param spaceDescriptor
	 */
	private void closeManagedSpace(SpaceDescriptor spaceDescriptor) {
		controlBroker.requestToLeave(spaceDescriptor);
		try {
			Thread.sleep(waitBeforeClosingChannels);
		} catch (InterruptedException e) {
			LogUtils.logError(context, SpaceManagerImpl.class,
					"closeManagedSpace", new Object[] { "Error during wait: "
							+ e.toString() }, null);
		}
		ChannelDescriptor peeringChannel = new ChannelDescriptor(
				spaceDescriptor.getSpaceCard().getPeeringChannelName(), "",
				null);
		List<ChannelDescriptor> channels = new ArrayList<ChannelDescriptor>();
		channels.add(peeringChannel);
		channels.addAll(spaceDescriptor.getBrokerChannels());
		controlBroker.resetModule(channels);
		controlBroker.destroySpace(spaceDescriptor.getSpaceCard());
	}

	public void addSpaceListener(SpaceListener listener) {
		if (listener != null && !listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeSpaceListener(SpaceListener listener) {
		if (listener != null)
			listeners.remove(listener);
	}

	public void setListOfPeers(Map<String, PeerCard> peers) {
		// verify if among the peers the coordinator is present. If not the
		// coordinator crashed
		if (currentSpace != null
				&& !peers.keySet().contains(
						currentSpace.getSpaceCard().getCoordinatorID())) {
			// coordinator crashed, leave from the Space

			leaveRequest(currentSpace);
		} else
			this.peers = peers;
	}

	public SpaceStatus getSpaceStatus() {
		return null;
	}

	public void mpaInstalled(SpaceDescriptor spaceDescriptor) {
		controlBroker.signalSpaceStatus(SpaceStatus.INSTALLED_UAAP,
				spaceDescriptor);
	}

	public void mpaInstalling(SpaceDescriptor spaceDescriptor) {
		// send a event notification to the Space
		controlBroker.signalSpaceStatus(SpaceStatus.INSTALLING_UAAP,
				spaceDescriptor);
	}

	public void spaceEvent(SpaceStatus newStatus) {
		LogUtils.logInfo(
				context,
				SpaceManagerImpl.class,
				"spaceEvent",
				new Object[] { "--->New event from Space: "
						+ newStatus.toString() }, null);

		for (SpaceListener elem : listeners) {
			elem.spaceStatusChanged(newStatus);
		}
	}

	public MatchingResult getMatchingPeers(Map<String, Serializable> filter) {
		final int limit = getPeers().size();
		final long timeout = TIMEOUT;
		final Map<PeerCard, Map<String, Serializable>> result = controlBroker
				.findMatchingPeers(filter, limit, (int) timeout);
		final MatchingResult response = new MatchingResultImpl(result);
		return response;
	}

	public Map<String, Serializable> getPeerAttributes(List<String> attributes,
			PeerCard target) {
		final int limit = 1;
		final long timeout = TIMEOUT;
		final Map<String, Serializable> result = controlBroker
				.requestPeerAttributes(attributes, target, limit, (int) timeout);
		return result;
	}
}
