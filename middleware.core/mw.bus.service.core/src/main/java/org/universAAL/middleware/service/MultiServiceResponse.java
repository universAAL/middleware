/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.service.owls.process.ProcessOutput;

/**
 * The {@link MultiServiceResponse} represents a set of {@link ServiceResponse}
 * s.
 * 
 * @author Carsten Stockloew
 */
public class MultiServiceResponse extends ServiceResponse {

    /**
     * A resource URI that specifies the resource as a multi service response.
     */
    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "MultiServiceResponse";

    public MultiServiceResponse() {
	super();
    }
    
    public MultiServiceResponse(String instanceURI) {
	super(instanceURI);
    }

    @Override
    public void addOutput(ProcessOutput output) {
	throw new UnsupportedOperationException(
		"Can not add an output to a MultiServiceResponse.");
    }

    /**
     * Retrieves the call status. The status is {@link CallStatus#succeeded} if
     * at least one of the responses has status {@link CallStatus#succeeded}.
     * 
     * @return the current call status. If the aggregated call status is not set
     *         and the individual status are all not succeeded then a random
     *         status from one of the responses is returned.
     */
    @Override
    public CallStatus getCallStatus() {
	CallStatus cs = super.getCallStatus();
	if (cs == null) {
	    // aggregated call status not set -> check individual status
	    List<ServiceResponse> l = getResponses();
	    for (ServiceResponse sr : l) {
		cs = sr.getCallStatus();
		if (cs == CallStatus.succeeded)
		    return CallStatus.succeeded;
	    }
	}
	return cs;
    }

    /**
     * Determines if one of the responses has the given call status.
     * 
     * @param status
     *            the status to search for.
     * @return true, if one of the responses has the given call status.
     */
    public boolean hasCallStatus(CallStatus status) {
	List<ServiceResponse> l = getResponses();
	for (ServiceResponse sr : l) {
	    CallStatus cs = sr.getCallStatus();
	    if (cs == status)
		return true;
	}
	return false;
    }

    /**
     * Determines if one of the responses has a status that is not succeeded.
     * 
     * @return true, of one of the responses has a status that is not succeeded.
     */
    public boolean hasNoSuccessCallStatus() {
	List<ServiceResponse> l = getResponses();
	for (ServiceResponse sr : l) {
	    CallStatus cs = sr.getCallStatus();
	    if (cs != CallStatus.succeeded)
		return true;
	}
	return false;
    }

    @Override
    public List<Object> getOutput(String paramURI) {
	List<ServiceResponse> responses = getResponses();
	List<Object> outputs = null;
	for (ServiceResponse sr : responses) {
	    List<Object> singleOutput = sr.getOutput(paramURI);
	    if (singleOutput != null) {
		// add all values
		if (outputs == null)
		    outputs = new ArrayList<Object>();
		outputs.addAll(singleOutput);
	    }
	}
	return outputs;
    }

    @Override
    public Map<String, List<Object>> getOutputsMap() {
	Map<String, List<Object>> result = new HashMap<String, List<Object>>();
	List<ServiceResponse> responses = getResponses();
	for (ServiceResponse sr : responses) {
	    Map<String, List<Object>> single = sr.getOutputsMap();
	    for (String key : single.keySet()) {
		List<Object> lst = single.get(key);
		List<Object> reslst = result.get(key);
		if (reslst == null) {
		    reslst = new ArrayList<Object>();
		    result.put(key, reslst);
		}
		reslst.addAll(lst);
	    }
	}

	return super.getOutputsMap();
    }

    @Override
    public List<ProcessOutput> getOutputs() {
	List<ProcessOutput> outputs = new ArrayList<ProcessOutput>();
	List<ServiceResponse> responses = getResponses();
	for (ServiceResponse sr : responses) {
	    outputs.addAll(sr.getOutputs());
	}
	return outputs;
    }

    @Override
    public boolean setProperty(String propURI, Object value) {
	if (propURI == null)
	    return false;
	if (propURI.equals(PROP_SERVICE_HAS_OUTPUT)) {
	    // check that value is a list of service responses
	    if (!(value instanceof List))
		return false;
	    List<Object> l = (List<Object>) value;
	    boolean retVal = false;
	    for (Object o : l) {
		if (o instanceof ServiceResponse) {
		    addResponse((ServiceResponse) o);
		    retVal = true;
		}
	    }
	    return retVal;
	}

	return super.setProperty(propURI, value);
    }

    /**
     * Add a new response to the list of responses.
     * 
     * @param response
     *            The response to add.
     */
    public void addResponse(ServiceResponse response) {
	if (response == null)
	    throw new NullPointerException();
	if (response instanceof MultiServiceResponse) {
	    List<ServiceResponse> l = ((MultiServiceResponse) response)
		    .getResponses();
	    for (ServiceResponse s : l)
		addResponse(s);
	    return;
	}
	List<ServiceResponse> l = (List<ServiceResponse>) props
		.get(PROP_SERVICE_HAS_OUTPUT);
	if (l == null)
	    l = new ArrayList<ServiceResponse>();
	// we have to make the response a literal because multiple responses
	// could have the same output URI and this causes problems with
	// serialization (two resources with the same URI cannot be serialized).
	response.literal();
	l.add(response);
	props.put(PROP_SERVICE_HAS_OUTPUT, l);
    }

    /**
     * Get a list of all responses.
     * 
     * @return the non-null list of all responses.
     */
    public List<ServiceResponse> getResponses() {
	List<ServiceResponse> l = (List<ServiceResponse>) props
		.get(PROP_SERVICE_HAS_OUTPUT);
	if (l == null)
	    l = new ArrayList<ServiceResponse>();
	return l;
    }
}
