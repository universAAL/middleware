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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;

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
        JSONObject obj = new JSONObject();
        try {
            // marshall channel names
            JSONArray channelSerial = new JSONArray();
            for (Object ch : channelNames) {
                String channelName = (String) ch;
                channelSerial.put(channelName);
            }

            obj.put(ChannelMessageFields.CHANNEL_NAMES, channelSerial);

            // marshall PeerCard
            obj.put(ChannelMessageFields.PEER_ID, sender.getPeerID());
            obj.put(ChannelMessageFields.PEER_ROLE, sender.getRole().toString());

            // marshall the content
            obj.put(ChannelMessageFields.CONTENT, content);
        } catch (JSONException e) {
            return "";
        }
        return obj.toString();

    }

    public static ChannelMessage unmarhall(String message) throws Exception {
        JSONObject obj = new JSONObject(message);

        // unmarshall the channel Names
        JSONArray channelNamesSerial = obj
                .getJSONArray(ChannelMessageFields.CHANNEL_NAMES);
        List channelNames = new ArrayList();

        for (int i = 0; i < channelNamesSerial.length(); i++) {
            channelNames.add(channelNamesSerial.get(i));
        }

        // unmarshall PeerCard
        PeerCard sender = new PeerCard(
                obj.getString(ChannelMessageFields.PEER_ID),
                PeerRole.valueOf(obj.getString(ChannelMessageFields.PEER_ROLE)));

        // unmarshall the content

        String content = obj.getString(ChannelMessageFields.CONTENT);

        ChannelMessage ch = new ChannelMessage(sender, content, channelNames);
        return ch;

    }

}
