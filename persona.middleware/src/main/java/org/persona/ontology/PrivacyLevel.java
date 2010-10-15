/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.persona.ontology;

/**
 * @author mtazari
 *
 */
public class PrivacyLevel extends ComparableIndividual {
	public static final String MY_URI;
	static {
		MY_URI = PERSONA_VOCABULARY_NAMESPACE + "PrivacyLevel";
		register(PrivacyLevel.class);
	}
	
	public static final int INSENSIBLE = 0;
	public static final int KNOWN_PEOPLE_ONLY = 1;
	public static final int INTIMATES_ONLY = 2;
	public static final int HOME_MATES_ONLY = 3;
	public static final int PERSONAL = 4;

	private static final String[] names = {
        "insensible", "known_people_only", "intimates_only", "home_mates_only", "personal"
    };
	
	public static final PrivacyLevel personal = new PrivacyLevel(PERSONAL);
	public static final PrivacyLevel homeMatesOnly = new PrivacyLevel(HOME_MATES_ONLY);
	public static final PrivacyLevel intimatesOnly = new PrivacyLevel(INTIMATES_ONLY);
	public static final PrivacyLevel knownPeopleOnly = new PrivacyLevel(KNOWN_PEOPLE_ONLY);
	public static final PrivacyLevel insensible = new PrivacyLevel(INSENSIBLE);
	
	/**
	 * Returns the list of all class members guaranteeing that no other members
	 * will be created after a call to this method.
	 */
	public static ManagedIndividual[] getEnumerationMembers() {
		return new ManagedIndividual[] {personal, homeMatesOnly, intimatesOnly, knownPeopleOnly, insensible};
	}
	
	/**
	 * Returns the privacy level with the given URI. 
	 */
	public static ManagedIndividual getIndividualByURI(String instanceURI) {
		return (instanceURI != null
				&&  instanceURI.startsWith(PERSONA_VOCABULARY_NAMESPACE))?
						valueOf(instanceURI.substring(PERSONA_VOCABULARY_NAMESPACE.length()))
						: null;
	}
	
	public static PrivacyLevel getMaxValue() {
		return personal;
	}
	
	public static PrivacyLevel getMinValue() {
		return insensible;
	}
	
	public static PrivacyLevel getLevelByOrder(int order) {
        switch (order) {
        case INSENSIBLE: return insensible;
        case KNOWN_PEOPLE_ONLY: return knownPeopleOnly;
        case INTIMATES_ONLY: return intimatesOnly;
        case HOME_MATES_ONLY: return homeMatesOnly;
        case PERSONAL: return personal;
        default: return null;
        }
	}
	
	/**
	 * Returns the value of the property <code>rdfs:comment</code> on this <code>owl:Class</code>
	 * from the underlying ontology.
	 */
	public static String getRDFSComment() {
		return "An enumeration for specifying the privacy level of information.";
	}

	/**
	 * Returns the value of the property <code>rdfs:label</code> on this <code>owl:Class</code>
	 * from the underlying ontology.
	 */
	public static String getRDFSLabel() {
		return "Privacy Level";
	}
    
    public static final PrivacyLevel valueOf(String name) {
        for (int i=INSENSIBLE;  i<=PERSONAL; i++)
            if (names[i].equals(name))
                return getLevelByOrder(i);
        return null;
    }
    
    private int order;
    
 // prevent the usage of the default constructor
    private PrivacyLevel() {
       
    }
    
    private PrivacyLevel(int order) {
    	super(PERSONA_VOCABULARY_NAMESPACE + names[order]);
        this.order = order;
    }

	public int compareTo(Object other) {
		return (this == other)? 0
				: (order < ((PrivacyLevel) other).order)? -1
						: 1;
	}

	public ComparableIndividual getNext() {
		return getLevelByOrder(order+1);
	}

	public ComparableIndividual getPrevious() {
		return getLevelByOrder(order-1);
	}

	public int getPropSerializationType(String propURI) {
		return PROP_SERIALIZATION_OPTIONAL;
	}
	
	public boolean isWellFormed() {
		return true;
	}
    
    public String name() {
        return names[order];
    }
    
    public int ord() {
        return order;
    }

	public void setProperty(String propURI, Object o) {
		// do nothing
	}
}
