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

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.output.impl.OutputBusImpl;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Publisher;
import org.universAAL.middleware.sodapop.msg.Message;

/**
 * Provides the interface to be implemented by output publishers together with
 * shared code. Only instances of this class can publish output events. The
 * convention of the output bus regarding the registration parameters is the
 * following:
 * <ul>
 * <li>OutputPublishers provide no registration params.</li>
 * <li>OutputSubscribers may pass an array of
 * {@link org.universAAL.middleware.output.OutputEventPattern}s as their initial
 * subscriptions and can always add new (and remove old) subscriptions
 * dynamically.</li>
 * </ul>
 * 
 * @author mtazari
 */
public abstract class OutputPublisher implements Publisher {
    private OutputBus bus;
    private String myID;

    protected OutputPublisher(ModuleContext context) {
	bus = (OutputBus) context.getContainer().fetchSharedObject(context,
		OutputBusImpl.busFetchParams);
	myID = bus.register(this);
    }

    public void abortDialog(String dialogID) {
	bus.abortDialog(myID, dialogID);
    }

    public void adaptationParametersChanged(OutputEvent oe, String changedProp) {
	if (this instanceof DialogManager)
	    bus.adaptationParametersChanged((DialogManager) this, oe,
		    changedProp);
    }

    public abstract void communicationChannelBroken();

    public void dialogSuspended(String dialogID) {
	if (this instanceof DialogManager)
	    bus.dialogSuspended((DialogManager) this, dialogID);
    }

    public final boolean eval(Message m) {
	return false;
    }

    public final void handleRequest(Message m) {
    }

    public final void busDyingOut(Bus b) {
	if (b == bus)
	    communicationChannelBroken();
    }

    public final void publish(OutputEvent e) {
	if (e != null) {
	    bus.sendMessage(myID, e);
	}
    }

    public void resumeDialog(String dialogID, Resource dialogData) {
	bus.resumeDialog(myID, dialogID, dialogData);
    }

    public void close() {
	bus.unregister(myID, this);
    }
}
