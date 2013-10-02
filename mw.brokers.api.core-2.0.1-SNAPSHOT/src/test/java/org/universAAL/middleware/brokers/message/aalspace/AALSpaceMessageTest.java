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
package org.universAAL.middleware.brokers.message.aalspace;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage.AALSpaceMessageTypes;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;

import com.google.gson.Gson;

/**
 * 
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */

public class AALSpaceMessageTest {

    @Test
    public void testGSONMessageAALSpace() throws Exception {

	AALSpaceCard spaceCard = new AALSpaceCard();
	spaceCard.setSpaceID("id");
	spaceCard.setDescription("descrizione");
	spaceCard.setPeerCoordinatorID("coord{   }&)?id");
	spaceCard.setPeeringChannel("canale");
	spaceCard.setPeeringChannelName("nome canaèle");
	spaceCard.setProfile("profilo");
	spaceCard.setRetry(10);
	ChannelDescriptor chd = new ChannelDescriptor("1339517729177690537L",
		"nome!", "http://google.it");

	List<org.universAAL.middleware.interfaces.ChannelDescriptor> brokerChannels = new ArrayList<org.universAAL.middleware.interfaces.ChannelDescriptor>();
	brokerChannels.add(chd);

	AALSpaceDescriptor spaceDescriptor = new AALSpaceDescriptor(spaceCard,
		brokerChannels);

	PeerCard deployManager = new PeerCard(PeerRole.PEER, "stringa1",
		"stringa2");

	spaceDescriptor.setDeployManager(deployManager);

	AALSpaceMessage original = new AALSpaceMessage(spaceDescriptor,	AALSpaceMessageTypes.CONNECT);

	String serializedMessage = GsonParserBuilder.getInstance().buildGson().toJson(original);

	AALSpaceMessage decodedMessage = GsonParserBuilder.getInstance().buildGson().fromJson(serializedMessage,
		AALSpaceMessage.class);

	System.out.println(original.toString());
	System.out.println(serializedMessage.toString());
	System.out.println(decodedMessage.toString());
	
	assertEquals(original.toString(), decodedMessage.toString());

    }

}
