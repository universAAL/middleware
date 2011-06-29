/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either.ss or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.output;

import org.osgi.framework.BundleContext;
import org.universAAL.middleware.io.Activator;
import org.universAAL.middleware.io.rdf.Submit;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Subscriber;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.util.LogUtils;
import org.universAAL.middleware.util.StringUtils;

/**
 * Provides the interface to be implemented by output subscribers together with
 * shared code. Only instances of this class can subscribe for output events.
 * The convention of the context bus regarding the registration parameters is
 * the following:
 * <ul>
 * <li>ContextPublishers provide only at the registration time info about
 * themselves using
 * {@link org.universAAL.middleware.owl.context.ContextProvider}.</li>
 * <li>ContextSubscribers may pass an array of
 * {@link org.universAAL.middleware.context.ContextEventPattern}s as their
 * initial subscriptions and can always add new (and remove old) subscriptions
 * dynamically.</li>
 * </ul>
 * 
 * @author mtazari
 * 
 */
public abstract class OutputSubscriber implements Subscriber {
    private OutputBus bus;
    private String myID;

    protected OutputSubscriber(BundleContext context,
	    OutputEventPattern initialSubscription) {
	Activator.checkOutputBus();
	bus = (OutputBus) context.getService(context
		.getServiceReference(OutputBus.class.getName()));
	myID = bus.register(this, initialSubscription);
    }

    public abstract void adaptationParametersChanged(String dialogID,
	    String changedProp, Object newVal);

    protected final void addNewRegParams(OutputEventPattern newSubscription) {
	bus.addNewRegParams(myID, newSubscription);
    }

    public final void busDyingOut(Bus b) {
	if (b == bus)
	    communicationChannelBroken();
    }

    public void close() {
	bus.unregister(myID, this);
    }

    public abstract void communicationChannelBroken();

    public abstract Resource cutDialog(String dialogID);

    public final void dialogFinished(Submit submission, boolean poppedMessage) {
	bus.dialogFinished(myID, submission, poppedMessage);
    }

    public final boolean eval(Message m) {
	return false;
    }

    public final void handleEvent(Message m) {
	if (m.getContent() instanceof OutputEvent) {
	    LogUtils
		    .logInfo(Activator.logger, "OutputSubscriber",
			    "handleEvent", new Object[] {
				    StringUtils.deriveLabel(myID),
				    " received output event:\n",
				    m.getContentAsString() }, null);
	    handleOutputEvent((OutputEvent) m.getContent());
	}
    }

    public abstract void handleOutputEvent(OutputEvent event);

    public final void handleReply(Message m) {
    }

    protected final void removeMatchingRegParams(
	    OutputEventPattern oldSubscription) {
	bus.removeMatchingRegParams(myID, oldSubscription);
    }
}
