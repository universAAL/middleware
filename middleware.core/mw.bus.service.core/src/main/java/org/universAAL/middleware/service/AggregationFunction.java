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
package org.universAAL.middleware.service;

import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.PropertyPath;

/**
 * The AggregationFunction class includes the required functions in the SELECT
 * clause of a the SPARQL-like query.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class AggregationFunction extends FinalizedResource {
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

    /**
     * Returns the order value of the AggregationFunction. Depending on the
     * inserted parameter, it returns only one of the predefined values for the
     * number of services.
     * 
     * @param order
     */
    public static AggregationFunction getAggregationFunctionByOrder(int order) {
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

    /**
     * Returns the value of the AggregationFunction. It returns the predefined
     * names for the AggregationFunction of services.
     * 
     * @param name
     *            can get null or the uAAL_VOCABULARY_NAMESPACE value
     */
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

    /**
     * Constructor for usage by de-serializers.
     */
    // prevent the usage of the default constructor
    private AggregationFunction() {

    }

    /**
     * Creates an {@link AggregationFunction} object.
     * 
     * @param order
     *            defines the order of each service
     */
    private AggregationFunction(int order) {
	super(uAAL_VOCABULARY_NAMESPACE + names[order]);
	addType(MY_URI, true);
	this.order = order;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#getPropSerializationType(String
     *      propURI)
     * @param propURI
     *            the URI of the property
     */
    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_OPTIONAL;
    }

    /**
     * Returns true, if the state of the resource is valid, otherwise false.
     */
    public boolean isWellFormed() {
	return true;
    }

    /**
     * Returns the human-readable name of this {@link AggregationFunction}.
     */
    public String name() {
	return names[order];
    }

    /**
     * Returns the number of parameters that must be given when using this
     * instance of {@link AggregationFunction}.
     */
    public int getNumberOfParams() {
	return (order > MAX_OF) ? 2 : 1;
    }

    /**
     * Returns the type of the parameter that is needed for this
     * {@link AggregationFunction}.
     * 
     * @param i
     *            the index of the parameter.
     * @return URI of the type of the parameter. The first parameter is always a
     *         {@link PropertyPath}, the second parameter is always an
     *         {@link AbsLocation}.
     * @see org.universAAL.middleware.rdf.PropertyPath
     * @see org.universAAL.middleware.owl.supply.AbsLocation
     */
    public String getParameterType(int i) {
	if (i == 0)
	    return PropertyPath.TYPE_PROPERTY_PATH;
	if (i == 1)
	    return AbsLocation.MY_URI;
	return null;
    }

    /**
     * Returns the number of the order value(integer).
     */
    public int ord() {
	return order;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#setProperty(String propURI,
     *      Object value)
     */
    public boolean setProperty(String propURI, Object o) {
	// do nothing
	return false;
    }
}
