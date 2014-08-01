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
package org.universAAL.middleware.connectors.discovery.jgroups.osgi;

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
import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.discovery.jgroups.core.jGroupsDiscoveryConnector;
import org.universAAL.middleware.connectors.discovery.jgroups.osgi.Activator;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.osgi.uAALBundleContext;
import org.universAAL.middleware.container.utils.LogUtils;

/**
 * OSGI bundle for the JGroups discovery connector
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:federico.volpini@isti.cnr.it">Federico Volpini</a>
 */
public class Activator implements BundleActivator, ManagedService {

	private static String SERVICE_PID = "mw.connectors.discovery.jgroups.core";
	private DiscoveryConnector jGroupsDiscoveryConnector;
	private ServiceRegistration myRegistration;

	public void start(BundleContext context) throws Exception {
		uAALBundleContext moduleContext = (uAALBundleContext) uAALBundleContainer.THE_CONTAINER
			.registerModule(new Object[] { context });

		// LOG
		LogUtils.logDebug(moduleContext, Activator.class, "Activator",
			new Object[] { "Starting the JGroupsDiscoveryConnector..." }, null);
		
		jGroupsDiscoveryConnector = new jGroupsDiscoveryConnector(moduleContext);
		
		Dictionary props = new Hashtable();
		props.put(Constants.SERVICE_PID, SERVICE_PID);
		myRegistration = context.registerService(
			ManagedService.class.getName(), this, props);

		ConfigurationAdmin configurationAdmin = null;
		
		ServiceReference sr = context
			.getServiceReference(ConfigurationAdmin.class.getName());
		if (sr != null && context.getService(sr) instanceof ConfigurationAdmin)
		    configurationAdmin = (ConfigurationAdmin) context.getService(sr);
		
		Configuration config = configurationAdmin.getConfiguration(SERVICE_PID);
		Dictionary jgroupsDConnectorProperties = config.getProperties();
		if (jgroupsDConnectorProperties != null) {
			jGroupsDiscoveryConnector.loadConfigurations(jgroupsDConnectorProperties);
			jGroupsDiscoveryConnector.init();
		}

		// register the JGroupsDiscoveryConnector
		uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
			jGroupsDiscoveryConnector,
			new Object[] { DiscoveryConnector.class.getName() });
		
		// LOG
		LogUtils.logDebug(moduleContext, Activator.class, "startBrokerClient",
			new Object[] { "Starting the JGroupsDiscoveryConnector..." }, null);

	}
	
	public void stop(BundleContext context) throws Exception {
		jGroupsDiscoveryConnector.dispose();
		myRegistration.unregister();
	}

	/**
	 * Called-back as soon as properties are loaded or modified
	 */
	public void updated(Dictionary properties) throws ConfigurationException {
		jGroupsDiscoveryConnector.loadConfigurations(properties);
		jGroupsDiscoveryConnector.init();
	}
		
}