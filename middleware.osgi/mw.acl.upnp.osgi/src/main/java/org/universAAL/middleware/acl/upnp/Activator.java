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

package org.universAAL.middleware.acl.upnp;

import java.util.Dictionary;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.upnp.UPnPDevice;
import org.universAAL.middleware.acl.upnp.exporter.SodaPopDevice;
import org.universAAL.middleware.acl.upnp.importer.PeerImporter;

import org.universAAL.middleware.acl.P2PConnector;
import org.universAAL.middleware.acl.PeerDiscoveryListener;
import org.universAAL.middleware.acl.SodaPopPeer;

/* 
 * This class represents the UPnP connector among the peers. As soon as the register(...) method is invoked,  a new SodaPopDevice wrapping the SodaPopPeer instance in created and registered
 * into the OSGi Service Registry. This allows the UPnP Base Driver to "inject" the SodaPopDevice into the UPnP network, by allowing the SodaPopPeer to act as regular UPnP device.
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */

public class Activator implements BundleActivator, P2PConnector {

    private BundleContext context;
    private ServiceRegistration localPeerRegistration, aclRegistration;
    private SodaPopPeer localInstance;
    private PeerImporter peerImporter;

    /**
     * The activator simply registers one P2PConnector instance within the
     * Service Registry.
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     * 
     */
    public void start(BundleContext context) throws Exception {
	this.context = context;
	aclRegistration = context.registerService(P2PConnector.class.getName(),
		this, null);
    }

    /**
     * Stop the acl.upnp bundle by unregistering the P2PConnector. If the
     * PeerImporter has been registered unregister it.
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
	aclRegistration.unregister();
	if (localPeerRegistration != null)
	    localPeerRegistration.unregister();
	if (peerImporter != null)
	    peerImporter.unregister();
    }

    /**
     * @return UPnP device category
     */
    public String getProtocol() {
	return UPnPDevice.DEVICE_CATEGORY;
    }

    /**
     * Add a new listener. The listener will be notified about the existence of
     * the peers.
     * 
     * @see org.universAAL.middleware.acl.P2PConnector#addPeerDiscoveryListener(org.universAAL.middleware.acl.PeerDiscoveryListener)
     */
    public void addPeerDiscoveryListener(PeerDiscoveryListener listener) {
	System.out.println("acl.upnp:: addPeerDiscoveryListener");

	if (peerImporter == null)
	    peerImporter = new PeerImporter(context, listener);
	else
	    peerImporter.addListener(listener);

    }

    /**
     * This method allows to the SodaPopPeer to be exported within the UpNP
     * network. The SodaPopPeer is firstly wrapped within the SodaPopDevice and
     * then registered within the Service Registry as UPnPDevice. The UPnP Base
     * Driver will "inject" the UPnP Device into the UpNP network.
     * 
     * @see org.universAAL.middleware.acl.P2PConnector#register(org.universAAL.middleware.acl.SodaPopPeer)
     */
    public void register(SodaPopPeer localInstance) {
	System.out.println("acl.upnp:: register");

	if (this.localInstance != null)
	    throw new RuntimeException("Repeated call to register not allowed!");

	this.localInstance = localInstance;
	SodaPopDevice sodapopDevice = new SodaPopDevice(localInstance);
	Dictionary dict = sodapopDevice.getDescriptions(null);

	localPeerRegistration = context.registerService(UPnPDevice.class
		.getName(), sodapopDevice, dict);

    }

    public void noticeLostBridgedPeer(String peerID) {
	// TODO not implemented yet
    }

    public void noticeNewBridgedPeer(SodaPopPeer newPeer) {
	// TODO not implemented yet
    }

}
