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
package org.universAAL.middleware.rdf;

import java.util.ArrayList;

public abstract class Property extends Resource {

    protected Object password;
    protected boolean isFunctional;
    private volatile ArrayList subPropertyOf = new ArrayList();
    private volatile ArrayList equivalentProperties = new ArrayList();
    private static Object equivalentPropertiesSync = new Object();

    protected Property(Object password, String uri) {
	super(uri);
	this.password = password;
    }

    public boolean isFunctional() {
	return isFunctional;
    }

    public void setFunctional(Object password, boolean isFunctional)
	    throws IllegalAccessException {
	if (!(this.password.equals(password)))
	    throw new IllegalAccessException(
		    "The specified password is not correct.");
	this.isFunctional = isFunctional;
    }

    public synchronized void addSuperProperty(Property superProperty) {
	if (subPropertyOf.contains(superProperty))
	    return;

	ArrayList al = new ArrayList(subPropertyOf);
	al.add(superProperty);
	subPropertyOf = al;
    }

    public void addEquivalentProperty(Property equivalentProperty) {
	// we have to synchronize for all Property instances that may be in the
	// set of equivalent properties
	// -> just synch over all Properties (synch only blocks this method, and
	// adding equivalent properties is assumed to happen not very often;
	// mainly at the beginning)
	synchronized (equivalentPropertiesSync) {
	    // get the two sets of Properties
	    ArrayList set1 = equivalentProperties;
	    ArrayList set2 = equivalentProperty.equivalentProperties;

	    // combine the two sets
	    ArrayList comb = new ArrayList(set1.size() + set2.size());
	    comb.addAll(set1);
	    for (int i = 0; i < set2.size(); i++)
		if (!comb.contains(set2.get(i)))
		    comb.add(set2.get(i));

	    // set the combined set in all Properties
	    for (int i = 0; i < comb.size(); i++)
		((Property) comb.get(i)).equivalentProperties = comb;
	}
    }
}
