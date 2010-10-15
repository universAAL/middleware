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
package org.persona.middleware.service.impl;

import java.util.Hashtable;
import java.util.Iterator;

import org.persona.middleware.MiddlewareConstants;
import org.persona.middleware.PResource;
import org.persona.middleware.service.ServiceCall;
import org.persona.middleware.service.ServiceRequest;
import org.persona.middleware.service.process.ProcessInput;
import org.persona.middleware.service.process.ProcessResult;
import org.persona.middleware.service.profile.ResponseTimeInMilliseconds;
import org.persona.middleware.service.profile.ServiceProfile;
import org.persona.middleware.util.IntAggregator;
import org.persona.middleware.util.RatingAggregator;
import org.persona.ontology.PClassExpression;
import org.persona.ontology.Rating;
import org.persona.ontology.Service;
import org.persona.ontology.expr.Intersection;
import org.persona.ontology.expr.Restriction;

/**
 * The profile of a realization of a service to be passed to the service bus
 * as registration parameter.
 * 
 * @author mtazari
 *
 */
public class ServiceRealization extends PResource {
	public static final String MY_URI = PERSONA_VOCABULARY_NAMESPACE + "ServiceRealization";

	public static final String PERSONA_SERVICE_RESPONSE_TIME = PERSONA_VOCABULARY_NAMESPACE + "responseTime";
	public static final String PERSONA_SERVICE_QUALITY_OF_SERVICE = PERSONA_VOCABULARY_NAMESPACE + "qos";
	public static final String PERSONA_SERVICE_PROVIDER = PERSONA_VOCABULARY_NAMESPACE + "theCallee";
	public static final String PERSONA_SERVICE_PROFILE = PERSONA_VOCABULARY_NAMESPACE + "theProfile";
	public static final String PERSONA_ASSERTED_SERVICE_CALL = PERSONA_VOCABULARY_NAMESPACE + "assertedServiceCall";
	
	public ServiceRealization() {
		super();
		addType(MY_URI, true);
	}
	
	public ServiceRealization(String theCallee, ServiceProfile theProfile) {
		super();
		addType(MY_URI, true);
		props.put(PERSONA_SERVICE_PROVIDER, theCallee);
		props.put(PERSONA_SERVICE_PROFILE, theProfile);
	}
	
	private void addAggregatedProperties(Hashtable context) {
		if (context == null)
			return;
		
		Rating r =getAvgQoSRating();
		if (r != null)
			context.put(ServiceProfile.PROP_PERSONA_AVERAGE_QOS_RATING, r);
		int t = getAvgResponseTime();
		if (t > -1)
			context.put(ServiceProfile.PROP_PERSONA_AVERAGE_RESPONSE_TIME, new Integer(t));
		r =getMaxQoSRating();
		if (r != null)
			context.put(ServiceProfile.PROP_PERSONA_MAX_QOS_RATING, r);
		t = getMaxResponseTime();
		if (t > -1)
			context.put(ServiceProfile.PROP_PERSONA_MAX_RESPONSE_TIME, new Integer(t));
		r =getMinQoSRating();
		if (r != null)
			context.put(ServiceProfile.PROP_PERSONA_MIN_QOS_RATING, r);
		t = getMinResponseTime();
		if (t > -1)
			context.put(ServiceProfile.PROP_PERSONA_MIN_RESPONSE_TIME, new Integer(t));
		t = getNumberOfQoSRatings();
		if (t > 0)
			context.put(ServiceProfile.PROP_PERSONA_NUMBER_OF_QOS_RATINGS, new Integer(t));
		t = getNumberOfResponseTimeMeasurements();
		if (t > 0)
			context.put(ServiceProfile.PROP_PERSONA_NUMBER_OF_RESPONSE_TIME_MEASUREMENTS, new Integer(t));
	}
	
	void addMeasuredResponseTime(int rt) {
		if (rt > 0) {
			IntAggregator ia = (IntAggregator) props.get(PERSONA_SERVICE_RESPONSE_TIME);
			if (ia == null) {
				ia = new IntAggregator();
				props.put(PERSONA_SERVICE_RESPONSE_TIME, ia);
			}
			ia.addVote(rt);
		}
	}
	
	void addQoSRating(Rating r) {
		if (r != null) {
			RatingAggregator ra = (RatingAggregator) props.get(PERSONA_SERVICE_QUALITY_OF_SERVICE);
			if (ra == null) {
				ra = new RatingAggregator();
				props.put(PERSONA_SERVICE_QUALITY_OF_SERVICE, ra);
			}
			ra.addRating(r);
		}
	}
	
	boolean assertServiceCall(Hashtable context) {
		ServiceProfile prof = (ServiceProfile) props.get(PERSONA_SERVICE_PROFILE);
		if (prof == null)
			return false;
		
		String processURI = prof.getProcessURI();
		if (processURI == null)
			return false;

		ServiceCall result = new ServiceCall(processURI);
		Object user = context.get(MiddlewareConstants.VAR_PERSONA_ACCESSING_HUMAN_USER);
		if (user instanceof PResource)
			result.setInvolvedUser((PResource) user);
		
		for (Iterator i=prof.getInputs(); i.hasNext();) {
			ProcessInput in = (ProcessInput) i.next();
			String uri = null;
			if (in != null)
				uri = in.getURI();
			if (uri == null)
				continue;
			
			Object o = context.get(uri);
			if (o != null)
				result.addInput(uri, o);
			else if (in.getMinCardinality() > 0)
				return false;
		}
		context.put(PERSONA_ASSERTED_SERVICE_CALL, result);
		return true;
	}
	
	public Rating getAvgQoSRating() {
		RatingAggregator ra = (RatingAggregator) props.get(PERSONA_SERVICE_RESPONSE_TIME);
		return (ra == null)?  null : ra.getAverage();
	}
	
	public int getAvgResponseTime() {
		IntAggregator ia = (IntAggregator) props.get(PERSONA_SERVICE_RESPONSE_TIME);
		return (ia == null)?  -1 : ia.getAverage();
	}
	
	public Rating getMaxQoSRating() {
		RatingAggregator ra = (RatingAggregator) props.get(PERSONA_SERVICE_RESPONSE_TIME);
		return (ra == null)?  null : ra.getMax();
	}
	
	public int getMaxResponseTime() {
		IntAggregator ia = (IntAggregator) props.get(PERSONA_SERVICE_RESPONSE_TIME);
		return (ia == null)?  -1 : ia.getMax();
	}
	
	public Rating getMinQoSRating() {
		RatingAggregator ra = (RatingAggregator) props.get(PERSONA_SERVICE_RESPONSE_TIME);
		return (ra == null)?  null : ra.getMin();
	}
	
	public int getMinResponseTime() {
		IntAggregator ia = (IntAggregator) props.get(PERSONA_SERVICE_RESPONSE_TIME);
		return (ia == null)?  -1 : ia.getMin();
	}
	
	public int getNumberOfQoSRatings() {
		RatingAggregator ra = (RatingAggregator) props.get(PERSONA_SERVICE_RESPONSE_TIME);
		return (ra == null)?  0 : ra.getNumberOfRatings();
	}
	
	public int getNumberOfResponseTimeMeasurements() {
		IntAggregator ia = (IntAggregator) props.get(PERSONA_SERVICE_RESPONSE_TIME);
		return (ia == null)?  0 : ia.getNumberOfVotes();
	}
	
	Object getProvider() {
		return props.get(PERSONA_SERVICE_PROVIDER);
	}
	
	public int getResponseTimeout() {
		ServiceProfile prof = (ServiceProfile) props.get(PERSONA_SERVICE_PROFILE);
		if (prof != null) {
			ResponseTimeInMilliseconds timeout = (ResponseTimeInMilliseconds)
					prof.getProperty(ServiceProfile.PROP_PERSONA_RESPONSE_TIMEOUT);
			if (timeout != null)
				return timeout.getNumberOfMilliseconds();
		}
		return -1;
	}
	
	public boolean matches(ServiceRequest request, Hashtable context) {
		if (request == null)
			return true;

		ServiceProfile prof = (ServiceProfile) props.get(PERSONA_SERVICE_PROFILE);
		if (prof == null)
			return false;
		
		Service requestedService = request.getRequestedService();
		if (requestedService == null)
			return true;
		
		Service offer = prof.getTheService();
		if (offer == null
				||  !Service.checkMembership(requestedService.getClassURI(), offer))
			return false;
		/*
		 * By checking the membership of offer in requestedServiceClass two lines before,
		 * the compatibility of offer with the request at hand is guaranteed => we do not
		 * need to check class level restrictions.
		 * 
		String[] props = requestedService.getRestrictedPropsOnClassLevel();
		if (props != null  &&  props.length > 0) {
			for (int i=0; i<props.length; i++) {
				Restriction r = Service.getClassRestrictionsOnProperty(requestedServiceClass, props[i]);
				if (r != null) {
					
				}
			}
		} 
		 */

		addAggregatedProperties(context);
		// for checking later if the concrete values provided by the requester are really used as input
		// this captures cases where, e.g., both getCalenderEvents(user) and getCalenderEvent(user, eventID)
		// have no effects and return objects of the same type
		// TODO: the better solution is to allow complex output bindings that state how the outputs are
		//       filtered by specifying a chain of restrictions (in addition to simple output bindings that
		//       only specify the corresponding property path)
		int expectedSize = context.size() + requestedService.getNumberOfValueRestrictions();
		
		String[] restrProps = requestedService.getRestrictedPropsOnInstanceLevel();
		if (restrProps != null  &&  restrProps.length > 0) {
			for (int i=0; i<restrProps.length; i++) {
				PClassExpression reqRestr = requestedService.getInstanceLevelRestrictionOnProp(restrProps[i]),
						offInsRestr = offer.getInstanceLevelRestrictionOnProp(restrProps[i]),
						offClsRestr = Service.getClassRestrictionsOnProperty(offer.getClassURI(), restrProps[i]);
				if (!(reqRestr instanceof Restriction)) {
					// makes no sense, because 'restrProps' must have instance level restrictions
					continue;
				}
				
				if (offInsRestr == null)
					if (offClsRestr == null) {
						// only in case of restrictions on the service profile, we may still proceed
						if (!Service.PROP_OWLS_PRESENTS.equals(restrProps[i]))
							return false;
						reqRestr = (PClassExpression) ((Restriction) reqRestr).getProperty(
								Restriction.PROP_OWL_ALL_VALUES_FROM);
						if (reqRestr instanceof Restriction) {
							restrProps[i] = ((Restriction) reqRestr).getOnProperty();
							if (restrProps[i] == null)
								// strange!
								continue;
							// some properties of service profiles are managed by the middleware
							Object o = context.get(restrProps[i]);
							if (o == null)
								// then, it relates to those properties set by the provider
								o = prof.getProperty(restrProps[i]);
							if (o == null  ||  !reqRestr.hasMember(o, context))
								return false;
						} else if (reqRestr instanceof Intersection)
							for (Iterator j=((Intersection) reqRestr).types(); j.hasNext();) {
								// the same as above, only this time in a loop over all members of the intersection
								reqRestr = (PClassExpression) j.next();
								if (reqRestr instanceof Restriction) {
									restrProps[i] = ((Restriction) reqRestr).getOnProperty();
									if (restrProps[i] == null)
										// strange!
										continue;
									Object o = context.get(restrProps[i]);
									if (o == null)
										o = prof.getProperty(restrProps[i]);
									if (o == null  ||  !reqRestr.hasMember(o, context))
										return false;
								}
							}
						else
							// strange!
							continue;
						// we are done with this property
						continue;
					} else {
						if (reqRestr.matches(offClsRestr, context))
							// tag the context that the offer restrictions are a subtype of request restrictions
							// because the other way around, it is not guaranteed that the service call will be successful
							context.put(ServiceStrategy.CONTEXT_SPECIALIZED_CLASS_MATCH, Boolean.TRUE);
						else if (!offClsRestr.matches(reqRestr, context))
							return false;
					}
				else {
					if (reqRestr.matches(offInsRestr, context))
						// tag the context that the offer restrictions are a subtype of request restrictions
						// because the other way around, it is not guaranteed that the service call will be successful
						context.put(ServiceStrategy.CONTEXT_SPECIALIZED_INSTANCE_MATCH, Boolean.TRUE);
					else if (!offInsRestr.matches(reqRestr, context))
						return false;
					
					if (offClsRestr != null)
						if (reqRestr.matches(offClsRestr, context))
							// tag the context that the offer restrictions are a subtype of request restrictions
							// because the other way around, it is not guaranteed that the service call will be successful
							context.put(ServiceStrategy.CONTEXT_SPECIALIZED_CLASS_MATCH, Boolean.TRUE);
						else if (!offClsRestr.matches(reqRestr, context))
							return false;
				}
			}
		}
		
		if (context.size() < expectedSize)
			return false;
		
		Hashtable cloned = (Hashtable) context.clone();
		if (!ProcessResult.checkEffects(request.getRequiredEffects(), prof.getEffects(), cloned)
				|| !ProcessResult.checkOutputBindings(request.getRequiredOutputs(), prof.getOutputBindings(), cloned))
			return false;
		
		if (cloned.size() > context.size())
			for (Iterator i = cloned.keySet().iterator();  i.hasNext();) {
				Object key = i.next();
				if (!context.containsKey(key))
					context.put(key, cloned.get(key));
			}
		
		return true;
	}
	
	public boolean matches(String word) {
		if (word == null)
			return true;

		ServiceProfile prof = (ServiceProfile) props.get(PERSONA_SERVICE_PROFILE);
		return prof != null
			&& matchStrings(word, prof.getServiceName(), prof.getServiceDescription());
	}
	
	public boolean matchesAll(String[] keywords) {
		if (keywords != null) {
			ServiceProfile prof = (ServiceProfile) props.get(PERSONA_SERVICE_PROFILE);
			if (prof == null)
				return false;
			String name = prof.getServiceName(), text = prof.getServiceDescription();
			for (int i=0; i<keywords.length; i++)
				if (keywords[i] != null  &&  !matchStrings(keywords[i], name, text))
					return false;
		}
		return true;
	}
	
	public boolean matchesOne(String[] keywords) {
		if (keywords != null) {
			ServiceProfile prof = (ServiceProfile) props.get(PERSONA_SERVICE_PROFILE);
			if (prof == null)
				return false;
			String name = prof.getServiceName(), text = prof.getServiceDescription();
			for (int i=0; i<keywords.length; i++)
				if (keywords[i] != null  &&  matchStrings(keywords[i], name, text))
					return true;
		}
		return false;
	}
	
	private boolean matchStrings(String searched, String name, String text) {
		if (searched == null  ||  "".equals(searched))
			return true;
		return (name != null  &&  name.toLowerCase().indexOf(searched.toLowerCase()) > -1)
			|| (text != null  &&  text.toLowerCase().indexOf(searched.toLowerCase()) > -1);
	}

	public int getPropSerializationType(String propURI) {
		return (PERSONA_SERVICE_PROFILE.equals(propURI)
				||  PERSONA_SERVICE_PROVIDER.equals(propURI))?
						PROP_SERIALIZATION_FULL : PROP_SERIALIZATION_OPTIONAL;
	}

	public boolean isWellFormed() {
		return props.containsKey(PERSONA_SERVICE_PROFILE)
			&& props.containsKey(PERSONA_SERVICE_PROVIDER);
	}

	public void setProperty(String propURI, Object value) {
		if (propURI == null  ||  value == null  ||  props.containsKey(propURI))
			return;
		if ((propURI.equals(PERSONA_SERVICE_PROFILE)
				&&  value instanceof ServiceProfile
				&&  ((ServiceProfile) value).isWellFormed())
				|| (propURI.equals(PERSONA_SERVICE_PROVIDER)  &&  value instanceof String))
			props.put(propURI, value);
	}
}
