/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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
package org.universAAL.middleware.sodapop;

import org.universAAL.middleware.sodapop.msg.Message;

/**
 * 
 * This interface models the bus. The bus has a name, its members can register
 * and unregister from it, and in meanwhile (between registering and
 * unregistering) bus members can post messages for other bus members.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public interface Bus {

    /**
     * 
     * @return bus name
     */
    public String getBusName();

    /**
     * 
     * @param member
     *            bus member to be registered on the bus
     * @return ID of registered bus member
     */
    public String register(BusMember member);

    /**
     * 
     * @param memberID
     *            ID of a bus member (sender)
     * @param msg
     *            message that is posted on a bus
     */
    public void sendMessage(String memberID, Message msg);

    /**
     * 
     * @param memberID
     *            ID of a bus member
     * @param member
     *            bus member to be unregistered from the bus
     */
    public void unregister(String memberID, BusMember member);
}
