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
package org.universAAL.middleware.sodapop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Apr 20, 2012
 *
 */
public class RegistryMap extends Object implements IRegistry {

	protected Map map = new HashMap();
	
	public void addBusMember(String memberID, BusMember busMember) {
		map.put(memberID, busMember);
	}
	
	public BusMember removeMemberByID(String memberID) {
		return (BusMember) map.remove(memberID);
	}

	public BusMember[] getAllBusMembers() {
		return (BusMember[]) map.values().toArray(new BusMember[0]);
	}

	public String[] getAllBusMembersIds() {
		return (String[]) map.keySet().toArray(new String[0]);
	}

	public BusMember getBusMemberByID(String memberID) {
		return (memberID == null) ? null : (BusMember) map.get(memberID);
	}

	public String getBusMemberID(BusMember busMember) {
		String result = null;
		if (busMember != null) {
			for (Iterator i = map.keySet().iterator(); i.hasNext();) {
				String id = (String) i.next();
				if (busMember.equals(map.get(id))) {
					result = id;
					break;
				}
			}
		}
		return result;
	}

	public int getBusMembersCount() {
		return map.size();
	}

	public void reset() {
		map.clear();
	}
}
