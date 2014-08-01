package org.universAAL.middleware.connectors.discovery.jgroups.core.messages;

import java.util.List;

import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.model.IAALSpace;

public class Response extends ChannelMessage{

	private IAALSpace spaceCard;
	
	public Response(PeerCard sender, String content, List channelNames) {
		super(sender, content, channelNames);
		// TODO Auto-generated constructor stub
	}

}
