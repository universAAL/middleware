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
    private boolean request = true;
    
    public enum ConfigurationMessageType{
	PROPAGATE, QUERY
    }
    
    /**
     * 
     */
    private ConfigurationMessage() {  }
    
    
    /**
     * Create a new Configuration Message.
     * @param mType the type of the message to sent
     * @param sender the sender of this message
     * @param propagateSerialized
     */
    public ConfigurationMessage(ConfigurationMessageType mType, PeerCard sender, String propagateSerialized){
	this.cType = mType;
	this.param = propagateSerialized;
	this.sender = sender;
    }
    
    public ConfigurationMessageType getMessageType(){
	return cType;
    }
    
    public String getPayload(){
	return param;
    }

    /** {@ inheritDoc}	 */
    public BrokerMessageTypes getMType() {
	return BrokerMessageTypes.ConfigurationMessage;
    }

    /** {@ inheritDoc}	 */
    public PeerCard[] getReceivers() {
	if (receiver != null){
	    return new PeerCard[]{receiver};
	}
	return new PeerCard[]{};
    }
    
    public boolean isRequest(){
	return request;
    }

    public ConfigurationMessage createResoponse(String serializedParam){
	ConfigurationMessage resp = new ConfigurationMessage();
	resp.cType = cType;
	resp.receiver = sender;
	resp.param = serializedParam;
	resp.request = false;
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


    /**
     * @return
     */
    public boolean isSentFrom(PeerCard sender) {
	return this.sender.equals(sender);
    }
    
}
