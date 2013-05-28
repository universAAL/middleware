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
package org.universAAL.middleware.ui;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.member.Caller;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.impl.UIBusImpl;

/**
 * Provides the interface to be implemented by applications together with shared
 * code. Only instances of this class can send {@link UIRequest}s. The
 * convention of the {@link IUIBus} regarding the registration parameters is the
 * following:
 * <ul>
 * <li>{@link UICaller}s provide no registration parameters</li>
 * <li>{@link UIHandler}s may pass an array of
 * {@link org.universAAL.middleware.ui.UIHandlerProfile}s as their initial
 * subscriptions and can always add new (and remove old) subscriptions
 * dynamically.</li>
 * </ul>
 * 
 * @author mtazari
 * @author eandgrg
 */

public abstract class UICaller extends Caller {

    /**
     * Instantiates a new {@link UICaller}.
     * 
     * @param context
     *            the module context
     */
    protected UICaller(ModuleContext context) {
	super(context, UIBusImpl.getUIBusFetchParams());
	if (this instanceof IDialogManager)
	    ((UIBusImpl) theBus).setDialogManager((IDialogManager) this);
    }

    /**
     * Abort dialog.
     * 
     * @param dialogID
     *            the dialog id
     */
    public void abortDialog(String dialogID) {
	((IUIBus) theBus).abortDialog(busResourceURI, dialogID);
    }

    /**
     * Adaptation parameters changed.
     * 
     * @param call
     *            the call
     * @param changedProp
     *            the changed prop
     */
    public final void adaptationParametersChanged(UIRequest call, String changedProp) {
	if (this instanceof IDialogManager)
	    ((IUIBus) theBus).adaptationParametersChanged((IDialogManager) this,
		    call, changedProp);
    }

    /**
     * @see BusMember#busDyingOut(AbstractBus)
     */
    public final void busDyingOut(AbstractBus bus) {
	if (bus == theBus)
	    communicationChannelBroken();
    }

    /**
     * Unregisters the {@link UICaller} from the {@link IUIBus}.
     */
    public void close() {
	theBus.unregister(busResourceURI, this);
    }

    /**
     * Method to be called when the communication of the {@link UICaller} with
     * the UI Bus is lost.
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
    public final void dialogSuspended(String dialogID) {
	if (this instanceof IDialogManager)
	    ((IUIBus) theBus).dialogSuspended((IDialogManager) this, dialogID);
    }

    /**
     * Handle reply.
     * 
     * @param msg
     *            the {@link BusMessage}
     */
    public final void handleReply(BusMessage msg) {
	if (msg != null && msg.getContent() instanceof UIResponse)
	    handleUIResponse((UIResponse) msg.getContent());
    }

    /**
     * Handle {@link UIResponse}.
     * 
     * @param uiResponse
     *            the {@link UIResponse}
     */
    public abstract void handleUIResponse(UIResponse uiResponse);

    /**
     * Resume dialog.
     * 
     * @param dialogID
     *            the dialog id
     * @param dialogData
     *            the dialog data
     */
    public void resumeDialog(String dialogID, Resource dialogData) {
	((IUIBus) theBus).resumeDialog(busResourceURI, dialogID, dialogData);
    }

    /**
     * Sends {@link UIRequest}.
     * 
     * @param uiRequest
     *            the {@link UIRequest}
     */
    public final void sendUIRequest(UIRequest uiRequest) {
	if (uiRequest != null) {
	    ((IUIBus) theBus).brokerUIRequest(busResourceURI, uiRequest);
	}
    }

    /**
     * Id with which the {@link UICaller} is registered in the {@link IUIBus}
     * 
     * @return {@link UICaller} ID
     */
    public String getMyID() {
	return busResourceURI;
    }
}
