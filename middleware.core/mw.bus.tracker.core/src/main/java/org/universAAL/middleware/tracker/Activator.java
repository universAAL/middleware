package org.universAAL.middleware.tracker;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.tracker.impl.BusMemberRegistryImpl;

public class Activator implements ModuleActivator {

    public static ModuleContext mc;
    
    private IBusMemberRegistry busRegistry;
    
    public void start(ModuleContext mc) throws Exception {
	Activator.mc = mc;
	busRegistry = new BusMemberRegistryImpl(mc);
	mc.getContainer().shareObject(mc, busRegistry, IBusMemberRegistry.busRegistryShareParams);
    }

    public void stop(ModuleContext mc) throws Exception {
	if (busRegistry != null){
	    ((BusMemberRegistryImpl)busRegistry).removeRegistryListeners();
	    busRegistry = null;
	}
    }

}
