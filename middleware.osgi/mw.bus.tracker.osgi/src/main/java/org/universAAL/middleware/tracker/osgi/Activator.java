package org.universAAL.middleware.tracker.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;

public class Activator implements BundleActivator {

    public static ModuleContext mc;
    
    public void start(BundleContext context) throws Exception {
	mc = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
    }
    
    public void stop(BundleContext mc) throws Exception {

    }
}

