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
package org.universAAL.middleware.service.owls.profile;

import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * Implementation of
 * http://www.daml.org/services/owl-s/1.1/Profile.owl#ServiceParameter, usually
 * covering the non-functional parameters. The type hierarchy of such parameters
 * (see example classes defined in this package and example instances in
 * {@link ServiceProfile}) should reflect the semantic of the parameter. Two
 * standard properties are defined by OWL-S: <code>serviceParameterName</code>
 * (the name of the actual parameter) and <code>sParameter</code> (an owl:Thing
 * pointing to the value of the parameter), both mandatory. uAAL modifies this
 * class the following way:
 * <ul>
 * <li>ignoring the cardinality restriction on both profile:serviceParameterName
 * and profile:sParameter;
 * <li>adding a new owl:DatatypeProperty called 'persona:valueData' for
 * literal-valued parameters; and
 * <li>requiring that the subclasses of profie:ServiceProperty restrict one of
 * profile:sParameter or persona:valueData to have a cardinality of 0 and the
 * other one to have minCardinality 1.
 * </ul>
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public abstract class ProfileParameter extends FinalizedResource {
    public static final String MY_URI = ServiceProfile.OWLS_PROFILE_NAMESPACE
	    + "ServiceParameter";
    public static final String PROP_OWLS_PROFILE_SERVICE_PARAMETER_NAME = ServiceProfile.OWLS_PROFILE_NAMESPACE
	    + "serviceParameterName";
    public static final String PROP_OWLS_PROFILE_S_PARAMETER = ServiceProfile.OWLS_PROFILE_NAMESPACE
	    + "sParameter";
    public static final String PROP_uAAL_PARAMETER_VALUE_DATA = uAAL_VOCABULARY_NAMESPACE
	    + "valueData";

    protected ProfileParameter() {
	super();
    }

    protected ProfileParameter(String uri) {
	super(uri);
    }

    /**
     * Returns {@link Resource#PROP_SERIALIZATION_REDUCED} for the predefined
     * properties otherwise {@link Resource#PROP_SERIALIZATION_OPTIONAL}. As the
     * only non-literal property is sParameter, it is assumed that for such as
     * parameter value, e.g. a location, the basic properties would be enough
     * for the evaluation of such a non-functional parameter. The two other
     * properties will have always literal values and there would make no
     * difference to return {@link Resource#PROP_SERIALIZATION_REDUCED} or
     * {@link Resource#PROP_SERIALIZATION_FULL}.
     * 
     * @see Resource#getPropSerializationType(java.lang.String)
     */
    public int getPropSerializationType(String propURI) {
	if (PROP_OWLS_PROFILE_SERVICE_PARAMETER_NAME.equals(propURI)
		|| PROP_OWLS_PROFILE_S_PARAMETER.equals(propURI)
		|| PROP_uAAL_PARAMETER_VALUE_DATA.equals(propURI))
	    return PROP_SERIALIZATION_REDUCED;
	return PROP_SERIALIZATION_OPTIONAL;
    }

    /**
     * The method returns the name of the name of the property (profile service
     * parameter)
     * 
     * @return name of property name for parameter (String)
     */
    public String getName() {
	return (String) props.get(PROP_OWLS_PROFILE_SERVICE_PARAMETER_NAME);
    }

    /**
     * The method returns the value of the specified parameter.
     * 
     * @return parameter value
     */
    public Object getValue() {
	Object answer = props.get(PROP_OWLS_PROFILE_S_PARAMETER);
	return (answer == null) ? props.get(PROP_uAAL_PARAMETER_VALUE_DATA)
		: answer;
    }

    /**
     * @see Resource#isWellFormed()
     */
    public boolean isWellFormed() {
	return (props.get(PROP_OWLS_PROFILE_S_PARAMETER) != null || props
		.get(PROP_uAAL_PARAMETER_VALUE_DATA) != null);
    }

    /**
     * @see Resource#setProperty(String, Object)
     */
   public boolean setProperty(String propURI, Object value) {
	if (propURI != null && value != null && !props.containsKey(propURI))
	    if (propURI.equals(PROP_OWLS_PROFILE_SERVICE_PARAMETER_NAME)) {
		if (value instanceof String) {
		    props.put(propURI, value);
		    return true;
		}
	    } else if (propURI.equals(PROP_OWLS_PROFILE_S_PARAMETER)) {
		if (props.containsKey(PROP_uAAL_PARAMETER_VALUE_DATA))
		    return false;
		if (value instanceof Resource) {
		    props.put(propURI, value);
		    return true;
		} else if (value instanceof List && !((List) value).isEmpty()) {
		    for (Iterator i = ((List) value).iterator(); i.hasNext();)
			if (!(i.next() instanceof Resource))
			    return false;
		    props.put(propURI, value);
		    return true;
		}
	    } else if (propURI.equals(PROP_uAAL_PARAMETER_VALUE_DATA)) {
		if (props.containsKey(PROP_OWLS_PROFILE_S_PARAMETER))
		    return false;
		if (TypeMapper.getDatatypeURI(value) != null) {
		    props.put(propURI, value);
		    return true;
		} else if (value instanceof List && !((List) value).isEmpty()) {
		    for (Iterator i = ((List) value).iterator(); i.hasNext();)
			if (TypeMapper.getDatatypeURI(i.next()) == null)
			    return false;
		    props.put(propURI, value);
		    return true;
		}
	    }
	return false;
    }
}
