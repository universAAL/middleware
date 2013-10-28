/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */
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
