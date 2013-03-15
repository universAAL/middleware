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
package org.universAAL.middleware.bus.model.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.serialization.MessageContentSerializer;

/**
 * @author mtazari - <a href="mailto:saied.tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public class Activator implements BundleActivator, ServiceListener {

    private BundleContext context = null;

    public void start(BundleContext context) throws Exception {
	this.context = context;

	// intentionally without checking for null, because if the following
	// OSGi services are not found, then it makes no sense to start this
	// bundle!

	ServiceReference sRef = context
		.getServiceReference(AALSpaceManager.class.getName());
	AALSpaceManager mgr = (AALSpaceManager) context.getService(sRef);
	CommunicationModule mdl = (CommunicationModule) context
		.getService(context
			.getServiceReference(CommunicationModule.class
				.getName()));
	AbstractBus.initBrokerage(mgr, mdl);

	BusMessage.setThisPeer(mgr.getmyPeerCard());

	ServiceReference sr = context
		.getServiceReference(MessageContentSerializer.class.getName());
	if (sr != null) {
	    Object o = context.getService(sr);
	    if (o instanceof MessageContentSerializer)
		BusMessage
			.setMessageContentSerializer((MessageContentSerializer) o);
	    else
		context.addServiceListener(this, "(" + Constants.OBJECTCLASS
			+ "=" + MessageContentSerializer.class.getName() + ")");
	} else
	    context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "="
		    + MessageContentSerializer.class.getName() + ")");
    }

    public void stop(BundleContext context) throws Exception {
    }

    public void serviceChanged(ServiceEvent se) {
	Object o = context.getService(se.getServiceReference());
	if (se.getType() == ServiceEvent.REGISTERED
		&& o instanceof MessageContentSerializer) {
	    BusMessage
		    .setMessageContentSerializer((MessageContentSerializer) o);
	    context.removeServiceListener(this);
	}
    }
}
