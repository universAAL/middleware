/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research 
	
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
package org.universAAL.middleware.bus.member;

import org.universAAL.middleware.container.ModuleContext;

/**
 * Registers to an RPC-bus for receiving requests to be replied.
 * 
 * @author mtazari - <a href="mailto:saied.tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public abstract class Callee extends BusMember {

    protected Callee(ModuleContext owner, Object[] busFetchParams) {
	super(owner, busFetchParams, BusMemberType.responder);
    }

    /**
     * Handles request coming from the bus.
     * 
     * @param m
     *            request message coming from the bus.
     */
    // public Response handleRequest(Request r);
}
