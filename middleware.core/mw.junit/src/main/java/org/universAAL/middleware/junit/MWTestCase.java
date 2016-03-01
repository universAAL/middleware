/*	
	Copyright 2016 Carsten Stockloew
	
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
package org.universAAL.middleware.junit;

import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.container.JUnit.JUnitModuleContext.LogLevel;
import org.universAAL.middleware.bus.junit.BusTestCase;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.managers.api.DistributedMWEventHandler;
import org.universAAL.middleware.managers.distributedmw.api.DistributedBusMemberManager;
import org.universAAL.middleware.managers.distributedmw.api.DistributedLogManager;
import org.universAAL.middleware.managers.distributedmw.impl.DistributedMWManagerImpl;
import org.universAAL.middleware.tracker.IBusMemberRegistry;

/**
 * A special test case that also initializes the buses.
 * 
 * @author Carsten Stockloew
 * 
 */
public class MWTestCase extends BusTestCase {
    
    DistributedMWManagerImpl mm;

    @Override
    protected void setUp() throws Exception {
	super.setUp();

	System.out.println(" - starting MWTestCase -");
	
	mc.setAttribute(AccessControl.PROP_MODE, "none");
	((JUnitModuleContext) mc).setLogLevel(LogLevel.ERROR);
	
	org.universAAL.middleware.tracker.impl.Activator.fetchParams = new Object[] { IBusMemberRegistry.class
		.getName() };
	//org.universAAL.middleware.tracker.osgi.Activator.mc = mc;
	org.universAAL.middleware.tracker.impl.Activator bta = new org.universAAL.middleware.tracker.impl.Activator();
	bta.start(mc);

	Object[] parBMLMgmt = new Object[] { DistributedBusMemberManager.class
		.getName() };
	Object[] parLLMgmt = new Object[] { DistributedLogManager.class
		.getName() };
	Object[] parEvtH = new Object[] { DistributedMWEventHandler.class
		.getName() };
	mm = new DistributedMWManagerImpl(mc, parBMLMgmt, parBMLMgmt,
		parLLMgmt, parLLMgmt, parEvtH, parEvtH);
    }
}
