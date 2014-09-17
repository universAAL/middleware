/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.ui.impl;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.ui.IDialogManager;
import org.universAAL.middleware.ui.impl.generic.CoordinatedRegistrationManagement;

/**
 * This section of the UI Bus Strategy Stack, will only hold the
 * {@link IDialogManager} instance. Managing the cases when there is already a
 * Coordinator, and when The dialogManager is unset.
 * 
 * @author amedrano
 * 
 */
public class UIStrategyCoordinatorMng extends CoordinatedRegistrationManagement {

    /**
     * The reference to the dialogManager
     */
    protected IDialogManager dialogManager;

    /**
     * @param commModule
     * @param name
     */
    public UIStrategyCoordinatorMng(CommunicationModule commModule, String name) {
	super(commModule, name);
    }

    /**
     * @param commModule
     */
    public UIStrategyCoordinatorMng(CommunicationModule commModule) {
	super(commModule);
    }

    boolean setDialogManager(IDialogManager dm) {
	if (dm != null) {
	    try {
		requestBecomeACoordinator();
		this.dialogManager = dm;
		return true;
	    } catch (CoordinatorAlreadyExistsException e) {
		LogUtils.logWarn(busModule, getClass(), "setDialogManager",
			"The peer: " + e.getExistingCoordinator().getPeerID()
				+ " is already coordinator");
		return false;
	    }
	}
	if (dm == null && iAmCoordinator()) {
	    try {
		resignFromCoordinator();
		this.dialogManager = null;
		return true;
	    } catch (CoordinatorAlreadyExistsException e) {
		LogUtils.logWarn(busModule, getClass(), "setDialogManager",
			"could not unset DM, not the coordinator");
		return false;
	    }
	}
	return false;
    }

}