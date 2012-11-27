package org.universAAL.middleware.tracker;

/**
 * Interface used for plugging into the registry of BusMembers in MW nodes.
 * 
 * @author dzmuda
 * 
 */
public interface IBusMemberRegistry {

    public final static Object[] busRegistryShareParams = new Object[] { IBusMemberRegistry.class
	    .getName() };

    /**
     * Enumeration used in notifications.
     * 
     * @author dzmuda
     *
     */
    public enum BusType {
	Service, Context, UI
    }

    /**
     * Method used for adding listener for notifications about changes in
     * BusMember registry.
     * 
     * @param listener
     *            - listener to be added
     * @param notifyAboutPreviouslyRegisteredMembers
     *            - if true then the listener is automatically notified about
     *            all BusMembers currently avaliable in registry.
     */
    public void addBusRegistryListener(IBusMemberRegistryListener listener,
	    boolean notifyAboutPreviouslyRegisteredMembers);

    /**
     * Method used for removal of listeners in BusMember registry
     * 
     * @param listener - listener to be removed
     */
    public void removeBusRegistryListener(IBusMemberRegistryListener listener);
}
