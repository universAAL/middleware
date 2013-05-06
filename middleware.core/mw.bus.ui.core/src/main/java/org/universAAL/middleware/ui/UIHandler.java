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

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.member.Callee;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.impl.UIBusImpl;

/**
 * Provides the interface to be implemented by {@link UIHandler}s together with
 * shared code. Only instances of this class can handle {@link UIRequest}s. The
 * convention of the {@link IUIBus} regarding the registration parameters is the
 * following:
 * <ul>
 * <li>{@link UIHandler}s provide only at the registration time info about
 * themselves</li>
 * </ul>
 * 
 * @author mtazari
 * @author eandgrg
 * 
 */
public abstract class UIHandler extends Callee {

    private List<UIHandlerProfile> realizedHandlerProfiles;

    /**
     * Instantiates a new {@link UIHandler}.
     * 
     * @param context
     *            the context
     * @param initialSubscription
     *            the initial subscription
     */
    protected UIHandler(ModuleContext context,
	    UIHandlerProfile initialSubscription) {
	super(context, UIBusImpl.getUIBusFetchParams());

	this.realizedHandlerProfiles = new ArrayList<UIHandlerProfile>();
	if (initialSubscription != null) {
	    this.realizedHandlerProfiles.add(initialSubscription);
	    ((IUIBus) theBus).addNewProfile(busResourceURI, initialSubscription);
	}
    }

    /**
     * Instantiates a new {@link UIHandler}.
     * 
     * @param context
     *            the context
     * @param initialSubscription
     *            the initial subscription
     */
    protected UIHandler(ModuleContext context,
	    UIHandlerProfile[] initialSubscriptions) {
	super(context, UIBusImpl.getUIBusFetchParams());

	this.realizedHandlerProfiles = new ArrayList<UIHandlerProfile>();
	if (initialSubscriptions != null) {
	    for (UIHandlerProfile profile : initialSubscriptions) {
		this.realizedHandlerProfiles.add(profile);
		((IUIBus) theBus).addNewProfile(busResourceURI, profile);
	    }
	}
    }

    /**
     * Adaptation parameters changed.
     * 
     * @param dialogID
     *            the dialog id
     * @param changedProp
     *            the changed prop
     * @param newVal
     *            the new val
     */
    public abstract void adaptationParametersChanged(String dialogID,
	    String changedProp, Object newVal);

    /**
     * Adds the new reg params.
     * 
     * @param newSubscription
     *            the new subscription
     */
    public final void addNewRegParams(UIHandlerProfile newSubscription) {
	((IUIBus) theBus).addNewProfile(busResourceURI, newSubscription);
	this.realizedHandlerProfiles.add(newSubscription);
    }

    /**
     * @see BusMember#busDyingOut(AbstractBus)
     */
    public final void busDyingOut(AbstractBus b) {
	if (b == theBus)
	    communicationChannelBroken();
    }

    /**
     * Unregisters the {@link UIHandler} from the {@link IUIBus}.
     */
    public void close() {
	theBus.unregister(busResourceURI, this);
    }

    /**
     * Method to be called when the communication of the {@link UIHandler} with
     * the {@link IUIBus} is lost.
     */
    public abstract void communicationChannelBroken();

    /**
     * Cut dialog.
     * 
     * @param dialogID
     *            the dialog id
     * @return the resource
     */
    public abstract Resource cutDialog(String dialogID);

    /**
     * Dialog finished.
     * 
     * @param uiResponse
     *            the {@link UIResponse}
     */
    public final void dialogFinished(UIResponse uiResponse) {
	((IUIBus) theBus).dialogFinished(busResourceURI, uiResponse);
    }

    /**
     * Handle request.
     * 
     * @param msg
     *            the message
     */
    public final void handleRequest(BusMessage msg) {
	if (msg.getContent() instanceof UIRequest) {
	    LogUtils.logInfo(owner, UIHandler.class, "handleRequest",
		    new Object[] { busResourceURI, " received UI request:\n",
			    msg.getContentAsString() }, null);
	    handleUICall((UIRequest) msg.getContent());
	}
    }

    /**
     * Handle ui call ({@link UIRequest}).
     * 
     * @param uiRequest
     *            the {@link UIRequest}
     */
    public abstract void handleUICall(UIRequest uiRequest);

    /**
     * Removes the matching reg params.
     * 
     * @param oldSubscription
     *            the old subscription
     */
    protected final void removeMatchingRegParams(
	    UIHandlerProfile oldSubscription) {
	((IUIBus) theBus).removeMatchingProfile(busResourceURI, oldSubscription);
	this.realizedHandlerProfiles.remove(oldSubscription);
    }

    /**
     * User logged in.
     * 
     * @param user
     *            the {@link User}
     * @param loginLocation
     *            the login location
     */
    public final void userLoggedIn(Resource user, AbsLocation loginLocation) {
	((IUIBus) theBus).userLoggedIn(busResourceURI, user, loginLocation);
    }

    public List<UIHandlerProfile> getRealizedHandlerProfiles() {
	return realizedHandlerProfiles;
    }

    /**
     * Id with which the {@link UIHandler} is registered in the {@link IUIBus}
     * 
     * @return {@link UIHandler} ID
     */
    public String getMyID() {
	return busResourceURI;
    }

}
