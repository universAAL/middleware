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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessageFields;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;

/**
 * Class for AALSpace Messages
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class AALSpaceMessage implements BrokerMessage {

    private AALSpaceMessageTypes aalSpaceMessageType;
    private AALSpaceDescriptor spaceDescriptor;
    private BrokerMessageTypes mtype;

    public AALSpaceDescriptor getSpaceDescriptor() {
	return spaceDescriptor;
    }

    public void setSpaceDescriptor(AALSpaceDescriptor spaceDescriptor) {
	this.spaceDescriptor = spaceDescriptor;
    }

    public enum AALSpaceMessageTypes {
	JOIN_REQUEST, LEAVE, REQUEST_TO_LEAVE, CONNECT, DISCONNECT, JOIN_RESPONSE, NEW_PEER, PEER_LOST, NOT_AALSPACE_COODINATOR, REQUEST_PEERCARD, PEERCARD
    }

    public AALSpaceMessage(AALSpaceDescriptor spaceDescriptor,
	    AALSpaceMessageTypes type) {
	this.spaceDescriptor = spaceDescriptor;
	this.aalSpaceMessageType = type;
	this.mtype = BrokerMessageTypes.AALSpaceMessage;
    }

    public AALSpaceMessageTypes getMessageType() {
	return this.aalSpaceMessageType;
    }

    public String toString() {
	JSONObject obj = new JSONObject();
	try {

	    // marshall the broker message type
	    obj.put(BrokerMessageFields.BROKER_MESSAGE_TYPE, mtype.toString());

	    // marshall the aal space message type
	    obj.put(AALSpaceMessageFields.AAL_SPACE_MTYPE,
		    aalSpaceMessageType.toString());

	    // marshall the AALSpaceDescriptor
	    // marshal the AALspace card
	    obj.put(AALSpaceMessageFields.AAL_SPACE_NAME, spaceDescriptor
		    .getSpaceCard().getSpaceName());
	    obj.put(AALSpaceMessageFields.spaceID, spaceDescriptor
		    .getSpaceCard().getSpaceID());
	    obj.put(AALSpaceMessageFields.description, spaceDescriptor
		    .getSpaceCard().getDescription());
	    obj.put(AALSpaceMessageFields.peerCoordinatorID, spaceDescriptor
		    .getSpaceCard().getCoordinatorID());
	    obj.put(AALSpaceMessageFields.peeringChannel, spaceDescriptor
		    .getSpaceCard().getPeeringChannel());
	    obj.put(AALSpaceMessageFields.peeringChannelName, spaceDescriptor
		    .getSpaceCard().getPeeringChannelName());
	    obj.put(AALSpaceMessageFields.retry, spaceDescriptor.getSpaceCard()
		    .getRetry());
	    obj.put(AALSpaceMessageFields.aalSpaceLifeTime, spaceDescriptor
		    .getSpaceCard().getAalSpaceLifeTime());

	    // marshall broker channel
	    JSONArray brokerChannels = new JSONArray();
	    for (ChannelDescriptor channel : spaceDescriptor
		    .getBrokerChannels()) {
		JSONArray channelSerial = new JSONArray();
		channelSerial.put(channel.getChannelName());
		channelSerial.put(channel.getChannelDescriptorFileURL());
		channelSerial.put(channel.getChannelValue());
		brokerChannels.put(channelSerial);
	    }
	    obj.put(AALSpaceMessageFields.brokerChannels, brokerChannels);

	    // marshall the DeployManager's PeerCard
	    if (spaceDescriptor.getDeployManager() != null) {
		obj.put(AALSpaceMessageFields.DEPLOY_MANAGER_ID,
			spaceDescriptor.getDeployManager().getPeerID());
		obj.put(AALSpaceMessageFields.DEPLOY_MANAGER_ROLE,
			spaceDescriptor.getDeployManager().getRole().toString());
	    }
	    return obj.toString();

	} catch (JSONException e) {

	    throw new AALSpaceMessageException(
		    "Unable to unmashall AALSpaceMessage. Full Stack: "
			    + e.toString());
	} catch (Exception e) {
	    throw new AALSpaceMessageException(
		    "Unable to unmashall AALSpaceMessage. Full Stack: "
			    + e.toString());
	}
    }

    public BrokerMessageTypes getMType() {
	return mtype;
    }

    /**
     * To implement
     */
    public PeerCard[] getReceivers() {
	// TODO Auto-generated method stub
	return null;
    }

}
