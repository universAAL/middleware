/*
	Copyright 2007-2016 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.TypeExpressionFactory;
import org.universAAL.middleware.rdf.ClosedCollection;
import org.universAAL.middleware.rdf.OpenCollection;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.GraphIterator.ObjectEqualsResource;

/**
 * Specialization of a resource graph. Takes an existing resource as input (with
 * resources may be specialized or not) and creates an appropriate copy of the
 * graph with all resources being specialized, i.e. the resources are instances
 * of some subclass of {@link Resource}.
 *
 * This class can be used to create custom de-serializers for different formats
 * (e.g. turtle, XML, N3, JSON). The de-serializer has to parse the serialized
 * string, create a graph of instances of {@link Resource} and let this class
 * make the specialization.
 *
 * @author Carsten Stockloew
 */
public final class Specializer {

    private Map<ObjectEqualsResource, Resource> specialized = new HashMap<ObjectEqualsResource, Resource>();

    /**
     * A dummy object for performance that is only used to call the
     * contains-method of {@link #specialized}.
     */
    private ObjectEqualsResource idxdummy = new ObjectEqualsResource(null);

    public Resource specialize(Resource root) {
	if (root == null)
	    return null;

	// get the list of resources and specialize all the resources
	List<Resource> lst = new LinkedList<Resource>();
	Iterator<Resource> it = GraphIterator.getResourceIterator(root);
	while (it.hasNext()) {
	    Resource r = it.next();
	    lst.add(r);
	    specializeSingle(r);
	}

	// debug: sysout specialized
	// for (Resource r : specialized.values()) {
	// System.out.println(" -- specialized: " + r.getURI() + "\t"
	// + r.getClass().getSimpleName());
	// }

	// now go over the list in reverse order and set the properties.
	// We use reverse order so that the property values are fully valid when
	// a property is set. This is not entirely true in case of cycles.
	ListIterator<Resource> li = lst.listIterator(lst.size());
	while (li.hasPrevious()) {
	    Resource unspec = li.previous();
	    Resource spec = specialized.get(idxdummy.set(unspec));
	    copyProperties(unspec, spec);
	}

	// clean up
	Resource ret = specialized.get(idxdummy.set(root));
	lst.clear();
	specialized.clear();
	return ret;
    }

    private void copyProperties(Resource r1, Resource r2) {
	// copy all properties from r1 (unspecialized) to r2 (specialized)
	// r1 and r2 must be valid resources at this point, i.e. not null
	for (Enumeration e = r1.getPropertyURIs(); e.hasMoreElements();) {
	    String propURI = (String) e.nextElement();
	    Object val = r1.getProperty(propURI);

	    if (val instanceof Resource) {
		val = specialized.get(idxdummy.set((Resource) val));
	    } else if (val instanceof List) {
		List list;
		if (val instanceof ClosedCollection)
		    list = new ClosedCollection();
		else if (val instanceof OpenCollection)
		    list = new OpenCollection();
		else
		    list = new ArrayList();

		ListIterator li = ((List) val).listIterator();
		while (li.hasNext()) {
		    Object el = li.next();
		    if (el instanceof Resource) {
			Resource specval = specialized.get(idxdummy
				.set(((Resource) el)));
			list.add(specval);
		    } else {
			list.add(el);
		    }
		}
		val = list;
	    }
	    if (!r2.setProperty(propURI, val))
		if (!Resource.PROP_RDF_TYPE.equals(propURI)) {
		    // The type is often set already by the class itself,
		    // preventing 'further' types from being set, therefore,
		    // we only provide a log entry if the property is not
		    // rdf:type
		    LogUtils.logDebug(
			    SharedResources.moduleContext,
			    Specializer.class,
			    "copyProperties",
			    new Object[] {
				    "Property ",
				    propURI,
				    " could not be correctly set for resource ",
				    r1.getURI() }, null);
		}
	}
    }

    private Resource specializeSingle(Resource r) {
	// check if it has already been handled
	Resource substitution = specialized.get(idxdummy.set(r));
	if (substitution != null) {
	    return substitution;
	}

	String[] types = r.getTypes();
	if (types == null || types.length == 0) {
	    // no type info -> this resource cannot be specialized
	    // (we still store it in 'specialized' table to avoid handling it
	    // again)
	    substitution = r;
	} else {
	    String type = OntologyManagement.getInstance()
		    .getMostSpecializedClass(types);
	    if (type == null) {
		substitution = TypeExpressionFactory.specialize(r);
	    } else {
		substitution = OntologyManagement.getInstance().getResource(
			type, r.getURI());
	    }

	    if (substitution == null) {
		// the resource cannot be specialized
		// LogUtils.logDebug(SharedResources.moduleContext,
		// Specializer.class, "specialize", new Object[] {
		// "Resource not specialized: type = ", type },
		// null);
		substitution = r;
	    }
	}

	if (r.serializesAsXMLLiteral())
	    substitution.literal();

	specialized.put(new ObjectEqualsResource(r), substitution);
	return substitution;
    }
}
