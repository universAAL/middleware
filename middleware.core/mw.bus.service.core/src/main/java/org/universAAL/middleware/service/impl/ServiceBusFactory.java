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
package org.universAAL.middleware.service.impl;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.service.AggregatingFilter;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owl.InitialServiceDialog;
import org.universAAL.middleware.service.owls.process.ProcessInput;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.process.ProcessParameter;
import org.universAAL.middleware.service.owls.process.ProcessResult;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * 
 * @author Carsten Stockloew
 * 
 */
public class ServiceBusFactory implements ResourceFactory {

    public Resource createInstance(String classURI, String instanceURI,
	    int factoryIndex) {

	switch (factoryIndex) {
	case 0:
	    return new AggregatingFilter(instanceURI);
	case 1:
	    return new ServiceRequest(instanceURI);
	case 2:
	    return new ServiceResponse(instanceURI);
	case 3:
	    return new ServiceCall(instanceURI);
	case 4:
	    return new ServiceProfile(instanceURI);
	case 5:
	    return new InitialServiceDialog(instanceURI);
	case 6:
	    return new ServiceRealization(instanceURI);
	case 7:
	    return new ProcessInput(instanceURI);
	case 8:
	    return new ProcessOutput(instanceURI);
	case 9:
	    return new ProcessResult(instanceURI);
	case 10:
	    return new ProcessParameter(instanceURI, ProcessParameter.MY_URI);
	}

	/*
	 * All classes that are sub classes of Resource, the ones marked with
	 * '+' are registered (e.g. for deserialisation). State: 2011-07-20
	 * 
	 * + AggregatingFilter MY_URI = uAAL_VOCABULARY_NAMESPACE +
	 * "AggregatingFilter" + ServiceCall MY_URI =
	 * ProcessInput.OWLS_PROCESS_NAMESPACE + "Perform" + ServiceRequest
	 * MY_URI = uAAL_VOCABULARY_NAMESPACE + "ServiceRequest" +
	 * ServiceResponse MY_URI = uAAL_VOCABULARY_NAMESPACE +
	 * "ServiceResponse" + ServiceProfile MY_URI = OWLS_PROFILE_NAMESPACE +
	 * "Profile" AggregationFunction MY_URI = uAAL_VOCABULARY_NAMESPACE +
	 * "AggregationFunction" CallStatus MY_URI = uAAL_VOCABULARY_NAMESPACE +
	 * "CallStatus" ServiceRealization MY_URI = uAAL_VOCABULARY_NAMESPACE +
	 * "ServiceRealization" ProcessInput MY_URI = OWLS_PROCESS_NAMESPACE +
	 * "Input" ProcessOutput MY_URI = OWLS_PROCESS_NAMESPACE + "Output"
	 * ProcessParameter MY_URI = OWLS_PROCESS_NAMESPACE + "Parameter"
	 * ProcessResult TYPE_OWLS_RESULT = ProcessOutput.OWLS_PROCESS_NAMESPACE
	 * + "Result" MultiLocationParameter MY_URI = uAAL_SERVICE_NAMESPACE +
	 * "MultiAbsLocationParameter" NumberOfSamples MY_URI =
	 * uAAL_SERVICE_NAMESPACE + "NumberOfSamples" ProfileParameter MY_URI =
	 * ServiceProfile.OWLS_PROFILE_NAMESPACE + "ServiceParameter" QoSRating
	 * MY_URI = uAAL_SERVICE_NAMESPACE + "QoSRating"
	 * ResponseTimeInMilliseconds MY_URI = uAAL_SERVICE_NAMESPACE +
	 * "ResponseTimeInMilliseconds" SingleLocationParameter MY_URI =
	 * uAAL_SERVICE_NAMESPACE + "SingleLocationParameter"
	 */

	// if (classURI == null)
	// return null;
	// if (classURI.startsWith(Resource.uAAL_VOCABULARY_NAMESPACE)) {
	// String className =
	// classURI.substring(Resource.uAAL_VOCABULARY_NAMESPACE.length());
	// if (className.equals("AggregatingFilter"))
	// return new AggregatingFilter(instanceURI);
	// else if (className.equals("ServiceRequest"))
	// return new ServiceRequest(instanceURI);
	// else if (className.equals("ServiceResponse"))
	// return new ServiceResponse(instanceURI);
	// } else if (classURI.equals(ServiceCall.MY_URI)) {
	// return new ServiceCall(instanceURI);
	// } else if (classURI.equals(ServiceProfile.MY_URI)) {
	// return new ServiceProfile(instanceURI);
	// }

	/*
	 * All classes that are sub classes of ManagedIndividual, the ones
	 * marked with '+' are registered (e.g. for deserialisation). State:
	 * 2011-07-20
	 * 
	 * + InitialServiceDialog MY_URI = uAAL_SERVICE_NAMESPACE +
	 * "InitialServiceDialog" + UserInterfaceService MY_URI =
	 * uAAL_SERVICE_NAMESPACE + "UserInterfaceService" + Service MY_URI =
	 * OWLS_SERVICE_NAMESPACE + "Service"
	 */

	// if (classURI.equals(InitialServiceDialog.MY_URI))
	// return new InitialServiceDialog(instanceURI);
	return null;
    }
}
