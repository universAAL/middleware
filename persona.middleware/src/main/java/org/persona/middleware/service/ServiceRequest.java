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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.persona.middleware.PResource;
import org.persona.middleware.service.process.OutputBinding;
import org.persona.middleware.service.process.ProcessOutput;
import org.persona.middleware.service.process.ProcessResult;
import org.persona.ontology.Service;

/**
 * @author mtazari
 *
 */
public class ServiceRequest extends PResource {
	
	public static final String MY_URI = PERSONA_VOCABULARY_NAMESPACE + "ServiceRequest";

	public static final String PROP_AGGREGATING_FILTER = 
		PERSONA_VOCABULARY_NAMESPACE + "aggregatingFilter";
	public static final String PROP_REQUESTED_SERVICE = 
		PERSONA_VOCABULARY_NAMESPACE + "requestedService";
	public static final String PROP_REQUIRED_PROCESS_RESULT = 
		PERSONA_VOCABULARY_NAMESPACE + "requiredResult";
	public static final String PROP_PERSONA_SERVICE_CALLER = 
		PERSONA_VOCABULARY_NAMESPACE + "theServiceCaller";
	
	/**
	 * Constructor for usage by de-serializers, as an ananymous node without a URI.
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
	 * The constructor to be used by {@link ServiceCaller}s. The given parameter will be used by the middleware
	 * for finding a matching service through extracting the following info set from it:<ul>
	 * <li>{@link Service#getClassURI()}, all services from this class and its sub-classes will match
	 * <li>{@link Service#getRestrictedPropsOnInstanceLevel()} will be used to fetch all the restrictions that were
	 *     added by the {@link ServiceCaller}. These restrictions will be checked against the services matched from the previous step
	 *     to narrow down the hit set.
	 * <li>Finally the required outputs and effects from this ServiceRequest will be checked against the
	 *     effects and outputs of the matched services. 
	 * </ul>
	 */
	public ServiceRequest(Service requestedService, PResource involvedHumanUser) {
		super();
		if (requestedService == null)
			throw new NullPointerException();
		addType(MY_URI, true);
		props.put(PROP_REQUESTED_SERVICE, requestedService);
		if (involvedHumanUser != null  &&  !involvedHumanUser.isAnon())
			props.put(PROP_PERSONA_INVOLVED_HUMAN_USER, involvedHumanUser);
	}
	
	public ServiceRequest(String uriPrefix, int numProps, Service requestedService, PResource involvedHumanUser) {
		super(uriPrefix, numProps);
		if (requestedService == null)
			throw new NullPointerException();
		addType(MY_URI, true);
		props.put(PROP_REQUESTED_SERVICE, requestedService);
		if (involvedHumanUser != null  &&  !involvedHumanUser.isAnon())
			props.put(PROP_PERSONA_INVOLVED_HUMAN_USER, involvedHumanUser);
	}

	/**
	 * See the comments above. This constructor is more appropriate for {@link AvailabilitySubscriber}s,
	 * because they will be notified later only with the URI.
	 */
	public ServiceRequest(String uri, Service requestedService, PResource involvedHumanUser) {
		super(uri);
		if (requestedService == null)
			throw new NullPointerException();
		addType(MY_URI, true);
		props.put(PROP_REQUESTED_SERVICE, requestedService);
		if (involvedHumanUser != null  &&  !involvedHumanUser.isAnon())
			props.put(PROP_PERSONA_INVOLVED_HUMAN_USER, involvedHumanUser);
	}
	
	/**
	 * Help function for the service bus to quickly decide if a coordination with other peers is necessary or not.
	 */
	public boolean acceptsRandomSelection() {
		List filters = (List) props.get(PROP_AGGREGATING_FILTER);
		if (filters == null)
			return false;

		for (Iterator i = filters.iterator();  i.hasNext(); )
			if (((AggregatingFilter) i.next()).getTheFunction() != AggregationFunction.oneOf)
				return false;

		return true;
	}
	
	/**
	 * Adds filtering functions such as max(aProp) to the request as criteria to be used by the service bus
	 * for match-making and service selection.
	 */
	public void addAggregatingFilter(AggregatingFilter f) {
		if (f != null  &&  f.isWellFormed())
			filters().add(f);
	}
	
	/**
	 * Adds the requirement that the requested service must have the effect of adding the given <code>value</code>
	 * to the property reachable by the given <code>ppath</code>. The property should normally be a
	 * multi-valued property.
	 */
	public void addAddEffect(PropertyPath ppath, Object value) {
		if (ppath != null  &&  value != null)
			theResult().addAddEffect(ppath, value);
	}
	
	/**
	 * Adds the requirement that the service must deliver an output with type restrictions bound to the given
	 * <code>toParam</code> and that the service bus then must select the result set that passes the given
	 * aggregating filter <code>f</code>.
	 */
	public void addAggregatingOutputBinding(ProcessOutput toParam, AggregatingFilter f) {
		if (toParam != null  &&  f != null)
			theResult().addAggregatingOutputBinding(toParam, f);
	}
	
	/**
	 * Adds the requirement that the requested service must have the effect of changing the value of the property
	 * reachable by the given <code>ppath</code> to the given <code>value</code>.
	 */
	public void addChangeEffect(PropertyPath ppath, Object value) {
		if (ppath != null  &&  value != null)
			theResult().addChangeEffect(ppath, value);
	}
	
	/**
	 * Adds the requirement that the requested service must have the effect of removing the value of the property
	 * reachable by the given <code>ppath</code>.
	 */
	public void addRemoveEffect(PropertyPath ppath) {
		if (ppath != null)
			theResult().addRemoveEffect(ppath);
	}
	
	/**
	 * Adds the requirement that the service must deliver an output with type restrictions bound to the given
	 * <code>toParam</code> and that this must reflect
	 * the value of a property reachable by the given property path <code>sourceProp</code>.
	 */
	public void addSimpleOutputBinding(ProcessOutput toParam, PropertyPath sourceProp) {
		if (toParam != null  &&  sourceProp != null)
			theResult().addSimpleOutputBinding(toParam, sourceProp);
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
	 * Returns the list of aggregating filters added previously by calls to {@link
	 * #addAggregatingFilter(AggregatingFilter)}. The service bus will be the main user of this method.
	 */
	public List getFilters() {
		return (List) props.get(PROP_AGGREGATING_FILTER);
	}
	
	/**
	 * Returns the requested service. The service bus will be the main user of this method.
	 */
	public Service getRequestedService() {
		return (Service) props.get(PROP_REQUESTED_SERVICE);
	}
	
	/**
	 * Returns the list of required process effects. The service bus will be the main user of this method.
	 */
	public PResource[] getRequiredEffects() {
		ProcessResult pr = (ProcessResult) props.get(PROP_REQUIRED_PROCESS_RESULT);
		List effects = (pr == null)? null : pr.getEffects();
		return (effects == null)?
				new PResource[0]
				: (PResource[]) effects.toArray(new PResource[effects.size()]);
	}
	
	/**
	 * Returns the list of required process outputs. The service bus will be the main user of this method.
	 */
	public PResource[]  getRequiredOutputs() {
		ProcessResult pr = (ProcessResult) props.get(PROP_REQUIRED_PROCESS_RESULT);
		List bindings = (pr == null)? null : pr.getBindings();
		return (bindings == null)?
				new PResource[0]
				: (PResource[]) bindings.toArray(new PResource[bindings.size()]);
	}
	
	/**
	 * Help function for the service bus to quickly decide which aggregations must be performed on outputs.
	 */
	public List getOutputAggregations() {
		PResource[] bindings = getRequiredOutputs();
		List result = new ArrayList(bindings.length);
		for (int i=0; i<bindings.length; i++) {
			Object o = bindings[i].getProperty(OutputBinding.PROP_OWLS_BINDING_VALUE_FUNCTION);
			if (o instanceof AggregatingFilter)
				result.add(o);
		}
		return result;
	}

	/**
	 * @see PResource#getPropSerializationType(String)
	 */
	public int getPropSerializationType(String propURI) {
		return PROP_PERSONA_INVOLVED_HUMAN_USER.equals(propURI)?
				PROP_SERIALIZATION_REDUCED : PROP_SERIALIZATION_FULL;
	}

	/**
	 * @see PResource#isWellFormed()
	 */
	public boolean isWellFormed() {
		return props.containsKey(PROP_REQUESTED_SERVICE);
	}

	/**
	 * Overrides {@link PResource#setProperty(String, Object)}. Main user of this method are
	 * the de-serializers.
	 */
	public void setProperty(String propURI, Object value) {
		if (propURI == null  ||  value == null  ||  props.containsKey(propURI))
			return;
		
		if (propURI.equals(PROP_AGGREGATING_FILTER)  &&  value instanceof List)
			for (Iterator i = ((List) value).iterator(); i.hasNext();) {
				Object o = i.next();
				if (!(o instanceof AggregatingFilter)  ||  !((AggregatingFilter) o).isWellFormed())
					return;
			}
		else if (propURI.equals(PROP_REQUIRED_PROCESS_RESULT))
			if (value instanceof ProcessResult) {
				if (!((ProcessResult) value).isWellFormed())
					return;
			} else if (value instanceof PResource) {
				value = ProcessResult.toResult((PResource) value);
				if (value == null)
					return;
			} else
				return;
		else if (propURI.equals(PROP_PERSONA_INVOLVED_HUMAN_USER)) {
			if (value instanceof String  &&  PResource.isQualifiedName((String) value))
				value = new PResource((String) value);
			else if (!(value instanceof PResource)  ||  ((PResource) value).isAnon())
				return;
		} else if (propURI.equals(PROP_PERSONA_SERVICE_CALLER)) {
			if (value instanceof String  &&  PResource.isQualifiedName((String) value))
				value = new PResource((String) value);
			else {
				if (!(value instanceof PResource)  ||  ((PResource) value).isAnon())
					return;
				if (((PResource) value).numberOfProperties() > 0)
					value = new PResource(((PResource) value).getURI());
			}
		} else if (!propURI.equals(PROP_REQUESTED_SERVICE)  || !( value instanceof Service))
			return;
		
		props.put(propURI, value);
	}
	
	private ProcessResult theResult() {
		ProcessResult pr = (ProcessResult) props.get(PROP_REQUIRED_PROCESS_RESULT);
		if (pr == null) {
			pr = new ProcessResult();
			props.put(PROP_REQUIRED_PROCESS_RESULT, pr);
		}
		return pr;
	}
}
