/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research

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
package org.universAAL.middleware.bus.model;

import org.universAAL.middleware.brokers.Broker;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.member.BusMemberType;
import org.universAAL.middleware.bus.model.util.IRegistry;
import org.universAAL.middleware.bus.model.util.IRegistryListener;
import org.universAAL.middleware.bus.model.util.RegistryMap;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.bus.permission.Permission;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.listener.MessageListener;

/**
 * Defines a shared mechanisms realized by all concrete buses. This mainly means
 * that it offers abstract view on the bus strategy and manages the bus message
 * queue and by that reduces the implementation of the concrete bus strategy.
 * 
 * @author mtazari - <a href="mailto:saied.tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public abstract class AbstractBus implements Broker, MessageListener {

    /**
     * Additional prefix for {@link #uAAL_SPACE_INSTANCE_URI_PREFIX} and
     * {@link #uAAL_MW_INSTANCE_URI_PREFIX} to make it start with a valid URI
     * scheme.
     * <p>
     * For example, this prefix could be <tt>urn:uaal_space:</tt>
     */
    private static String uAAL_OPTIONAL_URI_PREFIX = "urn:uaal_space:";

    /**
     * The prefix of the URI of {@link BusMember}s. Each time a new bus member
     * registers at the bus, this String is the prefix of the URI that is
     * created to identify the bus member. It consists of the ID of the AAL
     * Space, queried by {@link AALSpaceCard#getSpaceID()}, and a finalizing "/"
     * (if the Space ID does not create a valid URI,
     * {@link #uAAL_OPTIONAL_URI_PREFIX} is added at the beginning of this
     * prefix).
     * <p>
     * For example, if the Space ID is <tt>8224</tt> this prefix would be
     * <tt>urn:uaal_space:8224/</tt>
     */
    private static String uAAL_SPACE_INSTANCE_URI_PREFIX = null;

    /**
     * The prefix of the URI of {@link BusMember}s. Each time a new bus member
     * registers at the bus, this String is the prefix of the URI that is
     * created to identify the bus member. It consists of the
     * {@link #uAAL_SPACE_INSTANCE_URI_PREFIX} and the ID of the peer, queried
     * by {@link PeerCard#getPeerID()}, and a finalizing "#"
     * <p>
     * For example, if the Space ID is <tt>8224</tt> and the Peer ID is
     * <tt>7ca58fd6-fb5f-4c8e-8db2-4ba6807fa1bf</tt> this prefix would be
     * <tt>urn:uaal_space:8224/7ca58fd6-fb5f-4c8e-8db2-4ba6807fa1bf#</tt>
     */
    private static String uAAL_MW_INSTANCE_URI_PREFIX = null;

    /**
     * A counter for {@link BusMember}s. Each time a new bus member registers at
     * the bus, this counter is used as part of the URI of the bus member, and
     * then increased.
     */
    private static int uAAL_MW_INSTANCE_BUS_MEMBERSHIPS = 0;

    protected static AALSpaceManager aalSpaceManager;
    protected static CommunicationModule communicationModule;
    private static ModuleContext myContext;

    public static void initBrokerage(ModuleContext mc,
	    AALSpaceManager aalSpaceMgr, CommunicationModule commModule) {
	if (myContext != null) {
	    // LogUtils.logError(
	    // myContext,
	    // AbstractBus.class,
	    // "initBrokerage",
	    // new Object[] {
	    // "The init method was called already, it cannot be called a second time. The original caller was ",
	    // myContext.getID(), " and the current caller is ",
	    // mc.getID() }, null);
	    // return;
	    LogUtils.logDebug(
		    myContext,
		    AbstractBus.class,
		    "initBrokerage",
		    new Object[] {
			    "The init method is called again. The original caller was ",
			    myContext.getID(), " and the current caller is ",
			    mc.getID() }, null);
	}
	myContext = mc;

	AccessControl.INSTANCE.init(mc);
	Permission.init(mc);

	aalSpaceManager = aalSpaceMgr;
	communicationModule = commModule;
	// configure the MW's URI instance
	// first check if I already join an AALSpace

	AALSpaceDescriptor sd = aalSpaceMgr.getAALSpaceDescriptor();
	if (sd != null) {
	    uAAL_SPACE_INSTANCE_URI_PREFIX = sd.getSpaceCard().getSpaceID()
		    + "/";
	} else {
	    uAAL_SPACE_INSTANCE_URI_PREFIX = "unknown-space/";
	}
	// TODO: workaround for non-space-coordinators (space ID is unknown
	// then)
	uAAL_SPACE_INSTANCE_URI_PREFIX = "";

	PeerCard pc = aalSpaceMgr.getMyPeerCard();
	if (pc != null) {
	    uAAL_MW_INSTANCE_URI_PREFIX = uAAL_SPACE_INSTANCE_URI_PREFIX
		    + pc.getPeerID() + "#";
	} else {
	    uAAL_MW_INSTANCE_URI_PREFIX = uAAL_SPACE_INSTANCE_URI_PREFIX
		    + "unknown-peer#";
	}

	if (!StringUtils.startsWithURIScheme(uAAL_MW_INSTANCE_URI_PREFIX)) {
	    // to get a valid URI we have to add a valid URI schema prefix
	    uAAL_SPACE_INSTANCE_URI_PREFIX = uAAL_OPTIONAL_URI_PREFIX
		    + uAAL_SPACE_INSTANCE_URI_PREFIX;
	    uAAL_MW_INSTANCE_URI_PREFIX = uAAL_OPTIONAL_URI_PREFIX
		    + uAAL_MW_INSTANCE_URI_PREFIX;
	}
    }

    public static int getCurrentNumberOfPeers() {
	return aalSpaceManager.getPeers().size();
    }

    public static PeerCard getPeerFromBusResourceURI(String uri) {
	if (uri == null) {
	    return null;
	}
	int end = uri.lastIndexOf('#');
	if (end < 1) {
	    return null;
	}
	uri = uri.substring(uAAL_SPACE_INSTANCE_URI_PREFIX.length(), end);

	PeerCard retVal = aalSpaceManager.getPeers().get(uri);
	if (retVal == null) {
	    String myPeerID = "<unknown>";
	    PeerCard myPeerCard = aalSpaceManager.getMyPeerCard();
	    if (myPeerCard != null)
		myPeerID = myPeerCard.getPeerID();
	    LogUtils.logDebug(myContext, AbstractBus.class,
		    "getPeerFromBusResourceURI", new Object[] {
			    "The peer with ID ", uri,
			    " could not be retrieved. There are ",
			    aalSpaceManager.getPeers().size(),
			    " peers known and our own peer ID is ", myPeerID },
		    null);

	    if (uri.equals(myPeerID)) {
		// this case can happen if the space is not yet initialized
		// correctly
		retVal = myPeerCard;
	    }
	}
	return retVal;
    }

    protected ModuleContext context;
    protected IRegistry registry;
    protected BusStrategy busStrategy;
    private String brokerName;
    private PeerCard myCard = null;

    protected AbstractBus(ModuleContext module, String brokerName) {
	context = module;
	this.myCard = aalSpaceManager.getMyPeerCard();
	this.brokerName = brokerName;
	if (communicationModule != null)
	    communicationModule.addMessageListener(this, getBrokerName());
	else
	    LogUtils.logDebug(
		    myContext,
		    AbstractBus.class,
		    "AbstractBus",
		    "Could not add Message Listener to Communication Module because Communication Module is null");
	busStrategy = createBusStrategy(communicationModule);
	registry = createRegistry();
	busStrategy.start(module);
    }

    protected IRegistry createRegistry() {
	return new RegistryMap();
    }

    protected abstract BusStrategy createBusStrategy(
	    CommunicationModule commModule);

    /**
     * Returns bus member instance based on its member ID.
     * 
     * @param memberID
     *            bus member ID
     * @return bus member instance
     */
    public BusMember getBusMember(String memberID) {
	return registry.getBusMemberByID(memberID);
    }

    /**
     * If the passed argument is indeed a registered member of this bus, returns
     * the local ID with which it has been registered with the bus, otherwise
     * null.
     */
    public String getBusMemberID(BusMember bm) {
	return registry.getBusMemberID(bm);
    }

    public PeerCard getPeerCard() {
	return myCard;
    }

    /**
     * 
     * @return all bus members in BusMember array
     */
    public BusMember[] getBusMembers() {
	BusMember[] members = null;
	synchronized (registry) {
	    members = registry.getAllBusMembers();
	}
	return members;
    }

    /**
     * 
     * @return IDs of all bus members in array
     */
    public String[] getBusMembersByID() {
	String[] members = null;
	synchronized (registry) {
	    members = registry.getAllBusMembersIds();
	}
	return members;
    }

    /**
     * Registers a new bus member (adds a bus member to the list of all bus
     * members but only if it has not been added before). Returns the URI with
     * which the provided member will be known as a bus resource.
     */
    public final String register(ModuleContext module, BusMember m, BusMemberType type) {
	if (m == null)
	    return null;

	synchronized (registry) {
	    String id = getBusMemberID(m);
	    // register bus member only if it has not been added
	    // (registered) to the bus before
	    if (id == null) {
		// compose bus member ID
		id = m.getURI();
		// createBusSpecificID(module.getID(), type.name());
		registry.addBusMember(id, m);
	    }
	    LogUtils.logDebug(context, AbstractBus.class, "register",
		    new Object[] { "New bus member has registered: ", id },
		    null);
	    return id;
	}
    }

    /**
     * Calls message handling in BusStrategy (gives sender ID and message to
     * process on the bus).
     */
    public void brokerMessage(String senderID, BusMessage msg) {
	if (isValidMember(senderID)) {
//	    LogUtils.logDebug(context, AbstractBus.class, "sendMessage",
//		    new Object[] { " - ", senderID, " sending:\n",
//			    msg.toString() }, null);
	    busStrategy.handleMessage(msg, senderID);
	}
    }

    /**
     * 
     */
    public boolean init() {
	return aalSpaceManager != null && communicationModule != null;
    }

    /**
     * Stops the bus (deletes all bus members from the list, stops BusStrategy
     * thread and announces that the bus is being stopped to its members).
     */
    public void dispose() {
	BusMember[] members = registry.getAllBusMembers();
	registry.reset();
	registry = null;
	busStrategy.stopThread();
	for (BusMember member : members) {
	    member.busDyingOut(this);
	}
    }

    /**
     * Unregisters bus member from the bus (if the bus member with given ID
     * exists).
     */
    public void unregister(String memberID, BusMember m) {
	if (memberID != null) {
	    synchronized (registry) {
		BusMember o = registry.removeMemberByID(memberID);
		if (o != null && !o.equals(m)) {
		    registry.addBusMember(memberID, o);
		    LogUtils
			    .logError(
				    context,
				    AbstractBus.class,
				    "unregister",
				    new Object[] {
					    "Existing bus member should have been unregistered, but the ID does not match: "
						    + "\n    memberID to unregister: ",
					    memberID,
					    "\n    queried memberID: ",
					    o.getURI() }, null);
		} else {
		    LogUtils.logDebug(context, AbstractBus.class, "unregister",
			    new Object[] {
				    "Existing bus member has unregistered: ",
				    memberID }, null);
		}
	    }
	}
    }

    public void messageReceived(ChannelMessage message) {
	if (message != null) {
	    BusMessage busMessage = (BusMessage) unmarshall(message
		    .getContent());
	    LogUtils.logInfo(context, AbstractBus.class, "messageReceived",
		    new Object[] { context.getID(),
			    " - Received message from peer ",
			    message.getSender().getPeerID(), ":\n",
			    message.toString() }, null);
	    // sender ID is null for remote messages
	    busStrategy.handleMessage(busMessage, null);
	} else {
	    LogUtils.logWarn(context, AbstractBus.class, "messageReceived",
		    new Object[] { context.getID(),
			    " - message is null; ignoring.", }, null);
	}
    }

    public String getBrokerName() {
	return brokerName;
    }

    public String getURI() {
	return uAAL_MW_INSTANCE_URI_PREFIX + context.getID();
    }

    public final String createBusSpecificID(String module, String type) {
	StringBuffer sb = new StringBuffer(128);
	sb.append(uAAL_MW_INSTANCE_URI_PREFIX);
	// TODO: should we handle duplicates? Assume a malicious component that
	// creates its own bus and creates IDs in a loop, then the counter
	// could overflow
	sb.append(++uAAL_MW_INSTANCE_BUS_MEMBERSHIPS).append(type).append(".")
		.append(module);
	return sb.toString();
    }

    public boolean isValidMember(String memberURI) {
	return getBusMember(memberURI) != null;
    }

    public boolean isBusResourceURI(String uri) {
	return uri.startsWith(uAAL_MW_INSTANCE_URI_PREFIX);
    }

    public BrokerMessage unmarshall(String serializedBusMessage) {
	return new BusMessage(serializedBusMessage, this);
    }

    public boolean addRegistryListener(IRegistryListener listener) {
	return registry.addRegistryListener(listener);
    }

    public boolean removeRegistryListener(IRegistryListener listener) {
	return registry.removeRegistryListener(listener);
    }
}
