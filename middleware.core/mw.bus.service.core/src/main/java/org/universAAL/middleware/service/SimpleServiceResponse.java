package org.universAAL.middleware.service;

public class SimpleServiceResponse extends ServiceResponse {
    public SimpleServiceResponse() {
    }

    public SimpleServiceResponse(String uri) {
	super(uri);
    }

    public SimpleServiceResponse(CallStatus status) {
	super(status);
    }
    
    public void allowUnboundOutput() {
	super.allowUnboundOutput();
    }
    
    public void disallowUnboundOutput() {
	super.disallowUnboundOutput();
    }
}
