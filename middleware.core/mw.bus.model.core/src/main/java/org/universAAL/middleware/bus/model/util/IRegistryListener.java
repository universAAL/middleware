package org.universAAL.middleware.bus.model.util;

import org.universAAL.middleware.bus.member.BusMember;

public interface IRegistryListener {

    void busMemberAdded(BusMember busMember);

    void busMemberRemoved(BusMember busMember);

    void busCleared();

}
