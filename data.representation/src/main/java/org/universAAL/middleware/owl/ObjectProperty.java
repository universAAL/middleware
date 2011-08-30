package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.Property;

public abstract class ObjectProperty extends Property {

    protected boolean isInverseFunctional;
    protected boolean isReflexive;
    protected boolean isSymmetric;
    protected boolean isTransitive;
    protected ObjectProperty inverseOf;
    

    protected ObjectProperty(Object password, String uri) {
	super(password, uri);
    }
    
    
    public ObjectProperty inverseOf() {
	return inverseOf;
    }
    
    public void setInverseOf(Object password, ObjectProperty inverseOf)
	    throws IllegalAccessException {
	if (!(this.password.equals(password)))
	    throw new IllegalAccessException(
		    "The specified password is not correct.");
	this.inverseOf = inverseOf;
    }
    
    
    public boolean isInverseFunctional() {
	return isInverseFunctional;
    }
    
    public void setInverseFunctional(Object password, boolean isInverseFunctional)
	    throws IllegalAccessException {
	if (!(this.password.equals(password)))
	    throw new IllegalAccessException(
		    "The specified password is not correct.");
	this.isInverseFunctional = isInverseFunctional;
    }
    
    
    public boolean isTransitive() {
	return isTransitive;
    }
    
    public void setTransitive(Object password, boolean isTransitive)
	    throws IllegalAccessException {
	if (!(this.password.equals(password)))
	    throw new IllegalAccessException(
		    "The specified password is not correct.");
	this.isTransitive = isTransitive;
    }
    
    
    public boolean isSymmetric() {
	return isSymmetric;
    }
    
    public void setSymmetric(Object password, boolean isSymmetric)
	    throws IllegalAccessException {
	if (!(this.password.equals(password)))
	    throw new IllegalAccessException(
		    "The specified password is not correct.");
	this.isSymmetric = isSymmetric;
    }
    
    
    public boolean isReflexive() {
	return isReflexive;
    }
    
    public void setReflexive(Object password, boolean isReflexive)
	    throws IllegalAccessException {
	if (!(this.password.equals(password)))
	    throw new IllegalAccessException(
		    "The specified password is not correct.");
	this.isReflexive = isReflexive;
    }
}
