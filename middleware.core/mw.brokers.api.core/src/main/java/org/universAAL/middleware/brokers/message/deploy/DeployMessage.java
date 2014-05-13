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
package org.universAAL.middleware.brokers.message.deploy;

import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessage.BrokerMessageTypes;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.interfaces.PeerCard;

import com.google.gson.Gson;

/**
 * Deploy message
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * 
 */
public class DeployMessage implements BrokerMessage {

    public enum DeployMessageType {
	REQUEST_TO_INSTALL_PART, REQUEST_TO_UNINSTALL_PART, PART_NOTIFICATION;

    }

    private DeployPayload payload;
    private DeployMessageType deployMessageType;
    public BrokerMessageTypes mType;

    public DeployMessage(DeployMessageType messageType, DeployPayload payload) {

	this.deployMessageType = messageType;
	this.payload = payload;
	this.mType = BrokerMessageTypes.DeployMessage;
    }

    public DeployMessageType getMessageType() {
	return deployMessageType;
    }

    public void setMessageType(DeployMessageType messageType) {
	this.deployMessageType = messageType;
    }

    public String toString() {
	String serializedMessage = null;

	try {
	    Gson gson = GsonParserBuilder.getInstance();
	    serializedMessage = gson.toJson(this);

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return serializedMessage;
    }

    public DeployPayload getPayload() {
	return payload;

    }

    public BrokerMessageTypes getMType() {
	return mType;
    }

    /**
     * To implement
     */
    public PeerCard[] getReceivers() {
	// TODO Auto-generated method stub
	return null;
    }

}
