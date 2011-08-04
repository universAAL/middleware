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

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.input.impl.InputBusImpl;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Publisher;
import org.universAAL.middleware.sodapop.msg.Message;

/**
 * Provides the interface to be implemented by input publishers together with
 * shared code. Only instances of this class can publish input events.
 * 
 * @author mtazari
 */
public abstract class InputPublisher implements Publisher {
    private InputBus bus;
    private String myID;

    protected InputPublisher(ModuleContext context) {
	bus = (InputBus) context.getContainer().fetchSharedObject(context,
		InputBusImpl.busFetchParams);
	myID = bus.register(this);
    }

    public abstract void communicationChannelBroken();

    /**
     * @see org.universAAL.middleware.sodapop.Callee#eval(Message)
     */
    public final boolean eval(Message m) {
	return false;
    }

    /**
     * @see org.universAAL.middleware.sodapop.Callee#handleRequest(Message)
     */
    public final void handleRequest(Message m) {
    }

    /**
     * if the Input Bus is dying out, the communication channel for Input
     * Publisher is broken.
     */
    public final void busDyingOut(Bus b) {
	if (b == bus)
	    communicationChannelBroken();
    }

    /**
     * Publishes input event on the Input Bus.
     * 
     * @param e
     *            Input Event
     */
    public final void publish(InputEvent e) {
	if (e != null) {
	    bus.sendMessage(myID, e);
	}
    }

    /**
     * Closes this Input Publisher which means that it is being unregistered on
     * the Input Bus.
     */
    public void close() {
	bus.unregister(myID, this);
    }
}
