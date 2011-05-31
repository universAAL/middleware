/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.owl.supply;

import org.universAAL.middleware.owl.ComparableIndividual;


/**
 * A very abstract representation of locations just at a level needed by the middleware.
 * It is considered as crucial that in the realization of the brokerage mechanisms,
 * location is used as one of the non-functional parameters for selecting a server
 * or a client, e.g. being placed in a location or having the smallest distance to a
 * location.
 * 
 * By defining Location as a subclass of {@link org.universAAL.middleware.owl.ComparableIndividual}
 * we are implicitly defining a transitional property 'is in' with the following semantic:
 * <ul>
 * <li>loc<sub>1</sub> &lt;&nbsp;loc<sub>2</sub> &rArr; loc<sub>1</sub> is in loc<sub>2</sub></li>
 * <li>loc<sub>1</sub> &gt;&nbsp;loc<sub>2</sub> &rArr; loc<sub>2</sub> is in loc<sub>1</sub></li>
 * <li>loc<sub>1</sub> = loc<sub>2</sub> &rArr; both of the above are true</li>
 * </ul>
 * However, it should be clear that not all locations are comparable with each other; this implies
 * that 'is in' defines just a partial order between locations.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * @author cstockloew
 */
public abstract class AbsLocation extends ComparableIndividual {
	
	/**
	 * The URI of the ontology class.
	 * @see org.universAAL.middleware.owl.ManagedIndividual#MY_URI
	 */
	public static final String MY_URI;
	
	
	/**
	 * Register all necessary information.
	 */
	static {
		MY_URI = uAAL_VOCABULARY_NAMESPACE + "AbsLocation";
		register(AbsLocation.class);
	}

	/**
	 * Create an abstract location.
	 * @param uri URI of the location.
	 */
	protected AbsLocation(String uri) {
		super(uri);
	}
	
	/**
	 * Constructor just for usage by de-serializers. Do not use this constructor
	 * within applications as it may lead to incomplete instances that cause
	 * exceptions.
	 */
	protected AbsLocation() {
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
		return "Abstract Location";
	}
	
	/**
	 * Returns the distance (meters) between the current location and the argument location.
	 */
	public abstract float getDistanceTo(AbsLocation other);
	
}
