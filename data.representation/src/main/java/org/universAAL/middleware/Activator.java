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
package org.universAAL.middleware;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.middleware.rdf.TypeMapper;

public class Activator implements BundleActivator, ManagedService {
	// configuration parameters as property keys
	
	/**
	 * The URI prefix for the middleware.
	 */
	public static final String uAAL_AAL_SPACE_ID = "org.universAAL.middleware.peer.member_of";
	
	/**
	 * The root directory of the runtime configuration.
	 */
	public static final String uAAL_CONF_ROOT_DIR = "bundles.configuration.location";
	
	/**
	 * True, if this peer is the coordinator.
	 */
	public static final String uAAL_IS_COORDINATING_PEER = "org.universAAL.middleware.peer.is_coordinator";
	
	/**
	 * True, if debug mode is turned on.
	 */
	public static final String uAAL_IS_DEBUG_MODE = "org.universAAL.middleware.debugMode";

	
	public static Dictionary middlewareProps;
	public static ServiceRegistration registration;
	public static final Logger logger = LoggerFactory.getLogger(Activator.class);
	
	public static BundleContext context = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
	    
	    Activator.context = context;

		// load the reasoning engine
		// the subclasses (e.g., Restriction & ClassExpression) will be loaded automatically
		Class.forName("org.universAAL.middleware.owl.Complement");
		Class.forName("org.universAAL.middleware.owl.Enumeration");
		Class.forName("org.universAAL.middleware.owl.Intersection");
		Class.forName("org.universAAL.middleware.owl.OrderingRestriction");
		Class.forName("org.universAAL.middleware.owl.supply.LevelRating");
		Class.forName("org.universAAL.middleware.owl.supply.Rating");
		Class.forName("org.universAAL.middleware.owl.TypeURI");
		Class.forName("org.universAAL.middleware.owl.Union");
		Class.forName("org.universAAL.middleware.rdf.PropertyPath");
		
		setDefaults();
		
		Dictionary props = new Hashtable(1);
		props.put(Constants.SERVICE_PID, "org.aal-universAAL.middleware.upper");
		registration = context.registerService(ManagedService.class.getName(), this, props);
		
		context.registerService(TypeMapper.class.getName(), TypeMapper.getTypeMapper(), null);
	}
	
	private void setDefaults() {
		middlewareProps = new Hashtable(4);
		middlewareProps.put(uAAL_AAL_SPACE_ID,
				System.getProperty(uAAL_AAL_SPACE_ID, "urn:org.universAAL.aal_space:test_environment"));
		middlewareProps.put(uAAL_IS_COORDINATING_PEER,
				System.getProperty(uAAL_IS_COORDINATING_PEER, "true"));
		middlewareProps.put(uAAL_CONF_ROOT_DIR,
				System.getProperty(uAAL_CONF_ROOT_DIR, System.getProperty("user.dir")));
		if ("true".equals(System.getProperty(uAAL_IS_DEBUG_MODE)))
			middlewareProps.put(uAAL_IS_DEBUG_MODE, "true");
	}
	
	public static String getMiddlewareProp(String key) {
		return (key == null  ||  middlewareProps == null)? null : (String) middlewareProps.get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

	public synchronized void updated(Dictionary properties) throws ConfigurationException {
		if (properties == null)
			setDefaults();
		else {
			Object val = properties.remove(uAAL_AAL_SPACE_ID);
			if (val instanceof String)
				middlewareProps.put(uAAL_AAL_SPACE_ID, val);

			val = properties.remove(uAAL_CONF_ROOT_DIR);
			if (val instanceof String)
				middlewareProps.put(uAAL_CONF_ROOT_DIR, val);

			val = properties.remove(uAAL_IS_COORDINATING_PEER);
			if (val instanceof String)
				middlewareProps.put(uAAL_IS_COORDINATING_PEER, val);

			val = properties.remove(uAAL_IS_DEBUG_MODE);
			if (val instanceof String)
				middlewareProps.put(uAAL_IS_DEBUG_MODE, val);
			
			// according to the documentation of ManagedService: "As a convention, it is
			// recommended that when a Managed Service is updated, it should copy all the
			// properties it does not recognize into the service registration properties.
			// This will allow the Configuration Admin service to set properties on
			// services which can then be used by other applications."
			registration.setProperties(properties);
		}
	}
}
