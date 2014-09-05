package org.universAAL.middleware.service.test.util;

import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

public class ObjectCallHandler implements CallHandler {
    Object retVal;
    String outputURI;

    public ObjectCallHandler(String outputURI, Object retVal) {
	this.outputURI = outputURI;
	this.retVal = retVal;
    }

    public ServiceResponse handleCall(ServiceCall call) {
	ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	sr.addOutput(new ProcessOutput(outputURI, retVal));
	return sr;
    }
}
