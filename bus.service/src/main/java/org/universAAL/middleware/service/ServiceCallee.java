/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.service;

import org.osgi.framework.BundleContext;
import org.universAAL.middleware.service.impl.Activator;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Callee;
import org.universAAL.middleware.sodapop.msg.Message;


/**
 * The convention of the service bus regarding the registration parameters is the
 * following:
 * <ul><li>ServiceCallers don't need any registration parameters</li>
 * <li>ServiceCallees may pass an array of {@link org.universAAL.middleware.service.owls.profile.ServiceProfile}s</li>
 * </ul>
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public abstract class ServiceCallee implements Callee {
	private ServiceBus bus;
	private String myID;
	
	protected ServiceCallee(BundleContext context, ServiceProfile[] realizedServices) {
		Activator.checkServiceBus();
		bus = (ServiceBus) context.getService(
				context.getServiceReference(ServiceBus.class.getName()));
		myID = bus.register(this, realizedServices);
	}
	
	protected final void addNewRegParams(ServiceProfile[] realizedServices) {
		bus.addNewRegParams(myID, realizedServices);
	}
	
	protected final void removeMatchingRegParams(ServiceProfile[] realizedServices) {
		bus.removeMatchingRegParams(myID, realizedServices);
	}

	public abstract void communicationChannelBroken();

	public final void busDyingOut(Bus b) {
		if (b == bus)
			communicationChannelBroken();
	}

	public final boolean eval(Message m) {
		return false;
	}

	public abstract ServiceResponse handleCall(ServiceCall call);

	public final void handleRequest(Message m) {
		if (m != null  &&  m.getContent() instanceof ServiceCall) {
			ServiceResponse sr = handleCall((ServiceCall) m.getContent());
			if (sr == null)
				sr = new ServiceResponse(CallStatus.serviceSpecificFailure);
			Message reply = m.createReply(sr);
			if (reply != null)
				bus.sendReply(myID, reply);
		}
	}
	
	public void close(){
		bus.unregister(myID, this);
	}
}
