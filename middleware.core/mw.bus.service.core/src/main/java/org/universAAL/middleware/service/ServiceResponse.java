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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.bus.model.matchable.Response;
import org.universAAL.middleware.bus.model.matchable.UtilityReply;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ScopedResource;
import org.universAAL.middleware.service.impl.ServiceRealization;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

/**
 * A class that represents a service response resource, which is produced by the
 * <code>ServiceCallee</code>-s when handling calls, and are delivered to the
 * <code>ServiceCaller</code>-s as a result of their requests.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public class ServiceResponse extends ScopedResource implements Response,
	UtilityReply {

    /**
     * A resource URI that specifies the resource as a service response.
     */
    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "ServiceResponse";

    /**
     * A property key for the property where the status of the call is stored.
     */
    public static final String PROP_SERVICE_CALL_STATUS = uAAL_VOCABULARY_NAMESPACE
	    + "callStatus";

    /**
     * A property key for the property where the service outputs are stored.
     */
    public static final String PROP_SERVICE_HAS_OUTPUT = uAAL_VOCABULARY_NAMESPACE
	    + "returns";

    /**
     * A property key for the property where any errors occurred during the
     * service invocation are stored.
     */
    public static final String PROP_SERVICE_SPECIFIC_ERROR = uAAL_VOCABULARY_NAMESPACE
	    + "errorDescription";

    /**
     * A key of property indicating that not bound output is allowed.
     */
    public static final String PROP_UNBOUND_OUTPUT_ALLOWED = uAAL_VOCABULARY_NAMESPACE
	    + "unboundOutputAllowed";

    /**
     * Default constructor for the class. Only sets the class URI of the
     * <code>Resource</code> to <code>MY_URI</code>.
     */
    public ServiceResponse() {
	super();
	addType(MY_URI, true);
    }

    /** Constructor for the class. */
    public ServiceResponse(String uri) {
	super(uri);
	addType(MY_URI, true);
    }

    protected void allowUnboundOutput() {
	props.put(PROP_UNBOUND_OUTPUT_ALLOWED, Boolean.TRUE);
    }

    protected void disallowUnboundOutput() {
	props.remove(PROP_UNBOUND_OUTPUT_ALLOWED);
    }

    public boolean isUnboundOutputAllowed() {
	if (props.get(PROP_UNBOUND_OUTPUT_ALLOWED) != null) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Constructor which besides the URI, sets the status of the call.
     * 
     * @param status
     *            the current status of the call.
     */
    public ServiceResponse(CallStatus status) {
	super();
	props.put(PROP_SERVICE_CALL_STATUS, status);
	addType(MY_URI, true);
    }

    /**
     * Adds output payload to this object. Keeps any output payload that was
     * previously added.
     * 
     * @param output
     *            the ouput that needs to be added.
     */
    public void addOutput(ProcessOutput output) {
	if (output != null) {
	    List outputs = (List) props.get(PROP_SERVICE_HAS_OUTPUT);
	    if (outputs == null) {
		outputs = new ArrayList(3);
		props.put(PROP_SERVICE_HAS_OUTPUT, outputs);
	    }
	    outputs.add(output);
	}
    }

    /**
     * Retrieves the call status.
     * 
     * @return the current call status.
     */
    public CallStatus getCallStatus() {
	return (CallStatus) props.get(PROP_SERVICE_CALL_STATUS);
    }

    /**
     * Returns all value objects returned for a required output with the given
     * paramURI. Since the original request might have been responded by several
     * different service components, the responses are merged and returned into
     * one list. A return value of null indicates that there are no outputs in
     * the response. If an empty list is returned by this method, it indicates
     * that there are no output related to the given paramURI. Otherwise, the
     * return value is always a list even if there is only one value object in
     * that list.
     * 
     * @param paramURI
     *            the URI of the required output.
     * @return the output with the specified URI.
     */
    public List getOutput(String paramURI) {
	return getOutput(paramURI, true);
    }

    /**
     * Returns all value objects returned for a required output with the given
     * paramURI. Since the original request might have been responded by several
     * different service components, asMergedList decides if those responses are
     * returned separately or merged into one list. A return value of null
     * indicates that there are no outputs in the response. If an empty list is
     * returned by this method, it indicates that there are no output related to
     * the given paramURI. Otherwise, the return value is always a list even if
     * there is only one value object in that list.
     * 
     * @param paramURI
     *            the URI of the required output.
     * @param asMergedList
     *            specifies if the outputs of the separate services are merged.
     *            This parameter is not available at the moment and should be
     *            set to <tt>true</tt>
     * @return the output with the specified URI.
     */
    public List getOutput(String paramURI, boolean asMergedList) {
	List outputs = getOutputs();
	if (outputs == null || outputs.size() == 0) {
	    return null;
	}

	List result = new ArrayList();

	// iterate over the available output parameters
	for (Iterator iter1 = outputs.iterator(); iter1.hasNext();) {
	    Object obj = iter1.next();
	    if (obj instanceof ProcessOutput) {
		ProcessOutput output = (ProcessOutput) obj;
		// check by the param URI if this is the right output
		if (output.getURI().equals(paramURI)) {
		    Object ob = output.getParameterValue();
		    if (asMergedList && ob instanceof List)
			result.addAll((List) ob);
		    else
			result.add(ob);
		}
	    } else if (obj instanceof List) {
		// this can happen if we get responses from more than one
		// service
		List outputLists = (List) obj;
		for (Iterator iter2 = outputLists.iterator(); iter2.hasNext();) {
		    ProcessOutput output = (ProcessOutput) iter2.next();
		    if (output.getURI().equals(paramURI)) {
			Object ob = output.getParameterValue();
			if (asMergedList && ob instanceof List)
			    result.addAll((List) ob);
			else
			    result.add(ob);
		    }
		}
	    }
	}

	return result;
    }

    /**
     * Get all outputs. This method is similar to
     * {@link #getOutput(String, boolean)} but instead of providing the output
     * of one parameter, it provides the outputs of all parameters. The URI of
     * the parameter is the key of the returned map.
     * 
     * @return all outputs of the service.
     */
    public Map<String, List> getOutputsMap() {
	Map<String, List> result = new HashMap<String, List>();

	List outputs = getOutputs();
	if (outputs == null || outputs.size() == 0) {
	    return result;
	}

	// iterate over the available output parameters
	for (Object obj : outputs) {
	    if (obj instanceof ProcessOutput) {
		ProcessOutput output = (ProcessOutput) obj;

		List l = result.get(output.getURI());
		if (l == null) {
		    l = new ArrayList(3);
		    result.put(output.getURI(), l);
		}

		Object ob = output.getParameterValue();
		if (ob instanceof List)
		    l.addAll((List) ob);
		else
		    l.add(ob);
	    } else if (obj instanceof List) {
		// this can happen if we get responses from more than one
		// service
		List outputLists = (List) obj;
		for (Iterator iter2 = outputLists.iterator(); iter2.hasNext();) {
		    ProcessOutput output = (ProcessOutput) iter2.next();

		    List l = result.get(output.getURI());
		    if (l == null) {
			l = new ArrayList(3);
			result.put(output.getURI(), l);
		    }

		    Object ob = output.getParameterValue();
		    if (ob instanceof List)
			l.addAll((List) ob);
		    else
			l.add(ob);
		}
	    }
	}

	return result;
    }

    /**
     * Retrieves all of the service outputs as a raw <code>List</code> without
     * any rearranging.
     * 
     * @return the outputs that the invoked services produced.
     */
    public List<ProcessOutput> getOutputs() {
	return (List<ProcessOutput>) props.get(PROP_SERVICE_HAS_OUTPUT);
    }

    /**
     * Tests the object for correctness by verifying the presence of
     * <code>PROP_SERVICE_CALL_STATUS</code> property.
     * 
     * @see org.universAAL.middleware.rdf.Resource#isWellFormed()
     */
    public boolean isWellFormed() {
	return props.containsKey(PROP_SERVICE_CALL_STATUS);
    }
    
    /**
     * Get the provider of the requested service. The provider is the URI of the
     * bus member of the {@link ServiceCallee}.
     * 
     * @return The URI of the service provider.
     */
    public Resource getProvider() {
	return (Resource) props.get(ServiceRealization.uAAL_SERVICE_PROVIDER);
    }

    /**
     * This method inherits the superclass behavior, but performs some
     * additional checks for correctness of the property values, specific for
     * the <code>ServiceResponse</code>.
     * 
     * @see org.universAAL.middleware.rdf.Resource#setProperty(java.lang.String,
     *      java.lang.Object)
     */
    public boolean setProperty(String propURI, Object value) {
	if (propURI == null || value == null || props.containsKey(propURI))
	    return false;
	if (propURI.equals(PROP_SERVICE_CALL_STATUS)) {
	    if (!(value instanceof CallStatus))
		if (value instanceof Resource || value instanceof String)
		    value = CallStatus.valueOf(value.toString());
		else
		    return false;
	    if (value != null) {
		props.put(propURI, value);
		return true;
	    }
	} else if (propURI.equals(PROP_SERVICE_HAS_OUTPUT)) {
	    value = ProcessOutput.checkParameterList(value);
	    if (value != null) {
		props.put(propURI, value);
		return true;
	    }
	} else if (propURI.equals(ServiceRealization.uAAL_SERVICE_PROVIDER)) {
	    if (value instanceof Resource)
		props.put(propURI, value);
	}
	return false;
    }
}
