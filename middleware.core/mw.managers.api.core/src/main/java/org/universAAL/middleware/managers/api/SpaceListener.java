/*
    Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

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
package org.universAAL.middleware.managers.api;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;
import org.universAAL.middleware.interfaces.space.SpaceStatus;

/**
 * Manages notifications about Space events
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public interface SpaceListener {

	/**
	 * Called as soon as a Space has been joined
	 *
	 * @param spaceDescriptor
	 *            Space desce
	 */
	public void spaceJoined(SpaceDescriptor spaceDescriptor);

	/**
	 * Called as soon as an Space has been left
	 *
	 * @param spaceDescriptor
	 *            Space descriptor
	 */
	public void spaceLost(SpaceDescriptor spaceDescriptor);

	/**
	 * Called when a peers joins the Space
	 *
	 * @param peer
	 *            PeerCard
	 */
	public void peerJoined(PeerCard peer);

	/**
	 * Called when a Peer leaves the Space
	 *
	 * @param peer
	 */
	public void peerLost(PeerCard peer);

	/**
	 * Called when the Space changes status
	 *
	 * @param status
	 */
	public void spaceStatusChanged(SpaceStatus status);

}
