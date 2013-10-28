/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.service.impl;

import java.util.Hashtable;
import java.util.Iterator;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.Intersection;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.PropertyRestriction;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.aapi.AapiServiceRequest;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.process.ProcessResult;

public class ServiceMatcher {

    public boolean matches(ServiceWrapper superset, ServiceWrapper subset,
	    Hashtable context, Long logID) {
	// match the service
	if (!matchService(superset, subset))
	    return false;

	if (!matchRestrictions(superset, subset, context, logID))
	    return false;

	Hashtable cloned = (Hashtable) context.clone();

	if (!matchEffects(superset, subset, cloned, logID))
	    return false;

	if (!matchOutputs(superset, subset, cloned, logID))
	    return false;

	// synchronize the context for the effect and output bindings check
	if (cloned.size() > context.size())
	    for (Iterator i = cloned.keySet().iterator(); i.hasNext();) {
		Object key = i.next();
		if (!context.containsKey(key))
		    context.put(key, cloned.get(key));
	    }

	processNonSemanticInput(subset, context);
	processServiceUri(superset, subset, context);

	return true;
    }

    private boolean matchService(ServiceWrapper superset, ServiceWrapper subset) {
	Service subsetService = subset.getService();
	if (subsetService == null)
	    return true;

	Service superService = superset.getService();
	if (superService == null)
	    return false;

	if (!Service.checkMembership(subsetService.getClassURI(), superService))
	    return false;
	/*
	 * By checking the membership of offer in requestedServiceClass two
	 * lines before, the compatibility of offer with the request at hand is
	 * guaranteed => we do not need to check class level restrictions.
	 */
	return true;
    }

    private boolean matchRestrictions(ServiceWrapper superset,
	    ServiceWrapper subset, Hashtable context, Long logID) {
	Service subsetService = subset.getService();
	Service superService = superset.getService();

	// for checking later if the concrete values provided by the requester
	// are really used as input
	// this captures cases where, e.g., both getCalenderEvents(user) and
	// getCalenderEvent(user, eventID)
	// have no effects and return objects of the same type
	// TODO: the better solution is to allow complex output bindings that
	// state how the outputs are
	// filtered by specifying a chain of restrictions (in addition to simple
	// output bindings that
	// only specify the corresponding property path)
	int expectedSize = context.size()
		+ subsetService.getNumberOfValueRestrictions();

	String[] restrProps = subsetService.getRestrictedPropsOnInstanceLevel();
	if (restrProps != null && restrProps.length > 0) {
	    for (int i = 0; i < restrProps.length; i++) {
		// request instance level restrictions
		TypeExpression reqRestr = subsetService
			.getInstanceLevelRestrictionOnProp(restrProps[i]);

		// offer instance level restrictions
		TypeExpression offInsRestr = superService
			.getInstanceLevelRestrictionOnProp(restrProps[i]);

		// offer class level restrictions
		TypeExpression offClsRestr = Service
			.getClassRestrictionsOnProperty(
				superService.getClassURI(), restrProps[i]);

		if (!(reqRestr instanceof MergedRestriction)) {
		    // makes no sense, because 'restrProps' must have instance
		    // level restrictions
		    continue;
		}

		if (offInsRestr == null)
		    if (offClsRestr == null) {
			// only in case of restrictions on the service profile,
			// we may still proceed
			if (!Service.PROP_OWLS_PRESENTS.equals(restrProps[i]))
			    return false;
			reqRestr = (TypeExpression) ((MergedRestriction) reqRestr)
				.getConstraint(MergedRestriction.allValuesFromID);
			if (reqRestr instanceof PropertyRestriction) {
			    restrProps[i] = ((PropertyRestriction) reqRestr)
				    .getOnProperty();
			    if (restrProps[i] == null)
				// strange!
				continue;
			    // some properties of service profiles are managed
			    // by the middleware
			    Object o = context.get(restrProps[i]);
			    if (o == null)
				// then, it relates to those properties set by
				// the provider
				o = superset.getProperty(restrProps[i]);
			    if (o == null || !reqRestr.hasMember(o, context))
				return false;
			} else if (reqRestr instanceof Intersection)
			    for (Iterator j = ((Intersection) reqRestr).types(); j
				    .hasNext();) {
				// the same as above, only this time in a loop
				// over all members of the intersection
				reqRestr = (TypeExpression) j.next();
				if (reqRestr instanceof PropertyRestriction) {
				    restrProps[i] = ((PropertyRestriction) reqRestr)
					    .getOnProperty();
				    if (restrProps[i] == null)
					// strange!
					continue;
				    Object o = context.get(restrProps[i]);
				    if (o == null)
					o = superset.getProperty(restrProps[i]);
				    if (o == null
					    || !reqRestr.hasMember(o, context))
					return false;
				}
			    }
			else
			    // strange!
			    continue;
			// we are done with this property
			continue;
		    } else {
			// offInsRestr == null && offClsRestr != null

			if (reqRestr.matches(offClsRestr, context))
			    // tag the context that the offer restrictions are a
			    // subtype of request restrictions
			    // because the other way around, it is not
			    // guaranteed that the service call will be
			    // successful
			    context.put(
				    ServiceStrategy.CONTEXT_SPECIALIZED_CLASS_MATCH,
				    Boolean.TRUE);
			else if (!offClsRestr.matches(reqRestr, context)) {
			    if (logID != null)
				LogUtils.logTrace(
					ServiceBusImpl.getModuleContext(),
					ServiceRealization.class,
					"matches",
					new Object[] {
						ServiceBus.LOG_MATCHING_MISMATCH,
						"no subset relationship for restricted property",
						"\nrestricted property: ",
						restrProps[i],
						ServiceBus.LOG_MATCHING_MISMATCH_CODE,
						Integer.valueOf(1020),
						ServiceBus.LOG_MATCHING_MISMATCH_DETAILS,
						" A property is restricted in the request.  The service offer has class level restrictions, but no instance level restrictions."
							+ " Neither the request is a subset of the offer nor the offer a subset of the request.",
						logID }, null);
			    return false;
			}
		    }
		else {
		    // offInsRestr != null, offClsRestr unknown

		    if (reqRestr.matches(offInsRestr, context))
			// tag the context that the offer restrictions are a
			// subtype of request restrictions
			// because the other way around, it is not guaranteed
			// that the service call will be successful
			context.put(
				ServiceStrategy.CONTEXT_SPECIALIZED_INSTANCE_MATCH,
				Boolean.TRUE);
		    else if (!offInsRestr.matches(reqRestr, context)) {
			if (logID != null)
			    LogUtils.logTrace(
				    ServiceBusImpl.getModuleContext(),
				    ServiceRealization.class,
				    "matches",
				    new Object[] {
					    ServiceBus.LOG_MATCHING_MISMATCH,
					    "no subset relationship for restricted property",
					    "\nrestricted property: ",
					    restrProps[i],
					    ServiceBus.LOG_MATCHING_MISMATCH_CODE,
					    Integer.valueOf(1021),
					    ServiceBus.LOG_MATCHING_MISMATCH_DETAILS,
					    " A property is restricted in the request. The service offer has instance level restrictions."
						    + " Neither the request is a subset of the offer nor the offer a subset of the request.",
					    logID }, null);
			return false;
		    }

		    if (offClsRestr != null)
			// offInsRestr != null && offClsRestr != null

			if (reqRestr.matches(offClsRestr, context))
			    // tag the context that the offer restrictions are a
			    // subtype of request restrictions
			    // because the other way around, it is not
			    // guaranteed that the service call will be
			    // successful
			    context.put(
				    ServiceStrategy.CONTEXT_SPECIALIZED_CLASS_MATCH,
				    Boolean.TRUE);
			else if (!offClsRestr.matches(reqRestr, context)) {
			    if (logID != null)
				LogUtils.logTrace(
					ServiceBusImpl.getModuleContext(),
					ServiceRealization.class,
					"matches",
					new Object[] {
						ServiceBus.LOG_MATCHING_MISMATCH,
						"no subset relationship for restricted property",
						"\nrestricted property: ",
						restrProps[i],
						ServiceBus.LOG_MATCHING_MISMATCH_CODE,
						Integer.valueOf(1022),
						ServiceBus.LOG_MATCHING_MISMATCH_DETAILS,
						" A property is restricted in the request.  The service offer has class level and instance level restrictions. The instance level restrictions have been checked already, but class level restriction do not match."
							+ " Neither the request is a subset of the offer nor the offer a subset of the request.",
						logID }, null);
			    return false;
			}
		}
	    }
	}

	if (context.size() < expectedSize)
	    return false;

	return true;
    }

    private boolean matchEffects(ServiceWrapper superset,
	    ServiceWrapper subset, Hashtable context, Long logID) {
	// check effects
	if (!ProcessResult.checkEffects(subset.getEffects(),
		superset.getEffects(), context, logID))
	    return false;

	return true;
    }

    private boolean matchOutputs(ServiceWrapper superset,
	    ServiceWrapper subset, Hashtable context, Long logID) {
	// check output bindings
	if (!ProcessResult.checkOutputBindings(subset.getOutputs(),
		superset.getOutputs(), context, subset.getService(), logID))
	    return false;

	return true;
    }

    private void processNonSemanticInput(ServiceWrapper subset,
	    Hashtable context) {
	// NON_SEMANTIC_INPUT:
	// if service matches then non-semantic input has to be copied to the
	// context
	Hashtable nonSemanticInput = null;
	try {
	    nonSemanticInput = subset.getNonSemanticInput();
	} catch (Exception ex) {
	    LogUtils.logDebug(
		    ServiceBusImpl.getModuleContext(),
		    ServiceRealization.class,
		    "matches",
		    new Object[] { "Exception occured when trying to get non-semantic parameters from AapiServiceRequest" },
		    ex);
	}
	if (nonSemanticInput != null) {
	    context.put(AapiServiceRequest.PROP_NON_SEMANTIC_INPUT,
		    nonSemanticInput);
	}
    }

    private void processServiceUri(ServiceWrapper superset,
	    ServiceWrapper subset, Hashtable context) {
	// uAAL_SERVICE_URI_MATCHED:
	// if URI of offered service matches exactly URI specified in
	// ServiceRequest then it is indicated in the context by means of
	// uAAL_SERVICE_URI_MATCHED property.
	String requestedServiceUri = subset.getService().getURI();
	String offeredURI = superset.getService().getURI();
	if (requestedServiceUri != null) {
	    if (requestedServiceUri.equals(offeredURI)) {
		context.put(ServiceRealization.uAAL_SERVICE_URI_MATCHED,
			Boolean.valueOf(true));
	    }
	}
    }
}
