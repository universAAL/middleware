package org.universAAL.middleware.context.test.ont;

import org.universAAL.middleware.owl.ManagedIndividual;

public class Location extends ManagedIndividual {

    public static final String MY_URI = "org.universAAL.middleware.context.test.ont#Location";

    public Location(String uri) {
	super(uri);
    }

    public int getPropSerializationType(String propURI) {
	return 0;
    }

    public String getClassURI() {
	return MY_URI;
    }

}
