/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	2012 Ericsson Nikola Tesla d.d., www.ericsson.com/hr
	
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

import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.rdf.Submit;

/**
 * The UI Bus is responsible for brokerage between applications that need to
 * reach human users (in order to present information to them and / or ask them
 * for intervention) and the so-called UI handlers that can handle the
 * interaction with human users through UI channels under their control. This
 * bus is a call-based bus without any support for synchronized delivery of
 * replies (replies will always be delivered in a new thread). It defines
 * protocols for suspending and resuming dialogs, dynamic adaptation of
 * "rendering" by a previously selected UI handler during the dialog is running,
 * and transfer of responsibility to another UI handler. It accepts registration
 * parameters from UI handlers (when they register to the bus) and allows
 * removing and/or updating of these parameters.
 * 
 * @author mtazari
 * @author eandgrg
 * 
 */
public interface UIBus {

    /**
     * Aborts the dialog upon request from the application or Dialog Manager. No
     * matter which one has requested the abort, the bus informs both when the
     * operation is finished (informing the caller as a sort of acknowledgment
     * and informing the other one to prevent unnecessary waiting).
     * 
     * @param callerID
     *            ID of the application that had originally started the dialog
     * @param dialogID
     *            ID of the dialog
     * 
     */
    public void abortDialog(String callerID, String dialogID);

    /**
     * Only the Dialog Manager (DM) can call this method. When the DM notices
     * that personal and / or situational parameters relevant for a running
     * dialog have changed, it notifies the UI bus by calling this method. The
     * UI bus may then either notify the UI handler in charge of that dialog to
     * consider the changes (if the changes in the adaptation parameters still
     * match its profile -- see also
     * {@link UIHandler#adaptationParametersChanged(String, String, Object)}) or
     * switch to another UI handler (if the new situation cannot be handled by
     * the previous UI handler). In the latter case, the previous UI handler is
     * notified to abort the dialog while returning any intermediate user input
     * collected so far (by calling {@link UIHandler#cutDialog(String)}), and
     * then the new UI handler is mandated (by calling
     * {@link UIHandler#handleUICall(UIRequest)}) to continue with the dialog
     * presentation without loss of data.
     * 
     * @param dm
     *            Dialog Manager
     * @param uicall
     *            The whole call context that is affected by the change,
     *            including the dialog ID and the the new value of the changed
     *            property
     * @param changedProp
     *            the property (from among all properties of the call context)
     *            that has changed
     */
    public void adaptationParametersChanged(DialogManager dm, UIRequest uicall,
	    String changedProp);

    /**
     * Extends the profile of a registered subscriber (UI handler) with regard
     * to {@link UIRequest}s that it can handle. Responsible (together with
     * {@link #removeMatchingRegParams(String, UIHandlerProfile)}) for changing
     * the handler's profile dynamically.
     * 
     * @param handlerID
     *            ID of the UI handler introducing the new registration
     *            parameters
     * @param newSubscription
     *            the new class of {@link UIRequest}s that can additionally be
     *            handled by the given UI handler
     */
    public void addNewRegParams(String handlerID,
	    UIHandlerProfile newSubscription);

    /**
     * Whenever a dialog is finished, UI handlers must inform the UI Bus by
     * calling this method.
     * 
     * @param handlerID
     *            ID of the UI handler that has finished the dialog
     * @param input
     *            The user input constructed by calling
     *            {@link UIResponse#UIResponse(Resource, AbsLocation, Submit)}
     */
    public void dialogFinished(String handlerID, UIResponse input);

    /**
     * Only the Dialog Manager (DM) can call this method. When the DM wants that
     * a running dialog is substituted by another dialog (e.g., because a new
     * {@link UIRequest} with a higher priority than the running one is
     * addressing the same user, or because the user wants to switch to another
     * dialog using the "standard buttons"), then it must notify the UI bus by
     * calling this method. The UI bus will then ask the UI handler in charge of
     * handling the running dialog to cut that dialog (see
     * {@link UIHandler#cutDialog(String)}) and return all user input collected
     * so far so that the dialog can be resumed later without loss of data.
     * 
     * @param dm
     *            Dialog Manager
     * @param dialogID
     *            ID of the dialog to suspend
     */
    public void dialogSuspended(DialogManager dm, String dialogID);

    /**
     * Applications that need to reach human users must register a
     * {@link UICaller}. They can use this UICaller for sending their UI
     * requests as long as they remain registered.
     * 
     * @param caller
     *            An application's interface for sending UI requests and
     *            receiving handlers' replies.
     * @return An ID for this bus member that is used as a kind of "password" of
     *         the caller. It must be passed to the bus when calling the other
     *         bus methods.
     */
    public String register(UICaller caller);

    /**
     * UI handlers that can handle the interaction with human users must
     * register a UIHandler. The bus will use this interface for realizing the
     * protocols between the bus and the handler.
     * 
     * @param handler
     *            A UI handler's interface for receiving UI requests and sending
     *            replies.
     * @param initialSubscription
     *            The initial profile of the handler (see also
     *            {@link #addNewRegParams(String, UIHandlerProfile)} and
     *            {@link #removeMatchingRegParams(String, UIHandlerProfile)}).
     * @return An ID for this bus member that is used as a kind of "password" of
     *         the handler. It must be passed to the bus when calling the other
     *         bus methods.
     */
    public String register(UIHandler handler,
	    UIHandlerProfile initialSubscription);

    /**
     * Removes matching patterns of {@link UIRequest}s from the profile of the
     * UI handler. Responsible (together with
     * {@link #addNewRegParams(String, UIHandlerProfile)}) for changing the
     * handler's profile dynamically.
     * 
     * @param subscriberID
     *            ID of subscriber
     * @param oldSubscription
     *            old subscription
     */
    public void removeMatchingRegParams(String subscriberID,
	    UIHandlerProfile oldSubscription);

    /**
     * Applications can use this method to ask the UI bus to resume a dialog
     * that was interrupted due to the activation of a sub-dialog of it. This is
     * the only case where the applications are aware about a dialog having been
     * suspended because the resumption depends on them having processed the
     * user input in the context of the sub-dialog and having incorporated it
     * into the form data of the parent dialog if needed. Then, they can
     * activate the parent dialog using this method.
     * 
     * @param callerID
     *            ID of the application that had originally started the dialog
     * @param dialogID
     *            ID of the dialog
     * @param dialogData
     *            dialog data (see {@link Form#getData()})
     */
    public void resumeDialog(String callerID, String dialogID,
	    Resource dialogData);

    /**
     * Can be used by applications to send a UIRequest.
     * 
     * @param callerID
     *            ID of the caller that is sending the UI request
     * @param uicall
     *            The actual UI request
     */
    public void sendMessage(String callerID, UIRequest uicall);

    /**
     * Unregisters an application's caller from the bus.
     * 
     * @param callerID
     *            ID of the caller to be unregistered
     * @param uicaller
     *            the caller to be unregistered
     */
    public void unregister(String callerID, UICaller uicaller);

    /**
     * Unregisters the given UI handler from the bus..
     * 
     * @param handlerID
     *            ID of the handler to be unregistered
     * @param handler
     *            the handler to be unregistered
     */
    public void unregister(String handlerID, UIHandler handler);

    /**
     * Notifies bus that the human user has logged in (using UI handler).
     * 
     * @param handlerID
     *            id of the UIHandler which is received when registering to the
     *            UIBus. It must be passed to the bus when calling bus methods.
     * @param user
     *            human user
     * @param loginLocation
     *            login location of the user
     */
    public void userLoggedIn(String handlerID, Resource user,
	    AbsLocation loginLocation);
}
