/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.service.owls.process;

import java.util.Hashtable;
import java.util.List;

import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.Variable;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.owl.Service;

/**
 * Implementation of process:Parameter
 * (xmlns:process="http://www.daml.org/services/owl-s/1.1/Process.owl#"). A
 * parameter has an overall type and a value that can be set differently in
 * different utilization contexts, unless it is a constant.
 * <p>
 * This implementation of process:Parameter, however, differs from the OWL-S
 * process ontology in the following way:
 * <ol>
 * <li>Three uAAL-specific properties are added in order to be able to define
 * cardinality restrictions on process:parameterValue. All three are optional
 * properties and accept only positive integers. Negative values and zero will
 * simply be ignored. The corresponding properties are <code>cardinality</code>
 * for specifying a precise cardinality, <code>maxCardinality</code> for
 * specifying the maximum number of values assignable to the parameter, and
 * <code>minCardinality</code> for specifying the minimum number of values to be
 * assigned to the parameter. The <code>cardinality</code> property is not
 * visible at the programming level and is only used in serializations, when
 * both <code>maxCardinality</code> and <code>minCardinality</code> have the
 * same value in order to produce shorter serializations. If no value is
 * specified for <code>maxCardinality</code>, {@link Integer#MAX_VALUE} is
 * assumed; in case of <code>minCardinality</code>, zero is assumed, which means
 * that the parameter is an optional parameter. Hence, the original
 * process:parameterType specifies the type of a single value and the above
 * cardinality specifications determine how many of such values can be assigned
 * to process:parameterValue.
 * <li>If a parameter is optional (no minCardinality restriction specified) but
 * references to it are used in the definition of service effects or
 * restrictions, then its default value must be specified using a uAAL-specific
 * property, namely <code>defaultValue</code>.
 * </ol>
 * 
 * @author mtazari
 * 
 */
public abstract class ProcessParameter extends Variable {
    public static final String OWLS_PROCESS_NAMESPACE = Service.OWLS_NAMESPACE_PREFIX
	    + "Process.owl#";
    public static final String MY_URI = OWLS_PROCESS_NAMESPACE + "Parameter";

    public static final String PROP_OWLS_PROCESS_PARAMETER_TYPE = OWLS_PROCESS_NAMESPACE
	    + "parameterType";
    public static final String PROP_OWLS_PROCESS_PARAMETER_VALUE = OWLS_PROCESS_NAMESPACE
	    + "parameterValue";
    public static final String PROP_PARAMETER_DEFAULT_VALUE = uAAL_SERVICE_NAMESPACE
	    + "defaultValue";
    public static final String PROP_PARAMETER_CARDINALITY = uAAL_SERVICE_NAMESPACE
	    + "parameterCardinality";
    public static final String PROP_PARAMETER_MAX_CARDINALITY = uAAL_SERVICE_NAMESPACE
	    + "parameterMaxCardinality";
    public static final String PROP_PARAMETER_MIN_CARDINALITY = uAAL_SERVICE_NAMESPACE
	    + "parameterMinCardinality";

    public static final String PROP_OWLS_VALUE_OF_THE_VAR = OWLS_PROCESS_NAMESPACE
	    + "theVar";
    public static final String PROP_OWLS_VALUE_FROM_PROCESS = OWLS_PROCESS_NAMESPACE
	    + "fromProcess";
    public static final String TYPE_OWLS_VALUE_OF = OWLS_PROCESS_NAMESPACE
	    + "ValueOf";

    static {
	register(ProcessParameter.class);
    }

    public static final boolean checkDeserialization(Object o) {
	if (isVarRef(o)) {
	    Object var = ((Resource) o).getProperty(PROP_OWLS_VALUE_OF_THE_VAR);
	    if (var == null)
		return false;
	    if (var.getClass() != Resource.class)
		return var instanceof ProcessParameter;
	    var = ProcessInput.MY_URI.equals(((Resource) var).getType()) ? (ProcessParameter) ProcessInput
		    .toInput((Resource) var)
		    : (ProcessParameter) ProcessOutput.toOutput((Resource) var);
	    return (var == null) ? false : ((Resource) o).changeProperty(
		    PROP_OWLS_VALUE_OF_THE_VAR, var);
	}
	return true;
    }
    
    /**
     * Return true iff the object is Variable Reference 
     * (a resource of OWL-S 
     *      http://www.daml.org/services/owl-s/1.1/Process.owl#ValueOf class)
     * @param o - the object to test
     * @return - true iff the object is a resource of OWL-S ValueOf class
     */
    public static final boolean isVarRef(Object o) {
	return o instanceof Resource
		&& TYPE_OWLS_VALUE_OF.equals(((Resource) o).getType());
    }
    
    /**
     * Return the variable from the variable reference, either by the
     * property http://www.daml.org/services/owl-s/1.1/Process.owl#theVar or 
     * from the context
     * 
     * @param o - the variable reference
     * @param context - the context
     * @return - the variable
     */
    public static final Object resolveVarRef(Object o, Hashtable context) {
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
    

    protected ProcessParameter(String uri, String subType) {
	super(uri);
	addType(subType, true);
    }

    /**
     * Create a variable reference (a resource of OWL-S 
     *      http://www.daml.org/services/owl-s/1.1/Process.owl#ValueOf class) 
     *      from this ProcessParameter
     * 
     * @return the Variable Reference resource
     */
    public Resource asVariableReference() {
	Resource result = new Resource();
	result.addType(TYPE_OWLS_VALUE_OF, true);
	result.setProperty(PROP_OWLS_VALUE_OF_THE_VAR, this);
	result.setProperty(PROP_OWLS_VALUE_FROM_PROCESS,
		ServiceCall.THIS_SERVICE_CALL);
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
	return ((Resource) props.get(PROP_OWLS_PROCESS_PARAMETER_TYPE))
		.getURI();
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
     * 
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
	return o instanceof Resource
		&& ((Resource) o).numberOfProperties() == 0
		&& ((Resource) o).serializesAsXMLLiteral()
		&& !((Resource) o).isAnon();
    }
    
    /**
     * Set cardinality of the process parameter
     * 
     * @param max - maximal cardinality
     * @param min - minimal cardinality
     */
    public final void setCardinality(int max, int min) {
	if (!props.containsKey(PROP_PARAMETER_CARDINALITY)
		&& !props.containsKey(PROP_PARAMETER_MAX_CARDINALITY)
		&& !props.containsKey(PROP_PARAMETER_MIN_CARDINALITY)) {
	    if (max > 0)
		if (max == min)
		    props.put(PROP_PARAMETER_CARDINALITY, new Integer(max));
		else
		    props.put(PROP_PARAMETER_MAX_CARDINALITY, new Integer(max));
	    if (min > 0 && min != max)
		props.put(PROP_PARAMETER_MIN_CARDINALITY, new Integer(min));
	}
    }

    /**
     * Set the default value of this parameter
     * 
     * @param value - the object representing the default value
     */
    public final void setDefaultValue(Object value) {
	if (props.containsKey(PROP_PARAMETER_DEFAULT_VALUE))
	    return;

	value = TypeMapper.asLiteral(value);
	if (value != null)
	    props.put(PROP_PARAMETER_DEFAULT_VALUE, value);
    }
    
    /**
     * Set the parameter type of this parameter
     * 
     * @param typeURI - the URI of the parameter type
     */
    public final void setParameterType(String typeURI) {
	if (StringUtils.isQualifiedName(typeURI)
		&& !props.containsKey(PROP_OWLS_PROCESS_PARAMETER_TYPE))
	    props.put(PROP_OWLS_PROCESS_PARAMETER_TYPE, new Resource(typeURI,
		    true));
    }
    
    /**
     * Set the value of this parameter
     * 
     * @param value - the object representing the value
     */
    public final void setParameterValue(Object value) {
	if (!props.containsKey(PROP_OWLS_PROCESS_PARAMETER_VALUE)) {
	    value = TypeMapper.asLiteral(value);
	    if (value != null)
		props.put(PROP_OWLS_PROCESS_PARAMETER_VALUE, value);
	}
    }
    
    /**
     * Set a value of a property for this process parameter 
     * 
     * @param prop - the property to set
     * @param val - the value to set for the property
     */
    public void setProperty(String prop, Object val) {
	if (prop == null || val == null || props.containsKey(prop))
	    return;

	if (prop.equals(PROP_PARAMETER_CARDINALITY)) {
	    if (val instanceof Integer) {
		int i = ((Integer) val).intValue();
		setCardinality(i, i);
	    }
	} else if (prop.equals(PROP_PARAMETER_DEFAULT_VALUE)) {
	    setDefaultValue(val);
	} else if (prop.equals(PROP_PARAMETER_MAX_CARDINALITY)) {
	    if (!props.containsKey(PROP_PARAMETER_CARDINALITY)
		    && val instanceof Integer)
		props.put(prop, val);
	} else if (prop.equals(PROP_PARAMETER_MIN_CARDINALITY)) {
	    if (!props.containsKey(PROP_PARAMETER_CARDINALITY)
		    && val instanceof Integer)
		props.put(prop, val);
	} else if (prop.equals(PROP_OWLS_PROCESS_PARAMETER_TYPE)) {
	    setParameterType(val.toString());
	} else if (prop.equals(PROP_OWLS_PROCESS_PARAMETER_VALUE)) {
	    setParameterValue(val);
	} else
	    super.setProperty(prop, val);
    }
}
