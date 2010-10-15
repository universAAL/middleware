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
package de.fhg.igd.ima.persona.location.indoor;

import de.fhg.igd.ima.persona.location.Place;
import de.fhg.igd.ima.persona.shape.Shape;

/**
 * 
 * @author chwirth
 *
 */
public abstract class IndoorPlace extends Place {

	public static final String MY_URI;
	
	static {
		MY_URI = PERSONA_LOCATION_NAMESPACE + "IndoorPlace";
		register(IndoorPlace.class);
	}
		
	/**
	 * Creates a IndoorPlace object
	 * @param uri the object URI
	 */
	public IndoorPlace(String uri) {
		super(uri);
	}
	
	/**
	 * Creates a IndoorPlace object
	 */
	public IndoorPlace() {
		super();
	}
	
	/**
	 * Creates a IndoorPlace object
	 * @param uri this value can also be a null object
	 * @param name The place name. A null object is not allowed
	 */
	public IndoorPlace(String uri, String name) {
		super(uri,name);
	}
	
	public IndoorPlace(String uri,String name,Shape shape) {
		super(uri,name,shape);
	}
	
	public IndoorPlace(String uri,Shape shape) {
		super(uri,shape);
	}
	/**
	 * Returns a human readable description on the essence of this ontology class.
	 */
	public static String getRDFSComment() {
		return "The root class for all indoor places.";
	}
	
	/**
	 * Returns a label with which this ontology class can be introduced to human users.
	 */
	public static String getRDFSLabel() {
		return "IndoorPlace";
	}
	
}
