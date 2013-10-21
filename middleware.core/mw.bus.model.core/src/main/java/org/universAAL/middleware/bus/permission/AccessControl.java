/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research

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
package org.universAAL.middleware.bus.permission;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.member.BusMemberType;
import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;

/**
 * Management of permissions of bus members.
 * 
 * @author Carsten Stockloew
 * 
 */
public class AccessControl {
    public static final AccessControl INSTANCE = new AccessControl();

    public enum AccessControlMode {
	none, log, full;
    }

    private AccessControlMode mode = AccessControlMode.log;

    /**
     * Permissions for a specific {@link BusMember}. Maps the URI of a bus
     * member to its permissions.
     */
    // NOTE: if we have multiple bus members of the same type (e.g. multiple
    // ContextProviders from the same module, then the permissions are stored
    // multiple times. Maybe this can be implemented a bit more efficient.
    private Hashtable<String, Permission[]> permsMember = new Hashtable<String, Permission[]>();

    private Hashtable<String, ModuleContext> owners = new Hashtable<String, ModuleContext>();

    // singleton
    private AccessControl() {
    }

    /**
     * Check the permissions of a {@link BusMember} for a given
     * {@link Matchable}.
     * 
     * @param owner
     *            the owning module.
     * @param busMemberURI
     *            URI of the {@link BusMember}.
     * @param m
     *            the {@link Matchable} to check
     * @return if {@link AccessControlMode} is {@link AccessControlMode#full}
     *         the method return true if the bus member has the permission for
     *         the given matchable object. For all other
     *         {@link AccessControlMode}s the return value will always be true
     *         (but maybe a log message is issued).
     */
    public boolean checkPermission(ModuleContext owner, String busMemberURI,
	    Matchable m) {
	if (mode == AccessControlMode.none)
	    return true;

	Permission[] perms = permsMember.get(busMemberURI);
	for (int i = 0; i < perms.length; i++) {
	    try {
		if (perms[i].getMatchable().matches(m))
		    return true;
	    } catch (Exception e) {
		Resource r1 = (Resource) (perms[i].getMatchable());
		Resource r2 = (Resource) m;
		LogUtils.logDebug(
			owner,
			AccessControl.class,
			"checkPermission",
			new Object[] {
				"Caught Exception while trying to match:\n",
				r1.toStringRecursive(), r2.toStringRecursive() },
			null);
	    }
	}

	LogUtils.logDebug(owner, AccessControl.class, "checkPermission",
		new Object[] { "No permission for Matchable: ", m }, null);

	if (mode == AccessControlMode.full)
	    return false;
	return true;
    }

    /**
     * Check the permissions of a {@link BusMember} for a given array of
     * {@link Matchable}s.
     * 
     * @param owner
     *            the owning module.
     * @param busMemberURI
     *            URI of the {@link BusMember}.
     * @param m
     *            the {@link Matchable}s to check
     * @return if {@link AccessControlMode} is {@link AccessControlMode#full}
     *         the method returns a modified array of all {@link Matchable} for
     *         which the bus member has permissions. For all other
     *         {@link AccessControlMode}s the return value will always be the
     *         given array (but maybe a log message is issued).
     */
    public <T extends Matchable> T[] checkPermission(ModuleContext owner,
	    String busMemberURI, T[] m) {
	ArrayList<T> l = new ArrayList<T>(m.length);
	if (mode == AccessControlMode.none)
	    return m;

	for (int i = 0; i < m.length; i++) {
	    if (checkPermission(owner, busMemberURI, m[i])) {
		l.add(m[i]);
	    }
	}

	if (mode == AccessControlMode.full)
	    return l.toArray(m);

	return m;
    }

    public void registerBusMember(ModuleContext owner, BusMember m,
	    String brokerName) {
	boolean isAdvertisement = (m.getType() == BusMemberType.responder)
		|| (m.getType() == BusMemberType.publisher);
	Permission[] p = Permission.fromManifest(owner, brokerName,
		isAdvertisement);
	permsMember.put(m.getURI(), p);
	owners.put(m.getURI(), owner);

	// log permissions
	if (p.length == 0) {
	    LogUtils.logDebug(owner, AccessControl.class, "registerBusMember",
		    new Object[] { "Permissions for bus member ", m.getURI(),
			    ": -none-" }, null);
	} else {
	    LinkedList<String> msg = new LinkedList<String>();
	    msg.add("Permissions for bus member ");
	    msg.add(m.getURI());
	    msg.add(":\n");
	    for (int i = 0; i < p.length; i++) {
		msg.add("  " + i + "\t");
		msg.add(p[i].getTitle());
		msg.add("\n");
	    }
	    LogUtils.logDebug(owner, AccessControl.class, "registerBusMember",
		    msg.toArray(), null);
	}
    }

    public void unregisterBusMember(ModuleContext owner, BusMember m) {
	// security check: was this bus member really registered by this module?
	ModuleContext realOwner = owners.get(m.getURI());
	if (realOwner == owner) {
	    // remove
	    permsMember.remove(m.getURI());
	    owners.remove(m.getURI());
	    LogUtils.logDebug(
		    owner,
		    AccessControl.class,
		    "unregisterBusMember",
		    new Object[] { "Bus member ", m.getURI(), " unregistered." },
		    null);
	} else {
	    LogUtils.logWarn(
		    owner,
		    AccessControl.class,
		    "unregisterBusMember",
		    new Object[] { "The module ", owner.getID(),
			    " has tried to unregister a bus member that it did not register." },
		    null);
	}
    }
}
