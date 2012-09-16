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
import org.universAAL.middleware.container.utils.StringUtils;

/**
 * <p>
 * The implementation of an OWL ontology. Basically, an ontology is a model of a
 * part of the real world. From a programming point of view, the ontology is a
 * group of ontological classes where each ontology class represents a concept
 * of the real world. An ontology is typically accompanied by a
 * {@link ResourceFactory} to create new instances of the ontological classes.
 * </p>
 * 
 * <p>
 * For example, one might want to model multimedia devices and creates the
 * ontology <i>multimedia</i> with the classes <i>TV</i>, <i>LoudSpeaker</i>,
 * <i>VideoCamera</i> and <i>Microphone</i>. Each of these classes might have
 * some properties, e.g. <i>LoudSpeaker</i> could have a <i>volume</i> which is
 * given as an integer value (since integer is a datatype, it is defined as a
 * {@link DatatypeProperty}). Also, a <i>TV</i> typically itself has a
 * <i>LoudSpeaker</i>, so it could have a property where all values of that
 * property are instances of <i>LoudSpeaker</i> (since <i>LoudSpeaker</i> is a
 * class, the property would be an {@link ObjectProperty}).
 * </p>
 * 
 * <p>
 * Each ontology class corresponds to a Java class that must be a subclass of
 * {@link ManagedIndividual}. This class is then responsible to provide getter
 * and setter methods for its properties as a convenience for programmers. These
 * methods will internally realize an RDF structure to store the knowledge of
 * that class using the methods {@link Resource#getProperty(String)} and
 * {@link Resource#setProperty(String, Object)}.
 * </p>
 * 
 * <p>
 * Additionally, for every ontology class (which stores the information at the
 * instance level), there is an instance of {@link OntClassInfo} (which stores
 * the information at model level) and each {@link OntClassInfo} is accompanied
 * by an {@link OntClassInfoSetup} that provides the possibility to set the
 * model information (for security reasons, the setter methods are in
 * {@link OntClassInfoSetup} and the getter methods are in {@link OntClassInfo}
 * so that only the creator of an ontology can make changes to it).
 * </p>
 * 
 * <p>
 * In our example, the concept <i>LoudSpeaker</i> has the property <i>volume</i>
 * and this information is a model information which is stored in an
 * {@link OntClassInfo}, so the overwritten {@link #create()} method would
 * define:
 * </p>
 * <code>
 * OntClassInfoSetup oci = createNewOntClassInfo(URI_of_LoudSpeaker, factory, factoryIndex);<br>
 * oci.addDatatypeProperty(URI_of_Volume_Property).
 * </code>
 * <p>
 * For the convenience of programmers there exists a Java class
 * <code>LoudSpeaker</code> that corresponds to this class with the helper
 * methods <code>getVolume()</code> and <code>setVolume(int)</code>. An instance
 * of this class would then identify a specific instance of <i>LoudSpeaker</i>,
 * e.g. <i>LoudSpeaker_2</i> with the <i>volume</i> <i>100</i>.
 * </p>
 * 
 * <p>
 * To create an ontology in universAAL, a subclass of this class has to be
 * defined and the method {@link #create()} has to be overwritten. The
 * {@link #create()} method will set up all characteristics of the ontology.
 * Each {@link Ontology} has to be registered at the {@link OntologyManagement}
 * by calling {@link OntologyManagement#register(Ontology)} before it can be
 * used.
 * </p>
 * 
 * <p>
 * In our example, the classes <i>TV</i>, <i>LoudSpeaker</i>, <i>VideoCamera</i>
 * and <i>Microphone</i> are created with one of the
 * <code>createNewXXClassInfo</code> methods and then all characteristics of
 * these classes (like properties and restrictions) are added.
 * </p>
 * 
 * @author Carsten Stockloew
 * @see OntClassInfo
 * @see RDFClassInfo
 * @see OntClassInfoSetup
 * @see RDFClassInfoSetup
 * @see ResourceFactory
 */
public abstract class Ontology {

    /** URI of the ontology. */
    public static final String TYPE_OWL_ONTOLOGY = ManagedIndividual.OWL_NAMESPACE
	    + "Ontology";

    /** URI of the property 'imports'. */
    public static final String PROP_OWL_IMPORT = ManagedIndividual.OWL_NAMESPACE
	    + "imports";

    /**
     * The list of imports of the ontology. A list of URIs of the ontologies
     * that are imported.
     * 
     * @see #addImport(String)
     */
    private volatile ArrayList imports = new ArrayList();

    /**
     * The set of RDF classes defined in this ontology. It maps the URI of the
     * class to an {@link RDFClassInfo}.
     */
    private volatile HashMap rdfClassInfoMap = new HashMap();

    /**
     * The set of OWL classes defined in this ontology. It maps the URI of the
     * class to an {@link OntClassInfo}.
     */
    private volatile HashMap ontClassInfoMap = new HashMap();

    /**
     * The set of OWL classes that are extended in this ontology. It maps the
     * URI of the class to an {@link OntClassInfo}.
     */
    private volatile HashMap extendedOntClassInfoMap = new HashMap();

    /**
     * General information about the ontology, like description, version,
     * author, and imports.
     */
    private Resource info;

    /**
     * Internal security check: when creating a {@link RDFClassInfo} or an
     * {@link OntClassInfo}, {@link #checkPermission(String)} is called and
     * tested against this value to determine whether the call really originated
     * from this class.
     */
    private String ontClassInfoURIPermissionCheck = null;

    /**
     * Thread synchronization for the internal security check.
     */
    private Object ontClassInfoURIPermissionCheckSync = new Object();

    /**
     * Determines whether this ontology is locked. If it is locked, no new
     * information can be stored here.
     */
    private boolean locked = false;

    private class MyInfo extends Resource {
	public MyInfo(String ontURI) {
	    super(ontURI);
	}

	public boolean isClosedCollection(String propURI) {
	    if (PROP_OWL_IMPORT.equals(propURI))
		return false;
	    return super.isClosedCollection(propURI);
	}
    }

    /**
     * Standard constructor to create a new ontology.
     * 
     * @param ontURI
     *            The ontology URI. If this is a namespace, i.e. the ontology
     *            URI including the hash sign, the hash sign is removed.
     */
    public Ontology(String ontURI) {
	if ((ontURI = getValidOntologyURI(ontURI)) == null)
	    throw new IllegalArgumentException("Not a valid Ontology URI:"
		    + ontURI);

	info = new MyInfo(ontURI);
	info.addType(TYPE_OWL_ONTOLOGY, true);
    }

    /**
     * Test whether the given String is a valid ontology URI. If the URI
     * includes a trailing hash sign, this hash sign is removed.
     * 
     * @param ontURI
     *            The ontology URI.
     * @return The ontology URI without trailing hash signs, or null if the
     *         given value is not a valid ontology URI.
     */
    private String getValidOntologyURI(String ontURI) {
	if (ontURI == null)
	    return null;
	// remove trailing hash signs
	while (ontURI.endsWith("#")) {
	    if (ontURI.length() < 2)
		return null;
	    ontURI = ontURI.substring(0, ontURI.length() - 1);
	}
	if (!StringUtils.startsWithURIScheme(ontURI))
	    return null;
	return ontURI;
    }

    /**
     * Add an import to this ontology. An import states the URI of another
     * ontology from which some concepts are used in this ontology.
     * 
     * @param ontURI
     *            The URI of the import ontology.
     * @return true, if the import could be added, or false, if the given
     *         ontology URI is not valid.
     */
    protected boolean addImport(String ontURI) {
	if ((ontURI = getValidOntologyURI(ontURI)) == null)
	    return false;
	synchronized (imports) {
	    Resource ont = new Resource(ontURI);
	    if (imports.contains(ont))
		return true;
	    ArrayList temp = new ArrayList(imports.size() + 1);
	    temp.addAll(imports);
	    temp.add(ont);
	    imports = temp;
	    info.setProperty(PROP_OWL_IMPORT, imports);
	}
	return true;
    }

    /**
     * Get the object that stores all general information about the ontology,
     * like description, version, author, and imports.
     */
    public Resource getInfo() {
	return info;
    }

    /**
     * Create this ontology. This method is called by
     * {@link OntologyManagement#register(Ontology)} and MUST be overwritten by
     * all subclasses.
     */
    public abstract void create();

    /** Internal method. */
    public final boolean checkPermission(String uri) {
	if (uri == null)
	    return false;
	return uri.equals(ontClassInfoURIPermissionCheck);
    }

    /**
     * Determines whether this ontology defines or extends the given OWL class.
     * 
     * @param classURI
     *            URI of the class.
     * @return true, if this ontology defines or extends the given OWL class.
     */
    public boolean hasOntClass(String classURI) {
	if (ontClassInfoMap.containsKey(classURI))
	    return true;
	return extendedOntClassInfoMap.containsKey(classURI);
    }

    /**
     * Get the class information of all OWL classes of this ontology.
     * 
     * @see #getRDFClassInfo()
     */
    public final OntClassInfo[] getOntClassInfo() {
	synchronized (ontClassInfoMap) {
	    return (OntClassInfo[]) ontClassInfoMap.values().toArray(
		    new OntClassInfo[0]);
	}
    }

    /**
     * Get the class information of all RDF classes of this ontology.
     * 
     * @see #getOntClassInfo()
     */
    public final RDFClassInfo[] getRDFClassInfo() {
	synchronized (rdfClassInfoMap) {
	    return (RDFClassInfo[]) rdfClassInfoMap.values().toArray(
		    new RDFClassInfo[0]);
	}
    }

    /**
     * Register a new RDF class.
     * 
     * @param classURI
     *            URI of the class.
     * @param fac
     *            The factory to create new instances.
     * @param factoryIndex
     *            Factory index to be given to the factory.
     * @return The setup interface.
     */
    protected RDFClassInfoSetup createNewRDFClassInfo(String classURI,
	    ResourceFactory fac, int factoryIndex) {
	if (locked)
	    return null;

	RDFClassInfoSetup setup = null;
	synchronized (ontClassInfoURIPermissionCheckSync) {
	    ontClassInfoURIPermissionCheck = classURI;
	    setup = (RDFClassInfoSetup) RDFClassInfo.create(classURI, this,
		    fac, factoryIndex);
	    ontClassInfoURIPermissionCheck = null;
	}
	RDFClassInfo info = setup.getInfo();

	HashMap temp = new HashMap();
	synchronized (rdfClassInfoMap) {
	    temp.putAll(rdfClassInfoMap);
	    temp.put(classURI, info);
	    rdfClassInfoMap = temp;
	}
	return setup;
    }

    /**
     * Register a new abstract OWL class. An OWL class is abstract iff the Java
     * counterpart is abstract. As such a class can not be instantiated, no
     * factory can be given.
     * 
     * @param classURI
     *            URI of the class.
     * @return The setup interface.
     * @see #extendExistingOntClassInfo(String)
     */
    protected OntClassInfoSetup createNewAbstractOntClassInfo(String classURI) {
	return createNewOntClassInfo(classURI, null, -1);
    }

    /**
     * Register a new OWL class. This method calls
     * {@link #createNewOntClassInfo(String, ResourceFactory, int)} with a dummy
     * factory index of -1.
     * 
     * @param classURI
     *            URI of the class.
     * @param fac
     *            The factory to create new instances.
     * @return The setup interface.
     * @see #extendExistingOntClassInfo(String)
     */
    protected OntClassInfoSetup createNewOntClassInfo(String classURI,
	    ResourceFactory fac) {
	return createNewOntClassInfo(classURI, fac, -1);
    }

    /**
     * Register a new OWL class.
     * 
     * @param classURI
     *            URI of the class.
     * @param fac
     *            The factory to create new instances.
     * @param factoryIndex
     *            Factory index to be given to the factory.
     * @return The setup interface.
     * @see #extendExistingOntClassInfo(String)
     */
    protected OntClassInfoSetup createNewOntClassInfo(String classURI,
	    ResourceFactory fac, int factoryIndex) {
	if (locked)
	    return null;
	OntClassInfoSetup setup = newOntClassInfo(classURI, fac, factoryIndex);
	RDFClassInfo info = setup.getInfo();

	HashMap temp = new HashMap();
	synchronized (ontClassInfoMap) {
	    temp.putAll(ontClassInfoMap);
	    temp.put(classURI, info);
	    ontClassInfoMap = temp;
	}
	return setup;
    }

    /**
     * <p>
     * Extend an existing OWL class. A class is normally defined only once and
     * only in one ontology. However, other ontologies can add some
     * characteristics to that class. This method should be called in that case:
     * when a different ontology has already defined a class and some
     * characteristics (like additional properties) have to be added by this
     * ontology.
     * </p>
     * <p>
     * The {@link OntologyManagement} will combine all information (from the
     * definition and all extenders) and will provide a combined view as if all
     * these characteristics were defined only once and only in one ontology.
     * </p>
     * 
     * @param classURI
     *            URI of the class to extend.
     * @return The setup interface.
     */
    protected OntClassInfoSetup extendExistingOntClassInfo(String classURI) {
	if (locked)
	    return null;
	OntClassInfoSetup setup = newOntClassInfo(classURI, null, 0);
	RDFClassInfo info = setup.getInfo();

	HashMap temp = new HashMap();
	synchronized (extendedOntClassInfoMap) {
	    temp.putAll(extendedOntClassInfoMap);
	    temp.put(classURI, info);
	    extendedOntClassInfoMap = temp;
	}
	return setup;
    }

    /**
     * Internal helper method to create a new OWL class.
     * 
     * @param classURI
     *            URI of the class.
     * @param fac
     *            Factory.
     * @param factoryIndex
     *            Factory index.
     * @return The setup interface to the new class.
     */
    private final OntClassInfoSetup newOntClassInfo(String classURI,
	    ResourceFactory fac, int factoryIndex) {
	if (locked)
	    return null;
	OntClassInfoSetup setup = null;
	synchronized (ontClassInfoURIPermissionCheckSync) {
	    ontClassInfoURIPermissionCheck = classURI;
	    setup = (OntClassInfoSetup) OntClassInfo.create(classURI, this,
		    fac, factoryIndex);
	    ontClassInfoURIPermissionCheck = null;
	}
	return setup;
    }

    /**
     * Get a list of all resources of this ontology. The resources are the info
     * part, the RDF and OWL classes as well as the properties of these classes.
     */
    public Resource[] getResourceList() {
	ArrayList list = new ArrayList();
	list.add(info);

	for (Iterator it = ontClassInfoMap.values().iterator(); it.hasNext();) {
	    OntClassInfo info = (OntClassInfo) it.next();
	    list.add(info);
	    Property[] propArr = info.getProperties();
	    if (propArr.length != 0)
		Collections.addAll(list, propArr);
	}

	list.addAll(rdfClassInfoMap.values());

	return (Resource[]) list.toArray(new Resource[list.size()]);
    }

    /**
     * Lock this instance and all its elements. After it is locked, no changes
     * can be made. The {@link Ontology} is automatically locked when it is
     * registered at the {@link OntologyManagement}.
     */
    public void lock() {
	// lock this ontology
	locked = true;

	// lock all elements
	synchronized (ontClassInfoMap) {
	    Iterator it = ontClassInfoMap.keySet().iterator();
	    while (it.hasNext())
		((OntClassInfo) ontClassInfoMap.get(it.next())).lock();
	}
	synchronized (extendedOntClassInfoMap) {
	    Iterator it = extendedOntClassInfoMap.keySet().iterator();
	    while (it.hasNext())
		((OntClassInfo) extendedOntClassInfoMap.get(it.next())).lock();
	}
	synchronized (rdfClassInfoMap) {
	    Iterator it = rdfClassInfoMap.keySet().iterator();
	    while (it.hasNext())
		((RDFClassInfo) rdfClassInfoMap.get(it.next())).lock();
	}

	// TODO: lock/immutable info
    }
}
