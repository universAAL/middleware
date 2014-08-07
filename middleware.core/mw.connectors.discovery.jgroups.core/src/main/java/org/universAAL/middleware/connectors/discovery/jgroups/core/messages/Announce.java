package org.universAAL.middleware.connectors.discovery.jgroups.core.messages;

import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import com.google.gson.Gson;

public class Announce extends DiscoveryMessage{

	private AALSpaceCard spaceCard;
	
	public Announce() {
	}

	public Announce(PeerCard sender, AALSpaceCard spaceCard) {
		super(sender, spaceCard.toString());
		this.setMessageType(DiscoverMessageType.ANNOUNCE);
		this.setSpaceCard(spaceCard);
	}

	public AALSpaceCard getSpaceCard() {
		return spaceCard;
	}

	public void setSpaceCard(AALSpaceCard spaceCard) {
		this.spaceCard = spaceCard;
	}

	public static Announce unmarshall(String message) throws Exception {

		Announce announceMessage = null;
		try {

			Gson gson = new Gson();
			announceMessage = gson.fromJson(message, Announce.class);

		} catch (Exception e) {

			throw new Exception(
					"Unable to unmashall Announce Message. Original message: "
							+ message + ". Full Stack: " + e.toString());
		}

		return announceMessage;

	}
}
