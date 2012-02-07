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

public abstract class Ontology {

    public static final String TYPE_OWL_ONTOLOGY = ManagedIndividual.OWL_NAMESPACE
	    + "Ontology";

    public static final String PROP_OWL_IMPORT = ManagedIndividual.OWL_NAMESPACE
	    + "imports";

    // array of String: URIs of Ontologies
    private volatile ArrayList imports = new ArrayList();

    // classURI -> RDFClassInfo
    private volatile HashMap rdfClassInfoMap = new HashMap();

    // classURI -> OntClassInfo
    private volatile HashMap ontClassInfoMap = new HashMap();

    // classURI -> Gefährliche Nachbarn
    private volatile HashMap extendedOntClassInfoMap = new HashMap();

    private Resource info;

    private String ontClassInfoURIPermissionCheck = null;
    private Object ontClassInfoURIPermissionCheckSync = new Object();

    private boolean locked = false;

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

	info = new Resource(ontURI);
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

    protected boolean addImport(String ontURI) {
	if ((ontURI = getValidOntologyURI(ontURI)) == null)
	    return false;
	synchronized (imports) {
	    if (imports.contains(ontURI))
		return true;
	    ArrayList temp = new ArrayList(imports.size() + 1);
	    temp.addAll(imports);
	    temp.add(ontURI);
	    imports = temp;
	    info.setProperty(PROP_OWL_IMPORT, ontURI);
	}
	return true;
    }

    public Resource getInfo() {
	return info;
    }

    public abstract void create();

    /** Internal method. */
    public final boolean checkPermission(String uri) {
	if (uri == null)
	    return false;
	return uri.equals(ontClassInfoURIPermissionCheck);
    }

    public boolean hasOntClass(String classURI) {
	if (ontClassInfoMap.containsKey(classURI))
	    return true;
	return extendedOntClassInfoMap.containsKey(classURI);
    }

    public final OntClassInfo[] getOntClassInfo() {
	synchronized (ontClassInfoMap) {
	    return (OntClassInfo[]) ontClassInfoMap.values().toArray(
		    new OntClassInfo[0]);
	}
    }

    public final RDFClassInfo[] getRDFClassInfo() {
	synchronized (rdfClassInfoMap) {
	    return (RDFClassInfo[]) rdfClassInfoMap.values().toArray(
		    new RDFClassInfo[0]);
	}
    }

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

    protected OntClassInfoSetup createNewAbstractOntClassInfo(String classURI) {
	return createNewOntClassInfo(classURI, null, -1);
    }

    protected OntClassInfoSetup createNewOntClassInfo(String classURI,
	    ResourceFactory fac) {
	return createNewOntClassInfo(classURI, fac, -1);
    }

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

	return (Resource[]) list.toArray(new Resource[0]);
    }

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
