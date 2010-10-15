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
/**
 * 
 */
package org.persona.platform.casf.ontology;

import org.persona.middleware.TypeMapper;
import org.persona.ontology.Location;
import org.persona.ontology.ManagedIndividual;
import org.persona.ontology.expr.Restriction;
import org.persona.platform.casf.ontology.PhysicalContainer;

import de.fhg.igd.ima.persona.shape.Shape;

/**
 * Represents the class of physical things that are supposed to have a location.
 * 
 * @author mtazari
 *
 */
public class PhysicalThing extends ManagedIndividual {
	public static final String MY_URI;
	public static final String PROP_CARRIED_BY;
	public static final String PROP_PART_OF;
	public static final String PROP_IS_IN;
	public static final String PROP_IS_PORTABLE;
	public static final String PROP_IS_STATIONARY;
	public static final String PROP_PHYSICAL_LOCATION;
	public static final String PROP_HAS_SHAPE;

	static {
		MY_URI = PERSONA_VOCABULARY_NAMESPACE + "PhysicalThing";
		PROP_CARRIED_BY = PERSONA_VOCABULARY_NAMESPACE + "carriedBy";
		PROP_IS_IN = PERSONA_VOCABULARY_NAMESPACE + "isIn";
		PROP_IS_PORTABLE = PERSONA_VOCABULARY_NAMESPACE + "isPortable";
		PROP_IS_STATIONARY = PERSONA_VOCABULARY_NAMESPACE + "isStationary";
		PROP_PART_OF = PERSONA_VOCABULARY_NAMESPACE + "partOf";
		PROP_PHYSICAL_LOCATION = PERSONA_VOCABULARY_NAMESPACE + "hasLocation";
		PROP_HAS_SHAPE = PERSONA_VOCABULARY_NAMESPACE + "hasShape";
		register(PhysicalThing.class);
	}

	public static Restriction getClassRestrictionsOnProperty(String propURI) {
		if (PROP_CARRIED_BY.equals(propURI) || PROP_PART_OF.equals(propURI))
			return Restriction.getAllValuesRestrictionWithCardinality(
					propURI, MY_URI, 1, 0);
		if (PROP_IS_IN.equals(propURI))
			return Restriction.getAllValuesRestrictionWithCardinality(
					propURI, PhysicalContainer.MY_URI, 1, 0);
		if (PROP_IS_PORTABLE.equals(propURI) || PROP_IS_STATIONARY.equals(propURI))
			return Restriction.getAllValuesRestrictionWithCardinality(
					propURI, TypeMapper.getDatatypeURI(Boolean.class), 1, 1);
		if (PROP_PHYSICAL_LOCATION.equals(propURI))
			return Restriction.getAllValuesRestrictionWithCardinality(propURI,
					Location.MY_URI, 1, 0);
		if (PROP_HAS_SHAPE.equals(propURI))
			return Restriction.getAllValuesRestrictionWithCardinality(propURI,
					Shape.MY_URI, 1, 0);
		return ManagedIndividual.getClassRestrictionsOnProperty(propURI);
	}

	public static String[] getStandardPropertyURIs() {
		return new String[] {
				PROP_CARRIED_BY,
				PROP_IS_IN,
				PROP_IS_PORTABLE,
				PROP_IS_STATIONARY,
				PROP_PART_OF,
				PROP_PHYSICAL_LOCATION,
				PROP_HAS_SHAPE
		};
	}

	/**
	 * Returns a human readable description on the essence of this ontology class.
	 */
	public static String getRDFSComment() {
		return "The root class for all physical things in the PERSONA ontology. Physical things have a location";
	}

	/**
	 * Returns a label with which this ontology class can be introduced to human users.
	 */
	public static String getRDFSLabel() {
		return "Physical Thing";
	}

	protected PhysicalThing() {
		super();
	}

	public PhysicalThing(String uri) {
		super(uri);
	}

	protected PhysicalThing(String uriPrefix, int numProps) {
		super(uriPrefix, numProps);
	}

	public Location getLocation() {
		return (Location) props.get(PROP_PHYSICAL_LOCATION);
	}

	public Shape getShape() {
		return (Shape) props.get(PROP_HAS_SHAPE);
	}

	/**
	 * From the point of view of this top most class of things with a location, the location
	 * can be represented in its reduced form. As the class has no other property, for all
	 * other input, we return {@link ManagedIndividual#PROP_SERIALIZATION_OPTIONAL}. 
	 * 
	 * @see ManagedIndividual#getPropSerializationType(String).
	 */
	public int getPropSerializationType(String propURI) {
		if (PROP_PHYSICAL_LOCATION.equals(propURI) 
				|| PROP_HAS_SHAPE.equals(propURI))
			return PROP_SERIALIZATION_REDUCED;
		return PROP_SERIALIZATION_OPTIONAL;
	}
	
	public void setLocation(Location loc) {
		if(loc == null)throw new IllegalArgumentException();
		props.put(PROP_PHYSICAL_LOCATION, loc);
	}
	
	public void setShape(Shape o) {
		if (o != null)
			props.put(PROP_HAS_SHAPE, o);
	}

	public void setProperty(String propURI, Object o) {
		if (PROP_PHYSICAL_LOCATION.equals(propURI)  &&  o instanceof Location)
			setLocation((Location) o);
		else
			super.setProperty(propURI, o);
	}
}
