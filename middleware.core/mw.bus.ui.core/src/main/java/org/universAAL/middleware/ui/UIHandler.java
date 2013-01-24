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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Callee;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.ui.impl.UIBusImpl;

/**
 * Provides the interface to be implemented by UI Handlers together with shared
 * code. Only instances of this class can handle UI requests. The convention of
 * the UI bus regarding the registration parameters is the following:
 * <ul>
 * <li>UI Handlers provide at the registration time info about themselves, but
 * this info can also be updated in the future if there is a need</li>
 * </ul>
 * 
 * @author mtazari
 * 
 */
public abstract class UIHandler implements Callee {

    /** The bus. */
    private UIBus bus;

    /** The this callee context. */
    private ModuleContext thisCalleeContext;

    /** The local id. */
    private String myID, localID;

    private List realizedHandlerProfiles;

    /**
     * Instantiates a new uI handler.
     * 
     * @param context
     *            the context
     * @param initialSubscription
     *            the initial subscription
     */
    protected UIHandler(ModuleContext context,
	    UIHandlerProfile initialSubscription) {
	thisCalleeContext = context;
	bus = (UIBus) context.getContainer().fetchSharedObject(context,
		UIBusImpl.busFetchParams);
	myID = bus.register(this, initialSubscription);
	localID = myID.substring(myID.lastIndexOf('#') + 1);

	if (this.realizedHandlerProfiles == null) {
	    this.realizedHandlerProfiles = new ArrayList();
	}
	this.realizedHandlerProfiles.add(initialSubscription);
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
	bus.addNewRegParams(myID, newSubscription);
	this.realizedHandlerProfiles.add(newSubscription);
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
     * Unregisters the UI Handler from the UI Bus.
     */
    public void close() {
	bus.unregister(myID, this);
    }

    /**
     * Method to be called when the communication of the UI Handler with the UI
     * Bus is lost.
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
     * @param input
     *            the input
     */
    public final void dialogFinished(UIResponse input) {
	bus.dialogFinished(myID, input);
    }

    /**
     * @param m
     *            the m
     * @return true, if successful
     * @see org.universAAL.middleware.sodapop.Callee#eval(Message)
     */
    public final boolean eval(Message m) {
	return false;
    }

    /**
     * Handle request.
     * 
     * @param m
     *            the message
     * @see org.universAAL.middleware.sodapop.Callee#handleRequest(Message)
     */
    public final void handleRequest(Message m) {
	if (m.getContent() instanceof UIRequest) {
	    LogUtils.logInfo(thisCalleeContext, UIHandler.class,
		    "handleRequest",
		    new Object[] { localID, " received UI request:\n",
			    m.getContentAsString() }, null);
	    handleUICall((UIRequest) m.getContent());
	}
    }

    /**
     * Handle ui call.
     * 
     * @param uicall
     *            the uicall
     */
    public abstract void handleUICall(UIRequest uicall);

    /**
     * Removes the matching reg params.
     * 
     * @param oldSubscription
     *            the old subscription
     */
    protected final void removeMatchingRegParams(
	    UIHandlerProfile oldSubscription) {
	bus.removeMatchingRegParams(myID, oldSubscription);
	this.realizedHandlerProfiles.remove(oldSubscription);
    }

    /**
     * User logged in.
     * 
     * @param user
     *            the user
     * @param loginLocation
     *            the login location
     */
    public final void userLoggedIn(Resource user, AbsLocation loginLocation) {
	bus.userLoggedIn(myID, user, loginLocation);
    }

    public List getRealizedHandlerProfiles() {
	return realizedHandlerProfiles;
    }

    public String getMyID() {
	return myID;
    }

}
