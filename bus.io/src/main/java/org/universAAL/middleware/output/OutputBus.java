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

import org.universAAL.middleware.io.rdf.Submit;
import org.universAAL.middleware.rdf.Resource;

/**
 * The Output Bus is an event based bus. It defines protocols for suspending and
 * resuming dialogs, dynamic adaptation of “rendering” by a previously selected
 * I/O handler during the dialog is running, and transfer of responsibility to
 * another I/O handler. It accepts registration parameters from
 * OutputSubscribers (when they register to the bus) and allows removing and/or
 * updating of these parameters.
 * 
 * @author mtazari
 * 
 */
public interface OutputBus {

	/**
	 * Only the Dialog Manager (DM) can call this method. When the DM notices
	 * that personal and / or situational parameters relevant for a running
	 * dialog have changed, it notifies the output bus by calling this method.
	 * The output bus may then either notify the I/O handler in charge of that
	 * dialog to consider the changes (if the changes in the adaptation
	 * parameters still match its profile) or switch to another I/O handler (if
	 * the new situation cannot be handled by the previous I/O handler). In the
	 * latter case, the previous I/O handler is notified to abort the dialog
	 * while returning any intermediate user input collected so far, and then
	 * the new I/O handler is mandated to continue with the dialog presentation
	 * without loss of data.
	 * 
	 * @param dm
	 *            Dialog Manager
	 * @param oe
	 *            output event
	 * @param changedProp
	 *            new property that has changed
	 */
	public void adaptationParametersChanged(DialogManager dm, OutputEvent oe,
			String changedProp);

	/**
	 * Adds new subscriber with related parameters.
	 * 
	 * @param subscriberID
	 *            ID of a output bus subscriber
	 * @param newSubscription
	 *            parameters of a new subscriber
	 */
	public void addNewRegParams(String subscriberID,
			OutputEventPattern newSubscription);

	/**
	 * Aborts the dialog upon request from the application.
	 * 
	 * @param publisherID
	 *            ID of output publisher
	 * @param dialogID
	 *            ID of the dialog
	 */
	public void abortDialog(String publisherID, String dialogID);

	// TODO finish Javadoc here
	/**
	 * 
	 * @param subscriberID
	 * @param submission
	 * @param poppedMessage
	 */
	public void dialogFinished(String subscriberID, Submit submission,
			boolean poppedMessage);

	/**
	 * Only the Dialog Manager (DM) can call this method. When the DM wants that
	 * a running dialog is substituted by another dialog (e.g., because a new
	 * output event with a higher priority than the running one is addressing
	 * the same user, or because the user wants to switch to another dialog
	 * using the “standard buttons”), then it must notify the output bus by
	 * calling this method. The output bus will then ask the I/O handler in
	 * charge of handling the running dialog to cut that dialog and return all
	 * user input collected so far so that the dialog can be resumed later
	 * without loss of data.
	 * 
	 * @param dm
	 *            Dialog Manager
	 * @param dialogID
	 *            ID of the dialog
	 */
	public void dialogSuspended(DialogManager dm, String dialogID);

	public String register(OutputPublisher publisher);

	/**
	 * Registers output subscriber with certain parameters.
	 * 
	 * @param subscriber
	 *            output subscriber
	 * @param initialSubscription
	 *            subscription parameters
	 * @return
	 */
	public String register(OutputSubscriber subscriber,
			OutputEventPattern initialSubscription);

	/**
	 * Removes subscribtion that matches given pattern.
	 * 
	 * @param subscriberID
	 *            ID of subscriber
	 * @param oldSubscription
	 *            old subscription
	 */
	public void removeMatchingRegParams(String subscriberID,
			OutputEventPattern oldSubscription);

	/**
	 * Applications can use this method to ask the output bus to resume a dialog
	 * that was interrupted due to the activation of a subdialog of it. This is
	 * the only case where the application are aware about a dialog having been
	 * suspended because the resumption depends on them having processed the
	 * user input in the context of the subdialog and having incorporated it
	 * into the form data of the parent dialog if needed. Then, they can
	 * activate the parent dialog using this method.
	 * 
	 * @param publisherID
	 *            ID of the outout publisher
	 * @param dialogID
	 *            ID of the dialog
	 * @param dialogData
	 *            dialog data
	 */
	public void resumeDialog(String publisherID, String dialogID,
			Resource dialogData);

	/**
	 * Sends an Output Event.
	 * 
	 * @param publisherID
	 *            ID of output publisher
	 * @param event
	 *            output event
	 */
	public void sendMessage(String publisherID, OutputEvent event);

	/**
	 * Unregisteres Output Publisher.
	 * 
	 * @param publisherID
	 *            ID of output publisher
	 * @param publisher
	 *            output publisher reference
	 */
	public void unregister(String publisherID, OutputPublisher publisher);

	/**
	 * Unregisteres Output Subscriber.
	 * 
	 * @param publisherID
	 *            ID of output subscriber
	 * @param publisher
	 *            output subscriber reference
	 */
	public void unregister(String subscriberID, OutputSubscriber subscriber);
}
