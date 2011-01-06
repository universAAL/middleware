/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.universAAL.middleware.acl.upnp.exporter.actions;

import java.util.Dictionary;

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPStateVariable;

import org.universAAL.middleware.acl.SodaPopPeer;

/* 
* @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
*/

public class NoticePeerBussesAction implements UPnPAction {

	public final static String NAME = "NoticePeerBusses";
	public final static String BUSSES_NAME = "BussesName";
	public final static String PEER_ID = "PeerID";
	final private String[] IN_ARG_NAMES = new String[]{BUSSES_NAME,PEER_ID};
	private UPnPStateVariable peerId,busName;
	private SodaPopPeer localPeer;
	
	
	public NoticePeerBussesAction(SodaPopPeer localPeer,UPnPStateVariable peerId, UPnPStateVariable busName){
		this.peerId = peerId;
		this.busName = busName;
		this.localPeer=localPeer;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPAction#getName()
	 */
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPAction#getReturnArgumentName()
	 */
	public String getReturnArgumentName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPAction#getInputArgumentNames()
	 */
	public String[] getInputArgumentNames() {
		return IN_ARG_NAMES;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPAction#getOutputArgumentNames()
	 */
	public String[] getOutputArgumentNames() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPAction#getStateVariable(java.lang.String)
	 */
	public UPnPStateVariable getStateVariable(String argumentName) {
		if (argumentName.equals(BUSSES_NAME))
			return busName;
		else if (argumentName.equals(PEER_ID))
			return peerId;
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPAction#invoke(java.util.Dictionary)
	 */
	public synchronized Dictionary invoke(Dictionary args) throws Exception {
		String bussesName = (String) args.get(BUSSES_NAME);
		String peerID = (String) args.get(PEER_ID);
		//System.out.println("LOCAL_PEER:: noticePeerBusses invoked ## "+bussesName +", "+peerID);
		localPeer.noticePeerBusses(peerID,bussesName);
		//System.out.println("LOCAL_PEER:: noticePeerBusses returning");
		return null;
	}
}
