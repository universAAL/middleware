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

package org.universAAL.middleware.interfaces.configuration.scope;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.interfaces.PeerCard;

/**
 * Used for entities that are only applicable for the given module and instance.
 * 
 * @author amedrano
 * @see InstanceScope
 */
public final class ModuleScope extends InstanceScope {

    /**
     * The module identifier.
     */
    private String moduleId;

    /**
     * Constructor using strings.
     * 
     * @param id
     * @param peerID
     * @param moduleID
     */
    public ModuleScope(String id, String peerID, String moduleID) {
	super(id, peerID);
	if (moduleID == null || moduleID.isEmpty())
	    throw new IllegalArgumentException(
		    "moduleID cannot be null or empty");
	if (moduleID.matches(FORBIDDEN)) {
	    throw new IllegalArgumentException(
		    "moduleID contains forbiden format");
	}
	this.moduleId = moduleID;
    }

    /**
     * Constructor using default instance (this), and ModuleContext for module
     * Id.
     * 
     * @param id
     *            unique id.
     * @param pc
     *            for the instance which the scope is meant for, must not be
     *            null.
     * @param mc
     *            for the Module which the scope is meant for, must not be null.
     */
    public ModuleScope(String id, PeerCard pc, ModuleContext mc) {
	this(id, pc.getPeerID(), mc.getID());
    }

    /**
     * Get the set Module Id.
     * 
     * @return
     */
    public String getModuleID() {
	return moduleId;
    }
}
