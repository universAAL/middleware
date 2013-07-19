/*

        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */
package org.universAAL.middleware;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Spring;

import org.junit.Test;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage.AALSpaceMessageTypes;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;


import com.google.gson.Gson;

/**
 * 
 * 
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */

public class ChannelMessageTests {
  
	  @Test
	    public void ChannelMessageTest() throws Exception 
	    {
		  
		  
		  
		  
		  
		  String d = "stringgaaaaa";
		PeerRole e = PeerRole.COORDINATOR;
		PeerCard a =new PeerCard(d, e );
		String b ="striunbgaaaa";
		List<String> c = new ArrayList<String>();
		c.add("ciaoaao");
		ChannelMessage original = new ChannelMessage(a, b, c);
		
		
		String serializedMessage = GsonParserBuilder.getInstance().buildGson().toJson(original);

		ChannelMessage decodedMessage = GsonParserBuilder.getInstance().buildGson().fromJson(serializedMessage,
				ChannelMessage.class);

		System.out.println(original.toString());
		System.out.println(serializedMessage.toString());
		System.out.println(decodedMessage.toString());
		  
		  assertEquals(original.toString(), decodedMessage.toString());
	    }

}
