/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package de.fhg.igd.ima.sodapop.p2p.bridge;

import java.util.Hashtable;
import java.util.Iterator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import de.fhg.igd.ima.sodapop.p2p.P2PConnector;
import de.fhg.igd.ima.sodapop.p2p.PeerDiscoveryListener;
import de.fhg.igd.ima.sodapop.p2p.SodaPopPeer;

/**
 * @author <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class BridgeActivator implements BundleActivator, ServiceListener, PeerDiscoveryListener {
	private Hashtable connectors;
	private BundleContext myBundleContext;
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		connectors = new Hashtable();
		myBundleContext = context;
		
		context.addServiceListener(this);
		
		ServiceReference[] aclRefs = context.getAllServiceReferences(
				P2PConnector.class.getName(), null);
		for (int i=0; i<aclRefs.length; i++) {
			ServiceReference acl = aclRefs[i];
			P2PConnector c = (P2PConnector) context.getService(acl);
			c.addPeerDiscoveryListener(this);
			connectors.put(c.getProtocol(), c);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

	public void serviceChanged(ServiceEvent se) {
		Object service = myBundleContext.getService(se.getServiceReference());
		if (service instanceof P2PConnector)
			if (se.getType() == ServiceEvent.REGISTERED) {
				((P2PConnector) service).addPeerDiscoveryListener(this);
				connectors.put(((P2PConnector) service).getProtocol(),
						(P2PConnector) service);
			} else if (se.getType() == ServiceEvent.UNREGISTERING)
				connectors.remove(((P2PConnector) service).getProtocol());
	}

	public void noticeLostPeer(String peerID, String discoveryProtocol) {
		for (Iterator i = connectors.values().iterator(); i.hasNext(); ) {
			P2PConnector c = (P2PConnector) i.next();
			if (!c.getProtocol().equals(discoveryProtocol))
				c.noticeLostBridgedPeer(peerID);
		}
	}

	public void noticeNewPeer(SodaPopPeer peer, String discoveryProtocol) {
		for (Iterator i = connectors.values().iterator(); i.hasNext(); ) {
			P2PConnector c = (P2PConnector) i.next();
			if (!c.getProtocol().equals(discoveryProtocol))
				c.noticeNewBridgedPeer(peer);
		}
	}

}
