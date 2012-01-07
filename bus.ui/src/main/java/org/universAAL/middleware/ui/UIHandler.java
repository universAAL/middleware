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
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.Bus;
import org.universAAL.middleware.sodapop.Callee;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.ui.impl.UIBusImpl;

/**
 * Provides the interface to be implemented by output subscribers together with
 * shared code. Only instances of this class can subscribe for output events.
 * The convention of the context bus regarding the registration parameters is
 * the following:
 * <ul>
 * <li>ContextPublishers provide only at the registration time info about
 * themselves using
 * {@link org.universAAL.middleware.context.owl.ContextProvider}.</li>
 * <li>ContextSubscribers may pass an array of
 * {@link org.universAAL.middleware.context.ContextEventPattern}s as their
 * initial subscriptions and can always add new (and remove old) subscriptions
 * dynamically.</li>
 * </ul>
 * 
 * @author mtazari
 * 
 */
public abstract class UIHandler implements Callee {
    private UIBus bus;
    private ModuleContext thisCalleeContext;
    private String myID, localID;

    protected UIHandler(ModuleContext context, UIHandlerProfile initialSubscription) {
	thisCalleeContext = context;
	bus = (UIBus) context.getContainer().fetchSharedObject(context,
		UIBusImpl.busFetchParams);
	myID = bus.register(this, initialSubscription);
	localID = myID.substring(myID.lastIndexOf('#') + 1);
    }

    public abstract void adaptationParametersChanged(String dialogID,
	    String changedProp, Object newVal);

    protected final void addNewRegParams(UIHandlerProfile newSubscription) {
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

    public final void dialogFinished(UIResponse input) {
	bus.dialogFinished(myID, input);
    }

    public final boolean eval(Message m) {
	return false;
    }

    public final void handleRequest(Message m) {
	if (m.getContent() instanceof UIRequest) {
	    LogUtils.logInfo(thisCalleeContext, UIHandler.class,
		    "handleRequest",
		    new Object[] { localID, " received UI request:\n",
			    m.getContentAsString() }, null);
	    handleUICall((UIRequest) m.getContent());
	}
    }

    public abstract void handleUICall(UIRequest uicall);

    protected final void removeMatchingRegParams(UIHandlerProfile oldSubscription) {
	bus.removeMatchingRegParams(myID, oldSubscription);
    }

    public final void userLoggedIn(Resource user, AbsLocation loginLocation) {
	bus.userLoggedIn(myID, user, loginLocation);
    }
}
