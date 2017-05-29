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

import org.universAAL.middleware.rdf.Property;
import org.universAAL.middleware.rdf.Resource;

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
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "This is implemented in Resource based on URI and props.")
public final class ObjectProperty extends Property {

    /** The URI of this class. */
    public static final String MY_URI = TypeExpression.OWL_NAMESPACE
	    + "ObjectProperty";

    /** @see ObjectPropertySetup#setInverseFunctional() */
    public static final String TYPE_INVERSE_FUNCTIONAL = ManagedIndividual.OWL_NAMESPACE
	    + "InverseFunctionalProperty";

    /** @see ObjectPropertySetup#setReflexive() */
    public static final String TYPE_REFLEXIVE = ManagedIndividual.OWL_NAMESPACE
	    + "ReflexiveProperty";

    /** @see ObjectPropertySetup#setIrreflexive() */
    public static final String TYPE_IRREFLEXIVE = ManagedIndividual.OWL_NAMESPACE
	    + "IrreflexiveProperty";

    /** @see ObjectPropertySetup#setSymmetric() */
    public static final String TYPE_SYMMETRIC = ManagedIndividual.OWL_NAMESPACE
	    + "SymmetricProperty";

    /** @see ObjectPropertySetup#setAsymmetric() */
    public static final String TYPE_ASYMMETRIC = ManagedIndividual.OWL_NAMESPACE
	    + "AsymmetricProperty";

    /** @see ObjectPropertySetup#setTransitive() */
    public static final String TYPE_TRANSITIVE = ManagedIndividual.OWL_NAMESPACE
	    + "TransitiveProperty";

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

	public void setInverseOf(String _inverseOf) {
	    inverseOf = _inverseOf;
	}

	public void setInverseFunctional() {
	    isInverseFunctional = true;
	    addType(TYPE_INVERSE_FUNCTIONAL, false);
	}

	public void setTransitive() {
	    isTransitive = true;
	    addType(TYPE_TRANSITIVE, false);
	}

	public void setSymmetric() {
	    if (!isAsymmetric) {
		isSymmetric = true;
		addType(TYPE_SYMMETRIC, false);
	    }
	}

	public void setAsymmetric() {
	    if (!isSymmetric) {
		isAsymmetric = true;
		addType(TYPE_ASYMMETRIC, false);
	    }
	}

	public void setReflexive() {
	    if (!isIrreflexive) {
		isReflexive = true;
		addType(TYPE_REFLEXIVE, false);
	    }
	}

	public void setIrreflexive() {
	    if (!isReflexive) {
		isIrreflexive = true;
		addType(TYPE_IRREFLEXIVE, false);
	    }
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
	addType(MY_URI, false);
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
     * @see ObjectPropertySetup#setInverseOf(String)
     */
    public ObjectProperty inverseOf() {
	return null; // TODO: inverseOf;
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

    @Override
    public boolean setProperty(String propURI, Object value) {
	if (Resource.PROP_RDF_TYPE.equals(propURI)) {
	    if (containsType(TYPE_ASYMMETRIC, value))
		setup.setAsymmetric();
	    if (containsType(TYPE_INVERSE_FUNCTIONAL, value))
		setup.setInverseFunctional();
	    if (containsType(TYPE_IRREFLEXIVE, value))
		setup.setIrreflexive();
	    if (containsType(TYPE_REFLEXIVE, value))
		setup.setReflexive();
	    if (containsType(TYPE_SYMMETRIC, value))
		setup.setSymmetric();
	    if (containsType(TYPE_TRANSITIVE, value))
		setup.setTransitive();
	}
	return super.setProperty(propURI, value);
    }
}
