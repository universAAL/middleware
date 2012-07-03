/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

/**
 * A class that represents a service response resource, which is produced by the
 * <code>ServiceCallee</code>-s when handling calls, and are delivered to the
 * <code>ServiceCaller</code>-s as a result of their requests.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public class ServiceResponse extends FinalizedResource {

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
	props.put(PROP_UNBOUND_OUTPUT_ALLOWED, new Boolean(true));
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
     * @return the output with the specified URI.
     */
    public List getOutput(String paramURI, boolean asMergedList) {
	List outputs = getOutputs();
	if (outputs == null || outputs.size() == 0) {
	    LogUtils
		    .logWarn(
			    ServiceBusImpl.moduleContext,
			    ServiceResponse.class,
			    "getOutput",
			    new Object[] { "The response contains no output parameters!" },
			    null);
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
     * Retrieves all of the service outputs as a raw <code>List</code> without
     * any rearranging.
     * 
     * @return the outputs that the invoked services produced.
     */
    public List getOutputs() {
	return (List) props.get(PROP_SERVICE_HAS_OUTPUT);
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
     * This method inherits the superclass behavior, but performs some
     * additional checks for correctness of the property values, specific for
     * the <code>ServiceResponse</code>.
     * 
     * @see org.universAAL.middleware.rdf.Resource#setProperty(java.lang.String,
     *      java.lang.Object)
     */
    public void setProperty(String propURI, Object value) {
	if (propURI == null || value == null || props.containsKey(propURI))
	    return;
	if (propURI.equals(PROP_SERVICE_CALL_STATUS)) {
	    if (!(value instanceof CallStatus))
		if (value instanceof Resource || value instanceof String)
		    value = CallStatus.valueOf(value.toString());
		else
		    return;
	    if (value != null)
		props.put(propURI, value);
	} else if (propURI.equals(PROP_SERVICE_HAS_OUTPUT)) {
	    value = ProcessOutput.checkParameterList(value);
	    if (value != null)
		props.put(propURI, value);
	}
    }
}
