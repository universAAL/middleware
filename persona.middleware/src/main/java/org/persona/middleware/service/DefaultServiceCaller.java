/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.persona.middleware.service;

import org.osgi.framework.BundleContext;

/**
 * The default service caller simply ignores the notification about losing
 * connection to other instances of the middleware and calls the services
 * either only in an synchronous way (using the method {@link
 * #call(org.persona.middleware.service.ServiceRequest)}) or ignores the 
 * asynchronous responses.
 * 
 * @author mtazari
 */
public class DefaultServiceCaller extends ServiceCaller {

	/**
	 * @param context
	 */
	public DefaultServiceCaller(BundleContext context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see org.persona.middleware.service.ServiceCaller#communicationChannelBroken()
	 */
	public void communicationChannelBroken() {
	}

	/* (non-Javadoc)
	 * @see org.persona.middleware.service.ServiceCaller#handleResponse(java.lang.String, org.persona.ontology.service.ServiceResponse)
	 */
	public void handleResponse(String reqID, ServiceResponse response) {
	}

}
