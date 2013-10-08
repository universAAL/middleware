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

import java.io.IOException;
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
    LinkedList<Element<ContextEventPattern>> contextEventPatterns = new LinkedList<Element<ContextEventPattern>>();

    protected void writeManifest() {
	String current;
	try {
	    current = new java.io.File(".").getCanonicalPath();
	    System.out.println("Current dir:" + current);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    protected void add(String title, String description, ContextEventPattern cep) {
	contextEventPatterns.add(new Element<ContextEventPattern>(title,
		description, cep));
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
