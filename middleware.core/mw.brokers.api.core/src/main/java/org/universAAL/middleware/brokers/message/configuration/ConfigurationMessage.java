/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.brokers.message.configuration;

import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.gson.GsonParserBuilder;
import org.universAAL.middleware.interfaces.PeerCard;

import com.google.gson.Gson;

/**
 * @author amedrano
 *
 */
public class ConfigurationMessage implements BrokerMessage {

    private PeerCard sender;
    private PeerCard receiver;
    private ConfigurationMessageType cType;
    private String param;
    private String parsed;
    
    public enum ConfigurationMessageType{
	PROPAGATE, QUERY
    }
    
    /**
     * 
     */
    private ConfigurationMessage() {  }
    
    /**
     * Create a new Propagation type message.
     * @param propagateSerialized
     */
    public ConfigurationMessage(String propagateSerialized){
	param = propagateSerialized;
	cType = ConfigurationMessageType.PROPAGATE;
    }
    
    /**
     * Create a new Query type message.
     * @param sender
     * @param propagateSerialized
     */
    public ConfigurationMessage(PeerCard sender, String propagateSerialized){
	param = propagateSerialized;
	cType = ConfigurationMessageType.QUERY;
    }
    
    public ConfigurationMessageType getMessageType(){
	return cType;
    }
    
    public String getPayload(){
	return param;
    }

    /** {@ inheritDoc}	 */
    public BrokerMessageTypes getMType() {
	// TODO return always the same.
	return null;
    }

    /** {@ inheritDoc}	 */
    public PeerCard[] getReceivers() {
	if (receiver != null){
	    return new PeerCard[]{receiver};
	}
	return null;
    }
    
    public boolean isRequest(){
	return sender != null;
    }

    public ConfigurationMessage createResoponse(String serializedParam){
	ConfigurationMessage resp = new ConfigurationMessage();
	resp.cType = cType;
	resp.receiver = sender;
	resp.param = serializedParam;
	return resp;
    }
    
    public String toString() {
	String serializedMessage = null;
	if (parsed == null) {

	    try {
		Gson gson = GsonParserBuilder.getInstance().buildGson();
		serializedMessage = gson.toJson(this);

	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	parsed = serializedMessage;
	return parsed;
    }
    
    public static ConfigurationMessage unmarshall(String message) throws Exception {

	try {

	    Gson gson = GsonParserBuilder.getInstance().buildGson();

	    return gson.fromJson(message, ConfigurationMessage.class);

	} catch (Exception e) {

	    throw new Exception(
		    "Unable to unmashall ConfigurationMessage. Original message: "
			    + message + ". Full Stack: " + e.toString());
	}
    }
    
}
