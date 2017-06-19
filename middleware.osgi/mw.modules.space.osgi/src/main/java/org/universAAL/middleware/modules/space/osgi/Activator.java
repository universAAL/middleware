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
package org.universAAL.middleware.modules.space.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.osgi.uAALBundleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.modules.SpaceModule;
import org.universAAL.middleware.modules.space.SpaceModuleImpl;

/**
 * Activator class for the SpaceModule
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public class Activator implements BundleActivator, ManagedService {
	private static String SERVICE_PID = "mw.modules.space.core";
	private SpaceModule spaceModule;

	private ServiceRegistration myRegistration;

	public void start(BundleContext context) throws Exception {

		ModuleContext moduleContext = (uAALBundleContext) uAALBundleContainer.THE_CONTAINER
				.registerModule(new Object[] { context });
		spaceModule = new SpaceModuleImpl(moduleContext);

		Dictionary props = new Hashtable();
		props.put(Constants.SERVICE_PID, SERVICE_PID);
		myRegistration = context.registerService(ManagedService.class.getName(), this, props);

		ConfigurationAdmin configurationAdmin = null;
		ServiceReference sr = context.getServiceReference(ConfigurationAdmin.class.getName());
		if (sr != null && context.getService(sr) instanceof ConfigurationAdmin)
			configurationAdmin = (ConfigurationAdmin) context.getService(sr);
		Configuration config = configurationAdmin.getConfiguration(SERVICE_PID);

		Dictionary spaceModuleProp = config.getProperties();

		if (spaceModuleProp != null) {
			spaceModule.loadConfigurations(spaceModuleProp);

		} else
			spaceModule.loadConfigurations(new Hashtable<String, String>());

		spaceModule.init();

		uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext, spaceModule,
				new Object[] { SpaceModule.class.getName() });
		LogUtils.logDebug(moduleContext, Activator.class, "startBrokerClient",
				new Object[] { "SpaceModule registered" }, null);

	}

	public void stop(BundleContext arg0) throws Exception {
		if (spaceModule != null)
			spaceModule.dispose();
		myRegistration.unregister();
	}

	public void updated(Dictionary properties) throws ConfigurationException {
		spaceModule.loadConfigurations(properties);

	}

}
