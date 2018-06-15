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
package org.universAAL.middleware.managers.configuration.core.owl;

import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;

/**
 * Ontological representation of ConfigurationParameter in the configuration
 * ontology. Methods included in this class are the mandatory ones for
 * representing an ontological concept in Java classes for the universAAL
 * platform. In addition getters and setters for properties are included.
 *
 * @author amedrano
 * @author Generated initially by the OntologyUML2Java transformation of Studio; then completed manually by mtazari
 */
public class ConfigurationParameter extends Entity {
	public static final String MY_URI = ConfigurationOntology.NAMESPACE + "ConfigurationParameter";
	
	public static final String PROP_VALUE_RESTRICTION = ConfigurationOntology.NAMESPACE + "valueRestriction";
	
	public static final String PROP_DEFAULT_LITERAL_VALUE = ConfigurationOntology.NAMESPACE + "defaultLiteralValue";
	public static final String PROP_DEFAULT_OBJECT_VALUE = ConfigurationOntology.NAMESPACE + "defaultObjectValue";

	public static final String PROP_LITERAL_VALUE = org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationParameter.PROP_CONFIG_LITERAL_VALUE;
	public static final String PROP_OBJECT_VALUE = org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationParameter.PROP_CONFIG_OBJECT_VALUE;
	
	private Boolean isLiteral = null;

	public ConfigurationParameter() {
		super();
	}

	public ConfigurationParameter(String uri) {
		super(uri);
	}

	public String getClassURI() {
		return MY_URI;
	}

	public int getPropSerializationType(String propURI) {
		if (PROP_VALUE_RESTRICTION.equals(propURI))
			return PROP_SERIALIZATION_FULL;
		
		if (hasLiteralValue()) {
			if (PROP_DEFAULT_LITERAL_VALUE.equals(propURI)
					||  PROP_LITERAL_VALUE.equals(propURI))
				return PROP_SERIALIZATION_FULL;
			if (PROP_DEFAULT_OBJECT_VALUE.equals(propURI)
					||  PROP_OBJECT_VALUE.equals(propURI))
				return PROP_SERIALIZATION_OPTIONAL;
		} else {
			if (PROP_DEFAULT_LITERAL_VALUE.equals(propURI)
					||  PROP_LITERAL_VALUE.equals(propURI))
				return PROP_SERIALIZATION_OPTIONAL;
			if (PROP_DEFAULT_OBJECT_VALUE.equals(propURI)
					||  PROP_OBJECT_VALUE.equals(propURI))
				return PROP_SERIALIZATION_FULL;
		}
		
		return super.getPropSerializationType(propURI);
	}

	public boolean isWellFormed() {
		return super.isWellFormed()
				&&  isLiteral != null; // this is equivalent to props.containsKey(PROP_VALUE_RESTRICTION) --> check how isLiteral is set
	}
	
	public boolean hasLiteralValue() {
		return isLiteral.booleanValue();
	}

	public Object getValue() {
		return hasLiteralValue()?  props.get(PROP_LITERAL_VALUE) : props.get(PROP_OBJECT_VALUE);
	}

	private boolean checkValue(Object newPropValue) {
		Resource test = this.copy(false);
		if (hasLiteralValue())
			test.changeProperty(PROP_LITERAL_VALUE, newPropValue);
		else
			test.changeProperty(PROP_OBJECT_VALUE, newPropValue);
		
		// check Restrictions
		MergedRestriction mr = getValueRestriction();
		if (mr != null)
			return mr.hasMember(test);
		else
			return false;
	}

	public boolean setValue(Object newPropValue) {
		if (newPropValue == null  ||  checkValue(newPropValue)) {
			return hasLiteralValue()? changeProperty(PROP_LITERAL_VALUE, newPropValue)
					: changeProperty(PROP_OBJECT_VALUE, newPropValue);
		}
		return false;
	}

	public Object getDefaultValue() {
		return hasLiteralValue()? getProperty(PROP_DEFAULT_LITERAL_VALUE)
				: getProperty(PROP_DEFAULT_OBJECT_VALUE);
	}

	public void setDefaultValue(Object newPropValue) {
		if (newPropValue != null  &&  checkValue(newPropValue))
			if (hasLiteralValue())
				changeProperty(PROP_LITERAL_VALUE, newPropValue);
			else
				changeProperty(PROP_OBJECT_VALUE, newPropValue);
	}

	@SuppressWarnings("unchecked")
	public void setValueRestriction(MergedRestriction r) {
		if (r == null  ||  props.containsKey(PROP_VALUE_RESTRICTION))
			return;

		String prop = r.getOnProperty();
		if (PROP_LITERAL_VALUE.equals(prop))
			isLiteral = Boolean.TRUE;
		else if (PROP_OBJECT_VALUE.equals(prop))
			isLiteral = Boolean.FALSE;
		else return;
		
		props.put(PROP_VALUE_RESTRICTION, r);
	}

	public MergedRestriction getValueRestriction() {
		Object o = props.get(PROP_VALUE_RESTRICTION);
		return (o instanceof MergedRestriction)?  (MergedRestriction) o : null;
	}
	
	public boolean setProperty(String propURI, Object value) {
		if (PROP_VALUE_RESTRICTION.equals(propURI)) {
			if (value instanceof MergedRestriction)
				setValueRestriction((MergedRestriction) value);
			return props.get(propURI) == value;
		}
		
		if (isLiteral != null) {
			// when isLiteral is not null, we can do a more controlled setting of the other props
			// otherwise, they have to be set through the implementation in the super class as can be seen further below
			if (PROP_LITERAL_VALUE.equals(propURI)  ||  PROP_OBJECT_VALUE.equals(propURI)) {
				setValue(value);
				return getValue() == value;
			}
		
			if (PROP_DEFAULT_LITERAL_VALUE.equals(propURI)  ||  PROP_DEFAULT_OBJECT_VALUE.equals(propURI)) {
				setDefaultValue(value);
				return getDefaultValue() == value;
			}
		}
		
		return super.setProperty(propURI, value);
	}
}
