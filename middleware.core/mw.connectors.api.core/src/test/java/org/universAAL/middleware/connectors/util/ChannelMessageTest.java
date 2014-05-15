package org.universAAL.middleware.connectors.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;

import com.google.gson.Gson;

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

    @Test
    public void unmarhallJSonFromFile(){
        String JSonDump = "./ChannelMeessage.dump.json";
        URL res = ChannelMessageTest.class.getResource(JSonDump);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(res.openStream()));
            String msg = br.readLine();
            Gson parser = new Gson();
            while (msg != null){
                System.out.println(msg);
                ChannelMessage chMsg = parser.fromJson(msg, ChannelMessage.class);
                msg = br.readLine();
                //assertEquals("Failed to handle message"+msg, chMsg.toString(), msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception has been thrown");
        }

    }
}
