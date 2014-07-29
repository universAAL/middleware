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
package org.universAAL.middleware.brokers.message.deploy;

import static org.junit.Assert.*;


import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.deploy.DeployMessage.DeployMessageType;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.deploymanager.uapp.model.AalUapp.App;
import org.universAAL.middleware.deploymanager.uapp.model.ContactType;
import org.universAAL.middleware.deploymanager.uapp.model.ContactType.OtherChannel;
import org.universAAL.middleware.deploymanager.uapp.model.VersionType;

import org.universAAL.middleware.interfaces.mpa.UAPPCard;

import com.google.gson.Gson;

/**
 * 
 * 
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */

public class DeployMessageTest {
        
    @Test
    public void ByteArrayBase64EncodeSerializeDeserializeDecodeTest()
	    throws Exception {

	byte[] dati = new byte[] { (byte) 0x03, (byte) 0x08, (byte) 0x60,
		(byte) 0x99 };
	Base64.encodeBase64(dati);
	Gson gson = GsonParserBuilder.getInstance();
	String serializedMessage = gson.toJson(dati);
	System.out.println(serializedMessage.toString());
	byte[] gsondecoded = gson.fromJson(serializedMessage, byte[].class);
	System.out.println(Arrays.equals(dati, gsondecoded));

	assert (Arrays.equals(dati, gsondecoded));
    }

    @Test
    public void GSONControlMessageTest() throws Exception {

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

	Gson gson = GsonParserBuilder.getInstance();
	String serializedMessage = gson.toJson(sorgente);
	DeployMessage destinazione = gson.fromJson(serializedMessage,
		DeployMessage.class);

	System.out.println(sorgente.toString());
	System.out.println(serializedMessage.toString());
	System.out.println(destinazione.toString());

	assertEquals(sorgente.toString(), destinazione.toString());
    }

    @Test
    public void BrokerMessageTest() throws Exception {

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
	System.out.println(sorgente.toString());
	
	String serializedMessage = GsonParserBuilder.getInstance().toJson(sorgente);
	
	String destinazione =  GsonParserBuilder.getInstance().fromJson(serializedMessage,
		BrokerMessage.class).toString();

	
	System.out.println(serializedMessage.toString());
	System.out.println(destinazione.toString());

	assertEquals(sorgente.toString(), destinazione.toString());
	
	

    }

}
