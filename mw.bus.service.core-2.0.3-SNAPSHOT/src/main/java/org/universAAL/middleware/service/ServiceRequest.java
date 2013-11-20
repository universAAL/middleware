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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.model.matchable.Request;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.impl.ServiceMatcher;
import org.universAAL.middleware.service.impl.ServiceWrapper;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.process.OutputBinding;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.process.ProcessResult;

/**
 * A class that represents a service request resource, which is used by the
 * <code>ServiceCaller</code>-s when performing synchronous or asynchronous
 * requests.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public class ServiceRequest extends FinalizedResource implements Request {

    /**
     * A resource URI that specifies the resource as a service request.
     */
    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "ServiceRequest";

    /**
     * A property key for adding of the list of aggregating filters.
     */
    public static final String PROP_AGGREGATING_FILTER = uAAL_VOCABULARY_NAMESPACE
	    + "aggregatingFilter";

    /**
     * A property key for adding of the requested service.
     */
    public static final String PROP_REQUESTED_SERVICE = uAAL_VOCABULARY_NAMESPACE
	    + "requestedService";

    /**
     * A property key for adding of the process result.
     */
    public static final String PROP_REQUIRED_PROCESS_RESULT = uAAL_VOCABULARY_NAMESPACE
	    + "requiredResult";

    /**
     * A property key for adding of the related service caller.
     */
    public static final String PROP_uAAL_SERVICE_CALLER = uAAL_VOCABULARY_NAMESPACE
	    + "theServiceCaller";

    /**
     * Constructor for usage by de-serializers, as an anonymous node without a
     * URI.
     */
    public ServiceRequest() {
	super();
	addType(MY_URI, true);
    }

    /**
     * Constructor for usage by de-serializers, as a node with a URI.
     */
    public ServiceRequest(String uri) {
	super(uri);
	addType(MY_URI, true);
    }

    /**
     * The constructor to be used by {@link ServiceCaller}s. The given parameter
     * will be used by the middleware for finding a matching service through
     * extracting the following info set from it:
     * <ul>
     * <li>{@link Service#getClassURI()}, all services from this class and its
     * sub-classes will match
     * <li>{@link Service#getRestrictedPropsOnInstanceLevel()} will be used to
     * fetch all the restrictions that were added by the {@link ServiceCaller}.
     * These restrictions will be checked against the services matched from the
     * previous step to narrow down the result set.
     * <li>Finally the required outputs and effects from this ServiceRequest
     * will be checked against the effects and outputs of the matched services.
     * </ul>
     * 
     * @param requestedService
     *            the requested service. It is added as a property of the
     *            <code>ServiceRequest</code> with key
     *            <code>PROP_REQUESTED_SERVICE</code>.
     * @param involvedHumanUser
     *            the human user that is related to this service request. May be
     *            <code>null</code>. If not null or anonymous, then it is added
     *            as a property of the <code>ServiceRequest</code> with key
     *            <code>Resource.PROP_uAAL_INVOLVED_HUMAN_USER</code>.
     */
    public ServiceRequest(Service requestedService, Resource involvedHumanUser) {
	super();
	if (requestedService == null) {
	    throw new NullPointerException();
	}
	addType(MY_URI, true);
	props.put(PROP_REQUESTED_SERVICE, requestedService);
	if (involvedHumanUser != null && !involvedHumanUser.isAnon()) {
	    props.put(PROP_uAAL_INVOLVED_HUMAN_USER, involvedHumanUser);
	}
    }

    /**
     * Creates an instance of <code>ServiceRequest</code> with a URI that is
     * created by appending a unique ID to the given 'uriPrefix'. This
     * constructor has a pseudo parameter 'numProps' in order to make it
     * distinct from the other constructor that also takes a string. Later
     * versions of <code>ServiceRequest</code> may decide to make some use of
     * numProps in some way, however.
     * 
     * @param uriPrefix
     *            Prefix of the URI.
     * @param numProps
     *            Not used.
     * @param requestedService
     *            the requested service. It is added as a property of the
     *            <code>ServiceRequest</code> with key
     *            <code>PROP_REQUESTED_SERVICE</code>.
     * @param involvedHumanUser
     *            the human user that is related to this service request. May be
     *            <code>null</code>. If not null or anonymous, then it is added
     *            as a property of the <code>ServiceRequest</code> with key
     *            <code>Resource.PROP_uAAL_INVOLVED_HUMAN_USER</code>.
     */
    public ServiceRequest(String uriPrefix, int numProps,
	    Service requestedService, Resource involvedHumanUser) {
	super(uriPrefix, numProps);
	if (requestedService == null) {
	    throw new NullPointerException();
	}
	addType(MY_URI, true);
	props.put(PROP_REQUESTED_SERVICE, requestedService);
	if (involvedHumanUser != null && !involvedHumanUser.isAnon()) {
	    props.put(PROP_uAAL_INVOLVED_HUMAN_USER, involvedHumanUser);
	}
    }

    /**
     * Creates a service request with a specified URI. This constructor is more
     * appropriate for {@link AvailabilitySubscriber}s, because they will be
     * notified later only with the URI.
     * 
     * @param uri
     *            The URI of the <code>ServiceRequest</code>.
     * @param requestedService
     *            the requested service. It is added as a property of the
     *            <code>ServiceRequest</code> with key
     *            <code>PROP_REQUESTED_SERVICE</code>.
     * @param involvedHumanUser
     *            the human user that is related to this service request. May be
     *            <code>null</code>. If not null or anonymous, then it is added
     *            as a property of the <code>ServiceRequest</code> with key
     *            <code>Resource.PROP_uAAL_INVOLVED_HUMAN_USER</code>.
     */
    public ServiceRequest(String uri, Service requestedService,
	    Resource involvedHumanUser) {
	super(uri);
	if (requestedService == null) {
	    throw new NullPointerException();
	}
	addType(MY_URI, true);
	props.put(PROP_REQUESTED_SERVICE, requestedService);
	if (involvedHumanUser != null && !involvedHumanUser.isAnon()) {
	    props.put(PROP_uAAL_INVOLVED_HUMAN_USER, involvedHumanUser);
	}
    }

    /**
     * Help function for the service bus to quickly decide if a coordination
     * with other peers is necessary or not.
     */
    public boolean acceptsRandomSelection() {
	List filters = (List) props.get(PROP_AGGREGATING_FILTER);
	if (filters == null) {
	    return false;
	}

	for (Iterator i = filters.iterator(); i.hasNext();) {
	    if (((AggregatingFilter) i.next()).getTheFunction() != AggregationFunction.oneOf) {
		return false;
	    }
	}

	return true;
    }

    /**
     * Adds the requirement that the requested service must have the effect of
     * adding the given <code>value</code> to the property reachable by the
     * given <code>ppath</code>. The property should normally be a multi-valued
     * property.
     */
    public void addAddEffect(String[] ppath, Object value) {
	if (ppath != null && value != null) {
	    theResult()
		    .addAddEffect(new PropertyPath(null, true, ppath), value);
	}
    }

    /**
     * Adds filtering functions such as max(aProp) to the request as criteria to
     * be used by the service bus for match-making and service selection.
     * 
     * @see AggregatingFilterFactory
     */
    public void addAggregatingFilter(AggregatingFilter f) {
	if (f != null && f.isWellFormed()) {
	    filters().add(f);
	}
    }

    /**
     * Adds the requirement that the service must deliver an output with type
     * restrictions bound to the given <code>toParam</code> and that the service
     * bus then must select the result set that passes the given aggregating
     * filter <code>f</code>.
     */
    public void addAggregatingOutputBinding(ProcessOutput toParam,
	    AggregatingFilter f) {
	if (toParam != null && f != null) {
	    theResult().addAggregatingOutputBinding(toParam, f);
	}
    }

    /**
     * Adds the requirement that the requested service must have the effect of
     * changing the value of the property reachable by the given
     * <code>ppath</code> to the given <code>value</code>.
     */
    public void addChangeEffect(String[] ppath, Object value) {
	if (ppath != null && value != null) {
	    theResult().addChangeEffect(new PropertyPath(null, true, ppath),
		    value);
	}
    }

    /**
     * Adds the requirement that the requested service must have the effect of
     * removing the value of the property reachable by the given
     * <code>ppath</code>.
     */
    public void addRemoveEffect(String[] ppath) {
	if (ppath != null) {
	    theResult().addRemoveEffect(new PropertyPath(null, true, ppath));
	}
    }

    /**
     * Adds the requirement that the service must deliver an output with type
     * restrictions bound to the given <code>toParam</code> and that this must
     * reflect the value of a property reachable by the given property path
     * <code>sourceProp</code>.
     */
    public void addRequiredOutput(String paramURI, String[] fromProp) {
	if (paramURI != null && fromProp != null && fromProp.length > 0) {
	    theResult().addSimpleOutputBinding(new ProcessOutput(paramURI),
		    new PropertyPath(null, true, fromProp));
	}
    }

    /**
     * Adds the requirement that the service must deliver an output with type
     * restrictions bound to the given <code>toParam</code> and that this must
     * reflect the value of a property reachable by the given property path
     * <code>sourceProp</code>.
     */
    public void addSimpleOutputBinding(ProcessOutput toParam,
	    String[] sourceProp) {
	if (toParam != null && sourceProp != null) {
	    theResult().addSimpleOutputBinding(toParam,
		    new PropertyPath(null, true, sourceProp));
	}
    }

    /**
     * Restrict the scope of process results by selecting only those resources
     * whose property reachable by refPath is of type typeURI.
     */
    public void addTypeFilter(String[] refPath, String typeURI) {
	getRequestedService().addInstanceLevelRestriction(
		MergedRestriction.getAllValuesRestriction(
			refPath[refPath.length - 1], typeURI), refPath);
    }

    /**
     * Restrict the scope of process results by selecting only those resources
     * whose property reachable by refPath has a value equal to the given
     * hasValue.
     */
    public void addValueFilter(String[] refPath, Object hasValue) {
	getRequestedService().addInstanceLevelRestriction(
		MergedRestriction.getFixedValueRestriction(
			refPath[refPath.length - 1], hasValue), refPath);
    }

    private List filters() {
	List filters = (List) props.get(PROP_AGGREGATING_FILTER);
	if (filters == null) {
	    filters = new ArrayList(2);
	    props.put(PROP_AGGREGATING_FILTER, filters);
	}
	return filters;
    }

    /**
     * Returns the list of aggregating filters added previously by calls to
     * {@link #addAggregatingFilter(AggregatingFilter)}. The service bus will be
     * the main user of this method.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<AggregatingFilter> getFilters() {
	Object propAggregatingFilterList = props.get(PROP_AGGREGATING_FILTER);
	if (propAggregatingFilterList instanceof List<?>) {
	    List aggregatingFilterList = (List) propAggregatingFilterList;
	    if (aggregatingFilterList.isEmpty()) {
		return aggregatingFilterList;
	    } else if (aggregatingFilterList.get(0) instanceof AggregatingFilter) {
		return (List<AggregatingFilter>) propAggregatingFilterList;
	    }
	}
	return new LinkedList<AggregatingFilter>();
    }

    /**
     * Returns the requested service. The service bus will be the main user of
     * this method.
     */
    public Service getRequestedService() {
	return (Service) props.get(PROP_REQUESTED_SERVICE);
    }

    /**
     * Returns the list of required process effects. The service bus will be the
     * main user of this method.
     */
    public Resource[] getRequiredEffects() {
	ProcessResult pr = (ProcessResult) props
		.get(PROP_REQUIRED_PROCESS_RESULT);
	List effects = pr == null ? null : pr.getEffects();
	return effects == null ? new Resource[0] : (Resource[]) effects
		.toArray(new Resource[effects.size()]);
    }

    /**
     * Returns the list of required process outputs. The service bus will be the
     * main user of this method.
     */
    public Resource[] getRequiredOutputs() {
	ProcessResult pr = (ProcessResult) props
		.get(PROP_REQUIRED_PROCESS_RESULT);
	List bindings = pr == null ? null : pr.getBindings();
	return bindings == null ? new Resource[0] : (Resource[]) bindings
		.toArray(new Resource[bindings.size()]);
    }

    /**
     * Help function for the service bus to quickly decide which aggregations
     * must be performed on outputs.
     */
    public List getOutputAggregations() {
	Resource[] bindings = getRequiredOutputs();
	List result = new ArrayList(bindings.length);
	for (Resource binding : bindings) {
	    Object o = binding
		    .getProperty(OutputBinding.PROP_OWLS_BINDING_VALUE_FUNCTION);
	    if (o instanceof AggregatingFilter) {
		result.add(o);
	    }
	}
	return result;
    }

    /**
     * @see Resource#getPropSerializationType(String)
     */
    @Override
    public int getPropSerializationType(String propURI) {
	return PROP_uAAL_INVOLVED_HUMAN_USER.equals(propURI) ? PROP_SERIALIZATION_REDUCED
		: PROP_SERIALIZATION_FULL;
    }

    /**
     * @see Resource#isWellFormed()
     */
    @Override
    public boolean isWellFormed() {
	return props.containsKey(PROP_REQUESTED_SERVICE);
    }

    /**
     * Overrides {@link Resource#setProperty(String, Object)}. Main user of this
     * method are the de-serializers.
     */
    @Override
    public boolean setProperty(String propURI, Object value) {
	if (propURI == null || value == null || props.containsKey(propURI)) {
	    return false;
	}

	if (propURI.equals(PROP_AGGREGATING_FILTER) && value instanceof List) {
	    for (Iterator i = ((List) value).iterator(); i.hasNext();) {
		Object o = i.next();
		if (!(o instanceof AggregatingFilter)
			|| !((AggregatingFilter) o).isWellFormed()) {
		    return false;
		}
	    }
	} else if (propURI.equals(PROP_REQUIRED_PROCESS_RESULT)) {
	    if (value instanceof ProcessResult) {
		if (!((ProcessResult) value).isWellFormed()) {
		    return false;
		}
	    } else if (value instanceof Resource) {
		value = ProcessResult.toResult((Resource) value);
		if (value == null) {
		    return false;
		}
	    } else {
		return false;
	    }
	} else if (propURI.equals(PROP_uAAL_INVOLVED_HUMAN_USER)) {
	    if (value instanceof String
		    && Resource.isQualifiedName((String) value)) {
		value = new Resource((String) value);
	    } else if (!(value instanceof Resource)
		    || ((Resource) value).isAnon()) {
		return false;
	    }
	} else if (propURI.equals(PROP_uAAL_SERVICE_CALLER)) {
	    if (value instanceof String
		    && Resource.isQualifiedName((String) value)) {
		value = new Resource((String) value);
	    } else {
		if (!(value instanceof Resource) || ((Resource) value).isAnon()) {
		    return false;
		}
		if (((Resource) value).numberOfProperties() > 0) {
		    value = new Resource(((Resource) value).getURI());
		}
	    }
	} else if (!propURI.equals(PROP_REQUESTED_SERVICE)
		|| !(value instanceof Service)) {
	    return false;
	}

	props.put(propURI, value);
	return true;
    }

    private ProcessResult theResult() {
	ProcessResult pr = (ProcessResult) props
		.get(PROP_REQUIRED_PROCESS_RESULT);
	if (pr == null) {
	    pr = new ProcessResult();
	    props.put(PROP_REQUIRED_PROCESS_RESULT, pr);
	}
	return pr;
    }

    /**
     * @see Matchable#matches(Matchable)
     */
    public boolean matches(Matchable other) {
	// only ServiceRequests can match
	if (!(other instanceof ServiceRequest))
	    return false;
	ServiceWrapper superset = ServiceWrapper.create(this);
	ServiceWrapper subset = ServiceWrapper.create((ServiceRequest) other);

	boolean res = new ServiceMatcher().matches(superset, subset,
		new Hashtable(), null);

	// if (res == false) {
	// System.out.println(" -- matching error: trying to match "
	// + BusMessage.trySerializationAsContent(this)
	// + "\n -- with: "
	// + BusMessage.trySerializationAsContent(other));
	// new ServiceMatcher().matches(superset, subset, new Hashtable(),
	// null);
	// }
	return res;
    }
}
