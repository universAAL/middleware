/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

package org.universAAL.middleware.managers.configuration.core.impl.factories;

import org.universAAL.middleware.interfaces.configuration.scope.AALSpaceScope;
import org.universAAL.middleware.interfaces.configuration.scope.AppPartScope;
import org.universAAL.middleware.interfaces.configuration.scope.ApplicationScope;
import org.universAAL.middleware.interfaces.configuration.scope.InstanceScope;
import org.universAAL.middleware.interfaces.configuration.scope.ModuleScope;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;
import org.universAAL.middleware.managers.configuration.core.owl.Entity;

/**
 * Transform {@link Scope Scopes} to and from URI definitions.
 * 
 * @author amedrano
 */
public class ScopeFactory {

    /**
     * Application part level parameter Identifier
     */
    private static final String L_APP_PART = "part";
    /**
     * Module level parameter identifier
     */
    private static final String L_MODULE = "mod";
    /**
     * Application level parameter identifier.
     */
    private static final String L_APP = "app";
    /**
     * Instance Level parameter identifier
     */
    private static final String L_INSTANCE = "inst";
    /**
     * URN root, to identify URNs that are to be meant for this module.
     */
    private static final String URN_ROOT = "configscope";
    /**
     * Separator used to separate urn parts
     */
    private static final String SEPARATOR = ":";

    /**
     * Transform a {@link Scope} to a {@link String} that can be used to
     * uniquely identified the associated {@link Entity}.
     * 
     * @param scope
     * @return a URI.
     */
    public static String getScopeURN(Scope scope) {
	if (scope == null) {
	    return null;
	}
	String urn = "", l1 = "", l2 = "";
	urn = "urn:" + URN_ROOT + SEPARATOR + scope.getId();
	if (scope instanceof InstanceScope) {
	    l1 = L_INSTANCE + SEPARATOR + ((InstanceScope) scope).getPeerID();
	} else if (scope instanceof ApplicationScope) {
	    l1 = L_APP + SEPARATOR + ((ApplicationScope) scope).getAppID();
	}
	if (scope instanceof ModuleScope) {
	    l2 = L_MODULE + SEPARATOR + (((ModuleScope) scope).getModuleID());
	} else if (scope instanceof AppPartScope) {
	    l2 = L_APP_PART + SEPARATOR + ((AppPartScope) scope).getPartID();
	}
	if (!l1.isEmpty()) {
	    urn += SEPARATOR;
	}
	if (!l2.isEmpty()) {
	    l1 += SEPARATOR;
	}
	return urn + l1 + l2;
    }

    /**
     * Given a String, try to decifer the Scope instance that is associated to
     * it.
     * 
     * @param urn
     *            the URI.
     * @return a {@link Scope}, null if could not parse.
     */
    public static Scope getScope(String urn) {
	String[] params = urn.split(SEPARATOR);
	if (params.length <= 2 || !params[1].equals(URN_ROOT)) {
	    return null;
	}
	if (params.length == 3) {
	    return new AALSpaceScope(params[2]);
	}
	if (params.length == 5) {
	    if (params[3].equals(L_INSTANCE)) {
		return new InstanceScope(params[2], params[4]);
	    }
	    if (params[3].equals(L_APP)) {
		return new ApplicationScope(params[2], params[4]);
	    }
	}
	if (params.length == 7) {

	    if (params[3].equals(L_INSTANCE) && params[5].equals(L_MODULE)) {
		return new ModuleScope(params[2], params[4], params[6]);
	    }
	    if (params[3].equals(L_APP) && params[5].equals(L_APP_PART)) {
		return new AppPartScope(params[2], params[4], params[6]);
	    }
	}
	return null;
    }
}
