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
package org.universAAL.middleware.brokers.message.space;

import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.space.SpaceDescriptor;

/**
 * Class for Space Messages
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class SpaceMessage implements BrokerMessage {

	private SpaceMessageTypes spaceMessageType;
	private SpaceDescriptor spaceDescriptor;
	private BrokerMessageTypes mtype;

	public SpaceMessage() {
	}

	public SpaceDescriptor getSpaceDescriptor() {
		return spaceDescriptor;
	}

	public void setSpaceDescriptor(SpaceDescriptor spaceDescriptor) {
		this.spaceDescriptor = spaceDescriptor;
	}

	public enum SpaceMessageTypes {
		JOIN_REQUEST, LEAVE, REQUEST_TO_LEAVE, CONNECT, DISCONNECT, JOIN_RESPONSE, NEW_PEER, PEER_LOST, NOT_SPACE_COODINATOR, REQUEST_PEERCARD, PEERCARD
	}

	public SpaceMessage(SpaceDescriptor spaceDescriptor, SpaceMessageTypes type) {
		this.spaceDescriptor = spaceDescriptor;
		this.spaceMessageType = type;
		this.mtype = BrokerMessageTypes.SpaceMessage;
	}

	public SpaceMessageTypes getMessageType() {
		return this.spaceMessageType;
	}

	public String toString() {
		String serializedMessage = null;
		try {
			serializedMessage = GsonParserBuilder.getInstance().toJson(this);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return serializedMessage;

	}

	public BrokerMessageTypes getMType() {
		return mtype;
	}

	/**
	 * To implement
	 */
	public PeerCard[] getReceivers() {
		// TODO Auto-generated method stub
		return null;
	}

}
