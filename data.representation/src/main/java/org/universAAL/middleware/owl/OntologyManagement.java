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
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;

public class OntologyManagement {

    // Singleton instance
    private static OntologyManagement instance = new OntologyManagement();

    // maps Ontology URI -> Ontology
    private volatile HashMap ontologies = new HashMap();

    // maps classURI -> OntClassInfo
    private volatile HashMap ontClassInfoMap = new HashMap();

    // ArrayList of Ontology
    private volatile ArrayList pendingOntologies = new ArrayList();

    private String ontClassInfoURIPermissionCheck = null;

    
    private OntologyManagement() {
    }

    /** Get the Singleton instance. */
    public static final OntologyManagement getInstance() {
	return instance;
    }

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
		return false;
	    }

	    // copy all existing ontologies to temp
	    HashMap tempOntologies = new HashMap(ontologies.size() + 1);
	    OntClassInfo[] ontClassInfos = ont.getOntClassInfo();
	    HashMap tempOntClassInfoMap = new HashMap(ontClassInfoMap.size()
		    + ontClassInfos.length);

	    // add new ontology
	    LogUtils.logDebug(SharedResources.moduleContext,
		    OntologyManagement.class, "register", new Object[] {
			    "Registering ontology: ", ont.getInfo().getURI() },
		    null);
	    tempOntologies.putAll(ontologies);
	    tempOntologies.put(ont.getInfo().getURI(), ont);

	    tempOntClassInfoMap.putAll(ontClassInfoMap);
	    for (int i = 0; i < ontClassInfos.length; i++) {
		OntClassInfo info = ontClassInfos[i];
		ontClassInfoURIPermissionCheck = info.getURI();
		
		OntClassInfo combined = (OntClassInfo) ontClassInfoMap.get(info.getURI());
		if (combined == null) {
		    // if it does not not exist, add simple cloned one
		    tempOntClassInfoMap.put(info.getURI(), info.clone());
		} else {
		    // if it exists: add extender
		    combined.addExtender(info);
		}
		
		ontClassInfoURIPermissionCheck = null;
	    }

	    // set temp as new set of ontologies
	    ontologies = tempOntologies;
	    ontClassInfoMap = tempOntClassInfoMap;
	}

	// remove from pending
	removePendingOntology(ont);

	return true;
    }

    public void unregister(Ontology ont) {
	// TODO
    }

    public Ontology getOntology(String uri) {
	return (Ontology) ontologies.get(uri);
    }

    public OntClassInfo getOntClassInfo(String classURI) {
	if (classURI == null)
	    return null;
	return (OntClassInfo) ontClassInfoMap.get(classURI);
    }

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

    /** Internal method. */
    public final boolean checkPermission(String uri) {
	if (uri == null)
	    return false;
	return uri.equals(ontClassInfoURIPermissionCheck);
    }
}
