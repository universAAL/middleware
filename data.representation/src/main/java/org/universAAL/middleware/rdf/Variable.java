/*
	Copyright 2010-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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
import java.util.Hashtable;

/**
 * Super class for OWL-S Support of Process Parameters.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public abstract class Variable extends Resource {

    // URIs of standard variables managed by the uAAL middleware
    /**
     * The URI of a standard variable managed by the uAAL middleware indicating
     * the current time.
     */
    public static final String VAR_uAAL_CURRENT_DATETIME = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "currentDatetime";

    /**
     * The URI of a standard variable managed by the uAAL middleware indicating
     * the software component currently accessing the middleware.
     */
    public static final String VAR_uAAL_ACCESSING_BUS_MEMBER = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theAccessingBusMember";

    /**
     * The URI of a standard variable managed by the uAAL middleware indicating
     * the current human user as claimed by
     * {@link #VAR_uAAL_ACCESSING_BUS_MEMBER}.
     */
    public static final String VAR_uAAL_ACCESSING_HUMAN_USER = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theAccessingHumanUser";

    /**
     * The URI of a standard variable managed by the uAAL middleware indicating
     * the profile of a service that is estimated to be appropriate for
     * responding the current service request.
     */
    public static final String VAR_uAAL_SERVICE_TO_SELECT = Resource.uAAL_VOCABULARY_NAMESPACE
	    + "theServiceToSelect";

    /**
     * Storage for all subclasses. Subclasses have to {@link #register(Class)}
     * themselves to this class.
     */
    private static final ArrayList varClasses = new ArrayList(2);

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
	try {
	    Object aux = null;
	    for (int i = 0; i < varClasses.size(); i++) {
		aux = ((Class) varClasses.get(i)).getMethod(
			"checkDeserialization", new Class[] { Object.class })
			.invoke(null, new Object[] { o });
		if (aux instanceof Boolean)
		    return ((Boolean) aux).booleanValue();
	    }
	} catch (Exception e) {
	}
	return true;
    }

    /**
     * Determines if the specified object is a {@link Resource} and is of type
     * owls:ValueOf.
     */
    public static boolean isVarRef(Object o) {
	try {
	    Object aux = null;
	    for (int i = 0; i < varClasses.size(); i++) {
		aux = ((Class) varClasses.get(i)).getMethod("isVarRef",
			new Class[] { Object.class }).invoke(null,
			new Object[] { o });
		if (aux instanceof Boolean)
		    return ((Boolean) aux).booleanValue();
	    }
	} catch (Exception e) {
	}
	return false;
    }

    /** Registration: subclasses must register to this class. */
    protected static void register(Class clz) {
	if (Variable.class.isAssignableFrom(clz))
	    varClasses.add(clz);
    }

    public static Object resolveVarRef(Object o, Hashtable context) {
	try {
	    Object aux;
	    for (int i = 0; i < varClasses.size(); i++) {
		aux = ((Class) varClasses.get(i)).getMethod("resolveVarRef",
			new Class[] { Object.class, Hashtable.class }).invoke(
			null, new Object[] { o, context });
		if (aux != o)
		    return aux;
	    }
	} catch (Exception e) {
	}
	return o;
    }

    public abstract int getMinCardinality();

    public abstract Object getDefaultValue();

    public abstract String getParameterType();

}
