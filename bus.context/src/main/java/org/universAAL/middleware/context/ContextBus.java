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
package org.universAAL.middleware.context;

/**
 * Interface for classes that wish to implement a Context Bus based on uAAL
 * buses
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public interface ContextBus {
    /**
     * Allows a Context Subscriber to register to events in the bus that match
     * the given patterns
     * 
     * @param subscriberID
     *            The ID of the subscriber, received when registered to the bus
     * @param newSubscriptions
     *            An array of ConntextEventPattern containing the restrictions
     *            on Context Events that define the patterns to register to
     */
    public void addNewRegParams(String subscriberID,
	    ContextEventPattern[] newSubscriptions);

    /**
     * Register a ContextPublisher into the Context Bus
     * 
     * @param publisher
     *            the Context Publisher to register
     * @return The ID of the Context Publisher within the bus
     */
    public String register(ContextPublisher publisher);

    /**
     * Register a Context Subscriber into the Context Bus and immediately register
     * for certain patterns of Context Events
     * 
     * @param subscriber
     *            the Context Subscriber to register
     * @param initialSubscriptions
     *            An array of ConntextEventPattern containing the restrictions
     *            on Context Events that define the patterns to register to
     * @return The ID of the Context Publisher within the bus
     */
    public String register(ContextSubscriber subscriber,
	    ContextEventPattern[] initialSubscriptions);

    /**
     * Remove the patterns of Context Events that a Context Subscriber is
     * interested in, so it no longer receives Events matching them
     * 
     * @param subscriberID
     *            ID of the Subscriber, received when registered
     * @param oldSubscriptions
     *            An array of ConntextEventPattern containing the restrictions
     *            on Context Events that define the patterns to unregister. Must
     *            be equal to those registered at first.
     */
    public void removeMatchingRegParams(String subscriberID,
	    ContextEventPattern[] oldSubscriptions);

    /**
     * Send a Context Event through the Context Bus
     * 
     * @param publisherID
     *            ID of the Publisher, received when registered.
     * @param event
     *            Context Event to forward through the bus
     */
    public void sendMessage(String publisherID, ContextEvent event);

    /**
     * Unregister a Context Publisher from the Context Bus
     * 
     * @param publisherID
     *            ID of the Publisher, received when registered.
     * @param publisher
     *            The Publisher to unregister.
     */
    public void unregister(String publisherID, ContextPublisher publisher);

    /**
     * Unregister a Context Subscriber from the Context Bus
     * 
     * @param subscriberID
     *            ID of the Subscriber, received when registered.
     * @param subscriber
     *            The Subscriber to unregister.
     */
    public void unregister(String subscriberID, ContextSubscriber subscriber);
}
