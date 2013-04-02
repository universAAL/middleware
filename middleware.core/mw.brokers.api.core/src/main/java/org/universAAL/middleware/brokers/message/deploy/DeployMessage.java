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

import java.util.Arrays;
import java.util.Collections;

import org.json.JSONException;
import org.json.JSONObject;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessageFields;
import org.universAAL.middleware.interfaces.PeerCard;

/**
 * Deploy message
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public class DeployMessage implements BrokerMessage {

    public enum DeployMessageType {
        REQUEST_TO_INSTALL_PART, REQUEST_TO_UNINSTALL_PART, PART_NOTIFICATION;

    }

    private DeployPayload payload;
    private DeployMessageType deployMessageType;
    public BrokerMessageTypes mType;

    public DeployMessage(DeployMessageType messageType, DeployPayload payload) {

        this.deployMessageType = messageType;
        this.payload = payload;
        this.mType = BrokerMessageTypes.DeployMessage;
    }

    public DeployMessageType getMessageType() {
        return deployMessageType;
    }

    public void setMessageType(DeployMessageType messageType) {
        this.deployMessageType = messageType;
    }

    public String toString() {
        JSONObject obj = new JSONObject();

        try {

            // marhall the type
            obj.put(BrokerMessageFields.BROKER_MESSAGE_TYPE, mType.toString());

            // marhall deploy message type
            obj.put(DeployMessageFields.DEPLOY_MTYPE,
                    deployMessageType.toString());

            // marshall uAPP Card
            obj.put(DeployMessageFields.UAPP_NAME, payload.getuappCard()
                    .getName());
            obj.put(DeployMessageFields.UAPP_ID, payload.getuappCard().getId());
            obj.put(DeployMessageFields.UAPP_DESC, payload.getuappCard()
                    .getDescription());

            // marhall payload
            if (payload != null
                    && payload instanceof DeployNotificationPayload == false) {
                obj.put(DeployMessageFields.DEPLOY_PAYLOAD, "1");

                // marshall the part as a String
                //obj.put(DeployMessageFields.PART, new String(payload.getPart()).getBytes("UTF-8"));
                obj.put(DeployMessageFields.PART, Arrays.asList( payload.getPart() ) );
            } else if (payload != null
                    && payload instanceof DeployNotificationPayload) {

                obj.put(DeployMessageFields.DEPLOY_PAYLOAD, "2");

                DeployNotificationPayload deployNoPayload = (DeployNotificationPayload) payload;

                // marhsall uAPP PArt ID
                obj.put(DeployMessageFields.PART_ID,
                        deployNoPayload.getPartID());

                // Marshall UAPP part status
                obj.put(DeployMessageFields.PART_STATUS, deployNoPayload
                        .getMpaPartStatus().ordinal());

            }

        } catch (JSONException e) {
            new DeployMessageException("Unable to unmarshall message: "
                    + e.toString(), e);
        } catch (Exception e) {
            new DeployMessageException("Unable to unmarshall message: "
                    + e.toString(), e);
        }
        return obj.toString();
    }

    public DeployPayload getPayload() {
        return payload;

    }

    public BrokerMessageTypes getMType() {
        return mType;
    }

    /**
     * To implement
     */
    public PeerCard[] getReceivers() {
        // TODO Auto-generated method stub
        return null;
    }

}
