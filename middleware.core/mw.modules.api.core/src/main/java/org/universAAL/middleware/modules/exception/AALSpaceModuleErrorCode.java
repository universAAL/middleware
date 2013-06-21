/*	
	Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
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
package org.universAAL.middleware.modules.exception;

/**
 * Error codes AALSpaceModule
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:filippo.palumbo@isti.cnr.it">Filippo Palumbo</a>
 */
public class AALSpaceModuleErrorCode {
    public static final short NO_DISCOVERY_CONNECTORS = 0;
    public static final short ERROR_INTERACTING_DISCOVERY_CONNECTORS = 1;
    public static final short AALSPACE_JOIN_ERROR = 2;
    public static final short AALSPACE_JOIN_WRONG_PARAMETERS = 3;
    public static final short AALSPACE_JOIN_RESPONSE_WRONG_PARAMETERS = 4;
    public static final short AALSPACE_NEW_PEER_ERROR = 5;
    public static final short AALSPACE_NEW_PEER_ADDED_ERROR = 6;
    public static final short ERROR_MANAGING_AALSPACE_MESSAGE = 7;

    public static final short ERROR_SENDING_JOIN_REQUEST = 8;
    public static final short ERROR_SENDING_JOIN_RESPONSE = 9;
    public static final short ERROR_SENDING_NEW_PEER_ADDED = 10;

    public static final short AALSPACE_LEAVE_ERROR = 11;

}
