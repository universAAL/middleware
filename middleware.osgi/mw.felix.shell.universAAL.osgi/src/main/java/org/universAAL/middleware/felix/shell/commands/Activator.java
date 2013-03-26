package org.universAAL.middleware.felix.shell.commands;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {

	// Register the command service.
	context.registerService(org.apache.felix.shell.Command.class.getName(),
		new AALSpaceCommand(context), null);
    }

    public void stop(BundleContext arg0) throws Exception {
	// TODO Auto-generated method stub

    }

}
