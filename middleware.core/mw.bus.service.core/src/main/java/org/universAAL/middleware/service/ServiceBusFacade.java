package org.universAAL.middleware.service;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.impl.ServiceBusImpl;

public class ServiceBusFacade {
    public static ServiceBus fetchBus(ModuleContext mc) {
	return (ServiceBus) mc.getContainer().fetchSharedObject(mc,
		ServiceBusImpl.busFetchParams);
    }
}
