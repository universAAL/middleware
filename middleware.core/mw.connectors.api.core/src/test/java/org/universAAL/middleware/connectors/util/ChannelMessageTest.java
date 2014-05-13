package org.universAAL.middleware.connectors.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ChannelMessageTest {

    private PeerCard card = new PeerCard(PeerRole.COORDINATOR, "OSGi", "universAAL");
    
    @Test
    public void testJSon() {
	ChannelMessage msg = new ChannelMessage(card, "[ 3, 4, 5 ]", null);
	String json = msg.toString();
	System.out.println(json);
	Gson gson = new Gson();
	ChannelMessage parsed = gson.fromJson(json, ChannelMessage.class);
//	System.out.println(card);
//	System.out.println(parsed.getSender());
//	System.out.println(msg.getContent());
//	System.out.println(parsed.getContent());
	assertEquals("Problem serializing/deserializing PeerCard", card, parsed.getSender());
	assertEquals("Problem serializing/deserializing Content", msg.getContent(), parsed.getContent());
	assertEquals("Comparing JSon(s)",json, parsed.toString());
    }

}
