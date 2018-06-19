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

/**
 * Facotry to create and update {@link Entity Entities} from
 * {@link DescribedEntity DescribedEntities}.
 *
 * @author amedrano
 *
 */
public class EntityFactory {

	/**
	 * Given a {@link DescribedEntity} create a new Entity.
	 *
	 * @param dentity
	 *            template
	 * @param loc
	 *            default locale
	 * @return the entity or null if could not create.
	 */
	public static Entity getEntity(DescribedEntity dentity, Locale loc) {
		if (dentity == null) {
			throw new RuntimeException("Described entity must not be null");
		}
		String uri = ScopeFactory.getScopeURN(dentity.getScope());
		if (uri == null) {
			return null;
		}
		if (dentity instanceof ConfigurationParameter) {
			org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter cp = new org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter(
					uri);
			ConfigurationParameter de = (ConfigurationParameter) dentity;
			cp.setDescription(dentity.getDescription(loc), loc);
			
			// set restriction to type
			// it is important that this restriction is set before setting any value so that the conformance of the value can be checked in ConfigurationParameter
			cp.setValueRestriction(de.getType());
			
			Object o = de.getDefaultValue();
			if (o != null) {
				cp.setDefaultValue(o);
				// set value = default
				cp.setValue(o);
			}

			return cp;
		}
		
		if (dentity instanceof ConfigurationFile) {
			org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile cf = new org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile(
					uri);
			ConfigurationFile de = (ConfigurationFile) dentity;
			URL durl = de.getDefaultFileRef();
			if (durl != null) {
				cf.setDefaultURL(durl.toString());
				// set ref to default
				cf.setLocalURL(durl.toString());
			}
			cf.setExtensionFilter(de.getExtensionfilter());
			cf.setDescription(dentity.getDescription(loc), loc);

			return cf;
		}
		return null;
	}

	/**
	 * Update an entity from the associated {@link DescribedEntity}.
	 *
	 * @param OldEntity
	 *            the old {@link Entity} to update. if null then
	 *            {@link EntityFactory#getEntity(DescribedEntity, Locale)} is
	 *            called.
	 * @param dentity
	 *            the updated {@link DescribedEntity}.
	 * @param loc
	 *            the preferred locale.
	 * @return a copied entity from OldEntity, that is updated.
	 */
	public static Entity updateEntity(Entity oldEntity, DescribedEntity dentity, Locale loc) {

		if (dentity == null) {
			throw new RuntimeException("Described entity must not be null");
		}


		Entity newEntity = getEntity(dentity, loc);
		if (oldEntity != null  &&  newEntity != null)
			if (oldEntity.getClass() == newEntity.getClass()) {
				newEntity.setVersion(oldEntity.getVersion());
				String newDescription = dentity.getDescription(loc);
				if (newDescription != null && !newDescription.isEmpty() && !newDescription.equals(oldEntity.getDescription(loc)))
					newEntity.incrementVersion();

				if (dentity instanceof ConfigurationParameter) {
					// newEntity and dentity are assumed to be compatible; so is oldEntity
					org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter newCP = (org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter) newEntity;
					org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter oldCP = (org.universAAL.middleware.managers.configuration.core.owl.ConfigurationParameter) oldEntity;
			
					if (!newCP.getValueRestriction().equals(oldCP.getValueRestriction()))
						newEntity.incrementVersion();

					if (!newCP.getDefaultValue().equals(oldCP.getDefaultValue()))
						newEntity.incrementVersion();
				}
		
				if (dentity instanceof ConfigurationFile) {
					// newEntity and dentity are assumed to be compatible; so is oldEntity
					org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile newCF = (org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile) newEntity;
					org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile oldCF = (org.universAAL.middleware.managers.configuration.core.owl.ConfigurationFile) oldEntity;
			
					String defURL = newCF.getDefaultURL();
					if (defURL != null && !defURL.equals(oldCF.getDefaultURL()))
						newEntity.incrementVersion();

					if (!newCF.getExtensionFilter().equals(oldCF.getExtensionFilter()))
						newEntity.incrementVersion();
				}
			} else 
				newEntity.setVersion(oldEntity.getVersion()+3);
				
		return newEntity;
	}

}
