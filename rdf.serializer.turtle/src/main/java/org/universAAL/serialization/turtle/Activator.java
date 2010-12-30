/*
	Copyright 2009-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.serialization.turtle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;


/**
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * 
 */
public class Activator implements BundleActivator, ServiceListener {
	
	static BundleContext context = null;
	static final Logger logger = LoggerFactory.getLogger(Activator.class);
	TurtleParser ser;
	
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
		
		ser = new TurtleParser();
		
		context.registerService(
				new String[] {MessageContentSerializer.class.getName()},
				ser, null);
		
		String filter = "(objectclass=" + TypeMapper.class.getName() + ")";
		context.addServiceListener(this, filter);
		ServiceReference references[] = context.getServiceReferences(null, filter);
		for (int i = 0; references != null && i < references.length; i++)
			this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, references[i]));
				
	}

	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub	
	}

	public void serviceChanged(ServiceEvent event) {
		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
		case ServiceEvent.MODIFIED:
			TurtleUtil.typeMapper = (TypeMapper) context.getService(event.getServiceReference());
			break;
		case ServiceEvent.UNREGISTERING:
			TurtleUtil.typeMapper = null;
			break;
		}		
	}
}
