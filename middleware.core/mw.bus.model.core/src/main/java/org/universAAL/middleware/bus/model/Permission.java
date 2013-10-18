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
package org.universAAL.middleware.bus.model;

import java.util.LinkedList;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.ModuleContext;

public class Permission {
    String title = "";
    String description = "";
    String serialization = "";
    Matchable matchable = null;

    public static final String ADVERTISEMENT = "advertisement";
    public static final String REQUIREMENT = "requirement";
    public static final String PERMISSIONS = "App-permissions";
    public static final String SEPARATOR_TITLE = "---</title>---";
    public static final String SEPARATOR_DESCRIPTION = "---</description>---";
    public static final String SEPARATOR_SERIALIZATION = "---</serialization>---";

    public String getTitle() {
	return title;
    }

    public String getDescription() {
	return description;
    }

    public Matchable getMatchable() {
	return matchable;
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
	    // TODO: log entry - no permissions defined
	    System.out.println(" -- fromManifest no permissions defined for "
		    + name);
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
		// TODO: log entry - corrupt manifest
		System.out.println(" -- fromManifest corrupt manifest 1");
		return new Permission[0];
	    }
	    p.description = restsplit[0];

	    restsplit = restsplit[1].split(SEPARATOR_SERIALIZATION);
	    // if (restsplit.length != 2) {
	    if (((i != titlesplit.length - 1) && (restsplit.length != 2))
		    || ((i == titlesplit.length - 1) && restsplit.length != 1)) {
		// TODO: log entry - corrupt manifest
		System.out.println(" -- fromManifest corrupt manifest 2");
		return new Permission[0];
	    }
	    if (restsplit.length == 2)
		lastTitle = restsplit[1];
	    p.serialization = restsplit[0];
	    Object o = BusMessage.deserializeAsContent(p.serialization);
	    try {
		p.matchable = (Matchable) o;
	    } catch (ClassCastException e) {
		// TODO: log entry - invalid serialization
		System.out.println(" -- fromManifest invalid serialization");
		continue;
	    }
	    perms.add(p);
	}

	return perms.toArray(new Permission[0]);
    }
}
