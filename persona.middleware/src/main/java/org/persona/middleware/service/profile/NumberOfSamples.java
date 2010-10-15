/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package org.persona.middleware.service.profile;

/**
 * The superclass for profile parameters that deal with counting in statistical calculations,
 * hence the restriction for the property 'sParameter' is not supported by this class but 
 * 'valueData' will be mandatory accepting a single 'xsd:nonNegativeInteger'. For the sake of
 * well-formedness, serialization type of properties and setting property values, it relies on
 * the default implementations provided by the superclasses.
 * 
 * @author mtazari
 */
public class NumberOfSamples extends ProfileParameter {
	public static final String MY_URI = PERSONA_SERVICE_NAMESPACE + "NumberOfSamples";

	public NumberOfSamples(int value) {
		super();
		addType(MY_URI, true);
		if (value > -1)
			props.put(PROP_PERSONA_PARAMETER_VALUE_DATA, new Integer(value));
	}

	public NumberOfSamples(String uri, int value) {
		super(uri);
		addType(MY_URI, true);
		if (value > -1)
			props.put(PROP_PERSONA_PARAMETER_VALUE_DATA, new Integer(value));
	}
	
	public int getNumberOfSamples() {
		Object o = props.get(PROP_PERSONA_PARAMETER_VALUE_DATA);
		return (o instanceof Integer)? ((Integer) o).intValue() : -1;
	}
	
	public void setProperty(String propURI, Object value) {
		if (propURI != null  &&  value != null  && !props.containsKey(propURI))
			if (propURI.equals(PROP_OWLS_PROFILE_SERVICE_PARAMETER_NAME)) {
				if (value instanceof String)
					props.put(propURI, value);
			} else if (propURI.equals(PROP_OWLS_PROFILE_S_PARAMETER)) {
				if (value instanceof Integer  &&  ((Integer) value).intValue() > -1)
					props.put(propURI, value);
			}
	}
}
