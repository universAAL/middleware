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

import java.util.LinkedList;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;

public class Permission {
    private String title = "";
    private String description = "";
    private String serialization = "";
    private Matchable matchable = null;

    public static final String ADVERTISEMENT = "advertisement";
    public static final String REQUIREMENT = "requirement";
    public static final String PERMISSIONS = "App-permissions";
    public static final String SEPARATOR_TITLE = "---</title>---";
    public static final String SEPARATOR_DESCRIPTION = "---</description>---";
    public static final String SEPARATOR_SERIALIZATION = "---</serialization>---";

    private static ModuleContext mc = null;

    public String getTitle() {
	return title;
    }

    public String getDescription() {
	return description;
    }

    public Matchable getMatchable() {
	return matchable;
    }

    public static void init(ModuleContext mc) {
	if (Permission.mc != null)
	    throw new SecurityException(
		    "Permission can only be initialized once");

	Permission.mc = mc;
    }

    public static Permission[] fromManifest(ModuleContext mc,
	    String brokerName, boolean isAdvertisement) {
	return fromManifest(mc, brokerName, isAdvertisement ? ADVERTISEMENT
		: REQUIREMENT);
    }

    public static Permission[] fromManifest(ModuleContext mc,
	    String brokerName, String type) {
	if (brokerName == null)
	    return new Permission[0];
	if (type == null)
	    return new Permission[0];

	// prepare manifest name and get manifest entry
	brokerName = brokerName.replace(".", "_");
	brokerName = brokerName.toLowerCase();
	// TODO: remove this when the broker-/channel-names are fixed
	if (brokerName.endsWith("_osgi"))
	    brokerName = brokerName.substring(0,
		    brokerName.length() - "_osgi".length());
	type = type.replace(".", "_");
	type = type.toLowerCase();

	String name = PERMISSIONS + "-" + brokerName + "-" + type;
	String entry = mc.getManifestEntry("uaal-manifest.mf", name);
	if (entry == null)
	    entry = mc.getManifestEntry(name);
	if (entry == null) {
	    LogUtils.logDebug(mc, Permission.class, "fromManifest",
		    new Object[] { "No permissions defined for: ", name }, null);
	    return new Permission[0];
	}

	return parsePermission(entry);
    }

    public static Permission[] parsePermission(String entry) {
	// parse manifest entry
	LinkedList<Permission> perms = new LinkedList<Permission>();

	String[] titlesplit = entry.split(SEPARATOR_TITLE);
	if (titlesplit.length == 0)
	    return new Permission[0];
	String lastTitle = titlesplit[0];
	for (int i = 1; i < titlesplit.length; i++) {
	    Permission p = new Permission();
	    p.title = lastTitle;

	    String[] restsplit = titlesplit[i].split(SEPARATOR_DESCRIPTION);
	    if (restsplit.length != 2) {
		LogUtils.logDebug(
			mc,
			Permission.class,
			"parsePermission",
			new Object[] {
				"Manifest corrupt: each manifest entry must"
					+ " be separated by the three defined values"
					+ " for title, description and serialization."
					+ " This manifest entry (title: "
					+ p.title + ") has ",
				restsplit.length < 2 ? "no" : "more than one",
				" description separator" }, null);
		return new Permission[0];
	    }
	    p.description = restsplit[0];

	    restsplit = restsplit[1].split(SEPARATOR_SERIALIZATION);
	    // if (restsplit.length != 2) {
	    if (((i != titlesplit.length - 1) && (restsplit.length != 2))
		    || ((i == titlesplit.length - 1) && restsplit.length != 1)) {
		LogUtils.logDebug(
			mc,
			Permission.class,
			"parsePermission",
			new Object[] { "Manifest corrupt: each manifest entry must"
				+ " be separated by the three defined values"
				+ " for title, description and serialization."
				+ " This manifest entry (title: "
				+ p.title
				+ ") does not have a serialization separator or"
				+ " does not end with one." }, null);
		return new Permission[0];
	    }
	    if (restsplit.length == 2)
		lastTitle = restsplit[1];
	    p.serialization = restsplit[0];
	    Object o = BusMessage.deserializeAsContent(p.serialization);
	    if (o == null) {
		LogUtils.logDebug(mc, Permission.class, "parsePermission",
			new Object[] { "Manifest corrupt: the serialization of"
				+ " the manifest entry (title: " + p.title
				+ ") could not be deserialized." }, null);
		continue;
	    }
	    try {
		p.matchable = (Matchable) o;
	    } catch (ClassCastException e) {
		LogUtils.logDebug(
			mc,
			Permission.class,
			"parsePermission",
			new Object[] { "Manifest corrupt: the serialization of"
				+ " the manifest entry (title: "
				+ p.title
				+ ") could not be deserialized as a valid matchable resource." },
			null);
		continue;
	    }
	    perms.add(p);
	}

	return perms.toArray(new Permission[0]);
    }
}
