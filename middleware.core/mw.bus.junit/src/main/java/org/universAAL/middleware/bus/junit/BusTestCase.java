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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.JUnit.JUnitContainer;
import org.universAAL.middleware.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.managers.api.SpaceListener;
import org.universAAL.middleware.managers.api.SpaceManager;
import org.universAAL.middleware.managers.api.MatchingResult;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.exception.CommunicationModuleException;
import org.universAAL.middleware.modules.listener.MessageListener;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceBus.CallInjector;
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

	protected static ModuleContext mc;
	protected static HashMap<String, MessageContentSerializer> mcs = new HashMap<String, MessageContentSerializer>();
	private static boolean isInitialized = false;

	protected static String serializationTypeDefault;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (isInitialized)
			return;
		isInitialized = true;

//		System.out.println(" - starting BusTestCase -");
		mc = new JUnitModuleContext(new ModuleActivator() {

			public void stop(ModuleContext mc) throws Exception {
			}

			public void start(ModuleContext mc) throws Exception {
			}
		}, this.getClass().getName());

		mc.logInfo("BusTestCase", "Starting BusTestCase", null);

		// init data representation
		SharedResources.moduleContext = mc;
		SharedResources.loadReasoningEngine();
		SharedResources.setMiddlewareProp(SharedResources.IS_COORDINATING_PEER, "true");
		//SharedResources.loadReasoningEngine(); already registers dataRepOntology
		//OntologyManagement.getInstance().register(mc, new DataRepOntology());
		TurtleSerializer turtleS = new TurtleSerializer();
        Dictionary< String, Object> dic = new Hashtable<String, Object>();
        dic.put(MessageContentSerializer.CONTENT_TYPE, turtleS.getContentType());
		mc.getContainer().shareObject(mc, turtleS, new Object[] { MessageContentSerializer.class.getName(), MessageContentSerializerEx.class.getName(),dic });
		mcs.put(turtleS.getContentType(), turtleS);
		TurtleUtil.moduleContext = mc;
		
		//make it default
		serializationTypeDefault = turtleS.getContentType();

		JSONLDSerialization jsonS = new JSONLDSerialization();
        dic = new Hashtable<String, Object>();
        dic.put(MessageContentSerializer.CONTENT_TYPE, jsonS.getContentType());
		mc.getContainer().shareObject(mc, jsonS, new Object[] { MessageContentSerializer.class.getName(),  MessageContentSerializerEx.class.getName(), dic});
		mcs.put(jsonS.getContentType(), jsonS);
		JSONLDSerialization.owner = mc;

		// init bus model
		final PeerCard myCard = new PeerCard(PeerRole.COORDINATOR, "", "");

		SpaceManager sp = new SpaceManager() {
			public void dispose() {
			}

			public boolean init() {
				return false;
			}

			public void loadConfigurations(Dictionary arg0) {
			}

			public void addSpaceListener(SpaceListener arg0) {
			}

			public SpaceDescriptor getSpaceDescriptor() {
				return new SpaceDescriptor() {
					private static final long serialVersionUID = -7504183020450042989L;

					public SpaceCard getSpaceCard() {
						SpaceCard sc = new SpaceCard();
						sc.setSpaceID("TestSpaceID");
						return sc;
					}
				};
			}

			public Set<SpaceCard> getSpaces() {
				return null;
			}

			public Map<String, SpaceDescriptor> getManagedSpaces() {
				return null;
			}

			public MatchingResult getMatchingPeers(Map<String, Serializable> arg0) {
				return null;
			}

			public PeerCard getMyPeerCard() {
				return myCard;
			}

			public Map<String, Serializable> getPeerAttributes(List<String> arg0, PeerCard arg1) {
				return null;
			}

			public Map<String, PeerCard> getPeers() {
				HashMap map = new HashMap();
				map.put(myCard.getPeerID(), myCard);
				return map;
			}

			public void join(SpaceCard arg0) {
			}

			public void leaveSpace(SpaceDescriptor arg0) {
			}

			public void removeSpaceListener(SpaceListener arg0) {
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

			public MessageListener getListenerByNameAndType(String arg0, Class arg1) {
				return null;
			}

			public boolean hasChannel(String arg0) {
				return true;
			}

			public void messageReceived(ChannelMessage arg0) {
			}

			public void removeMessageListener(MessageListener arg0, String arg1) {
			}

			public void send(ChannelMessage arg0, PeerCard arg1) throws CommunicationModuleException {
			}

			public void send(ChannelMessage arg0, MessageListener arg1, PeerCard arg2)
					throws CommunicationModuleException {
			}

			public void sendAll(ChannelMessage arg0) throws CommunicationModuleException {
			}

			public void sendAll(ChannelMessage arg0, List<PeerCard> arg1) throws CommunicationModuleException {
			}

			public void sendAll(ChannelMessage arg0, MessageListener arg1) throws CommunicationModuleException {
			}

			public void sendAll(ChannelMessage arg0, List<PeerCard> arg1, MessageListener arg2)
					throws CommunicationModuleException {
			}
		};

		mc.getContainer().shareObject(mc, sp, new Object[] { SpaceManager.class.getName() });

		AbstractBus.initBrokerage(mc, sp, com);
		BusMessage.setThisPeer(myCard);

		// init buses
		Object[] busFetchParams;

		busFetchParams = new Object[] { ContextBus.class.getName() };
		ContextBusImpl.startModule(JUnitContainer.getInstance(), mc, busFetchParams, busFetchParams);

		busFetchParams = new Object[] { ServiceBus.class.getName() };
		Object[] busInjectFetchParams = new Object[] { CallInjector.class.getName() };
		ServiceBusImpl.startModule(mc, busFetchParams, busFetchParams, busInjectFetchParams, busInjectFetchParams);

		busFetchParams = new Object[] { IUIBus.class.getName() };
		UIBusImpl.startModule(JUnitContainer.getInstance(), mc, busFetchParams, busFetchParams);

		// Package p[] = Package.getPackages();
		// for (int i=0; i<p.length; i++)
		// System.out.println("--Package: " + p[i].getName());
		// getOntologies();

		mc.logInfo("BusTestCase", "Buses Loaded.", null);
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

	public String serialize(Resource r) {
		return serialize(r, serializationTypeDefault);
	}

	public Resource deserialize(String s) {
		return deserialize(s, serializationTypeDefault);
	}
	public String serialize(Resource r, String serializationType) {
		return mcs.get(serializationType).serialize(r);
	}

	public Resource deserialize(String s, String serializationType) {
		return (Resource) mcs.get(serializationType).deserialize(s);
	}
}
