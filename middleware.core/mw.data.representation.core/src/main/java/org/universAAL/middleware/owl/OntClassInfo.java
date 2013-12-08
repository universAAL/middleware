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
package org.universAAL.middleware.owl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.rdf.Property;
import org.universAAL.middleware.rdf.RDFClassInfo;
import org.universAAL.middleware.rdf.RDFClassInfoSetup;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;

/**
 * Definition of an OWL class. An instance of this class stores all model
 * information, like properties and restrictions. The creation is separated from
 * the usage; for every OntClassInfo there is exactly one
 * {@link OntClassInfoSetup} where all the characteristics of this class are
 * defined.
 * 
 * To create a new {@link OntClassInfo}, define a subclass of {@link Ontology}
 * and overwrite the {@link Ontology#create()} method.
 * 
 * @author Carsten Stockloew
 * @see RDFClassInfoSetup
 * @see RDFClassInfo
 * @see OntClassInfoSetup
 */
public final class OntClassInfo extends RDFClassInfo {

    /** URI of this class. */
    public static final String MY_URI = ManagedIndividual.OWL_NAMESPACE
	    + "Class";

    /**
     * Repository of the restrictions for all properties. If a
     * {@link MergedRestriction} is defined for a property then an entry is
     * stored here that maps the URI of the property to its restriction. If a
     * property has no restrictions, there is no entry in this repository.
     * 
     * @see #properties
     */
    // maps property URI to MergedRestriction
    private HashMap<String, MergedRestriction> propRestriction = new HashMap<String, MergedRestriction>();

    /**
     * Repository of all properties defined for this class. It maps the URI of
     * the property to a {@link Property} (which is either an
     * {@link ObjectProperty} or a {@link DatatypeProperty}).
     */
    private HashMap<String, Property> properties = new HashMap<String, Property>();

    /**
     * The set of all equivalent classes.
     * 
     * @see OntClassInfoSetup#addEquivalentClass(TypeExpression)
     */
    // Members of this arrays are instances of {@link TypeExpression}.
    private ArrayList<TypeExpression> equivalentClasses = new ArrayList<TypeExpression>();

    /**
     * The set of all disjoint classes.
     * 
     * @see OntClassInfoSetup#addDisjointClass(TypeExpression)
     */
    // Members of this arrays are instances of {@link TypeExpression}.
    private ArrayList<TypeExpression> disjointClasses = new ArrayList<TypeExpression>();

    /**
     * The complement class.
     * 
     * @see OntClassInfoSetup#setComplementClass(TypeExpression)
     */
    private TypeExpression complementClass = null;

    /**
     * Determines whether this class is an enumeration class.
     * 
     * @see OntClassInfoSetup#toEnumeration(ManagedIndividual[])
     */
    private boolean isEnumeration = false;

    /** The setup interface. */
    private PrivateOntSetup setup = null;

    /** The mgmt interface. */
    private ManagementOperation mgmt = null;

    /**
     * Internal security check: when creating a {@link Property},
     * {@link #checkPermission(String)} is called and tested against this value
     * to determine whether the call really originated from this class.
     */
    private String propertyURIPermissionCheck;

    /**
     * The set of extenders of this class. A class with a URI is normally
     * defined in one ontology. But it can be enhanced with additional
     * characteristics in other ontologies. This is a set of all classes that
     * contribute to the final combined class.
     * 
     * @see Ontology#extendExistingOntClassInfo(String)
     */
    private ArrayList<OntClassInfo> extenders = new ArrayList<OntClassInfo>();

    /**
     * Implementation of the setup interface. For security reasons, this is
     * realized as a protected nested class so that only the creator of an
     * {@link Ontology} has access to it and can make changes.
     */
    private class PrivateOntSetup extends PrivateRDFSetup implements
	    OntClassInfoSetup {

	/** The info object. */
	private OntClassInfo info;

	/** Constructor. */
	public PrivateOntSetup(OntClassInfo info) {
	    super(info);
	    this.info = info;
	}

	/** @see OntClassInfoSetup#addDatatypeProperty(String) */
	public DatatypePropertySetup addDatatypeProperty(String propURI) {
	    if (locked)
		return null;
	    propertyURIPermissionCheck = propURI;
	    DatatypePropertySetup prop = DatatypeProperty.create(propURI, info);
	    propertyURIPermissionCheck = null;
	    prop.setDomain(new TypeURI(getURI(), false));
	    properties.put(propURI, prop.getProperty());
	    return prop;
	}

	/** @see OntClassInfoSetup#addObjectProperty(String) */
	public ObjectPropertySetup addObjectProperty(String propURI) {
	    if (locked)
		return null;
	    propertyURIPermissionCheck = propURI;
	    ObjectPropertySetup prop = ObjectProperty.create(propURI, info);
	    propertyURIPermissionCheck = null;
	    prop.setDomain(new TypeURI(getURI(), false));
	    properties.put(propURI, prop.getProperty());
	    return prop;
	}

	/** @see OntClassInfoSetup#addRestriction(MergedRestriction) */
	public void addRestriction(MergedRestriction r) {
	    if (locked)
		return;
	    if (r == null)
		throw new NullPointerException(
			"The restriction must be not null.");

	    r = (MergedRestriction) r.copy();

	    if (propRestriction.containsKey(r.getOnProperty()))
		// a restriction for this property already exists
		throw new IllegalAccessError(
			"A restriction for this property (" + r.getOnProperty()
				+ ") already exists. It can't be overwritten");

	    // add to local variable
	    HashMap tmp = new HashMap(propRestriction);
	    tmp.put(r.getOnProperty(), r);
	    propRestriction = tmp;

	    // add to RDF graph: don't add the MergedRestriction directly, but
	    // the list of simple restrictions
	    ArrayList al = new ArrayList(combinedSuperClasses);
	    al.addAll(r.types);
	    combinedSuperClasses = al;
	    setProperty(TypeExpression.PROP_RDFS_SUB_CLASS_OF,
		    Collections.unmodifiableList(combinedSuperClasses));
	}

	/** @see OntClassInfoSetup#addInstance(ManagedIndividual) */
	public void addInstance(ManagedIndividual instance)
		throws UnsupportedOperationException {
	    if (isEnumeration)
		throw new UnsupportedOperationException(
			"Not allowed to add new instances to an enumeration class (class: "
				+ getURI() + ")!");

	    if (instance != null && uri.equals(instance.getClassURI())) {
		super.addInstance(instance);
	    } else {
		if (instance == null) {
		    LogUtils.logDebug(
			    SharedResources.moduleContext,
			    OntClassInfo.class,
			    "addInstance",
			    new Object[] {
				    "An instance of the ontology class ", uri,
				    " should be added but the given instance is null." },
			    null);
		} else {
		    LogUtils.logDebug(
			    SharedResources.moduleContext,
			    OntClassInfo.class,
			    "addInstance",
			    new Object[] {
				    "The instance ",
				    instance.getURI(),
				    " of the ontology class ",
				    uri,
				    " should be added but the class URI of the given instance does not match the URI of the class it should be added to. The instance is not added." },
			    null);
		}
	    }
	}

	/** @see OntClassInfoSetup#toEnumeration(ManagedIndividual[]) */
	public void toEnumeration(ManagedIndividual[] individuals) {
	    if (locked)
		return;
	    for (int i = 0; i < individuals.length; i++)
		super.addInstance(individuals[i]);
	    isEnumeration = true;
	}

	/** @see OntClassInfoSetup#addEquivalentClass(TypeExpression) */
	public void addEquivalentClass(TypeExpression eq) {
	    if (locked)
		return;
	    // TODO
	}

	/** @see OntClassInfoSetup#addDisjointClass(TypeExpression) */
	public void addDisjointClass(TypeExpression dj) {
	    // TODO Auto-generated method stub
	}

	/** @see OntClassInfoSetup#setComplementClass(TypeExpression) */
	public void setComplementClass(TypeExpression complement) {
	    // TODO Auto-generated method stub
	}
    }

    public class ManagementOperation {
	OntClassInfo info = null;

	private ManagementOperation(OntClassInfo info) {
	    this.info = info;
	}

	/**
	 * Add a new extender. This OntClassInfo object should be the combined
	 * version of all extenders.
	 */
	public void addExtender(OntClassInfo extenderInfo) {
	    extenderInfo.mgmt.copyTo(info);
	}

	/**
	 * Remove an existing extender. This OntClassInfo object should be the
	 * combined version of all extenders.
	 */
	public void removeExtender(OntClassInfo extenderInfo) {
	    extenders.remove(extenderInfo);

	    // simple processing: just create a new combined version by adding
	    // all extenders
	    OntClassInfo newCombinedInfo = new OntClassInfo(getURI(), ont,
		    factory, factoryIndex);
	    for (OntClassInfo tmp : extenders)
		newCombinedInfo.mgmt.addExtender(tmp);

	    // now copy all data structures to this class
	    namedSuperClasses = newCombinedInfo.namedSuperClasses;
	    propRestriction = newCombinedInfo.propRestriction;
	    properties = newCombinedInfo.properties;
	    instances = newCombinedInfo.instances;
	    superClasses = newCombinedInfo.superClasses;
	    equivalentClasses = newCombinedInfo.equivalentClasses;
	    disjointClasses = newCombinedInfo.disjointClasses;
	    factory = newCombinedInfo.factory;
	    isEnumeration = newCombinedInfo.isEnumeration;
	    complementClass = newCombinedInfo.complementClass;
	}

	public int getNumberOfExtenders() {
	    return extenders.size();
	}

	/** Internal method. */
	public OntClassInfo getClone() {
	    // create a clone, the clone is not locked, but setup is only
	    // available here, so that extenders can copy their properties to
	    // the clone
	    // -> the clone is the combined version of multiple
	    // ontology-specific OntClassInfos

	    extenders.add(info);

	    OntClassInfo cl = new OntClassInfo(getURI(), ont, factory,
		    factoryIndex); // (OntClassInfo)
	    // super.clone();
	    // cl.setup = new PrivateOntSetup(cl);
	    // cl.rdfsetup = cl.setup;
	    // cl.isEnumeration = false;
	    copyTo(cl);
	    cl.extenders = extenders;
	    return cl;
	}

	/**
	 * Internal method. Copy all properties from this class to the given
	 * class.
	 */
	private void copyTo(OntClassInfo info) {
	    Iterator it;

	    it = namedSuperClasses.iterator();
	    while (it.hasNext())
		info.setup.addSuperClass((String) it.next());

	    it = propRestriction.keySet().iterator();
	    while (it.hasNext())
		info.setup.addRestriction((MergedRestriction) propRestriction
			.get(it.next()));

	    it = properties.keySet().iterator();
	    while (it.hasNext()) {
		Property p = (Property) properties.get(it.next());
		if (p instanceof DatatypeProperty)
		    info.setup.addDatatypeProperty(p.getURI());
		else
		    info.setup.addObjectProperty(p.getURI());
	    }

	    it = instances.keySet().iterator();
	    while (it.hasNext())
		info.setup.addInstance((ManagedIndividual) instances.get(it
			.next()));

	    it = superClasses.iterator();
	    while (it.hasNext())
		info.setup.addSuperClass((TypeExpression) it.next());

	    it = equivalentClasses.iterator();
	    while (it.hasNext())
		info.setup.addEquivalentClass((TypeExpression) it.next());

	    it = disjointClasses.iterator();
	    while (it.hasNext())
		info.setup.addDisjointClass((TypeExpression) it.next());

	    info.setup.setComplementClass(complementClass);

	    info.isEnumeration = info.isEnumeration || isEnumeration;

	    if (info.factory == null)
		info.factory = factory;
	}
    }

    /**
     * Create a new OWL Class.
     * 
     * @param classURI
     *            The URI of the class.
     * @param ont
     *            The {@link Ontology} that creates this instance.
     * @param factory
     *            A factory to create new instances; it is <i>null</i> iff the
     *            class is abstract.
     * @param factoryIndex
     *            An index to be given to the <code>factory</code>. If the
     *            <code>factory</code> is <i>null</i>, this parameter is
     *            ignored.
     */
    private OntClassInfo(String classURI, Ontology ont,
	    ResourceFactory factory, int factoryIndex) {
	super(classURI, ont, factory, factoryIndex);

	setup = new PrivateOntSetup(this);
	super.rdfsetup = setup;
	props.put(Resource.PROP_RDF_TYPE,
		new Resource(TypeExpression.OWL_CLASS));
	mgmt = new ManagementOperation(this);
    }

    /**
     * Create a new OWL Class. This method can only be called from an
     * {@link Ontology}.
     * 
     * @param classURI
     *            The URI of the class.
     * @param ont
     *            The {@link Ontology} that creates this instance.
     * @param factory
     *            A factory to create new instances; it is <i>null</i> iff the
     *            class is abstract.
     * @param factoryIndex
     *            An index to be given to the <code>factory</code>. If the
     *            <code>factory</code> is <i>null</i>, this parameter is
     *            ignored.
     * @return The setup interface to set all information of this class.
     */
    public static RDFClassInfoSetup create(String classURI, Ontology ont,
	    ResourceFactory factory, int factoryIndex) {
	if (ont == null)
	    throw new NullPointerException("The ontology must be not null.");
	if (!ont.checkPermission(classURI))
	    throw new IllegalAccessError(
		    "The given class URI is not defined in the context of the given ontology.");
	OntClassInfo info = new OntClassInfo(classURI, ont, factory,
		factoryIndex);
	return info.setup;
    }

    /** Internal method for management. */
    public boolean checkPermission(String uri) {
	if (uri == null)
	    return false;
	return uri.equals(propertyURIPermissionCheck);
    }

    /** Internal method for management. */
    public ManagementOperation getManagementOperation(String permissionCheck) {
	if (!OntologyManagement.getInstance().checkPermission(permissionCheck))
	    throw new IllegalAccessError(
		    "The management operations can only be called from OntologyManagement!");
	return new ManagementOperation(this);
    }

    /**
     * Get the set of URIs of all properties for this class. To add new
     * properties, call {@link OntClassInfoSetup#addDatatypeProperty(String)} or
     * {@link OntClassInfoSetup#addObjectProperty(String)}.
     */
    public String[] getDeclaredPropertyURIs() {
	Set set = properties.keySet();
	String[] p = new String[set.size()];
	return (String[]) set.toArray(p);
    }

    /**
     * Get all properties of this class. To add new properties, call
     * {@link OntClassInfoSetup#addDatatypeProperty(String)} or
     * {@link OntClassInfoSetup#addObjectProperty(String)}.
     */
    public Property[] getDeclaredProperties() {
	return (Property[]) properties.values().toArray(new Property[0]);
    }

    /**
     * Get a specific property of this class. To add new properties, call
     * {@link OntClassInfoSetup#addDatatypeProperty(String)} or
     * {@link OntClassInfoSetup#addObjectProperty(String)}.
     */
    public Property getDeclaredProperty(String propURI) {
	return (Property) properties.get(propURI);
    }

    /**
     * Get the restriction that are defined for a property.
     * 
     * @param propURI
     *            URI of the property for which the restrictions apply.
     * @return The restrictions of the given property.
     */
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

    /** Determines whether this class is an enumeration class. */
    public boolean isEnumeration() {
	return isEnumeration;
    }

    /** @see Resource#setProperty(String, Object) */
    public boolean setProperty(String propURI, Object value) {
	if (locked)
	    return false;
	return super.setProperty(propURI, value);
    }
}
