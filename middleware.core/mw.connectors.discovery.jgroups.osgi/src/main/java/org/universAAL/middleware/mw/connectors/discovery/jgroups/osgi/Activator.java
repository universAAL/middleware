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
package org.universAAL.middleware.mw.connectors.discovery.jgroups.osgi;

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
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.osgi.uAALBundleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.mw.connectors.discovery.jgroups.core.JGroupsDiscoveryConnector;

/**
 * OSGI bundle for the SLP discovery connector
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class Activator implements BundleActivator, ManagedService {

    private static String SERVICE_PID = "mw.connectors.discovery.jgroups.core";
    DiscoveryConnector jgroupDiscoveryConnector;
    private ServiceRegistration myRegistration;

    public void start(BundleContext context) throws Exception {

	uAALBundleContext moduleContext = (uAALBundleContext) uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });

	LogUtils.logDebug(moduleContext, Activator.class, "Activator",
		new Object[] { "Starting the JGroupsDiscoveryConnector..." }, null);
	jgroupDiscoveryConnector = new JGroupsDiscoveryConnector(moduleContext);

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

	Dictionary slpDConnectorProperties = config.getProperties();

	if (slpDConnectorProperties != null) {
	    jgroupDiscoveryConnector.loadConfigurations(slpDConnectorProperties);
	    jgroupDiscoveryConnector.init();
	}

	// register the jgroupDiscoveryConnector
	uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
		jgroupDiscoveryConnector,
		new Object[] { DiscoveryConnector.class.getName() });
	LogUtils.logDebug(moduleContext, Activator.class, "startBrokerClient",
		new Object[] { "Starting the JGroups"
			+ "DiscoveryConnector..." }, null);

    }

    public void stop(BundleContext arg0) throws Exception {
	jgroupDiscoveryConnector.dispose();
	myRegistration.unregister();
    }

    /**
     * Called-back as soon as properties are loaded or modified
     */
    public void updated(Dictionary properties) throws ConfigurationException {
	jgroupDiscoveryConnector.loadConfigurations(properties);
	jgroupDiscoveryConnector.init();

    }

}
