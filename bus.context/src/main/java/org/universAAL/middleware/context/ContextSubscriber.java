/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut f�r Graphische Datenverarbeitung 
	
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

import org.osgi.framework.BundleContext;
import org.universAAL.middleware.context.impl.Activator;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Subscriber;
import org.universAAL.middleware.sodapop.msg.Message;

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
public abstract class ContextSubscriber implements Subscriber {
    private ContextBus bus;
    private String myID;

    protected ContextSubscriber(BundleContext context,
	    ContextEventPattern[] initialSubscriptions) {
	Activator.checkContextBus();
	bus = (ContextBus) context.getService(context
		.getServiceReference(ContextBus.class.getName()));
	myID = bus.register(this, initialSubscriptions);
    }

    protected final void addNewRegParams(ContextEventPattern[] newSubscriptions) {
	bus.addNewRegParams(myID, newSubscriptions);
    }

    protected final void removeMatchingRegParams(
	    ContextEventPattern[] oldSubscriptions) {
	bus.removeMatchingRegParams(myID, oldSubscriptions);
    }

    public abstract void communicationChannelBroken();

    public final void busDyingOut(Bus b) {
	if (b == bus)
	    communicationChannelBroken();
    }

    public final boolean eval(Message m) {
	return false;
    }

    public abstract void handleContextEvent(ContextEvent event);

    public final void handleEvent(Message m) {
	if (m.getContent() instanceof ContextEvent)
	    handleContextEvent((ContextEvent) m.getContent());
    }

    public final void handleReply(Message m) {
    }

    public void close() {
	bus.unregister(myID, this);
    }
}
