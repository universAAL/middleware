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
package org.universAAL.middleware.brokers.message.control;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessage.BrokerMessageTypes;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage.AALSpaceMessageTypes;
import org.universAAL.middleware.brokers.message.control.ControlMessage.ControlMessageType;
import org.universAAL.middleware.brokers.message.deploy.DeployMessage;
import org.universAAL.middleware.brokers.message.deploy.DeployPayload;
import org.universAAL.middleware.brokers.message.deploy.DeployMessage.DeployMessageType;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.deploymanager.uapp.model.ContactType;
import org.universAAL.middleware.deploymanager.uapp.model.VersionType;
import org.universAAL.middleware.deploymanager.uapp.model.AalUapp.App;
import org.universAAL.middleware.deploymanager.uapp.model.ContactType.OtherChannel;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.mpa.UAPPCard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */

public class ControlMessageTest {

    @Test
    public void testControlMessageAttributes() throws Exception {
	ControlMessage original = new ControlMessage(null,
		Arrays.asList(new String[] { "ciao", "mio" }));
	String txt = original.toString();
	ControlMessage parsed = ControlMessage.unmarshall(txt);
	assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageAttributesEscaping() throws Exception {
	{
	    ControlMessage original = new ControlMessage(null,
		    Arrays.asList(new String[] { "{ciao", "mio" }));
	    String txt = original.toString();
	    ControlMessage parsed = ControlMessage.unmarshall(txt);
	    assertEquals(txt, parsed.toString());
	}
	{
	    ControlMessage original = new ControlMessage(
		    null,
		    Arrays.asList(new String[] { "\"ci/a\n\r\u9922o", "\\mio}" }));
	    String txt = original.toString();
	    ControlMessage parsed = ControlMessage.unmarshall(txt);
	    assertEquals(txt, parsed.toString());
	}

    }

    @Test
    public void testControlMessageGetPeers() throws Exception {
	HashMap<String, Serializable> filter = new HashMap<String, Serializable>();
	filter.put("OS", "android");
	filter.put("system", null);
	filter.put("sysversion", 3);
	ControlMessage original = new ControlMessage(null, filter);
	String txt = original.toString();
	ControlMessage parsed = ControlMessage.unmarshall(txt);
	assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageGetPeersEscaping() throws Exception {
	HashMap<String, Serializable> filter = new HashMap<String, Serializable>();
	filter.put("OS", "{}\\}\\{");
	filter.put("system", "\"ci/a\n\r\u9922o}");
	ControlMessage original = new ControlMessage(null, filter);
	String txt = original.toString();
	ControlMessage parsed = ControlMessage.unmarshall(txt);
	assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageAttibuteValues() throws Exception {
	HashMap<String, Serializable> filter = new HashMap<String, Serializable>();
	filter.put("OS", "{}\\}\\{");
	filter.put("system", "\"ci/a\n\r\u9922o}");
	ControlMessage original = new ControlMessage(null, UUID.randomUUID()
		.toString(), filter);
	String txt = original.toString();
	ControlMessage parsed = ControlMessage.unmarshall(txt);
	assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageMatchingPeerResponse() throws Exception {
	HashMap<String, Serializable> map = new HashMap<String, Serializable>();
	map.put("OS", "{}\\}\\{");
	map.put("system", "\"ci/a\n\r\u9922o}");
	ControlMessage original = new ControlMessage(null, UUID.randomUUID()
		.toString(), map, true);
	String txt = original.toString();
	ControlMessage parsed = ControlMessage.unmarshall(txt);
	assertEquals(txt, parsed.toString());
    }

    @Test
    public void testControlMessageMatchingPeerResponseFalse() throws Exception {
	HashMap<String, Serializable> map = new HashMap<String, Serializable>();
	map.put("OS", "{}\\}\\{");
	map.put("system", "\"ci/a\n\r\u9922o}");
	ControlMessage original = new ControlMessage(null, UUID.randomUUID()
		.toString(), map, false);
	String txt = original.toString();
	ControlMessage parsed = ControlMessage.unmarshall(txt);
	assertEquals(txt, parsed.toString());
    }

    @Test
    public void GSONControlMessageTest() throws Exception {

	ControlMessageType messageType = ControlMessageType.GET_ATTRIBUTES;

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
	String id = "stringa id";

	String payload = "blablabla";

	String parsed = null;

	ArrayList<String> attributes = new ArrayList<String>();
	attributes.add("ciao");

	HashMap<String, Serializable> filter = new HashMap<String, Serializable>();
	filter.put("filtro1", "hello");
	filter.put("filtro2", null);
	filter.put("filtro3", 3);
	filter.put("filtro4", "{}\\}\\{");
	filter.put("filtro5", "\"ci/a\n\r\u9922o}");

	HashMap<String, Serializable> values = new HashMap<String, Serializable>();
	values.put("one", "hello");
	values.put("system", null);
	values.put("sysversion", 3);
	values.put("O7S", "{}\\}\\{");
	values.put("sysyuem", "\"ci/a\n\r\u9922o}");

	boolean match = true;

	String TransactionID = "id transazione";

	ControlMessage sorgente = new ControlMessage(spaceDescriptor,
		attributes);

	Gson gson = GsonParserBuilder.getInstance();
	String serializedMessage = gson.toJson(sorgente);
	ControlMessage destinazione = gson.fromJson(serializedMessage,
		ControlMessage.class);
	assertEquals(sorgente.toString(), destinazione.toString());
    }

    @Test
    public void GSONC1ontrolMessageTest() throws Exception {

	Gson gson = GsonParserBuilder.getInstance();

	DeployMessageType type = DeployMessageType.REQUEST_TO_INSTALL_PART;

	byte[] bytearray = new byte[] { 4, 4, 56, 35, 64, (byte) 0xfe6, 4 };

	App uapp = new App();
	uapp.setAppId("10");
	uapp.setApplicationProfile("ciao");
	uapp.setDescription("descrizione app");
	uapp.setMultipart(true);
	uapp.setName("nome");
	uapp.setTags("tags");
	VersionType tipoversione = new VersionType();

	tipoversione.setBuild("stringa build");
	tipoversione.setMajor(1);
	tipoversione.setMicro(2);
	tipoversione.setMinor(3);

	uapp.setVersion(tipoversione);
	ContactType contatto = new ContactType();
	contatto.setCertificate("certificato");
	contatto.setContactPerson("persona");
	contatto.setEmail("indirizzo email");
	contatto.setOrganizationName("nome Organizzazione");
	contatto.setPhone("32525");
	contatto.setStreetAddress("indirizzo");
	contatto.setWebAddress("web address");
	OtherChannel altrocanale = new OtherChannel();

	altrocanale.setChannelName("nome altro canale");
	altrocanale.setChannelDetails("dettagli");

	contatto.setOtherChannel(altrocanale);

	uapp.setApplicationProvider(contatto);

	UAPPCard card = new UAPPCard("service id", "part id", uapp);
	DeployPayload payload = new DeployPayload(bytearray, card);

	DeployMessage sorgente = new DeployMessage(type, payload);

	BrokerMessage message = sorgente;

	String serial = gson.toJson(sorgente);

	BrokerMessage brokerMessage = gson
		.fromJson(serial, BrokerMessage.class);

	DeployMessage deployMessage = null;
	if (brokerMessage instanceof DeployMessage) {
	    deployMessage = (DeployMessage) brokerMessage;

	}

	assertEquals(sorgente.toString(), deployMessage.toString());

    }

}
