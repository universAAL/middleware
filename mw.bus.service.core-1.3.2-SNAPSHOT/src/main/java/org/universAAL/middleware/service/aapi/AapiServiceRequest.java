/*	
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
package org.universAAL.middleware.service.aapi;

import java.util.Hashtable;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.owl.Service;

public class AapiServiceRequest extends ServiceRequest {

    /**
     * A property key for adding non semantic input.
     */
    public static final String PROP_NON_SEMANTIC_INPUT = "http://ontology.universAAL.org/uAAL.owl#nonSemanticInput";

    public AapiServiceRequest() {
    }

    public AapiServiceRequest(String uri) {
	super(uri);
    }

    public AapiServiceRequest(Service requestedService,
	    Resource involvedHumanUser) {
	super(requestedService, involvedHumanUser);
    }

    public AapiServiceRequest(String uriPrefix, int numProps,
	    Service requestedService, Resource involvedHumanUser) {
	super(uriPrefix, numProps, requestedService, involvedHumanUser);
    }

    public AapiServiceRequest(String uri, Service requestedService,
	    Resource involvedHumanUser) {
	super(uri, requestedService, involvedHumanUser);
    }

    /**
     * Add non-semantic input.
     */
    public void addInput(String uri, Object input) {
	Hashtable nonSemanticInput = (Hashtable) props
		.get(AapiServiceRequest.PROP_NON_SEMANTIC_INPUT);
	if (nonSemanticInput == null) {
	    nonSemanticInput = new Hashtable();
	    props.put(AapiServiceRequest.PROP_NON_SEMANTIC_INPUT,
		    nonSemanticInput);
	}
	if (nonSemanticInput.contains(uri)) {
	    throw new IllegalArgumentException();
	} else {
	    nonSemanticInput.put(uri, input);
	}
    }

    /**
     * Get hashtable containing non-semantic input or null if none was provided.
     */
    public Hashtable getInput() {
	return (Hashtable) props
		.get(AapiServiceRequest.PROP_NON_SEMANTIC_INPUT);
    }
}
