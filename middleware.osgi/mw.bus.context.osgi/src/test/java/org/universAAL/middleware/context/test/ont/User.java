package org.universAAL.middleware.context.test.ont;

import org.universAAL.middleware.owl.ManagedIndividual;

public class User extends ManagedIndividual {

    public static final String MY_URI = "org.universAAL.middleware.context.test.ont#User";
    public static final String PROP_PHYSICAL_LOCATION = "org.universAAL.middleware.context.test.ont#PROP_PHYSICAL_LOCATION";

    public User(String uri) {
	super(uri);
    }

    public int getPropSerializationType(String propURI) {
	return 0;
    }

    public void setLocation(Location location) {
	if (location != null)
	    setProperty(PROP_PHYSICAL_LOCATION, location);
    }

    public String getClassURI() {
	return MY_URI;
    }
}
