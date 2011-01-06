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

import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * 
 */
public class AggregationFunction extends Resource {
	public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
			+ "AggregationFunction";

	public static final int ONE_OF = 0;
	public static final int MIN_OF = 1;
	public static final int MAX_OF = 2;
	public static final int MIN_DISTANCE_TO_REF_LOC = 3;
	public static final int MAX_DISTANCE_TO_REF_LOC = 4;

	private static final String[] names = { "one_of", "min_of", "max_of",
		"min_distance_to_ref_loc", "max_distance_to_ref_loc" };

	public static final AggregationFunction oneOf = new AggregationFunction(
			ONE_OF);
	public static final AggregationFunction minOf = new AggregationFunction(
			MIN_OF);
	public static final AggregationFunction maxOf = new AggregationFunction(
			MAX_OF);
	public static final AggregationFunction minDistanceToRefLoc = new AggregationFunction(
			MIN_DISTANCE_TO_REF_LOC);
	public static final AggregationFunction maxDistanceToRefLoc = new AggregationFunction(
			MAX_DISTANCE_TO_REF_LOC);

	public static AggregationFunction getAggregationFunctionByOrder(
			int order) {
		switch (order) {
		case ONE_OF:
			return oneOf;
		case MIN_OF:
			return minOf;
		case MAX_OF:
			return maxOf;
		case MIN_DISTANCE_TO_REF_LOC:
			return minDistanceToRefLoc;
		case MAX_DISTANCE_TO_REF_LOC:
			return maxDistanceToRefLoc;
		default:
			return null;
		}
	}

	public static final AggregationFunction valueOf(String name) {
		if (name == null)
			return null;
		if (name.startsWith(uAAL_VOCABULARY_NAMESPACE))
			name = name.substring(uAAL_VOCABULARY_NAMESPACE.length());
		for (int i = ONE_OF; i <= MAX_DISTANCE_TO_REF_LOC; i++)
			if (names[i].equals(name))
				return getAggregationFunctionByOrder(i);
		return null;
	}

	private int order;

	// prevent the usage of the default constructor
	private AggregationFunction() {

	}

	private AggregationFunction(int order) {
		super(uAAL_VOCABULARY_NAMESPACE + names[order]);
		addType(MY_URI, true);
		this.order = order;
	}

	public int getPropSerializationType(String propURI) {
		return PROP_SERIALIZATION_OPTIONAL;
	}
	
	public boolean isWellFormed() {
		return true;
	}
    
    public String name() {
        return names[order];
    }

	public int getNumberOfParams() {
		return (order > MAX_OF)? 2 : 1;
	}
	
	public String getParameterType(int i) {
		if (i == 0)
			return PropertyPath.TYPE_PROPERTY_PATH;
		if (i == 1)
			return AbsLocation.MY_URI;
		return null;
	}
    
    public int ord() {
        return order;
    }

	public void setProperty(String propURI, Object o) {
		// do nothing
	}
}
