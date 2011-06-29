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
package org.universAAL.middleware.input;

import org.osgi.framework.BundleContext;
import org.universAAL.middleware.io.Activator;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Subscriber;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.util.LogUtils;

/**
 * Provides the interface to be implemented by input subscribers together with
 * shared code. Only instances of this class can subscribe for input events. The
 * convention of the input bus regarding subscription is that dialog handlers
 * subscribe with the dialog id that they get from the output bus in the course
 * of publishing output events, using {@link #addNewRegParams(String)}. The
 * subscription will then be automatically removed after the corresponding input
 * event has been passed to the subscriber.
 * 
 * @author mtazari
 * 
 */
public abstract class InputSubscriber implements Subscriber {
    private InputBus bus;
    private String myID, localID;

    protected InputSubscriber(BundleContext context) {
	Activator.checkInputBus();
	bus = (InputBus) context.getService(context
		.getServiceReference(InputBus.class.getName()));
	myID = bus.register(this);
	localID = myID.substring(myID.lastIndexOf('#') + 1);
    }

    public abstract void dialogAborted(String dialogID);

    protected final void addNewRegParams(String dialogID) {
	bus.addNewRegParams(myID, dialogID);
    }

    protected final void removeMatchingRegParams(String dialogID) {
	bus.removeMatchingRegParams(myID, dialogID);
    }

    public abstract void communicationChannelBroken();

    /**
     * @see org.universAAL.middleware.sodapop.BusMember#busDyingOut(Bus)
     */
    public final void busDyingOut(Bus b) {
	if (b == bus)
	    communicationChannelBroken();
    }

    /**
     * @see org.universAAL.middleware.sodapop.Subscriber#eval(Message)
     */
    public final boolean eval(Message m) {
	return false;
    }

    public abstract void handleInputEvent(InputEvent event);

    /**
     * @see org.universAAL.middleware.sodapop.Subscriber#handleEvent(Message)
     */
    public final void handleEvent(Message m) {
	if (m.getContent() instanceof InputEvent) {
	    LogUtils.logInfo(Activator.logger, "InputSubscriber",
		    "handleEvent",
		    new Object[] { localID, " received Input event:\n",
			    m.getContentAsString() }, null);
	    handleInputEvent((InputEvent) m.getContent());
	}
    }

    /**
     * @see org.universAAL.middleware.sodapop.Caller#handleReply(Message)
     */
    public final void handleReply(Message m) {
    }

    public void close() {
	bus.unregister(myID, this);
    }
}
