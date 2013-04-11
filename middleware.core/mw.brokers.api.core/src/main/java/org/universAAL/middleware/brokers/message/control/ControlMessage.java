package org.universAAL.middleware.brokers.message.control;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessageFields;
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
 */
public class ControlMessage implements BrokerMessage {

	private ControlMessageType messageType;
	private BrokerMessageTypes mType;
	private AALSpaceDescriptor space;
	private String payload;

	public enum ControlMessageType {
		AALSPACE_EVENT, GET_ATTRIBUTES, MATCH_ATTRIBUTES
	}
	
	public enum ControlMessageFields {
		TYPE, PAYLOAD
	}

	public ControlMessage(AALSpaceDescriptor space, ControlMessageType type, String payload) {
		this.space = space;
		this.messageType = type;
		this.payload = payload;
	}

	public String toString() {
        JSONObject obj = new JSONObject();

        try {
            obj.put(ControlMessageFields.TYPE.toString(), mType.toString());
            obj.put(ControlMessageFields.PAYLOAD.toString(), payload );
        } catch (JSONException e) {
            new DeployMessageException("Unable to unmarshall ControlMessage, invalid JSON: "
                    + e.toString(), e);
        } catch (Exception e) {
            new DeployMessageException("Unable to unmarshall ControlMessage: "
                    + e.toString(), e);
        }
        
        return obj.toString();
	}

	public ControlMessageType getMessageType() {
		return messageType;
	}

	public String marshall(BrokerMessage message) {
		// TODO Auto-generated method stub
		return null;
	}

	public BrokerMessage unmarshall(String message) {
		// TODO Auto-generated method stub
		return null;
	}

	public BrokerMessageTypes getMType() {
		// TODO Auto-generated method stub
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
