package org.universAAL.middleware.owl;

public abstract class PrivateResource extends ManagedIndividual {
    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "PrivateResource";

    protected PrivateResource() {
	super();
    }

    public String getClassURI() {
	return MY_URI;
    }

    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_FULL;
    }

    public boolean isWellFormed() {
	return true;
    }
    
}