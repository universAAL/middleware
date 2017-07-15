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
package org.universAAL.middleware.container.pojo;

import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.pojo.layers.AbstractBusLayer;
import org.universAAL.middleware.container.pojo.layers.BusesLayer;
import org.universAAL.middleware.container.pojo.layers.DataRepresentation;
import org.universAAL.middleware.container.pojo.layers.TurtleSerialization;

/**
 * A Helper class to run universAAL on plain java.
 * 
 * @author Carsten Stockloew
 *
 */
public class POJORunner {

	protected POJOContainer theContainer;

	protected void setUp() throws Exception {
		theContainer = new POJOContainer();
		startModule(new DataRepresentation());
		startModule(new TurtleSerialization());
		startModule(new AbstractBusLayer(false));
		startModule(new BusesLayer());
	}

	/**
	 * @throws Exception
	 * 
	 */
	protected void startModule(ModuleActivator ma) throws Exception {
		ModuleContext mc = theContainer.registerModule(new Object[] { ma });
		ma.start(mc);
	}
}
