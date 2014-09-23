package org.universAAL.middleware.service.test.util;

import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;

public interface CallHandler {
    public ServiceResponse handleCall(ServiceCall call);
}
