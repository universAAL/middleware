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
package de.fhg.igd.ima.persona.service;

import java.util.Hashtable;

import org.persona.ontology.Service;
import org.persona.ontology.expr.Restriction;

import de.fhg.igd.ima.persona.FHPhysicalThing;
import de.fhg.igd.ima.persona.location.FHLocation;

public class SpaceConfigurationService extends Service {
	public static final String MY_URI;
	public static final String PROP_MANAGED_LOCATIONS;
	public static final String PROP_MANAGED_PHYSICAL_THINGS;
	
	private static Hashtable locationRestrictions = new Hashtable(1);
	static {
		MY_URI = FHLocation.PERSONA_LOCATION_NAMESPACE + "locationConfigurationService";
		PROP_MANAGED_LOCATIONS = FHLocation.PERSONA_LOCATION_NAMESPACE + "managedLocations";
		PROP_MANAGED_PHYSICAL_THINGS = FHLocation.PERSONA_LOCATION_NAMESPACE + "managedPhysicalThings";
		register(SpaceConfigurationService.class);
		addRestriction(
				Restriction.getAllValuesRestriction(PROP_MANAGED_LOCATIONS, FHLocation.MY_URI),
				new String[] {PROP_MANAGED_LOCATIONS},
				locationRestrictions);
		addRestriction(
				Restriction.getAllValuesRestriction(PROP_MANAGED_PHYSICAL_THINGS, FHPhysicalThing.MY_URI),
				new String[] {PROP_MANAGED_PHYSICAL_THINGS},
				locationRestrictions);
	}
	
	public static Restriction getClassRestrictionsOnProperty(String propURI) {
		if (propURI == null)
			return null;
		Object r = locationRestrictions.get(propURI);
		if (r instanceof Restriction)
			return (Restriction) r;
		return Service.getClassRestrictionsOnProperty(propURI);
	}
	
	public static String getRDFSComment() {
		return "The class of services controlling locations.";
	}
	
	public static String getRDFSLabel() {
		return "LocationService";
	}
	
	public SpaceConfigurationService() {
		super();
	}
	
	public SpaceConfigurationService(String uri) {
		super(uri);
	}

	/* (non-Javadoc)
	 * @see org.persona.ontology.Service#getClassLevelRestrictions()
	 */
	protected Hashtable getClassLevelRestrictions() {
		return locationRestrictions;
	}

	/* (non-Javadoc)
	 * @see org.persona.ontology.ManagedIndividual#getPropSerializationType(java.lang.String)
	 */
	public int getPropSerializationType(String propURI) {
		return (PROP_MANAGED_LOCATIONS.equals(propURI)) ? PROP_SERIALIZATION_FULL
				: PROP_SERIALIZATION_REDUCED;
	}

	public boolean isWellFormed() {
		return true;
	}
}
