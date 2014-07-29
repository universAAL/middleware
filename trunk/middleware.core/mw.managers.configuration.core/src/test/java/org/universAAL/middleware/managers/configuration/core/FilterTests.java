/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationParameter;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.ApplicationPartPattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.ApplicationPattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.IdPattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.ModulePattern;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.NotPattern;
import org.universAAL.middleware.interfaces.configuration.scope.AALSpaceScope;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;
import org.universAAL.middleware.managers.configuration.core.impl.factories.EntityFactory;
import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.EntityManager;
import org.universAAL.middleware.managers.configuration.core.owl.AALConfigurationOntology;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.IntRestriction;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;

/**
 * @author amedrano
 * 
 */
public class FilterTests {

    private static JUnitModuleContext mc;

    @BeforeClass
    public static void init() {
	mc = new JUnitModuleContext();
	mc.getContainer().shareObject(mc, new TurtleSerializer(),
		new Object[] { MessageContentSerializer.class.getName() });

	OntologyManagement.getInstance().register(mc, new DataRepOntology());
	OntologyManagement.getInstance().register(mc,
		new AALConfigurationOntology());
	TurtleUtil.moduleContext = mc;
    }

    @Test
    public void testPatterns(){

	Entity e = EntityFactory.getEntity(new ConfigurationParameter() {
	    
	    public Scope getScope() {
		return Scope.applicationPartScope("my.id", "app", "myPart");
	    }
	    
	    public String getDescription(Locale loc) {
		return "some config";
	    }
	    
	    public MergedRestriction getType() {
		MergedRestriction mr = MergedRestriction
			.getAllValuesRestrictionWithCardinality(ConfigurationParameter.PROP_CONFIG_VALUE, 
				new IntRestriction(0, true, 10, true), 1, 1);
		return mr;
	    }
	    
	    public Object getDefaultValue() {
		return Integer.valueOf(1);
	    }
	}, Locale.ENGLISH);
	assertTrue(new ApplicationPartPattern("myPart").getRestriction().hasMember(e));
	assertFalse(new ApplicationPartPattern("yourPart").getRestriction().hasMember(e));
	assertTrue(new ApplicationPartPattern().getRestriction().hasMember(e));
	assertTrue(new ApplicationPattern("app").getRestriction().hasMember(e));
	assertFalse(new ApplicationPattern("appo").getRestriction().hasMember(e));
	assertTrue(new ApplicationPattern().getRestriction().hasMember(e));
	assertTrue(new IdPattern("my.id").getRestriction().hasMember(e));
	assertFalse(new IdPattern("your.id").getRestriction().hasMember(e));
	assertFalse(new IdPattern().getRestriction().hasMember(e));
	assertTrue(new NotPattern(new ModulePattern()).getRestriction().hasMember(e));
    }
    
    @Test
    public void testFilter1(){
Entity e = EntityFactory.getEntity(new ConfigurationParameter() {
	    
	    public Scope getScope() {
		return Scope.applicationPartScope("my.id", "app", "myPart");
	    }
	    
	    public String getDescription(Locale loc) {
		return "some config";
	    }
	    
	    public MergedRestriction getType() {
		MergedRestriction mr = MergedRestriction
			.getAllValuesRestrictionWithCardinality(ConfigurationParameter.PROP_CONFIG_VALUE, 
				new IntRestriction(0, true, 10, true), 1, 1);
		return mr;
	    }
	    
	    public Object getDefaultValue() {
		return Integer.valueOf(1);
	    }
	}, Locale.ENGLISH);

	List<Entity> in = new ArrayList<Entity>();
	in.add(e);
	
	List<TypeExpression> filters = new ArrayList<TypeExpression>();
	filters.add(new ApplicationPartPattern("myPart").getRestriction());
	filters.add(new ApplicationPattern("app").getRestriction());
	
	List<Entity> out = EntityManager.filter(in, filters);
	assertEquals(in.size(),out.size());
	assertEquals(in.get(0), out.get(0));
    }
}
