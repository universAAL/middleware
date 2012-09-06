package org.universAAL.middleware.service.aapi;

import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceResponse;

public class AapiServiceResponse extends ServiceResponse {
    public AapiServiceResponse() {
    }

    public AapiServiceResponse(String uri) {
	super(uri);
    }

    public AapiServiceResponse(CallStatus status) {
	super(status);
    }

    public void allowUnboundOutput() {
	super.allowUnboundOutput();
    }

    public void disallowUnboundOutput() {
	super.disallowUnboundOutput();
    }
}
