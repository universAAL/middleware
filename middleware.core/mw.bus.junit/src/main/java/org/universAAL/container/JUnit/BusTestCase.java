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
package org.universAAL.container.JUnit;

//import java.io.File;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.util.LinkedList;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.ui.IUIBus;
import org.universAAL.middleware.ui.impl.UIBusImpl;

import junit.framework.TestCase;

/**
 * A special test case that also initializes the buses.
 * @author Carsten Stockloew
 *
 */
public class BusTestCase extends TestCase {

    protected ModuleContext mc;
    private MessageContentSerializer mcs;

    @Override
    protected void setUp() throws Exception {
	super.setUp();

	mc = new JUnitModuleContext();

	// init data representation
	OntologyManagement.getInstance().register(mc, new DataRepOntology());
	mc.getContainer().shareObject(mc, new TurtleSerializer(),
		new Object[] { MessageContentSerializer.class.getName() });
	mcs = (MessageContentSerializer) mc.getContainer().fetchSharedObject(
		mc, new Object[] { MessageContentSerializer.class.getName() });

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
