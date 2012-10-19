package org.universAAL.middleware.service;

import java.util.Hashtable;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.owl.Service;

public class SimpleServiceRequest extends ServiceRequest {

    /**
     * A property key for adding non semantic input.
     */
    public static final String PROP_NON_SEMANTIC_INPUT = "http://ontology.universAAL.org/uAAL.owl#nonSemanticInput";

    
    public SimpleServiceRequest() {
    }

    public SimpleServiceRequest(String uri) {
	super(uri);
    }

    public SimpleServiceRequest(Service requestedService,
	    Resource involvedHumanUser) {
	super(requestedService, involvedHumanUser);
    }

    public SimpleServiceRequest(String uriPrefix, int numProps,
	    Service requestedService, Resource involvedHumanUser) {
	super(uriPrefix, numProps, requestedService, involvedHumanUser);
    }

    public SimpleServiceRequest(String uri, Service requestedService,
	    Resource involvedHumanUser) {
	super(uri, requestedService, involvedHumanUser);
    }
    
    public void addInput(String uri, Object input) {
	super.addInput(uri, input);
    }
    
    public Hashtable getInput() {
	return super.getInput();
    }

}
