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
package org.universAAL.middleware.managers.aalspace;

import java.io.File;
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
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceStatus;
import org.universAAL.middleware.interfaces.aalspace.Consts;
import org.universAAL.middleware.interfaces.aalspace.model.Aalspace;
import org.universAAL.middleware.interfaces.aalspace.model.ObjectFactory;
import org.universAAL.middleware.managers.aalspace.util.AALSpaceSchemaEventHandler;
import org.universAAL.middleware.managers.aalspace.util.CheckPeerThread;
import org.universAAL.middleware.managers.aalspace.util.Joiner;
import org.universAAL.middleware.managers.aalspace.util.RefreshAALSpaceThread;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.MatchingResult;
import org.xml.sax.SAXException;

/**
 * The implementation of the AALSpaceManager and AALSpaceEventHandler
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class AALSpaceManagerImpl implements AALSpaceEventHandler,
        AALSpaceManager, SharedObjectListener {

    private ModuleContext context;
    private ControlBroker controlBroker;
    private boolean initialized = false;
    // data structure for the MW
    /**
     * The AALSpace to which the MW is connected. Currently the MW can join to
     * only one AAL space
     */
    private AALSpaceDescriptor currentAALSpace;
    private PeerCard myPeerCard;
    private PeerRole peerRole;
    private ChannelDescriptor peeringChannel;
    /**
     * The list of AALSpace discovered by the MW
     */
    private Set<AALSpaceCard> foundAALSpaces;

    /**
     * The set of peers joining to my AAL Space
     */
    private Map<String, PeerCard> peers;

    /**
     * A map of AALSpaces managed from this MW instance
     */
    private Map<String, AALSpaceDescriptor> managedAALspaces;
    private Boolean pendingAALSpace = new Boolean(false);
    private String spaceExtension;
    private Aalspace aalSpaceDefaultConfiguration;

    // thread
    private Joiner joiner;
    private ScheduledFuture joinerFuture;

    private CheckPeerThread checkPeerThread;
    private ScheduledFuture checkerFuture;

    private RefreshAALSpaceThread refreshAALSpaceThread;
    private ScheduledFuture refreshFuture;

    private String aalSpaceConfigurationPath;
    private JAXBContext jc;
    private Unmarshaller unmarshaller;
    private boolean aalSpaceValidation;
    private String aalSpaceSchemaURL;
    private String aalSpaceSchemaName;
    private int aalSpaceLifeTime;
    private long waitBeforeClosingChannels;
    private long waitAfterJoinRequest;
    private String altConfigDir;

    private List<AALSpaceListener> listeners;

    private long TIMEOUT;

    // scheduler
    private final ScheduledExecutorService scheduler = Executors
            .newScheduledThreadPool(10);

    public AALSpaceManagerImpl(ModuleContext context, String altConfigDir) {
        this.context = context;
        this.altConfigDir = altConfigDir;
        try {
            jc = JAXBContext.newInstance(ObjectFactory.class);
            unmarshaller = jc.createUnmarshaller();
            managedAALspaces = new Hashtable<String, AALSpaceDescriptor>();
            foundAALSpaces = Collections
                    .synchronizedSet(new HashSet<AALSpaceCard>());
            peers = new HashMap<String, PeerCard>();
            listeners = new ArrayList<AALSpaceListener>();
        } catch (JAXBException e) {

            LogUtils.logError(
                    context,
                    AALSpaceManagerImpl.class,
                    "AALSpaceManagerImpl",
                    new Object[] { "Error during AALSpace parser intialization: "
                            + e.toString() }, null);
        }
        try {
            TIMEOUT = Long.parseLong(System.getProperty(
                    AALSpaceManager.COMUNICATION_TIMEOUT_KEY,
                    AALSpaceManager.COMUNICATION_TIMEOUT_VALUE));
        } catch (Exception ex) {
            LogUtils.logError(
                    context,
                    AALSpaceManagerImpl.class,
                    "AALSpaceManagerImpl",
                    new Object[] { "intalization timeout, falling back to default value: "
                            + AALSpaceManager.COMUNICATION_TIMEOUT_VALUE },
                    null);
            TIMEOUT = Long
                    .parseLong(AALSpaceManager.COMUNICATION_TIMEOUT_VALUE);
        }

    }

    public Map<String, AALSpaceDescriptor> getManagedAALSpaces() {
        return managedAALspaces;
    }

    public Map<String, PeerCard> getPeers() {
        return peers;
    }

    public Aalspace getAalSpaceDefaultConfiguration() {
        return aalSpaceDefaultConfiguration;
    }

    public Boolean getPendingAALSpace() {
        return this.pendingAALSpace;
    }

    public long getWaitAfterJoinRequest() {
        return waitAfterJoinRequest;
    }

    public AALSpaceDescriptor getAALSpaceDescriptor() {
        return currentAALSpace;
    }

    public PeerCard getMyPeerCard() {
        return myPeerCard;
    }

    public Set<AALSpaceCard> getAALSpaces() {
        synchronized (foundAALSpaces) {
            return foundAALSpaces;
        }
    }

    public synchronized boolean init() {
        if (!initialized) {

            LogUtils.logDebug(context, AALSpaceManagerImpl.class, "init",
                    new Object[] { "Creating the PeerCard..." }, null);
            // to fix empty fields
            myPeerCard = new PeerCard(peerRole, "", "");
            myPeerCard.setRole(peerRole);
            LogUtils.logInfo(
                    context,
                    AALSpaceManagerImpl.class,
                    "init",
                    new Object[] { "--->PeerCard created: "
                            + myPeerCard.toString() }, null);

            // fetching the services
            LogUtils.logDebug(context, AALSpaceManagerImpl.class, "init",
                    new Object[] { "Fetching the ContextBroker..." }, null);
            Object[] cBrokers = context.getContainer().fetchSharedObject(
                    context,
                    new Object[] { ControlBroker.class.getName().toString() },
                    this);
            if (cBrokers != null) {
                LogUtils.logDebug(context, AALSpaceManagerImpl.class, "init",
                        new Object[] { "Found  ContextBrokers..." }, null);
                if (cBrokers[0] instanceof ControlBroker)
                    controlBroker = (ControlBroker) cBrokers[0];
                else {
                    initialized = false;
                    return initialized;
                }
            } else {
                LogUtils.logWarn(context, AALSpaceManagerImpl.class, "init",
                        new Object[] { "No ContextBroker found" }, null);
                initialized = false;
                return initialized;
            }

            // XML Schema validation
            if (aalSpaceValidation && aalSpaceConfigurationPath != null
                    && aalSpaceSchemaName != null) {
                LogUtils.logDebug(
                        context,
                        AALSpaceManagerImpl.class,
                        "init",
                        new Object[] { "Initialize AALSpace schema validation" },
                        null);
                SchemaFactory sf = SchemaFactory
                        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                try {
                    File aalSpaceSchemaFile = new File(aalSpaceSchemaURL
                            + File.separatorChar + aalSpaceSchemaName);
                    Schema aalSpaceSchema = null;
                    if (aalSpaceSchemaFile.canRead()) {
                        aalSpaceSchema = sf.newSchema(aalSpaceSchemaFile);
                        unmarshaller.setSchema(aalSpaceSchema);
                        unmarshaller
                                .setEventHandler(new AALSpaceSchemaEventHandler(
                                        context));
                    } else
                        LogUtils.logWarn(
                                context,
                                AALSpaceManagerImpl.class,
                                "init",
                                new Object[] { "Unable to read AALSpace Scham from path: "
                                        + aalSpaceSchemaFile.getAbsolutePath() },
                                null);

                } catch (SAXException e) {
                    LogUtils.logError(
                            context,
                            AALSpaceManagerImpl.class,
                            "init",
                            new Object[] { "Error during AALSpace schema initialization: "
                                    + e.toString() }, null);
                } catch (NullPointerException e) {
                    LogUtils.logError(
                            context,
                            AALSpaceManagerImpl.class,
                            "init",
                            new Object[] { "Error during AALSpace schema initialization: "
                                    + e.toString() }, null);
                } catch (JAXBException e) {
                    LogUtils.logError(
                            context,
                            AALSpaceManagerImpl.class,
                            "init",
                            new Object[] { "Error during AALSpace Schema Event handler initialization: "
                                    + e.toString() }, null);
                }
                initialized = true;
            }

            // start the threads
            // Joiner -> AALSapce joiner
            joiner = new Joiner(this, context);
            joinerFuture = scheduler.scheduleAtFixedRate(joiner, 0, 1,
                    TimeUnit.SECONDS);

            // Configure the AAL Space
            if (aalSpaceConfigurationPath == null
                    || aalSpaceConfigurationPath.length() == 0) {
                LogUtils.logWarn(
                        context,
                        AALSpaceManagerImpl.class,
                        "init",
                        new Object[] { "AALSpace default configurations are null" },
                        null);
                initialized = true;
            } else {
                LogUtils.logDebug(
                        context,
                        AALSpaceManagerImpl.class,
                        "init",
                        new Object[] { "Parse the AALSpace default configurations" },
                        null);
                aalSpaceDefaultConfiguration = readAALSpaceDefaultConfigurations();
                initAALSpace(aalSpaceDefaultConfiguration);
                initialized = true;
            }
        }
        return initialized;
    }

    /**
     * Private method to manage the creation of a new AALSpace starting from the
     * default configurations
     *
     * @param aalSpaceDefaultConfiguration
     *            Default AAL Space configurations
     * @return true if the creation succeeded, false otherwise
     */
    public synchronized void initAALSpace(Aalspace aalSpaceDefaultConfiguration) {
        // configure the MW with the space configurations
        if (currentAALSpace != null) {
            // EXPLAIN AALSpace has been already configured
            LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                    "initAALSpace", new Object[] { "The MW belongs to: "
                            + currentAALSpace.getSpaceCard().toString() }, null);
            return;
        }
        if (aalSpaceDefaultConfiguration == null) {
            // EXPLAIN no configuration path given so we cannot initialize
            LogUtils.logDebug(
                    context,
                    AALSpaceManagerImpl.class,
                    "initAALSpace",
                    new Object[] { "No AALSpace default configuration found on the path: "
                            + aalSpaceConfigurationPath }, null);
            return;
        }
        try {
            LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                    "initAALSpace",
                    new Object[] { "AALSpace default configuration found" },
                    null);
            // first look for existing AALSpace with the same name as the
            // one reported in the default config.file
            List<AALSpaceCard> spaceCards = controlBroker
                    .discoverAALSpace(buildAALSpaceFilter(aalSpaceDefaultConfiguration));
            if (spaceCards != null && spaceCards.size() > 0) {
                LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                        "initAALSpace",
                        new Object[] { "Default AALSpace found" }, null);
                synchronized (foundAALSpaces) {
                    this.foundAALSpaces.addAll(spaceCards);
                }
            } else {
                if (myPeerCard.getRole().equals(PeerRole.COORDINATOR)) {

                    LogUtils.logInfo(
                            context,
                            AALSpaceManagerImpl.class,
                            "initAALSpace",
                            new Object[] { "No default AALSpace found...creating it " },
                            null);

                    List<org.universAAL.middleware.interfaces.ChannelDescriptor> communicationChannels = new ArrayList<org.universAAL.middleware.interfaces.ChannelDescriptor>();
                    // fetch the communication channels
                    communicationChannels = getChannels(aalSpaceDefaultConfiguration
                            .getCommunicationChannels().getChannelDescriptor());
                    // fetch the peering channel
                    org.universAAL.middleware.interfaces.ChannelDescriptor peeringChannel = getChannel(aalSpaceDefaultConfiguration
                            .getPeeringChannel().getChannelDescriptor());
                    // configure the MW channels
                    if (controlBroker != null) {
                        controlBroker.configurePeeringChannel(peeringChannel,
                                myPeerCard.getPeerID());
                        controlBroker.configureChannels(communicationChannels,
                                myPeerCard.getPeerID());

                        // create the new AALSpace
                        AALSpaceCard myAALSpace = new AALSpaceCard(
                                getAALSpaceProperties(aalSpaceDefaultConfiguration));
                        myAALSpace.setAalSpaceLifeTime(aalSpaceLifeTime);
                        currentAALSpace = new AALSpaceDescriptor(myAALSpace,
                                communicationChannels);
                        // since coordinator and deployCoordinator matches,
                        // configure the space Descriptor
                        currentAALSpace.setDeployManager(myPeerCard);

                        // announce the AAL Space
                        controlBroker.buildAALSpace(myAALSpace);

                        // strat thread
                        refreshAALSpaceThread = new RefreshAALSpaceThread(
                                context);
                        refreshFuture = scheduler.scheduleAtFixedRate(
                                refreshAALSpaceThread, 0, aalSpaceLifeTime - 1,
                                TimeUnit.SECONDS);

                        // start the thread for management of AALSpace
                        checkPeerThread = new CheckPeerThread(context);
                        checkerFuture = scheduler.scheduleAtFixedRate(
                                checkPeerThread, 0, 1, TimeUnit.SECONDS);

                        // add the AALSpace created to the list of managed
                        // AAL spaces
                        managedAALspaces.put(myAALSpace.getSpaceID(),
                                currentAALSpace);

                        // notify to all the listeners a new AAL Space has
                        // been joined
                        for (AALSpaceListener spaceListener : listeners) {
                            spaceListener.aalSpaceJoined(currentAALSpace);
                        }
                        peers.put(myPeerCard.getPeerID(), myPeerCard);

                        // init the control broker
                        LogUtils.logInfo(context, AALSpaceManagerImpl.class,
                                "initAALSpace",
                                new Object[] { "New AALSpace created!" }, null);

                    } else {
                        LogUtils.logWarn(
                                context,
                                AALSpaceManagerImpl.class,
                                "initAALSpace",
                                new Object[] { "Control Broker is not initialize" },
                                null);
                    }

                } else {
                    LogUtils.logInfo(
                            context,
                            AALSpaceManagerImpl.class,
                            "initAALSpace",
                            new Object[] { "No default AALSpace found...waiting to join an AALSpace as :"
                                    + myPeerCard.getRole() }, null);
                }
            }

        } catch (Exception e) {
            LogUtils.logError(
                    context,
                    AALSpaceManagerImpl.class,
                    "initAALSpace",
                    new Object[] { "Error during AALSpace initialization: "
                            + e.toString() }, null);
        }

    }

    public void join(AALSpaceCard spaceCard) {
        if (currentAALSpace != null) {
            LogUtils.logWarn(
                    context,
                    AALSpaceManagerImpl.class,
                    "join",
                    new Object[] { "Cannot join to multiple AALSpace. First leave the current AALSpace " },
                    null);

        }
        if (init()) {
            synchronized (pendingAALSpace) {

                pendingAALSpace = true;
                LogUtils.logInfo(context, AALSpaceManagerImpl.class, "join",
                        new Object[] { "--->Start the join phase to AALSpace: "
                                + spaceCard.toString() }, null);
                LogUtils.logDebug(context, AALSpaceManagerImpl.class, "join",
                        new Object[] { "Configure the peering channel..." },
                        null);

                // fetch the default peering channel
                org.universAAL.middleware.interfaces.ChannelDescriptor defaultPeeringChannel = getChannel(aalSpaceDefaultConfiguration
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
                                myPeerCard.getPeerID());
                        LogUtils.logInfo(
                                context,
                                AALSpaceManagerImpl.class,
                                "join",
                                new Object[] { "--->Peering channel configured!" },
                                null);
                    } else {
                        LogUtils.logWarn(
                                context,
                                AALSpaceManagerImpl.class,
                                "join",
                                new Object[] { "Peering channel is null not able to join the AALSpace" },
                                null);
                    }
                    LogUtils.logInfo(context, AALSpaceManagerImpl.class,
                            "join",
                            new Object[] { "--->Sending join request..." },
                            null);
                    PeerCard spaceCoordinator = new PeerCard(
                            spaceCard.getCoordinatorID(), PeerRole.COORDINATOR);
                    controlBroker.join(spaceCoordinator, spaceCard);

                } catch (Exception e) {
                    LogUtils.logError(context, AALSpaceManagerImpl.class,
                            "join",
                            new Object[] { "Error during AALSpace join: "
                                    + spaceCard.toString() }, null);
                    pendingAALSpace = false;
                }
            }
        } else {
            LogUtils.logWarn(context, AALSpaceManagerImpl.class, "join",
                    new Object[] { "AALSpace Manager not initialized" }, null);
        }

    }

    public void cleanUpJoinRequest() {
        synchronized (pendingAALSpace) {
            List<ChannelDescriptor> pendingPC = new ArrayList<ChannelDescriptor>();
            pendingPC.add(peeringChannel);
            controlBroker.resetModule(pendingPC);
            pendingAALSpace = false;
        }

    }

    public void aalSpaceJoined(AALSpaceDescriptor descriptor) {
        if (init()) {
            LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                    "aalSpaceJoined", new Object[] { "Joining to AALSpace: "
                            + descriptor.getSpaceCard().toString() }, null);

            synchronized (pendingAALSpace) {
                currentAALSpace = descriptor;
                pendingAALSpace = false;

                LogUtils.logInfo(context, AALSpaceManagerImpl.class,
                        "aalSpaceJoined",
                        new Object[] { "--->AALSpace Joined!" }, null);
                try {
                    pendingAALSpace.notifyAll();
                } catch (Exception e) {
                    LogUtils.logError(
                            context,
                            AALSpaceManagerImpl.class,
                            "aalSpaceJoined",
                            new Object[] { "Error during notify: "
                                    + e.toString() }, null);
                }
            }
            // creating AALSpace channels
            List<ChannelDescriptor> communicationChannels = currentAALSpace
                    .getBrokerChannels();
            if (communicationChannels != null)
                controlBroker.configureChannels(communicationChannels,
                        myPeerCard.getPeerID());
            // start checking for members peers in the AALSpace
            checkPeerThread = new CheckPeerThread(context);
            checkerFuture = scheduler.scheduleAtFixedRate(checkPeerThread, 0,
                    1, TimeUnit.SECONDS);
            // add myself to the list of peers
            peerFound(myPeerCard);
            controlBroker.newPeerAdded(currentAALSpace.getSpaceCard(),
                    myPeerCard);

            LogUtils.logInfo(context, AALSpaceManagerImpl.class,
                    "aalSpaceJoined",
                    new Object[] { "--->Announced my presence!" }, null);

            for (AALSpaceListener spaceListener : listeners) {
                spaceListener.aalSpaceJoined(currentAALSpace);
            }

        } else {
            LogUtils.logWarn(
                    context,
                    AALSpaceManagerImpl.class,
                    "aalSpaceJoined",
                    new Object[] { "AALSpace Manager is not initialized aborting." },
                    null);
            pendingAALSpace = false;
        }

    }

    private Dictionary<String, String> buildAALSpaceFilter(Aalspace space) {
        Dictionary<String, String> filters = new Hashtable<String, String>();
        if (space != null) {
            try {
                filters.put(Consts.AALSPaceID, space.getSpaceDescriptor()
                        .getSpaceId());
                LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                        "buildAALSpaceFilter",
                        new Object[] { "Filter created" }, null);
            } catch (NullPointerException e) {
                LogUtils.logError(
                        context,
                        AALSpaceManagerImpl.class,
                        "buildAALSpaceFilter",
                        new Object[] { "Error while building AALSpace filter...returning empty filter"
                                + e.toString() }, null);
                return filters;
            } catch (Exception e) {
                LogUtils.logError(
                        context,
                        AALSpaceManagerImpl.class,
                        "buildAALSpaceFilter",
                        new Object[] { "Error while building AALSpace filter...returning empty filter"
                                + e.toString() }, null);
                return filters;
            }
        }
        return filters;
    }

    private List<ChannelDescriptor> getChannels(
            List<org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor> channels) {
        List<ChannelDescriptor> theChannels = new ArrayList<ChannelDescriptor>();

        for (org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor channel : channels) {
            ChannelDescriptor singleChannel = new ChannelDescriptor(
                    channel.getChannelName(), channel.getChannelURL(),
                    channel.getChannelValue());
            theChannels.add(singleChannel);
        }
        return theChannels;

    }

    private ChannelDescriptor getChannel(
            org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor channel) {
        ChannelDescriptor singleChannel = new ChannelDescriptor(
                channel.getChannelName(), channel.getChannelURL(),
                channel.getChannelValue());
        return singleChannel;

    }

    /**
     * This method collects in a dictionary the properties associated with a new
     * AAL Space in order to announce them. The properties are read from the
     * data structure AalSpace. The properties added to the AALSpace card are
     * the name,id,description and coordinator ID and the peering channel
     * serialized as XML string
     *
     * @param space
     * @return
     */
    private Dictionary<String, String> getAALSpaceProperties(Aalspace space) {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        try {

            // general purpose properties
            properties.put(Consts.AALSPaceName, space.getSpaceDescriptor()
                    .getSpaceName());
            properties.put(Consts.AALSPaceID, space.getSpaceDescriptor()
                    .getSpaceId());
            properties.put(Consts.AALSPaceDescription, space
                    .getSpaceDescriptor().getSpaceDescription());
            properties.put(Consts.AALSpaceCoordinator, myPeerCard.getPeerID());
            // URL where to fetch the peering channel
            properties
                    .put(Consts.AALSpacePeeringChannelURL, space
                            .getPeeringChannel().getChannelDescriptor()
                            .getChannelURL());
            properties.put(Consts.AALSpacePeeringChannelName, space
                    .getPeeringChannel().getChannelDescriptor()
                    .getChannelName());
            properties.put(Consts.AALSPaceProfile, space.getSpaceDescriptor()
                    .getProfile());

        } catch (NullPointerException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
        return properties;

    }

    private String[] getFileList(String aalSpaceConfigurationPath) {
        File spaceConfigDirectory = new File(aalSpaceConfigurationPath);
        if (!spaceConfigDirectory.canRead()) {
            LogUtils.logWarn(context, AALSpaceManagerImpl.class, "getFileList",
                    new Object[] { "File: " + aalSpaceConfigurationPath
                            + " cannot be read." }, null);
            return null;
        }
        String[] spaces = spaceConfigDirectory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(spaceExtension));
            }
        });
        return spaces;
    }

    public Aalspace readAALSpaceDefaultConfigurations() {
        LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                "AALSpaceManagerImpl",
                new Object[] { "Reading AALSpace configuration." }, null);
        try {
            String aalSpaceConfigurationPath = this.aalSpaceConfigurationPath;
            File spaceConfigDirectory = new File(aalSpaceConfigurationPath);

            // debug output: log the current path
            String currPath = "";
            try {
                currPath = new java.io.File(".").getCanonicalPath();
            } catch (IOException e) {
            }
            LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                    "readAALSpaceDefaultConfigurations", new Object[] {
                            "Reading AALSpace configuration from directory: ",
                            spaceConfigDirectory.toString(),
                            " The current path is: ", currPath }, null);

            // get the list of config files
            String[] spaces = getFileList(aalSpaceConfigurationPath);
            if (spaces == null || spaces.length == 0) {
                LogUtils.logWarn(context, AALSpaceManagerImpl.class,
                        "readAALSpaceDefaultConfigurations", new Object[] {
                                "File: ", aalSpaceConfigurationPath,
                                " cannot be read, trying alternative: ",
                                altConfigDir }, null);

                aalSpaceConfigurationPath = altConfigDir;
                spaces = getFileList(aalSpaceConfigurationPath);
            }

            // evaluate the list of config files
            if (spaces != null && spaces.length > 0) {
                LogUtils.logDebug(
                        context,
                        AALSpaceManagerImpl.class,
                        "readAALSpaceDefaultConfigurations",
                        new Object[] { "Found: "
                                + spaces.length
                                + " space configurations...picking up the default one" },
                        null);
                // Currently only one space is read from the file system
                File defaultSpaceConfiguration = new File(
                        aalSpaceConfigurationPath + File.separatorChar
                                + spaces[0]);
                if (defaultSpaceConfiguration.canRead()) {

                    Aalspace space = (Aalspace) unmarshaller
                            .unmarshal(defaultSpaceConfiguration);
                    if (space != null) {
                        return space;
                    } else {
                        LogUtils.logWarn(
                                context,
                                AALSpaceManagerImpl.class,
                                "readAALSpaceDefaultConfigurations",
                                new Object[] { "Unable to parse default AALSpace configuration" },
                                null);
                        return null;
                    }
                } else {
                    LogUtils.logWarn(
                            context,
                            AALSpaceManagerImpl.class,
                            "readAALSpaceDefaultConfigurations",
                            new Object[] { "Directory were files are located is not accessible" },
                            null);
                    return null;
                }
            } else {
                LogUtils.logWarn(context, AALSpaceManagerImpl.class,
                        "readAALSpaceDefaultConfigurations",
                        new Object[] { "No default AALSpaces found" }, null);
                return null;
            }
        } catch (JAXBException e) {
            LogUtils.logError(
                    context,
                    AALSpaceManagerImpl.class,
                    "readAALSpaceDefaultConfigurations",
                    new Object[] { "Error during JAXB initialization: "
                            + e.toString() }, null);
            return null;
        }

    }

    public void loadConfigurations(Dictionary configurations) {
        LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                "loadConfigurations",
                new Object[] { "Updating AALSpaceManager properties" }, null);
        if (configurations == null) {
            LogUtils.logWarn(context, AALSpaceManagerImpl.class,
                    "loadConfigurations",
                    new Object[] { "AALSpaceManager properties are null!!!" },
                    null);
            return;
        } else {
            LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                    "loadConfigurations",
                    new Object[] { "Fetching the PeerRole" }, null);
            String role = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.PEER_ROLE);
            String roleOverride = System
                    .getProperty(org.universAAL.middleware.managers.aalspace.util.Consts.PEER_ROLE);
            if (roleOverride != null)
                role = roleOverride;
            if (role != null) {
                try {
                    peerRole = PeerRole.valueOf(role);
                } catch (IllegalArgumentException e) {
                    LogUtils.logError(
                            context,
                            AALSpaceManagerImpl.class,
                            "loadConfigurations",
                            new Object[] { "Unable to initialize the peer with the role: "
                                    + role }, null);
                    LogUtils.logError(context, AALSpaceManagerImpl.class,
                            "loadConfigurations",
                            new Object[] { "...configuring as regular PEER: "
                                    + role }, null);
                    peerRole = PeerRole.PEER;
                }
            } else {
                LogUtils.logWarn(
                        context,
                        AALSpaceManagerImpl.class,
                        "loadConfigurations",
                        new Object[] { "The role is null...configuring as regular PEER: "
                                + role }, null);
                peerRole = PeerRole.PEER;
            }

            LogUtils.logDebug(
                    context,
                    AALSpaceManagerImpl.class,
                    "loadConfigurations",
                    new Object[] { "Fetching AALSpace default configurations" },
                    null);
            aalSpaceConfigurationPath = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_CONFIGURATION_PATH);
            try {
                if ( aalSpaceConfigurationPath == null ) {
                    LogUtils.logWarn(
                            context,
                            AALSpaceManagerImpl.class,
                            "loadConfigurations",
                            new Object[] { "AALSpace default configurations are null!" },
                            null);
                } else {
                    //Resolving the relative path to absolute path
                    File config = new File(aalSpaceConfigurationPath);
                    aalSpaceConfigurationPath = config.getCanonicalPath();
                    if ( config.isDirectory() == false ) {
                        LogUtils.logWarn(
                                context,
                                AALSpaceManagerImpl.class,
                                "loadConfigurations",
                                new Object[] { "AALSpace default configurations ", aalSpaceConfigurationPath, " does not point to a directory or is not readable" },
                                null);
                    } else {
                        LogUtils.logInfo(
                                context,
                                AALSpaceManagerImpl.class,
                                "loadConfigurations",
                                new Object[] { "AALSpace default configurations fetched: ",
                                    aalSpaceConfigurationPath }, null);
                    }
                }
            } catch (IOException e) {
                LogUtils.logError(
                        context,
                        AALSpaceManagerImpl.class,
                        "loadConfigurations",
                        new Object[] { "AALSpace default configurations is set by property \"aalSpaceConfigurationPath\" but it points to invalid location ", aalSpaceConfigurationPath },
                        null);
                aalSpaceConfigurationPath = null;
            }
            LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                    "loadConfigurations",
                    new Object[] { "Fetching AALSpace extension" }, null);
            spaceExtension = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.SPACE_EXTENSION);
            aalSpaceValidation = Boolean
                    .parseBoolean((String) configurations
                            .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_VALIDATION));
            aalSpaceSchemaURL = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_SCHEMA_URL);
            aalSpaceLifeTime = Integer
                    .parseInt((String) configurations
                            .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_LIFETIME));

            aalSpaceSchemaName = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_SCHEMA_NAME);
            waitBeforeClosingChannels = Long
                    .parseLong((String) configurations
                            .get(org.universAAL.middleware.managers.aalspace.util.Consts.WAIT_BEFEORE_CLOSING_CHANNEL));
            waitAfterJoinRequest = Long
                    .parseLong((String) configurations
                            .get(org.universAAL.middleware.managers.aalspace.util.Consts.WAIT_BEFEORE_CLOSING_CHANNEL));

        }
    }

    public void joinRequest(AALSpaceCard spaceCard, PeerCard peer) {
        if (init()) {
            if (spaceCard != null && peer != null
                    && spaceCard.getSpaceID() != null) {
                LogUtils.logInfo(context, AALSpaceManagerImpl.class,
                        "joinRequest", new Object[] { "---> Peer:"
                                + peer.getPeerID().toString()
                                + " requests to join to the AAL Space: " },
                        null);
                if (!managedAALspaces.containsKey(spaceCard.getSpaceID())) {
                    LogUtils.logWarn(
                            context,
                            AALSpaceManagerImpl.class,
                            "joinRequest",
                            new Object[] { "Received a join request to an AALSpace not managed: my AALSpace: "
                                    + currentAALSpace.getSpaceCard()
                                            .getSpaceID()
                                    + " while received: "
                                    + spaceCard.getSpaceID() }, null);

                } else {
                    // send unicast message to the peer with the space
                    // descriptor
                    LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                            "joinRequest",
                            new Object[] { "Sending the space descriptor..." },
                            null);
                    // update the peers
                    // add the new peer to the map of peers
                    peerFound(peer);

                    controlBroker.addNewPeer(currentAALSpace, peer);
                    LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                            "joinRequest",
                            new Object[] { "Space descriptor sent!" }, null);
                    // newPeerJoined(peer)

                    // announce bcast the new peer
                    LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                            "joinRequest",
                            new Object[] { "Announcing the new peer..." }, null);
                }

            } else
                LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                        "joinRequest",
                        new Object[] { "Invalid join request parameter" }, null);
        } else {
            LogUtils.logWarn(context, AALSpaceManagerImpl.class, "joinRequest",
                    new Object[] { "AALSpace Manager not initialized" }, null);
        }

    }

    public synchronized void newAALSpacesFound(Set<AALSpaceCard> spaceCards) {
        boolean result = false;
        if (spaceCards != null) {
            synchronized (foundAALSpaces) {
                foundAALSpaces = spaceCards;
            }
            if (foundAALSpaces.size() > 0) {
                LogUtils.logTrace(
                        context,
                        AALSpaceManagerImpl.class,
                        "newAALSpacesFound",
                        new Object[] { "--->The list of AAL Spaces has been updated:"
                                + foundAALSpaces.toString() }, null);
            }
        }
    }

    public synchronized void peerFound(PeerCard peer) {

        if (peer != null && !peers.containsKey(peer.getPeerID())) {
            LogUtils.logInfo(context, AALSpaceManagerImpl.class, "peerFound",
                    new Object[] { "--->The Peer: "
                            + peer.getPeerID().toString()
                            + " joins the AALSpace: " }, null);
            peers.put(peer.getPeerID(), peer);
            for (AALSpaceListener list : listeners) {
                list.newPeerJoined(peer);

            }

        }

    }

    public synchronized void peerLost(PeerCard peer) {

        if (peer != null) {
            LogUtils.logInfo(context, AALSpaceManagerImpl.class, "peerLost",
                    new Object[] { "--->Peer +" + peer.getPeerID()
                            + " left the AALSpace" }, null);
            peers.remove(peer.getPeerID());
            for (AALSpaceListener list : listeners) {
                list.peerLost(peer);

            }

        }

    }

    public void sharedObjectAdded(Object sharedObj, Object removeHook) {
        if (sharedObj instanceof ControlBroker) {
            LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                    "sharedObjectAdded",
                    new Object[] { "ControlBroker service added" }, null);
            this.controlBroker = (ControlBroker) sharedObj;
        }
    }

    public void sharedObjectRemoved(Object removeHook) {
        if (removeHook instanceof ControlBroker) {
            LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                    "sharedObjectRemoved",
                    new Object[] { "ControlBroker service removed" }, null);
            this.controlBroker = null;
            initialized = false;
        }
    }

    public synchronized void leaveRequest(AALSpaceDescriptor spaceDescriptor) {
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
            LogUtils.logInfo(context, AALSpaceManagerImpl.class,
                    "leaveRequest", new Object[] { "--->Leaving the AALSpace: "
                            + spaceDescriptor.getSpaceCard().getSpaceName() },
                    null);
            controlBroker.resetModule(channels);
            // we assume the current aal space is the only one
            currentAALSpace = null;
            peers.clear();
        }
    }

    public void leaveAALSpace(AALSpaceDescriptor spaceDescriptor) {
        if (init()) {
            if (spaceDescriptor != null
                    && managedAALspaces.containsKey(spaceDescriptor
                            .getSpaceCard().getSpaceID())) {
                LogUtils.logInfo(
                        context,
                        AALSpaceManagerImpl.class,
                        "leaveAALSpace",
                        new Object[] { "--->Leaving a managed AALSpace: "
                                + spaceDescriptor.getSpaceCard().getSpaceName() },
                        null);
                closeManagedSpace(spaceDescriptor);
            } else if (spaceDescriptor.getSpaceCard().getSpaceID()
                    .equals(currentAALSpace.getSpaceCard().getSpaceID())) {
                // send a leave message
                LogUtils.logInfo(
                        context,
                        AALSpaceManagerImpl.class,
                        "leaveAALSpace",
                        new Object[] { "--->Leaving the AALSpace: "
                                + spaceDescriptor.getSpaceCard().getSpaceName() },
                        null);
                PeerCard spaceCoordinator = new PeerCard(spaceDescriptor
                        .getSpaceCard().getCoordinatorID(),
                        PeerRole.COORDINATOR);
                controlBroker.leaveAALSpace(spaceCoordinator,
                        spaceDescriptor.getSpaceCard());
                LogUtils.logDebug(context, AALSpaceManagerImpl.class,
                        "leaveAALSpace",
                        new Object[] { "Leave message sent!" }, null);

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
                // we assume the current aal space is the only one
                currentAALSpace = null;
                // reset list of peers
                peers.clear();

            }

            for (AALSpaceListener elem : listeners) {

                elem.aalSpaceLost(spaceDescriptor);
            }

        } else {
            LogUtils.logWarn(context, AALSpaceManagerImpl.class,
                    "leaveAALSpace",
                    new Object[] { "AALSpace Manager not initialized" }, null);
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

            if (!managedAALspaces.isEmpty()) {
                LogUtils.logInfo(context, AALSpaceManagerImpl.class, "dispose",
                        new Object[] { "Closing all the managed AAL Spaces" },
                        null);
                try {
                    for (String spaceID : managedAALspaces.keySet()) {
                        closeManagedSpace(managedAALspaces.get(spaceID));
                    }
                } catch (Exception e) {
                    LogUtils.logError(context, AALSpaceManagerImpl.class,
                            "dispose", new Object[] { "Error during dispose: "
                                    + e.toString() }, null);
                }
            } else {
                if (currentAALSpace != null)
                    leaveAALSpace(currentAALSpace);

            }
        } else {
            LogUtils.logWarn(context, AALSpaceManagerImpl.class, "dispose",
                    new Object[] { "AALSpace Manager not initialized" }, null);
        }
        currentAALSpace = null;
        managedAALspaces.clear();
        synchronized (foundAALSpaces) {
            foundAALSpaces.clear();
        }
        initialized = false;
    }

    /**
     * Destroy all the managed AALSpace
     *
     * @param spaceDescriptor
     */
    private void closeManagedSpace(AALSpaceDescriptor spaceDescriptor) {

        controlBroker.requestToLeave(spaceDescriptor);
        try {
            Thread.sleep(waitBeforeClosingChannels);
        } catch (InterruptedException e) {
            LogUtils.logError(context, AALSpaceManagerImpl.class,
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
        controlBroker.destroyAALSpace(spaceDescriptor.getSpaceCard());

    }

    public void addAALSpaceListener(AALSpaceListener listener) {
        if (listener != null && !listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeAALSpaceListener(AALSpaceListener listener) {
        if (listener != null)
            listeners.remove(listener);
    }

    public void setListOfPeers(Map<String, PeerCard> peers) {

        // verify if among the peers the coordinator is present. If not the
        // coordinator crashed
        if (currentAALSpace != null
                && !peers.keySet().contains(
                        currentAALSpace.getSpaceCard().getCoordinatorID())) {
            // coordinator crashed, leave from the AAL Space

            leaveRequest(currentAALSpace);
        } else
            this.peers = peers;
    }

    public AALSpaceStatus getAALSpaceStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    public void mpaInstalled(AALSpaceDescriptor spaceDescriptor) {

        controlBroker.signalAALSpaceStatus(AALSpaceStatus.INSTALLED_UAAP,
                spaceDescriptor);

    }

    public void mpaInstalling(AALSpaceDescriptor spaceDescriptor) {

        // send a event notification to the AALSpace
        controlBroker.signalAALSpaceStatus(AALSpaceStatus.INSTALLING_UAAP,
                spaceDescriptor);

    }

    public void aalSpaceEvent(AALSpaceStatus newStatus) {
        LogUtils.logInfo(
                context,
                AALSpaceManagerImpl.class,
                "aalSpaceEvent",
                new Object[] { "--->New event from AALSpace: "
                        + newStatus.toString() }, null);

        for (AALSpaceListener elem : listeners) {
            elem.aalSpaceStatusChanged(newStatus);
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
