/*	
	Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
	Institute of Information Science and Technologies 
	of the Italian National Research Council 

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
package org.universAAL.middleware.shell.universAAL.osgi;

import java.util.Set;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.managers.api.AALSpaceManager;

/**
 * Commands for universAAL
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
@Command(scope = "universAAL", name = "spaces", description = "Discover the existing AAL Spaces")
public class AALSpaceCommand extends OsgiCommandSupport {

    private AALSpaceManager aalSpaceManager;

    @Override
    protected Object doExecute() throws Exception {
	log.debug("Executing command...");
	ServiceReference ref = bundleContext
		.getServiceReference(AALSpaceManager.class.getName());
	if (ref != null) {
	    aalSpaceManager = (AALSpaceManager) bundleContext.getService(ref);
	} else {
	    return null;
	}
	Set<AALSpaceCard> aalSpaces = aalSpaceManager.getAALSpaces();
	if (aalSpaces != null) {

	    System.out.println(" Found: " + aalSpaces.size() + " AAL Spaces");
	    System.out.println(" ----------------------------------------");
	    if (aalSpaces.size() == 0)
		System.out.println("Waiting to join an AALSpace");

	    for (AALSpaceCard aalSpace : aalSpaces) {

		if (aalSpaceManager.getAALSpaceDescriptor() != null
			&& aalSpace.equals(aalSpaceManager
				.getAALSpaceDescriptor().getSpaceCard()))
		    System.out.println(" * " + aalSpace.toString());
		else
		    System.out.println(aalSpace.toString());
	    }
	}

	return null;
    }
}
