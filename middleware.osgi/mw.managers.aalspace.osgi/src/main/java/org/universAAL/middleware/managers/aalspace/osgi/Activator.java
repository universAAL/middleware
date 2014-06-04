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
package org.universAAL.middleware.managers.aalspace.osgi;

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
import org.universAAL.middleware.managers.aalspace.AALSpaceManagerImpl;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceManager;

/**
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Activator implements BundleActivator, ManagedService {

    private static String SERVICE_PID = "mw.managers.aalspace.core";
    private AALSpaceManager spaceManager;
    private ServiceRegistration myRegistration;
    private ModuleContext moduleContext;

    public void start(BundleContext context) throws Exception {
	moduleContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	BundleConfigHome confHome = new BundleConfigHome(moduleContext.getID());
	spaceManager = new AALSpaceManagerImpl(moduleContext, confHome);

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

	Dictionary aalSpaceManagerProps = config.getProperties();

	// if null, the configuration is new
	if (aalSpaceManagerProps == null) {
	    aalSpaceManagerProps = new Hashtable<String, String>();
	} else
	    spaceManager.loadConfigurations(aalSpaceManagerProps);

	// init
	spaceManager.init();

	LogUtils.logDebug(moduleContext, Activator.class, "Activator",
		new Object[] { "Starting AALSpaceManager..." }, null);

	uAALBundleContainer.THE_CONTAINER.shareObject(moduleContext,
		spaceManager, new String[] { AALSpaceManager.class.getName(),
			AALSpaceEventHandler.class.getName() });
	LogUtils.logDebug(moduleContext, Activator.class, "Activator",
		new Object[] { "Registered" }, null);

    }

    public void stop(BundleContext context) throws Exception {
	spaceManager.dispose();
	myRegistration.unregister();
    }

    public void updated(Dictionary properties) throws ConfigurationException {
	spaceManager.loadConfigurations(properties);
	if (myRegistration == null) {
	    LogUtils.logDebug(
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
	// spaceManager.init();
    }
}
