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

import org.universAAL.middleware.brokers.message.Payload;
import org.universAAL.middleware.interfaces.mpa.UAPPCard;

/**
 * Payload for Deploy messages
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class DeployPayload extends Payload {

    private byte[] part;
    private static final long serialVersionUID = 4155680139788101950L;
    private UAPPCard uappCard;

    public DeployPayload(byte[] part, UAPPCard mpaCard) {

	this.part = part;
	this.uappCard = mpaCard;
    }

    public byte[] getPart() {
	return part;
    }

    public void setPart(byte[] part) {
	this.part = part;
    }

    public UAPPCard getuappCard() {
	return uappCard;
    }

    public void setuappCard(UAPPCard mpaCard) {
	this.uappCard = mpaCard;
    }

}
