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
package org.universAAL.middleware.context.owl;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.owl.ManagedIndividual;

/**
 * Ontological representation of a ContextProvider
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class ContextProvider extends ManagedIndividual {
    public static final String MY_URI;

    /**
     * An array of {@link ContextEventPattern}s each of which declares a class
     * of context events that the provider provides.
     */
    public static final String PROP_CONTEXT_PROVIDED_EVENTS;

    /**
     * The type of a context provider. The range is {@link ContextProviderType}.
     */
    public static final String PROP_CONTEXT_PROVIDER_TYPE;

    /**
     * The underlying device(s) used by a context provider to determine the
     * state of the provided context elements. The range is
     * {@link org.universAAL.middleware.context.owl.ManagedIndividual}.
     */
    public static final String PROP_CONTEXT_SOURCE;

    static {
	MY_URI = ContextEvent.uAAL_CONTEXT_NAMESPACE + "ContextProvider";
	PROP_CONTEXT_PROVIDED_EVENTS = ContextEvent.uAAL_CONTEXT_NAMESPACE
		+ "myClassesOfEvents";
	PROP_CONTEXT_PROVIDER_TYPE = ContextEvent.uAAL_CONTEXT_NAMESPACE
		+ "hasType";
	PROP_CONTEXT_SOURCE = ContextEvent.uAAL_CONTEXT_NAMESPACE + "hasSource";
    }

    public ContextProvider() {
	super();
    }

    public ContextProvider(String uri) {
	super(uri);
    }

    public String getClassURI() {
	return MY_URI;
    }

    protected ContextProvider(String uriPrefix, int numProps) {
	super(uriPrefix, numProps);
    }

    /**
     * Returns the list of devices used by this context provider.
     */
    public ManagedIndividual[] getContextSources() {
	List l = (List) getProperty(PROP_CONTEXT_SOURCE);
	return (l == null) ? null : (ManagedIndividual[]) l
		.toArray(new ManagedIndividual[l.size()]);
    }

    /**
     * The type of a context provider should be of minor interest, whereas the
     * source could be important; but even the latter can be represented in its
     * reduced form.
     * 
     * @see ManagedIndividual#getPropSerializationType(String)
     */
    public int getPropSerializationType(String propURI) {
	if (PROP_CONTEXT_SOURCE.equals(propURI)
		|| PROP_CONTEXT_PROVIDER_TYPE.equals(propURI))
	    return PROP_SERIALIZATION_REDUCED;
	return PROP_SERIALIZATION_OPTIONAL;
    }

    /**
     * Returns the classes of context events provided by this context provider.
     */
    public ContextEventPattern[] getProvidedEvents() {
	List l = (List) getProperty(PROP_CONTEXT_PROVIDED_EVENTS);
	return (l == null) ? null : (ContextEventPattern[]) l
		.toArray(new ContextEventPattern[l.size()]);
    }

    public ContextProviderType getProviderType() {
	return (ContextProviderType) props.get(PROP_CONTEXT_PROVIDER_TYPE);
    }

    /**
     * @see ManagedIndividual#isWellFormed()
     */
    public boolean isWellFormed() {
	ContextProviderType type = getProviderType();
	return type != null
	// forcing the existence of context sources is probably not necessary
		// an example: controllers usually report about the state of the
		// controlled device, hence the device will appear already as
		// the subject and there is no need to redundantly specify it
		// also as the source
		// && (type == ContextProviderType.reasoner || props
		// .containsKey(PROP_CONTEXT_SOURCE))
		&& props.containsKey(PROP_CONTEXT_PROVIDED_EVENTS);
    }

    /**
     * Allows to assign a set of ManagedIndividuals as the origin of the Context
     * Information provided by this ContextProvider
     * 
     * @param devices
     *            An Array of ManagedIndividuals representing the Devices (or
     *            whatever) that generate the actual information provided by
     *            this ContextProvider
     */
    public boolean setContextSources(ManagedIndividual[] devices) {
	if (devices != null && devices.length > 0
		&& !props.containsKey(PROP_CONTEXT_SOURCE)) {
	    List l = new ArrayList(devices.length);
	    for (int i = 0; i < devices.length; i++)
		l.add(devices[i]);
	    props.put(PROP_CONTEXT_SOURCE, l);
	    return true;
	}
	return false;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#setProperty(java.lang.String,
     *      java.lang.Object)
     */
    public boolean setProperty(String propURI, Object value) {
	if (PROP_CONTEXT_SOURCE.equals(propURI)) {
	    if (value instanceof ManagedIndividual[])
		return setContextSources((ManagedIndividual[]) value);
	    else if (value instanceof ManagedIndividual) {
		List l = new ArrayList(1);
		l.add(value);
		props.put(PROP_CONTEXT_SOURCE, l);
		return true;
	    } else if (value instanceof List) {
		for (int i = 0; i < ((List) value).size(); i++)
		    if (!(((List) value).get(i) instanceof ManagedIndividual))
			return false;
		props.put(PROP_CONTEXT_SOURCE, (List) value);
		return true;
	    }
	    return false;
	} else if (PROP_CONTEXT_PROVIDER_TYPE.equals(propURI)
		&& value instanceof ContextProviderType)
	    return setType((ContextProviderType) value);
	else if (PROP_CONTEXT_PROVIDED_EVENTS.equals(propURI)) {
	    if (value instanceof ContextEventPattern[])
		return setProvidedEvents((ContextEventPattern[]) value);
	    else if (value instanceof ContextEventPattern) {
		List l = new ArrayList(1);
		l.add(value);
		props.put(PROP_CONTEXT_PROVIDED_EVENTS, l);
		return true;
	    } else if (value instanceof List) {
		for (int i = 0; i < ((List) value).size(); i++)
		    if (!(((List) value).get(i) instanceof ContextEventPattern))
			return false;
		props.put(PROP_CONTEXT_PROVIDED_EVENTS, (List) value);
		return true;
	    }
	    return false;
	} else
	    return super.setProperty(propURI, value);
    }

    /**
     * Allows to assign a set of {@link ContextEventPattern}s as the classes of
     * context events provided by this ContextProvider
     * 
     * @param devices
     *            An Array of ManagedIndividuals representing the Devices (or
     *            whatever) that generate the actual information provided by
     *            this ContextProvider
     */
    public boolean setProvidedEvents(ContextEventPattern[] myEvents) {
	if (myEvents != null && myEvents.length > 0
		&& !props.containsKey(PROP_CONTEXT_PROVIDED_EVENTS)) {
	    List l = new ArrayList(myEvents.length);
	    for (int i = 0; i < myEvents.length; i++)
		l.add(myEvents[i]);
	    props.put(PROP_CONTEXT_PROVIDED_EVENTS, l);
	    return true;
	}
	return false;
    }

    /**
     * Set the type of this ContextProvider to one of those defined in
     * ContextProviderType
     * 
     * @param type
     *            The ContextProviderType of the ContextProvider
     */
    public boolean setType(ContextProviderType type) {
	if (type != null && !props.containsKey(PROP_CONTEXT_PROVIDER_TYPE)) {
	    props.put(PROP_CONTEXT_PROVIDER_TYPE, type);
	    return true;
	}
	return false;
    }
}
