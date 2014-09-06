package org.universAAL.middleware.service.test.util;

import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

public class TwoObjectCallHandler implements CallHandler {
    Object retVal1;
    Object retVal2;
    String outputURI1;
    String outputURI2;

    public TwoObjectCallHandler(String outputURI1, Object retVal1,
	    String outputURI2, Object retVal2) {
	this.outputURI1 = outputURI1;
	this.outputURI2 = outputURI2;
	this.retVal1 = retVal1;
	this.retVal2 = retVal2;
    }

    public ServiceResponse handleCall(ServiceCall call) {
	ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	sr.addOutput(new ProcessOutput(outputURI1, retVal1));
	sr.addOutput(new ProcessOutput(outputURI2, retVal2));
	return sr;
    }
}
