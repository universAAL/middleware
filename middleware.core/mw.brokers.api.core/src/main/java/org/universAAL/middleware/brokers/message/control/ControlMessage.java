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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sound.sampled.AudioFileFormat.Type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessageFields;
import org.universAAL.middleware.brokers.message.Payload;
import org.universAAL.middleware.brokers.message.BrokerMessage.BrokerMessageTypes;
import org.universAAL.middleware.brokers.message.deploy.DeployMessageException;
import org.universAAL.middleware.brokers.message.deploy.DeployMessageFields;
import org.universAAL.middleware.brokers.message.deploy.DeployNotificationPayload;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;

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
        if (parsed == null) {
            JSONObject obj = new JSONObject();

            try {
                obj.put(BrokerMessageFields.BROKER_MESSAGE_TYPE.toString(),
                        mType.toString());
                obj.put(ControlMessageFields.TYPE.toString(),
                        messageType.ordinal());
                obj.put(ControlMessageFields.ID.toString(), transactionId);
                if (payload == null) {
                    generatePaylod();
                }
                obj.put(ControlMessageFields.PAYLOAD.toString(), payload);
            } catch (JSONException e) {
                new DeployMessageException(
                        "Unable to unmarshall ControlMessage, invalid JSON: "
                                + e.toString(), e);
            } catch (Exception e) {
                new DeployMessageException(
                        "Unable to unmarshall ControlMessage: " + e.toString(),
                        e);
            }

            parsed = obj.toString();
        }
        return parsed;
    }

    public boolean getMatchFilter() {
        return match;
    }

    private void generatePaylod() {
        JSONObject response;
        switch (messageType) {
        case AALSPACE_EVENT:
            payload = "";
            break;
        case GET_ATTRIBUTES:
            payload = new JSONArray(attributes).toString();
            break;
        case GET_ATTRIBUTES_RESPONSE:
            payload = new JSONObject(values).toString();
            break;
        case MATCH_ATTRIBUTES:
            payload = new JSONObject(filter).toString();
            break;
        case MATCH_ATTRIBUTES_RESPONSE:
            if (values == null || values.isEmpty() || match == false) {
                payload = "";
            } else {
                payload = new JSONObject(values).toString();
            }
            break;
        default:
            break;
        }
    }

    public ControlMessageType getMessageType() {
        return messageType;
    }

    public static ControlMessage unmarshall(String message) throws Exception {
        JSONObject obj = new JSONObject(message);
        BrokerMessageTypes type = BrokerMessageTypes.valueOf(obj
                .getString(BrokerMessageFields.BROKER_MESSAGE_TYPE));
        if (type != BrokerMessageTypes.ControlMessage) {
            throw new IllegalArgumentException(
                    "Invalid message type, unable to unmarshall it:" + type
                            + " must be " + BrokerMessageTypes.ControlMessage);
        }
        ControlMessage value = new ControlMessage();
        value.messageType = ControlMessageType.values()[obj
                .getInt(ControlMessageFields.TYPE.toString())];
        value.transactionId = obj.getString(ControlMessageFields.ID.toString());
        switch (value.messageType) {

        case GET_ATTRIBUTES: {
            ArrayList<String> attributes = new ArrayList<String>();
            String tmp = obj.getString(ControlMessageFields.PAYLOAD.toString());
            JSONArray array = new JSONArray(tmp);
            for (int i = 0; i < array.length(); i++) {
                attributes.add(array.getString(i));
            }
            value.attributes = attributes;
        }
            break;

        case MATCH_ATTRIBUTES: {
            Map<String, Serializable> filter = new HashMap<String, Serializable>();
            String tmp = obj.getString(ControlMessageFields.PAYLOAD.toString());
            JSONObject map = new JSONObject(tmp);
            Iterator i = map.keys();
            while (i.hasNext()) {
                String name = (String) i.next();
                if (map.isNull(name)) {
                    filter.put(name, null);
                } else {
                    Object item = map.get(name);
                    filter.put(name, (Serializable) item);
                }
            }
            value.filter = filter;
        }
            break;
        case GET_ATTRIBUTES_RESPONSE: {
            Map<String, Serializable> filter = new HashMap<String, Serializable>();
            String tmp = obj.getString(ControlMessageFields.PAYLOAD.toString());
            JSONObject map = new JSONObject(tmp);
            Iterator i = map.keys();
            while (i.hasNext()) {
                String name = (String) i.next();
                Object item = map.get(name);
                filter.put(name, (Serializable) item);
            }
            value.values = filter;
        }
            break;
        case MATCH_ATTRIBUTES_RESPONSE: {
            Map<String, Serializable> filter = new HashMap<String, Serializable>();
            String tmp = obj.getString(ControlMessageFields.PAYLOAD.toString());
            if ("".equals(tmp)) {
                value.match = false;
                value.values = filter;
            } else {
                JSONObject map = new JSONObject(tmp);
                Iterator i = map.keys();
                while (i.hasNext()) {
                    String name = (String) i.next();
                    Object item = map.get(name);
                    filter.put(name, (Serializable) item);
                }
                value.values = filter;
                value.match = true;
            }
        }
            break;
        case AALSPACE_EVENT: // No payload used for this message
            break;
        default:
            throw new IllegalAccessException("Unsupported message type:"
                    + value.messageType);
        }
        return value;
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
