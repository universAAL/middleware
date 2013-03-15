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
package org.universAAL.middleware.serialization;

import java.util.List;

import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.rdf.Resource;

public class RDFSerializationResult {

    public static final int TYPE_ONTOLOGY = 0;
    public static final int TYPE_RESOURCE = 1;
    public static final int TYPE_RESOURCE_LIST = 2;
    public static final int TYPE_LIST = 3;
    
    private Ontology ontology = null;
    private Resource resource = null;
    private List list = null;
    private int type = -1;
    
    public RDFSerializationResult(Ontology ont) {
	ontology = ont;
	type = TYPE_ONTOLOGY;
    }
    
    public RDFSerializationResult(Resource res) {
	resource = res;
	type = TYPE_RESOURCE;
    }
    
    public RDFSerializationResult(List list, boolean isRdfList) {
	this.list = list;
	if (isRdfList)
	    type = TYPE_RESOURCE_LIST;
	else
	    type = TYPE_LIST;
    }
    
    public int getType() {
	return type;
    }
    
    public Ontology getOntology() {
	return ontology;
    }

    public Resource getResource() {
	switch(type) {
	case TYPE_RESOURCE:
	    return resource;
	case TYPE_RESOURCE_LIST:
	    if (list == null || list.isEmpty())
		return null;
	    return Resource.asRDFList(list, false);
	case TYPE_LIST:
	    if (list == null || list.isEmpty())
		return null;
	    return (Resource) list.get(0);
	}
	return null;
    }
    
    public Resource getResource(String rootURI) {
	// TODO: we probably need one more argument in the constructor for that
	return null;
    }
    
    public List getList() {
	return list;
    }
}
