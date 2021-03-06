/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.bus.member;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.container.ModuleContext;

/**
 * A SpaceParticipant can connect to the different buses of the universAAL
 * middleware by becoming a member, i.e., by creating instances of this class.
 * Each instance of BusMember represents exactly one connection with a specific
 * role on one specific bus: on call-based buses either as a caller or as a
 * callee and on event-based buses either as a publisher or as a subscriber.
 * However, there is no limitation for the number of instances of BusMember that
 * an SpaceParticipant can create; i.e., an SpaceParticipant can create
 * different instances of BusMember to connect to different buses; it is also
 * possible to connect with different roles to the same bus or even create, say,
 * several publishers on one and the same event-based bus while at the same time
 * creating even several subscribers on that very bus. BusMember is an abstract
 * class with the idea that for each concrete role on each concrete bus a
 * specific subclass is created. It also forces that the creation of an instance
 * is bound to a specific SpaceParticipant.
 *
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 *
 */
public abstract class BusMember {
	protected final ModuleContext owner;
	protected final AbstractBus theBus;
	protected final String busResourceURI;
	private BusMemberType type;
	private String label = null;
	private String comment = null;

	protected BusMember(ModuleContext owner, Object[] busFetchParams, BusMemberType type) {
		this.owner = owner;
		this.type = type;
		theBus = (AbstractBus) owner.getContainer().fetchSharedObject(owner, busFetchParams);
		busResourceURI = theBus.createBusSpecificID(owner.getID(), type.name());
		theBus.register(owner, this, type);
		AccessControl.INSTANCE.registerBusMember(owner, this, theBus.getBrokerName());
	}

	/**
	 * Unregisters the Subscriber from the bus.
	 */
	public void close() {
		theBus.unregister(busResourceURI, this);
		AccessControl.INSTANCE.unregisterBusMember(owner, this);
	}

	/**
	 * This method is called when the bus is stopped to announce this to the bus
	 * members.
	 *
	 * @see AbstractBus
	 *
	 * @param b
	 *            bus on which this member has been registered
	 */
	public abstract void busDyingOut(AbstractBus b);

	/**
	 * URI of this bus member. The URI is created by the bus and set during
	 * registration of the the bus member at the bus.
	 */
	public final String getURI() {
		return busResourceURI;
	}

	/**
	 * Get the type of this bus member.
	 *
	 * @return the type of this bus member.
	 */
	public BusMemberType getType() {
		return type;
	}

	/**
	 * Set a human-readable label for this bus member.
	 *
	 * @param label
	 *            the new label.
	 */
	public void setLabel(String label) {
		if (this.label == null)
			this.label = label;
	}

	/**
	 * Get the human-readable label for this bus member.
	 *
	 * @return the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set a human-readable comment for this bus member.
	 *
	 * @param comment
	 *            the new comment.
	 */

	public void setComment(String comment) {
		if (this.comment == null)
			this.comment = comment;
	}

	/**
	 * Get the human-readable comment for this bus member.
	 *
	 * @return the comment.
	 */
	public String getComment() {
		return comment;
	}
}
