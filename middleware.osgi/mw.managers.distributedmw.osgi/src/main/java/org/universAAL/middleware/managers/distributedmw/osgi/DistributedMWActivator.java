/*	
	Copyright 2007-2016 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
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
package org.universAAL.middleware.managers.distributedmw.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.managers.api.DistributedMWEventHandler;
import org.universAAL.middleware.managers.distributedmw.api.DistributedMWManager;
import org.universAAL.middleware.managers.distributedmw.impl.DistributedMWManagerImpl;

public class DistributedMWActivator implements BundleActivator {
    ModuleContext context;
    DistributedMWManagerImpl mm;

    public void start(BundleContext arg0) throws Exception {
	context = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { arg0 });
	LogUtils.logDebug(context, getClass(), "start",
		"Starting DistributedMWM Manager.");

	Object[] parMgmt = new Object[] { DistributedMWManager.class.getName() };
	Object[] parEvtH = new Object[] { DistributedMWEventHandler.class
		.getName() };
	mm = new DistributedMWManagerImpl(context, parMgmt, parMgmt, parEvtH,
		parEvtH);

	LogUtils.logDebug(context, getClass(), "start", "Started.");
    }

    public void stop(BundleContext arg0) throws Exception {
	LogUtils.logDebug(context, getClass(), "stop", "Stopping.");
	mm.dispose();
	mm = null;
	LogUtils.logDebug(context, getClass(), "stop", "Stopped.");
    }
}
