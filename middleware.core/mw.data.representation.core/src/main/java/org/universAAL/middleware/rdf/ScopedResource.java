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
 * A {@link ScopedResource} is a Resource that may have been generated at, or be
 * sent to, another Space. Thus it can be annotated with one or more Scopes
 * (a.k.a tenant ID, origin Space ID).
 *
 * @author amedrano
 * @author Carsten Stockloew
 *
 */
public class ScopedResource extends FinalizedResource {

	/**
	 * The property URI for holding the Destination Scopes.
	 */
	public static final String PROP_SCOPES = VOCABULARY_NAMESPACE + "hasScopes";

	/**
	 * The property URI for holding the Origin Scope. This property is to be
	 * used directly by {@link Resource#getProperty(String)} and
	 * {@link Resource#setProperty(String, Object)}, only by the router
	 * artifacts.
	 *
	 */
	public static final String PROP_ORIG_SCOPE = VOCABULARY_NAMESPACE + "hasOriginScope";

	/**
	 * A special scope indicating the Resource may not be serialized to other
	 * Spaces.
	 */
	public static final String ONLY_LOCAL_SCOPE = VOCABULARY_NAMESPACE + "localScope";

	public ScopedResource() {
		super();
	}

	public ScopedResource(boolean isXMLLiteral) {
		super(isXMLLiteral);
	}

	public ScopedResource(String uri) {
		super(uri);
	}

	public ScopedResource(String uri, boolean isXMLLiteral) {
		super(uri, isXMLLiteral);
	}

	public ScopedResource(String uriPrefix, int numProps) {
		super(uriPrefix, numProps);
	}

	/**
	 * Check if there is any scopes for this resource. This includes
	 * {@link ScopedResource#ONLY_LOCAL_SCOPE}.
	 *
	 * @return true if there is one or more scopes annotated for this resource.
	 */
	public boolean isScoped() {
		return props.containsKey(PROP_SCOPES);
	}

	/**
	 * List all scopes associated to this resource.
	 *
	 * @return always a list, empty if there are no scopes.
	 */
	public final List getScopes() {
		Object s = getProperty(PROP_SCOPES);
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
	public final boolean addScope(String newScope) {
		if (newScope == null) {
			return false;
		}
		Object s = getProperty(PROP_SCOPES);
		if (s instanceof String) {
			List res = new ArrayList();
			res.add(s);
			res.add(newScope);
			props.put(PROP_SCOPES, res);
			return true;
		} else if (s instanceof List) {
			((List) s).add(newScope);
			props.put(PROP_SCOPES, s);
			return true;
		} else if (s == null) {
			props.put(PROP_SCOPES, newScope);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Remove all scope annotations from this resource. If the Resource is being
	 * sent and the scopes are cleared it is assumed that it has to be sent to
	 * all available Scopes.
	 *
	 * @return iff the scopes have been cleared.
	 */
	public final boolean clearScopes() {
		return changeProperty(PROP_SCOPES, null);
	}

	/**
	 * Copy the scope from another {@link ScopedResource}.
	 *
	 * @param src
	 *            the source from which to copy the scope.
	 * @return whether the change was successful or not.
	 */
	public final boolean setScope(ScopedResource src) {
		Object scope = src.getProperty(PROP_SCOPES);
		if (scope == null) {
			return clearScopes();
		} else {
			props.put(PROP_SCOPES, scope);
			return true;
		}
	}

	/**
	 * Check whether if this {@link ScopedResource} may be sent to a destination
	 * scope <br>
	 *
	 * <ul>
	 * <li>Deny if the origin is the destination candidate (this will cause
	 * message loops)
	 * <li>Deny if the destinations do contain
	 * {@link ScopedResource#ONLY_LOCAL_SCOPE} (tenant-aware application has
	 * decided this message is only local)
	 * <li>Deny if the destinations are not empty and do not contain the
	 * candidate destination
	 * <li>Allow if the destinations are empty (non-tenant-aware application has
	 * generated the message)
	 * <li>Allow if the destinations are not empty and contains the candidate
	 * destination.
	 * </ul>
	 *
	 * @param destinationCandidateScope
	 *            The destination to be checked, If null returns false.
	 * @return true iff it may be sent according to origin / destination
	 *         restrictions.
	 */
	public final boolean isSerializableTo(String destinationCandidateScope) {
		if (destinationCandidateScope == null) {
			return false;
		}
		String orig = getOriginScope();
		List dest = getScopes();
		return !destinationCandidateScope.equals(orig) && !dest.contains(ONLY_LOCAL_SCOPE)
				&& (dest.isEmpty() || dest.contains(destinationCandidateScope));
	}

	/**
	 * Get the Scope of the original Space sender of this
	 * {@link ScopedResource}. To be used only by Gateways!
	 *
	 * @return The scoped assiged usig
	 *         {@link ScopedResource#setOriginScope(String)}.
	 */
	public final String getOriginScope() {
		return (String) props.get(PROP_ORIG_SCOPE);
	}

	/**
	 * Set the Origin Scope of an incoming {@link ScopedResource}. To be used by
	 * Gateways! To reset, use with scope = null.
	 *
	 * @param scope
	 *            The Scope ID.
	 */
	public final void setOriginScope(String scope) {
		if (scope != null) {
			props.put(PROP_ORIG_SCOPE, scope);
		} else {
			props.remove(PROP_ORIG_SCOPE);
		}
	}
}
