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

import org.universAAL.middleware.rdf.ResourceFactory;

public class SimpleOntology extends Ontology {

    /**
     * Create a simple ontology with only one class and without properties and
     * factory.
     * 
     * @param classURI
     * @param superClassURI
     */
    public SimpleOntology(String classURI, String superClassURI) {
	super(classURI.substring(0, classURI.indexOf('#') + 1));
	OntClassInfoSetup info = createNewAbstractOntClassInfo(classURI);
	info.addSuperClass(superClassURI);
    }

    public SimpleOntology(String classURI, String superClassURI,
	    ResourceFactory factory) {
	super(classURI.substring(0, classURI.indexOf('#') + 1));
	OntClassInfoSetup info = createNewOntClassInfo(classURI, factory);
	info.addSuperClass(superClassURI);
    }

    public void create() {
    }
}
