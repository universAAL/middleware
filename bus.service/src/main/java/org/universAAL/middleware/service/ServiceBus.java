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

import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.sodapop.msg.Message;

/**
 * The service bus is a call-based bus, i.e., the <code>ServiceCaller</code>
 * members who post a message to this bus normally have a service request and
 * would like to receive a response in return, especially if the service result
 * also includes the provision of specific info. Even if no specific info is
 * expected from the <code>ServiceCallee</code> member, at least a hint about
 * the status of the provision of the service is required, e.g. an
 * acknowledgment stating that the request could be forwarded to an appropriate
 * <code>ServiceCallee</code>, or an error message stating that no appropriate
 * service realization could be found. This interface is available as an OSGi
 * service at the OSGi framework. it is implicitly used by the
 * <code>ServiceCaller</code>-s and <code>ServiceCallee</code>-s that are
 * created within the same OSGi bundle context.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public interface ServiceBus {

    /**
     * Adds an availability subscription, in other words a listener, to receive
     * events about the availability of a specified service.
     * 
     * @param callerID
     *            the ID of the caller to which the desired service should be
     *            relevant.
     * @param subscriber
     *            the object which will receive events when the appropriate
     *            service registers or unregisters.
     * @param request
     *            the request that describes the desired service.
     */
    public void addAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, ServiceRequest request);

    /**
     * Registers additional services to be provided by the ServiceCalee with the
     * specified ID.
     * 
     * @param calleeID
     *            the ID of the ServiceCalee to register additional services
     *            for.
     * @param realizedServices
     *            the new services.
     */
    public void addNewRegParams(String calleeID,
	    ServiceProfile[] realizedServices);

    /**
     * A method used to instantly retrieve all services for a specified caller.
     * 
     * @param callerID
     *            the ID of the caller that the services must be relevant to.
     * @return all available services.
     */
    public ServiceProfile[] getAllServices(String callerID);

    /**
     * A method used to retrieve a specified service that is available for the
     * specified caller.
     * 
     * @param callerID
     *            the ID of the caller that the services must be relevant to.
     * @param s
     *            the desired service.
     * @return the service that is available, or null if no such service is
     *         available.
     */

    public ServiceProfile[] getMatchingService(String callerID, Service s);

    /**
     * A method used to retrieve an available service for the specified caller,
     * that matches the specified keywords.
     * 
     * @param callerID
     *            the ID of the caller that the services must be relevant to.
     * @param keywords
     *            the keywords that should be matched by the service.
     * @return the service that matches the keywords, or null if no such service
     *         is available.
     */
    public ServiceProfile[] getMatchingService(String callerID,
	    String[] keywords);

    /**
     * Registers a service caller to the bus.
     * 
     * @param caller
     *            the caller to be registered.
     * @return the ID by which the caller was registered.
     */
    public String register(ServiceCaller caller);

    /**
     * Registers a service callee to the bus.
     * 
     * @param callee
     *            the callee to be registered.
     * @param realizedServices
     *            the list of the services that are initially realized by this
     *            callee.
     * @return the ID by which the callee was registered.
     */
    public String register(ServiceCallee callee,
	    ServiceProfile[] realizedServices);

    /**
     * Removes an availability subscription from the bus, which was previously
     * added using <code>addAvailabilitySubscription</code> method.
     * 
     * @param callerID
     *            the ID of the caller to which the listener was previously
     *            attached.
     * @param subscriber
     *            the object which used to receive the events.
     * @param requestURI
     *            the service that was being monitored.
     */
    public void removeAvailabilitySubscription(String callerID,
	    AvailabilitySubscriber subscriber, String requestURI);

    /**
     * Removes specified services that were previously provided by the
     * ServiceCalee with the specified ID.
     * 
     * @param calleeID
     *            the ID of the ServiceCalee to remove the services from.
     * @param realizedServices
     *            the services that need to be removed.
     */
    public void removeMatchingRegParams(String calleeID,
	    ServiceProfile[] realizedServices);

    /**
     * Sends a request to the bus. Any matching callees will be invoked.
     * 
     * @param callerID
     *            the ID of the caller that is sending the request.
     * @param request
     *            the actual request message.
     */
    public void sendMessage(String callerID, Message request);

    /**
     * Sends a response to the bus which will be delivered to the caller who
     * initiated the initial request.
     * 
     * @param calleeID
     *            the ID of the service callee which processed the request.
     * @param response
     *            the actual response message.
     */
    public void sendReply(String calleeID, Message response);

    /**
     * Unregisters a service caller from the bus.
     * 
     * @param callerID
     *            the ID of the caller to be unregistered.
     * @param caller
     *            the ServiceCaller object to be unregistered.
     */
    public void unregister(String callerID, ServiceCaller caller);

    /**
     * Unregisters a service callee from the bus.
     * 
     * @param calleeID
     *            the ID of the callee to be unregistered.
     * @param callee
     *            the ServiceCallee object to be unregistered.
     */
    public void unregister(String calleeID, ServiceCallee callee);
}
