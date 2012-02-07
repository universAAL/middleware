/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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
package org.universAAL.middleware.rdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.universAAL.middleware.owl.ClassExpression;
import org.universAAL.middleware.owl.OntClassInfo;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;

public class RDFClassInfo extends Resource {

    // set of URIs
    protected volatile HashSet namedSuperClasses = new HashSet();

    /**
     * repository of all known (non-anonymous) instances.
     */
    protected HashMap instances = new HashMap();

    // The combined list of all superclasses as set in the RDF graph (contains
    // named super classes and restrictions)
    protected volatile ArrayList combinedSuperClasses = new ArrayList();

    // Members of all the following arrays are instances of {@link
    // ClassExpression}.
    protected ArrayList superClasses = new ArrayList();

    protected ResourceFactory factory;
    protected int factoryIndex;
    protected Ontology ont;
    protected boolean locked = false;
    protected PrivateRDFSetup rdfsetup = null;

    protected class PrivateRDFSetup implements RDFClassInfoSetup {
	RDFClassInfo info;

	public PrivateRDFSetup(RDFClassInfo info) {
	    this.info = info;
	}

	public void addInstance(Resource instance) {
	    if (locked)
		return;

	    if (instance != null && !instance.isAnon())
		if (!instances.containsKey(instance.getURI()))
		    instances.put(instance.getURI(), instance);
	}

	public void addSuperClass(ClassExpression superClass) {
	    if (locked)
		return;
	    if (superClass != null) {
		superClasses.add(superClass);
		// if (superClass instanceof Restriction) {
		// // TODO: will we have subclasses of Restriction? If yes,
		// how to handle subclasses?
		// String propURI = ((Restriction)
		// superClass).getOnProperty();
		// MergedRestriction existing = (MergedRestriction)
		// propRestriction
		// .get(propURI);
		// if (existing == null) {
		// existing = new MergedRestriction((Restriction)
		// superClass);
		// propRestriction.put(propURI, existing);
		// } else
		// existing.addRestriction((Restriction) superClass);

		// currently, only one Restriction possible!
		// propRestriction.put(((Restriction) superClass)
		// .getOnProperty(), superClass);
		// }
		// TODO: should we inherit all properties from superclasses
	    }
	}

	public void addSuperClass(String namedSuperClass) {
	    if (locked)
		return;
	    if (namedSuperClass == null)
		return;

	    // add to local variable
	    HashSet tmp = new HashSet(namedSuperClasses);
	    tmp.add(namedSuperClass);
	    namedSuperClasses = tmp;

	    // add to RDF graph
	    ArrayList al = new ArrayList(combinedSuperClasses);
	    al.add(new Resource(namedSuperClass));
	    combinedSuperClasses = al;
	    setProperty(ClassExpression.PROP_RDFS_SUB_CLASS_OF, Collections
		    .unmodifiableList(combinedSuperClasses));
	}

	public RDFClassInfo getInfo() {
	    return info;
	}

	public void setResourceComment(String comment) {
	    if (locked)
		return;
	    info.setResourceComment(comment);
	}

	public void setResourceLabel(String label) {
	    if (locked)
		return;
	    info.setResourceLabel(label);
	}
    }

    protected RDFClassInfo(String classURI, Ontology ont,
	    ResourceFactory factory, int factoryIndex) {
	super(classURI);
	if (classURI == null || isAnonymousURI(classURI))
	    throw new NullPointerException(
		    "The class URI must be not null and not anonymous.");

	this.factory = factory;
	this.factoryIndex = factoryIndex;
	this.ont = ont;
	rdfsetup = new PrivateRDFSetup(this);
	addType(Resource.TYPE_RDFS_CLASS, true);
    }

    public static RDFClassInfoSetup create(String classURI, Ontology ont,
	    ResourceFactory factory, int factoryIndex) {
	if (ont == null)
	    throw new NullPointerException("The ontology must be not null.");
	if (!ont.checkPermission(classURI))
	    throw new IllegalAccessError(
		    "The given class URI is not defined in the context of the given ontology.");
	RDFClassInfo info = new RDFClassInfo(classURI, ont, factory,
		factoryIndex);
	return info.rdfsetup;
    }

    public boolean isAbstract() {
	return factory == null;
    }

    public ResourceFactory getFactory() {
	return factory;
    }

    public int getFactoryIndex() {
	return factoryIndex;
    }

    public boolean hasSuperClass(String classURI, boolean inherited) {
	if (namedSuperClasses.contains(classURI))
	    return true;
	if (!inherited)
	    return false;

	Iterator it = namedSuperClasses.iterator();
	while (it.hasNext()) {
	    String superClassURI = (String) it.next();
	    OntClassInfo superInfo = OntologyManagement.getInstance()
		    .getOntClassInfo(superClassURI);
	    if (superInfo != null)
		if (superInfo.hasSuperClass(superClassURI, inherited))
		    return true;
	}
	return false;
    }

    public String[] getNamedSuperClasses(boolean inherited,
	    boolean includeAbstractClasses) {

	ArrayList al = new ArrayList();

	if (includeAbstractClasses)
	    al.addAll(namedSuperClasses);
	else {
	    // add only non-abstract super classes
	    Iterator it = namedSuperClasses.iterator();
	    while (it.hasNext()) {
		String superClassURI = (String) it.next();
		OntClassInfo info = OntologyManagement.getInstance()
			.getOntClassInfo(superClassURI);
		if (info != null)
		    if (!info.isAbstract())
			al.add(superClassURI);
	    }
	}

	if (inherited) {
	    // add parent super classes
	    Iterator it = namedSuperClasses.iterator();
	    while (it.hasNext()) {
		String superClassURI = (String) it.next();
		OntClassInfo info = OntologyManagement.getInstance()
			.getOntClassInfo(superClassURI);
		if (info != null) {
		    String[] res = info.getNamedSuperClasses(inherited,
			    includeAbstractClasses);
		    for (int i = 0; i < res.length; i++)
			al.add(res[i]);
		}
	    }
	}

	return (String[]) al.toArray(new String[al.size()]);
    }

    public ClassExpression[] getSuperClasses() {
	return (ClassExpression[]) superClasses
		.toArray(new ClassExpression[superClasses.size()]);
    }

    public Resource[] getInstances() {
	return (Resource[]) instances.values().toArray(
		new Resource[instances.size()]);
    }

    public Resource getInstanceByURI(String uri) {
	return (Resource) instances.get(uri);
    }

    public void lock() {
	locked = true;
    }

    public boolean isClosedCollection(String propURI) {
	if (ClassExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI))
	    return false;
	return super.isClosedCollection(propURI);
    }
}
