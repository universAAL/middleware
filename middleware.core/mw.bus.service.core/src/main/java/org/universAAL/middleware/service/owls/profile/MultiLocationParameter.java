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
package org.universAAL.middleware.service.owls.profile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.owl.supply.AbsLocation;

/**
 * The superclass for profile parameters that deal with locations in relation to
 * services, hence the restriction for the property 'valueData' is not supported
 * by this class but 'sParameter' will be mandatory accepting an array of
 * {@link org.persona.ontology.AbsLocation}s. For the sake of well-formedness,
 * serialization type of properties and setting property values, it relies on
 * the default implementations provided by the superclasses.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public class MultiLocationParameter extends ProfileParameter {
    public static final String MY_URI = uAAL_SERVICE_NAMESPACE
	    + "MultiAbsLocationParameter";

    public MultiLocationParameter(AbsLocation[] value) {
	super();
	addType(MY_URI, true);
	if (value != null && value.length > 0) {
	    ArrayList l = new ArrayList(value.length);
	    for (int i = 0; i < value.length && value[i] != null; i++)
		l.add(value[i]);
	    if (!l.isEmpty())
		props.put(PROP_OWLS_PROFILE_S_PARAMETER, l);
	}
    }

    public MultiLocationParameter(String uri, AbsLocation[] value) {
	super(uri);
	addType(MY_URI, true);
	if (value != null && value.length > 0) {
	    ArrayList l = new ArrayList(value.length);
	    for (int i = 0; i < value.length && value[i] != null; i++)
		l.add(value[i]);
	    if (!l.isEmpty())
		props.put(PROP_OWLS_PROFILE_S_PARAMETER, l);
	}
    }

    /**
     * This method returns a list of abstract locations that are relevant for
     * the parameters.
     * 
     * @return abstract locations in form of a list
     */
    public List getAbsLocations() {
	return (List) props.get(PROP_OWLS_PROFILE_S_PARAMETER);
    }

    /**
     * Sets the property for a specified URI to a value.
     * 
     * @param propURI
     *            property URI
     * @param value
     *            value of the property
     */
    public boolean setProperty(String propURI, Object value) {
	if (propURI != null && value != null && !props.containsKey(propURI))
	    if (propURI.equals(PROP_OWLS_PROFILE_SERVICE_PARAMETER_NAME)) {
		if (value instanceof String) {
		    props.put(propURI, value);
		    return true;
		}
	    } else if (propURI.equals(PROP_OWLS_PROFILE_S_PARAMETER)) {
		if (value instanceof AbsLocation) {
		    ArrayList l = new ArrayList(1);
		    l.add(value);
		    props.put(propURI, l);
		    return true;
		} else if (value instanceof List && !((List) value).isEmpty()) {
		    for (Iterator i = ((List) value).iterator(); i.hasNext();)
			if (!(i.next() instanceof AbsLocation))
			    return false;
		    props.put(propURI, value);
		    return true;
		}
	    }
	return false;
    }
}
