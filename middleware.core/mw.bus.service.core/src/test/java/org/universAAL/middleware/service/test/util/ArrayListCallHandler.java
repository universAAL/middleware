package org.universAAL.middleware.service.test.util;

import java.util.ArrayList;

import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

public class ArrayListCallHandler implements CallHandler {
    ArrayList<Object> al = new ArrayList<Object>();
    String outputURI;

    public ArrayListCallHandler(String outputURI, Object... args) {
	this.outputURI = outputURI;
	for (Object arg : args) {
	    al.add(arg);
	}
    }

    public ServiceResponse handleCall(ServiceCall call) {
	ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	sr.addOutput(new ProcessOutput(outputURI, al));
	return sr;
    }

}
