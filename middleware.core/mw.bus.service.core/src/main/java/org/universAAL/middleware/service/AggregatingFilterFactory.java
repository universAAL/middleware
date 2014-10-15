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

import java.util.ArrayList;

import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * A Factory to create different kinds of {@link AggregatingFilter}. These
 * filters can be given to a {@link ServiceRequest} to influence the selection
 * of services during matchmaking or their output when processing the result
 * from services after they have been called and before the result is given to
 * the {@link ServiceCaller}. {@link AggregatingFilter}s are only evaluated if
 * there is more than one service that match a {@link ServiceRequest}.
 * 
 * There are basically two types of filter:
 * <ul>
 * <li>
 * Service Selection:
 * <p>
 * The service selection filters restrict the number of service calls to calling
 * only one service even if more than one service would match.
 * </p>
 * <p>
 * If there is only one filter with the filtering function
 * {@link AggregationFunction#oneOf}, then one of the available services is
 * selected randomly. If a filter with one of the other aggregation functions is
 * set, then the service with a matching non-functional parameter is called (see
 * description of the appropriate methods).
 * </p>
 * <p>
 * Service selection filters are given to a service request via
 * {@link ServiceRequest#addAggregatingFilter(AggregatingFilter)}. Creation is
 * done with one of the methods {@link #createServiceSelectionFilter()},
 * {@link #createServiceSelectionFilter(MinMax, String)}, or
 * {@link #createServiceSelectionFilter(MinMax, String, AbsLocation)}.
 * </p>
 * </li>
 * <li>
 * Output Aggregation:
 * <p>
 * The output aggregation filters aggregate the output coming from several
 * service calls.
 * </p>
 * <p>
 * These filters can be used, for example, when your request requires two
 * outputs: a location and the brightness in that location, but you don't want
 * to receive a whole list of all available data but just the one location with
 * the maximum brightness.
 * </p>
 * <p>
 * Output aggregation filters are given to a service request via
 * {@link ServiceRequest#addAggregatingOutputBinding(org.universAAL.middleware.service.owls.process.ProcessOutput, AggregatingFilter)}
 * . Creation is done with one of the methods
 * {@link #createOutputAggregationFilter(MinMax)} or
 * {@link #createOutputAggregationFilter(MinMax, AbsLocation)}.
 * </p>
 * </li>
 * </ul>
 * 
 * @author Carsten Stockloew
 */
public class AggregatingFilterFactory {
    /**
     * Definition of the type of the aggregation functions: either a minimum
     * value or a maximum value.
     */
    public enum MinMax {
	min, max
    };

    public final static String PROP_DUMMY = Resource.uAAL_NAMESPACE_PREFIX
	    + "dummy.owl#dummyPropURI";

    /**
     * Create a service selection filter that selects one service randomly. The
     * aggregation function is {@link AggregationFunction#oneOf}. This filter is
     * ignored if another filter with a different function is added to the
     * service request.
     * 
     * @return a filter with a {@link AggregationFunction#oneOf} function.
     */
    public static AggregatingFilter createServiceSelectionFilter() {
	ArrayList params = new ArrayList(1);
	params.add(new PropertyPath(null, false, new String[] {
		Service.PROP_OWLS_PRESENTS, PROP_DUMMY }));

	return new AggregatingFilter(AggregationFunction.oneOf, params, true);
    }

    /**
     * Creates a service selection filter that evaluates a non-functional
     * parameter that is not related to a location.
     * 
     * @param type
     *            determines whether either the service with the minimum value
     *            or the maximum value of the parameter is called.
     * @param nonFunctionalParamPropURI
     *            the URI of the non-functional parameter. Typical values are:
     *            {@link ServiceProfile#PROP_uAAL_MIN_QOS_RATING},
     *            {@link ServiceProfile#PROP_uAAL_AVERAGE_QOS_RATING},
     *            {@link ServiceProfile#PROP_uAAL_MAX_QOS_RATING},
     *            {@link ServiceProfile#PROP_uAAL_MIN_RESPONSE_TIME},
     *            {@link ServiceProfile#PROP_uAAL_AVERAGE_RESPONSE_TIME},
     *            {@link ServiceProfile#PROP_uAAL_MAX_RESPONSE_TIME},
     *            {@link ServiceProfile#PROP_uAAL_NUMBER_OF_QOS_RATINGS},
     *            {@link ServiceProfile#PROP_uAAL_NUMBER_OF_RESPONSE_TIME_MEASUREMENTS}
     *            , {@link ServiceProfile#PROP_uAAL_RESPONSE_TIMEOUT}.
     * @return a new aggregating filter.
     */
    public static AggregatingFilter createServiceSelectionFilter(MinMax type,
	    String nonFunctionalParamPropURI) {
	AggregationFunction func = type == MinMax.min ? AggregationFunction.minOf
		: AggregationFunction.maxOf;

	ArrayList params = new ArrayList(1);
	params.add(new PropertyPath(null, false, new String[] {
		Service.PROP_OWLS_PRESENTS, nonFunctionalParamPropURI }));

	return new AggregatingFilter(func, params, true);
    }

    /**
     * Creates a service selection filter that evaluates a non-functional
     * parameter that is related to a location.
     * 
     * @param type
     *            determines whether either the service with the minimum value
     *            or the maximum value of the parameter is called.
     * @param nonFunctionalParamPropURI
     *            {@link ServiceProfile#PROP_uAAL_HOST_LOCATION},
     *            {@link ServiceProfile#PROP_uAAL_SPATIAL_COVERAGE}.
     * @param location
     *            the location that is referenced by the parameter.
     * @return a new aggregating filter.
     */
    public static AggregatingFilter createServiceSelectionFilter(MinMax type,
	    String nonFunctionalParamPropURI, AbsLocation location) {
	AggregationFunction func = type == MinMax.min ? AggregationFunction.minDistanceToRefLoc
		: AggregationFunction.maxDistanceToRefLoc;

	ArrayList params = new ArrayList(2);
	params.add(new PropertyPath(null, false, new String[] {
		Service.PROP_OWLS_PRESENTS, nonFunctionalParamPropURI }));
	params.add(location);

	return new AggregatingFilter(func, params, true);
    }

    /**
     * Creates an output aggregation filter.
     * 
     * @param type
     *            determines whether the output with the minimum value or the
     *            maximum value is used.
     * @return a new aggregating filter.
     */
    public static AggregatingFilter createOutputAggregationFilter(MinMax type) {
	AggregationFunction func = type == MinMax.min ? AggregationFunction.minOf
		: AggregationFunction.maxOf;

	ArrayList params = new ArrayList(1);
	params.add(new PropertyPath(null, false, new String[] {
		Service.PROP_OWLS_PRESENTS, PROP_DUMMY }));

	return new AggregatingFilter(func, params, true);
    }

    /**
     * Creates an output aggregation filter.
     * 
     * @param type
     *            determines whether the output with the minimum value or the
     *            maximum value is used.
     * @param location
     *            the location that is referenced by the parameter.
     * @return a new aggregating filter.
     */
    public static AggregatingFilter createOutputAggregationFilter(MinMax type,
	    AbsLocation location) {
	AggregationFunction func = type == MinMax.min ? AggregationFunction.minDistanceToRefLoc
		: AggregationFunction.maxDistanceToRefLoc;

	ArrayList params = new ArrayList(1);
	params.add(new PropertyPath(null, false, new String[] {
		Service.PROP_OWLS_PRESENTS, PROP_DUMMY }));
	params.add(location);

	return new AggregatingFilter(func, params, true);
    }
}
