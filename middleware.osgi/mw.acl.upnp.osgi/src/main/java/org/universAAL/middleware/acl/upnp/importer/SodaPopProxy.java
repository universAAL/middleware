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

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPService;
import org.universAAL.middleware.acl.upnp.exporter.actions.JoinBusAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.LeaveBusAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.NoticePeerBussesAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.PrintStatusAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.ProcessBusMessageAction;
import org.universAAL.middleware.acl.upnp.exporter.actions.ReplyPeerBussesAction;
import org.universAAL.middleware.acl.upnp.exporter.services.SodaPopPeerService;

import org.universAAL.middleware.acl.SodaPopPeer;

/* 
 * The SodaPopProxy acts as proxy for remote peers exported through the UPnP protocol. The methods of the SodaPopPeer interface are here implemented by invoking accordingly the UPnP actions 
 * on the target UPnP device representing the peer. Generally invokin an action on the UPnP device corresponds to :
 * 1)prepare a Dictionary object with all the relevant properties
 * 2)fetch the object modeling the required UPnP action
 * 3)invoke the action by passing the Dictionary previously created.
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */

public class SodaPopProxy implements SodaPopPeer {
    private final String UUID_PREFIX = "uuid:";
    private UPnPService sodapopPeerService;
    private String id;

    /**
     * Proxy constructor. The UPnPDevice parameter is used in order to fetch all
     * the required references able to interact with the remote peer.
     * 
     * @param device
     */
    public SodaPopProxy(UPnPDevice device) {
	sodapopPeerService = device.getService(SodaPopPeerService.SERVICE_ID);
	id = (String) device.getDescriptions(null).get(UPnPDevice.ID);
	id = id.substring(UUID_PREFIX.length());
    }

    public String getID() {
	// System.out.println("REMOTE_PEER:: getID invoked (cached)");
	return id;
	// try {
	//			
	// System.out.println("REMOTE_PEER:: getID invoked");
	// Dictionary dictionary =
	// sodapopPeerService.getAction(GetIdAction.NAME).invoke(null);
	// String id = (String) dictionary.get(GetIdAction.RESULT_ID);
	// System.out.println("REMOTE_PEER:: getID returned:" +id);
	// return id;
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
    }

    public void joinBus(final String busName, final String joiningPeer) {
	// new Thread(){
	// public void run(){
	try {
	    // System.out.println("REMOTE_PEER:: joinBus invoked:" +busName +
	    // ", " + joiningPeer);
	    Dictionary dictionary = new Hashtable();
	    dictionary.put(JoinBusAction.BUS_NAME, busName);
	    dictionary.put(JoinBusAction.JOINING_PEER, joiningPeer);
	    sodapopPeerService.getAction(JoinBusAction.NAME).invoke(dictionary);
	    return;
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	// }
	// }.start();
	return;
    }

    public void leaveBus(final String busName, final String leavingPeer) {
	// new Thread(){
	// public void run(){
	try {
	    // System.out.println("REMOTE_PEER:: leaveBus invoked:" +busName +
	    // ", " + leavingPeer);
	    Dictionary dictionary = new Hashtable();
	    dictionary.put(LeaveBusAction.BUS_NAME, busName);
	    dictionary.put(LeaveBusAction.LEAVING_PEER, leavingPeer);
	    sodapopPeerService.getAction(LeaveBusAction.NAME)
		    .invoke(dictionary);
	    return;
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	// }
	// }.start();
	return;

    }

    public void noticePeerBusses(final String peerID, final String busNames) {
	// new Thread(){
	// public void run(){
	try {
	    // System.out.println("REMOTE_PEER:: noticePeerBusses invoked:"
	    // +peerID + ", " + busNames);
	    Dictionary dictionary = new Hashtable();
	    dictionary.put(NoticePeerBussesAction.PEER_ID, peerID);
	    dictionary.put(NoticePeerBussesAction.BUSSES_NAME, busNames);
	    sodapopPeerService.getAction(NoticePeerBussesAction.NAME).invoke(
		    dictionary);
	    return;
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	// }
	// }.start();
	return;
    }

    public void replyPeerBusses(String peerID, String busNames) {
	// new Thread(){
	// public void run(){
	try {
	    // System.out.println("REMOTE_PEER:: replyPeerBusses invoked:"
	    // +peerID + ", " + busNames);
	    Dictionary dictionary = new Hashtable();
	    dictionary.put(ReplyPeerBussesAction.PEER_ID, peerID);
	    dictionary.put(ReplyPeerBussesAction.BUSSES_NAME, busNames);
	    sodapopPeerService.getAction(ReplyPeerBussesAction.NAME).invoke(
		    dictionary);
	    return;
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	// }
	// }.start();

    }

    public void processBusMessage(final String busName, final String msg) {
	// new Thread(){
	// public void run(){
	try {
	    // System.out.println("REMOTE_PEER:: processBusMessage invoked:"
	    // +busName + ", " + msg);
	    Dictionary dictionary = new Hashtable();
	    dictionary.put(ProcessBusMessageAction.BUS_NAME, busName);
	    dictionary.put(ProcessBusMessageAction.MESSAGE, msg);
	    sodapopPeerService.getAction(ProcessBusMessageAction.NAME).invoke(
		    dictionary);
	    return;
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	// }
	// }.start();
	return;
    }

    public void printStatus() {
	Dictionary dictionary = new Hashtable();
	try {
	    sodapopPeerService.getAction(PrintStatusAction.NAME).invoke(
		    dictionary);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
