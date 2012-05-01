package org.universAAL.middleware.sodapop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
