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

import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;

/**
 * Dialog Manager is the main (application-independent) component for handling
 * system dialogs. It (1) represents the whole system by providing system menus
 * (a unified view of all services available), possibilities to search for
 * specific services, and by handling context-free user input (user input that
 * cannot be assigned to any running dialog), and (2) assists the I/O buses by
 * (2.1) acting as a representative for the whole framework supporting
 * context-awareness & personalization, and by (2.2) providing user-specific
 * management of dialogs initiated by different applications to protect the user
 * against a mess of parallel dialogs (decides whether some dialog needs to wait
 * for another dialog to finish).
 * 
 * This interface is supposed to be implemented by exactly one component. The
 * first component that registers as an output publisher to the output bus (and
 * implements this interface) blocks the registration of any further
 * implementation of this interface.
 * 
 * @author mtazari
 * 
 */
public interface DialogManager {
    /**
     * 
     * @param event
     *            output event published on Output Bus
     * @return the decision if the new output event published on the Output Bus
     *         can be immediately forwarded to an I/O Handler (returns true) or
     *         must wait for a higher priority dialog to finish (return false).
     *         In case of returning true, the Dialog Manager must also add the
     *         current personal and situational parameters to the output event
     *         so that the matchmaking on the bus results in adaptive selection
     *         of I/O channels. In case of returning false, the bus ignores the
     *         output event because it trusts that the Dialog Manager will keep
     *         the event in a queue of suspended dialogs and will re-activate it
     *         whenever appropriate.
     */
    public boolean checkNewDialog(UIRequest event);

    /**
     * Informs the Dialog Manager that a running dialog has finished according
     * to the information received from an I/O handler. As a result this may
     * result in re-activation of previously suspended dialog (by the Dialog
     * Manager).
     * 
     * @param dialogID
     *            ID of the dialog
     */
    public void dialogFinished(String dialogID);

    public void getMainMenu(Resource user, AbsLocation loginLocation);

    /**
     * When the application has informed the bus that a suspended parent dialog
     * is now ready to be resumed, then the bus uses this method in order to
     * fetch the suspended parent dialog.
     * 
     * @param dialogID
     *            ID of the dialog
     * @return the suspended parent dialog
     */
    public UIRequest getSuspendedDialog(String dialogID);

    /**
     * The bus must use this method in order to inform the Dialog Manager that a
     * dialog has to be suspended. This is the case when during a dialog is
     * running the user steps into a subdialog so the parent dialog must be
     * suspended until the application receives the user input from the
     * subdialog.
     * 
     * @param dialogID
     *            ID of the dialog
     */
    public void suspendDialog(String dialogID);
}
