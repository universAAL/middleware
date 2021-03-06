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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.model.matchable.Request;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ScopedResource;
import org.universAAL.middleware.service.impl.ServiceMatcher;
import org.universAAL.middleware.service.impl.ServiceWrapper;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.process.OutputBinding;
import org.universAAL.middleware.service.owls.process.ProcessEffect;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.process.ProcessResult;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.util.ResourceUtil;

/**
 * A class that represents a service request resource, which is used by the
 * <code>ServiceCaller</code>-s when performing synchronous or asynchronous
 * requests.
 *
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public class ServiceRequest extends ScopedResource implements Request {

	/**
	 * A resource URI that specifies the resource as a service request.
	 */
	public static final String MY_URI = VOCABULARY_NAMESPACE + "ServiceRequest";

	/**
	 * A property key for adding of the list of aggregating filters.
	 */
	public static final String PROP_AGGREGATING_FILTER = VOCABULARY_NAMESPACE + "aggregatingFilter";

	/**
	 * A property key for adding of the requested service.
	 */
	public static final String PROP_REQUESTED_SERVICE = VOCABULARY_NAMESPACE + "requestedService";

	/**
	 * A property key for adding of the process result.
	 */
	public static final String PROP_REQUIRED_PROCESS_RESULT = VOCABULARY_NAMESPACE + "requiredResult";

	/**
	 * A property key for adding of the related service caller.
	 */
	public static final String PROP_SERVICE_CALLER = VOCABULARY_NAMESPACE + "theServiceCaller";

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
	 *
	 * @param uri
	 *            the URI of this resource.
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
	 *            <code>Resource.PROP_INVOLVED_HUMAN_USER</code>.
	 */
	public ServiceRequest(Service requestedService, Resource involvedHumanUser) {
		super();
		if (requestedService == null) {
			throw new NullPointerException();
		}
		addType(MY_URI, true);
		props.put(PROP_REQUESTED_SERVICE, requestedService);
		if (involvedHumanUser != null && !involvedHumanUser.isAnon()) {
			props.put(PROP_INVOLVED_HUMAN_USER, involvedHumanUser);
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
	 *            <code>Resource.PROP_INVOLVED_HUMAN_USER</code>.
	 */
	public ServiceRequest(String uriPrefix, int numProps, Service requestedService, Resource involvedHumanUser) {
		super(uriPrefix, numProps);
		if (requestedService == null) {
			throw new NullPointerException();
		}
		addType(MY_URI, true);
		props.put(PROP_REQUESTED_SERVICE, requestedService);
		if (involvedHumanUser != null && !involvedHumanUser.isAnon()) {
			props.put(PROP_INVOLVED_HUMAN_USER, involvedHumanUser);
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
	 *            <code>Resource.PROP_INVOLVED_HUMAN_USER</code>.
	 */
	public ServiceRequest(String uri, Service requestedService, Resource involvedHumanUser) {
		super(uri);
		if (requestedService == null) {
			throw new NullPointerException();
		}
		addType(MY_URI, true);
		props.put(PROP_REQUESTED_SERVICE, requestedService);
		if (involvedHumanUser != null && !involvedHumanUser.isAnon()) {
			props.put(PROP_INVOLVED_HUMAN_USER, involvedHumanUser);
		}
	}

	/**
	 * Help function for the service bus to quickly decide if a coordination
	 * with other peers is necessary or not. It checks for the aggregating
	 * filters whether a filter with the function
	 * {@link AggregationFunction#oneOf} exists that determines that only one
	 * service is called even if there is more than one service that would match
	 * the request.
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
			theResult().addAddEffect(new PropertyPath(null, true, ppath), value);
		}
	}

	/**
	 * Adds filtering functions such as max(aProp) to the request as criteria to
	 * be used by the service bus for match-making and service selection.
	 *
	 * @param f
	 *            the filter to add.
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
	public void addAggregatingOutputBinding(ProcessOutput toParam, AggregatingFilter f) {
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
			theResult().addChangeEffect(new PropertyPath(null, true, ppath), value);
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
	 * Creates and output parameter with URI equal to <code>paramURI</code> and then
	 * calls {@link #addSimpleOutputBinding(ProcessOutput, String[])}. 
	 */
	public ProcessOutput addRequiredOutput(String paramURI, String[] fromProp) {
		if (paramURI != null && fromProp != null && fromProp.length > 0) {
			ProcessOutput po = new ProcessOutput(paramURI);
			theResult().addSimpleOutputBinding(po, new PropertyPath(null, true, fromProp));
			return po;
		}
		return null;
	}

	/**
	 * Adds the requirement that the service must deliver an output with type
	 * restrictions bound to the given <code>toParam</code> and that this must
	 * reflect the value of a property reachable by the given property path
	 * <code>sourceProp</code>.
	 */
	public void addSimpleOutputBinding(ProcessOutput toParam, String[] sourceProp) {
		if (toParam != null && sourceProp != null) {
			theResult().addSimpleOutputBinding(toParam, new PropertyPath(null, true, sourceProp));
		}
	}

	/**
	 * Restrict the scope of process results by selecting only those resources
	 * whose property reachable by refPath is of type typeURI.
	 */
	public void addTypeFilter(String[] refPath, String typeURI) {
		getRequestedService().addInstanceLevelRestriction(
				MergedRestriction.getAllValuesRestriction(refPath[refPath.length - 1], typeURI), refPath);
	}

	/**
	 * Restrict the scope of process results by selecting only those resources
	 * whose property reachable by refPath has a value equal to the given
	 * hasValue.
	 */
	public void addValueFilter(String[] refPath, Object hasValue) {
		getRequestedService().addInstanceLevelRestriction(
				MergedRestriction.getFixedValueRestriction(refPath[refPath.length - 1], hasValue), refPath);
	}

	/**
	 * Get a list of {@link AggregatingFilter}s. If there are no filters yet, a
	 * new list is created and added as property to this resource.
	 *
	 * @return the non-null list. Changes to this list are reflected in the
	 *         property value of this resource.
	 */
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
	 * {@link #addAggregatingFilter(AggregatingFilter)}.
	 *
	 * @return the non-null list of filters.
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
	 * Returns the requested service that was given to the constructor during
	 * instantiation.
	 *
	 * @return the requested service.
	 */
	public Service getRequestedService() {
		return (Service) props.get(PROP_REQUESTED_SERVICE);
	}

	/**
	 * Returns the list of required process effects.
	 *
	 * @return the list of required process effects
	 */
	public Resource[] getRequiredEffects() {
		ProcessResult pr = (ProcessResult) props.get(PROP_REQUIRED_PROCESS_RESULT);
		List effects = pr == null ? null : pr.getEffects();
		return effects == null ? new Resource[0] : (Resource[]) effects.toArray(new Resource[effects.size()]);
	}

	/**
	 * Returns the list of required process outputs.
	 *
	 * @return the list of required process outputs.
	 */
	public Resource[] getRequiredOutputs() {
		ProcessResult pr = (ProcessResult) props.get(PROP_REQUIRED_PROCESS_RESULT);
		List bindings = pr == null ? null : pr.getBindings();
		return bindings == null ? new Resource[0] : (Resource[]) bindings.toArray(new Resource[bindings.size()]);
	}

	/**
	 * Help function for the service bus to quickly decide which aggregations
	 * must be performed on outputs.
	 */
	public List<AggregatingFilter> getOutputAggregations() {
		Resource[] bindings = getRequiredOutputs();
		List<AggregatingFilter> result = new ArrayList<AggregatingFilter>(bindings.length);
		for (Resource binding : bindings) {
			Object o = binding.getProperty(OutputBinding.PROP_OWLS_BINDING_VALUE_FUNCTION);
			if (o instanceof AggregatingFilter) {
				result.add((AggregatingFilter) o);
			}
		}
		return result;
	}

	/**
	 * @see Resource#getPropSerializationType(String)
	 */
	@Override
	public int getPropSerializationType(String propURI) {
		return PROP_INVOLVED_HUMAN_USER.equals(propURI) ? PROP_SERIALIZATION_REDUCED : PROP_SERIALIZATION_FULL;
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
				if (!(o instanceof AggregatingFilter) || !((AggregatingFilter) o).isWellFormed()) {
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
		} else if (propURI.equals(PROP_INVOLVED_HUMAN_USER)) {
			if (value instanceof String && Resource.isQualifiedName((String) value)) {
				value = new Resource((String) value);
			} else if (!(value instanceof Resource) || ((Resource) value).isAnon()) {
				return false;
			}
		} else if (propURI.equals(PROP_SERVICE_CALLER)) {
			if (value instanceof String && Resource.isQualifiedName((String) value)) {
				value = new Resource((String) value);
			} else {
				if (!(value instanceof Resource) || ((Resource) value).isAnon()) {
					return false;
				}
				if (((Resource) value).numberOfProperties() > 0) {
					value = new Resource(((Resource) value).getURI());
				}
			}
		} else if (!propURI.equals(PROP_REQUESTED_SERVICE) || !(value instanceof Service)) {
			return false;
		}

		props.put(propURI, value);
		return true;
	}

	private ProcessResult theResult() {
		ProcessResult pr = (ProcessResult) props.get(PROP_REQUIRED_PROCESS_RESULT);
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
		ServiceWrapper subset = null;
		if (other instanceof ServiceProfile) {
			subset = ServiceWrapper.create((ServiceProfile) other);
		} else if (other instanceof ServiceRequest) {
			subset = ServiceWrapper.create((ServiceRequest) other);
		}
		if (subset == null)
			return false;

		ServiceWrapper superset = ServiceWrapper.create(this);

		return new ServiceMatcher().matches(superset, subset, new HashMap(), null);
	}
	
	private String getLastPathElement(Object pp) {
		if (pp instanceof PropertyPath)
			return ((PropertyPath) pp).getLastPathElement();
		else if (pp instanceof Resource)
			return PropertyPath.toPropertyPath((Resource) pp).getLastPathElement();
		return null;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("\n>>>>>>>>>>>>>>>>> service: ");
		Service s = getRequestedService();
		ResourceUtil.addResource2SB(s, sb);
		if (s != null) {
			Map<String, Object> conds = s.getFixedValueConditions();
			sb.append("\n>>>>>>>>>>>>>>>>> conditions (partial): ");
			for (String prop : conds.keySet()) {
				sb.append("\n    >>>>>>>>>>>>>>>>> ");
				ResourceUtil.addURI2SB(prop, sb);
				sb.append(" = ");
				ResourceUtil.addObject2SB(conds.get(prop), sb);
			}
		}
		
		Resource[] effects = getRequiredEffects();
		if (effects != null  &&  effects.length > 0) {
			sb.append("\n>>>>>>>>>>>>>>>>> effects: ");
			for (Resource r : effects) {
				String pp = getLastPathElement(r.getProperty(ProcessEffect.PROP_PROCESS_AFFECTED_PROPERTY));
				if (pp == null)
					continue;
				
				String type = r.getType();
				Object val = r.getProperty(ProcessEffect.PROP_PROCESS_PROPERTY_VALUE);
				if (ProcessEffect.TYPE_PROCESS_ADD_EFFECT.equals(type)) {
					sb.append("\n    >>>>>>>>>>>>>>>>> add: ");
					ResourceUtil.addObject2SB(val, sb);
					sb.append("    >>>>>>> to: ");
					ResourceUtil.addURI2SB(pp, sb);
				} else if (ProcessEffect.TYPE_PROCESS_CHANGE_EFFECT.equals(type)) {
					sb.append("\n    >>>>>>>>>>>>>>>>> set: ");
					ResourceUtil.addURI2SB(pp, sb);
					sb.append("    >>>>>>> equal to: ");
					ResourceUtil.addObject2SB(val, sb);
				} else if (ProcessEffect.TYPE_PROCESS_REMOVE_EFFECT.equals(type)) {
					sb.append("\n    >>>>>>>>>>>>>>>>> remove: ");
					ResourceUtil.addURI2SB(pp, sb);
				}
			}
		}
		
		Resource[] outputs = getRequiredOutputs();
		if (outputs != null  &&  outputs.length > 0) {
			sb.append("\n>>>>>>>>>>>>>>>>> fetch: ");
			for (Resource r : outputs) {
				String pp = getLastPathElement(r.getProperty(OutputBinding.PROP_OWLS_BINDING_VALUE_FORM));
				if (pp != null) {
					sb.append("\n    >>>>>>>>>>>>>>>>> ");
					ResourceUtil.addURI2SB(pp, sb);
				}
			}
		}
		
		sb.append("\n");
		return sb.toString();
	}
}
