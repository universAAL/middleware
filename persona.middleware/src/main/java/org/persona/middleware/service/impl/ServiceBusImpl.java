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
package org.persona.middleware.service.impl;

import org.persona.middleware.MiddlewareConstants;
import org.persona.middleware.service.AvailabilitySubscriber;
import org.persona.middleware.service.ServiceBus;
import org.persona.middleware.service.ServiceCallee;
import org.persona.middleware.service.ServiceCaller;
import org.persona.middleware.service.ServiceRequest;
import org.persona.middleware.service.profile.ServiceProfile;
import org.persona.ontology.Service;

import de.fhg.igd.ima.sodapop.AbstractBus;
import de.fhg.igd.ima.sodapop.BusMember;
import de.fhg.igd.ima.sodapop.SodaPop;
import de.fhg.igd.ima.sodapop.msg.Message;

/**
 * @author mtazari
 * 
 */
public class ServiceBusImpl extends AbstractBus implements ServiceBus {

	public ServiceBusImpl(SodaPop g) {
		super(MiddlewareConstants.PERSONA_BUS_NAME_SERVICE,
				new ServiceStrategy(g),
				g);
		busStrategy.setBus(this);
	}

	public void addAvailabilitySubscription(String callerID,
			AvailabilitySubscriber subscriber,
			ServiceRequest request) {
		if (callerID != null
				&& callerID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)
				&& registry.get(callerID.substring(
						MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length())) instanceof ServiceCaller)
			((ServiceStrategy) busStrategy).addAvailabilitySubscription(callerID, subscriber, request);
	}

	public void addNewRegParams(String calleeID, ServiceProfile[] realizedServices) {
		if (calleeID != null
				&&  calleeID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			((ServiceStrategy) busStrategy).addRegParams(calleeID, realizedServices);
	}

	public ServiceProfile[] getAllServices(String callerID) {
		// TODO Auto-generated method stub
		return null;
	}

	public ServiceProfile[] getMatchingService(String callerID, Service s) {
		// TODO Auto-generated method stub
		return null;
	}

	public ServiceProfile[] getMatchingService(String callerID, String[] keywords) {
		// TODO Auto-generated method stub
		return null;
	}

	public String register(BusMember member) {
		return null;
	}

	public String register(ServiceCallee callee, ServiceProfile[] realizedServices) {
		String id = MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + super.register(callee);
		if (realizedServices != null)
			((ServiceStrategy) busStrategy).addRegParams(id, realizedServices);
		return id;
	}

	public String register(ServiceCaller caller) {
		return MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + super.register(caller);
	}

	public void removeAvailabilitySubscription(String callerID,
			AvailabilitySubscriber subscriber,
			String requestURI) {
		if (callerID != null
				&& callerID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)
				&& registry.get(callerID.substring(
						MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length())) instanceof ServiceCaller)
			((ServiceStrategy) busStrategy).removeAvailabilitySubscription(callerID, subscriber, requestURI);
	}

	public void removeMatchingRegParams(String calleeID, ServiceProfile[] realizedServices) {
		if (calleeID != null
				&&  calleeID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			((ServiceStrategy) busStrategy).removeMatchingRegParams(calleeID,
					realizedServices);
	}

	public void sendReply(String calleeID, Message response) {
		if (calleeID != null
				&&  calleeID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(calleeID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					response);
	}

	public void sendMessage(String callerID, Message request) {
		if (callerID != null
				&&  callerID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			super.sendMessage(callerID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					request);
	}

	public void unregister(String id, BusMember member) {
	}

	public void unregister(String calleeID, ServiceCallee callee) {
		if (calleeID != null
				&&  calleeID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) {
			((ServiceStrategy) busStrategy).removeRegParams(calleeID);
			super.unregister(calleeID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					callee);
		}
	}

	public void unregister(String callerID, ServiceCaller caller) {
		if (callerID != null
				&&  callerID.startsWith(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX)) 
			super.unregister(callerID.substring(
					MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length()),
					caller);
	}
}
