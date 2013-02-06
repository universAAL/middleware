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
package org.universAAL.middleware.sodapop.impl;

/**
 * This class represent a command sent to peers. It is needed in order to
 * process a queue of commands to execute (instead of sending the commands to
 * the remote peers immediately).
 * 
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */

public class PeerCommand {
    public static final int NOTICE_PEER_BUSES = 1;
    public static final int REPLY_PEER_BUSES = 2;
    public static final int JOIN_BUS = 3;
    public static final int LEAVE_BUS = 4;
    public static final int PROCESS_MESSAGE = 5;
    String peerId;
    String message;
    int mesageType;

    public PeerCommand(int messageType, String peerId, String message) {
	this.mesageType = messageType;
	this.peerId = peerId;
	this.message = message;
    }
}
