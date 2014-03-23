/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.managers.configuration.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.interfaces.configuration.ConfigurableModule;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DescribedEntity;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;
import org.universAAL.middleware.managers.configuration.core.impl.factories.ScopeFactory;
import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.ModuleRegistry;
import org.universAAL.middleware.managers.configuration.core.owl.AALConfigurationOntology;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;

/**
 * @author amedrano
 *
 */
public class ModuleRegistryTest  {

  private class TestModule implements ConfigurableModule{

      boolean configured = false;
      
    /** {@ inheritDoc}	 */
    public boolean configurationChanged(Scope param, Object value) {
	  configured = true;
	  return true;
    }
  }

  private static JUnitModuleContext mc;

  @BeforeClass
  public static void init(){
	mc = new JUnitModuleContext();
	mc.getContainer().shareObject(mc,
			new TurtleSerializer(),
			new Object[] { MessageContentSerializer.class.getName() });

	OntologyManagement.getInstance().register(mc, new DataRepOntology());
	OntologyManagement.getInstance().register(mc, new AALConfigurationOntology());
	TurtleUtil.moduleContext = mc;
  }
  
  @Test
  public void simpleTest(){
	DescribedEntity[] des = ConfigSample.getConfigurationDescription();
	String urn = ScopeFactory.getScopeURN(des[0].getScope());
	ModuleRegistry mr = new ModuleRegistry();
	TestModule tm1 = new TestModule();
	mr.put(urn, tm1);
	assertTrue(mr.contains(urn));
	assertFalse(mr.configurationChanged(des[1].getScope(), 1));
	assertTrue(mr.configurationChanged(des[0].getScope(), 1));
	assertTrue(tm1.configured);
	mr.remove(tm1);
	assertFalse(mr.contains(urn));
	mr.clear();
  }
  
  @Test
  public void multipleTest(){
	DescribedEntity[] des = ConfigSample.getConfigurationDescription();
	String urn = ScopeFactory.getScopeURN(des[0].getScope());
	ModuleRegistry mr = new ModuleRegistry();
	TestModule tm1 = new TestModule();
	TestModule tm2 = new TestModule();
	TestModule tm3 = new TestModule();
	mr.put(urn, tm1);
	mr.put(urn, tm2);
	mr.put(urn, tm3);
	assertTrue(mr.contains(urn));
	assertTrue(mr.configurationChanged(des[0].getScope(), 1));
	assertTrue(tm1.configured);
	assertTrue(tm2.configured);
	assertTrue(tm3.configured);
	mr.clear();
	assertFalse(mr.contains(urn));
  }
}
