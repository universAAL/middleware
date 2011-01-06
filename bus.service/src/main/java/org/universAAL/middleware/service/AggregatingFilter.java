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
package org.universAAL.middleware.service;

import java.util.List;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class AggregatingFilter extends Resource {
	public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE + "AggregatingFilter";

	public static final String PROP_uAAL_AGGREGATION_FUNCTION = 
		uAAL_VOCABULARY_NAMESPACE + "theFunction";
	public static final String PROP_uAAL_AGGREGATION_PARAMS = 
		uAAL_VOCABULARY_NAMESPACE + "aggregationParameters";
	
	static {
		addResourceClass(MY_URI, AggregatingFilter.class);
	}
	
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
			props.put(PROP_uAAL_AGGREGATION_FUNCTION, func);
			props.put(PROP_uAAL_AGGREGATION_PARAMS, params);
		} else
			throw new IllegalArgumentException();
	}
	
	public List getFunctionParams() {
		return (List) props.get(PROP_uAAL_AGGREGATION_PARAMS);
	}
	
	public AggregationFunction getTheFunction() {
		return (AggregationFunction) props.get(PROP_uAAL_AGGREGATION_FUNCTION);
	}
	
	public boolean isWellFormed() {
		return props.containsKey(PROP_uAAL_AGGREGATION_FUNCTION)
			&& props.containsKey(PROP_uAAL_AGGREGATION_PARAMS);
	}
	
	public void setProperty(String propURI, Object value) {
		if (propURI == null  ||  value == null  ||  props.containsKey(propURI))
			return;
		AggregationFunction func = null;
		List params = null;
		if (propURI.equals(PROP_uAAL_AGGREGATION_FUNCTION)) {
			if (!(value instanceof AggregationFunction)) {
				if (value instanceof Resource  &&  ((Resource) value).numberOfProperties() == 0)
					value = AggregationFunction.valueOf(((Resource) value).getURI());
				else if (value instanceof String)
					value = AggregationFunction.valueOf((String) value);
				else
					return;
				if (value == null)
					return;
			}
			func = (AggregationFunction) value;
			params = getFunctionParams();
		} else if (propURI.equals(PROP_uAAL_AGGREGATION_PARAMS)
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
		result.props.put(PROP_uAAL_AGGREGATION_FUNCTION, getProperty(PROP_uAAL_AGGREGATION_FUNCTION));
		result.props.put(PROP_uAAL_AGGREGATION_PARAMS, getProperty(PROP_uAAL_AGGREGATION_PARAMS));
		return result;
	}
}
