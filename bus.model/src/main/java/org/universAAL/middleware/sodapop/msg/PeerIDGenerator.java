/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.middleware.sodapop.msg;

import java.net.InetAddress;
import java.util.Random;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Apr 24, 2012
 *
 */
public class PeerIDGenerator {
	
	public final static String SYS_PROPERTY_SODAPOP_PEER_ID = "sodapop.peerID";
	
	public static String generatePeerID() {
		String peerID = "";
		
		String host = "localhost";
		try {
		    host = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
		}
		long now = System.currentTimeMillis();
		peerID = System.getProperty(SYS_PROPERTY_SODAPOP_PEER_ID);
		if (peerID == null) {
		    peerID = Long.toHexString(now) + '@' + host + '+'
			    + Integer.toHexString(new Random(now).nextInt());
		    
		    System.setProperty(SYS_PROPERTY_SODAPOP_PEER_ID, peerID);
		}
		
		return peerID;
	}
}
