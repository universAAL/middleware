package org.universAAL.middleware.context;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.impl.ContextBusImpl;

public class ContextBusFacade {
    public static ContextBus fetchBus(ModuleContext mc) {
	return (ContextBus) mc.getContainer().fetchSharedObject(mc,
		ContextBusImpl.busFetchParams);
    }
}
