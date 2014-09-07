/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
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

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.member.Caller;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * /** This is an abstract class that the service caller members of the service
 * bus must derive from. Any <code>ServiceCaller</code> must incorporate the
 * logic of handling service responses into the implementation of the abstract
 * method <code>handleResponse(String, ServiceResponse)</code>.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public abstract class ServiceCaller extends Caller {
    private Hashtable waitingCalls;
    private Hashtable readyResponses;

    /**
     * The default constructor for this class.
     * 
     * @param context
     *            The module context where the {@link ServiceBus} is registered.
     *            Note that if no service bus is registered within the passed
     *            <code>ModuleContext</code> at the time of creation, this
     *            object will not be operational.
     */
    protected ServiceCaller(ModuleContext context) {
	super(context, ServiceBusImpl.getServiceBusFetchParams());
	waitingCalls = new Hashtable();
	readyResponses = new Hashtable();
    }

    /**
     * @see BusMember#busDyingOut(AbstractBus)
     */
    public final void busDyingOut(AbstractBus b) {
	if (b == theBus)
	    communicationChannelBroken();
    }

    /**
     * The "normal" (synchronous) way of calling a service. Use
     * {@link #sendRequest(ServiceRequest)}, if you want to handle the response
     * asynchronously in another thread.
     * 
     * @throws NullPointerException
     *             if request is null
     */
    public ServiceResponse call(ServiceRequest request) {
	if (AccessControl.INSTANCE.checkPermission(owner, getURI(), request)) {
	    ServiceResponse sr = null;
	    synchronized (waitingCalls) {
		String callID = sendRequest(request);
		waitingCalls.put(callID, this);
		while (sr == null) {
		    try {
			waitingCalls.wait();
			sr = (ServiceResponse) readyResponses.remove(callID);
		    } catch (InterruptedException e) {
		    }
		}
	    }
	    return sr;
	} else {
	    return new ServiceResponse(CallStatus.denied);
	}
    }

    /**
     * This method is a way of calling a service using Turtle Strings as input.
     * Turtle Strings are converted to Service Requests.
     * 
     * @param request
     *            the Turtle String which will be converted into
     *            <code>ServiceRequest</code>
     * @return the expected Service Response
     */
    public final ServiceResponse call(String request) {
	Object o = BusMessage.deserializeAsContent(request);
	return (o instanceof ServiceRequest) ? call((ServiceRequest) o) : null;
    }

    /**
     * This abstract method is called for each member of the bus when the bus is
     * being stopped.
     */
    public abstract void communicationChannelBroken();

    public final void handleReply(BusMessage m) {
	if (m.getType() == MessageType.reply
		&& (m.getContent() instanceof ServiceResponse)) {
	    LogUtils.logDebug(
		    owner,
		    ServiceCaller.class,
		    "handleReply",
		    new Object[] { busResourceURI,
			    " received service response:\n",
			    m.getContentAsString() }, null);
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
     * Will be called automatically in a new thread whenever the response
     * corresponding to a previous call to {@link #sendRequest(ServiceRequest)}
     * is ready.
     * 
     * @param reqID
     *            the ID returned by the previous call to
     *            {@link #sendRequest(ServiceRequest)}.
     * @param response
     *            the expected response.
     */
    public abstract void handleResponse(String reqID, ServiceResponse response);

    /**
     * To be used if the caller would like to handle the reply asynchronously
     * within the method {@link #handleResponse(String, ServiceResponse)}, which
     * will automatically be called in another thread when the response is
     * ready.
     * 
     * @return a unique ID for this request that will be passed to the method
     *         {@link #handleResponse(String, ServiceResponse)} for an
     *         unambiguous mapping of responses to requests. Returns null, if
     *         this caller does not have the permission for the given request
     * @throws NullPointerException
     *             if the request is null
     */
    public final String sendRequest(ServiceRequest request) {
	request.setProperty(ServiceRequest.PROP_uAAL_SERVICE_CALLER,
		busResourceURI);
	if (AccessControl.INSTANCE.checkPermission(owner, getURI(), request)) {
	    BusMessage reqMsg = new BusMessage(MessageType.request, request,
		    theBus);
	    ((ServiceBus) theBus).brokerRequest(busResourceURI, reqMsg);
	    return reqMsg.getID();
	} else {
	    return null;
	}
    }

    /**
     * A method used to instantly retrieve all services available to this
     * caller.
     * 
     * @return all available services.
     */
    public ServiceProfile[] getAllServices() {
	return ((ServiceBus) theBus).getAllServices(busResourceURI);
    }

    /**
     * A method used to retrieve a specified service, available to this
     * <code>ServiceCaller</code>.
     * 
     * @param s
     *            the desired service.
     * @return the service that is available, or null if no such service is
     *         available.
     */
    public ServiceProfile[] getMatchingService(Service s) {
	return ((ServiceBus) theBus).getMatchingServices(busResourceURI, s);
    }

    /**
     * A method used to retrieve a specified service, available to this
     * <code>ServiceCaller</code>.
     * 
     * @param serviceClassURI
     *            the class URI of the desired service.
     * @return the service that is available, or null if no such service is
     *         available.
     */
    public ServiceProfile[] getMatchingService(String serviceClassURI) {
	return ((ServiceBus) theBus).getMatchingServices(busResourceURI,
		serviceClassURI);
    }

    /**
     * A method used to retrieve a service available to this caller, that
     * matches the specified keywords.
     * 
     * @param keywords
     *            the keywords that should be matched by the service.
     * @return the service that matches the keywords, or null if no such service
     *         is available.
     */
    public ServiceProfile[] getMatchingService(String[] keywords) {
	return ((ServiceBus) theBus).getMatchingServices(busResourceURI,
		keywords);
    }

    /**
     * Adds an availability subscription, in other words a listener, to receive
     * events about the availability of a specified service.
     * 
     * @param subscriber
     *            the object which will receive events when the appropriate
     *            service registers or unregisters.
     * @param request
     *            the request that describes the desired service.
     */
    public void addAvailabilitySubscription(AvailabilitySubscriber subscriber,
	    ServiceRequest request) {
	((ServiceBus) theBus).addAvailabilitySubscription(busResourceURI,
		subscriber, request);
    }

    /**
     * Removes an availability subscription for this <code>Caller</code>, which
     * was previously added using <code>addAvailabilitySubscription</code>
     * method.
     * 
     * @param subscriber
     *            the object which used to receive the events.
     * @param requestURI
     *            the service that was being monitored.
     */
    public void removeAvailabilitySubscription(
	    AvailabilitySubscriber subscriber, String requestURI) {
	((ServiceBus) theBus).removeAvailabilitySubscription(busResourceURI,
		subscriber, requestURI);
    }

    public String getMyID() {
	return busResourceURI;
    }
}
