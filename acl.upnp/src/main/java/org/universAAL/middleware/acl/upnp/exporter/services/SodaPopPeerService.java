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

package org.universAAL.middleware.acl.upnp.exporter.services;

import java.util.HashMap;

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPService;
import org.osgi.service.upnp.UPnPStateVariable;
import org.universAAL.middleware.acl.upnp.exporter.actions.GetIdAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.JoinBusAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.LeaveBusAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.NoticePeerBussesAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.PrintStatusAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.ProcessBusMessageAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.ReplyPeerBussesAction;
import org.universAAL.middleware.acl.upnp.exporter.stateVariables.BusNameStateVariable;
import org.universAAL.middleware.acl.upnp.exporter.stateVariables.MessageStateVariable;
import org.universAAL.middleware.acl.upnp.exporter.stateVariables.PeerIDStateVariable;

import org.universAAL.middleware.acl.SodaPopPeer;

/* 
* @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
*/

public class SodaPopPeerService implements UPnPService{
	
	public final static String SERVICE_ID = "urn:upnp-org:serviceId:SodaPopPeer:1";
	public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:SodaPopPeer:1";
	public final static String VERSION ="1";

	private UPnPStateVariable peerId,busName,message;
	private UPnPStateVariable[] states;
	private HashMap actions = new HashMap();
	
	
	public SodaPopPeerService(SodaPopPeer localPeer){
		peerId = new PeerIDStateVariable();
		busName = new BusNameStateVariable();
		message = new MessageStateVariable();
		
		this.states = new UPnPStateVariable[]{peerId,busName,message};
		
		UPnPAction getId = new GetIdAction(localPeer,peerId);
		UPnPAction joinBus = new JoinBusAction(localPeer,busName,peerId);
		UPnPAction leaveBus = new LeaveBusAction(localPeer,busName,peerId);
		UPnPAction noticePeerBusses = new NoticePeerBussesAction(localPeer,peerId,busName);
		UPnPAction replyPeerBusses = new ReplyPeerBussesAction(localPeer,peerId,busName);
		UPnPAction processBusMessage = new ProcessBusMessageAction(localPeer,busName,message);
		UPnPAction printStatus = new PrintStatusAction(localPeer,busName);
		actions.put(getId.getName(),getId);
		actions.put(joinBus.getName(),joinBus);
		actions.put(leaveBus.getName(),leaveBus);
		actions.put(noticePeerBusses.getName(),noticePeerBusses);
		actions.put(replyPeerBusses.getName(),replyPeerBusses);
		actions.put(processBusMessage.getName(),processBusMessage);
		actions.put(printStatus.getName(),printStatus);
		
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPService#getId()
	 */
	public String getId() {
		return SERVICE_ID;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPService#getType()
	 */
	public String getType() {
		return SERVICE_TYPE;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPService#getVersion()
	 */
	public String getVersion() {
		return VERSION;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPService#getAction(java.lang.String)
	 */
	public UPnPAction getAction(String name) {
		return (UPnPAction)actions.get(name);
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPService#getActions()
	 */
	public UPnPAction[] getActions() {
		return (UPnPAction[])(actions.values()).toArray(new UPnPAction[]{});
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPService#getStateVariables()
	 */
	public UPnPStateVariable[] getStateVariables() {
		return states;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPService#getStateVariable(java.lang.String)
	 */
	public UPnPStateVariable getStateVariable(String name) {
		for (int i=0; i<states.length; i++) {
			if (name.equals(states[i].getName()))
				return states[i];			
		}
		return null;
	}
}
