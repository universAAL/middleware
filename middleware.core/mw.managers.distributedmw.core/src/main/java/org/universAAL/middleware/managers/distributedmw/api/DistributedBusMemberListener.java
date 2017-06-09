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

import org.universAAL.middleware.bus.member.BusMemberType;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.rdf.Resource;

/**
 * Interface of BusMember registry listener. If registered at the service
 * {@link IBusMemberRegistry} it gets notified if any changes occurs in it.
 * 
 * @author Carsten Stockloew
 * 
 */
public interface DistributedBusMemberListener {
	/**
	 * Invoked when a new BusMember is registered in the bus.
	 * 
	 * @param origin
	 *            the peer on which the event occurred.
	 * @param member
	 *            newly added bus member
	 * @param type
	 *            type of bus {@link IBusMemberRegistry}
	 */
	public void busMemberAdded(PeerCard origin, String busMemberID, String busName, BusMemberType memberType,
			String label, String comment);

	/**
	 * Invoked when an existing BusMember is unregistered from the bus.
	 * 
	 * @param origin
	 *            the peer on which the event occurred.
	 * @param member
	 *            removed bus member
	 * @param type
	 *            type of bus {@link IBusMemberRegistry}
	 */
	public void busMemberRemoved(PeerCard origin, String busMemberID);

	/**
	 * Invoked when registration parameters of an existing BusMember are added.
	 * Registration parameters can be, for example, {@link ServiceProfile}s for
	 * {@link ServiceCallee}s or {@link ContextEventPattern} for
	 * {@link ContextSubscriber}.
	 * 
	 * @param origin
	 *            the peer on which the event occurred.
	 * @param busMemberID
	 *            the ID of the bus member for which the registration parameters
	 *            have been added.
	 * @param params
	 *            the registration parameters that have been added.
	 */
	public void regParamsAdded(PeerCard origin, String busMemberID, Resource[] params);

	/**
	 * Invoked when registration parameters of an existing BusMember are
	 * removed. Registration parameters can be, for example,
	 * {@link ServiceProfile}s for {@link ServiceCallee}s or
	 * {@link ContextEventPattern} for {@link ContextSubscriber}.
	 * 
	 * @param origin
	 *            the peer on which the event occurred.
	 * @param busMemberID
	 *            the ID of the bus member for which the registration parameters
	 *            have been removed.
	 * @param params
	 *            the registration parameters that have been removed.
	 */
	public void regParamsRemoved(PeerCard origin, String busMemberID, Resource[] params);
}
