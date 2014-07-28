/*	
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
package org.universAAL.middleware.bus.model.util;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.rdf.Resource;

public interface IRegistryListener {

    /**
     * Invoked when a new BusMember is registered in the bus.
     * 
     * @param member
     *            newly added bus member
     */
    void busMemberAdded(BusMember busMember);

    /**
     * Invoked when an existing BusMember is unregistered from the bus.
     * 
     * @param member
     *            removed bus member
     */
    void busMemberRemoved(BusMember busMember);

    /**
     * Invoked when registration parameters of an existing BusMember are added.
     * 
     * @param member
     *            the bus member for which the registration parameters have been
     *            added.
     * @param params
     *            the registration parameters.
     */
    public void regParamsAdded(BusMember busMember, Resource[] params);

    /**
     * Invoked when registration parameters of an existing BusMember are
     * removed.
     * 
     * @param member
     *            the bus member for which the registration parameters have been
     *            removed.
     * @param params
     *            the registration parameters.
     */
    public void regParamsRemoved(BusMember busMember, Resource[] params);
    
    /**
     * Invoked when all bus members are removed. This typically only happens
     * when the bus is stopped.
     */
    void busCleared();
}
