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

import org.universAAL.middleware.interfaces.configuration.ConfigurableModule;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;

/**
 * Ontological representation of ConfigurationParameter in the AAL configuration
 * ontology. When creating instances of this class, it is recommended to make sure that the property
 * {@link #PROP_VALUE_RESTRICTION} is set before the properties related to the (default) value,
 * otherwise there is the risk of getting a {@link NullPointerException} when calling certain methods.
 * 
 * @author amedrano
 * @author Generated initially by the OntologyUML2Java transformation of Studio; then completed manually by mtazari
 */
public class ConfigurationParameter extends Entity {
	public static final String MY_URI = ConfigurableModule.uAAL_CONFIG_FRAMEWORK_NAMESPACE + "ConfigurationParameter";
	
	public static final String PROP_VALUE_RESTRICTION = ConfigurableModule.uAAL_CONFIG_FRAMEWORK_NAMESPACE + "valueRestriction";
	
	public static final String PROP_DEFAULT_LITERAL_VALUE = ConfigurableModule.uAAL_CONFIG_FRAMEWORK_NAMESPACE + "defaultLiteralValue";
	public static final String PROP_DEFAULT_OBJECT_VALUE = ConfigurableModule.uAAL_CONFIG_FRAMEWORK_NAMESPACE + "defaultObjectValue";

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
		if (!isWellFormed())
			return PROP_SERIALIZATION_OPTIONAL;
		
		if (PROP_VALUE_RESTRICTION.equals(propURI))
			return PROP_SERIALIZATION_FULL;
		
		if (isLiteral) {
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

	public Object getValue() {
		if (isLiteral == null)
			return null;
		return isLiteral?  props.get(PROP_LITERAL_VALUE) : props.get(PROP_OBJECT_VALUE);
	}

	private boolean checkValue(MergedRestriction mr, Object newPropValue) {
		if (newPropValue == null)
			return true;
		
		if (mr == null)
			return false;
		
		String prop = mr.getOnProperty();
		if (PROP_LITERAL_VALUE.equals(prop)  ||  PROP_OBJECT_VALUE.equals(prop)) {
			Resource test = new Resource();
			test.setProperty(prop, newPropValue);
			return mr.hasMember(test);
		} else
			return false;
			
	}

	private boolean checkValue(Object newPropValue) {
		return checkValue(getValueRestriction(), newPropValue);
	}

	@SuppressWarnings("unchecked")
	public boolean setValue(Object newPropValue) {
		if (newPropValue == null) {
			props.remove(PROP_LITERAL_VALUE);
			props.remove(PROP_OBJECT_VALUE);
		} else if (checkValue(newPropValue)) {
			props.put(isLiteral? PROP_LITERAL_VALUE : PROP_OBJECT_VALUE, newPropValue);
			props.remove(isLiteral? PROP_OBJECT_VALUE : PROP_LITERAL_VALUE);
		} else
			return false;
		
		return true;
	}

	public Object getDefaultValue() {
		if (isLiteral == null)
			return null;
		return isLiteral? props.get(PROP_DEFAULT_LITERAL_VALUE)
				: props.get(PROP_DEFAULT_OBJECT_VALUE);
	}

	@SuppressWarnings("unchecked")
	public boolean setDefaultValue(Object newPropValue) {
		if (newPropValue == null) {
			props.remove(PROP_DEFAULT_LITERAL_VALUE);
			props.remove(PROP_DEFAULT_OBJECT_VALUE);
		} else if (checkValue(newPropValue)) {
			props.put(isLiteral? PROP_DEFAULT_LITERAL_VALUE : PROP_DEFAULT_OBJECT_VALUE, newPropValue);
			props.remove(isLiteral? PROP_DEFAULT_OBJECT_VALUE : PROP_DEFAULT_LITERAL_VALUE);
		} else
			return false;
		
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean setValueRestriction(MergedRestriction r) {
		if (r == null  ||  props.containsKey(PROP_VALUE_RESTRICTION))
			return false;

		String prop = r.getOnProperty();
		if (PROP_LITERAL_VALUE.equals(prop)) {
			if (props.get(PROP_DEFAULT_OBJECT_VALUE) != null  ||  props.get(PROP_OBJECT_VALUE) != null
					||  !checkValue(r, props.get(PROP_DEFAULT_LITERAL_VALUE))
					||  !checkValue(r, props.get(PROP_LITERAL_VALUE)))
				return false;
			isLiteral = Boolean.TRUE;
		} else if (PROP_OBJECT_VALUE.equals(prop)) {
			if (props.get(PROP_DEFAULT_LITERAL_VALUE) != null  ||  props.get(PROP_LITERAL_VALUE) != null
					||  !checkValue(r, props.get(PROP_DEFAULT_OBJECT_VALUE))
					||  !checkValue(r, props.get(PROP_OBJECT_VALUE)))
				return false;
			isLiteral = Boolean.FALSE;
		} else
			return false;
		
		props.put(PROP_VALUE_RESTRICTION, r);
		return true;
	}

	public MergedRestriction getValueRestriction() {
		Object o = props.get(PROP_VALUE_RESTRICTION);
		return (o instanceof MergedRestriction)?  (MergedRestriction) o : null;
	}
	
	public boolean setProperty(String propURI, Object value) {
		if (PROP_VALUE_RESTRICTION.equals(propURI))
			return (value instanceof MergedRestriction)?
					setValueRestriction((MergedRestriction) value)
					: false;

		if (isLiteral == null
				||  (!PROP_LITERAL_VALUE.equals(propURI)  &&  !PROP_DEFAULT_LITERAL_VALUE.equals(propURI)
						&&  !PROP_OBJECT_VALUE.equals(propURI)  &&  !PROP_DEFAULT_OBJECT_VALUE.equals(propURI)))
			// when isLiteral is null, we cannot do a more controlled setting of the above props
			// --> they are handled like other unknown props
			return super.setProperty(propURI, value);
		
		// in this case, isLiteral is not null AND propURI is one of the four props related to the (default) value of the conf param
		// --> we can do a more controlled setting of these props
		if ((isLiteral  &&  PROP_LITERAL_VALUE.equals(propURI))
				||  (!isLiteral  &&  PROP_OBJECT_VALUE.equals(propURI)))
			return setValue(value);
		else if ((isLiteral  &&  PROP_DEFAULT_LITERAL_VALUE.equals(propURI))
				||  (!isLiteral  &&  PROP_DEFAULT_OBJECT_VALUE.equals(propURI)))
			return setDefaultValue(value);
		else
			return false;
	}
}
