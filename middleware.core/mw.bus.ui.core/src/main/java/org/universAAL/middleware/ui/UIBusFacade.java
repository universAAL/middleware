package org.universAAL.middleware.ui;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.ui.impl.UIBusImpl;

public class UIBusFacade {
    public static UIBus fetchBus(ModuleContext mc) {
	return (UIBus) mc.getContainer().fetchSharedObject(mc,
		UIBusImpl.busFetchParams);
    }
}
