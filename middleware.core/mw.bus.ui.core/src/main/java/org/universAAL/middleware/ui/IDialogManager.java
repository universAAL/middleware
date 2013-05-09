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
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.ui;

import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;

/**
 * {@link IDialogManager} is the main (application-independent) component for
 * handling system dialogs. It (1) represents the whole system by providing
 * system menus (a unified view of all services available), possibilities to
 * search for specific services, and by handling context-free user input (user
 * input that cannot be assigned to any running dialog), and (2) assists the UI
 * bus by (2.1) acting as a representative for the whole framework supporting
 * context-awareness & personalization, and by (2.2) providing user-specific
 * management of dialogs initiated by different applications to protect the user
 * against a mess of parallel dialogs (decides whether some dialog needs to wait
 * for another dialog to finish).
 * 
 * This interface is supposed to be implemented by exactly one component. The
 * first component that registers as a {@link UICaller} to the UI bus (and
 * implements this interface) blocks the registration of any further
 * implementation of this interface.
 * 
 * @author mtazari
 * @author eandgrg
 */
public interface IDialogManager {

    /**
     * Check new dialog.
     * 
     * @param request
     *            {@link UIRequest} to {@link IUIBus}
     * @return the decision if the new {@link UIRequest} to {@link IUIBus} can
     *         be immediately forwarded to an {@link UIHandler} (returns true)
     *         or must wait for a higher priority dialog to finish (return
     *         false). In case of returning true, the {@link IDialogManager}
     *         must also add the current adaptation parameters to
     *         {@link UIRequest} so that the matchmaking on the {@link IUIBus}
     *         results in adaptive selection of UI channel. In case of returning
     *         false, the {@link IUIBus} ignores the {@link UIRequest} because
     *         it trusts that the {@link IDialogManager} will keep the
     *         {@link UIRequest} in a queue of suspended dialogs and will
     *         re-activate it whenever appropriate.
     */
    public boolean checkNewDialog(UIRequest request);

    /**
     * Informs the {@link IDialogManager} that a running dialog has finished
     * according to the information received from an {@link UIHandler}. As a
     * result this may result in re-activation of previously suspended dialog
     * (by the {@link IDialogManager}).
     * 
     * @param dialogID
     *            ID of the dialog
     */
    public void dialogFinished(String dialogID);

    /**
     * Show main menu of the system.
     * 
     * @param user
     *            {@link User} of a system
     * @param loginLocation
     *            {@link Location} from which {@link User} has logged in to the
     *            system
     */
    public void getMainMenu(Resource user, AbsLocation loginLocation);

    /**
     * Show default login screen for the system. To be shown when no user is
     * logged in.
     * 
     * @param user
     *            {@link User} of a system
     * 
     * @param loginLocation
     *            {@link Location} from which {@link User} has logged out
     */
    public void getLoginScreen(Resource user, AbsLocation loginLocation);

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
     * The bus must use this method in order to inform the
     * {@link IDialogManager} that a dialog has to be suspended. This is the
     * case when during a dialog is running the user steps into a subdialog so
     * the parent dialog must be suspended until the application receives the
     * user input from the subdialog.
     * 
     * @param dialogID
     *            ID of the dialog
     */
    public void suspendDialog(String dialogID);
}
