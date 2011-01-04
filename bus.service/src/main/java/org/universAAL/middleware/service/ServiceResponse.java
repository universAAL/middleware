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

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.impl.Activator;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.util.LogUtils;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class ServiceResponse extends Resource {
	public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE + "ServiceResponse";
	
	public static final String PROP_SERVICE_CALL_STATUS = 
		uAAL_VOCABULARY_NAMESPACE + "callStatus";
	public static final String PROP_SERVICE_HAS_OUTPUT = 
		uAAL_VOCABULARY_NAMESPACE + "returns";
	public static final String PROP_SERVICE_SPECIFIC_ERROR = 
		uAAL_VOCABULARY_NAMESPACE + "errorDescription";
	
	static {
		addResourceClass(MY_URI, ServiceResponse.class);
	}

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
	
	/**
	 * Returns all value objects returned for a required output with the given paramURI.
	 * Since the original request might have been responded by several different service components,
	 * asMergedList decides if those responses are returned separately or merged into one list.
	 * A return value of null indicates that there are no outputs in the response.
	 * If an empty list is returned by this method, it indicates that there are no output related to the given paramURI.
	 * Otherwise, the return value is always a list even if there is only one value object in that list.
	 */
	public List getOutput(String paramURI, boolean asMergedList) {
		List outputs = getOutputs();
		if (outputs == null || outputs.size() == 0) {
			LogUtils.logWarning(Activator.logger, "ServiceResponse", "getOutput", new Object[]{"The response contains no output parameters!"}, null);
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
					if (asMergedList  &&  ob instanceof List)
						result.addAll((List)ob);
					else
						result.add(ob);
				}
			} else if (obj instanceof List) {
				List outputLists = (List)obj;
				for (Iterator iter2 = outputLists.iterator(); iter2.hasNext();) {
					ProcessOutput output = (ProcessOutput) iter2.next();
					if (output.getURI().equals(paramURI)) {
						Object ob = output.getParameterValue();
						if (asMergedList  &&  ob instanceof List)
							result.addAll((List)ob);
						else
							result.add(ob);
					}
				}
			}
		}
		
		return result;
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
				if (value instanceof Resource  ||  value instanceof String)
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
