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

package org.persona.mw.acl.upnp;

import java.util.Dictionary;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.upnp.UPnPDevice;
import org.persona.mw.acl.upnp.exporter.SodaPopDevice;
import org.persona.mw.acl.upnp.importer.PeerImporter;

import de.fhg.igd.ima.sodapop.p2p.P2PConnector;
import de.fhg.igd.ima.sodapop.p2p.PeerDiscoveryListener;
import de.fhg.igd.ima.sodapop.p2p.SodaPopPeer;


/* 
* @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
*/

public class Activator implements BundleActivator, P2PConnector {

	private BundleContext context;
	private ServiceRegistration localPeerRegistration,aclRegistration;
	private SodaPopPeer localInstance;
	private PeerImporter peerImporter;
	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		aclRegistration = context.registerService(
				P2PConnector.class.getName(),
				this,
				null
			);
	}


	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		aclRegistration.unregister();
		if (localPeerRegistration != null)
			localPeerRegistration.unregister();
		if (peerImporter != null)
			peerImporter.unregister();
	}

	public String getProtocol() {		
		return UPnPDevice.DEVICE_CATEGORY;
	}
	
	public void addPeerDiscoveryListener(PeerDiscoveryListener listener) {
		System.out.println("acl.upnp:: addPeerDiscoveryListener");
		
		if (peerImporter == null)
			peerImporter = new PeerImporter(context,listener);
		else
			peerImporter.addListener(listener);
		
	}

	public void register(SodaPopPeer localInstance) {
		System.out.println("acl.upnp:: register");

		if (this.localInstance != null)
			throw new RuntimeException("Repeated call to register not allowed!");
		
		this.localInstance = localInstance;
		SodaPopDevice sodapopDevice = new SodaPopDevice(localInstance);
		Dictionary dict = sodapopDevice.getDescriptions(null);
				
		localPeerRegistration = context.registerService(
				UPnPDevice.class.getName(),
				sodapopDevice,
				dict
			);
		
	}
	
	public void noticeLostBridgedPeer(String peerID) {
		// TODO not implemented yet 		
	}

	public void noticeNewBridgedPeer(SodaPopPeer newPeer) {
		// TODO not implemented yet 		
	}

}
