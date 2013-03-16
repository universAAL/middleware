package org.universAAL.middleware.tracker;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;

/**
 * Interface of BusMember registry listener. If registered in registry it gets
 * notified if any changes occurs in it.
 * 
 * @author dzmuda
 * 
 */
public interface IBusMemberRegistryListener {
    /**
     * Method invoked if new BusMember is registered in the bus of specific
     * 'BusType'.
     * 
     * @param member
     *            - newly added bus member
     * @param type
     *            - type of bus {@link IBusMemberRegistry}
     */
    public void busMemberAdded(BusMember member, BusType type);

    /**
     * Method invoked if BusMember is unregistered from the bus of specific
     * 'BusType'.
     * 
     * @param member
     *            - removed bus member
     * @param type
     *            - type of bus {@link IBusMemberRegistry}
     */
    public void busMemberRemoved(BusMember member, BusType type);
}
