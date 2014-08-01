package org.universAAL.middleware.connectors.discovery.jgroups.core.messages;

import java.util.Dictionary;
import java.util.List;

import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;

public class Query extends ChannelMessage{

	private Dictionary<String, String> filter;
	
	public Query(PeerCard sender, String content, List channelNames) {
		super(sender, content, channelNames);
		// TODO Auto-generated constructor stub
	}

}
