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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.Property;
import org.universAAL.middleware.rdf.RDFClassInfo;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.util.OntologyListener;

/**
 * The Ontology Management mainly serves two purposes:
 * <ol>
 * <li>Management of ontologies</li>
 * <li>Providing a global view on all ontological information</li>
 * </ol>
 * <p>
 * The management of {@link Ontology ontologies} is achieved by providing
 * methods to
 * {@link #register(org.universAAL.middleware.container.ModuleContext, Ontology)
 * register} and
 * {@link #unregister(org.universAAL.middleware.container.ModuleContext, Ontology)
 * unregister} an ontology as well as to query information about ontologies,
 * i.e. to get a list of URIs of all registered ontologies with
 * {@link #getOntoloyURIs()} and to get a specific ontology with
 * {@link #getOntology(String)}.
 * </p>
 * <p>
 * To add a new ontology, the method {@link Ontology#create()} has to be
 * overwritten and the ontology needs to be registered by calling
 * {@link #register(org.universAAL.middleware.container.ModuleContext, Ontology)}
 * . The ontology is then added to the internal list of <i>pending</i>
 * ontologies. That means, that the information of the ontology is available for
 * some special methods (e.g. {@link #isRegisteredClass(String, boolean)}) to
 * provide the possibility to create instances of the classes of that ontology.
 * Then, the <code>create()</code> method is called to actually create all this
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

    // for debugging: store for each Java .class name the URI of the class
    // maps Java class name -> URI
    private HashMap<String, String> dbgClass = new HashMap<String, String>();

    /**
     * The list of ontology listeners. The listeners are notified when a change
     * occurs.
     */
    private ArrayList<OntologyListener> listeners = new ArrayList<OntologyListener>();

    /**
     * Factory information to create new instances of registered classes.
     */
    private class FactoryEntry {
	private ResourceFactory factory;
	private int factoryIndex;

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

    /**
     * Get the Singleton instance.
     * 
     * @return The Singleton instance.
     */
    public static OntologyManagement getInstance() {
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
	    ArrayList newPendingOntologies = new ArrayList(
		    pendingOntologies.size() - 1);
	    for (int i = 0; i < pendingOntologies.size(); i++) {
		Object o = pendingOntologies.get(i);
		if (o != ont)
		    newPendingOntologies.add(o);
	    }
	    pendingOntologies = newPendingOntologies;
	}
    }

    /**
     * Perform a sanity check of the given ontology. The check is done during
     * registration of the ontology.
     * 
     * @param ont
     *            The ontology that defined the class.
     */
    private boolean register_testOntology(Ontology ont) {
	try {
	    boolean retVal = true;

	    Resource[] rsc = ont.getResourceList();
	    Resource r;
	    // maps factoryIndex to RDFClassInfo
	    HashMap factories = new HashMap();

	    for (int i = 0; i < rsc.length; i++) {
		r = rsc[i];
		if (r instanceof RDFClassInfo) {
		    RDFClassInfo rci = (RDFClassInfo) r;
		    if (rci.isAbstract())
			continue;

		    Integer factoryIndex = Integer.valueOf(rci
			    .getFactoryIndex());

		    // test factory
		    Object o = factories.get(factoryIndex);
		    if (o != null) {
			LogUtils.logWarn(
				SharedResources.moduleContext,
				OntologyManagement.class,
				"register_testOntology",
				new Object[] {
					"Duplicate factory index: the classes ",
					rci.getURI(), " and ",
					((RDFClassInfo) o).getURI(),
					" of the ontology ",
					ont.getInfo().getURI(),
					" have the same factory index. Is this intended?" },
				null);
		    } else {
			factories.put(factoryIndex, rci);
		    }
		}
	    }

	    return retVal;
	} catch (Exception e) {
	    LogUtils.logError(
		    SharedResources.moduleContext,
		    OntologyManagement.class,
		    "register_testOntology",
		    new Object[] {
			    "An unknown exception occured while testing the ontology ",
			    ont.getInfo().getURI(), " during registration." },
		    e);
	}
	return false;
    }

    /**
     * Perform a sanity check of the given class that is defined in the given
     * ontology. The check is done after registration of the ontology.
     * 
     * @param ont
     *            The ontology that defined the class.
     * @param info
     *            the ontology class.
     */
    private boolean register_postTestClass(Ontology ont, OntClassInfo info) {
	ResourceFactory fact = info.getFactory();
	if (fact == null)
	    // must be an abstract class
	    return true;
	Resource testInstance = fact.createInstance(info.getURI(),
		Resource.uAAL_NAMESPACE_PREFIX + "testInstance",
		info.getFactoryIndex());
	ManagedIndividual m = (ManagedIndividual) testInstance;

	// test the super class
	String[] superClasses = info.getNamedSuperClasses(true, true);
	if (superClasses.length == 0) {
	    LogUtils.logError(
		    SharedResources.moduleContext,
		    OntologyManagement.class,
		    "register_postTestClass",
		    new Object[] { "No super class: the ontology class ",
			    info.getURI(), " of the ontology ",
			    ont.getInfo().getURI(),
			    " does not have a super class defined." }, null);
	    return false;
	}
	HashSet supcls = new HashSet();
	for (String tmp : superClasses)
	    supcls.add(tmp);
	// if (!supcls.contains(ManagedIndividual.MY_URI)) {
	// LogUtils.logDebug(
	// SharedResources.moduleContext,
	// OntologyManagement.class,
	// "register_testClass",
	// new Object[] {
	// "ManagedIndividual is not a super class: the ontology class ",
	// info.getURI(),
	// " of the ontology ",
	// ont.getInfo().getURI(),
	// " does not have ManagedIndividual defined as a super class. ManagedIndividual, as super class of ontology classes "
	// +
	// "should either be a direct super class or a super class of one of the super classes of this class."
	// },
	// null);
	// }
	// System.out.println("----------------- checking class: "
	// + m.getClass().getName());
	// for (Object o : superClasses) {
	// System.out.println("    -- registered super class: " + o);
	// }
	Class superClassJava = m.getClass().getSuperclass();
	do {
	    superClassJava = superClassJava.getSuperclass();
	} while (Modifier.isAbstract(superClassJava.getModifiers()));
	while (!(superClassJava.equals(Object.class))
		&& !(superClassJava.equals(ManagedIndividual.class))
		&& !(superClassJava.equals(FinalizedResource.class))
		&& !(superClassJava.equals(Resource.class))) {
	    // System.out.println("    -- super: " + superClassJava.getName());
	    String superClassURI = dbgClass.get(superClassJava.getName());

	    if (superClassURI == null) {
		LogUtils.logDebug(
			SharedResources.moduleContext,
			OntologyManagement.class,
			"register_postTestClass",
			new Object[] {
				"Unregistered super class: the ontology class ",
				info.getURI(),
				" of the ontology ",
				ont.getInfo().getURI(),
				" has a super class from Java inheritance (",
				superClassJava.getName(),
				") that is not registered as a class in Ontology Management (or the super class is a non-abstract class that is registered as an abstract ontology class)." },
			null);
	    } else {
		if (!(supcls.contains(superClassURI))) {
		    LogUtils.logDebug(
			    SharedResources.moduleContext,
			    OntologyManagement.class,
			    "register_postTestClass",
			    new Object[] {
				    "Undefined super class: the ontology class ",
				    info.getURI(),
				    " of the ontology ",
				    ont.getInfo().getURI(),
				    " has a super class from Java inheritance (",
				    superClassJava.getName(),
				    ") that is registered in Ontology Management with the URI '",
				    superClassURI,
				    "', but this class is not defined as a super class of this class (neither directly nor indirectly)." },
			    null);
		}
	    }
	    do {
		superClassJava = superClassJava.getSuperclass();
	    } while (Modifier.isAbstract(superClassJava.getModifiers()));
	}

	// test special case fixed for a service in service bus, so we need
	// to use concrete URIs here
	// Note, that it is not completely correct to do this here since DataRep
	// is a dependency of service bus and not the other way around,
	// therefore DataRep should be independent from the service bus.
	// However, this test may help to find some serious problems during
	// serialization!
	if (supcls
		.contains("http://www.daml.org/services/owl-s/1.1/Service.owl#Service")) {
	    // we have a subclass of Service
	    // test the serialization type for some of the properties

	    String[] propURIs = {
		    "http://www.daml.org/services/owl-s/1.1/Service.owl#presents",
		    "http://ontology.universAAL.org/uAAL.owl#instanceLevelRestrictions",
		    "http://ontology.universAAL.org/uAAL.owl#numberOfValueRestrictions" };
	    for (String propURI : propURIs) {
		if (m.getPropSerializationType(propURI) != Resource.PROP_SERIALIZATION_FULL) {
		    LogUtils.logWarn(
			    SharedResources.moduleContext,
			    OntologyManagement.class,
			    "register_postTestClass",
			    new Object[] {
				    "Wrong serialization type: the property ",
				    propURI,
				    " of the ontology class ",
				    info.getURI(),
				    " of the ontology ",
				    ont.getInfo().getURI(),
				    " does not return Resource.PROP_SERIALIZATION_FULL as serialization type for a property used by the service bus."
					    + " Please check the method getPropSerializationType(String propURI) of that class;"
					    + " this might cause an incomplete serialization result." },
			    null);
		}
	    }
	}

	return true;
    }

    /**
     * Perform a sanity check of the given class that is defined in the given
     * ontology. The check is done during registration of the ontology.
     * 
     * @param ont
     *            The ontology that defined the class.
     * @param info
     *            the ontology class.
     */
    private boolean register_testClass(Ontology ont, OntClassInfo info) {
	try {
	    if (!Resource.isQualifiedName(info.getURI())) {
		LogUtils.logError(
			SharedResources.moduleContext,
			OntologyManagement.class,
			"register_testClass",
			new Object[] {
				"Unqualified URI: the ontology class ",
				info.getURI(),
				" of the ontology ",
				ont.getInfo().getURI(),
				" does not have a qualified URI. Please check the URI you give as parameter to creator methods like createNewOntClassInfo()." },
			null);
		return false;
	    }

	    if (info.isAbstract())
		return true;

	    ResourceFactory fact = info.getFactory();
	    if (fact == null) {
		LogUtils.logError(
			SharedResources.moduleContext,
			OntologyManagement.class,
			"register_testClass",
			new Object[] {
				"Missing factory: the ontology class ",
				info.getURI(),
				" of the ontology ",
				ont.getInfo().getURI(),
				" is not an abstract class but it does not define a factory to create instances of this class." },
			null);
		return false;
	    }

	    Resource testInstance = fact.createInstance(info.getURI(),
		    Resource.uAAL_NAMESPACE_PREFIX + "testInstance",
		    info.getFactoryIndex());
	    if (testInstance == null) {
		LogUtils.logError(
			SharedResources.moduleContext,
			OntologyManagement.class,
			"register_testClass",
			new Object[] {
				"Missing factory result: the ontology class ",
				info.getURI(),
				" (factory index: " + info.getFactoryIndex()
					+ ") of the ontology ",
				ont.getInfo().getURI(),
				" is not an abstract class and it defines a factory, but the factory does not create instances for this class (the factory returned null)." },
			null);
		return false;
	    }

	    if (!(testInstance instanceof ManagedIndividual)) {
		LogUtils.logError(
			SharedResources.moduleContext,
			OntologyManagement.class,
			"register_testClass",
			new Object[] {
				"Factory returned non-ManagedIndividual: the ontology class ",
				info.getURI(),
				" of the ontology ",
				ont.getInfo().getURI(),
				" is registered as an ontology class (OWL), but the factory does not return a subclass of ManagedIndividual."
					+ " All OWL classes must be a subclass of ManagedIndividual." },
			null);
		return false;
	    }

	    ManagedIndividual m = (ManagedIndividual) testInstance;
	    if (!info.getURI().equals(m.getClassURI())) {
		LogUtils.logError(
			SharedResources.moduleContext,
			OntologyManagement.class,
			"register_testClass",
			new Object[] {
				"Wrong class URI: the ontology class ",
				info.getURI(),
				" of the ontology ",
				ont.getInfo().getURI(),
				" does not return the URI that was used for registration."
					+ " Please check that the method \"getClassURI()\" is overwritten and matches the URI you specify as parameter to creator methods like createNewOntClassInfo()."
					+ " The factory could also be the source of this error: please check that the factoryIndex is correct." },
			null);
		return false;
	    }

	    // we store the Java .class for later testing
	    dbgClass.put(m.getClass().getName(), m.getClassURI());

	    // test properties
	    String[] props = info.getDeclaredPropertyURIs();
	    for (int i = 0; i < props.length; i++) {
		String propURI = props[i];
		// test that the property has a serialization type
		int serType = m.getPropSerializationType(propURI);
		if (serType == Resource.PROP_SERIALIZATION_UNDEFINED)
		    LogUtils.logWarn(
			    SharedResources.moduleContext,
			    OntologyManagement.class,
			    "register_testClass",
			    new Object[] {
				    "Undefined serialization type: the property ",
				    propURI,
				    " of the ontology class ",
				    info.getURI(),
				    " of the ontology ",
				    ont.getInfo().getURI(),
				    " returns <undefined> as serialization type for a declared property."
					    + " Is this intended? If not,"
					    + " please check the method getPropSerializationType(String propURI);"
					    + " this might cause an incomplete serialization result." },
			    null);

		// test that the restrictions match the type (i.e. a
		// DatatypeProperty has a literal as value, not a Resource)
		MergedRestriction res = info.getRestrictionsOnProp(propURI);
		if (res == null)
		    continue;
		Property prop = info.getDeclaredProperty(propURI);
		boolean isDatatype = prop instanceof DatatypeProperty;
		Object constraint;
		constraint = res
			.getConstraint(MergedRestriction.allValuesFromID);
		if (constraint != null) {
		    if (constraint instanceof TypeURI) {
			TypeURI t = (TypeURI) constraint;
			String err = null;
			if (TypeMapper.isRegisteredDatatypeURI(t.getURI())) {
			    if (!isDatatype) {
				err = "ObjectProperty";
			    }
			} else {
			    if (isDatatype) {
				err = "DatatypeProperty";
			    }
			}
			if (err != null) {
			    LogUtils.logError(
				    SharedResources.moduleContext,
				    OntologyManagement.class,
				    "register_testClass",
				    new Object[] {
					    "Wrong property definition: the property ",
					    propURI,
					    " of the ontology class ",
					    info.getURI(),
					    " of the ontology ",
					    ont.getInfo().getURI(),
					    " is registered as a "
						    + err
						    + " but the defined restrictions indicate otherwise." },
				    null);
			}
		    }
		}
		// TODO: there is much more we can test!
	    }

	    return true;
	} catch (Exception e) {
	    LogUtils.logError(
		    SharedResources.moduleContext,
		    OntologyManagement.class,
		    "register_testClass",
		    new Object[] {
			    "An unknown exception occured while testing the ontology class ",
			    info.getURI(),
			    " during registration of the ontology ",
			    ont.getInfo().getURI(), "." }, e);
	}
	return false;
    }

    /**
     * Register a new ontology.
     * 
     * @param ont
     *            the ontology.
     * @return false, if the Ontology is already available.
     */
    public boolean register(ModuleContext mc, Ontology ont) {
	// add to pending
	synchronized (pendingOntologies) {
	    ArrayList newPendingOntologies = new ArrayList(
		    pendingOntologies.size() + 1);
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
		LogUtils.logError(
			SharedResources.moduleContext,
			OntologyManagement.class,
			"register",
			new Object[] { "The ontology ", ont.getInfo().getURI(),
				" is already registered; it can not be registered a second time." },
			null);
		return false;
	    }

	    // add new ontology
	    LogUtils.logDebug(
		    SharedResources.moduleContext,
		    OntologyManagement.class,
		    "register",
		    new Object[] {
			    "Registering ontology: ",
			    ont.getInfo().getURI(),
			    " (classes: ",
			    ont.getOntClassInfo().length
				    + ont.getRDFClassInfo().length, ")" }, null);

	    // make some sanity tests
	    register_testOntology(ont);

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

		    // make some sanity tests
		    register_testClass(ont, info);

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
			tempFactories.put(
				info.getURI(),
				new FactoryEntry(info.getFactory(), info
					.getFactoryIndex()));

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
			tempFactories.put(
				info.getURI(),
				new FactoryEntry(info.getFactory(), info
					.getFactoryIndex()));

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

	    // some last tests
	    if (ontClassInfos != null) {
		for (int i = 0; i < ontClassInfos.length; i++) {
		    OntClassInfo info = ontClassInfos[i];
		    OntClassInfo combined = (OntClassInfo) ontClassInfoMap
			    .get(info.getURI());
		    register_postTestClass(ont, combined);
		}
	    }
	}

	// remove from pending
	removePendingOntology(ont);

	synchronized (listeners) {
	    for (OntologyListener l : listeners)
		l.ontologyAdded(ont.getInfo().getURI());
	}

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
    public String getMostSpecializedClass(String[] classURIs) {
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
    public void unregister(ModuleContext mc, Ontology ont) {
	// TODO
	// synchronized (listeners) {
	// for (OntologyListener l : listeners)
	// l.ontologyRemoved(ont.getInfo().getURI());
	// }
    }

    /**
     * Get an ontology by its URI. The ontology must be registered by calling
     * {@link #register(org.universAAL.middleware.container.ModuleContext, Ontology)}
     * .
     * 
     * @param uri
     *            URI of the ontology.
     * @return The ontology.
     */
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

	// not found -> debug out
	if (pendingOntologies.size() != 0) {
	    String cls = "The following classes are registered:\n";
	    if (includePending) {
		// test pending classes
		ArrayList pend = pendingOntologies;
		cls += "Pending ontologies:\n";
		for (int i = 0; i < pend.size(); i++) {
		    Ontology ont = (Ontology) pend.get(i);
		    cls += "  Ontology " + ont.getInfo().getURI() + "\n";
		    RDFClassInfo rdf[] = ont.getRDFClassInfo();
		    for (int j = 0; j < rdf.length; j++)
			cls += "    RDF: " + rdf[j].getURI() + "\n";
		    OntClassInfo owl[] = ont.getOntClassInfo();
		    for (int j = 0; j < owl.length; j++)
			cls += "    OWL: " + owl[j].getURI() + "\n";
		}
	    }
	    cls += "Registered ontologies:\n";
	    Object[] classes = rdfClassInfoMap.values().toArray();
	    for (int i = 0; i < classes.length; i++) {
		Resource r = (Resource) classes[i];
		cls += "    RDF: " + r.getURI() + "\n";
	    }
	    classes = ontClassInfoMap.values().toArray();
	    for (int i = 0; i < classes.length; i++) {
		Resource r = (Resource) classes[i];
		cls += "    OWL: " + r.getURI() + "\n";
	    }
	    LogUtils.logDebug(SharedResources.moduleContext,
		    OntologyManagement.class, "isRegisteredClass", cls);
	}

	return false;
    }

    /**
     * Get the set of URIs of all registered ontologies.
     * 
     * @return an array with the URIs of all registered ontologies.
     */
    public String[] getOntoloyURIs() {
	return (String[]) ontologies.keySet().toArray(new String[0]);
    }

    /**
     * Internal method. Used to verify that an ontology is currently in the
     * registration phase.
     * 
     * @param uri
     *            URI of the ontology.
     * @return true, iff the ontology is currently in the registration phase.
     */
    public boolean checkPermission(String uri) {
	if (uri == null)
	    return false;
	return uri.equals(ontClassInfoURIPermissionCheck);
    }

    /**
     * Add a new ontology listener. This listener is notified when a change in
     * ontology management occurs.
     * 
     * @param owner
     *            the {@link ModuleContext} of the caller of this method.
     * @param listener
     *            the listener to add.
     */
    public void addOntologyListener(ModuleContext owner,
	    OntologyListener listener) {
	synchronized (listeners) {
	    if (!listeners.contains(listener))
		listeners.add(listener);
	}
    }

    /**
     * Remove an existing ontology listener. This listener is notified when a
     * change in ontology management occurs.
     * 
     * @param owner
     *            the {@link ModuleContext} of the caller of this method.
     * @param listener
     *            the listener to remove.
     */
    public void removeOntologyListener(ModuleContext owner,
	    OntologyListener listener) {
	synchronized (listeners) {
	    listeners.remove(listener);
	}
    }
}
