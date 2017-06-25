/*
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research

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
package org.universAAL.middleware.test.util;

import java.util.HashMap;
import java.util.List;

import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.Variable;

/**
 * Implementation of process:Parameter; see service bus for details
 */
public class ProcessParameter extends Variable {
	public static final String OWLS_PROCESS_NAMESPACE = "http://universAAL.org/Process.owl#";
	public static final String MY_URI = OWLS_PROCESS_NAMESPACE + "Parameter";

	public static final String PROP_OWLS_PROCESS_PARAMETER_TYPE = OWLS_PROCESS_NAMESPACE + "parameterType";
	public static final String PROP_OWLS_PROCESS_PARAMETER_VALUE = OWLS_PROCESS_NAMESPACE + "parameterValue";
	public static final String PROP_PARAMETER_DEFAULT_VALUE = SERVICE_NAMESPACE + "defaultValue";
	public static final String PROP_PARAMETER_CARDINALITY = SERVICE_NAMESPACE + "parameterCardinality";
	public static final String PROP_PARAMETER_MAX_CARDINALITY = SERVICE_NAMESPACE + "parameterMaxCardinality";
	public static final String PROP_PARAMETER_MIN_CARDINALITY = SERVICE_NAMESPACE + "parameterMinCardinality";

	public static final String PROP_OWLS_VALUE_OF_THE_VAR = OWLS_PROCESS_NAMESPACE + "theVar";
	public static final String TYPE_OWLS_VALUE_OF = OWLS_PROCESS_NAMESPACE + "ValueOf";

	private static VariableHandler handler = new VariableHandler() {
		/**
		 * Return true iff the object is Variable Reference (a resource of OWL-S
		 * http://www.daml.org/services/owl-s/1.1/Process.owl#ValueOf class)
		 *
		 * @param o
		 *            - the object to test
		 * @return - true iff the object is a resource of OWL-S ValueOf class
		 */
		public boolean isVarRef(Object o) {
			return o instanceof Resource && TYPE_OWLS_VALUE_OF.equals(((Resource) o).getType());
		}

		/**
		 * Return the variable from the variable reference, either by the
		 * property {@link #PROP_OWLS_VALUE_OF_THE_VAR}
		 * (http://www.daml.org/services/owl-s/1.1/Process.owl#theVar) or from
		 * the context.
		 *
		 * @param o
		 *            - the variable reference
		 * @param context
		 *            - the context
		 * @return - the variable
		 */
		public Object resolveVarRef(Object o, HashMap context) {
			ProcessParameter var = null;
			if (isVarRef(o)) {
				Object aux = ((Resource) o).getProperty(PROP_OWLS_VALUE_OF_THE_VAR);
				if (aux == null)
					// strange
					return o;
				if (aux instanceof ProcessParameter)
					var = (ProcessParameter) aux;
				else if (context != null) {
					// it can be a standard variable supported by the middleware
					aux = context.get(aux.toString());
					if (aux != null)
						return aux;
				}
			}

			if (var == null)
				return o;

			if (context == null)
				return var;
			o = context.get(var.getURI());
			return (o == null) ? var : o;
		}

		public boolean checkDeserialization(Object o) {
			// not needed for test
			return true;
		}
	};

	static {
		register(handler);
	}

	public ProcessParameter(String uri) {
		super(uri);
		addType(MY_URI, true);
	}

	/**
	 * Create a variable reference (a resource of OWL-S
	 * http://www.daml.org/services/owl-s/1.1/Process.owl#ValueOf class) from
	 * this ProcessParameter
	 *
	 * @return the Variable Reference resource
	 */
	public Resource asVariableReference() {
		Resource result = new Resource();
		result.addType(TYPE_OWLS_VALUE_OF, true);
		result.setProperty(PROP_OWLS_VALUE_OF_THE_VAR, this);
		return result;
	}

	/**
	 * Returns the maximal cardinality of this parameter
	 *
	 * @return - the maximal cardinality
	 */
	public int getMaxCardinality() {
		Integer i = (Integer) props.get(PROP_PARAMETER_CARDINALITY);
		if (i == null)
			i = (Integer) props.get(PROP_PARAMETER_MAX_CARDINALITY);
		if (i == null)
			return Integer.MAX_VALUE;
		return i.intValue();
	}

	/**
	 * Returns the minimal cardinality of this parameter
	 *
	 * @return - the minimal cardinality
	 */
	public int getMinCardinality() {
		Integer i = (Integer) props.get(PROP_PARAMETER_CARDINALITY);
		if (i == null)
			i = (Integer) props.get(PROP_PARAMETER_MIN_CARDINALITY);
		if (i == null)
			return 0;
		return i.intValue();
	}

	/**
	 * Returns the default value of this parameter
	 *
	 * @return - the object representing the default value
	 */
	public Object getDefaultValue() {
		Object o = props.get(PROP_PARAMETER_DEFAULT_VALUE);
		if (o instanceof Resource) {
			List aux = ((Resource) o).asList();
			if (aux != null)
				return aux;
		}
		return o;
	}

	/**
	 * Returns the parameter type of this parameter
	 *
	 * @return String - the parameter type
	 */
	public String getParameterType() {
		Resource parameterType = (Resource) props.get(PROP_OWLS_PROCESS_PARAMETER_TYPE);
		if (parameterType == null)
			return null;
		return parameterType.getURI();
	}

	/**
	 * Returns the value of this parameter
	 *
	 * @return - the object representing the value
	 */
	public Object getParameterValue() {
		Object o = props.get(PROP_OWLS_PROCESS_PARAMETER_VALUE);
		if (o instanceof Resource) {
			List aux = ((Resource) o).asList();
			if (aux != null)
				return aux;
		}
		return o;
	}

	/**
	 * return true iff this process parameter is well formed (the properties
	 * have consistent values)
	 */
	public boolean isWellFormed() {
		Object o = props.get(PROP_PARAMETER_MAX_CARDINALITY);
		// check cardinality
		if (o != null) {
			int max;
			if (o instanceof Integer)
				if (props.containsKey(PROP_PARAMETER_CARDINALITY))
					return false;
				else
					max = ((Integer) o).intValue();
			else
				return false;
			o = props.get(PROP_PARAMETER_MIN_CARDINALITY);
			if (o != null)
				if (o instanceof Integer) {
					int min = ((Integer) o).intValue();
					if (min < 1 || min >= max)
						return false;
				} else
					return false;
		} else {
			o = props.get(PROP_PARAMETER_CARDINALITY);
			if (o != null)
				if (o instanceof Integer)
					if (props.containsKey(PROP_PARAMETER_MIN_CARDINALITY))
						return false;
					else {
						if (((Integer) o).intValue() < 1)
							return false;
					}
				else
					return false;
			else {
				o = props.get(PROP_PARAMETER_MIN_CARDINALITY);
				if (o != null)
					if (o instanceof Integer) {
						if (((Integer) o).intValue() < 1)
							return false;
					} else
						return false;
			}
		}
		// check value
		o = props.get(PROP_OWLS_PROCESS_PARAMETER_VALUE);
		if (o != null && !TypeMapper.isLiteral(o))
			return false;
		// check default value
		o = props.get(PROP_PARAMETER_DEFAULT_VALUE);
		if (o != null && !TypeMapper.isLiteral(o))
			return false;
		// check type
		o = props.get(PROP_OWLS_PROCESS_PARAMETER_TYPE);
		if (o == null)
			return true;
		return o instanceof Resource && ((Resource) o).numberOfProperties() == 0
				&& ((Resource) o).serializesAsXMLLiteral() && !((Resource) o).isAnon();
	}

	/**
	 * Set cardinality of the process parameter
	 *
	 * @param max
	 *            - maximal cardinality
	 * @param min
	 *            - minimal cardinality
	 */
	public final boolean setCardinality(int max, int min) {
		if (!props.containsKey(PROP_PARAMETER_CARDINALITY) && !props.containsKey(PROP_PARAMETER_MAX_CARDINALITY)
				&& !props.containsKey(PROP_PARAMETER_MIN_CARDINALITY)) {
			if (max > 0)
				if (max == min) {
					props.put(PROP_PARAMETER_CARDINALITY, new Integer(max));
					return true;
				} else {
					props.put(PROP_PARAMETER_MAX_CARDINALITY, new Integer(max));
					return true;
				}
			if (min > 0 && min != max) {
				props.put(PROP_PARAMETER_MIN_CARDINALITY, new Integer(min));
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the default value of this parameter
	 *
	 * @param value
	 *            - the object representing the default value
	 */
	public final boolean setDefaultValue(Object value) {
		if (props.containsKey(PROP_PARAMETER_DEFAULT_VALUE))
			return false;

		value = TypeMapper.asLiteral(value);
		if (value != null) {
			props.put(PROP_PARAMETER_DEFAULT_VALUE, value);
			return true;
		}
		return false;
	}

	/**
	 * Set the parameter type of this parameter
	 *
	 * @param typeURI
	 *            - the URI of the parameter type
	 */
	public final boolean setParameterType(String typeURI) {
		if (StringUtils.isQualifiedName(typeURI) && !props.containsKey(PROP_OWLS_PROCESS_PARAMETER_TYPE)) {
			props.put(PROP_OWLS_PROCESS_PARAMETER_TYPE, new Resource(typeURI, true));
			return true;
		}
		return false;
	}

	/**
	 * Set the value of this parameter
	 *
	 * @param value
	 *            - the object representing the value
	 */
	public final boolean setParameterValue(Object value) {
		if (!props.containsKey(PROP_OWLS_PROCESS_PARAMETER_VALUE)) {
			value = TypeMapper.asLiteral(value);
			if (value != null) {
				props.put(PROP_OWLS_PROCESS_PARAMETER_VALUE, value);
				return true;
			}
		}
		return false;
	}

	/**
	 * Set a value of a property for this process parameter
	 *
	 * @param prop
	 *            - the property to set
	 * @param val
	 *            - the value to set for the property
	 */
	public boolean setProperty(String prop, Object val) {
		if (prop == null || val == null || props.containsKey(prop))
			return false;

		if (prop.equals(PROP_PARAMETER_CARDINALITY)) {
			if (val instanceof Integer) {
				int i = ((Integer) val).intValue();
				return setCardinality(i, i);
			}
		} else if (prop.equals(PROP_PARAMETER_DEFAULT_VALUE)) {
			return setDefaultValue(val);
		} else if (prop.equals(PROP_PARAMETER_MAX_CARDINALITY)) {
			if (!props.containsKey(PROP_PARAMETER_CARDINALITY) && val instanceof Integer) {
				props.put(prop, val);
				return true;
			}
		} else if (prop.equals(PROP_PARAMETER_MIN_CARDINALITY)) {
			if (!props.containsKey(PROP_PARAMETER_CARDINALITY) && val instanceof Integer) {
				props.put(prop, val);
				return true;
			}
		} else if (prop.equals(PROP_OWLS_PROCESS_PARAMETER_TYPE)) {
			return setParameterType(val.toString());
		} else if (prop.equals(PROP_OWLS_PROCESS_PARAMETER_VALUE)) {
			return setParameterValue(val);
		} else
			return super.setProperty(prop, val);
		return false;
	}
}
