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
package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.Property;

/**
 * Definition of an OWL datatype property. The creation is separated from the
 * usage; for every DatatypeProperty there is exactly one
 * {@link DatatypePropertySetup} where all the characteristics of this property
 * are defined.
 * 
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ObjectProperty
 * @see org.universAAL.middleware.owl.ObjectPropertySetup
 * @see org.universAAL.middleware.owl.DatatypePropertySetup
 * @see org.universAAL.middleware.rdf.Property
 * @see org.universAAL.middleware.rdf.PropertySetup
 */
public final class DatatypeProperty extends Property {

    /** The URI of this class. */
    public static final String MY_URI = TypeExpression.OWL_NAMESPACE
	    + "DatatypeProperty";

    /**
     * Implementation of the setup interface. For security reasons, this is
     * realized as a protected nested class so that only the creator of an
     * {@link Ontology} has access to it and can make changes.
     */
    private class PrivateDataTypePropertySetup extends PrivatePropertySetup
	    implements DatatypePropertySetup {
	/** Constructor. */
	public PrivateDataTypePropertySetup(Property prop) {
	    super(prop);
	}
    }

    /**
     * Protected constructor, call
     * {@link DatatypeProperty#create(String, OntClassInfo)} to create
     * instances.
     */
    protected DatatypeProperty(String uri, OntClassInfo info) {
	super(uri, info);
	setup = new PrivateDataTypePropertySetup(this);
	addType(MY_URI, true);
    }

    /**
     * Create a new instance.
     * 
     * @param propURI
     *            URI of the property.
     * @param info
     *            The class for which this property is defined.
     * @return The setup interface to set all information of this property.
     */
    public static DatatypePropertySetup create(String propURI, OntClassInfo info) {
	DatatypeProperty prop = new DatatypeProperty(propURI, info);
	return (DatatypePropertySetup) prop.setup;
    }
}
