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
