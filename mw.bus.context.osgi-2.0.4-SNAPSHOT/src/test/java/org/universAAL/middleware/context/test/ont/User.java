package org.universAAL.middleware.context.test.ont;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.Resource;

public class User extends ManagedIndividual {

    public static final String MY_URI = TestOntology.NAMESPACE + "User";
    public static final String PROP_PHYSICAL_LOCATION = TestOntology.NAMESPACE
	    + "PROP_PHYSICAL_LOCATION";

    public User(String uri) {
	super(uri);
    }

    public int getPropSerializationType(String propURI) {
	return Resource.PROP_SERIALIZATION_FULL;
    }

    public void setLocation(Location location) {
	if (location != null)
	    setProperty(PROP_PHYSICAL_LOCATION, location);
    }

    public String getClassURI() {
	return MY_URI;
    }
}
