/*
	Copyright 2008-2010 CNR-ISTI, http://isti.cnr.it
	Institute of Information Science and Technologies 
	of the Italian National Research Council  
	
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
package de.fhg.igd.ima.sodapop.impl;

import de.fhg.igd.ima.sodapop.p2p.SodaPopPeer;



/* 
* @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
*/

public class Dispatcher extends Thread {

	public CommandQueue queue;
	public SodaPopPeer peer;
	private boolean running = true;

    public Dispatcher(SodaPopPeer peer) {
		super("sodapop.impl.Dispatcher");
		this.queue = new CommandQueue();
		this.peer = peer;
		
	}

	public void run() {
		while (running) {
			PeerCommand msg = (PeerCommand) queue.dequeue();
			if (running) {
				switch (msg.mesageType){
				case PeerCommand.PROCESS_MESSAGE:
					try {
					peer.processBusMessage(msg.peerId, msg.message);
					} catch (Exception ex){
					    System.out.println("Failed processBusMessage() invocation with peer: "+msg.peerId);
					}
					break;
				case PeerCommand.JOIN_BUS:
					try {
					peer.joinBus(msg.message, msg.peerId);
					} catch (Exception ex){
					    System.out.println("Failed joinBus() invocation with peer: "+msg.peerId);
					}
					break;
				case PeerCommand.LEAVE_BUS:
					try {
					peer.leaveBus(msg.message, msg.peerId);
					} catch (Exception ex){
					    System.out.println("Failed leaveBus() invocation with peer: "+msg.peerId);
					}
					break;               		               		            	
				case PeerCommand.NOTICE_PEER_BUSES:
					try {
					peer.noticePeerBusses(msg.peerId, msg.message);
					} catch (Exception ex){
					    System.out.println("Failed noticePeerBusses() invocation with peer: "+msg.peerId);
					}
					break;
				case PeerCommand.REPLY_PEER_BUSES:
					try {
					peer.replyPeerBusses(msg.peerId, msg.message);
					} catch (Exception ex){
					    System.out.println("Failed replyPeerBusses() invocation with peer: "+msg.peerId);
					}
					break;
				}
			}
		}
	}

	public void close() {
		running  = false;
		queue.close();
	}
}
