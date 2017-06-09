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
package org.universAAL.middleware.brokers.message.distributedmw;

import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.interfaces.PeerCard;

import com.google.gson.Gson;

public class DistributedMWMessage implements BrokerMessage {

	private String param;
	private String parsed;

	/**
	 * Create a new Message.
	 */
	public DistributedMWMessage() {
	}

	/**
	 * Create a new Message.
	 * 
	 * @param param
	 */
	public DistributedMWMessage(String param) {
		this.param = param;
	}

	public String getPayload() {
		return param;
	}

	public void setPayload(String param) {
		this.param = param;
	}

	/** {@ inheritDoc} */
	public BrokerMessageTypes getMType() {
		return BrokerMessageTypes.DistributedMWMessage;
	}

	/** {@ inheritDoc} */
	public PeerCard[] getReceivers() {
		return new PeerCard[] {};
	}

	public String toString() {
		// return param;
		String serializedMessage = null;
		if (parsed == null) {
			try {
				Gson gson = GsonParserBuilder.getInstance();
				serializedMessage = gson.toJson(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		parsed = serializedMessage;
		return parsed;
	}

	public static DistributedMWMessage unmarshall(String message) throws Exception {
		try {
			Gson gson = GsonParserBuilder.getInstance();
			return gson.fromJson(message, DistributedMWMessage.class);
		} catch (Exception e) {
			throw new Exception("Unable to unmashall ConfigurationMessage. Original message: " + message
					+ ". Full Stack: " + e.toString());
		}
	}
}
