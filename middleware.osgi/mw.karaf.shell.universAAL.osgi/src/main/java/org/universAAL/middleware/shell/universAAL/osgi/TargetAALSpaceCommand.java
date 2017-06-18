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

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.managers.api.SpaceManager;

/**
 * Commands for universAAL
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
@Command(scope = "universAAL", name = "targetSpace", description = "Print the Target AAL Spaces")
public class TargetAALSpaceCommand extends OsgiCommandSupport {

	private SpaceManager aalSpaceManager;

	@Override
	protected Object doExecute() throws Exception {
		log.debug("Executing command...");
		ServiceReference ref = bundleContext.getServiceReference(SpaceManager.class.getName());
		if (ref != null) {
			aalSpaceManager = (SpaceManager) bundleContext.getService(ref);
		} else {
			return null;
		}
		// Aalspace aalSpace =
		// aalSpaceManager.readAALSpaceDefaultConfigurations();
		System.out.println("--------------------------");
		System.out.println("Target AAL Space:");
		// System.out.println("AAL Space Name:
		// "+aalSpace.getSpaceDescriptor().getSpaceName()
		// + "" +
		// "AAL Space ID: "+aalSpace.getSpaceDescriptor().getSpaceId() +
		// " AAL Space Descriptor:
		// "+aalSpace.getSpaceDescriptor().getSpaceDescription());
		System.out.println("--------------------------");
		return null;
	}
}
