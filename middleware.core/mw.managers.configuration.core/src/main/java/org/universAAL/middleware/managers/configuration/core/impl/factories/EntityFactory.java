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

package org.universAAL.middleware.managers.configuration.core.impl.factories;

import java.net.URL;
import java.util.Locale;

import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationFile;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationParameter;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DescribedEntity;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;
import org.universAAL.middleware.owl.MergedRestriction;

/**
 * Facotry to create and update {@link Entity Entities} from {@link DescribedEntity DescribedEntities}.
 * @author amedrano
 *
 */
public class EntityFactory {

    /**
     * Given a {@link DescribedEntity} create a new Entity.
     * @param dentity template
     * @param loc default locale
     * @return the entity or null if could not create.
     */
    public static Entity getEntity(DescribedEntity dentity, Locale loc){
	if (dentity == null){
	    throw new RuntimeException("Described entity must not be null");
	}
	String uri = ScopeFactory.getScopeURN(dentity.getScope());
	if (uri == null){
	    return null;
	}
	if (dentity instanceof ConfigurationParameter){
	    org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter cp = 
		    new org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter(uri);
	    ConfigurationParameter de = (ConfigurationParameter)dentity;
	    cp.setDefaultValue(de.getDefaultValue());
	    cp.setDescription(dentity.getDescription(loc),loc);
	    // add restriction to type
	    cp.changeParameterRestriction(de.getType());
	    //set value = default (if default is null no value will be set).
	    cp.setValue(cp.getDefaultValue());
	   
	    
	    return cp;
	}
	if (dentity instanceof ConfigurationFile){
	    org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile cf = 
		    new org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile(uri);
	    ConfigurationFile de = (ConfigurationFile)dentity;
	    URL durl = de.getDefaultFileRef();
	    if (durl != null){
	    	cf.setDefaultURL(durl.toString());
	    	//set ref to default
	    	cf.setLocalURL(durl.toString());
	    }
	    cf.setExtensionFilter(de.getExtensionfilter());
	    cf.setDescription(dentity.getDescription(loc),loc);
	    
	    return cf;
	}
	return null;
    }
    
    /**
     * Update an entity from the associated {@link DescribedEntity}.
     * @param OldEntity the old {@link Entity} to update. if null then {@link EntityFactory#getEntity(DescribedEntity, Locale)} is called.
     * @param dentity the updated {@link DescribedEntity}.
     * @param loc the preferred locale.
     * @return a copied entity from OldEntity, that is updated.
     */
    public static Entity updateEntity(Entity OldEntity, DescribedEntity dentity, Locale loc){

	if (dentity == null){
	    throw new RuntimeException("Described entity must not be null");
	}
	
	if (OldEntity == null) {
	    return getEntity(dentity, loc);
	} 
	
	Entity entity = (Entity) OldEntity.copy(false);
	
	String newDescription = dentity.getDescription(loc);
	
	if (newDescription != null
		&& newDescription.isEmpty()
		&& !newDescription.equals(entity.getDescription(loc))){
	    //Locale is the same, description is different;
	    //or a new Locale is added.
	    entity.setDescription(newDescription,loc);
	    entity.incrementVersion();
	}
	
	if (dentity instanceof ConfigurationParameter){
	    // enity and dentity are assumed to be compatible.
	    org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter cp = 
		    (org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter) entity;
	    ConfigurationParameter de = (ConfigurationParameter)dentity;
	    
	    Object newDefValue = de.getDefaultValue();
	    if (!newDefValue.equals(cp.getDefaultValue())) {
		cp.setDefaultValue(newDefValue);
		cp.incrementVersion();
	    }
	    
	    MergedRestriction newType = de.getType();
	    if (!newType.equals(cp.getParameterRestriction())){
		//update restriction to type; if different, also increase version
		cp.changeParameterRestriction(newType);
		cp.incrementVersion();
	    }
	}
	if (dentity instanceof ConfigurationFile){
	    org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile cf = 
		    (org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile) entity;
	    ConfigurationFile de = (ConfigurationFile)dentity;

	    String newDefault = de.getDefaultFileRef().toString();
	    if (!newDefault.equals(cf.getDefaultURL())){
		cf.setDefaultURL(newDefault);
		cf.incrementVersion();
	    }
	    
	    String newFilter = de.getExtensionfilter();
	    if (!newFilter.equals(cf.getExtensionFilter())){
		cf.setExtensionFilter(newFilter);
		cf.incrementVersion();
	    }
	    
	}
	return entity;
    }

}
