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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * OSGI bundle for the SLP discovery connector
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:federico.volpini@isti.cnr.it">Francesco Furfari</a>
 */
public class Activator implements BundleActivator, ManagedService {

	private static String SERVICE_PID = "mw.connectors.discovery.jgroups.core";

	public void stop(BundleContext arg0) throws Exception {

	}

	/**
	 * Called-back as soon as properties are loaded or modified
	 */
	public void updated(Dictionary properties) throws ConfigurationException {

	}

	public void start(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
