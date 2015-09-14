/*	
	Copyright 2007-2016 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
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
package org.universAAL.middleware.managers.distributedmw.api;

import java.util.List;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.api.Manager;

/**
 * Manager for {@link DistributedBusMemberListener}s. The manager registers as a
 * shared object. Registered listeners will be notified if a change occurs in
 * the set of bus members or their registration parameters.
 * 
 * @author Carsten Stockloew
 * 
 */
public interface DistributedBusMemberListenerManager extends Manager {

    /**
     * Add a new listener.
     * 
     * @param listener
     *            The callback that is invoked when a change occurs.
     * @param nodes
     *            The set of nodes on which the listener should be added. An
     *            empty list will subscribe to all nodes, including nodes that
     *            will join the space in the future. Null will subscribe to this
     *            node only and is equivalent with a list that contains the
     *            {@link PeerCard} of this node.
     */
    void addListener(DistributedBusMemberListener listener, List<PeerCard> nodes);

    /**
     * Remove an existing listener.
     * 
     * @param listener
     *            The callback that was registered before with
     *            {@link #addListener(DistributedBusMemberListener, List)}.
     * @param nodes
     *            The set of nodes on which the listener should be removed. An
     *            empty list will unsubscribe from all nodes. Null will
     *            unsubscribe from this node only and is equivalent with a list
     *            that contains the {@link PeerCard} of this node.
     */
    void removeListener(DistributedBusMemberListener listener,
	    List<PeerCard> nodes);
}
