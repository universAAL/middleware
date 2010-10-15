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
 * A location is the ontologic object that represents a physical thing position in the space.
 * A location can have 3 properties: HAS_POSITION, IS_IN PLACE, and IS_NEAR_ADDRESS.
 * A location position can be absolute (i.e. a GPS global position) or relative (in such case
 * the referring system is related to the place indicated in IS_IN_PLACE attribute). It is
 * possible to indicate a location without using a position, but in this case the IS_IN_PLACE
 * attribute must be present.
 * IS_IN_PLACE represents the place when the subject is located. There are 4 different kind of
 * places (please read PLACE ontology).
 * IS_NEAR_ADDRESS represents a physical address near to the location. This attribute doesn't
 * state that the location is inside the building, but it can be also used for outdoor
 * locations.
 * 
 * 
 * 
 * @author smazzei
 */
public abstract class Location extends ComparableIndividual {
	public static final String MY_URI;
	
	
	static {
		MY_URI = PERSONA_VOCABULARY_NAMESPACE + "Location";
		register(Location.class);
	}
	
	protected Location(String uri) {
		super(uri);
	}
	
	protected Location() {
		super();
	}
	
	/**
	 * Returns a human readable description on the essence of this ontology class.
	 */
	public static String getRDFSComment() {
		return "The root class for all locations.";
	}
	
	/**
	 * Returns a label with which this ontology class can be introduced to human users.
	 */
	public static String getRDFSLabel() {
		return "Location";
	}
	
	/**
	 * Returns the distance (meters) between the current location and the argument location.
	 * This is an estimation of the actual distance between locations.
	 * The following cases are possible (let's consider the distance between location A and 
	 * location B):
	 * <ol> <li> A and B "has_position" attribute is present and it is an
	 * <u>absolute position</u>. The method returns the distance between A and B GPS
	 * positions applying Euler's theorem (i.e. ignoring altitude).
	 * <li>A and B "has_position" attribute is present and it is a
	 * <u>relative position</u>. In this case there are two options:
	 * <ul><li>A and B <i>his_in_place </i> attributes are both present and they have the same
	 * global position (i.e. they refer to the same coordinate system). Compute the distance
	 * between A and B using their relative positions and Manhattan distance algorithm.
	 * For buildings with more than one floor, lifts / stairs positions are taken in account.
	 * <li> A and B <i>his_in_place </i> attributes are both present and they have different
	 * global positions. In this case the method takes in account only the places global
	 * positions and compute their distance using Euler's theorem.</ul>
	 * A and B "has position" attribute is NOT present, but "is in place" attribute is present.
	 * Calculate d(A,B) using their "is in place" places coordinates.
	 * <li> In any other case A or B are not well formed and the method returns
	 * NOT_COMPUTABLE_DISTANCE.
	 */
	public abstract float getDistanceTo(Location other);
	/**
	 * @return <b>true</b> if two locations are in connected places.
	 * If at least one location doesn't have "is in place" property the method returns <b>false</b>.
	 */
	public abstract boolean hasConnectionTo(Location other);
	
	
	/**
	 * @return <b>true</b> if two locations are in adjacent places.
	 * If at least one location doesn't have "is in place" property the method returns <b>false</b>.
	 */
	public abstract boolean isAdjacentTo(Location other);
	
}
