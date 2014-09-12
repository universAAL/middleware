/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.middleware.rdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link ScopedResource} is a Resource that may have been generated at
 * another AALSpace. Thus it can be annotated with the origin Scope (a.k.a
 * tenant ID, origin AALSpace Id); and different destination Scopes.
 * 
 * @author amedrano
 * @author Carsten Stockloew
 * 
 */
public class ScopedResource extends FinalizedResource {

	/**
	 * The property URI for holding the Destination Scopes.
	 */
	public static String PROP_DEST_SCOPE = uAAL_VOCABULARY_NAMESPACE
			+ "hasDestinationScopes";

	/**
	 * The property URI for holding the Origin Scope. This property is to be
	 * used directly by {@link Resource#getProperty(String)} and
	 * {@link Resource#setProperty(String, Object)}, only by the router
	 * artifacts.
	 * 
	 */
	public static String PROP_ORIG_SCOPE = uAAL_VOCABULARY_NAMESPACE
			+ "hasOriginScope";

	/**
	 * A special scope indicating the Resource may not be serialized to other
	 * AALSpaces.
	 */
	public static String ONLY_LOCAL_SCOPE = uAAL_VOCABULARY_NAMESPACE
			+ "localScope";

	/**
	 * A special scope indicating the Resource may be serialized to all other
	 * AALSpaces.
	 */
	public static String ALL_SCOPES = uAAL_VOCABULARY_NAMESPACE + "allScopes";

	/** {@inheritDoc} */
	public ScopedResource() {
		super();
	}

	/** {@inheritDoc} */
	public ScopedResource(boolean isXMLLiteral) {
		super(isXMLLiteral);
	}

	/** {@inheritDoc} */
	public ScopedResource(String uri) {
		super(uri);
	}

	/** {@inheritDoc} */
	public ScopedResource(String uri, boolean isXMLLiteral) {
		super(uri, isXMLLiteral);
	}

	/** {@inheritDoc} */
	public ScopedResource(String uriPrefix, int numProps) {
		super(uriPrefix, numProps);
	}

	/**
	 * Check if there is any scopes for this resource.
	 * 
	 * @return true if there is one or more scopes annotated for this resource.
	 */
	public boolean isScoped() {
		return props.contains(PROP_DEST_SCOPE);
	}

	/**
	 * List all scopes associated to this resource.
	 * 
	 * @return always a list, empty if there are no scopes.
	 */
	public List getScopes() {
		Object s = getProperty(PROP_DEST_SCOPE);
		if (s instanceof String) {
			List res = new ArrayList();
			res.add(s);
			return res;
		} else if (s instanceof List) {
			return (List) s;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * Add a new scope to this resource.
	 * 
	 * @param newScope
	 *            the new scope to be added
	 * @return whether the change was successful or not.
	 */
	public boolean addScope(String newScope) {
		if (newScope == null) {
			return false;
		}
		Object s = getProperty(PROP_DEST_SCOPE);
		if (s instanceof String) {
			List res = new ArrayList();
			res.add(s);
			res.add(newScope);
			props.put(PROP_DEST_SCOPE, res);
			return true;
		} else if (s instanceof List) {
			((List) s).add(newScope);
			props.put(PROP_DEST_SCOPE, s);
			return true;
		} else if (s == null) {
			props.put(PROP_DEST_SCOPE, newScope);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Remove all scope annotations from this resource.
	 * 
	 * @return iff the scopes have been cleared.
	 */
	public boolean clearScopes() {
		return changeProperty(PROP_DEST_SCOPE, null);
	}

	/**
	 * Copy the scope from another {@link ScopedResource}.
	 * 
	 * @param src
	 *            the source from which to copy the scope.
	 * @return whether the change was successful or not.
	 */
	public boolean setScope(ScopedResource src) {
		Object scope = src.getProperty(PROP_DEST_SCOPE);
		if (scope == null) {
			return clearScopes();
		} else {
			props.put(PROP_DEST_SCOPE, scope);
			return true;
		}
	}

	/**
	 * Get the Scope at which the Resource was created.
	 * 
	 * @return a ScopeID, null if origin was never set (i.e: local).
	 */
	public String getOriginScope() {
		return (String) getProperty(PROP_ORIG_SCOPE);
	}

	/**
	 * Check whether if this {@link ScopedResource} may be sent to a destination
	 * scope <br>
	 * 
	 * <ul>
	 *  <li>Deny if the origin is the destination candidate (this will cause message loops) 
	 *  <li>Deny if the destinations do contain {@link ScopedResource#ONLY_LOCAL_SCOPE} (tenant-aware application has decided this message is only local)
	 *  <li>Allow if the destinations are empty (non-tenant-aware application has generated the message)
	 *  <li>Deny if the destinations are not empty and do not contain the candidate destination
	 *  <li>Allow if the destinations are not empty and contain a {@link ScopedResource#ALL_SCOPES} scope or the candidate destination.
	 *  </ul>
	 * 
	 * @param destinationCandidateScope
	 *            The destination to be checked.
	 * @return true iff it may be sent according to origin / destination
	 *         restrictions.
	 */
	public boolean isSerializableTo(String destinationCandidateScope) {
		String orig = (String) getProperty(PROP_ORIG_SCOPE);
		if (orig == null || destinationCandidateScope == null) {
			return false;
		}
		List dest = getScopes();
		return !orig.equals(destinationCandidateScope)
				&& !dest.contains(ONLY_LOCAL_SCOPE)
				&& (dest.isEmpty()
						|| dest.contains(destinationCandidateScope) 
						|| dest.contains(ALL_SCOPES));
	}
}
