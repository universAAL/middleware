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

    /** The bus. */
    private UIBus bus;

    /** The my id. */
    private String myID;

    /**
     * Instantiates a new uI caller.
     * 
     * @param context
     *            the module context
     */
    protected UICaller(ModuleContext context) {
	bus = (UIBus) context.getContainer().fetchSharedObject(context,
		UIBusImpl.busFetchParams);
	myID = bus.register(this);
    }

    /**
     * Abort dialog.
     * 
     * @param dialogID
     *            the dialog id
     */
    public void abortDialog(String dialogID) {
	bus.abortDialog(myID, dialogID);
    }

    /**
     * Adaptation parameters changed.
     * 
     * @param call
     *            the call
     * @param changedProp
     *            the changed prop
     */
    public void adaptationParametersChanged(UIRequest call, String changedProp) {
	if (this instanceof DialogManager)
	    bus.adaptationParametersChanged((DialogManager) this, call,
		    changedProp);
    }

    /**
     * Bus dying out.
     * 
     * @param b
     *            the bus
     * @see org.universAAL.middleware.sodapop.BusMember#busDyingOut(Bus)
     */
    public final void busDyingOut(Bus b) {
	if (b == bus)
	    communicationChannelBroken();
    }

    /**
     * Unregisters the UI Caller from the UI Bus.
     */
    public void close() {
	bus.unregister(myID, this);
    }

    /**
     * Method to be called when the communication of the UI Caller with the UI
     * Bus is lost.
     */
    public abstract void communicationChannelBroken();

    /**
     * Dialog aborted.
     * 
     * @param dialogID
     *            the dialog id
     */
    public abstract void dialogAborted(String dialogID);

    /**
     * Dialog suspended.
     * 
     * @param dialogID
     *            the dialog id
     */
    public void dialogSuspended(String dialogID) {
	if (this instanceof DialogManager)
	    bus.dialogSuspended((DialogManager) this, dialogID);
    }

    /**
     * 
     * @param m
     *            the message
     * @return true, if successful
     * @see org.universAAL.middleware.sodapop.Callee#eval(Message)
     */
    public final boolean eval(Message m) {
	return false;
    }

    /**
     * Handle reply.
     * 
     * @param m
     *            the message
     * @see org.universAAL.middleware.sodapop.Caller#handleReply(org.universAAL.
     *      middleware.sodapop.msg.Message)
     */
    public final void handleReply(Message m) {
	if (m != null && m.getContent() instanceof UIResponse)
	    handleUIResponse((UIResponse) m.getContent());
    }

    /**
     * Handle ui response.
     * 
     * @param input
     *            the input
     */
    public abstract void handleUIResponse(UIResponse input);

    /**
     * Resume dialog.
     * 
     * @param dialogID
     *            the dialog id
     * @param dialogData
     *            the dialog data
     */
    public void resumeDialog(String dialogID, Resource dialogData) {
	bus.resumeDialog(myID, dialogID, dialogData);
    }

    /**
     * Send ui request.
     * 
     * @param e
     *            the e
     */
    public final void sendUIRequest(UIRequest e) {
	if (e != null) {
	    bus.sendMessage(myID, e);
	}
    }
}
