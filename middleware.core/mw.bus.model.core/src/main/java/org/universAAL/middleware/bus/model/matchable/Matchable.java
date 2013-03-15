/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung

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
package org.universAAL.middleware.bus.model.matchable;

/**
 * Represents the concept of matchable Classes.
 * 
 * Implementing Classes must specify how to match against other matchable
 * Classes.
 * 
 * @author Dominik Schreiber <ow91fibo@rbg.informatik.tu-darmstadt.de>
 */
public interface Matchable {
	/**
	 * @param other
	 * @return <tt>true</tt> if the other Matchable matches, <tt>false</tt> if
	 *         not
	 */
	public boolean matches(Matchable other);
}
