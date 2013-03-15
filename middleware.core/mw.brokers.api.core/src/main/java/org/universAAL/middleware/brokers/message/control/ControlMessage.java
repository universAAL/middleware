package org.universAAL.middleware.brokers.message.control;

import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.interfaces.PeerCard;

/**
 * Control message
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class ControlMessage implements BrokerMessage {

    private ControlMessageType messageType;
    public BrokerMessageTypes mType;

    public enum ControlMessageType {
	AALSPACE_EVENT
    }

    public ControlMessage(ControlMessageType type) {

	this.messageType = type;
    }

    public String toString() {
	return toString();
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
