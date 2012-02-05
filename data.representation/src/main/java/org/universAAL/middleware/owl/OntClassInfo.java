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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.universAAL.middleware.rdf.Property;
import org.universAAL.middleware.rdf.RDFClassInfo;
import org.universAAL.middleware.rdf.RDFClassInfoSetup;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;

public final class OntClassInfo extends RDFClassInfo implements Cloneable {

    public static final String MY_URI = ManagedIndividual.OWL_NAMESPACE
	    + "Class";

    /**
     * repository of all properties mapped to a {@link MergedRestriction} that
     * represents all class restrictions on the property used as key.
     */
    // (getstandardproperty()?)
    // maps property property URIs to MergedRestriction
    private HashMap propRestriction = new HashMap();
    
    private HashMap properties = new HashMap();

    // Members of all the following arrays are instances of {@link
    // ClassExpression}.
    private ArrayList equivalentClasses = new ArrayList();
    private ArrayList disjointClasses = new ArrayList();
    private ClassExpression complementClass = null;
    private boolean isEnumeration = false; // weg??


    private PrivateOntSetup setup = null;
    
    private String propertyURIPermissionCheck;

    // list of OntClassInfo
    private ArrayList extenders = new ArrayList();

    private class PrivateOntSetup extends PrivateRDFSetup implements OntClassInfoSetup {
	OntClassInfo info;

	public PrivateOntSetup(OntClassInfo info) {
	    super(info);
	    this.info = info;
	}

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
				+ ")already exists. It can't be overwritten");
	    
	    // add to local variable
	    HashMap tmp = new HashMap(propRestriction);
	    tmp.put(r.getOnProperty(), r);
	    propRestriction = tmp;

	    // add to RDF graph: don't add the MergedRestriction directly, but
	    // the list of simple restrictions
	    ArrayList al = new ArrayList(combinedSuperClasses);
	    al.addAll(r.types);
	    combinedSuperClasses = al;
	    setProperty(ClassExpression.PROP_RDFS_SUB_CLASS_OF, Collections
			.unmodifiableList(combinedSuperClasses));
	}

	public void addInstance(ManagedIndividual instance)
		throws UnsupportedOperationException {
	    if (isEnumeration)
		throw new UnsupportedOperationException(
			"Not allowed to add new instances to an enumeration class (class: " + getURI() + ")!");

	    if (instance != null && uri.equals(instance.getClassURI()))
		super.addInstance(instance);
	}

	public void toEnumeration(ManagedIndividual[] individuals) {
	    if (locked)
		return;
	    for (int i = 0; i < individuals.length; i++)
		super.addInstance(individuals[i]);
	    isEnumeration = true;
	}

	public void addEquivalentClass(ClassExpression eq) {
	    if (locked)
		return;
	    // TODO
	}

	public void addDisjointClass(ClassExpression dj) {
	    // TODO Auto-generated method stub
	}

	public void setComplementClass(ClassExpression complement) {
	    // TODO Auto-generated method stub
	}
    }

    private OntClassInfo(String classURI, Ontology ont,
	    ResourceFactory factory, int factoryIndex) {
	super(classURI, ont, factory, factoryIndex);

	setup = new PrivateOntSetup(this);
	super.rdfsetup = setup;
	props.put(Resource.PROP_RDF_TYPE, ClassExpression.OWL_CLASS);
    }

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

    /** Internal method. */
    public final boolean checkPermission(String uri) {
	if (uri == null)
	    return false;
	return uri.equals(propertyURIPermissionCheck);
    }


    // getStandardPropertyURIs()...
    public String[] getDeclaredProperties() {
	return (String[]) propRestriction.keySet().toArray();
    }
    
    public Property[] getProperties() {
	return (Property[]) properties.values().toArray(new Property[0]);
    }

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

    public boolean isEnumeration() {
	return isEnumeration;
    }

    /** Internal method. */
    public void addExtender(OntClassInfo info) {
	if (!OntologyManagement.getInstance().checkPermission(info.getURI()))
	    throw new IllegalAccessError(
		    "The given class is not defined in the context of the given ontology.");

	// add the extender, 'this' is the combined version
	info.copyTo(this);
    }

    /** Internal method. */
    public void removeExtender(OntClassInfo info) {
	if (!OntologyManagement.getInstance().checkPermission(info.getURI()))
	    throw new IllegalAccessError(
		    "The given class is not defined in the context of the given ontology.");
	// TODO
    }

    /** Internal method. Copy all properties from this class to the given class. */
    private void copyTo(OntClassInfo info) {
	Iterator it;

	it = namedSuperClasses.iterator();
	while (it.hasNext())
	    info.setup.addSuperClass((String) it.next());

	it = propRestriction.keySet().iterator();
	while (it.hasNext())
	    info.setup.addRestriction((MergedRestriction) propRestriction.get(it.next()));
	
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
	    info.setup.addInstance((ManagedIndividual) instances.get(it.next()));
	
	it = superClasses.iterator();
	while (it.hasNext())
	    info.setup.addSuperClass((ClassExpression) it.next());

	it = equivalentClasses.iterator();
	while (it.hasNext())
	    info.setup.addEquivalentClass((ClassExpression) it.next());

	it = disjointClasses.iterator();
	while (it.hasNext())
	    info.setup.addDisjointClass((ClassExpression) it.next());

	info.setup.setComplementClass(complementClass);
	
	info.isEnumeration = info.isEnumeration || isEnumeration;

	if (info.factory == null)
	    info.factory = factory;
    }

    /** Internal method. */
    public Object clone() {
	// create a clone, the clone is not locked, but setup is only available
	// here, so that extenders can copy their properties to the clone
	// -> the clone is the combined version of multiple ontology-specific
	// OntClassInfos
	if (!OntologyManagement.getInstance().checkPermission(getURI()))
	    throw new IllegalAccessError(
		    "The given class is not defined in the context of the given ontology.");
	//try {
	    extenders.add(this);
	    
	    OntClassInfo cl = new OntClassInfo(getURI(), ont, factory, factoryIndex); //(OntClassInfo) super.clone();
	    //cl.setup = new PrivateOntSetup(cl);
	    //cl.rdfsetup = cl.setup;
	    //cl.isEnumeration = false;
	    copyTo(cl);
	    cl.extenders = extenders;
	    return cl;
/*	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError("Error while cloning OntClassInfo");
	}
*/    }
    
    
    public void setProperty(String propURI, Object value) {
	if (locked)
	    return;
	super.setProperty(propURI, value);
    }
}
