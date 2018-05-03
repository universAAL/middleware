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
package org.universAAL.middleware.felix.shell.commands;

import java.io.PrintStream;
import java.util.Set;

import org.apache.felix.shell.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.interfaces.space.SpaceCard;
import org.universAAL.middleware.managers.api.SpaceManager;

public class SpaceCommand implements Command {
	private BundleContext m_context = null;
	private SpaceManager spaceManager;

	public SpaceCommand(BundleContext context) {
		this.m_context = context;
	}

	public void execute(String arg0, PrintStream out, PrintStream err) {
		ServiceReference ref = m_context.getServiceReference(SpaceManager.class.getName());
		if (ref != null) {
			spaceManager = (SpaceManager) m_context.getService(ref);
		} else {
			err.println("SpaceManager not found");
			return;
		}
		Set<SpaceCard> spaces = spaceManager.getSpaces();
		if (spaces != null) {
			out.println(" Found: " + spaces.size() + " Spaces");
			out.println(" ----------------------------------------");
			for (SpaceCard space : spaces) {

				if (spaceManager.getSpaceDescriptor() != null
						&& space.equals(spaceManager.getSpaceDescriptor().getSpaceCard()))
					out.println(" * " + space.toString());
				else
					out.println(space.toString());
			}
		}
	}

	public String getName() {
		return "universAAL:spaces";
	}

	public String getShortDescription() {
		return "List the Spaces found on this network";
	}

	public String getUsage() {
		return "universAAL:spaces";
	}
}
