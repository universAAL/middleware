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

package org.universAAL.middleware.mw.manager.configuration.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.mw.manager.configuration.core.impl.factories.EntityFactory;
import org.universAAL.middleware.mw.manager.configuration.core.impl.secondaryManagers.EntityManager;
import org.universAAL.middleware.mw.manager.configuration.core.impl.secondaryManagers.SharedObjectConnector;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationDefinitionTypes.ConfigurationParameter;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationDefinitionTypes.DescribedEntity;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.AALSpaceScope;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.Scope;
import org.universAAL.middleware.mw.manager.configuration.core.owl.AALConfigurationOntology;
import org.universAAL.middleware.mw.manager.configuration.core.owl.Entity;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.IntRestriction;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;

/**
 * @author amedrano
 *
 */
public class EntityMngrTest {


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

    private EntityManager init(File f){
	f.delete();
	return new EntityManager(new SharedObjectConnector(mc), f);
    }
    
    @Test
    public void initTest(){
	init(new File("target/Entities.ttl"));
    }
    
    @Test
    public void addTest(){
	
	Entity e = EntityFactory.getEntity(new ConfigurationParameter() {
	    
	    public Scope getScope() {
		return new AALSpaceScope("aalspace.config");
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

	EntityManager em = init(new File("target/addTest.ttl"));
	assertTrue(em.addEntity(e));
    }
    
    @Test
    public void addTests2(){
	Entity e = EntityFactory.getEntity(new ConfigurationParameter() {
	    
	    public Scope getScope() {
		return new AALSpaceScope("aalspace.config");
	    }
	    
	    public String getDescription(Locale loc) {
		return "some config";
	    }
	    
	    public MergedRestriction getType() {
		MergedRestriction mr = MergedRestriction
			.getAllValuesRestrictionWithCardinality(ConfigurationParameter.PROP_CONFIG_VALUE, 
				TypeMapper.getDatatypeURI(Integer.class), 1, 1);
		mr.addType(new IntRestriction(0, true, 10, true));
		return mr;
	    }
	    
	    public Object getDefaultValue() {
		return Integer.valueOf(1);
	    }
	}, Locale.ENGLISH);

	EntityManager em = init(new File("target/addTest2.ttl"));
	assertTrue(em.addEntity(e));
	assertFalse(em.addEntity(e));
	e.incrementVersion();
	assertTrue(em.addEntity(e));
    }
    
    @Test
    public void addTests3(){
	EntityManager em = init(new File("target/addTest3.ttl"));
	assertFalse(em.addEntity(null));
    }
    
    @Test
    public void findTests1(){
	EntityManager em = init(new File("target/Entities.ttl"));
	assertNull(em.find((String)null));
	assertNull(em.find(""));
	assertNull(em.find("lolz"));
    }
    
    @Test
    public void findTests2(){
	EntityManager em = init(new File("target/findTest2.ttl"));
	Entity e = EntityFactory.getEntity(ConfigSample.getConfigurationDescription()[1], Locale.ENGLISH);
	em.addEntity(e);
	assertNull(em.find("urn:configscope:aalspace.config"));
	assertNotNull(em.find(e.getURI()));
	assertEquals(e, em.find(e.getURI()));
    }
    
    @Test
    public void mergeTest1(){
	EntityManager em = init(new File("target/mergeTest1.ttl"));
	List<Entity> les = getAListOfEntities();
	List<Entity> a = em.mergeAdd(les);
	assertTrue(a.isEmpty());
	les.get(1).incrementVersion();
	em.addEntity(les.get(1));
	a = em.mergeAdd(getAListOfEntities());
	assertEquals(1, a.size());
	assertEquals(les.get(1), a.get(0));
    }
    
    private List<Entity> getAListOfEntities(){
	DescribedEntity[] des = ConfigSample.getConfigurationDescription();
	List<Entity> les = new ArrayList<Entity>();
	for (int i = 0; i < des.length; i++) {
	    les.add(EntityFactory.getEntity(des[i], Locale.ENGLISH));
	}
	return les;
    }
}
