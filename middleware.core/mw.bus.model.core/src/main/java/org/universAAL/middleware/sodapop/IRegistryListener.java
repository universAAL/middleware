package org.universAAL.middleware.sodapop;

public interface IRegistryListener {
	
	void busMemberAdded(BusMember busMember);
	
	void busMemberRemoved(BusMember busMember);
	
	void busCleared();
	
}
