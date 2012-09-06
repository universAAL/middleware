package org.universAAL.middleware.api;

import org.universAAL.middleware.api.exception.SimplifiedRegistrationException;
import org.universAAL.middleware.api.impl.DynamicServiceProxy;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.DefaultServiceCaller;

public class SimpleServiceLocator {
    public ModuleContext mc;

    public SimpleServiceLocator(ModuleContext mc) {
	this.mc = mc;
    }

    public Object lookupService(Class interfaceClazz)
	    throws IllegalArgumentException, SimplifiedRegistrationException,
	    InstantiationException, IllegalAccessException {
	return DynamicServiceProxy.newInstance(interfaceClazz,
		new DefaultServiceCaller(mc));
    }
}
