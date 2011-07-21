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
package org.universAAL.middleware.service.impl;

import org.universAAL.middleware.util.Constants;
import org.universAAL.middleware.service.AvailabilitySubscriber;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.sodapop.AbstractBus;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;


/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * 
 */
public class ServiceBusImpl extends AbstractBus implements ServiceBus {

	public ServiceBusImpl(SodaPop g) {
		super(Constants.uAAL_BUS_NAME_SERVICE,
				new ServiceStrategy(g),
				g);
		busStrategy.setBus(this);
	}

	public void addAvailabilitySubscription(String callerID,
			AvailabilitySubscriber subscriber,
			ServiceRequest request) {
		if (callerID != null
				&& callerID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)
				&& registry.get(callerID.substring(
						Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length())) instanceof ServiceCaller)
			((ServiceStrategy) busStrategy).addAvailabilitySubscription(callerID, subscriber, request);
	}

	public void addNewRegParams(String calleeID, ServiceProfile[] realizedServices) {
		if (calleeID != null
				&&  calleeID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			((ServiceStrategy) busStrategy).addRegParams(calleeID, realizedServices);
	}

	public ServiceProfile[] getAllServices(String callerID) {
		// TODO Auto-generated method stub
		return null;
	}

	public ServiceProfile[] getMatchingService(String callerID, Service s) {
		return ((ServiceStrategy) busStrategy).getAllServiceProfiles(s.getType());
	}

	public ServiceProfile[] getMatchingService(String callerID, String[] keywords) {
		// TODO Auto-generated method stub
		return null;
	}

	public String register(BusMember member) {
		return null;
	}

	public String register(ServiceCallee callee, ServiceProfile[] realizedServices) {
		String id = Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + super.register(callee);
		if (realizedServices != null)
			((ServiceStrategy) busStrategy).addRegParams(id, realizedServices);
		return id;
	}

	public String register(ServiceCaller caller) {
		return Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + super.register(caller);
	}

	public void removeAvailabilitySubscription(String callerID,
			AvailabilitySubscriber subscriber,
			String requestURI) {
		if (callerID != null
				&& callerID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)
				&& registry.get(callerID.substring(
						Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length())) instanceof ServiceCaller)
			((ServiceStrategy) busStrategy).removeAvailabilitySubscription(callerID, subscriber, requestURI);
	}

	public void removeMatchingRegParams(String calleeID, ServiceProfile[] realizedServices) {
		if (calleeID != null
				&&  calleeID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			((ServiceStrategy) busStrategy).removeMatchingRegParams(calleeID,
					realizedServices);
	}

	public void sendReply(String calleeID, Message response) {
		if (calleeID != null
				&&  calleeID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(calleeID.substring(
					Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					response);
	}

	public void sendMessage(String callerID, Message request) {
		if (callerID != null
				&&  callerID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(callerID.substring(
					Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					request);
	}

	public void unregister(String id, BusMember member) {
	}

	public void unregister(String calleeID, ServiceCallee callee) {
		if (calleeID != null
				&&  calleeID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			((ServiceStrategy) busStrategy).removeRegParams(calleeID);
			super.unregister(calleeID.substring(
					Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					callee);
		}
	}

	public void unregister(String callerID, ServiceCaller caller) {
		if (callerID != null
				&&  callerID.startsWith(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX)) 
			super.unregister(callerID.substring(
					Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					caller);
	}
}
