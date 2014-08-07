package org.universAAL.middleware.connectors.discovery.jgroups.core.messages;

import org.universAAL.middleware.interfaces.PeerCard;

import com.google.gson.Gson;

public class DiscoveryMessage {

	/**
	 * Message sender
	 */
	private PeerCard sender;
	
	/**
	 * The body of the message as a String
	 */
	private String content;

	/**
	 * The type of the message
	 */
	public DiscoverMessageType messageType;
	
	public DiscoveryMessage() {
    }
	
	public DiscoveryMessage(PeerCard sender, String content) {
        this.sender = sender;
        this.content = content;
    }

	public PeerCard getSender() {
		return sender;
	}

	public void setSender(PeerCard sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public DiscoverMessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(DiscoverMessageType messageType) {
		this.messageType = messageType;
	}

	public String toString() {
		String serializedMessage = null;
		try {
			Gson gson = new Gson();
			serializedMessage = gson.toJson(this);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return serializedMessage;
	}

	public static DiscoveryMessage unmarshall(String message) throws Exception {

		DiscoveryMessage discoveryMessage = null;
		try {

			Gson gson = new Gson();
			discoveryMessage = gson.fromJson(message, DiscoveryMessage.class);

		} catch (Exception e) {

			throw new Exception(
					"Unable to unmashall DiscoveryMessage. Original message: "
							+ message + ". Full Stack: " + e.toString());
		}

		return discoveryMessage;

	}

}
