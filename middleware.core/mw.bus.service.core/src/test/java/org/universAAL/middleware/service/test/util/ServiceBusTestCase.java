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
package org.universAAL.middleware.service.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.universAAL.container.JUnit.JUnitContainer;
import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.service.impl.ServiceStrategy;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.service.test.ontology.DeviceService;
import org.universAAL.middleware.service.test.ontology.TestOntology;

import junit.framework.TestCase;

/**
 * An abstract class as base for unit tests that creates 3 instances of the
 * service bus; a coordinator and two nodes.
 * 
 * It uses some weird hacks to make it possible to have the 3 instances as the
 * bus normally is started only once. Main problems are that some variables are
 * static but need to be different for the different instances. This was
 * "solved" with Java reflection, a specific ordering of the creation of buses
 * and bus members, and certain assumptions (e.g. the custom AAL Space Manager
 * assumes that the method getPeerID is called exactly twice for each bus).
 * 
 * @author Carsten Stockloew
 * 
 */
public abstract class ServiceBusTestCase extends TestCase {
    /*
     * TODO: put the setUp()-method in a junit.extensions.TestSetup and add a
     * tearDown()-method so that it is possible to have multiple subclasses and
     * to allow a reset() in a new setUp()-method so that not every untit test
     * has to call it.
     */

    private static boolean isSetup = false;

    public static int COORD = 0;
    public static int NODE1 = 1;
    public static int NODE2 = 2;

    public static ModuleContext mc;
    public static MessageContentSerializer mcs;

    public static PeerCard coordCard = new PeerCard(PeerRole.COORDINATOR,
	    "OSGi", "universAAL");
    public static PeerCard node1Card = new PeerCard(PeerRole.PEER, "OSGi",
	    "universAAL");
    public static PeerCard node2Card = new PeerCard(PeerRole.PEER, "OSGi",
	    "universAAL");

    public static Map<String, String> mapReadableNodes = new HashMap<String, String>();
    public static List<String> lstReadableNodes = new ArrayList<String>();

    public static MyServiceCallee coordCallee1;
    public static MyServiceCallee coordCallee2;
    public static MyServiceCallee node1Callee1;
    public static MyServiceCallee node1Callee2;
    public static MyServiceCallee node2Callee1;
    public static MyServiceCallee node2Callee2;
    public static ServiceCaller coordCaller;
    public static ServiceCaller node1Caller;
    public static ServiceCaller node2Caller;

    public static List<ServiceBusImpl> lstBus = new ArrayList<ServiceBusImpl>();
    public static List<MyServiceCallee> lstCallees = new ArrayList<MyServiceCallee>();
    public static List<ServiceCaller> lstCallers = new ArrayList<ServiceCaller>();

    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
	super.setUp();

	if (isSetup)
	    return;
	isSetup = true;

	System.setProperty("org.universaal.bus.permission.mode", "none");

	System.out.println(" - starting BusTestCase -");
	mc = new JUnitModuleContext();

	// init data representation
	SharedResources.moduleContext = mc;
	SharedResources.loadReasoningEngine();
	SharedResources.middlewareProps.put(
		SharedResources.uAAL_IS_COORDINATING_PEER, "true");
	OntologyManagement.getInstance().register(mc, new DataRepOntology());
	OntologyManagement.getInstance().register(mc, new TestOntology());
	mc.getContainer().shareObject(mc, new TurtleSerializer(),
		new Object[] { MessageContentSerializer.class.getName() });
	mcs = (MessageContentSerializer) mc.getContainer().fetchSharedObject(
		mc, new Object[] { MessageContentSerializer.class.getName() });
	TurtleUtil.moduleContext = mc;

	// init bus model
	HashMap<String, PeerCard> mapCards = new HashMap<String, PeerCard>();
	mapCards.put(coordCard.getPeerID(), coordCard);
	mapCards.put(node1Card.getPeerID(), node1Card);
	mapCards.put(node2Card.getPeerID(), node2Card);
	List<PeerCard> lstCards = new ArrayList<PeerCard>();
	lstCards.add(coordCard);
	lstCards.add(node1Card);
	lstCards.add(node2Card);
	mapReadableNodes.put(coordCard.getPeerID(), "Coord");
	mapReadableNodes.put(node1Card.getPeerID(), "Node1");
	mapReadableNodes.put(node2Card.getPeerID(), "Node2");
	lstReadableNodes.add("Coord");
	lstReadableNodes.add("Node1");
	lstReadableNodes.add("Node2");

	AALSpaceManager sp = new MyAALSpaceManager(mapCards, lstCards);
	CommunicationModule com = new MyCommunicationModule(lstCards,
		mapReadableNodes);

	AbstractBus.initBrokerage(mc, sp, com);
	// BusMessage.setThisPeer(coordCard);
	BusMessage.setMessageContentSerializer(mcs);

	// init bus instances
	Object[] busFetchParams;

	// coordinator
	busFetchParams = new Object[] { "coordinator-container-service" };
	ServiceBusImpl.startModule(JUnitContainer.getInstance(), mc,
		busFetchParams, busFetchParams);

	// coordCallee1 = new MyServiceCallee(mc,
	// new ServiceProfile[] { ProfileUtil
	// .create_getControlledLamps(true) }, 1);
	coordCallee1 = new MyServiceCallee(mc, new ServiceProfile[0], 0, 0);
	coordCallee2 = new MyServiceCallee(mc, new ServiceProfile[0], 0, 1);
	coordCaller = new DefaultServiceCaller(mc);

	// ---------
	// node1

	Hashtable<String, String> newPropValues = new Hashtable<String, String>();
	newPropValues.put("org.universAAL.middleware.peer.is_coordinator",
		"false");
	SharedResources.updateProps(newPropValues);

	ServiceBusImpl sb = getBus(true);
	String peerID = sb.getPeerCard().getPeerID();
	System.out.println("-- registered new bus instance: Coord = " + peerID);

	createNode(1);
	createNode(2);

	lstCallees.add(coordCallee1);
	lstCallees.add(coordCallee2);
	lstCallees.add(node1Callee1);
	lstCallees.add(node1Callee2);
	lstCallees.add(node2Callee1);
	lstCallees.add(node2Callee2);

	lstCallers.add(coordCaller);
	lstCallers.add(node1Caller);
	lstCallers.add(node2Caller);

	// ServiceResponse sr = coordCaller.call(RequestUtil
	// .getAllLampsRequest(true));
	// List<?> l = sr.getOutput(RequestUtil.OUTPUT_LIST_OF_LAMPS, true);
	// System.out.println(" -- result: " + l.toArray().toString());

	// reset();

	System.out.println("\n\n----------------------\ninitialization done");
	for (int i = 0; i < 3; i++) {
	    String pi = lstCards.get(i).getPeerID();
	    System.out.println("\t" + mapReadableNodes.get(pi) + ":  " + pi);
	}
	System.out.println();
	int i = 0;
	for (MyServiceCallee c : lstCallees) {
	    if (c != null)
		System.out.println("\t" + lstReadableNodes.get(i / 2)
			+ " Callee (" + i + "): " + c.getMyID());
	    i++;
	}
	System.out.println();
	i = 0;
	for (ServiceCaller c : lstCallers) {
	    if (c != null)
		System.out.println("\t" + lstReadableNodes.get(i) + " Caller ("
			+ i + "): " + c.getMyID());
	    i++;
	}
	System.out.println("let the tests begin..\n----------------------\n\n");
    }

    /**
     * Creates a new instance of the service bus; thus, simulating a new node in
     * the network.
     * 
     * @param i
     *            One-based index of the node.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private void createNode(int i) throws NoSuchFieldException,
	    SecurityException, IllegalArgumentException,
	    IllegalAccessException, NoSuchMethodException,
	    InvocationTargetException {
	Object[] busFetchParams = new Object[] { "node" + i
		+ "-container-service" };
	ServiceBusImpl.startModule(JUnitContainer.getInstance(), mc,
		busFetchParams, busFetchParams);

	// We have to re-initialize the URIs of the bus, otherwise, the bus
	// members of the new node will have the same node id than the
	// coordinator.
	// But we can only do this ~after~ the coordinator-messages have been
	// exchanged and the coordinator is known. Otherwise the new node will
	// ask for the coordinator and the coordinator will get the URI from the
	// bus which is then the wrong URI (the URI of the new node)
	ServiceBusImpl sb = getBus(true);

	Field f = AbstractBus.class.getDeclaredField("busStrategy");
	f.setAccessible(true);
	ServiceStrategy strategy = (ServiceStrategy) f.get(sb);

	f = ServiceStrategy.class.getDeclaredField("theCoordinator");
	f.setAccessible(true);
	//System.out.println(" -- setting coord on peer ..");
	f.set(strategy, coordCard);
	//System.out.println(" -- setting coord on peer done");

	Method method = AbstractBus.class.getDeclaredMethod("createURIs");
	method.setAccessible(true);
	method.invoke(null);

	// node1Callee1 = new MyServiceCallee(mc,
	// new ServiceProfile[] { ProfileUtil
	// .create_getControlledLamps(true) }, 2);
	if (i == 1) {
	    node1Callee1 = new MyServiceCallee(mc, new ServiceProfile[0], 1, 0);
	    node1Callee2 = new MyServiceCallee(mc, new ServiceProfile[0], 1, 1);
	    node1Caller = new DefaultServiceCaller(mc);
	} else {
	    node2Callee1 = new MyServiceCallee(mc, new ServiceProfile[0], 2, 0);
	    node2Callee2 = new MyServiceCallee(mc, new ServiceProfile[0], 2, 1);
	    node2Caller = new DefaultServiceCaller(mc);
	}

	// sb = getBus(true);
	String peerID = sb.getPeerCard().getPeerID();
	System.out.println("-- registered new bus instance: Node" + i + " = "
		+ peerID);

    }

    // private void printMap(HashMap map) {
    // for (Object o : map.keySet()) {
    // Object v = map.get(o);
    // System.out.println("\t" + o + "\t" + v);
    // }
    // }

    /**
     * Removes all service call handler and service profiles from all callees.
     */
    public void reset() {
	resetProfiles();
	resetHandler();
	System.out.println("-------------------------------\nreset done\n");
    }

    /**
     * Removes all service call handler from all callees.
     */
    public void resetHandler() {
	for (MyServiceCallee c : lstCallees) {
	    if (c != null)
		c.setHandler(null);
	}
    }

    /**
     * Removes all service profiles from all callees.
     */
    public void resetProfiles() {
	HashMap<?, ?> map = lstBus.get(0).getMatchingServices(
		DeviceService.MY_URI);

	for (MyServiceCallee c : lstCallees) {
	    if (c != null)
		c.reset();
	}

	do {
	    try {
		Thread.sleep(10);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    map = lstBus.get(0).getMatchingServices(DeviceService.MY_URI);
	} while (map.keySet().size() != 0);
    }

    /**
     * Get the number of registered profiles.
     * 
     * @return
     */
    public int getNumRegisteredProfiles() {
	HashMap<?, ?> map = lstBus.get(0).getMatchingServices(
		DeviceService.MY_URI);
	int num = 0;
	for (Object o : map.values()) {
	    num += ((List<?>) o).size();
	}
	return num;
    }

    /**
     * Wait until the number of registered profiles have changed.
     * 
     * @param old
     */
    public void waitForProfileNumberChange(int old) {
	int to = 0;
	while (getNumRegisteredProfiles() == old) {
	    try {
		Thread.sleep(10);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    to++;
	    if (to > 1000)
		throw new RuntimeException(
			"Timeout occurred while waiting for the number of registered profiles to change; old number: "
				+ old);
	}
    }

    /**
     * Get the service caller on a specific node.
     * 
     * @param node
     * @return
     */
    public ServiceCaller getCaller(int node) {
	return lstCallers.get(node);
    }

    /**
     * Get s specific service callee on a specific node.
     * 
     * @param node
     * @return
     */
    public MyServiceCallee getCallee(int node, int callee) {
	// determine the index of the callee assuming that we have 2 callees per
	// node
	int i = node * 2 + callee;
	return lstCallees.get(i);
    }

    /**
     * Set the handler to handle service calls for a specific callee.
     * 
     * @param node
     * @param callee
     * @param handler
     */
    public void setHandler(int node, int callee, CallHandler handler) {
	MyServiceCallee c = getCallee(node, callee);
	c.setHandler(handler);
    }

    /**
     * Set the handler to handle service calls for the first callee.
     * 
     * @param node
     * @param handler
     */
    public void setHandler(int node, CallHandler handler) {
	setHandler(node, 0, handler);
    }

    /**
     * Call a service from a given node.
     * 
     * @param node
     * @param sr
     * @return
     */
    public ServiceResponse call(int node, ServiceRequest sr) {
	ServiceCaller c = getCaller(node);
	return c.call(sr);
    }

    /**
     * Deploy some profiles to a given callee on the given node. The profiles
     * are added to existing profiles.
     * 
     * @param node
     * @param profiles
     */
    public void deployProfiles(int node, int callee, ServiceProfile profiles[]) {
	int num = getNumRegisteredProfiles();
	MyServiceCallee c = getCallee(node, callee);
	c.addProfiles(profiles);
	waitForProfileNumberChange(num);
    }

    /**
     * Deploy a profile to a given callee on the given node. The profiles are
     * added to existing profiles.
     * 
     * @param node
     * @param profiles
     */
    public void deployProfiles(int node, int callee, ServiceProfile profile) {
	deployProfiles(node, callee, new ServiceProfile[] { profile });
    }

    /**
     * Deploy some profiles to the first callee on the given node. The profiles
     * are added to existing profiles.
     * 
     * @param node
     * @param profiles
     */
    public void deployProfiles(int node, ServiceProfile profiles[]) {
	deployProfiles(node, 0, profiles);
    }

    /**
     * Deploy a single profiles to the first callee on the given node. The
     * profile is added to existing profiles.
     * 
     * @param node
     * @param profiles
     */
    public void deployProfiles(int node, ServiceProfile profile) {
	deployProfiles(node, new ServiceProfile[] { profile });
    }

    private ServiceBusImpl getBus(boolean setToNull)
	    throws NoSuchFieldException, SecurityException,
	    IllegalArgumentException, IllegalAccessException {
	Field f = ServiceBusImpl.class.getDeclaredField("theServiceBus");
	f.setAccessible(true);
	Object objSBus = f.get(null);
	if (setToNull)
	    f.set(null, null);
	lstBus.add((ServiceBusImpl) objSBus);
	return (ServiceBusImpl) objSBus;
    }

    public String serialize(Resource r) {
	return mcs.serialize(r);
    }

    public Resource deserialize(String s) {
	return (Resource) mcs.deserialize(s);
    }
}
