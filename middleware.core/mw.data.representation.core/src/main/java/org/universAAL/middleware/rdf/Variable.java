/*
	Copyright 2010-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.rdf;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Super class for OWL-S Support of Process Parameters.
 *
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public abstract class Variable extends FinalizedResource {

	// URIs of standard variables managed by the universAAL middleware
	/**
	 * The URI of a standard variable managed by the universAAL middleware indicating
	 * the current time.
	 */
	public static final String VAR_CURRENT_DATETIME = Resource.VOCABULARY_NAMESPACE + "currentDatetime";

	/**
	 * The URI of a standard variable managed by the universAAL middleware indicating
	 * the software component currently accessing the middleware.
	 */
	public static final String VAR_ACCESSING_BUS_MEMBER = Resource.VOCABULARY_NAMESPACE
			+ "theAccessingBusMember";

	/**
	 * The URI of a standard variable managed by the universAAL middleware indicating
	 * the current human user as claimed by
	 * {@link #VAR_ACCESSING_BUS_MEMBER}.
	 */
	public static final String VAR_ACCESSING_HUMAN_USER = Resource.VOCABULARY_NAMESPACE
			+ "theAccessingHumanUser";

	/**
	 * The URI of a standard variable managed by the universAAL middleware indicating
	 * the profile of a service that is estimated to be appropriate for
	 * responding the current service request.
	 */
	public static final String VAR_SERVICE_TO_SELECT = Resource.VOCABULARY_NAMESPACE + "theServiceToSelect";

	/**
	 * Storage for all handlers of subclasses. Subclasses have to
	 * {@link #register(VariableHandler)} themselves to this class.
	 */
	private static final ArrayList<VariableHandler> handlers = new ArrayList<VariableHandler>(2);

	/**
	 * Subclasses must implement and register this interface.
	 */
	public static interface VariableHandler {
		/** @see Variable#isVarRef(Object) */
		public abstract boolean isVarRef(Object o);

		/** @see Variable#resolveVarRef(Object, HashMap) */
		public abstract Object resolveVarRef(Object o, HashMap context);

		/** @see Variable#checkDeserialization(Object) */
		public abstract boolean checkDeserialization(Object o);
	}

	/** The constructor. */
	protected Variable(String uri) {
		super(uri);
	}

	/**
	 * Determines if a specified object can be de-serialized to a subclass of
	 * {@link Variable}. Must be implemented by subclasses.
	 *
	 * @param o
	 *            The object to be investigated, must be a subclass of
	 *            {@link Resource}.
	 */
	public static boolean checkDeserialization(Object o) {
		for (int i = 0; i < handlers.size(); i++) {
			try {
				if (handlers.get(i).checkDeserialization(o))
					return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * Determines if the specified object is a {@link Resource} and is of type
	 * owls:ValueOf.
	 */
	public static boolean isVarRef(Object o) {
		for (int i = 0; i < handlers.size(); i++) {
			try {
				if (handlers.get(i).isVarRef(o))
					return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	/** Registration: subclasses must register to this class. */
	protected static void register(VariableHandler h) {
		handlers.add(h);
	}

	public static Object resolveVarRef(Object o, HashMap context) {
		Object aux;
		for (int i = 0; i < handlers.size(); i++) {
			try {
				aux = handlers.get(i).resolveVarRef(o, context);
				if (aux != o)
					return aux;
			} catch (Exception e) {
			}
		}
		return o;
	}

	public abstract int getMinCardinality();

	public abstract Object getDefaultValue();

	public abstract String getParameterType();

}
