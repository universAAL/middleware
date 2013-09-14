/*******************************************************************************
 * Copyright 2013-2014 Ericsson Nikola Tesla d.d., www.ericsson.com/hr/
 *
 * See the NOTICE file distributed with this work for additional 
 * information regarding copyright ownership
 *	
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *	
 * http://www.apache.org/licenses/LICENSE-2.0
 *	
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.middleware.ui.owl;

import org.universAAL.middleware.owl.ManagedIndividual;

/**
 * Main class used for modeling of User Preferences.
 * 
 * @author eandgrg
 * 
 */
public abstract class Preference extends ManagedIndividual {

    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "Preference";

    /** The constructor for (de-)serializers. */
    public Preference() {
	super();
    }

    /** The constructor for subclasses */
    public Preference(String uri) {
	super(uri);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.universAAL.middleware.owl.ManagedIndividual#getClassURI()
     */
    public String getClassURI() {
	return MY_URI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.universAAL.middleware.owl.ManagedIndividual#isWellFormed()
     */
    public abstract boolean isWellFormed();
}
