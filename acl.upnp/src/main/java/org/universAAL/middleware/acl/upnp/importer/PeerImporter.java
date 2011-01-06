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
package org.universAAL.middleware.acl.upnp.importer;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;
import org.universAAL.middleware.acl.upnp.exporter.SodaPopDevice;

import org.universAAL.middleware.acl.PeerDiscoveryListener;
import org.universAAL.middleware.acl.SodaPopPeer;

/* 
* @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
*/

public class PeerImporter implements ServiceListener{
	
	private BundleContext context;
	private Hashtable proxies; // <String,SodaPopProxy>
	private Vector  listeners; // <PeerDiscoveryListener>
	final private Object LOCK = new Object();
	
	public PeerImporter(BundleContext context, PeerDiscoveryListener listener){
		this.context = context;
		listeners = new Vector(); // <PeerDiscoveryListener>
		proxies = new Hashtable(); // <String, SodaPopProxy>
		listeners.add(listener);
		
		
		String filter =
			"(&" + 
				"(" + Constants.OBJECTCLASS	+ "=" + UPnPDevice.class.getName() + ")" + 
				"( !("	+ UPnPDevice.UPNP_EXPORT + "=*) )" + 
				"(" + UPnPDevice.TYPE + "=" + SodaPopDevice.TYPE +")" + 
			")";

		synchronized (LOCK) {
		    try {
				context.addServiceListener(this,filter);
			} catch (Exception ex) {
				System.out.println(ex);
			}
			
			
			ServiceReference[] services = null; 
			try {
				services = context.getServiceReferences(UPnPDevice.class.getName(),filter);
				if (services != null){
					for (int i = 0;i<services.length;i++){
						addRemotePeer(services[i]);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex);
			}
		
		}

	}

	public void serviceChanged(ServiceEvent event) {
		synchronized (LOCK) {			
			switch (event.getType()) {
			case ServiceEvent.REGISTERED: {
				addRemotePeer(event.getServiceReference());
			}break;

			case ServiceEvent.MODIFIED: {
				removeRemotePeer(event.getServiceReference());
				addRemotePeer(event.getServiceReference());			
			}
			break;

			case ServiceEvent.UNREGISTERING: {
				removeRemotePeer(event.getServiceReference());
			}break;
			}
		}
	}

    private final String UUID_PREFIX = "uuid:";
    
	private void addRemotePeer(ServiceReference serviceReference) {
		String id  = (String) serviceReference.getProperty(UPnPDevice.ID);
		//System.out.println("addRemotePeer:: " + id);
		id = id.substring(UUID_PREFIX.length());
		UPnPDevice device = (UPnPDevice) context.getService(serviceReference);
		SodaPopProxy proxy = new SodaPopProxy((UPnPDevice) device);
		proxies.put(id,proxy);
		for (Iterator i = listeners.iterator(); i.hasNext(); ) {
			//System.out.println("sending noticeNewPeer:: " + id);
			((PeerDiscoveryListener) i.next()).noticeNewPeer(
					proxy, UPnPDevice.DEVICE_CATEGORY);
		}		
	}
	

	private void removeRemotePeer(ServiceReference serviceReference) {
		String id  = (String) serviceReference.getProperty(UPnPDevice.ID);
		//System.out.println("removeRemotePeer:: " + id);
		id = id.substring(UUID_PREFIX.length());
		SodaPopProxy proxy = (SodaPopProxy) proxies.get(id);
		if (proxy != null){
			for (Iterator i = listeners.iterator(); i.hasNext(); ) {
				//System.out.println("sending noticeLostPeer:: " + id);
				((PeerDiscoveryListener) i.next()).noticeLostPeer(
						id, UPnPDevice.DEVICE_CATEGORY);
			}		
			proxies.remove(id);		
		}
	}

	public void addListener(PeerDiscoveryListener listener) {
		synchronized (LOCK) {			
			for (Iterator i = proxies.keySet().iterator(); i.hasNext(); ) {
				listener.noticeNewPeer(
						(SodaPopPeer) proxies.get(i.next()),
						UPnPDevice.DEVICE_CATEGORY);
			}
			listeners.add(listener);	
		}
	}

	public void unregister() {
		synchronized (LOCK) {	
			System.out.println("peerImporter unregistering ");

			context.removeServiceListener(this);
			for (Iterator i = proxies.keySet().iterator(); i.hasNext(); ) {
				for (Iterator j = listeners.iterator(); j.hasNext(); ) {
					((PeerDiscoveryListener) j.next()).noticeLostPeer(
							(String) i.next(), UPnPDevice.DEVICE_CATEGORY);
				}		
			}			
		}
	}
	

}
