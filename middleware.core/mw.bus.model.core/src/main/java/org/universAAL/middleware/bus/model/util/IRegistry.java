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
package org.universAAL.middleware.bus.model.util;

import org.universAAL.middleware.bus.member.BusMember;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 20, 2012
 * 
 */
public interface IRegistry {

    void addBusMember(String memberID, BusMember busMember);

    BusMember removeMemberByID(String memberID);

    BusMember getBusMemberByID(String memberID);

    String getBusMemberID(BusMember busMember);

    BusMember[] getAllBusMembers();

    String[] getAllBusMembersIds();

    int getBusMembersCount();

    void reset();

    boolean addRegistryListener(IRegistryListener listener);

    boolean removeRegistryListener(IRegistryListener listener);
}
