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
import org.universAAL.middleware.bus.member.Publisher;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.context.owl.ContextProvider;

/**
 * Provides the interface to be implemented by context publishers together with
 * shared code. Only instances of this class can publish context events. The
 * convention of the context bus regarding the registration parameters is the
 * following:
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
 */
public abstract class ContextPublisher extends Publisher {
    private ContextProvider providerInfo;

    /**
     * Creates a Context Publisher with the associated Context Provider
     * Information
     * 
     * @param context
     *            The context of the Bundle creating the Publisher
     * @param providerInfo
     *            The Information describing the Publisher
     */
    protected ContextPublisher(ModuleContext context,
	    ContextProvider providerInfo) {
	super(context, ContextBusImpl.getContextBusFetchParams());
	if (providerInfo == null || !providerInfo.isWellFormed())
	    throw new IllegalArgumentException(
		    "Missing the well-formed provider info!");

	this.providerInfo = providerInfo;
    }

    /**
     * Method to be called when the communication of the Publisher with the
     * Context Bus is lost.
     */
    public abstract void communicationChannelBroken();

    public final void busDyingOut(AbstractBus b) {
	if (b == theBus)
	    communicationChannelBroken();
    }

    /**
     * Forward a Context Event through the Context Bus
     * 
     * @param e
     *            The Context Event to forward
     * @throws NullPointerException
     *             if the event is null
     */
    public final void publish(ContextEvent e) {
	if (e != null) {
	    if (e.getProvider() == null && providerInfo != null)
		e.setProvider(providerInfo);
	    else if (providerInfo != e.getProvider())
		return;
	    if (AccessControl.INSTANCE.checkPermission(owner, getURI(), e))
		((ContextBus) theBus).brokerContextEvent(busResourceURI, e);
	}
    }

    public String getMyID() {
	return busResourceURI;
    }
}
