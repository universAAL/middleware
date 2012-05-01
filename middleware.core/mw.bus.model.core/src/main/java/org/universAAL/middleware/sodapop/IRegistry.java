package org.universAAL.middleware.sodapop;

public interface IRegistry {
	
	void addBusMember(String memberID, BusMember busMember);
	
	BusMember removeMemberByID(String memberID);
	
	BusMember getBusMemberByID(String memberID);
	
	String getBusMemberID(BusMember busMember);
	
	BusMember[] getAllBusMembers();
	
	String[] getAllBusMembersIds();
	
	int getBusMembersCount();

	void reset();
}
