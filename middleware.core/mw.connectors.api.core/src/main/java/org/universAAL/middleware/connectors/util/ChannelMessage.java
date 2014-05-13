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
package org.universAAL.middleware.connectors.util;

import java.util.ArrayList;
import java.util.List;


import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;

import com.google.gson.Gson;

/**
 * Row message type that wraps the BrokerMessage type
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
*/
public class ChannelMessage {

    /**
     * The list of channels to which to send the message
     */
    private List channelNames;
    /**
     * Message sender
     */
    private PeerCard sender;
    private String content;

    /*
    private ChannelMessage() {
    }
	*/
    
    public ChannelMessage(PeerCard sender, String content, List channelNames) {
        this.sender = sender;
        this.content = content;
        this.channelNames = channelNames;
    }

    public PeerCard getSender() {
        return sender;
    }

    public void setSender(PeerCard sender) {
        this.sender = sender;
    }

    public List getChannelNames() {
        return channelNames;
    }

    public void setChannelNames(List channelNames) {
        this.channelNames = channelNames;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        String serializedMessage=null;
        try {
            Gson gson = new Gson();
            serializedMessage = gson.toJson(this);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return serializedMessage;
    }

    public static ChannelMessage unmarshall(String message) throws Exception {

        ChannelMessage ch=null;
        try {

                    Gson gson = new Gson();
                 ch = gson.fromJson(message, ChannelMessage.class);

        } catch (Exception e) {

            throw new Exception(
                    "Unable to unmashall AALSpaceMessage. Original message: "
                            + message + ". Full Stack: " + e.toString());
        }

        return ch;

    }

}
