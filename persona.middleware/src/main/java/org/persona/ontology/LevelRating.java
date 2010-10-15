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
public class LevelRating extends ComparableIndividual {
	public static final String MY_URI;
	static {
		MY_URI = PERSONA_VOCABULARY_NAMESPACE + "LevelRating";
		register(LevelRating.class);
	}
	
	public static final int NONE = 0;
	public static final int LOW = 1;
	public static final int MIDDLE = 2;
	public static final int HIGH = 3;
	public static final int FULL = 4;

	private static final String[] names = {
        "none", "low", "middle", "high", "full"
    };
	
	public static final LevelRating none = new LevelRating(NONE);
	public static final LevelRating low = new LevelRating(LOW);
	public static final LevelRating middle = new LevelRating(MIDDLE);
	public static final LevelRating high = new LevelRating(HIGH);
	public static final LevelRating full = new LevelRating(FULL);
	
	/**
	 * Returns the list of all class members guaranteeing that no other members
	 * will be created after a call to this method.
	 */
	public static ManagedIndividual[] getEnumerationMembers() {
		return new ManagedIndividual[] {none, low, middle, high, full};
	}
	
	/**
	 * Returns the level rating with the given URI. 
	 */
	public static ManagedIndividual getIndividualByURI(String instanceURI) {
		return (instanceURI != null
				&&  instanceURI.startsWith(PERSONA_VOCABULARY_NAMESPACE))?
						valueOf(instanceURI.substring(PERSONA_VOCABULARY_NAMESPACE.length()))
						: null;
	}
	
	public static LevelRating getMaxValue() {
		return full;
	}
	
	public static LevelRating getMinValue() {
		return none;
	}
	
	public static LevelRating getLevelByOrder(int order) {
        switch (order) {
        case NONE: return none;
        case LOW: return low;
        case MIDDLE: return middle;
        case HIGH: return high;
        case FULL: return full;
        default: return null;
        }
	}
	
	/**
	 * Returns the value of the property <code>rdfs:comment</code> on this <code>owl:Class</code>
	 * from the underlying ontology.
	 */
	public static String getRDFSComment() {
		return "An enumeration for specifying the level of appearance / availability of a phenomen.";
	}

	/**
	 * Returns the value of the property <code>rdfs:label</code> on this <code>owl:Class</code>
	 * from the underlying ontology.
	 */
	public static String getRDFSLabel() {
		return "Level Rating";
	}
    
    public static final LevelRating valueOf(String name) {
        for (int i=NONE;  i<=FULL; i++)
            if (names[i].equals(name))
                return getLevelByOrder(i);
        return null;
    }
    
    private int order;
    
 // prevent the usage of the default constructor
    private LevelRating() {
       
    }
    
    private LevelRating(int order) {
    	super(PERSONA_VOCABULARY_NAMESPACE + names[order]);
        this.order = order;
    }

	public int compareTo(Object other) {
		return (this == other)? 0
				: (order < ((LevelRating) other).order)? -1
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
