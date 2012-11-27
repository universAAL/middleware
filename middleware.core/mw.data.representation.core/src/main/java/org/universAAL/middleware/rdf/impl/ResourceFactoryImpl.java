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
package org.universAAL.middleware.rdf.impl;

import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;

public abstract class ResourceFactoryImpl implements ResourceFactory {

    /**
     * @see org.universAAL.middleware.rdf.ResourceFactory#createInstance(java.lang.String,
     *      java.lang.String, int)
     */
    public abstract Resource createInstance(String classURI,
	    String instanceURI, int factoryIndex);

    /**
     * @see org.universAAL.middleware.rdf.ResourceFactory#castAs(org.universAAL.middleware.rdf.Resource,
     *      java.lang.String)
     */
    public Resource castAs(Resource r, String classURI) {
	Resource r2 = OntologyManagement.getInstance().getResource(classURI,
		r.getURI());
	// TODO: copy all properties from r to r2
	return r2;
    }
}
