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
package org.universAAL.middleware.serialization;

import java.util.List;

import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.rdf.Resource;

// just a test: that's how the turtle serializer could look like
// if Java 1.5 is used, this class could also implement MessageContentSerializer
public class TurtleSerializerImpl implements RDFSerializer {

    public RDFSerializationResult deserialize(String serialized) {
	// TODO Auto-generated method stub
	return null;
    }

    public String getFormatURI() {
	return "http://www.w3.org/ns/formats/Turtle";
    }

    public String getMimeType() {
	// TODO Auto-generated method stub
	return null;
    }

//    // TODO: should this be in RDFSerializer, making it an abstract class?
//    public String serialize(Object messageContent) {
//	if (messageContent instanceof Resource)
//	    return serialize((Resource)messageContent);
//	else if (messageContent instanceof Ontology)
//	    return serialize((Ontology)messageContent);
//	else if (messageContent instanceof List)
//	    return serialize((List)messageContent);
//	return null;
//    }

    public String serialize(Resource messageContent) {
	// TODO Auto-generated method stub
	return null;
    }

    public String serialize(Ontology messageContent) {
	// TODO Auto-generated method stub
	return null;
    }

    public String serialize(List messageContent) {
	// TODO Auto-generated method stub
	return null;
    }
}
