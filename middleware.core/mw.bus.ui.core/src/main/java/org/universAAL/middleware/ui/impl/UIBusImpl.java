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
package org.universAAL.middleware.ui.impl;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.DialogManager;
import org.universAAL.middleware.ui.UIBus;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.owl.UIBusOntology;
import org.universAAL.middleware.util.Constants;
import org.universAAL.middleware.util.ResourceComparator;

/**
 * @author mtazari
 * 
 * @see org.universAAL.middleware.ui.UIBus
 * @author Carsten Stockloew
 */
public class UIBusImpl extends AbstractBus implements UIBus {
    private static Object[] busFetchParams;
    private static UIBusImpl theUIBus = null;
    private static UIBusOntology uiBusOntology = new UIBusOntology();
    private static ModuleContext mc;

    public static Object[] getUIBusFetchParams() {
	return busFetchParams.clone();
    }

    public synchronized void assessContentSerialization(Resource content) {
	if (org.universAAL.middleware.util.Constants.debugMode()) {
	    LogUtils
		    .logDebug(
			    context,
			    UIBusImpl.class,
			    "assessContentSerialization",
			    new Object[] { "Assessing message content serialization:" },
			    null);

	    String str = BusMessage.trySerializationAsContent(content);
	    LogUtils
		    .logDebug(
			    context,
			    UIBusImpl.class,
			    "assessContentSerialization",
			    new Object[] { "\n      1. serialization dump\n",
				    str,
				    "\n      2. deserialize & compare with the original resource\n" },
			    null);
	    new ResourceComparator().printDiffs(content, (Resource) BusMessage
		    .deserializeAsContent(str));
	}
    }

    public static void startModule(Container c, ModuleContext mc,
	    Object[] uiBusShareParams, Object[] uiBusFetchParams) {
	if (theUIBus == null) {
	    UIBusImpl.mc = mc;
	    OntologyManagement.getInstance().register(mc, uiBusOntology);
	    theUIBus = new UIBusImpl(mc);
	    busFetchParams = uiBusFetchParams;
	    c.shareObject(mc, theUIBus, uiBusShareParams);
	}
    }

    public static void stopModule() {
	if (theUIBus != null) {
	    OntologyManagement.getInstance().unregister(mc, uiBusOntology);
	    theUIBus.dispose();
	    theUIBus = null;
	}
    }

    /**
     * Create an instance of the UIBus.
     * 
     * @param g
     *            Pointer to the local instance of the SodaPop bus-system
     */
    private UIBusImpl(ModuleContext mc) {
	super(mc);
	busStrategy.setBus(this);
    }

    public static ModuleContext getModuleContext() {
	return mc;
    }

    @Override
    protected BusStrategy createBusStrategy(CommunicationModule commModule) {
	return new UIStrategy(commModule);
    }

    public void setDialogManager(DialogManager dm) {
	((UIStrategy) busStrategy).setDialogManager(dm);
    }

    /**
     * Closes a running dialog
     * 
     * @param callerID
     *            ID of the publisher of the Dialog
     * @param dialogID
     *            ID of the dialog to delete
     */
    public void abortDialog(String callerID, String dialogID) {
	BusMember bm = getBusMember(callerID);
	if (bm instanceof UICaller) {
	    ((UIStrategy) busStrategy).abortDialog(callerID, dialogID);
	}
    }

    /**
     * @param dm
     *            The responsible Dialog Manager
     * @param uiRequest
     *            New/Changed UIRequest
     * @param changedProp
     *            Property that has been changed since last time
     */
    public void adaptationParametersChanged(DialogManager dm,
	    UIRequest uiRequest, String changedProp) {
	((UIStrategy) busStrategy).adaptationParametersChanged(dm, uiRequest,
		changedProp);
    }

    /**
     * 
     * Adds a new subscription to the bus
     * 
     * @param handlerID
     *            ID of the subscriber like given by register
     * @param newProfile
     *            Description of the subscription
     */
    public void addNewProfile(String handlerID, UIHandlerProfile newProfile) {
	Object o = registry.getBusMemberByID(handlerID);
	if (o instanceof UIHandler) {
	    ((UIStrategy) busStrategy).addRegParams(handlerID, newProfile);
	}
    }

    /**
     * @see UIBus#dialogFinished(String, UIResponse)
     */
    public void dialogFinished(String handlerID, UIResponse response) {
	if (response != null) {
	    Object o = registry.getBusMemberByID(handlerID);
	    if (o instanceof UIHandler) {
		if (response.isSubdialogCall()) {
		    ((UIStrategy) busStrategy).suspendDialog(response
			    .getDialogID());
		} else {
		    ((UIStrategy) busStrategy).dialogFinished(handlerID,
			    response);
		}

		// send a notification to the calling app with the UI Response
		((UIStrategy) busStrategy).notifyUserInput(response);
	    }
	}
    }

    /**
     * Can only be called by the DialogManager. Suspend the given dialog
     * 
     * @param dm
     *            Instance of the DialogManager
     * @param dialogID
     *            ID of the dialog to suspend
     */
    public void dialogSuspended(DialogManager dm, String dialogID) {
	((UIStrategy) busStrategy).dialogSuspended(dm, dialogID);
    }

    /**
     * Removes a subscription from the bus
     * 
     * @param handlerID
     *            ID from the owner of the subscription
     * @param oldProfile
     *            Subscription to remove
     */
    public void removeMatchingProfile(String handlerID,
	    UIHandlerProfile oldProfile) {
	Object o = registry.getBusMemberByID(handlerID);
	if (o instanceof UIHandler) {
	    ((UIStrategy) busStrategy).removeMatchingProfile(handlerID,
		    oldProfile);
	}
    }

    /**
     * 
     * @see UIBus#resumeDialog(String, String, Resource)
     */
    public void resumeDialog(String callerID, String dialogID,
	    Resource dialogData) {
	BusMember bm = getBusMember(callerID);
	if (bm instanceof DialogManager && dialogData instanceof UIRequest) {
	    ((UIStrategy) busStrategy).adaptationParametersChanged(
		    (DialogManager) bm, (UIRequest) dialogData, null);
	} else if (bm instanceof UICaller) {
	    ((UIStrategy) busStrategy).resumeDialog(dialogID, dialogData);
	}
    }

    /**
     * 
     * Asks the bus to find an appropriate UI handler and forward the request to
     * it for handling
     * 
     * @param callerID
     *            the ID of the UICaller that is asking the bus
     * @param req
     *            the request to be forwarded to a UI handler
     * 
     */
    public void brokerUIRequest(String callerID, UIRequest req) {
	BusMember bm = getBusMember(callerID);
	if (bm instanceof UICaller) {
	    assessContentSerialization(req);
	    brokerMessage(callerID, new BusMessage(MessageType.request, req,
		    this));
	}
    }

    /**
     * @see org.universAAL.middleware.ui.UIBus#unregister(java.lang.String,
     *      org.universAAL.middleware.ui.UICaller)
     */
    public void unregister(String handlerID, UICaller handler) {
	super.unregister(handlerID, handler);
    }

    /**
     * @see org.universAAL.middleware.ui.UIBus#unregister(java.lang.String,
     *      org.universAAL.middleware.ui.UIHandler)
     */
    public void unregister(String handlerID, UIHandler handler) {
	Object o = registry.getBusMemberByID(handlerID);
	if (o != null && o == handler) {
	    super.unregister(handlerID, handler);
	    ((UIStrategy) busStrategy).removeRegParams(handlerID);
	}
    }

    /**
     * @see org.universAAL.middleware.ui.UIBus#userLoggedIn(java.lang.String,
     *      org.universAAL.middleware.rdf.Resource,
     *      org.universAAL.middleware.owl.supply.AbsLocation)
     */
    public void userLoggedIn(String handlerID, Resource user,
	    AbsLocation loginLocation) {
	Object o = registry.getBusMemberByID(handlerID);
	if (o instanceof UIHandler && user != null) {
	    ((UIStrategy) busStrategy).userLoggedIn(user, loginLocation);
	}

    }

    public void handleSendError(ChannelMessage message,
	    CommunicationConnectorException e) {
	// TODO Auto-generated method stub

    }
}
