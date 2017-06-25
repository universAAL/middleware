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
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.managers.api.SpaceManager;

/**
 * Commands for universAAL
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
@Command(scope = "universAAL", name = "spaces", description = "Discover the existing Spaces")
public class SpaceCommand extends OsgiCommandSupport {

	private SpaceManager spaceManager;

	@Override
	protected Object doExecute() throws Exception {
		log.debug("Executing command...");
		ServiceReference ref = bundleContext.getServiceReference(SpaceManager.class.getName());
		if (ref != null) {
			spaceManager = (SpaceManager) bundleContext.getService(ref);
		} else {
			return null;
		}
		Set<SpaceCard> spaces = spaceManager.getSpaces();
		if (spaces != null) {

			System.out.println(" Found: " + spaces.size() + " Spaces");
			System.out.println(" ----------------------------------------");
			if (spaces.size() == 0)
				System.out.println("Waiting to join a Space");

			for (SpaceCard space : spaces) {

				if (spaceManager.getSpaceDescriptor() != null
						&& space.equals(spaceManager.getSpaceDescriptor().getSpaceCard()))
					System.out.println(" * " + space.toString());
				else
					System.out.println(space.toString());
			}
		}

		return null;
	}
}
