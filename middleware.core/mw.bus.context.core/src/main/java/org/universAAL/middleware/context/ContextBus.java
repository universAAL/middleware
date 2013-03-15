/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
     * Allows the members of the context bus to provide registration parameters;
     * for the meaning of the provided "registration parameters" see the
     * explanation for the input parameter <code>registrParams</code>.
     * 
     * @param memberID
     *            The ID of the subscriber or publisher registered to the
     *            context bus
     * @param registrParams
     *            If the memberID refers to a context subscriber,
     *            <code>registrParams</code> will be interpreted as the patterns
     *            of the context events to which the subscriber wants to
     *            subscribe, and if the memberID refers to a context publisher,
     *            it will be interpreted as the patterns of the context events
     *            that the publisher will publish
     */
    public void addNewRegParams(String memberID,
	    ContextEventPattern[] registrParams);

    /**
     * Returns all provisions registered by all {@link ContextPublisher}s on all
     * instances of this bus in the current AAL Space. Only
     * {@link ContextPublisher}s are allowed to call this method, hence they
     * must provide their member-ID so that the bus can check this.
     */
    public ContextEventPattern[] getAllProvisions(String publisherID);

    /**
     * Removes registration parameters introduced previously through
     * {@link #addNewRegParams(String, ContextEventPattern[])}.
     */
    public void removeMatchingRegParams(String memberID,
	    ContextEventPattern[] oldRegistrParams);

    /**
     * Send a Context Event through the Context Bus
     * 
     * @param publisherID
     *            ID of the Publisher registered to the context bus
     * @param event
     *            Context Event to forward through the bus
     */
    public void brokerContextEvent(String publisherID, ContextEvent event);

    /**
     * Unregister a Context Publisher from the Context Bus
     * 
     * @param publisherID
     *            ID of the Publisher registered to the context bus
     * @param publisher
     *            The Publisher to unregister.
     */
    public void unregister(String publisherID, ContextPublisher publisher);

    /**
     * Unregister a Context Subscriber from the Context Bus
     * 
     * @param subscriberID
     *            ID of the Subscriber registered to the context bus
     * @param subscriber
     *            The Subscriber to unregister.
     */
    public void unregister(String subscriberID, ContextSubscriber subscriber);
}
