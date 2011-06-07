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

import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.universAAL.middleware.service.impl.Activator;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Caller;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageType;


/**
/**
 * This is an abstract class that the service caller members of the service bus must derive from.
 * Any <code>ServiceCaller</code> must incorporate the logic of handling service responses
 * into the implementation of the abstract method <code>handleResponse(String, ServiceResponse)</code>.
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public abstract class ServiceCaller implements Caller {
	private ServiceBus bus;
	private Hashtable waitingCalls, readyResponses;
	private String myID;
	
	/**
	 * The default constructor for this class.
	 * @param context The OSGI bundle context where the ServiceBus is registered. Note that if no service bus is 
	 * registered within the passed <code>BundleContext</code> at the time of creation, this object will not be operational.
	 * @param realizedServices The initial set of services that are realized by this callee.
	 */
	protected ServiceCaller(BundleContext context) {
		waitingCalls = new Hashtable();
		readyResponses = new Hashtable();
		
		Activator.checkServiceBus();
		bus = (ServiceBus) context.getService(
				context.getServiceReference(ServiceBus.class.getName()));
		myID = bus.register(this);
	}

	
	/* (non-Javadoc)
	 * @see org.universAAL.middleware.sodapop.BusMember#busDyingOut(org.universAAL.middleware.sodapop.Bus)
	 */
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

	/**
	 * Unregisters this <code>ServiceCaller</code> from the bus.
	 */
	public void close(){
		bus.unregister(myID, this);
	}

	/**
	 * This abstract method is called for each member of the bus when the bus is being stopped.
	 */
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
		request.setProperty(ServiceRequest.PROP_uAAL_SERVICE_CALLER, myID);
		Activator.assessContentSerialization(request);
		Message reqMsg = new Message(MessageType.request, request);
		bus.sendMessage(myID, reqMsg);
		return reqMsg.getID();
	}
	
	/**
	 * A method used to instantly retrieve all services available to this caller.
	 * @return all available services.
	 */
	public ServiceProfile[] getAllServices() {
		return bus.getAllServices(myID);
	}
	
	/**
	 * A method used to retrieve a specified service, available to this <code>ServiceCaller</code>.
	 * @param s the desired service.
	 * @return the service that is available, or null if no such service is available.
	 */
	public ServiceProfile[] getMatchingService(Service s) {
		return bus.getMatchingService(myID, s);
	}
	
	/**
	 * A method used to retrieve a service available to this caller, that matches the specified keywords.
	 * @param keywords the keywords that should be matched by the service.
	 * @return the service that matches the keywords, or null if no such service is available.
	 */
	public ServiceProfile[] getMatchingService(String[] keywords) {
		return bus.getMatchingService(myID, keywords);
	}
	
	/**
	 * Adds an availability subscription, in other words a listener, to receive events about the availability of a specified service. 
	 * @param subscriber the object which will receive events when the appropriate service registers or unregisters.
	 * @param request the request that describes the desired service.
	 */
	public void addAvailabilitySubscription(
			AvailabilitySubscriber subscriber,
			ServiceRequest request) {
		bus.addAvailabilitySubscription(myID, subscriber, request);
	}

	/**
	 * Removes an availability subscription for this <code>Caller</code>, which was previously added using <code>addAvailabilitySubscription</code> method.
	 * @param subscriber the object which used to receive the events.
	 * @param requestURI the service that was being monitored.
	 */
	public void removeAvailabilitySubscription(
			AvailabilitySubscriber subscriber,
			String requestURI) {
		bus.removeAvailabilitySubscription(myID, subscriber, requestURI);
	}
}
