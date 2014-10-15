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
package org.universAAL.middleware.modules.communication.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.osgi.uAALBundleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.communication.CommunicationModuleImpl;

/**
 * Activator class for the CommunicationModule
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class Activator implements BundleActivator {

    CommunicationModule communicationModule;

    public void start(BundleContext context) throws Exception {

	ModuleContext moduleContext = (uAALBundleContext) uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	LogUtils.logDebug(moduleContext, Activator.class, "startBrokerClient",
		new Object[] { "Starting the CommunicationModule..." }, null);

	communicationModule = new CommunicationModuleImpl(moduleContext);
	communicationModule.init();
	LogUtils.logDebug(moduleContext, Activator.class, "startBrokerClient",
		new Object[] { "Started the CommunicationModule..." }, null);
	uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
		communicationModule,
		new Object[] { CommunicationModule.class.getName() });

    }

    public void stop(BundleContext context) throws Exception {
	if (communicationModule != null)
	    communicationModule.dispose();
    }

}
