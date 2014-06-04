/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

import java.net.URL;
import java.util.Locale;

import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationDefinedElsewhere;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationFile;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationParameter;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DescribedEntity;
import org.universAAL.middleware.interfaces.configuration.scope.InstanceScope;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;
import org.universAAL.middleware.owl.IntRestriction;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * @author amedrano
 *
 */
public class ConfigSample {

   public static DescribedEntity[] getConfigurationDescription(){
	return new DescribedEntity[]{
		new ConfigurationParameter() {
		    
		    public Scope getScope() {
			return Scope.applicationScope("number.of.foos", "foo.giver");
		    }
		    
		    public String getDescription(Locale loc) {
			if (loc.equals(Locale.FRENCH)){
			    //return description in french.
			}
			return "The number of foos to give by this foo giver module";
		    }
		    
		    public MergedRestriction getType() {
			MergedRestriction mr = MergedRestriction
				.getAllValuesRestrictionWithCardinality(ConfigurationParameter.PROP_CONFIG_VALUE, 
					TypeMapper.getDatatypeURI(Integer.class), 1, 1);
			mr.addType(new IntRestriction(0, true, 10, true));
			return mr;
		    }
		    
		    public Object getDefaultValue() {
			return 1;
		    }
		},
		new ConfigurationParameter() {
		    
		    public Scope getScope() {
			return Scope.aalScope("foo.target");
		    }
		    
		    public String getDescription(Locale loc) {
			// use localized messages properties file to return description.
			return null;
		    }
		    
		    public MergedRestriction getType() {
			return MergedRestriction
				.getAllValuesRestrictionWithCardinality(ConfigurationParameter.PROP_CONFIG_VALUE, 
					TypeMapper.getDatatypeURI(String.class), 1, 1);
		    }
		    
		    public Object getDefaultValue() {
			return null;
		    }
		},
		new ConfigurationDefinedElsewhere(Scope.aalScope("super.config")),
		new ConfigurationParameter() {
		    
		    public Scope getScope() {
			return new InstanceScope("noise.limit", "mySuperPeer");
		    }
		    
		    public String getDescription(Locale loc) {
			return "the noise limit of the instance, in dB";
		    }
		    
		    public MergedRestriction getType() {
			return MergedRestriction.getAllValuesRestrictionWithCardinality(PROP_CONFIG_VALUE, TypeMapper.getDatatypeURI(Integer.class), 0, 1);
		    }
		    
		    public Object getDefaultValue() {
			return Integer.valueOf(-3);
		    }
		},

		
		//TROLL config for testing
		new ConfigurationFile() {
		    
		    public Scope getScope() {
			return null;
		    }
		    
		    public String getDescription(Locale loc) {
			return null;
		    }
		    
		    public URL getDefaultFileRef() {
			return null;
		    }

		    public String getExtensionfilter() {
			return "*.*";
		    }
		},
	};
    }
}
