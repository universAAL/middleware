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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.rdf.RDFClassInfo;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;

/**
 * The Ontology Management mainly serves two purposes:
 * <ol>
 * <li>Management of ontologies</li>
 * <li>Providing a global view on all ontological information</li>
 * </ol>
 * <p>
 * The management of {@link Ontology ontologies} is achieved by providing
 * methods to {@link #register(Ontology) register} and
 * {@link #unregister(Ontology) unregister} an ontology as well as to query
 * information about ontologies, i.e. to get a list of URIs of all registered
 * ontologies with {@link #getOntoloyURIs()} and to get a specific ontology with
 * {@link #getOntology(String)}.
 * </p>
 * <p>
 * To add a new ontology, the method {@link Ontology#create()} has to be
 * overwritten and the ontology needs to be registered by calling
 * {@link #register(Ontology)}. The ontology is then added to the internal list
 * of <i>pending</i> ontologies. That means, that the information of the
 * ontology is available for some special methods (e.g.
 * {@link #isRegisteredClass(String, boolean)}) to provide the possibility to
 * create instances of the classes of that ontology. Then, the
 * <code>create()</code> method is called to actually create all this
 * information. If <code>create()</code> returns without errors, the ontology is
 * removed from the list of <i>pending</i> ontologies and is then available in
 * the system.
 * </p>
 * <p>
 * Ontological information can be distributed in different ontologies, e.g. one
 * ontology can define a class and a different ontology can extend this class by
 * adding some properties. The second purpose of the {@link OntologyManagement}
 * is to provide a combined view on all this information as if all was defined
 * in only one ontology.
 * </p>
 * <p>
 * {@link OntologyManagement} implements the <i>Singleton</i> design pattern. To
 * get an instance of this class and be able to call the methods, call
 * {@link #getInstance()}.
 * </p>
 * 
 * @author Carsten Stockloew
 */
public final class OntologyManagement {

    /**
     * Singleton instance.
     */
    private static OntologyManagement instance = new OntologyManagement();

    /**
     * The set of registered ontologies. It maps the URI of the ontology to an
     * instance of {@link Ontology}.
     */
    private volatile HashMap ontologies = new HashMap();

    /**
     * The set of OWL classes that are defined in the registered ontologies. It
     * maps the URI of the class to its {@link OntClassInfo}.
     * 
     * @see #rdfClassInfoMap
     */
    private volatile HashMap ontClassInfoMap = new HashMap();

    /**
     * The set of RDF classes that are defined in the registered ontologies. It
     * maps the URI of the class to its {@link RDFClassInfo}.
     * 
     * @see #ontClassInfoMap
     */
    private volatile HashMap rdfClassInfoMap = new HashMap();

    /**
     * Repository of sub class relationships. It maps the URI of the super class
     * to a list of URIs of all known sub classes.
     */
    private Hashtable namedSubClasses = new Hashtable();

    /**
     * The set of pending ontologies. When an ontology is registered, it's
     * status is set to <i>pending</i> and the method {@link Ontology#create()
     * create()} is called. If this method returns without error, the ontology
     * is registered and removed from the list of <i>pending</i> ontologies and,
     * thus, available in the system.
     */
    private volatile ArrayList pendingOntologies = new ArrayList();

    /**
     * Registration of named objects. When getting a Resource (e.g. by a
     * serializer), either the named Resource is retrieved according to the URI
     * of the specific instance, or a new object is created which is derived
     * from Resource according to the class URI. Register an object by calling
     * {@link OntClassInfoSetup#addInstance(ManagedIndividual)}. The registered
     * instance - given its instance URI - can then be retrieved by calling
     * {@link #getNamedResource(String)} with the URI of the instance.
     */
    // maps URI (of instance) to Resource
    private volatile HashMap namedResources = new HashMap();

    /**
     * Factory information to create new instances of registered classes.
     */
    private class FactoryEntry {
	public ResourceFactory factory;
	public int factoryIndex;

	FactoryEntry(ResourceFactory factory, int factoryIndex) {
	    this.factory = factory;
	    this.factoryIndex = factoryIndex;
	}
    }

    /**
     * Repository of all factories. It maps the URI of an {@link OntClassInfo}
     * to a {@link FactoryEntry}.
     */
    private volatile HashMap factories = new HashMap();

    /**
     * Internal security check:
     * {@link org.universAAL.middleware.owl.OntClassInfo#addExtender(OntClassInfo)}
     * will call this method to ensure that it is called from this class.
     */
    private String ontClassInfoURIPermissionCheck = null;

    /**
     * Private constructor for this Singleton. Use {@link #getInstance()} to get
     * the instance.
     */
    private OntologyManagement() {
    }

    /** Get the Singleton instance. */
    public static final OntologyManagement getInstance() {
	return instance;
    }

    /**
     * Remove an ontology from the list of <i>pending</i> ontologies.
     * 
     * @param ont
     *            The ontology to remove.
     */
    private void removePendingOntology(Ontology ont) {
	synchronized (pendingOntologies) {
	    ArrayList newPendingOntologies = new ArrayList(pendingOntologies
		    .size() - 1);
	    for (int i = 0; i < pendingOntologies.size(); i++)
		if (pendingOntologies.get(i) != ont)
		    newPendingOntologies.add(ont);
	    pendingOntologies = newPendingOntologies;
	}
    }

    /**
     * Register a new ontology.
     * 
     * @param ont
     *            the ontology.
     * @return false, if the Ontology is already available.
     */
    public boolean register(Ontology ont) {
	// add to pending
	synchronized (pendingOntologies) {
	    ArrayList newPendingOntologies = new ArrayList(pendingOntologies
		    .size() + 1);
	    newPendingOntologies.addAll(pendingOntologies);
	    newPendingOntologies.add(ont);
	    pendingOntologies = newPendingOntologies;
	}

	// create and lock the ontology
	ont.create();
	ont.lock();

	// add ontology to set of ontologies
	synchronized (ontologies) {
	    // don't add if already existing
	    if (ontologies.containsKey(ont.getInfo().getURI())) {
		removePendingOntology(ont);
		LogUtils
			.logError(
				SharedResources.moduleContext,
				OntologyManagement.class,
				"register",
				new Object[] { "The ontology ",
					ont.getInfo().getURI(),
					" is already registered; it can not be registered a second time." },
				null);
		return false;
	    }

	    // add new ontology
	    LogUtils.logDebug(SharedResources.moduleContext,
		    OntologyManagement.class, "register", new Object[] {
			    "Registering ontology: ", ont.getInfo().getURI() },
		    null);

	    // copy all existing ontologies to temp
	    OntClassInfo[] ontClassInfos = ont.getOntClassInfo();

	    HashMap tempOntologies = new HashMap(ontologies.size() + 1);
	    HashMap tempOntClassInfoMap = new HashMap(ontClassInfoMap.size()
		    + ontClassInfos.length);
	    HashMap tempNamedResources = new HashMap();
	    HashMap tempFactories = new HashMap();

	    tempOntologies.putAll(ontologies);
	    tempOntologies.put(ont.getInfo().getURI(), ont);
	    tempNamedResources.putAll(namedResources);
	    tempOntClassInfoMap.putAll(ontClassInfoMap);
	    tempFactories.putAll(factories);

	    // process ontology classes
	    if (ontClassInfos != null) {
		for (int i = 0; i < ontClassInfos.length; i++) {
		    OntClassInfo info = ontClassInfos[i];

		    // add ontology class
		    ontClassInfoURIPermissionCheck = info.getURI();
		    OntClassInfo combined = (OntClassInfo) ontClassInfoMap
			    .get(info.getURI());
		    if (combined == null) {
			// if it does not not exist, add simple cloned one
			tempOntClassInfoMap.put(info.getURI(), info.clone());
		    } else {
			// if it exists: add extender
			combined.addExtender(info);
		    }
		    ontClassInfoURIPermissionCheck = null;

		    // add named instances of this ontology class
		    Resource[] instances = info.getInstances();
		    for (int j = 0; j < instances.length; j++)
			tempNamedResources.put(instances[j].getURI(),
				instances[j]);

		    // add factories
		    if (info.getFactory() != null)
			tempFactories.put(info.getURI(), new FactoryEntry(info
				.getFactory(), info.getFactoryIndex()));

		    // process namedSuperClasses -> put in namedSubClasses
		    String namedSuperClasses[] = info.getNamedSuperClasses(
			    false, true);
		    for (int j = 0; j < namedSuperClasses.length; j++) {
			ArrayList namedSubClassesList = (ArrayList) namedSubClasses
				.get(namedSuperClasses[j]);

			if (namedSubClassesList == null)
			    namedSubClassesList = new ArrayList();

			if (!namedSubClassesList.contains(info.getURI()))
			    namedSubClassesList.add(info.getURI());

			namedSubClasses.put(namedSuperClasses[j],
				namedSubClassesList);
		    }
		}
	    }

	    // process rdf classes
	    RDFClassInfo[] rdfClassInfos = ont.getRDFClassInfo();
	    if (rdfClassInfos != null) {
		for (int i = 0; i < rdfClassInfos.length; i++) {
		    RDFClassInfo info = rdfClassInfos[i];

		    // add named instances of this ontology class
		    Resource[] instances = info.getInstances();
		    for (int j = 0; j < instances.length; j++)
			tempNamedResources.put(instances[j].getURI(),
				instances[j]);

		    // add factories
		    if (info.getFactory() != null)
			tempFactories.put(info.getURI(), new FactoryEntry(info
				.getFactory(), info.getFactoryIndex()));

		    // add rdf classes
		    if (!rdfClassInfoMap.containsKey(info.getURI()))
			rdfClassInfoMap.put(info.getURI(), info);
		}
	    }

	    // set temp as new set of ontologies
	    ontologies = tempOntologies;
	    ontClassInfoMap = tempOntClassInfoMap;
	    namedResources = tempNamedResources;
	    factories = tempFactories;
	}

	// remove from pending
	removePendingOntology(ont);

	return true;
    }

    /**
     * Get a named resource. A named resource is a registered instance of an OWL
     * or RDF class.
     * 
     * @param instanceURI
     *            URI of the instance.
     * @return The instance.
     */
    public Resource getNamedResource(String instanceURI) {
	if (instanceURI == null)
	    return null;
	return (Resource) namedResources.get(instanceURI);
    }

    /**
     * Get a Resource with the given class and instance URI.
     * 
     * @param classURI
     *            The URI of the class.
     * @param instanceURI
     *            The URI of the instance.
     * @return The Resource object with the given 'instanceURI', or a new
     *         Resource, if it does not exist.
     * @see #getNamedResource(String)
     */
    public Resource getResource(String classURI, String instanceURI) {
	if (classURI == null)
	    return null;

	Resource r = getNamedResource(instanceURI);
	if (r != null)
	    return r;

	FactoryEntry entry = (FactoryEntry) factories.get(classURI);
	if (entry == null) {
	    LogUtils.logDebug(SharedResources.moduleContext,
		    OntologyManagement.class, "getResource", new Object[] {
			    "No factory entry for ", classURI, "  ",
			    instanceURI }, null);
	    return null;
	}
	ResourceFactory fac = entry.factory;
	if (fac == null) {
	    // this should never happen!!
	    LogUtils.logError(SharedResources.moduleContext,
		    OntologyManagement.class, "getResource", new Object[] {
			    "No factory for ", classURI, "  ", instanceURI },
		    null);
	    return null;
	}

	try {
	    return (Resource) fac.createInstance(classURI, instanceURI,
		    entry.factoryIndex);
	} catch (Exception e) {
	    LogUtils.logError(SharedResources.moduleContext,
		    OntologyManagement.class, "getResource", new Object[] {
			    "The factory for ", classURI, "  ", instanceURI,
			    " stopped with an exception" }, e);
	    return null;
	}
    }

    /**
     * For a given set of URIs get the class that is most specialized, i.e. all
     * other classes are super classes of this class. The method can be used for
     * transformations to/from other representations, e.g. turtle, jena, and it
     * considers both, RDF classes and OWL classes.
     * 
     * @param classURIs
     *            The set of URIs of classes.
     * @return The URI of the most specialized class.
     */
    public final String getMostSpecializedClass(String[] classURIs) {
	if (classURIs == null)
	    return null;

	String result = null;
	RDFClassInfo info;
	for (int i = 0; i < classURIs.length; i++) {
	    if (result == null) {
		// get a registered class
		if (getRDFClassInfo(classURIs[i], true) != null)
		    result = classURIs[i];
	    } else {
		// test whether the new value is a more specialized class
		info = getRDFClassInfo(classURIs[i], true);
		if (info != null)
		    if (info.hasSuperClass(result, true))
			result = classURIs[i];
	    }
	}

	return result;
    }

    /**
     * Get the URIs of all sub classes of the given class.
     * 
     * @param superClassURI
     *            URI of the super class.
     * @param inherited
     *            false, iff only <i>direct</i> sub classes should be
     *            considered.
     * @param includeAbstractClasses
     *            true, iff abstract classes should be included.
     * @return The set of URIs of all sub classes.
     */
    public Set getNamedSubClasses(String superClassURI, boolean inherited,
	    boolean includeAbstractClasses) {

	HashSet retval = new HashSet();
	ArrayList namedSubClassesList = (ArrayList) namedSubClasses
		.get(superClassURI);

	if (namedSubClassesList == null)
	    namedSubClassesList = new ArrayList();

	if (includeAbstractClasses)
	    retval.addAll(namedSubClassesList);
	else {
	    // add only non-abstract sub classes
	    Iterator it = namedSubClassesList.iterator();
	    while (it.hasNext()) {
		String subClassURI = (String) it.next();
		OntClassInfo info = getOntClassInfo(subClassURI);
		if (info != null)
		    if (!info.isAbstract())
			retval.add(subClassURI);
	    }
	}

	if (inherited) {
	    // add child sub classes
	    Iterator it = namedSubClassesList.iterator();
	    while (it.hasNext()) {
		String subClassURI = (String) it.next();
		retval.addAll(getNamedSubClasses(subClassURI, inherited,
			includeAbstractClasses));
	    }
	}

	return retval;
    }

    /**
     * Unregister an ontology.
     * 
     * @param ont
     *            The ontology to unregister.
     */
    public void unregister(Ontology ont) {
	// TODO
    }

    /** Get an ontology by its URI. */
    public Ontology getOntology(String uri) {
	return (Ontology) ontologies.get(uri);
    }

    /**
     * Get the model information of an RDF class.
     * 
     * @param classURI
     *            URI of the class.
     * @param includeOntClasses
     *            true, if OWL classes should be included.
     * @return The model information.
     * @see #getOntClassInfo(String)
     */
    public RDFClassInfo getRDFClassInfo(String classURI,
	    boolean includeOntClasses) {
	if (classURI == null)
	    return null;

	RDFClassInfo info = (RDFClassInfo) rdfClassInfoMap.get(classURI);
	if (info != null)
	    return info;

	if (includeOntClasses)
	    return (OntClassInfo) ontClassInfoMap.get(classURI);
	return null;
    }

    /**
     * Get the model information of an OWL class.
     * 
     * @param classURI
     *            URI of the class.
     * @return The model information.
     * @see #getRDFClassInfo(String, boolean)
     */
    public OntClassInfo getOntClassInfo(String classURI) {
	if (classURI == null)
	    return null;
	return (OntClassInfo) ontClassInfoMap.get(classURI);
    }

    /**
     * Determines whether an OWL class is registered. It is registered if it is
     * defined or extended in one of the registered ontologies.
     * 
     * @param classURI
     *            URI of the class.
     * @param includePending
     *            If true, classes from <i>pending</i> ontologies are also
     *            considered.
     * @return true, iff the class is registered.
     */
    public boolean isRegisteredClass(String classURI, boolean includePending) {
	// test registered classes
	if (ontClassInfoMap.containsKey(classURI))
	    return true;

	if (includePending) {
	    // test pending classes
	    ArrayList pend = pendingOntologies;
	    for (int i = 0; i < pend.size(); i++)
		if (((Ontology) pend.get(i)).hasOntClass(classURI))
		    return true;
	}

	// last test: test registered classes (threading-problem: this happens,
	// when a pending ontology gets registered while checking the list of
	// pending ontologies)
	if (ontClassInfoMap.containsKey(classURI))
	    return true;

	return false;
    }

    /**
     * Get the set of URIs of all registered ontologies.
     */
    public String[] getOntoloyURIs() {
	return (String[]) ontologies.keySet().toArray(new String[0]);
    }

    /** Internal method. */
    public final boolean checkPermission(String uri) {
	if (uri == null)
	    return false;
	return uri.equals(ontClassInfoURIPermissionCheck);
    }
}
