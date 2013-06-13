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

import org.universAAL.middleware.interfaces.mpa.UAPPCard;
import org.universAAL.middleware.interfaces.mpa.UAPPPartStatus;

/**
 * Payload for deploy notifications
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class DeployNotificationPayload extends DeployPayload {

    private static final long serialVersionUID = -3622178451748007711L;
    private String mpaPartID;
    private UAPPPartStatus mpaPartStatus;

    public DeployNotificationPayload(byte[] part, UAPPCard mpaCard,
	    String partID, UAPPPartStatus mpaPartStatus) {
	super(part, mpaCard);
	this.mpaPartID = partID;
	this.mpaPartStatus = mpaPartStatus;
    }

    public String getPartID() {
	return mpaPartID;
    }

    public UAPPPartStatus getMpaPartStatus() {
	return mpaPartStatus;
    }

}
