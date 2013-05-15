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
package org.universAAL.middleware.managers.deploy.osgi;

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
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.managers.api.DeployManager;
import org.universAAL.middleware.managers.deploy.DeployManagerImpl;

/**
 * Activator for the Deploy Manager
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * 
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class Activator implements BundleActivator, ManagedService {

    private DeployManager deployManager;
    private static String SERVICE_PID = "mw.managers.deploy.core";
    private ServiceRegistration myRegistration;
    private ModuleContext moduleContext;

    public void start(BundleContext context) throws Exception {
	moduleContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	LogUtils.logDebug(moduleContext, Activator.class, "startBrokerClient",
		new Object[] { "Starting the Deploymanager..." }, null);

	BundleConfigHome configHome = new BundleConfigHome("mw.manager.deploy");
	deployManager = new DeployManagerImpl(moduleContext, configHome);

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

	Dictionary deployManagerProps = config.getProperties();

	// if null, the configuration is new
	if (deployManagerProps == null) {
	    deployManagerProps = new Hashtable<String, String>();
	} else {
	    deployManager.loadConfigurations(deployManagerProps);
	}
	deployManager.init();
	uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
		deployManager, new Object[] { DeployManager.class.getName() });
    }

    public void stop(BundleContext context) throws Exception {
	deployManager.dispose();
	myRegistration.unregister();
    }

    public void updated(Dictionary properties) throws ConfigurationException {
	deployManager.loadConfigurations(properties);
	if (myRegistration == null) {
	    LogUtils
		    .logDebug(
			    moduleContext,
			    Activator.class,
			    "updated",
			    new Object[] { "Race Condition: the ServiceRegistration"
				    + " is not yet initialized, waiting for registerService." },
			    null);
	    int numLoops = 20;
	    while (myRegistration == null && numLoops != 0) {
		numLoops--;
		try {
		    Thread.sleep(500);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
	if (myRegistration != null)
	    myRegistration.setProperties(properties);
    }
}
