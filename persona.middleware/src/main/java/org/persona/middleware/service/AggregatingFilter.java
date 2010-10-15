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
package org.persona.middleware.service;

import java.util.List;

import org.persona.middleware.PResource;
import org.persona.ontology.ManagedIndividual;

/**
 * @author mtazari
 *
 */
public class AggregatingFilter extends PResource {
	public static final String MY_URI = PERSONA_VOCABULARY_NAMESPACE + "AggregatingFilter";

	public static final String PROP_PESONA_AGGREGATION_FUNCTION = 
		PERSONA_VOCABULARY_NAMESPACE + "theFunction";
	public static final String PROP_PESONA_AGGREGATION_PARAMS = 
		PERSONA_VOCABULARY_NAMESPACE + "aggregationParameters";
	
	private static boolean checkIntegrity(AggregationFunction func, List params) {
		if (func.getNumberOfParams() != params.size())
			return false;
		for (int i=0; i<params.size(); i++) {
			Object o = params.get(i);
			if ((PropertyPath.TYPE_PROPERTY_PATH.equals(func.getParameterType(i))
					&& !(o instanceof PropertyPath))
					|| !ManagedIndividual.checkMembership(func.getParameterType(i), o))
				return false;
		}
		return true;
	}
	
	public AggregatingFilter() {
		super();
		addType(MY_URI, true);
	}
	
	public AggregatingFilter(boolean asLiteral) {
		super(asLiteral);
		addType(MY_URI, true);
	}
	
	public AggregatingFilter(AggregationFunction func, List params, boolean asLiteral) {
		super(asLiteral);
		addType(MY_URI, true);
		if (func != null  &&  params != null  &&  checkIntegrity(func, params)) {
			props.put(PROP_PESONA_AGGREGATION_FUNCTION, func);
			props.put(PROP_PESONA_AGGREGATION_PARAMS, params);
		} else
			throw new IllegalArgumentException();
	}
	
	public List getFunctionParams() {
		return (List) props.get(PROP_PESONA_AGGREGATION_PARAMS);
	}
	
	public AggregationFunction getTheFunction() {
		return (AggregationFunction) props.get(PROP_PESONA_AGGREGATION_FUNCTION);
	}
	
	public boolean isWellFormed() {
		return props.containsKey(PROP_PESONA_AGGREGATION_FUNCTION)
			&& props.containsKey(PROP_PESONA_AGGREGATION_PARAMS);
	}
	
	public void setProperty(String propURI, Object value) {
		if (propURI == null  ||  value == null  ||  props.containsKey(propURI))
			return;
		AggregationFunction func = null;
		List params = null;
		if (propURI.equals(PROP_PESONA_AGGREGATION_FUNCTION)) {
			if (!(value instanceof AggregationFunction)) {
				if (value instanceof PResource  &&  ((PResource) value).numberOfProperties() == 0)
					value = AggregationFunction.valueOf(((PResource) value).getURI());
				else if (value instanceof String)
					value = AggregationFunction.valueOf((String) value);
				else
					return;
				if (value == null)
					return;
			}
			func = (AggregationFunction) value;
			params = getFunctionParams();
		} else if (propURI.equals(PROP_PESONA_AGGREGATION_PARAMS)
				&& value instanceof List  &&  !((List) value).isEmpty()) {
			func = getTheFunction();
			params = (List) value;
		} else
			return;
		if (func == null  ||  params == null  ||  checkIntegrity(func, params))
			props.put(propURI, value);
	}
	
	public AggregatingFilter toLiteral() {
		if (serializesAsXMLLiteral())
			return this;
		
		AggregatingFilter result = new AggregatingFilter(true);
		result.props.put(PROP_PESONA_AGGREGATION_FUNCTION, getProperty(PROP_PESONA_AGGREGATION_FUNCTION));
		result.props.put(PROP_PESONA_AGGREGATION_PARAMS, getProperty(PROP_PESONA_AGGREGATION_PARAMS));
		return result;
	}
}
