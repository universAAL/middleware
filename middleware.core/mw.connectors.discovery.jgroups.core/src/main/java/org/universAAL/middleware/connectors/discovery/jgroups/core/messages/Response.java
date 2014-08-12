package org.universAAL.middleware.connectors.discovery.jgroups.core.messages;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import com.google.gson.Gson;

public class Response extends DiscoveryMessage{

	private AALSpaceCard spaceCard;

	public Response() {
	}
	
	public Response(PeerCard sender, AALSpaceCard spaceCard) {
		super(sender, spaceCard.toString());
		this.setMessageType(DiscoverMessageType.RESPONSE);
		this.setSpaceCard(spaceCard);
	}
	
	public static Response unmarshall(String message) throws Exception {

		Response responseMessage = null;
		try {

			Gson gson = new Gson();
			responseMessage = gson.fromJson(message, Response.class);

		} catch (Exception e) {

			throw new Exception(
					"Unable to unmashall Announce Message. Original message: "
							+ message + ". Full Stack: " + e.toString());
		}

		return responseMessage;

	}

	public AALSpaceCard getSpaceCard() {
		return spaceCard;
	}

	public void setSpaceCard(AALSpaceCard spaceCard) {
		this.spaceCard = spaceCard;
	}
}
