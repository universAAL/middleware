/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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
package org.universAAL.middleware.datarep.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.datarep.SharedResources;

public class Activator implements BundleActivator, ManagedService {
    private static ServiceRegistration registration;

    /**
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     *      )
     */
    public void start(BundleContext context) throws Exception {

	SharedResources.moduleContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	SharedResources.loadReasoningEngine();
	SharedResources.setDefaults();

	Dictionary props = new Hashtable(1);
	props.put(Constants.SERVICE_PID,
		SharedResources.uAAL_MW_SHARED_PROPERTY_FILE);
	registration = context.registerService(ManagedService.class.getName(),
		this, props);
    }

    /**
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
	SharedResources.unloadReasoningEngine();
    }

    /**
     * @see org.osgi.service.cm.ManagedService#updated(Dictionary)
     */
    public synchronized void updated(Dictionary properties)
	    throws ConfigurationException {

	SharedResources.updateProps(properties);

	// according to the documentation of ManagedService: "As a
	// convention, it is
	// recommended that when a Managed Service is updated, it should
	// copy all the
	// properties it does not recognize into the service registration
	// properties.
	// This will allow the Configuration Admin service to set properties
	// on
	// services which can then be used by other applications."
	registration.setProperties(properties);
    }
}
