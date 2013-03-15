/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.context.owl;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.context.ContextEvent;

/**
 * An enumeration for indicating the type of a context provider, which may be a
 * controller, a gauge, or a reasoner. A controller is a context provider that
 * has the control over some context element and hence can report about changes
 * in its/their state(s) or answer queries about that. A gauge is a sensor
 * wrapper; sensors measure real things, such as weight, temperature, the angle
 * to a satellite, etc. A reasoner uses different sources (including other
 * context providers) and infers the state of a context element using different
 * methods, such as aggregation, statistical analysis, and logical deduction, to
 * name a few.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class ContextProviderType extends ManagedIndividual {

    public static final String MY_URI = ContextEvent.uAAL_CONTEXT_NAMESPACE
	    + "ContextProviderType";

    /**
     * The ordering number for controllers, needed for the implementation of the
     * enumeration.
     */
    public static final int CONTROLLER = 0;

    /**
     * The ordering number for gauges, needed for the implementation of the
     * enumeration.
     */
    public static final int GAUGE = 1;

    /**
     * The ordering number for reasoners, needed for the implementation of the
     * enumeration.
     */
    public static final int REASONER = 2;

    /**
     * The the names used to construct the URIs of context provider types.
     */
    private static final String[] names = { "controller", "gauge", "reasoner" };

    /**
     * The type of a controller as an instance of this class.
     */
    public static final ContextProviderType controller = new ContextProviderType(
	    CONTROLLER);

    /**
     * The type of a gauge as an instance of this class.
     */
    public static final ContextProviderType gauge = new ContextProviderType(
	    GAUGE);

    /**
     * The type of a reasoner as an instance of this class.
     */
    public static final ContextProviderType reasoner = new ContextProviderType(
	    REASONER);

    public static ContextProviderType getProviderByOrder(int order) {
	switch (order) {
	case CONTROLLER:
	    return controller;
	case GAUGE:
	    return gauge;
	case REASONER:
	    return reasoner;
	default:
	    return null;
	}
    }

    public static final ContextProviderType valueOf(String name) {
	for (int i = CONTROLLER; i <= REASONER; i++)
	    if (names[i].equals(name))
		return getProviderByOrder(i);
	return null;
    }

    private int order;

    // prevent the usage of the default constructor
    private ContextProviderType() {
    }

    // for the internal usage above
    private ContextProviderType(int order) {
	super(ContextEvent.uAAL_CONTEXT_NAMESPACE + names[order]);
	this.order = order;
    }

    public String getClassURI() {
	return MY_URI;
    }

    /**
     * The class has no property; any "imaginary" property can be ignored!
     * 
     * @see ManagedIndividual#getPropSerializationType(String).
     */
    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_OPTIONAL;
    }

    /**
     * The internal instances created above are always well-formed.
     * 
     * @see ManagedIndividual#isWellFormed().
     */
    public boolean isWellFormed() {
	return true;
    }

    /**
     * Returns the local name used for constructing the URI of this instance.
     */
    public String name() {
	return names[order];
    }

    /**
     * Returns the order of this instance that can be useful for switch-case
     * statements.
     */
    public int ord() {
	return order;
    }

    /**
     * The class has no property, so ignore the call!
     * 
     * @see ManagedIndividual#setProperty(String, Object).
     */
    public void setProperty(String propURI, Object o) {
	// do nothing
    }
}
