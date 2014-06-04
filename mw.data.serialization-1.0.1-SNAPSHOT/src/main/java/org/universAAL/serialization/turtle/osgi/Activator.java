/*
	Copyright 2009-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut f�r Graphische Datenverarbeitung
	
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
package org.universAAL.serialization.turtle.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;
import org.universAAL.serialization.turtle.TurtleParser;
import org.universAAL.serialization.turtle.TurtleUtil;

/**
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
	TurtleUtil.moduleContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	uAALBundleContainer.THE_CONTAINER.shareObject(TurtleUtil.moduleContext,
		new TurtleParser(),
		new Object[] { MessageContentSerializer.class.getName() });
    }

    public void stop(BundleContext arg0) throws Exception {
	// TODO Auto-generated method stub
    }
}