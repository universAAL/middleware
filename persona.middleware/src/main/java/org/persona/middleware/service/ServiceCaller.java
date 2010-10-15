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

import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.persona.middleware.Activator;
import org.persona.middleware.service.profile.ServiceProfile;
import org.persona.ontology.Service;

import de.fhg.igd.ima.sodapop.Bus;
import de.fhg.igd.ima.sodapop.Caller;
import de.fhg.igd.ima.sodapop.msg.Message;
import de.fhg.igd.ima.sodapop.msg.MessageType;

/**
 * @author mtazari
 *
 */
public abstract class ServiceCaller implements Caller {
	private ServiceBus bus;
	private Hashtable waitingCalls, readyResponses;
	private String myID;
	
	protected ServiceCaller(BundleContext context) {
		waitingCalls = new Hashtable();
		readyResponses = new Hashtable();
		
		Activator.checkServiceBus();
		bus = (ServiceBus) context.getService(
				context.getServiceReference(ServiceBus.class.getName()));
		myID = bus.register(this);
	}

	public final void busDyingOut(Bus b) {
		if (b == bus)
			communicationChannelBroken();
	}
	
	/**
	 * The "normal" (synchronous) way of calling a service. Use
	 * {@link #sendRequest(ServiceRequest)}, if you want to handle the response
	 * asynchronously in another thread.
	 */
	public final ServiceResponse call(ServiceRequest request) {
		ServiceResponse sr = null;
		synchronized (waitingCalls) {
			String callID = sendRequest(request);
			waitingCalls.put(callID, this);
			while (sr == null) {
				try {
					waitingCalls.wait();
					sr = (ServiceResponse) readyResponses.remove(callID);
				} catch (InterruptedException e) {}
			}
		}
		return sr;
	}

	public void close(){
		bus.unregister(myID, this);
	}

	public abstract void communicationChannelBroken();
	
	public final void handleReply(Message m) {
		if (m.getType() == MessageType.reply  &&  (m.getContent() instanceof ServiceResponse)) {
			String reqID = m.getInReplyTo();
			synchronized (waitingCalls) {
				if (waitingCalls.remove(reqID) == null)
					handleResponse(reqID, (ServiceResponse) m.getContent());
				else {
					readyResponses.put(reqID, m.getContent());
					waitingCalls.notifyAll();
				}
			}
		}
	}
	
	/**
	 * Will be called automatically in a new thread whenever the response corresponding
	 * to a previous call to {@link #sendRequest(ServiceRequest)} is ready.
	 * @param reqID the ID returned by the previous call to {@link #sendRequest(ServiceRequest)}.
	 * @param response the expected response.
	 */
	public abstract void handleResponse(String reqID, ServiceResponse response);
	
	/**
	 * To be used if the caller would like to handle the reply asynchronously within the method
	 * {@link #handleResponse(String, ServiceResponse)}, which will automatically
	 * be called in another thread when the response is ready.
	 * 
	 * @return a unique ID for this request that will be passed to the method
	 * {@link #handleResponse(String, ServiceResponse)} for an unambiguous mapping of
	 * responses to requests.
	 */
	public final String sendRequest(ServiceRequest request) {
		request.setProperty(ServiceRequest.PROP_PERSONA_SERVICE_CALLER, myID);
		Activator.assessContentSerialization(request);
		Message reqMsg = new Message(MessageType.request, request);
		bus.sendMessage(myID, reqMsg);
		return reqMsg.getID();
	}
	
	public ServiceProfile[] getAllServices() {
		return bus.getAllServices(myID);
	}
	
	public ServiceProfile[] getMatchingService(Service s) {
		return bus.getMatchingService(myID, s);
	}
	
	public ServiceProfile[] getMatchingService(String[] keywords) {
		return bus.getMatchingService(myID, keywords);
	}
	
	public void addAvailabilitySubscription(
			AvailabilitySubscriber subscriber,
			ServiceRequest request) {
		bus.addAvailabilitySubscription(myID, subscriber, request);
	}

	public void removeAvailabilitySubscription(
			AvailabilitySubscriber subscriber,
			String requestURI) {
		bus.removeAvailabilitySubscription(myID, subscriber, requestURI);
	}
}
