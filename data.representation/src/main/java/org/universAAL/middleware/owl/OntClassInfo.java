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
package org.universAAL.middleware.owl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.rdf.ResourceRegistry;

public final class OntClassInfo extends Resource implements Cloneable {

    public static final String MY_URI = ManagedIndividual.OWL_NAMESPACE
	    + "Class";

    /**
     * repository of all properties mapped to a {@link MergedRestriction} that
     * represents all class restrictions on the property used as key.
     */
    // (getstandardproperty()?)
    // maps property property URIs to MergedRestriction
    private Hashtable propRestriction = new Hashtable();

    // set of URIs
    private HashSet namedSuperClasses = new HashSet();

    /**
     * repository of all known (non-anonymous) instances.
     */
    private Hashtable instances = new Hashtable();

    // Members of all the following arrays are instances of {@link
    // ClassExpression}.
    private ArrayList superClasses = new ArrayList();
    private ArrayList equivalentClasses = new ArrayList();
    private ArrayList disjointClasses = new ArrayList();
    private ClassExpression complementClass = null;
    private boolean isEnumeration = false; // weg??
    private Object password = new Object();
    private ResourceFactory factory;
    private Ontology ont;

    private boolean locked = false;

    private PrivateSetup setup = null;

    // list of OntClassInfo
    private ArrayList extenders = new ArrayList();

    private class PrivateSetup implements OntClassInfoSetup {
	OntClassInfo info;

	public PrivateSetup(OntClassInfo info) {
	    this.info = info;
	}

	public DataTypeProperty addDatatypeProperty(String propURI,
		boolean isFunctional) {
	    if (locked)
		return null;
	    DataTypeProperty prop = ResourceRegistry.getInstance()
		    .createDataTypeProperty(password, propURI);
	    if (prop == null)
		return null;

	    try {
		prop.setFunctional(password, isFunctional);
	    } catch (Exception e) {
	    }
	    return prop;
	}

	public void addInstance(ManagedIndividual instance) {
	    // TODO Auto-generated method stub

	}

	public ObjectProperty addObjectProperty(String propURI,
		boolean isFunctional, boolean isInverseFunctional,
		boolean isSymmetric, boolean isTransitive) {
	    if (locked)
		return null;
	    ObjectProperty prop = ResourceRegistry.getInstance()
		    .createObjectProperty(password, propURI);
	    if (prop == null)
		return null;

	    try {
		prop.setFunctional(password, isFunctional);
		prop.setSymmetric(password, isSymmetric);
		prop.setTransitive(password, isTransitive);
		prop.setInverseFunctional(password, isInverseFunctional);
	    } catch (Exception e) {
	    }
	    return prop;
	}

	public void addRestriction(MergedRestriction r) {
	    if (locked)
		return;
	    if (r == null)
		throw new NullPointerException(
			"The restriction must be not null.");
	    propRestriction.put(r.getOnProperty(), r);
	}

	public void addSuperClass(ClassExpression superClass) {
	    if (locked)
		return;
	    if (superClass != null) {
		superClasses.add(superClass);
		// if (superClass instanceof Restriction) {
		// // TODO: will we have subclasses of Restriction? If yes,
		// how
		// to
		// // handle subclasses?
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
	    namedSuperClasses.add(namedSuperClass);
	}

	public void toEnumeration(ManagedIndividual[] individuals) {
	    if (locked)
		return;
	    for (int i = 0; i < individuals.length; i++)
		addInstance(individuals[i]);
	    isEnumeration = true;
	}

	public OntClassInfo getInfo() {
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

    private OntClassInfo(String classURI, Ontology ont,
	    ResourceFactory factory, int factoryIndex) {
	super(classURI);
	if (classURI == null || isAnonymousURI(classURI))
	    throw new NullPointerException(
		    "The class URI must be not null and not anonymous.");
	if (ont == null)
	    throw new NullPointerException("The ontology must be not null.");
	if (!ont.checkPermission(classURI))
	    throw new IllegalAccessError(
		    "The given class URI is not defined in the context of the given ontology.");

	// Resource.addResourceClass(classURI, clz);
	ResourceRegistry.getInstance().registerResourceFactory(classURI,
		factory, factoryIndex);
	this.factory = factory;
	this.ont = ont;
	setup = new PrivateSetup(this);
    }

    public static OntClassInfoSetup create(String classURI, Ontology ont,
	    ResourceFactory factory, int factoryIndex) {
	OntClassInfo info = new OntClassInfo(classURI, ont, factory,
		factoryIndex);
	return info.setup;
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

    public boolean isAbstract() {
	return factory == null;
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

    public void addInstance(ManagedIndividual instance)
	    throws UnsupportedOperationException {
	if (isEnumeration)
	    throw new UnsupportedOperationException(
		    "Not allowed to add new instances to an enumeration class!");
	// TODO: what about repeated insert of the "same" instance?
	if (instance != null && !instance.isAnon() && // instance.isDirectInstanceOf(instance.getURI()))
		// {
		uri.equals(instance.getClassURI())) {
	    instances.put(instance.getURI(), instance);
	    ResourceRegistry.getInstance().registerNamedResource(instance);
	    // Resource.addSpecialResource(instance);
	    // addToSuperClasses(instance); //TODO??
	}
    }

    // private void addToSuperClasses(ManagedIndividual instance)
    // throws UnsupportedOperationException {
    // // TODO: add recursively for elements in superClasses that are of type
    // // TypeURI
    // }

    // getStandardPropertyURIs()...
    public String[] getDeclaredProperties() {
	return (String[]) propRestriction.keySet().toArray();
    }

    // public MergedRestriction getRestrictionsOnProp(String propURI) {
    public MergedRestriction getRestrictionsOnProp(String propURI) {
	// check this class
	MergedRestriction r;
	r = (MergedRestriction) propRestriction.get(propURI);
	if (r != null)
	    return r;

	// check super classes
	Iterator it = namedSuperClasses.iterator();
	while (it.hasNext()) {
	    String superClassURI = (String) it.next();
	    OntClassInfo superInfo = OntologyManagement.getInstance()
		    .getOntClassInfo(superClassURI);
	    if (superInfo != null) {
		r = superInfo.getRestrictionsOnProp(propURI);
		if (r != null)
		    return r;
	    }
	}
	return null;
    }

    public ClassExpression[] getSuperClasses() {
	return (ClassExpression[]) superClasses
		.toArray(new ClassExpression[superClasses.size()]);
    }

    public boolean isEnumeration() {
	return isEnumeration;
    }

    public ManagedIndividual[] getInstances() {
	return (ManagedIndividual[]) instances.values().toArray(
		new ManagedIndividual[instances.size()]);
    }

    public ManagedIndividual getInstanceByURI(String uri) {
	return (ManagedIndividual) instances.get(uri);
    }

    public void lock() {
	locked = true;
    }

    /** Internal method. */
    public void addExtender(OntClassInfo info) {
	if (!OntologyManagement.getInstance().checkPermission(info.getURI()))
	    throw new IllegalAccessError(
		    "The given class is not defined in the context of the given ontology.");

	// add the extender
	// 'this' is the combined version
	if (extenders.size() == 1) {
	    OntClassInfo extender = (OntClassInfo) extenders.get(0);

	    if (extender.propRestriction == propRestriction) {
		// this is the first extender
		// -> the combined version is the cloned version of this object
		// -> make the combined version a "real" clone
		// -> create new objects of all fields and copy everything from
		// info
		// and parent to 'this'

		propRestriction = new Hashtable();
		propRestriction.putAll(extender.propRestriction);

		namedSuperClasses = new HashSet();
		namedSuperClasses.addAll(extender.namedSuperClasses);

		instances = new Hashtable();
		instances.putAll(extender.instances);

		superClasses = new ArrayList();
		superClasses.addAll(extender.superClasses);

		equivalentClasses = new ArrayList();
		equivalentClasses.addAll(extender.equivalentClasses);

		disjointClasses = new ArrayList();
		disjointClasses.addAll(extender.disjointClasses);

		setup = new PrivateSetup(this);

		extenders.add(info);

		// staying the same: ont, locked, extenders
	    }
	}

	propRestriction.putAll(info.propRestriction);
	namedSuperClasses.addAll(info.namedSuperClasses);
	instances.putAll(info.instances);
	superClasses.addAll(info.superClasses);
	equivalentClasses.addAll(info.equivalentClasses);
	disjointClasses.addAll(info.disjointClasses);

	if (complementClass == null)
	    complementClass = info.complementClass;

	isEnumeration = info.isEnumeration || isEnumeration;

	if (factory == null)
	    factory = info.factory;
    }

    /** Internal method. */
    public void removeExtender(OntClassInfo info) {
	if (!OntologyManagement.getInstance().checkPermission(info.getURI()))
	    throw new IllegalAccessError(
		    "The given class is not defined in the context of the given ontology.");
    }

    /** Internal method. */
    public Object clone() {
	if (!OntologyManagement.getInstance().checkPermission(getURI()))
	    throw new IllegalAccessError(
		    "The given class is not defined in the context of the given ontology.");
	try {
	    extenders.add(this);
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError("Error while cloning OntClassInfo");
	}
    }
}
