/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.context;

import org.osgi.framework.BundleContext;
import org.universAAL.middleware.context.owl.ContextProvider;

/**
 * The default context publisher simply ignores the notification about losing
 * connection to other instances of the middleware.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 */
public class DefaultContextPublisher extends ContextPublisher {

	/**
	 * @param context
	 */
	public DefaultContextPublisher(BundleContext context, ContextProvider providerInfo) {
		super(context, providerInfo);
	}

	/**
	 * @see org.universAAL.middleware.context.ContextPublisher#communicationChannelBroken()
	 */
	public void communicationChannelBroken() {
	}

}
