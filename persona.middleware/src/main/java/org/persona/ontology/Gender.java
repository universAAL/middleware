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
public class Gender extends ManagedIndividual {
	public static final String MY_URI;
	static {
		MY_URI = PERSONA_VOCABULARY_NAMESPACE + "Gender";
		register(Gender.class);
	}
	
	public static final int FEMALE = 0;
	public static final int MALE = 1;

	private static final String[] names = {
        "female", "male"
    };
	
	public static final Gender female = new Gender(FEMALE);
	public static final Gender male = new Gender(MALE);
	
	/**
	 * Returns the list of all class members guaranteeing that no other members
	 * will be created after a call to this method.
	 */
	public static ManagedIndividual[] getEnumerationMembers() {
		return new ManagedIndividual[] {female, male};
	}
	
	/**
	 * Returns the modality with the given URI. 
	 */
	public static ManagedIndividual getIndividualByURI(String instanceURI) {
		return (instanceURI != null
				&&  instanceURI.startsWith(PERSONA_VOCABULARY_NAMESPACE))?
						valueOf(instanceURI.substring(PERSONA_VOCABULARY_NAMESPACE.length()))
						: null;
	}
	
	public static Gender getLevelByOrder(int order) {
        switch (order) {
        case FEMALE: return female;
        case MALE: return male;
        default: return null;
        }
	}
	
	/**
	 * Returns the value of the property <code>rdfs:comment</code> on this <code>owl:Class</code>
	 * from the underlying ontology.
	 */
	public static String getRDFSComment() {
		return "An enumeration for specifying the gender in different contexts";
	}

	/**
	 * Returns the value of the property <code>rdfs:label</code> on this <code>owl:Class</code>
	 * from the underlying ontology.
	 */
	public static String getRDFSLabel() {
		return "Gender";
	}
    
    public static final Gender valueOf(String name) {
        for (int i=FEMALE;  i<=MALE; i++)
            if (names[i].equals(name))
                return getLevelByOrder(i);
        return null;
    }
    
    private int order;
    
 // prevent the usage of the default constructor
    private Gender() {
       
    }
    
    private Gender(int order) {
    	super(PERSONA_VOCABULARY_NAMESPACE + names[order]);
        this.order = order;
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
