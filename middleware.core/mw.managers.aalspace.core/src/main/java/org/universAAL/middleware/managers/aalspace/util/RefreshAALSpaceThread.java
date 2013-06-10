package org.universAAL.middleware.managers.aalspace.util;

import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.managers.api.AALSpaceManager;

/**
 * Thread for refreshing AAL Spaces
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public class RefreshAALSpaceThread implements Runnable {

    ModuleContext moduleContext;
    AALSpaceManager aalSpaceManager;
    ControlBroker controlBroker;

    public RefreshAALSpaceThread(ModuleContext moduleContext) {
	this.moduleContext = moduleContext;
    }

    public void run() {
	Object o = moduleContext.getContainer()
		.fetchSharedObject(moduleContext,
			new Object[] { AALSpaceManager.class.getName() });
	Object o1 = moduleContext.getContainer().fetchSharedObject(
		moduleContext, new Object[] { ControlBroker.class.getName() });
	if (o != null && o1 != null) {
	    try {
		aalSpaceManager = (AALSpaceManager) o;
		controlBroker = (ControlBroker) o1;
		if (aalSpaceManager.getAALSpaceDescriptor() != null)
		    controlBroker.renewAALSpace(aalSpaceManager
			    .getAALSpaceDescriptor().getSpaceCard());
	    } catch (Exception e) {
		LogUtils.logError(moduleContext, RefreshAALSpaceThread.class,
			"RefreshAALSpaceThread",
			new Object[] { "Error during AAL Space refresh" }, null);
	    }

	}

    }

}
