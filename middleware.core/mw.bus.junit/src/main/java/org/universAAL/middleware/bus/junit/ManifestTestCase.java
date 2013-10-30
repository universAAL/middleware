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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.owl.HasValueRestriction;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.owls.process.ProcessEffect;
import org.universAAL.middleware.service.owls.process.ProcessParameter;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.util.GraphIterator;
import org.universAAL.middleware.util.GraphIteratorElement;
import org.universAAL.middleware.util.ResourceComparator;

/**
 * A test case that is specifically used to create a functional manifest.
 * 
 * @author Carsten Stockloew
 * 
 */
public class ManifestTestCase extends BusTestCase {

    public static final int EXTEND_NOTHING = 0;

    public static final int EXTEND_HASVALUE = 1;
    public static final int EXTEND_ENUMERATION = 2;
    public static final int EXTEND_CHANGE_EFFECT = 4;

    public static final int EXTEND_ALL = 255;

    private ResourceComparator rc = new ResourceComparator();

    private class Element<T extends Resource> {
	String title = null;
	String description = null;
	T el = null;

	Element(String title, String description, T el) {
	    this.title = title;
	    this.description = description;
	    this.el = el;
	}
    };

    LinkedList<Element<ServiceRequest>> serviceRequests = new LinkedList<Element<ServiceRequest>>();
    LinkedList<Element<ServiceProfile>> serviceProfiles = new LinkedList<Element<ServiceProfile>>();
    LinkedList<Element<ContextEventPattern>> contextEventPatternsPublish = new LinkedList<Element<ContextEventPattern>>();
    LinkedList<Element<ContextEventPattern>> contextEventPatternsSubscribe = new LinkedList<Element<ContextEventPattern>>();
    LinkedList<Element<UIHandlerProfile>> uiHandler = new LinkedList<Element<UIHandlerProfile>>();
    LinkedList<Element<UIRequest>> uiRequests = new LinkedList<Element<UIRequest>>();

    protected void writeManifest() {
	PrintWriter writer;
	try {
	    File file = new File(".\\target\\uaal-manifest.xml");
	    try {
		String filename = file.getCanonicalPath();
		System.out.println("-- writing manifest to file " + filename);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    writer = new PrintWriter(file, "UTF-8");
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    return;
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	    return;
	}

	writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	writer.println("<application>");
	writer.println("   <permissions>");
	writeManifestEntry(writer, "mw.bus.service", serviceRequests,
		serviceProfiles);
	writeManifestEntry(writer, "mw.bus.context",
		contextEventPatternsSubscribe, contextEventPatternsPublish);
	writeManifestEntry(writer, "mw.bus.ui", uiRequests, uiHandler);
	writer.println("   </permissions>");
	writer.println("</application>");
	writer.close();
    }

    private void writeManifestEntry(PrintWriter writer, String brokerName,
	    LinkedList elReq, LinkedList elAd) {
	if (elReq.size() != 0 || elAd.size() != 0) {
	    writer.println("      <" + brokerName + ">");
	    writeManifestEntry(writer, "requirement", elReq);
	    writeManifestEntry(writer, "advertisement", elAd);
	    writer.println("      </" + brokerName + ">");
	}
    }

    private void writeManifestEntry(PrintWriter writer, String type,
	    LinkedList<Element> elements) {
	if (elements != null)
	    for (Element el : elements)
		writeManifestEntry(writer, type, el);
    }

    private void writeManifestEntry(PrintWriter writer, String type, Element el) {
	// escape
	String serialized = serialize(el.el);
	serialized.replace("]]>", "]]]]><![CDATA[>");

	// write
	writer.println("         <" + type + ">");
	writer.println("            <title>" + el.title + "</title>");
	writer.println("            <description>" + el.description
		+ "</description>");
	writer.println("            <serialization>");
	writer.println("               <![CDATA[" + serialized + "]]>");
	writer.println("            </serialization>");
	writer.println("         </" + type + ">");
    }

    protected void add(String title, String description,
	    ContextEventPattern cep, boolean isPublisher) {
	LogUtils.logDebug(mc, ManifestTestCase.class, "add", new Object[] {
		"Adding manifest entry: ", title }, null);
	assertTrue(cep.matches(cep));
	checkserialization(cep);
	LinkedList<Element<ContextEventPattern>> lst;
	if (isPublisher)
	    lst = contextEventPatternsPublish;
	else
	    lst = contextEventPatternsSubscribe;

	lst.add(new Element<ContextEventPattern>(title, description, cep));
    }

    protected void add(String title, String description,
	    UIHandlerProfile profile) {
	LogUtils.logDebug(mc, ManifestTestCase.class, "add", new Object[] {
		"Adding manifest entry: ", title }, null);
	assertTrue(profile.matches(profile));
	checkserialization(profile);
	uiHandler
		.add(new Element<UIHandlerProfile>(title, description, profile));
    }

    protected void add(String title, String description, UIRequest request) {
	LogUtils.logDebug(mc, ManifestTestCase.class, "add", new Object[] {
		"Adding manifest entry: ", title }, null);
	request.changeProperty(UIRequest.PROP_ADDRESSED_USER, null);
	request.changeProperty(UIRequest.PROP_DIALOG_FORM, null);
	assertTrue(request.matches(request));
	checkserialization(request);
	uiRequests.add(new Element<UIRequest>(title, description, request));
    }

    protected void add(String title, String description, ServiceProfile profile) {
	LogUtils.logDebug(mc, ManifestTestCase.class, "add", new Object[] {
		"Adding manifest entry: ", title }, null);
	assertTrue(profile.matches(profile));
	checkserialization(profile);
	serviceProfiles.add(new Element<ServiceProfile>(title, description,
		profile));
    }

    protected ServiceRequest add(String title, String description,
	    ServiceRequest r, boolean extendAll) {
	return add(title, description, r, extendAll ? EXTEND_ALL
		: EXTEND_NOTHING);
    }

    protected ServiceRequest add(String title, String description,
	    ServiceRequest r, int options) {
	LogUtils.logDebug(mc, ManifestTestCase.class, "add", new Object[] {
		"Adding manifest entry: ", title }, null);
	checkserialization(r);
	String serialized = serialize(r);
	ServiceRequest orig = (ServiceRequest) deserialize(serialized);

	// System.out.println("\n\n-- new Request " + title +
	// "(not extended):\n" + serialized);
	if (options != EXTEND_NOTHING) {
	    while (extendRequest(r, options)) {
	    }
	    // System.out.println("\n\n-- extended version of " + title + ":\n"
	    // + serialize(r));
	}
	assertTrue(r.matches(orig));
	checkserialization(r);

	serviceRequests.add(new Element<ServiceRequest>(title, description, r));
	return r;
    }

    private void checkserialization(Resource r1) {
	String serialized = serialize(r1);
	Resource r2 = deserialize(serialized);
	if (!rc.areEqual(r1, r2)) {
	    System.out.println(" -- ERROR during serialization: ");
	    System.out.println(rc.getDiffsAsString(r1, r2));
	    System.out.println(" -- 1. Resource:\n" + r1.toStringRecursive());
	    System.out.println(" -- Serialized:\n" + serialized);
	    System.out.println(" -- 2. Resource:\n" + r2.toStringRecursive());
	    System.out.println(" -- serialized again:\n" + serialize(r2));
	    // r2 = deserialize(serialized);
	    assertTrue(false);
	}
    }

    private boolean extendRequest(ServiceRequest r, int options) {
	// return true, if the RDF graph was changed
	Iterator git = GraphIterator.getIterator(r);
	while (git.hasNext()) {
	    GraphIteratorElement el = (GraphIteratorElement) git.next();
	    if ((options & EXTEND_HASVALUE) != 0) {
		if (extendRequestHasValue(el))
		    return true;
	    }
	    if ((options & EXTEND_CHANGE_EFFECT) != 0) {
		if (extendRequestChangeEffect(el))
		    return true;
	    }
	}
	return false;
    }

    private boolean extendRequestChangeEffect(GraphIteratorElement el) {
	// change ChangeEffect from a concrete value to a Variable
	Object sub = el.getSubject();
	if (sub instanceof Resource) {
	    Resource r = (Resource) sub;
	    if (ProcessEffect.TYPE_PROCESS_CHANGE_EFFECT.equals(r.getType())) {
		if (extendRequestTrySettingProcessParameter(r,
			ProcessEffect.PROP_PROCESS_PROPERTY_VALUE))
		    return true;
	    }
	}
	return false;
    }

    private boolean extendRequestTrySettingProcessParameter(Resource r,
	    String propURI) {
	Object obj = r.getProperty(propURI);
	if (!(obj instanceof ProcessParameter)) {
	    String typeURI = TypeMapper.getDatatypeURI(obj);
	    if (typeURI != null) {
		// System.out.println(" - changing type: " + typeURI
		// + " of Resource: " + r.toStringRecursive());
		// change the value to a Variable with the type of the value
		ProcessParameter in = new ProcessParameter(null,
			ProcessParameter.MY_URI);
		if (obj instanceof ManagedIndividual) {
		    in.setParameterType(((ManagedIndividual) obj).getClassURI());
		} else {
		    in.setParameterType(typeURI);
		}
		in.setCardinality(1, 1);
		r.changeProperty(propURI, in);
		// System.out.println(res);
		return true;
	    }
	}

	return false;
    }

    private boolean extendRequestHasValue(GraphIteratorElement el) {
	// change owl:hasValue from a concrete value to a Variable
	Object sub = el.getSubject();
	if (sub instanceof HasValueRestriction) {
	    HasValueRestriction has = (HasValueRestriction) sub;

	    if (extendRequestTrySettingProcessParameter(has,
		    HasValueRestriction.PROP_OWL_HAS_VALUE))
		return true;
	}

	// try to extend the request by changing owl:hasValue to
	// owl:allValuesFrom
	// return true, if the RDF graph was changed
	/*
	 * Object obj = el.getObject(); if (obj instanceof HasValueRestriction)
	 * { // we found a triple with owl:hasValue: the object of the triple is
	 * // a HasValueRestriction that we now have to change to an //
	 * AllValuesFromRestriction HasValueRestriction has =
	 * (HasValueRestriction) obj;
	 * 
	 * // create the AllValuesFromRestriction AllValuesFromRestriction all =
	 * null; Object constraint = has.getConstraint(); if (constraint
	 * instanceof ManagedIndividual) { all = new
	 * AllValuesFromRestriction(has.getOnProperty(), ((ManagedIndividual)
	 * constraint).getClassURI()); } else if (constraint instanceof
	 * Resource) { return false; } else { String datatypeURI =
	 * TypeMapper.getDatatypeURI(constraint); if (datatypeURI != null) { all
	 * = new AllValuesFromRestriction(has.getOnProperty(), datatypeURI); } }
	 * 
	 * // if we could not create the AllValuesFromRestriction just // go on
	 * searching if (all == null) return false;
	 * 
	 * // remove the HasValueRestriction and // set the
	 * AllValuesFromRestriction if (el.isList()) {
	 * el.getList().set(el.getListIndex() - 1, all); } else {
	 * el.getSubject().setProperty(el.getPredicate(), all); }
	 * 
	 * return true; }
	 */
	return false;
    }
}
