/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

package org.universAAL.middleware.ui.impl.generic;

import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.rdf.Resource;

/**
 * @author amedrano
 * 
 */
public abstract class CallMessage<Strategy extends CallBasedStrategy> extends
	Resource implements EventMessage<Strategy> {

    /** {@ inheritDoc} */
    public void onReceived(Strategy strategy, BusMessage m, String senderID) {
	if (m.getType().equals(MessageType.p2p_request)
		|| m.getType().equals(MessageType.request)) {
	    onRequest(strategy, m, senderID);
	} else {
	    onResponse(strategy, m, senderID);
	}

    }

    /**
     * @param strategy
     * @param m
     * @param senderID
     */
    protected abstract void onRequest(Strategy strategy, BusMessage m,
	    String senderID);

    /**
     * @param strategy
     * @param m
     * @param senderID
     */
    protected abstract void onResponse(Strategy strategy, BusMessage m,
	    String senderID);

}
