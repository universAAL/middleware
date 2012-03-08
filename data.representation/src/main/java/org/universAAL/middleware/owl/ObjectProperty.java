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
 * Definition of an OWL object property. The creation is separated from the
 * usage; for every ObjectProperty there is exactly one
 * {@link ObjectPropertySetup} where all the characteristics of this property
 * are defined.
 * 
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ObjectPropertySetup
 * @see org.universAAL.middleware.owl.DatatypeProperty
 * @see org.universAAL.middleware.owl.DatatypePropertySetup
 * @see org.universAAL.middleware.rdf.Property
 * @see org.universAAL.middleware.rdf.PropertySetup
 */
public final class ObjectProperty extends Property {

    /** The URI of this class. */
    public static final String MY_URI = ClassExpression.OWL_NAMESPACE
	    + "ObjectProperty";

    /** Determines whether this property is inverse-functional. */
    private boolean isInverseFunctional = false;

    /** Determines whether this property is reflexive. */
    private boolean isReflexive = false;

    /** Determines whether this property is irreflexive. */
    private boolean isIrreflexive = false;

    /** Determines whether this property is symmetric. */
    private boolean isSymmetric = false;

    /** Determines whether this property is asymmetric. */
    private boolean isAsymmetric = false;

    /** Determines whether this property is transitive. */
    private boolean isTransitive = false;

    /** The inverse of this property. */
    private String inverseOf = null;

    /** The setup interface for this property. */
    private PrivateObjectPropertySetup setup;

    /**
     * Implementation of the setup interface. For security reasons, this is
     * realized as a protected nested class so that only the creator of an
     * {@link Ontology} has access to it and can make changes.
     */
    private class PrivateObjectPropertySetup extends PrivatePropertySetup
	    implements ObjectPropertySetup {
	/** Constructor. */
	public PrivateObjectPropertySetup(ObjectProperty prop) {
	    super(prop);
	}

	/** @see ObjectPropertySetup#setInverseOf(String) */
	public void setInverseOf(String _inverseOf) {
	    inverseOf = _inverseOf;
	}

	/** @see ObjectPropertySetup#setInverseFunctional() */
	public void setInverseFunctional() {
	    isInverseFunctional = true;
	}

	/** @see ObjectPropertySetup#setTransitive() */
	public void setTransitive() {
	    isTransitive = true;
	}

	/** @see ObjectPropertySetup#setSymmetric() */
	public void setSymmetric() {
	    if (!isAsymmetric)
		isSymmetric = true;
	}

	/** @see ObjectPropertySetup#setAsymmetric() */
	public void setAsymmetric() {
	    if (!isSymmetric)
		isAsymmetric = true;
	}

	/** @see ObjectPropertySetup#setReflexive() */
	public void setReflexive() {
	    if (!isIrreflexive)
		isReflexive = true;
	}

	/** @see ObjectPropertySetup#setIrreflexive() */
	public void setIrreflexive() {
	    if (!isReflexive)
		isIrreflexive = true;
	}
    }

    /**
     * Protected constructor, call
     * {@link ObjectProperty#create(String, OntClassInfo)} to create instances.
     */
    protected ObjectProperty(String uri, OntClassInfo info) {
	super(uri, info);
	setup = new PrivateObjectPropertySetup(this);
	super.setup = setup;
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
    public static ObjectPropertySetup create(String propURI, OntClassInfo info) {
	ObjectProperty prop = new ObjectProperty(propURI, info);
	return prop.setup;
    }

    /**
     * Get the inverse of this property.
     * 
     * @see ObjectPropertySetup#setInverseOf(ObjectProperty)
     */
    public ObjectProperty inverseOf() {
	return null;	//TODO: inverseOf;
    }

    /**
     * Determines whether this property is inverse-functional.
     * 
     * @see ObjectPropertySetup#setInverseFunctional()
     */
    public boolean isInverseFunctional() {
	return isInverseFunctional;
    }

    /**
     * Determines whether this property is transitive.
     * 
     * @see ObjectPropertySetup#setTransitive()
     */
    public boolean isTransitive() {
	return isTransitive;
    }

    /**
     * Determines whether this property is symmetric.
     * 
     * @see ObjectPropertySetup#setSymmetric()
     */
    public boolean isSymmetric() {
	return isSymmetric;
    }

    /**
     * Determines whether this property is asymmetric.
     * 
     * @see ObjectPropertySetup#setAsymmetric()
     */
    public boolean isAsymmetric() {
	return isAsymmetric;
    }

    /**
     * Determines whether this property is reflexive.
     * 
     * @see ObjectPropertySetup#setReflexive()
     */
    public boolean isReflexive() {
	return isReflexive;
    }

    /**
     * Determines whether this property is irreflexive.
     * 
     * @see ObjectPropertySetup#setIrreflexive()
     */
    public boolean isIrreflexive() {
	return isIrreflexive;
    }
}
