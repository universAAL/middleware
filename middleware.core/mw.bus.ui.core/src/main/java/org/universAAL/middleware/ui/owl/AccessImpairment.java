/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either.ss or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.ui.owl;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.supply.LevelRating;

/**
 * 
 * Main class used for modeling of access impairments (e.g. related to sight,
 * hearing, speaking, physical condition). Impairments can be of levels as
 * defined in {@link org.universAAL.middleware.owl.supply.LevelRating} , namely:
 * none, low, medium, high and full.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ManagedIndividual
 */
public class AccessImpairment extends ManagedIndividual {
    public static final String MY_URI;
    public static final String PROP_IMPAIRMENT_LEVEL;

    static {
	MY_URI = uAAL_VOCABULARY_NAMESPACE + "AccessImpairment";
	PROP_IMPAIRMENT_LEVEL = uAAL_VOCABULARY_NAMESPACE + "impairmentLevel";
    }

    /** The constructor for (de-)serializers. */
    public AccessImpairment() {
	super();
    }

    /** The constructor for use by applications. */
    public AccessImpairment(LevelRating impairmentLevel) {
	super();
	props.put(PROP_IMPAIRMENT_LEVEL, impairmentLevel);
    }

    /** @see org.universAAL.middleware.owl.ManagedIndividual#getClassURI() */
    public String getClassURI() {
	return MY_URI;
    }

    /**
     * @return level rating
     */
    public LevelRating getImpaimentLevel() {
	return (LevelRating) props.get(PROP_IMPAIRMENT_LEVEL);
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#getPropSerializationType
     *      (java.lang.String)
     */
    public int getPropSerializationType(String propURI) {
	return (PROP_IMPAIRMENT_LEVEL.equals(propURI)) ? PROP_SERIALIZATION_REDUCED
		: PROP_SERIALIZATION_OPTIONAL;
    }

    /**
     * @see org.universAAL.middleware.owl.ManagedIndividual#isWellFormed()
     */
    public boolean isWellFormed() {
	return props.containsKey(PROP_IMPAIRMENT_LEVEL);
    }
}
