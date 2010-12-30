/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - InstitutInstitut für Graphische Datenverarbeitung
	
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

import org.universAAL.middleware.rdf.Resource;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class CallStatus extends Resource {
	public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE + "CallStatus";
	
	public static final int SUCCEEDED = 0;
    public static final int NO_MATCHING_SERVICE_FOUND = 1;
    public static final int RESPONSE_TIMED_OUT = 2;
    public static final int SERVICE_SPECIFIC_FAILURE = 3;

    private static final String[] names = {
        "call_succeeded", "no_matching_service_found", "response_timed_out", "service_specific_failure"
    };
	
	public static final CallStatus succeeded = new CallStatus(SUCCEEDED);
    public static final CallStatus noMatchingServiceFound = new CallStatus(NO_MATCHING_SERVICE_FOUND);
    public static final CallStatus responseTimedOut = new CallStatus(RESPONSE_TIMED_OUT);
    public static final CallStatus serviceSpecificFailure = new CallStatus(SERVICE_SPECIFIC_FAILURE);
    
    public static CallStatus valueOf(String name) {
    	if (name == null)
    		return null;
    	if (name.startsWith(uAAL_VOCABULARY_NAMESPACE))
    		name = name.substring(uAAL_VOCABULARY_NAMESPACE.length());
        for (int i=SUCCEEDED;  i<=SERVICE_SPECIFIC_FAILURE; i++)
            if (names[i].equals(name)) {
                switch (i) {
                case SUCCEEDED: return succeeded;
                case NO_MATCHING_SERVICE_FOUND: return noMatchingServiceFound;
                case RESPONSE_TIMED_OUT: return responseTimedOut;
                case SERVICE_SPECIFIC_FAILURE: return serviceSpecificFailure;
                }
            }
        return null;
    }
    
    private int order;
    
    // prevent the usage of the default constructor
    private CallStatus() {
       
    }
    
    private CallStatus(int order) {
    	super(uAAL_VOCABULARY_NAMESPACE + names[order]);
    	addType(MY_URI, true);
        this.order = order;
    }

	public int getPropSerializationType(String propURI) {
		return PROP_SERIALIZATION_OPTIONAL;
	}
    
    public String name() {
        return names[order];
    }
    
    public int ord() {
        return order;
    }

	public void setProperty(String propURI, Object o) {
		// do nothing
	}
}
