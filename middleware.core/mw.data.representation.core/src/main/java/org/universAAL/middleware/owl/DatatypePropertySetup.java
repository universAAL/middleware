/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
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
package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.PropertySetup;

/**
 * <p>
 * Setup interface for creating new datatype properties. The creation is
 * separated from the usage; for every {@link DatatypeProperty} there is exactly
 * one DatatypePropertySetup where all the characteristics of this property are
 * defined.
 * </p>
 * <p>
 * To create a new {@link DatatypeProperty}, define a subclass of
 * {@link Ontology} , overwrite the {@link Ontology#create()} method, create an
 * {@link OntClassInfo} and call
 * {@link OntClassInfoSetup#addDatatypeProperty(String)}.
 * </p>
 * <p>
 * There are no specific properties/methods for datatype properties. This
 * interface is empty and only exists as equivalent for
 * {@link ObjectPropertySetup}.
 * </p>
 * 
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ObjectProperty
 * @see org.universAAL.middleware.owl.ObjectPropertySetup
 * @see org.universAAL.middleware.owl.DatatypeProperty
 * @see org.universAAL.middleware.rdf.Property
 * @see org.universAAL.middleware.rdf.PropertySetup
 */
public interface DatatypePropertySetup extends PropertySetup {
}
