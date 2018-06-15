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

package org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;

/**
 * An Configuration entity that consists of a key and a direct value (as opposed to {@link ConfigurationFile}
 * whose value is a reference to a file when that file as a whole serves as the value). The key is the 
 *
 * @author amedrano
 *
 */
public interface ConfigurationParameter extends DescribedEntity {

	/**
	 * The property-URI to be used in the role of <code>"owl:onProperty"</code> when constructing the
	 * {@link MergedRestriction} to be used as the type of the values of a conf param if the conf
	 * param is supposed to have literal values. In other words, when {@link MergedRestriction#getOnProperty()}
	 * called on {@link #getType()} of a conf param returns this property-URI, it means that the conf param
	 * accepts only literal values. 
	 */
	public static final String PROP_CONFIG_LITERAL_VALUE = "http://ontology.universAAL.org/ConfigurationOntology#" + "hasLiteralValue";
	
	/**
	 * The property-URI to be used in the role of <code>"owl:onProperty"</code> when constructing the
	 * {@link MergedRestriction} to be used as the type of the values of a conf param if the conf
	 * param is supposed to have object values. In other words, when {@link MergedRestriction#getOnProperty()}
	 * called on {@link #getType()} of a conf param returns this property-URI, it means that the conf param
	 * accepts only instances of {@link ManagedIndividual}.
	 */
	public static final String PROP_CONFIG_OBJECT_VALUE = "http://ontology.universAAL.org/ConfigurationOntology#" + "hasObjectValue";

	/**
	 * The default value for a mandatory conf param whose value may not be set. Optional conf params do not need any default value.
	 * By defining a default value for a mandatory conf param, de facto it is made optional!
	 *
	 * @return default vaule to use.
	 */
	public Object getDefaultValue();

	/**
	 * The type of the value(s) that the configuration parameter is allowed to have. The
	 * {@link MergedRestriction} has to be bounded to one of the two properties
	 * {@link ConfigurationParameter#PROP_CONFIG_LITERAL_VALUE} or {@link ConfigurationParameter#PROP_CONFIG_OBJECT_VALUE}
	 * depending on whether the conf param accepts literal values or object values (instances of {@link ManagedIndividual}, otherwise the
	 * {@link ConfigurationParameter} will not register properly.
	 *
	 * @return a {@link MergedRestriction} containing all conditions on the value of the this {@link ConfigurationParameter}.
	 */
	public MergedRestriction getType();
}
