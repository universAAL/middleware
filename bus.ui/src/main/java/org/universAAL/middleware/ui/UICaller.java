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
package org.universAAL.middleware.ui;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Caller;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.ui.impl.UIBusImpl;

/**
 * Provides the interface to be implemented by applications together with shared
 * code. Only instances of this class can send UI requests. The convention of
 * the UI bus regarding the registration parameters is the following:
 * <ul>
 * <li>UICallers provide no registration params.</li>
 * <li>UIHandlers may pass an array of
 * {@link org.universAAL.middleware.ui.UIHandlerProfile}s as their initial
 * subscriptions and can always add new (and remove old) subscriptions
 * dynamically.</li>
 * </ul>
 * 
 * @author mtazari
 */
public abstract class UICaller implements Caller {
    private UIBus bus;
    private String myID;

    protected UICaller(ModuleContext context) {
	bus = (UIBus) context.getContainer().fetchSharedObject(context,
		UIBusImpl.busFetchParams);
	myID = bus.register(this);
    }

    public void abortDialog(String dialogID) {
	bus.abortDialog(myID, dialogID);
    }

    public void adaptationParametersChanged(UIRequest call, String changedProp) {
	if (this instanceof DialogManager)
	    bus.adaptationParametersChanged((DialogManager) this, call,
		    changedProp);
    }

    public final void busDyingOut(Bus b) {
	if (b == bus)
	    communicationChannelBroken();
    }

    public void close() {
	bus.unregister(myID, this);
    }

    public abstract void communicationChannelBroken();

    public abstract void dialogAborted(String dialogID);

    public void dialogSuspended(String dialogID) {
	if (this instanceof DialogManager)
	    bus.dialogSuspended((DialogManager) this, dialogID);
    }

    public final boolean eval(Message m) {
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.universAAL.middleware.sodapop.Caller#handleReply(org.universAAL.
     * middleware.sodapop.msg.Message)
     */
    public final void handleReply(Message m) {
	if (m != null && m.getContent() instanceof UIResponse)
	    handleUIResponse((UIResponse) m.getContent());
    }

    public abstract void handleUIResponse(UIResponse input);

    public void resumeDialog(String dialogID, Resource dialogData) {
	bus.resumeDialog(myID, dialogID, dialogData);
    }

    public final void sendUIRequest(UIRequest e) {
	if (e != null) {
	    bus.sendMessage(myID, e);
	}
    }
}
