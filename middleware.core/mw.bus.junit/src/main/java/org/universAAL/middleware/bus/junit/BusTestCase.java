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
package org.universAAL.middleware.bus.junit;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.container.JUnit.JUnitContainer;
import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.MatchingResult;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.exception.CommunicationModuleException;
import org.universAAL.middleware.modules.listener.MessageListener;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.ui.IUIBus;
import org.universAAL.middleware.ui.impl.UIBusImpl;

import junit.framework.TestCase;

/**
 * A special test case that also initializes the buses.
 * 
 * @author Carsten Stockloew
 * 
 */
public class BusTestCase extends TestCase {

    protected ModuleContext mc;
    private MessageContentSerializer mcs;

    @Override
    protected void setUp() throws Exception {
	super.setUp();

	System.out.println(" - starting BusTestCase -");
	mc = new JUnitModuleContext();

	// init data representation
	SharedResources.moduleContext = mc;
	SharedResources.loadReasoningEngine();
	SharedResources.middlewareProps.put(
		SharedResources.uAAL_IS_COORDINATING_PEER, "true");
	OntologyManagement.getInstance().register(mc, new DataRepOntology());
	mc.getContainer().shareObject(mc, new TurtleSerializer(),
		new Object[] { MessageContentSerializer.class.getName() });
	mcs = (MessageContentSerializer) mc.getContainer().fetchSharedObject(
		mc, new Object[] { MessageContentSerializer.class.getName() });
	TurtleUtil.moduleContext = mc;

	// init bus model
	final PeerCard myCard = new PeerCard(PeerRole.COORDINATOR, "", "");

	AALSpaceManager sp = new AALSpaceManager() {
	    public void dispose() {
	    }

	    public boolean init() {
		return false;
	    }

	    public void loadConfigurations(Dictionary arg0) {
	    }

	    public void addAALSpaceListener(AALSpaceListener arg0) {
	    }

	    public AALSpaceDescriptor getAALSpaceDescriptor() {
		return null;
	    }

	    public Set<AALSpaceCard> getAALSpaces() {
		return null;
	    }

	    public Map<String, AALSpaceDescriptor> getManagedAALSpaces() {
		return null;
	    }

	    public MatchingResult getMatchingPeers(
		    Map<String, Serializable> arg0) {
		return null;
	    }

	    public PeerCard getMyPeerCard() {
		return myCard;
	    }

	    public Map<String, Serializable> getPeerAttributes(
		    List<String> arg0, PeerCard arg1) {
		return null;
	    }

	    public Map<String, PeerCard> getPeers() {
		HashMap map = new HashMap();
		map.put(myCard.getPeerID(), myCard);
		return map;
	    }

	    public void join(AALSpaceCard arg0) {
	    }

	    public void leaveAALSpace(AALSpaceDescriptor arg0) {
	    }

	    public void removeAALSpaceListener(AALSpaceListener arg0) {
	    }
	};

	CommunicationModule com = new CommunicationModule() {
	    public void dispose() {
	    }

	    public String getDescription() {
		return null;
	    }

	    public String getName() {
		return null;
	    }

	    public String getProvider() {
		return null;
	    }

	    public String getVersion() {
		return null;
	    }

	    public boolean init() {
		return false;
	    }

	    public void loadConfigurations(Dictionary arg0) {
	    }

	    public void addMessageListener(MessageListener arg0, String arg1) {
	    }

	    public MessageListener getListenerByNameAndType(String arg0,
		    Class arg1) {
		return null;
	    }

	    public boolean hasChannel(String arg0) {
		return true;
	    }

	    public void messageReceived(ChannelMessage arg0) {
	    }

	    public void removeMessageListener(MessageListener arg0, String arg1) {
	    }

	    public void send(ChannelMessage arg0, PeerCard arg1)
		    throws CommunicationModuleException {
	    }

	    public void send(ChannelMessage arg0, MessageListener arg1,
		    PeerCard arg2) throws CommunicationModuleException {
	    }

	    public void sendAll(ChannelMessage arg0)
		    throws CommunicationModuleException {
	    }

	    public void sendAll(ChannelMessage arg0, List<PeerCard> arg1)
		    throws CommunicationModuleException {
	    }

	    public void sendAll(ChannelMessage arg0, MessageListener arg1)
		    throws CommunicationModuleException {
	    }

	    public void sendAll(ChannelMessage arg0, List<PeerCard> arg1,
		    MessageListener arg2) throws CommunicationModuleException {
	    }
	};

	AbstractBus.initBrokerage(mc, sp, com);
	BusMessage.setThisPeer(myCard);

	// init buses
	Object[] busFetchParams;

	busFetchParams = new Object[] { ContextBus.class.getName() };
	ContextBusImpl.startModule(JUnitContainer.getInstance(), mc,
		busFetchParams, busFetchParams);

	busFetchParams = new Object[] { ServiceBusImpl.class.getName() };
	ServiceBusImpl.startModule(JUnitContainer.getInstance(), mc,
		busFetchParams, busFetchParams);

	busFetchParams = new Object[] { IUIBus.class.getName() };
	UIBusImpl.startModule(JUnitContainer.getInstance(), mc, busFetchParams,
		busFetchParams);

	// Package p[] = Package.getPackages();
	// for (int i=0; i<p.length; i++)
	// System.out.println("--Package: " + p[i].getName());
	// getOntologies();
    }

    /*
     * private void getOntologies() { try { ClassLoader cl =
     * Thread.currentThread().getContextClassLoader(); URLClassLoader ucl =
     * (URLClassLoader) cl; URL[] url = ucl.getURLs(); for (int i = 0; i <
     * url.length; i++) { System.out.println("--URL: " + url[i]); String
     * urlString = url[i].toString(); LinkedList<URL> result = new
     * LinkedList<URL>(); if (urlString.endsWith("/")) { // directory search if
     * (urlString.contains("ont.phWorld")) searchOntologiesInDirectory(new
     * File(url[i].toURI()), result); } else if (urlString.endsWith(".jar")) {
     * 
     * }
     * 
     * for (URL utemp : result) { System.out.println("   --Ontology: " + utemp);
     * } } } catch (Exception e) { e.printStackTrace(); } }
     * 
     * private void searchOntologiesInDirectory(File file, LinkedList<URL>
     * result) { //System.out.println("   - searching file: " +
     * file.toString()); if (file.isDirectory()) { if (file.canRead()) { for
     * (File temp : file.listFiles()) searchOntologiesInDirectory(temp, result);
     * } } else { //System.out.println("   - found file: " + file.toString());
     * if (file.toString().endsWith("Ontology.class")) { try {
     * result.add(file.toURI().toURL()); } catch (MalformedURLException e) {
     * e.printStackTrace(); } } } }
     */

    protected String serialize(Resource r) {
	return mcs.serialize(r);
    }

    protected Resource deserialize(String s) {
	return (Resource) mcs.deserialize(s);
    }
}
