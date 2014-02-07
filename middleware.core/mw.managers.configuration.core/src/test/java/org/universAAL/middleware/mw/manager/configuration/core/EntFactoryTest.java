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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.mw.manager.configuration.core.impl.factories.EntityFactory;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationDefinitionTypes.ConfigurationFile;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationDefinitionTypes.ConfigurationParameter;
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
public class EntFactoryTest {

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
	
	System.out.println(e.toStringRecursive());
    }
    
    
    @Test
    public void SmurfTest(){
	Entity e = EntityFactory.getEntity(new ConfigurationParameter() {
	    
	    public Scope getScope() {
		return new AALSpaceScope("aalspace.config");
	    }
	    
	    public String getDescription(Locale loc) {
		return "";
	    }
	    
	    public MergedRestriction getType() {
		return null;
	    }
	    
	    public Object getDefaultValue() {
		return null;
	    }
	}, Locale.ENGLISH);
	
	System.out.println(e.toStringRecursive());
    }
    
    @Test
    public void fileTest(){
	Entity e = EntityFactory.getEntity(new ConfigurationFile() {
	    
	    public Scope getScope() {
		return new AALSpaceScope("AALSpace.somefile");
	    }
	    
	    public String getDescription(Locale loc) {
		return "very important file";
	    }
	    
	    public String getExtensionfilter() {
		return "*.zip";
	    }
	    
	    public URL getDefaultFileRef() {
		try {
		    return new URL("http://host/sample.zip");
		} catch (MalformedURLException e) {
		    e.printStackTrace();
		    return null;
		}
	    }
	}, Locale.ENGLISH);
	
	System.out.println(e.toStringRecursive());
    }
}
