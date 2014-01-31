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
package org.universAAL.middleware.util;

import org.universAAL.middleware.owl.OntologyManagement;

/**
 * Callback for ontology changes. This interface must be implemented and can be
 * registered at
 * {@link OntologyManagement#addOntologyListener(org.universAAL.middleware.container.ModuleContext, OntologyListener)}
 * to be notified when a certain change occurs.
 * 
 * @author Carsten Stockloew
 */
public interface OntologyListener {

    /**
     * Notification when a new ontology is added.
     * 
     * @param ontURI
     *            the URI of the new ontology.
     */
    public void ontologyAdded(String ontURI);

    /**
     * Notification when an existing ontology is removed.
     * 
     * @param ontURI
     *            the URI of the ontology.
     */
    public void ontologyRemoved(String ontURI);
}
