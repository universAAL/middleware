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

package org.persona.mw.acl.upnp.exporter.actions;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPStateVariable;

import de.fhg.igd.ima.sodapop.p2p.SodaPopPeer;

/* 
* @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
*/

public class GetIdAction implements UPnPAction {

	public final static String NAME = "GetID";
	public final static String RESULT_ID = "ResultID";
	final private String[] OUT_ARG_NAMES = new String[]{RESULT_ID};
	private UPnPStateVariable peerId;
	private SodaPopPeer localPeer;
	
	
	public GetIdAction(SodaPopPeer localPeer,UPnPStateVariable peerId){
		this.peerId = peerId;
		this.localPeer = localPeer;
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
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPAction#getOutputArgumentNames()
	 */
	public String[] getOutputArgumentNames() {
		return OUT_ARG_NAMES;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPAction#getStateVariable(java.lang.String)
	 */
	public UPnPStateVariable getStateVariable(String argumentName) {
		return peerId;
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.upnp.UPnPAction#invoke(java.util.Dictionary)
	 */
	public Dictionary invoke(Dictionary args) throws Exception {
		//System.out.println("LOCAL_PEER:: GetID invoked");
		String id = localPeer.getID();
		Hashtable result = new Hashtable();
		result.put(RESULT_ID,id);
		//System.out.println("LOCAL_PEER:: GetID returning ## " +id);
		return result;
	}
}
