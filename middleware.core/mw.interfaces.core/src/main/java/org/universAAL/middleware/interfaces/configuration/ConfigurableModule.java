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

package org.universAAL.middleware.interfaces.configuration;

import java.io.File;

import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationFile;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;

/**
 * Implementations of this interface should take configuration changes.
 *
 * @author amedrano
 *
 */
public interface ConfigurableModule {

	/**
	 * The namespace to be used in the configuration framework of universAAL when constructing standard URIs of ontological classes and properties.
	 * "Standard" URIs are those defined within the implementation of the universAAL middleware core.
	 */
	public static final String uAAL_CONFIG_FRAMEWORK_NAMESPACE = "http://ontology.universAAL.org/ConfigurationOntology#";
	
	/**
	 * When a configuration is changed this method is called.
	 *
	 * @param param
	 *            The parameter scope + id that has to change value.
	 * @param value
	 *            The new value to be set, in case of {@link ConfigurationFile}
	 *            this value will be a {@link File}.
	 * @return true iff the value was accepted and processed (even if it hasn't
	 *         changed).
	 */
	public boolean configurationChanged(Scope param, Object value);

}
