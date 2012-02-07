package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.Property;

public final class ObjectProperty extends Property {

    public static final String MY_URI = ClassExpression.OWL_NAMESPACE
	    + "ObjectProperty";

    private boolean isInverseFunctional = false;
    private boolean isReflexive = false;
    private boolean isIrreflexive = false;
    private boolean isSymmetric = false;
    private boolean isAsymmetric = false;
    private boolean isTransitive = false;
    private ObjectProperty inverseOf = null;

    private PrivateObjectPropertySetup setup;

    private class PrivateObjectPropertySetup extends PrivatePropertySetup
	    implements ObjectPropertySetup {
	ObjectProperty prop;

	public PrivateObjectPropertySetup(ObjectProperty prop) {
	    super(prop);
	    this.prop = prop;
	}

	public void setInverseOf(ObjectProperty inverseOf) {
	    prop.inverseOf = inverseOf;
	}

	public void setInverseFunctional() {
	    isInverseFunctional = true;
	}

	public void setTransitive() {
	    isTransitive = true;
	}

	public void setSymmetric() {
	    if (!isAsymmetric)
		isSymmetric = true;
	}

	public void setAsymmetric() {
	    if (!isSymmetric)
		isAsymmetric = true;
	}

	public void setReflexive() {
	    if (!isIrreflexive)
		isReflexive = true;
	}

	public void setIrreflexive() {
	    if (!isReflexive)
		isIrreflexive = true;
	}
    }

    protected ObjectProperty(String uri, OntClassInfo info) {
	super(uri, info);
	setup = new PrivateObjectPropertySetup(this);
	super.setup = setup;
	addType(MY_URI, true);
    }

    public static ObjectPropertySetup create(String propURI, OntClassInfo info) {
	ObjectProperty prop = new ObjectProperty(propURI, info);
	return prop.setup;
    }

    public ObjectProperty inverseOf() {
	return inverseOf;
    }

    public boolean isInverseFunctional() {
	return isInverseFunctional;
    }

    public boolean isTransitive() {
	return isTransitive;
    }

    public boolean isSymmetric() {
	return isSymmetric;
    }

    public boolean isAsymmetric() {
	return isAsymmetric;
    }

    public boolean isReflexive() {
	return isReflexive;
    }

    public boolean isIrreflexive() {
	return isIrreflexive;
    }
}
