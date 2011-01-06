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
package org.universAAL.middleware.context.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;
import org.universAAL.middleware.util.Constants;
import org.universAAL.middleware.util.LogUtils;
import org.universAAL.middleware.util.ResourceComparator;


/**
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class Activator implements BundleActivator {
	
	private static BundleContext context = null;
	private static MessageContentSerializer contentSerializer = null;
	private static boolean contextBusStarted = false;
	public static final Logger logger = LoggerFactory.getLogger(Activator.class);
	
	public static synchronized void assessContentSerialization(Resource content) {
		if (Constants.debugMode()) {
			if (contentSerializer == null) {
				ServiceReference sr = context.getServiceReference(MessageContentSerializer.class.getName());
				if (sr == null)
					return;
				
				contentSerializer = (MessageContentSerializer) context.getService(sr);
			}

			LogUtils.logDebug(logger, "Activator", "assessContentSerialization", new Object[] {"Assessing message content serialization:"}, null);
			// System.out.println(new RuntimeException().getStackTrace()[1]);
			
			String str = contentSerializer.serialize(content);
			LogUtils.logDebug(logger, "Activator", "assessContentSerialization", new Object[] {"\n      1. serialization dump\n", str, "\n      2. deserialize & compare with the original resource\n"}, null);
			new ResourceComparator().printDiffs(content, (Resource) contentSerializer.deserialize(str));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Activator.context = context;

		// load classes exported by the context bus
		Class.forName("org.universAAL.middleware.context.ContextEvent");
		Class.forName("org.universAAL.middleware.context.ContextEventPattern");
		Class.forName("org.universAAL.middleware.context.owl.ContextProvider");
	}
	
	public static void checkContextBus() {
		synchronized (context) {
			if (!contextBusStarted) {
				SodaPop sodapop = (SodaPop) context.getService(
						context.getServiceReference(SodaPop.class.getName()));
				context.registerService(ContextBus.class.getName(),
						new ContextBusImpl(sodapop), null);
				contextBusStarted = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}
}
