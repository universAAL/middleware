/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung

	Copyright 2008-2010 CNR-ISTI, http://isti.cnr.it
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
/**
 *	Revision;
 *
 *	26/09/2007 (francesco.furfari@isti.cnr.it)
 *		- changed in leave() the list of remote peers to which send the message leaveBus(...),
 *         now all the peers are notified
 *
 *	06/09/2007 (francesco.furfari@isti.cnr.it)
 *		- removed invocation of peer.getID() in log messages.
 *		- changed in join() the list of remote peers to which send the message joinBus(...),
 *         now all the peers are notified
 *         
 *   16/01/2008 (francesco.furfari@isti.cnr.it)
 *       - Major changes to synchronization algorithm by adopting lexicographic sort among peers
 *       - added replyBusestoPeer message to the ACL interfaces according to the new algorithm
 *       - added Dipatcher threads for buffering message executions and increase sodapop asynchronism
 *       
 *   20/01/2008 (francesco.furfari@isti.cnr.it)
 *		- wrapped log service in logger util (TODO service unregistering)
 *		- added sybchronizzation between noticeBusesToPeer and Join
 */
// Vadim - testing pre-commit hook, adding ".osgi" string, take 11
package org.universAAL.middleware.sodapop.impl;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.universAAL.middleware.acl.P2PConnector;
import org.universAAL.middleware.acl.PeerDiscoveryListener;
import org.universAAL.middleware.acl.SodaPopPeer;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.sodapop.AbstractBus;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class SodaPopImpl implements SodaPop, SodaPopPeer,
	PeerDiscoveryListener, SharedObjectListener {
    public static ModuleContext moduleContext;
    private static Object[] contentSerializerParams;

    public static MessageContentSerializer getContentSerializer() {
	return (MessageContentSerializer) SodaPopImpl.moduleContext
		.getContainer().fetchSharedObject(SodaPopImpl.moduleContext,
			SodaPopImpl.contentSerializerParams);
    }

    public MessageContentSerializer getContentSerializer(ModuleContext context) {
	return (MessageContentSerializer) context
		.getContainer()
		.fetchSharedObject(context, SodaPopImpl.contentSerializerParams);
    }

    private String myID;
    private Hashtable localBusses; // <String, AbstractBus>
    private Hashtable peersOnBus; // <String, Vector<String>>
    private Hashtable remoteSodapops; // <String, SodaPopPeer>
    private Hashtable dispatchers; // <String,Dispatcher>
    private HashSet peersToNotifyBuses;
    private HashSet contactedPeers;

    // private StringBuffer logBuffer = new StringBuffer();
    // private long startTime;
    // private long stopTime;

    public SodaPopImpl(ModuleContext mc, String configHomePath,
	    Object[] connectorFetchParams, Object[] csFetchParams)
	    throws Exception {
	SodaPopImpl.moduleContext = mc;
	SodaPopImpl.contentSerializerParams = csFetchParams;
	myID = Message.thisJVM;

	localBusses = new Hashtable(5);
	peersOnBus = new Hashtable(5);
	remoteSodapops = new Hashtable();
	dispatchers = new Hashtable();
	contactedPeers = new HashSet();
	peersToNotifyBuses = new HashSet();

	moduleContext.logInfo(CryptUtil.init(configHomePath), null);

	synchronized (SodaPopImpl.moduleContext) {
	    try {
		Object[] connectors = SodaPopImpl.moduleContext.getContainer()
			.fetchSharedObject(SodaPopImpl.moduleContext,
				connectorFetchParams, this);
		if (connectors != null) {
		    for (int i = 0; i < connectors.length; i++) {
			P2PConnector c = (P2PConnector) connectors[i];
			c.register(this);
			c.addPeerDiscoveryListener(this);
		    }
		}
	    } catch (Exception e) {
		System.out.println(e);
	    }
	}
	LogUtils.logInfo(moduleContext, SodaPopImpl.class, "init",
		new Object[] { "SodaPopPeer with ID '", myID, "' started!" },
		null);
	// startTime = System.currentTimeMillis();
    }

    public void sharedObjectAdded(Object sharedObj, Object removeHook) {
	synchronized (SodaPopImpl.moduleContext) {
	    if (sharedObj instanceof P2PConnector) {
		((P2PConnector) sharedObj).register(this);
		((P2PConnector) sharedObj).addPeerDiscoveryListener(this);
	    }
	}
    }

    public void sharedObjectRemoved(Object removeHook) {
	// TODO
    }

    // //////////////////////////////////////////////////////////////////////
    // Remote Section : //
    // methods which invoke dual methods on remote peers. //
    // These methods are initially activated by connectors //
    // by noticing new peers //
    // //////////////////////////////////////////////////////////////////////

    public void noticeNewPeer(SodaPopPeer peer, String discoveryProtocol) {
	String id = peer.getID();
	Dispatcher dispatcher = new Dispatcher(peer);
	synchronized (remoteSodapops) {
	    remoteSodapops.put(id, peer);
	    dispatchers.put(id, dispatcher);
	}
	dispatcher.start();
	if (id.compareTo(myID) > 0) {
	    LogUtils.logInfo(moduleContext, SodaPopImpl.class, "noticeNewPeer",
		    new Object[] { "Discovered CONS peer  '", id, "'!" }, null);
	    forwardBusesToPeer(dispatcher);
	} else if (id.compareTo(myID) < 0) {
	    LogUtils.logInfo(moduleContext, SodaPopImpl.class, "noticeNewPeer",
		    new Object[] { "Discovered PRE peer  '", id, "'!" }, null);
	    synchronized (peersToNotifyBuses) {
		if (peersToNotifyBuses.contains(id)) {
		    replyBusesToPeer(dispatcher);
		    peersToNotifyBuses.remove(id);
		}
	    }
	} else {
	    LogUtils
		    .logWarn(
			    moduleContext,
			    SodaPopImpl.class,
			    "noticeNewPeer",
			    new Object[] { "Discovered peer with same ID: ", id },
			    null);
	}
    }

    public void noticeLostPeer(String peerID, String discoveryProtocol) {
	if (peerID == null || peerID.equals(""))
	    throw new IllegalArgumentException("Illegal peerID value");
	synchronized (remoteSodapops) {
	    remoteSodapops.remove(peerID);
	    ((Dispatcher) dispatchers.remove(peerID)).close();
	}
	synchronized (peersOnBus) {
	    for (Iterator i = peersOnBus.values().iterator(); i.hasNext();)
		((Vector) i.next()).remove(peerID);
	}
	LogUtils.logInfo(moduleContext, SodaPopImpl.class, "noticeLostPeer",
		new Object[] { "Peer '", peerID, "' disconnected!" }, null);
    }

    private void forwardBusesToPeer(Dispatcher dispatcher) {

	StringBuffer myBusses = new StringBuffer(256);
	synchronized (localBusses) {
	    for (Iterator i = localBusses.keySet().iterator(); i.hasNext();)
		myBusses.append(i.next()).append(',');
	}
	if (myBusses.length() > 0)
	    myBusses.deleteCharAt(myBusses.length() - 1);
	PeerCommand message = new PeerCommand(PeerCommand.NOTICE_PEER_BUSES,
		myID, myBusses.toString());
	dispatcher.queue.enqueue(message);
	LogUtils.logInfo(moduleContext, SodaPopImpl.class,
		"forwardBusesToPeer", new Object[] {
			"call remote NOTICE_PEER_BUSES on '",
			dispatcher.peer.getID(), "'!" }, null);
	synchronized (contactedPeers) {
	    contactedPeers.add(dispatcher);
	}
    }

    /**
     * @see org.universAAL.middleware.sodapop.SodaPop#join(AbstractBus)
     */
    public void join(AbstractBus b) {
	String busName = b.getBusName();
	synchronized (localBusses) {
	    if (localBusses.containsKey(busName))
		throw new RuntimeException("A bus with the name '" + busName
			+ "' is already registered!");
	    localBusses.put(busName, b);
	}
	synchronized (remoteSodapops) {
	    Enumeration list = dispatchers.elements();
	    while (list.hasMoreElements()) {
		Dispatcher dispatcher = (Dispatcher) list.nextElement();
		synchronized (contactedPeers) {
		    if (contactedPeers.contains(dispatcher)) {
			PeerCommand command = new PeerCommand(
				PeerCommand.JOIN_BUS, myID, busName);
			dispatcher.queue.enqueue(command);
			LogUtils.logInfo(moduleContext, SodaPopImpl.class,
				"join", new Object[] {
					"call remote JOIN_BUS on '",
					dispatcher.peer.getID(), "'!" }, null);
		    }
		}

	    }
	}
	LogUtils
		.logInfo(moduleContext, SodaPopImpl.class, "join",
			new Object[] { "Bus '", busName,
				"' joined the SodaPop engine!" }, null);
    }

    /**
     * @see org.universAAL.middleware.sodapop.SodaPop#leave(AbstractBus)
     */
    public void leave(AbstractBus b) {
	String busName = b.getBusName();
	synchronized (localBusses) {
	    if (localBusses.remove(busName) == null)
		throw new RuntimeException("A bus with the name '" + busName
			+ "' was never registered!");
	}
	synchronized (remoteSodapops) {
	    Enumeration list = dispatchers.elements();
	    while (list.hasMoreElements()) {
		Dispatcher dispatcher = (Dispatcher) list.nextElement();
		synchronized (contactedPeers) {
		    if (contactedPeers.contains(dispatcher)) {
			PeerCommand command = new PeerCommand(
				PeerCommand.LEAVE_BUS, myID, busName);
			dispatcher.queue.enqueue(command);
			LogUtils.logInfo(moduleContext, SodaPopImpl.class,
				"leave", new Object[] {
					"call remote LEAVE_BUS on '",
					dispatcher.peer.getID(), "'!" }, null);
		    }
		}
	    }
	}
	LogUtils.logInfo(moduleContext, SodaPopImpl.class, "leave",
		new Object[] { "Bus '", busName, "' stopped!" }, null);
    }

    /**
     * @see org.universAAL.middleware.sodapop.SodaPop#propagateMessage(AbstractBus,
     *      Message)
     */
    public int propagateMessage(AbstractBus b, Message m) {
	if (m == null || !myID.equals(m.getSource()))
	    return 0;

	String msg = m.toString(), cipher = msg, rcvrs = "";

	String busName = b.getBusName();
	int result = 0;
	synchronized (peersOnBus) {
	    Vector peersOnThisBus = (Vector) peersOnBus.get(busName);
	    if (peersOnThisBus != null && !peersOnThisBus.isEmpty()
		    && b == localBusses.get(busName)) {
		try {
		    cipher = CryptUtil.encrypt(msg);
		} catch (Exception e) {
		    LogUtils
			    .logWarn(
				    moduleContext,
				    SodaPopImpl.class,
				    "propagateMessage",
				    new Object[] { "Message encryption failed - trying to send it as clear text!" },
				    e);
		}
		PeerCommand command = new PeerCommand(
			PeerCommand.PROCESS_MESSAGE, busName, cipher);

		String[] receivers = m.getReceivers();
		if (receivers == null || receivers.length == 0) {
		    rcvrs = "all peers";
		    Iterator peerList = peersOnThisBus.iterator();
		    synchronized (remoteSodapops) {
			while (peerList.hasNext()) {
			    String peerID = (String) peerList.next();
			    ((Dispatcher) dispatchers.get(peerID)).queue
				    .enqueue(command);
			    result++;
			}
		    }
		} else {
		    synchronized (remoteSodapops) {
			for (int i = 0; i < receivers.length; i++)
			    if (peersOnThisBus.contains(receivers[i])) {
				rcvrs += receivers[i];
				((Dispatcher) dispatchers.get(receivers[i])).queue
					.enqueue(command);
				result++;
				rcvrs += " ";
			    }
		    }
		}

	    }
	}
	if (!"".equals(rcvrs))
	    LogUtils.logInfo(moduleContext, SodaPopImpl.class,
		    "propagateMessage", new Object[] { busName,
			    " - Message sent to ", rcvrs, ":\n", msg }, null);
	return result;
    }

    // ///////////////////////////////////////////////////////////////////////
    // LOCAL Section : //
    // methods invoked by remote peers //
    // //////////////////////////////////////////////////////////////////////

    /**
     * @see org.universAAL.middleware.acl.SodaPopPeer#noticePeerBusses(String,
     *      String)
     */
    public void noticePeerBusses(String peerID, String busNames) {
	LogUtils.logInfo(moduleContext, SodaPopImpl.class, "noticePeerBusses",
		new Object[] { "local NOTICE_PEER_BUSES called from '", peerID,
			"'!" }, null);
	synchronized (peersOnBus) {
	    StringTokenizer names = new StringTokenizer(busNames, ",");
	    while (names.hasMoreTokens()) {
		Vector peers = getBusPeers(names.nextToken());
		peers.add(peerID);
	    }
	}
	synchronized (remoteSodapops) {
	    Dispatcher dispatcher = (Dispatcher) dispatchers.get(peerID);
	    if (dispatcher == null) {
		synchronized (peersToNotifyBuses) {
		    peersToNotifyBuses.add(peerID);
		}
	    } else {
		replyBusesToPeer(dispatcher);
	    }
	}

	LogUtils.logInfo(moduleContext, SodaPopImpl.class, "noticePeerBusses",
		new Object[] { "Got busses of peer '", peerID, "'!" }, null);
    }

    private void replyBusesToPeer(Dispatcher dispatcher) {

	StringBuffer myBusses = new StringBuffer(256);
	synchronized (localBusses) {
	    for (Iterator i = localBusses.keySet().iterator(); i.hasNext();)
		myBusses.append(i.next()).append(',');
	}
	if (myBusses.length() > 0) {
	    myBusses.deleteCharAt(myBusses.length() - 1);
	    PeerCommand message = new PeerCommand(PeerCommand.REPLY_PEER_BUSES,
		    myID, myBusses.toString());
	    dispatcher.queue.enqueue(message);
	    LogUtils.logInfo(moduleContext, SodaPopImpl.class,
		    "replyBusesToPeer", new Object[] {
			    "call remote REPLY_PEER_BUSES on '",
			    dispatcher.peer.getID(), "'!" }, null);
	}
	synchronized (contactedPeers) {
	    contactedPeers.add(dispatcher);
	}
    }

    public void replyPeerBusses(String peerID, String busNames) {
	synchronized (peersOnBus) {
	    StringTokenizer names = new StringTokenizer(busNames, ",");
	    while (names.hasMoreTokens()) {
		Vector peers = getBusPeers(names.nextToken());
		peers.add(peerID);
	    }
	}
	LogUtils.logInfo(moduleContext, SodaPopImpl.class, "replyPeerBusses",
		new Object[] { "Got busses reply of peer '", peerID, "'!" },
		null);
    }

    /**
     * @see org.universAAL.middleware.acl.SodaPopPeer#joinBus(String, String)
     */
    public void joinBus(String busName, String joiningPeer) {
	synchronized (peersOnBus) {
	    getBusPeers(busName).add(joiningPeer);
	}
	LogUtils.logInfo(moduleContext, SodaPopImpl.class, "joinBus",
		new Object[] { "Peer '", joiningPeer, "' joins bus '", busName,
			"'!" }, null);
    }

    private Vector getBusPeers(String busName) {
	Vector peers = (Vector) peersOnBus.get(busName);
	if (peers == null) {
	    peers = new Vector();
	    peersOnBus.put(busName, peers);
	}
	return peers;
    }

    /**
     * @see org.universAAL.middleware.acl.SodaPopPeer#leaveBus(String, String)
     */
    public void leaveBus(String busName, String leavingPeer) {
	synchronized (peersOnBus) {
	    Vector peers = (Vector) peersOnBus.get(busName);
	    if (peers != null)
		peers.remove(leavingPeer);
	}
	LogUtils.logInfo(moduleContext, SodaPopImpl.class, "leaveBus",
		new Object[] { "Peer '", leavingPeer, "' leaves bus '",
			busName, "'!" }, null);
    }

    /**
     * @see org.universAAL.middleware.acl.SodaPopPeer#processBusMessage(String,
     *      String)
     */
    public void processBusMessage(String busName, String msg) {
	Message m = null;
	synchronized (localBusses) {
	    AbstractBus b = (AbstractBus) localBusses.get(busName);
	    if (b != null) {
		try {
		    m = new Message(CryptUtil.decrypt(msg));
		    b.handleRemoteMessage(m);
		} catch (Exception e) {
		    LogUtils
			    .logWarn(
				    moduleContext,
				    SodaPopImpl.class,
				    "processBusMessage",
				    new Object[] { "Message processing aborted due to the following exception:" },
				    e);
		}
	    } else
		LogUtils.logWarn(moduleContext, SodaPopImpl.class,
			"processBusMessage", new Object[] { "Bus '", busName,
				"' is absent locally!" }, null);
	}
	if (m == null)
	    LogUtils.logDebug(moduleContext, SodaPopImpl.class,
		    "processBusMessage", new Object[] { "Message '\n", msg,
			    "\n' received on bus '", busName,
			    "' could not be parsed!" }, null);
	else
	    LogUtils.logDebug(moduleContext, SodaPopImpl.class,
		    "processBusMessage", new Object[] { "Message '",
			    Long.toString(m.getSourceTimeOrder()),
			    "' received on bus '", busName, "' from peer '",
			    m.getSource(), "'!" }, null);
    }

    /**
     * @see org.universAAL.middleware.acl.SodaPopPeer#getID()
     * @see org.universAAL.middleware.sodapop.SodaPop#getID()
     */
    public String getID() {
	return myID;
    }

    // synchronized private final void logger(int level, String msg){
    //
    // // logBuffer.append(msg).append("\n)");
    // stopTime = System.currentTimeMillis();
    // if (log != null)
    // log.log(level, msg);
    // }

    // synchronized private final void logger(int level, String msg,Exception
    // ex){
    // // logBuffer.append(msg).append("\n)");
    // stopTime = System.currentTimeMillis();
    // if (log != null)
    // log.log(level, msg, ex);
    // }

    public void stop() {
	// TODO
    }

    /**
     * @see org.universAAL.middleware.sodapop.SodaPop#getLocalBusByName(String)
     */
    public AbstractBus getLocalBusByName(String name) {
	return (name == null) ? null : (AbstractBus) localBusses.get(name);
    }

    public void printStatus() {
	System.out.println();
	System.out.println("localBusses");
	System.out.println(localBusses.toString());
	System.out.println("===========\n");

	System.out.println("peersOnBus");
	System.out.println(peersOnBus.toString());
	System.out.println("===========\n");

	System.out.println("remoteSodapops");
	System.out.println(remoteSodapops.toString());
	System.out.println("===========\n");

	System.out.println("dispatchers");
	System.out.println(dispatchers.toString());
	System.out.println("===========\n");

	System.out.println("peersToNotifyBuses");
	System.out.println(peersToNotifyBuses.toString());
	System.out.println("===========\n");

	System.out.println("contactedPeers");
	System.out.println(contactedPeers.toString());
	System.out.println("===========\n");

	// System.out.println("logBuffer");
	// System.out.println(logBuffer.toString());
	// System.out.println("===========\n");

	// System.out.println("Timing");
	// System.out.println("started: "+ startTime);
	// System.out.println("stopped: "+ stopTime);
	// System.out.println("delta: "+ String.valueOf(stopTime-startTime));
	// System.out.println("===========\n");

    }

}
