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

import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.rdf.FinalizedResource;

/**
 * The CallStatus simply describes the possible status of the services. The
 * possible status for services can take the values below: 0.SUCCEEDED
 * 1.NO_MATCHING_SERVICE_FOUND 2.RESPONSE_TIMED_OUT 3.SERVICE_SPECIFIC_FAILURE
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class CallStatus extends FinalizedResource {
    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "CallStatus";

    public static final int SUCCEEDED = 0;
    public static final int NO_MATCHING_SERVICE_FOUND = 1;
    public static final int RESPONSE_TIMED_OUT = 2;
    public static final int SERVICE_SPECIFIC_FAILURE = 3;
    public static final int DENIED = 4;

    private static final Map<Integer, String> names = new HashMap<Integer, String>();

    static {
	names.put(SUCCEEDED, "call_succeeded");
	names.put(NO_MATCHING_SERVICE_FOUND, "no_matching_service_found");
	names.put(RESPONSE_TIMED_OUT, "response_timed_out");
	names.put(SERVICE_SPECIFIC_FAILURE, "service_specific_failure");
	names.put(DENIED, "denied");
    }

    public static final CallStatus succeeded = new CallStatus(SUCCEEDED);
    public static final CallStatus noMatchingServiceFound = new CallStatus(
	    NO_MATCHING_SERVICE_FOUND);
    public static final CallStatus responseTimedOut = new CallStatus(
	    RESPONSE_TIMED_OUT);
    public static final CallStatus serviceSpecificFailure = new CallStatus(
	    SERVICE_SPECIFIC_FAILURE);
    public static final CallStatus denied = new CallStatus(DENIED);

    /**
     * Returns the value of the call status. It returns the predefined names for
     * the call status of services according touAAL_VOCABULARY_NAMESPACE.
     * 
     * @param name
     *            gets the status value
     */
    public static CallStatus valueOf(String name) {
	if (name != null) {
	    if (name.startsWith(uAAL_VOCABULARY_NAMESPACE)) {
		name = name.substring(uAAL_VOCABULARY_NAMESPACE.length());
	    }
	    for (Integer status : names.keySet()) {
		if (names.get(status).equals(name)) {
		    switch (status) {
		    case SUCCEEDED:
			return succeeded;
		    case NO_MATCHING_SERVICE_FOUND:
			return noMatchingServiceFound;
		    case RESPONSE_TIMED_OUT:
			return responseTimedOut;
		    case SERVICE_SPECIFIC_FAILURE:
			return serviceSpecificFailure;
		    case DENIED:
			return denied;
		    }
		}
	    }
	    return null;
	}
	return null;
    }

    private int order;

    /**
     * Constructor for usage by de-serializers.
     */
    // prevent the usage of the default constructor
    private CallStatus() {

    }

    /**
     * Creates a CallStatus object.
     * 
     * @param order
     *            defines the order of each service call status
     */
    private CallStatus(int order) {
	super(uAAL_VOCABULARY_NAMESPACE + names.get(order));
	addType(MY_URI, true);
	this.order = order;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#getPropSerializationType(String
     *      propURI)
     * @param propURI
     *            the URI of the property
     */
    @Override
    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_OPTIONAL;
    }

    /**
     * Returns the name of the {@link CallStatus} as specified by the order
     */
    public String name() {
	return names.get(order);
    }

    /**
     * Returns the number of the order (integer).
     */
    public int ord() {
	return order;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#setProperty(String propURI,
     *      Object value)
     */
    @Override
    public boolean setProperty(String propURI, Object o) {
	// do nothing
	return false;
    }
}
