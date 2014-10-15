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

package org.universAAL.middleware.managers.api;

import java.util.List;
import java.util.Locale;

import org.universAAL.middleware.interfaces.configuration.ConfigurableModule;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DescribedEntity;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.ConfigurableEntityEditor;
import org.universAAL.middleware.interfaces.configuration.configurationEditionTypes.pattern.EntityPattern;

/**
 * Configuration Edition user interfaces should look for this service to enable
 * remote or local configuration of {@link ConfigurableModule}s.
 * 
 * @author amedrano
 * 
 */
public interface ConfigurationEditor {

    /**
     * An interface to receive asynchronously the
     * {@link ConfigurableEntityEditor}s.
     * 
     * @author amedrano
     * 
     */
    public interface ConfigurableEntityManager {

	/**
	 * Called when a new Matching {@link ConfigurableEntityEditor} is
	 * available.
	 * 
	 * @param cent
	 *            the new editor.
	 */
	public void addConfigurableEntity(ConfigurableEntityEditor cent);
    }

    /**
     * Get all the individual {@link ConfigurableEntityEditor
     * ConfigurableEntities} to configure each matching {@link DescribedEntity}.
     * This call is done synchronously, internally it will call the asynchronous
     * method, and wait for all the responses.
     * 
     * @param configPattern
     *            the matching entities pattern, to get only the corresponding
     *            {@link ConfigurableEntityEditor}s of those entities. use Empty
     *            list to get all.
     * @param locale
     *            the preferred locale for descriptions.
     * @return a list of {@link ConfigurableEntityEditor}s corresponding to
     *         matching Entities.
     */
    public List<ConfigurableEntityEditor> getMatchingConfigurationEditors(
	    List<EntityPattern> configPattern, Locale locale);

    /**
     * Subscribe asynchronously to all the all the individual
     * {@link ConfigurableEntityEditor ConfigurableEntities} to configure each
     * matching {@link DescribedEntity}.
     * 
     * @param manager
     *            the listener to be called asynchronously when a new matching
     *            {@link DescribedEntity} is found.
     * @param configPattern
     *            the pattern to match {@link DescribedEntity DescribedEntities}
     *            .
     * @param locale
     *            the preferred language of the manager.
     */
    public void registerConfigurableEntityManager(
	    ConfigurableEntityManager manager,
	    List<EntityPattern> configPattern, Locale locale);

    /**
     * Unregister a previously registered {@link ConfigurableEntityManager}, it
     * un registers all configPattern registered.
     * 
     * @param manager
     * @param configPattern
     */
    public void unRegisterConfigurableEntityManager(
	    ConfigurableEntityManager manager);
}
