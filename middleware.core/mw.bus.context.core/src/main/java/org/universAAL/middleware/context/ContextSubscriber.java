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

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.Subscriber;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.impl.ContextBusImpl;

/**
 * Provides the interface to be implemented by context subscribers together with
 * shared code. Only instances of this class can subscribe for context events.
 * The convention of the context bus regarding the registration parameters is
 * the following:
 * <ul>
 * <li>ContextPublishers provide only at the registration time info about
 * themselves using
 * {@link org.universAAL.middleware.context.owl.ContextProvider}.</li>
 * <li>ContextSubscribers may pass an array of
 * {@link org.universAAL.middleware.context.ContextEventPattern}s as their
 * initial subscriptions and can always add new (and remove old) subscriptions
 * dynamically.</li>
 * </ul>
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public abstract class ContextSubscriber extends Subscriber {

    public ContextSubscriber(boolean dummy) {
		super(dummy);
		// PATCH This  is for Android port :(
	}

	/**
     * Creates a Context Subscriber and immediately registers a set of Context
     * Event Patterns for it, so it receives the matching events.
     * 
     * @param connectingModule
     *            The module context of the module creating the Publisher
     * @param initialSubscriptions
     *            Array of ContextEventPattern that are immediately registered
     *            for this Subscriber
     * @throws NullPointerException
     *             if newSubscriptions is null or one of the elements of that
     *             array is null
     */
    protected ContextSubscriber(ModuleContext connectingModule,
	    ContextEventPattern[] initialSubscriptions) {
	super(connectingModule, ContextBusImpl.getContextBusFetchParams());
	initialSubscriptions = AccessControl.INSTANCE.checkPermission(owner,
		getURI(), initialSubscriptions);
	addNewRegParams(initialSubscriptions);
    }

    /**
     * Registers more ContextEventPattern for this Subscriber in addition to
     * those that might have passed initially
     * 
     * @param newSubscriptions
     *            The additional array of ContextEventPattern
     * @throws NullPointerException
     *             if newSubscriptions is null or one of the elements of that
     *             array is null
     */
    protected final void addNewRegParams(ContextEventPattern[] newSubscriptions) {
	newSubscriptions = AccessControl.INSTANCE.checkPermission(owner,
		getURI(), newSubscriptions);
	((ContextBus) theBus).addNewRegParams(busResourceURI, newSubscriptions);
    }

    /**
     * Unregisters a set of ContextEventPattern that had been previously
     * registered for this Subscriber. The Subscriber will no longer receive
     * Events matching these Patterns.
     * 
     * @param oldSubscriptions
     */
    protected final void removeMatchingRegParams(
	    ContextEventPattern[] oldSubscriptions) {
	((ContextBus) theBus).removeMatchingRegParams(busResourceURI,
		oldSubscriptions);
    }

    /**
     * Method to be called when the communication of the Subsccriber with the
     * Context Bus is lost.
     */
    public abstract void communicationChannelBroken();

    public final void busDyingOut(AbstractBus b) {
	if (b == theBus)
	    communicationChannelBroken();
    }

    /**
     * Returns all provisions registered by all {@link ContextPublisher}s on all
     * instances of context bus in the current AAL Space.
     */
    public ContextEventPattern[] getAllProvisions() {
	return ((ContextBus) theBus).getAllProvisions(busResourceURI);
    }

    /**
     * Method to be called when an Event forwarded in the Context Bus matches
     * one of the Patterns registered by this Subscriber.
     * 
     * @param event
     *            The Context Event that matched the registered Patterns
     * @throws NullPointerException
     *             if the event is null
     */
    public abstract void handleContextEvent(ContextEvent event);

    public final void handleEvent(BusMessage m) {
	if (m.getContent() instanceof ContextEvent) {
	    ContextEvent ce = (ContextEvent) (m.getContent());
	    if (AccessControl.INSTANCE.checkPermission(owner, getURI(), ce)) {
		LogUtils.logInfo(
			owner,
			ContextSubscriber.class,
			"handleEvent",
			new Object[] { busResourceURI,
				" received context event:\n",
				m.getContentAsString() }, null);
		handleContextEvent((ContextEvent) m.getContent());
	    }
	}
    }

    public String getMyID() {
	return busResourceURI;
    }
}
