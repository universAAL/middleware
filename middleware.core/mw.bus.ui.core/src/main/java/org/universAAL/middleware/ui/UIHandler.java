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
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.impl.UIBusImpl;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.rdf.SubdialogTrigger;
import org.universAAL.middleware.ui.rdf.Submit;

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
     * @throws NullPointerException
     *             if initialSubscription is null or one of the elements of that
     *             array is null
     */
    protected UIHandler(ModuleContext context,
	    UIHandlerProfile initialSubscription) {
	super(context, UIBusImpl.getUIBusFetchParams());

	this.realizedHandlerProfiles = new ArrayList<UIHandlerProfile>();
	if (initialSubscription != null) {
	    if (AccessControl.INSTANCE.checkPermission(owner, getURI(),
		    initialSubscription)) {
		this.realizedHandlerProfiles.add(initialSubscription);
		((IUIBus) theBus).addNewProfile(busResourceURI,
			initialSubscription);
	    }
	}
    }

    /**
     * Instantiates a new {@link UIHandler}.
     * 
     * @param context
     *            the context
     * @param initialSubscription
     *            the initial subscription
     * @throws NullPointerException
     *             if initialSubscriptions is null or one of the elements of
     *             that array is null
     */
    protected UIHandler(ModuleContext context,
	    UIHandlerProfile[] initialSubscriptions) {
	super(context, UIBusImpl.getUIBusFetchParams());

	this.realizedHandlerProfiles = new ArrayList<UIHandlerProfile>();
	if (initialSubscriptions != null) {
	    initialSubscriptions = AccessControl.INSTANCE.checkPermission(
		    owner, getURI(), initialSubscriptions);
	    for (UIHandlerProfile profile : initialSubscriptions) {
		this.realizedHandlerProfiles.add(profile);
		((IUIBus) theBus).addNewProfile(busResourceURI, profile);
	    }
	}
    }

    /**
     * Adaptation parameters changed. The Dialog must be redrawn according to
     * the new value of the changedProp.
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
     * Adds the new {@link UIHandler} registration parameters.
     * 
     * @param newSubscription
     *            the new subscription - as a {@link UIHandlerProfile}
     * @throws NullPointerException
     *             if newSubscription is null
     */
    public final void addNewRegParams(UIHandlerProfile newSubscription) {
	if (AccessControl.INSTANCE.checkPermission(owner, getURI(),
		newSubscription)) {
	    ((IUIBus) theBus).addNewProfile(busResourceURI, newSubscription);
	    this.realizedHandlerProfiles.add(newSubscription);
	}
    }

    /**
     * @see BusMember#busDyingOut(AbstractBus)
     */
    public final void busDyingOut(AbstractBus b) {
	if (b == theBus)
	    communicationChannelBroken();
    }

    /**
     * Method to be called when the communication of the {@link UIHandler} with
     * the {@link IDialogManager} is lost. All dialogs must be de-renderized
     * (handlers may whant to inform the user about why de dialogs are being
     * de-renderized). The bus will automatically resend all HandlerProfiles.
     */
    public abstract void communicationChannelBroken();

    /**
     * Cut dialog. The DM is requesting an IMEDIATE de-renderization of the
     * dialog with given dialogID.
     * 
     * @param dialogID
     *            the dialog id
     * @return the resource data form the {@link Form} filled by the user up to
     *         the moment this call is performed.
     */
    public abstract Resource cutDialog(String dialogID);

    /**
     * Dialog finished. UIHandler reporting the user has submitted a
     * {@link Submit} or a {@link SubdialogTrigger}.
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
     * Handle ui call ({@link UIRequest}). The bus is soliciting a
     * Render/display of the {@link UIRequest}.
     * 
     * @param uiRequest
     *            the {@link UIRequest}
     */
    public abstract void handleUICall(UIRequest uiRequest);

    /**
     * Removes the matching registration parameters.
     * 
     * @param oldSubscription
     *            the old subscription
     */
    protected final void removeMatchingRegParams(
	    UIHandlerProfile oldSubscription) {
	((IUIBus) theBus)
		.removeMatchingProfile(busResourceURI, oldSubscription);
	this.realizedHandlerProfiles.remove(oldSubscription);
    }

    /**
     * User logged in.
     * 
     * @param user
     *            the {@link User}, It is declared as Resource because the type
     *            User is defined in the Profiling Ontology. The type is not
     *            needed for for matchmaking Either.
     * @param loginLocation
     *            the login location
     */
    public final void userLoggedIn(Resource user, AbsLocation loginLocation) {
	((IUIBus) theBus).userLoggedIn(busResourceURI, user, loginLocation);
    }

    /**
     * @return realized {@link UIHandlerProfile}s
     */
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
