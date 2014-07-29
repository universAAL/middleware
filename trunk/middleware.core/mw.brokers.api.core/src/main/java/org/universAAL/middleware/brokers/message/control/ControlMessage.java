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
package org.universAAL.middleware.brokers.message.control;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;

import com.google.gson.Gson;

/**
 * Control message
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class ControlMessage implements BrokerMessage {

    private ControlMessageType messageType;
    private BrokerMessageTypes mType;
    private AALSpaceDescriptor space;
    private String payload;
    private String parsed = null;
    private List<String> attributes;
    private Map<String, Serializable> filter;
    private Map<String, Serializable> values;
    private boolean match;
    private String transactionId;

    public enum ControlMessageType {
	AALSPACE_EVENT, GET_ATTRIBUTES, MATCH_ATTRIBUTES, GET_ATTRIBUTES_RESPONSE, MATCH_ATTRIBUTES_RESPONSE
    }

    public enum ControlMessageFields {
	ID, TYPE, PAYLOAD
    }

    public ControlMessage(AALSpaceDescriptor space, List<String> attributes) {
	this(space, ControlMessageType.GET_ATTRIBUTES);
	this.attributes = attributes;
    }

    public ControlMessage(AALSpaceDescriptor space, ControlMessageType type) {
	this();
	this.space = space;
	this.messageType = type;
    }

    private ControlMessage() {
	this.mType = BrokerMessageTypes.ControlMessage;
	this.transactionId = UUID.randomUUID().toString();
    }

    public ControlMessage(AALSpaceDescriptor space,
	    Map<String, Serializable> filter) {
	this(space, ControlMessageType.MATCH_ATTRIBUTES);
	this.filter = filter;
    }

    public ControlMessage(AALSpaceDescriptor space, String id,
	    HashMap<String, Serializable> map) {
	this(space, ControlMessageType.GET_ATTRIBUTES_RESPONSE, id);
	this.values = map;
    }

    public ControlMessage(AALSpaceDescriptor space, String id,
	    HashMap<String, Serializable> map, boolean match) {
	this(space, ControlMessageType.MATCH_ATTRIBUTES_RESPONSE, id);
	this.values = map;
	this.match = match;
    }

    public ControlMessage(AALSpaceDescriptor space, ControlMessageType type,
	    String id) {
	this.space = space;
	this.messageType = type;
	this.mType = BrokerMessageTypes.ControlMessage;
	this.transactionId = id;
    }

    public String toString() {
	String serializedMessage = null;
	if (parsed == null) {

	    try {
		Gson gson = GsonParserBuilder.getInstance();
		serializedMessage = gson.toJson(this);

	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	parsed = serializedMessage;
	return parsed;
    }

    public boolean getMatchFilter() {
	return match;
    }

    public ControlMessageType getMessageType() {
	return messageType;
    }

    public static ControlMessage unmarshall(String message) throws Exception {

	try {

	    Gson gson = GsonParserBuilder.getInstance();

	    return gson.fromJson(message, ControlMessage.class);

	} catch (Exception e) {

	    throw new Exception(
		    "Unable to unmashall ControlMessage. Original message: "
			    + message + ". Full Stack: " + e.toString());
	}
    }

    public String getTransactionId() {
	return transactionId;
    }

    public Map<String, Serializable> getAttributeFilter() {
	return filter;
    }

    public Map<String, Serializable> getAttributeValues() {
	return values;
    }

    public BrokerMessageTypes getMType() {
	return mType;
    }

    public List<String> getAttributes() {
	return attributes;
    }

    /**
     * To implement
     */
    public PeerCard[] getReceivers() {
	// TODO Auto-generated method stub
	return null;
    }

}