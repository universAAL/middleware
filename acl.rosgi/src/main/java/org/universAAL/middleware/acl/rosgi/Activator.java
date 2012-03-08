/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.acl.rosgi;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.universAAL.middleware.acl.P2PConnector;
import org.universAAL.middleware.acl.PeerDiscoveryListener;
import org.universAAL.middleware.acl.SodaPopPeer;

import ch.ethz.iks.r_osgi.RemoteOSGiService;
import ch.ethz.iks.r_osgi.URI;
import ch.ethz.iks.r_osgi.service_discovery.ServiceDiscoveryListener;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class Activator implements P2PConnector, BundleActivator, ServiceDiscoveryListener {

	private RemoteOSGiService remote;
	private Vector listeners; // <PeerDiscoveryListener>
	private BundleContext context;
	private Hashtable bridgedRegs; // <String, ServiceRegistration>
	private SodaPopPeer localInstance = null;
	private Hashtable sodapops; // <ServiceURL, String>

	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		listeners = new Vector(2);
		bridgedRegs = new Hashtable();
		sodapops = new Hashtable();

		ServiceReference remoteRef = context.getServiceReference(RemoteOSGiService.class
				.getName());
		if (remoteRef != null)
			remote = (RemoteOSGiService) context.getService(remoteRef);
		else
			throw new RuntimeException("OSGi remote service is not present!");
		
		context.registerService(new String[] {
				P2PConnector.class.getName(), ServiceDiscoveryListener.class.getName()
			}, this, null);
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

	public void announceService(String serviceInterface, URI uri) {
		System.out.println("acl_rOSGi discovered a peer with class name " + serviceInterface);
		if (!serviceInterface.equals(SodaPopPeer.class.getName()))
				return;

		synchronized(remote) {
			try {
				remote.connect(uri);
				Object proxy = remote.getRemoteService(remote.getRemoteServiceReference(uri));
				System.out.println("The acl_rOSGi proxy is of type " + proxy.getClass().getName());
				if (!(proxy instanceof SodaPopPeer)
						||  (localInstance != null
								&&  localInstance.getID().equals(
										((SodaPopPeer) proxy).getID())))
					return;
				
				// notify all listeners
				System.out.println("acl_rOSGi notifies its listeners!");
				for (Iterator i = listeners.iterator(); i.hasNext(); )
					((PeerDiscoveryListener) i.next()).noticeNewPeer(
							(SodaPopPeer) proxy, getProtocol());
				
				// remember this service URL
				sodapops.put(uri, ((SodaPopPeer) proxy).getID());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void discardService(String serviceInterface, URI uri) {
		System.out.println("acl_rOSGi lost a peer with class name " + serviceInterface);
		if (!serviceInterface.equals(SodaPopPeer.class.getName()))
				return;
		
		synchronized (remote) {
			String id = (String) sodapops.remove(uri);
			if (id != null)
				for (Iterator i = listeners.iterator(); i.hasNext(); )
					((PeerDiscoveryListener) i.next()).noticeLostPeer(
							id, getProtocol());
		}
					
	}
	
	// PERSONA-ACL interfaces
	
	public void addPeerDiscoveryListener(PeerDiscoveryListener listener) {
			synchronized (remote) {
				listeners.add(listener);
				for (Iterator i = sodapops.keySet().iterator(); i.hasNext(); ) {
					try {
						listener.noticeNewPeer((SodaPopPeer)
								remote.getRemoteService(remote.getRemoteServiceReference((URI) i.next())),
								getProtocol());
					} catch (Exception e) {}
				}
			}
		}

	public String getProtocol() {
		return "R-OSGi";
	}

	public void noticeLostBridgedPeer(String peerID) {
		synchronized (remote) {
			ServiceRegistration sreg = (ServiceRegistration) bridgedRegs.remove(peerID);
			if (sreg != null)
				sreg.unregister();
		}
	}

	public void noticeNewBridgedPeer(SodaPopPeer newPeer) {
		synchronized (remote) {
			final Hashtable properties = new Hashtable();
			properties.put(RemoteOSGiService.R_OSGi_REGISTRATION,
					Boolean.TRUE);
			bridgedRegs.put(newPeer.getID(),
					context.registerService(SodaPopPeer.class.getName(),
							newPeer, properties));
		}
	}

	public void register(SodaPopPeer localInstance) {
		if (this.localInstance != null)
			throw new RuntimeException("Repeated call to register not allowed!");
		
		this.localInstance = localInstance;

		synchronized(remote) {
			final Hashtable properties = new Hashtable();
			properties.put(RemoteOSGiService.R_OSGi_REGISTRATION,
					Boolean.TRUE);
			context.registerService(SodaPopPeer.class.getName(),
					localInstance, properties);
		}
	}
}
