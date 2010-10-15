/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.persona.middleware.service;

import java.util.ArrayList;
import java.util.List;

import org.persona.middleware.PResource;
import org.persona.middleware.service.process.ProcessOutput;

/**
 * @author mtazari
 *
 */
public class ServiceResponse extends PResource {
	public static final String MY_URI = PERSONA_VOCABULARY_NAMESPACE + "ServiceResponse";
	
	public static final String PROP_SERVICE_CALL_STATUS = 
		PERSONA_VOCABULARY_NAMESPACE + "callStatus";
	public static final String PROP_SERVICE_HAS_OUTPUT = 
		PERSONA_VOCABULARY_NAMESPACE + "returns";
	public static final String PROP_SERVICE_SPECIFIC_ERROR = 
		PERSONA_VOCABULARY_NAMESPACE + "errorDescription";

	public ServiceResponse() {
		super();
		addType(MY_URI, true);
	}

	public ServiceResponse(CallStatus status) {
		super();
		props.put(PROP_SERVICE_CALL_STATUS, status);
		addType(MY_URI, true);
	}

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

	public CallStatus getCallStatus() {
		return (CallStatus) props.get(PROP_SERVICE_CALL_STATUS);
	}
	
	public List getOutputs() {
		return (List) props.get(PROP_SERVICE_HAS_OUTPUT);
	}

	public boolean isWellFormed() {
		return props.containsKey(PROP_SERVICE_CALL_STATUS);
	}

	public void setProperty(String propURI, Object value) {
		if (propURI == null  ||  value == null  ||  props.containsKey(propURI))
			return;
		if (propURI.equals(PROP_SERVICE_CALL_STATUS)) {
			if (!(value instanceof CallStatus))
				if (value instanceof PResource  ||  value instanceof String)
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
