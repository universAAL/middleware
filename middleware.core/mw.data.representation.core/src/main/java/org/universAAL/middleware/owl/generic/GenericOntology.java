/*
	Copyright 2016-2020 Carsten Stockloew

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
package org.universAAL.middleware.owl.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.owl.DatatypeProperty;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.ObjectProperty;
import org.universAAL.middleware.owl.OntClassInfoSetup;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.PropertyRestriction;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.rdf.ClosedCollection;
import org.universAAL.middleware.rdf.Property;
import org.universAAL.middleware.rdf.PropertySetup;
import org.universAAL.middleware.rdf.RDFClassInfoSetup;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.GraphIterator;
import org.universAAL.middleware.util.GraphIteratorElement;
import org.universAAL.middleware.util.Specializer;

/**
 * A generic ontology that can be used to register an ontology from a plain list
 * of resource, e.g. coming from deserializing an ontology.
 * 
 * @author Carsten Stockloew
 */
public class GenericOntology extends Ontology {

    private List<Resource> plain = null;
    private static GenericResourceFactory fac = new GenericResourceFactory();
    private int idx = 0;

    public GenericOntology(String uri, List<Resource> plain) {
	super(uri);
	if (plain == null)
	    throw new NullPointerException("Plain ontology info is null");
	if (plain.size() == 0)
	    throw new NullPointerException("Plain ontology info has size 0");
	this.plain = plain;
    }

    @Override
    public void create() {
	// go through the 'parsed' - list and set all infos

	// the 'root' resource is just a helper to avoid looping over the list
	Resource root = new Resource();
	root.setProperty("propURI", plain);

	// the first element is the ontology definition
	Resource info = plain.get(0);
	Resource r = getInfo();
	Enumeration infoProps = info.getPropertyURIs();
	while (infoProps.hasMoreElements()) {
	    String propURI = (String) infoProps.nextElement();
	    // TODO: handle imports correctly
	    // -> should be done in Ontology class directly
	    r.setProperty(propURI, info.getProperty(propURI));
	}

	// now find all classes, we need to set them up first to avoid problems
	// with references (e.g. if a class has an AllValuesFromRestriction with
	// a class that is not registered yet)
	// all new classes should have a property
	// rdf:type -> owl:class/rdfs:class
	// store all classes in a Map classURI -> XXXClassInfoSetup
	// also: find all properties
	// also: find all subclass definitions
	Map<String, OntClassInfoSetup> ontClassSetup = new HashMap<String, OntClassInfoSetup>();
	Map<String, RDFClassInfoSetup> rdfClassSetup = new HashMap<String, RDFClassInfoSetup>();
	List<Resource> props = new LinkedList<Resource>();
	Map<Resource, List<Resource>> defSubClass = new HashMap<Resource, List<Resource>>();
	Iterator<GraphIteratorElement> it = GraphIterator.getIterator(root);
	Set<Resource> defEnum = new HashSet<Resource>();
	Object o;
	while (it.hasNext()) {
	    GraphIteratorElement el = it.next();
	    Resource subj = el.getSubject();
	    if (subj.isAnon())
		continue;
	    if (Resource.PROP_RDF_TYPE.equals(el.getPredicate())) {
		o = el.getObject();
		if (o instanceof Resource) {
		    r = (Resource) o;
		    String classURI = r.getURI();
		    RDFClassInfoSetup tmpSetup = null;
		    if (TypeExpression.OWL_CLASS.equals(classURI)) {
			// found a new ont class
			if (!ontClassSetup.containsKey(subj.getURI())) {
			    // System.out.println("Found ont class: "
			    // + subj.getURI());
			    OntClassInfoSetup setup = createNewOntClassInfo(
				    subj.getURI(), fac, ++idx);
			    ontClassSetup.put(subj.getURI(), setup);
			    tmpSetup = setup;
			} else {
			    tmpSetup = ontClassSetup.get(subj.getURI());
			}
			if (subj.hasProperty(org.universAAL.middleware.owl.Enumeration.PROP_OWL_ONE_OF)) {
			    defEnum.add(subj);
			}
		    } else if (Resource.TYPE_RDFS_CLASS.equals(classURI)) {
			// found a new rdf class
			if (!rdfClassSetup.containsKey(subj.getURI())) {
			    // System.out.println("Found rdf class: "
			    // + subj.getURI());
			    RDFClassInfoSetup setup = createNewRDFClassInfo(
				    subj.getURI(), fac, ++idx);
			    rdfClassSetup.put(subj.getURI(), setup);
			    tmpSetup = setup;
			} else {
			    tmpSetup = rdfClassSetup.get(subj.getURI());
			}
		    } else if (DatatypeProperty.MY_URI.equals(classURI)
			    || ObjectProperty.MY_URI.equals(classURI)) {
			props.add(subj);
		    }
		    if (tmpSetup != null) {
			// set label and comment
			tmpSetup.setResourceLabel(subj.getResourceLabel());
			tmpSetup.setResourceComment(subj.getResourceComment());
		    }
		}
	    } else if (TypeExpression.PROP_RDFS_SUB_CLASS_OF.equals(el
		    .getPredicate())) {
		if (el.getObject() instanceof Resource) {
		    List<Resource> lst = defSubClass.get(subj);
		    if (lst == null) {
			lst = new ArrayList<Resource>();
			defSubClass.put(subj, lst);
		    }
		    lst.add((Resource) el.getObject());
		    // System.out.println("Found super class for: "
		    // + subj.getURI());
		}
	    }
	}

	// register all properties
	Map<String, PropertySetup> propSetup = new HashMap<String, PropertySetup>();
	for (Resource p : props) {
	    Resource domain = (Resource) p
		    .getProperty(Property.PROP_RDFS_DOMAIN);
	    if (domain != null) {
		// the domain is the class to which we have to add the property
		// get the ont class setup
		OntClassInfoSetup setup = ontClassSetup.get(domain.getURI());
		if (setup == null) {
		    // found a property with a domain that does not exist
		    // (at least not in this ontology)
		    // -> extend an ont class and create the setup
		    setup = extendExistingOntClassInfo(domain.getURI());
		    ontClassSetup.put(domain.getURI(), setup);
		}
		PropertySetup tmpSetup = null;
		if (Arrays.asList(p.getTypes()).contains(
			DatatypeProperty.MY_URI)) {
		    tmpSetup = setup.addDatatypeProperty(p.getURI());
		} else if (Arrays.asList(p.getTypes()).contains(
			ObjectProperty.MY_URI)) {
		    tmpSetup = setup.addObjectProperty(p.getURI());
		} else {
		    LogUtils.logWarn(SharedResources.moduleContext,
			    GenericOntology.class, "create",
			    "Found property that is neither object property nor datatype property: "
				    + p.getURI());
		}
		if (tmpSetup != null) {
		    propSetup.put(p.getURI(), tmpSetup);
		    tmpSetup.setProperty(Resource.PROP_RDF_TYPE,
			    p.getProperty(Resource.PROP_RDF_TYPE));
		}
	    } else {
		LogUtils.logWarn(SharedResources.moduleContext,
			GenericOntology.class, "create",
			"Found property without domain: " + p.getURI());
	    }
	}

	// register all subclass info
	for (Entry<Resource, List<Resource>> entry : defSubClass.entrySet()) {
	    r = entry.getKey();
	    List lst = entry.getValue();
	    OntClassInfoSetup setup = ontClassSetup.get(r.getURI());
	    if (setup == null) {
		LogUtils.logWarn(SharedResources.moduleContext,
			GenericOntology.class, "create",
			"No setup available for class " + r.getURI()
				+ " to setup super classes.");
		continue;
	    }

	    Map<String, List<Resource>> onprops = new HashMap<String, List<Resource>>();
	    for (Object obj : lst) {
		if (!(obj instanceof Resource)) {
		    LogUtils.logWarn(
			    SharedResources.moduleContext,
			    GenericOntology.class,
			    "create",
			    "The class "
				    + r.getURI()
				    + " has a super class definition that is not a resource.");
		    continue;
		}

		r = (Resource) obj;

		boolean isRestriction = false;
		for (String s : r.getTypes())
		    if (PropertyRestriction.MY_URI.equals(s))
			isRestriction = true;
		if (isRestriction) {
		    // a propertyrestriction
		    // store for later use (to create a MergedRestriction)
		    String onprop = ((Resource) (r
			    .getProperty(PropertyRestriction.PROP_OWL_ON_PROPERTY)))
			    .getURI();
		    List<Resource> restrictions = onprops.get(onprop);
		    if (restrictions == null) {
			restrictions = new ArrayList<Resource>();
			onprops.put(onprop, restrictions);
		    }
		    restrictions.add(r);
		} else {
		    // a single named type
		    setup.addSuperClass(r.getURI());
		}
	    }

	    // specialize the resources, create MergedRestrictions and set as
	    // super class
	    for (Entry<String, List<Resource>> propEntry : onprops.entrySet()) {
		String onprop = propEntry.getKey();
		// lst is the (unspecialized) list of property restrictions for
		// the property onprop
		List<Resource> restrictions = propEntry.getValue();

		MergedRestriction mr = new MergedRestriction(onprop);
		for (Resource restriction : restrictions) {
		    // specialize the property restriction
		    Resource spec = new Specializer().specialize(restriction);
		    if (!(spec instanceof PropertyRestriction)) {
			LogUtils.logWarn(
				SharedResources.moduleContext,
				GenericOntology.class,
				"create",
				"Found a super class definition that is not a named individual and not a property restriction.");
			continue;
		    }
		    mr.addRestriction((PropertyRestriction) spec);
		}
		setup.addRestriction(mr);
	    }
	}

	// register all individuals
	// iterate again to find resources that have an rdf:type that is a
	// registered class URI
	Iterator<Resource> rit = GraphIterator.getResourceIterator(root);
	Map<String, ManagedIndividual> individuals = new HashMap<String, ManagedIndividual>();
	while (rit.hasNext()) {
	    r = rit.next();
	    if (r.isAnon())
		continue;
	    // the list of all types (typically only one)
	    List<String> lst = new ArrayList<String>();
	    for (String s : r.getTypes()) {
		if (Resource.isAnon(s))
		    continue;
		if (OntologyManagement.getInstance().isRegisteredClass(s, true)) {
		    // System.out.println("Found instance: " + r.getURI());
		    lst.add(s);
		}
	    }
	    for (String s : lst) {
		OntClassInfoSetup setup = ontClassSetup.get(s);
		if (setup != null) {

		    // if the 'plain' set of resources are just resources (which
		    // we get from deserialization), then we need to specialize
		    // the individual, i.e. create a subclass of
		    // ManagedIndividual
		    GenericManagedIndividual m = new GenericManagedIndividual(
			    r.getType(), r.getURI());
		    // now copy all properties
		    Enumeration enPropURIs = r.getPropertyURIs();
		    while (enPropURIs.hasMoreElements()) {
			String propURI = (String) enPropURIs.nextElement();
			m.setProperty(propURI, r.getProperty(propURI));
		    }

		    setup.addInstance(m);
		    individuals.put(m.getURI(), m);
		}
	    }
	}

	// enumerations
	for (Resource en : defEnum) {
	    o = en.getProperty(org.universAAL.middleware.owl.Enumeration.PROP_OWL_ONE_OF);
	    if (o instanceof List) {
		List lst = (List) o;
		ClosedCollection col = new ClosedCollection();
		for (Object el : lst) {
		    if (!(el instanceof Resource))
			continue;
		    ManagedIndividual m = individuals.get(((Resource) el)
			    .getURI());
		    col.add(m);
		}

		// get the setup for en and set the enumeration (=col);
		OntClassInfoSetup setup = ontClassSetup.get(en.getURI());
		if (setup != null) {
		    ManagedIndividual[] arr = (ManagedIndividual[]) col
			    .toArray(new ManagedIndividual[] {});
		    setup.toEnumeration(arr);
		}
	    }
	}
    }
}
