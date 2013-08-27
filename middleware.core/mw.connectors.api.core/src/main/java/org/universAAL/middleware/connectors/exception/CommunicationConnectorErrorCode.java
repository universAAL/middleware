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
package org.universAAL.middleware.connectors.exception;

/**
 * List of common error code for the CommunicationConnector
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:filippo.palumbo@isti.cnr.it">Filippo Palumbo</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public enum CommunicationConnectorErrorCode {
    NEW_CHANNEL_ERROR, SEND_MESSAGE_ERROR, CHANNEL_INIT_ERROR,

    /**
     * This error code means that the message was not sent because the
     * destination of the message was not found among the member of the JGroups
     * cluster (e.g. the channel in uAAL jargon)
     */
    RECEIVER_NOT_EXISTS,

    /**
     * This error code means that the message that we are trying to send does
     * not contain the name of the JGroup cluster (e.g. the channel in uAAL
     * jargon) where JGroups has to push the message
     */
    NO_CHANNEL_SPECIFIED,

    /**
     * This error code means that we are trying to perform and unicast
     * communication, but the message contains multiple receivers
     */
    MULTIPLE_RECEIVERS,

    /**
     * This error code means that the JGroup cluster (e.g. the channel in uAAL
     * jargon) destination has not been found among our JGroups cluster that we
     * are joined to
     */
    CHANNEL_NOT_FOUND,

    /**
     * This error code means that we are not connected to the JGroup cluster
     * (e.g. the channel in uAAL jargon) destination
     */
    NOT_CONNECTED_TO_CHANNEL;
}
