/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

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
package org.universAAL.middleware.tracker;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;

/**
 * Interface of BusMember registry listener. If registered at the service
 * {@link IBusMemberRegistry} it gets notified if any changes occurs in it.
 * 
 * @author dzmuda
 * @author Carsten Stockloew
 * 
 */
public interface IBusMemberRegistryListener {
    /**
     * Invoked when a new BusMember is registered in the bus.
     * 
     * @param member
     *            newly added bus member
     * @param type
     *            type of bus {@link IBusMemberRegistry}
     */
    public void busMemberAdded(BusMember member, BusType type);

    /**
     * Invoked when an existing BusMember is unregistered from the bus.
     * 
     * @param member
     *            removed bus member
     * @param type
     *            type of bus {@link IBusMemberRegistry}
     */
    public void busMemberRemoved(BusMember member, BusType type);

    /**
     * Invoked when registration parameters of an existing BusMember are added.
     * Registration parameters can be, for example, {@link ServiceProfile}s for
     * {@link ServiceCallee}s or {@link ContextEventPattern} for
     * {@link ContextSubscriber}.
     * 
     * @param busMemberID
     *            the ID of the bus member for which the registration parameters
     *            have been added.
     * @param params
     *            the registration parameters.
     */
    public void regParamsAdded(String busMemberID, Resource[] params);

    /**
     * Invoked when registration parameters of an existing BusMember are
     * removed. Registration parameters can be, for example,
     * {@link ServiceProfile}s for {@link ServiceCallee}s or
     * {@link ContextEventPattern} for {@link ContextSubscriber}.
     * 
     * @param busMemberID
     *            the ID of the bus member for which the registration parameters
     *            have been removed.
     * @param params
     *            the registration parameters.
     */
    public void regParamsRemoved(String busMemberID, Resource[] params);
}
