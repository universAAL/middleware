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

import org.universAAL.middleware.interfaces.PeerCard;

/**
 * Defines entity that are only applicable for the instance given in the scope.
 * Entities with this scope will be shared in the whole AALSpaced, but stored
 * only locally.
 * 
 * @author amedrano
 * 
 */
public class InstanceScope extends AALSpaceScope {

    /**
     * The unique instance ID.
     */
    private String peerID;

    /**
     * Constructor for an entity with unique id, and given instance ID.
     * 
     * @param id
     *            the id of the entity.
     * @param peerID
     *            the id of the instance.
     */
    public InstanceScope(String id, String peerID) {
	super(id);
	if (peerID == null || peerID.isEmpty())
	    throw new IllegalArgumentException("peerID cannot be null or empty");
	if (peerID.matches(FORBIDDEN)) {
	    throw new IllegalArgumentException(
		    "peerID contains forbiden format");
	}
	this.peerID = peerID;
    }

    /**
     * Constructor for an entity residing in the same node (this).
     * 
     * @param id
     *            the id of the entity.
     * @param pc
     *            the peerCard for the instance which the scope is meant for,
     *            must not be null.
     */
    public InstanceScope(String id, PeerCard pc) {
	this(id, pc.getPeerID());
    }

    /**
     * Get the id of the instace.
     * 
     * @return
     */
    public String getPeerID() {
	return peerID;
    }
}