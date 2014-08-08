package org.universAAL.middleware.connectors.discovery.jgroups.core.messages;

import java.util.Dictionary;
import java.util.Hashtable;

import org.universAAL.middleware.interfaces.PeerCard;

import com.google.gson.Gson;

public class Query extends DiscoveryMessage{

	private Hashtable<String, String> filter;
	
	public Query(PeerCard sender, Hashtable<String, String> filter) {
		super(sender, filter.toString());
		this.setMessageType(DiscoverMessageType.QUERY);
		this.filter = filter;
	}

	public Query() {
	}

	public static Query unmarshall(String message) throws Exception {

		Query queryMessage = null;
		try {

			Gson gson = new Gson();
			queryMessage = gson.fromJson(message, Query.class);

		} catch (Exception e) {

			throw new Exception(
					"Unable to unmashall Query Message. Original message: "
							+ message + ". Full Stack: " + e.toString());
		}

		return queryMessage;

	}

	public Dictionary<String, String> getFilter() {
		return filter;
	}

	public void setFilter(Hashtable<String, String> filter) {
		this.filter = filter;
	}
}
