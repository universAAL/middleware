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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.owl.AllValuesFromRestriction;
import org.universAAL.middleware.owl.HasValueRestriction;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.util.GraphIterator;
import org.universAAL.middleware.util.GraphIteratorElement;

/**
 * A test case that is specifically used to create a functional manifest.
 * 
 * @author Carsten Stockloew
 * 
 */
public class ManifestTestCase extends BusTestCase {

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
	writer.println("   </permissions>");
	writer.println("</application>");
	writer.close();
    }

    private void writeManifestEntry(PrintWriter writer, String brokerName,
	    LinkedList elReq, LinkedList elAd) {
	writer.println("      <" + brokerName + ">");
	writeManifestEntry(writer, "requirement", elReq);
	writeManifestEntry(writer, "advertisement", elAd);
	writer.println("      </" + brokerName + ">");
    }

    private void writeManifestEntry(PrintWriter writer, String type,
	    LinkedList<Element> elements) {
	if (elements != null)
	    for (Element el : elements)
		writeManifestEntry(writer, type, el);
    }

    private void writeManifestEntry(PrintWriter writer, String type, Element el) {
	// TODO: escape the manifest entry: escape the <![CDATA[]]>
	writer.println("         <" + type + ">");
	writer.println("            <title>" + el.title + "</title>");
	writer.println("            <description>" + el.description
		+ "</description>");
	writer.println("            <serialization>");
	writer.println("               <![CDATA[" + serialize(el.el) + "]]>");
	writer.println("            </serialization>");
	writer.println("         </" + type + ">");
    }

    protected void add(String title, String description,
	    ContextEventPattern cep, boolean isPublisher) {
	LinkedList<Element<ContextEventPattern>> lst;
	if (isPublisher)
	    lst = contextEventPatternsPublish;
	else
	    lst = contextEventPatternsSubscribe;

	lst.add(new Element<ContextEventPattern>(title, description, cep));
    }

    protected void add(String title, String description, ServiceProfile profile) {
	serviceProfiles.add(new Element<ServiceProfile>(title, description,
		profile));
    }

    protected void add(String title, String description, ServiceRequest r,
	    boolean extend) {
	System.out.println("\n\n-- new Request (not extended):\n"
		+ serialize(r));
	if (extend) {
	    while (extendRequest(r)) {
	    }
	    System.out.println("\n\n-- new Request (extended):\n"
		    + serialize(r));
	}

	serviceRequests.add(new Element<ServiceRequest>(title, description, r));
    }

    private boolean extendRequest(ServiceRequest r) {
	// try to extend the request by changing owl:hasValue to
	// owl:allValuesFrom
	Iterator git = GraphIterator.getIterator(r);
	while (git.hasNext()) {
	    GraphIteratorElement el = (GraphIteratorElement) git.next();
	    Object obj = el.getObject();
	    if (el.getObject() instanceof HasValueRestriction) {
		// we found a triple with owl:hasValue: the object of the triple
		// is a HasValueRestriction that we now have to change to an
		// AllValuesFromRestriction
		HasValueRestriction has = (HasValueRestriction) obj;

		// create the AllValuesFromRestriction
		AllValuesFromRestriction all = null;
		Object constraint = has.getConstraint();
		if (constraint instanceof ManagedIndividual) {
		    all = new AllValuesFromRestriction(has.getOnProperty(),
			    ((ManagedIndividual) constraint).getClassURI());
		} else if (constraint instanceof Resource) {
		    continue;
		} else {
		    String datatypeURI = TypeMapper.getDatatypeURI(constraint);
		    if (datatypeURI != null) {
			all = new AllValuesFromRestriction(has.getOnProperty(),
				datatypeURI);
		    }
		}

		// if we could not create the AllValuesFromRestriction just go
		// on searching
		if (all == null)
		    continue;

		// remove the HasValueRestriction and
		// set the AllValuesFromRestriction
		if (el.isList()) {
		    el.getList().set(el.getListIndex() - 1, all);
		} else {
		    el.getSubject().setProperty(el.getPredicate(), all);
		}

		return true;
	    } else {
		// TODO: extend propertyValue of a ChangeEffect
		// TODO: what about other effects?
	    }
	}
	return false;
    }
}
