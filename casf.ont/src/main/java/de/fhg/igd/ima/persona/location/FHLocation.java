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
/** Superclass for all locations that are defined according to the Fraunhofer location ontology.
 *  New implementations need to be added to the Activator.loadClasses() list.
 * 
 */
package de.fhg.igd.ima.persona.location;

import java.util.List;
import java.util.Vector;

import org.persona.middleware.TypeMapper;
import org.persona.ontology.ComparableIndividual;
import org.persona.ontology.Location;
import org.persona.ontology.ManagedIndividual;
import org.persona.ontology.expr.Restriction;


/**
 * @author chwirth
 */
public class FHLocation extends Location {

	public static final String PERSONA_LOCATION_NAMESPACE = "http://ontology.persona.ima.igd.fhg.de/Location.owl#";
	public static final String MY_URI;

	public static final String PROP_HAS_NAME;
	public static final String PROP_IS_ADJACENT_TO;
	public static final String PROP_IS_CONNECTED_TO;
	public static final String PROP_IS_CONTAINED_IN;
	public static final String PROP_CONTAINS;

	static {
		MY_URI = PERSONA_LOCATION_NAMESPACE + "FHLocation";
		PROP_HAS_NAME = PERSONA_LOCATION_NAMESPACE + "hasName";
		PROP_IS_ADJACENT_TO =PERSONA_LOCATION_NAMESPACE + "isAdjacentTo";
		PROP_IS_CONNECTED_TO =PERSONA_LOCATION_NAMESPACE + "isConnectedTo";
		PROP_IS_CONTAINED_IN = PERSONA_LOCATION_NAMESPACE + "isContainedIn";
		PROP_CONTAINS = PERSONA_LOCATION_NAMESPACE + "contains";
		register(FHLocation.class);
	}

	public static Restriction getClassRestrictionsOnProperty(String propURI) {

		if (PROP_HAS_NAME.equals(propURI))
			return Restriction.getAllValuesRestrictionWithCardinality(propURI,
					TypeMapper.getDatatypeURI(String.class),1, 0);
		if (PROP_IS_ADJACENT_TO.equals(propURI))
			return Restriction.getAllValuesRestriction(propURI,
					FHLocation.MY_URI);
		if (PROP_IS_CONNECTED_TO.equals(propURI))
			return Restriction.getAllValuesRestriction(propURI,
					FHLocation.MY_URI);
		if (PROP_IS_CONTAINED_IN.equals(propURI))
			return Restriction.getAllValuesRestriction(propURI,
					FHLocation.MY_URI);
		if (PROP_CONTAINS.equals(propURI))
			return Restriction.getAllValuesRestriction(propURI,
					FHLocation.MY_URI);
		return ManagedIndividual.getClassRestrictionsOnProperty(propURI);
	}

	public static String[] getStandardPropertyURIs() {
		String[] inherited = Location.getStandardPropertyURIs();
		String[] toReturn = new String[inherited.length+5];
		int i = 0;
		while (i < inherited.length) {
			toReturn[i] = inherited[i];
			i++;
		}
		toReturn[i++] = PROP_HAS_NAME;
		toReturn[i++] = PROP_IS_ADJACENT_TO;
		toReturn[i++] = PROP_IS_CONNECTED_TO;
		toReturn[i++] = PROP_IS_CONTAINED_IN;
		toReturn[i] = PROP_CONTAINS;
		return toReturn;
	}

	/**
	 * Constructor just for usage by de-serializers. Do not use this constructor
	 * within applications as it may lead to incomplete instances that cause exceptions.
	 */
	public FHLocation() {
		super();
	}

	/**
	 * Constructor just for usage by de-serializers. Do not use this constructor
	 * within applications as it may lead to incomplete instances that cause exceptions.
	 */
	public FHLocation(String uri) {
		super(uri);
	}

	/**
	 * Creates a new Location object
	 * @param uri The Location instance URI
	 * @param name The name of the location
	 */
	public FHLocation(String uri, String name) {
		super(uri);
		if (name == null)
			throw new IllegalArgumentException();

		props.put(PROP_HAS_NAME, name);
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
		return "FHLocation";
	}

	/**
	 * Add a new FHLocation in the given property list
	 * @param propURI the property
	 * @param location If it is null no property is added.
	 * @return false if no property is added.
	 */
	private boolean addMultipleValueFHLocationProperty(String propURI, FHLocation location){
		if (location==null || (propURI!=PROP_IS_ADJACENT_TO && propURI!=PROP_IS_CONNECTED_TO && propURI!=PROP_CONTAINS))
			return false;
		List locations;
		if (props.containsKey(propURI)){
			locations=(List)props.get(propURI);
			if (locations.contains(location))
				return false;
		}
		else
			locations= new Vector();
		locations.add(location);
		props.put(propURI, locations);
		return true;
	}

	/**
	 * Returns an array of all FHLocations that are value of the given property
	 * @param propURI
	 * @return
	 */
	private FHLocation[] getMultipleValueFHLocationProperty(String propURI) {
		if (!props.containsKey(propURI)) return null;
		List conn= (List)props.get(propURI);
		return (FHLocation[])conn.toArray(new FHLocation[0]);
	}

	/**
	 * Removes a FHLocation value in a value list of the given property
	 * @param propURI 
	 * @param location The location to remove, must not be null.
	 * @return false if the location was not removed or not valid
	 */
	private boolean removeMulitpleValueFHLocationProperty(String propURI,FHLocation location) {
		if (!props.containsKey(propURI) || location==null || (propURI!=PROP_IS_ADJACENT_TO && propURI!=PROP_IS_CONNECTED_TO && propURI!=PROP_IS_CONTAINED_IN && propURI!=PROP_CONTAINS))
			return false;
		List locations;
		if (props.containsKey(propURI)){
			locations=(List)props.get(propURI);
			if (!locations.contains(location))
				return false;
		} else return false;
		locations.remove(location);
		props.put(propURI, location);
		return true;	
	}

	/**
	 * 
	 * @return The value of "is adjacent to" property. If the property is not present a null
	 * object is returned.
	 */
	public FHLocation[] getAdjacentLocations(){
		return getMultipleValueFHLocationProperty(PROP_IS_ADJACENT_TO);
	}

	/**
	 * Set the "is adjacent to" attribute.
	 * @param location A null value is not admitted.
	 */
	public void addAdjacentLocation(FHLocation location){
		if (location==null) throw new IllegalArgumentException();
		addMultipleValueFHLocationProperty(PROP_IS_ADJACENT_TO,location);
	}

	/**
	 * removes a location from the "is adjacent to" attribute.
	 * 
	 */
	public void removeAdjacentLocation(FHLocation location) {
		if (location==null) throw new IllegalArgumentException();
		removeMulitpleValueFHLocationProperty(PROP_IS_ADJACENT_TO,location);
	}

	/**
	 * removes all adjacent locations from this location.
	 */
	public void clearAdjacentLocations() {
		props.put(PROP_IS_ADJACENT_TO, new Vector());
	}

	/**
	 * 
	 * @return The value of "is connected to" property. If the property is not present a null
	 * object is returned.
	 */
	public FHLocation[] getConnectedLocations(){
		return getMultipleValueFHLocationProperty(PROP_IS_CONNECTED_TO);
	}

	/**
	 * Adds a value to the "is connected to" attribute.
	 * @param location A null value is not admitted.
	 */
	public void addConnectedLocation(FHLocation location){
		if (location==null) throw new IllegalArgumentException();
		addMultipleValueFHLocationProperty(PROP_IS_CONNECTED_TO,location);
	}

	/**
	 * removes a location from the "is connected to" attribute.
	 * 
	 */
	public void removeConnectedLocation(FHLocation location) {
		if (location==null) throw new IllegalArgumentException();
		removeMulitpleValueFHLocationProperty(PROP_IS_CONNECTED_TO,location);
	}

	/**
	 * removes all connected locations from this location.
	 */
	public void clearConnectedLocations() {
		props.put(PROP_IS_CONNECTED_TO, new Vector());
	}

	/**
	 * 
	 * @return The value of "contains" property. If the property is not present a null
	 * object is returned.
	 */
	public FHLocation[] getContainedLocations(){
		return getMultipleValueFHLocationProperty(PROP_CONTAINS);
	}

	/**
	 * Adds a value to the "contains" attribute.
	 * it also sets the location.containedBy property if not set.
	 * @param location A null value is not admitted.
	 */
	public void addContainedLocation(FHLocation location){
		if (location==null) throw new IllegalArgumentException();
		
		addMultipleValueFHLocationProperty(PROP_CONTAINS,location);
		
		location.setContainingLocation(this);
	}

	/**
	 * removes a location from the "contains" attribute.
	 * 
	 */
	public void removeContainedLocation(FHLocation location) {
		if (location==null) throw new IllegalArgumentException();
		removeMulitpleValueFHLocationProperty(PROP_CONTAINS,location);
	}

	/**
	 * removes all contained locations from this location.
	 */
	public void clearContainedLocations() {
		props.put(PROP_CONTAINS, new Vector());
	}

	/**
	 * 
	 * @return The value of "is contained in" property. If the property is not present a null
	 * object is returned.
	 */
	public FHLocation getContainingLocation(){
		return (FHLocation) props.get(PROP_IS_CONTAINED_IN);
	}

	/**
	 * Sets the "is contained in" attribute.
	 * @param location A null value is not admitted.
	 */
	public void setContainingLocation(FHLocation location){
		if (location==null) throw new IllegalArgumentException();
		props.put(PROP_IS_CONTAINED_IN,location);
	}


	/**
	 * removes all containing locations from this location.
	 */
	public void clearContainingLocations() {
		props.put(PROP_IS_CONTAINED_IN, null);
	}

	public int getPropSerializationType(String propURI) {
		if (PROP_IS_CONNECTED_TO.equals(propURI)
				||PROP_HAS_NAME.equals(propURI)
				||PROP_IS_ADJACENT_TO.equals(propURI)
				||PROP_IS_CONTAINED_IN.equals(propURI)
				||PROP_CONTAINS.equals(propURI))
			return PROP_SERIALIZATION_REDUCED;

		return PROP_SERIALIZATION_OPTIONAL;
	}

//	private boolean contains(FHLocation[] locs, FHLocation loc) {
//		for(int i=0;i<locs.length;i++) {
//			if(locs[i].equals(loc)) return true;
//		}
//		return false;
//	}

	public int compareTo(Object arg0) {
		if (this.equals(arg0))
			return 0;
		
		if (arg0 instanceof FHLocation) {
			FHLocation currContaining=this.getContainingLocation();
			FHLocation compare = (FHLocation)arg0;
			while(currContaining != null) {
				if(currContaining.equals(compare))
					return -1;
				currContaining = currContaining.getContainingLocation();
			}
			
			currContaining=compare.getContainingLocation();
			compare = this;
			while(currContaining != null) {
				if(currContaining.equals(compare))
					return 1;
				currContaining = currContaining.getContainingLocation();
			}
			
			if (recursiveCompare(getContainedLocations(), (FHLocation) arg0))
				return 1;
			
			if (recursiveCompare(((FHLocation) arg0).getContainedLocations(), this))
				return -1;
		}
		
		return NON_COMPARABLE_INDIVIDUALS;
	}
	
	private boolean recursiveCompare(FHLocation[] current,FHLocation compare) {
		if (current != null) {
			for(int i=0;i<current.length;i++) {
				if(current[i].equals(compare)) return true;
				if(recursiveCompare(current[i].getContainedLocations(),compare)) return true;
			}
		}	
		return false;
	}

	public boolean hasConnectionTo(Location arg0) {
		List connected = (List)props.get(PROP_IS_CONNECTED_TO);
		return connected.contains(arg0);
	}

	public boolean isAdjacentTo(Location arg0) {
		List connected = (List)props.get(PROP_IS_ADJACENT_TO);
		return connected.contains(arg0);
	}

	public ComparableIndividual getNext() {
		return getContainingLocation();
	}

	public ComparableIndividual getPrevious() {
		FHLocation[] children = getContainedLocations();
		if (children != null  &&  children.length > 0) {
			// direct children of a location are normally not comparable to each other
			// so, simply pick the first child
			return children[0];
		}
		return null;
	}

	public int ord() {
		// TODO Auto-generated method stub
		return Integer.MIN_VALUE;
	}

	public float getDistanceTo(Location other) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
