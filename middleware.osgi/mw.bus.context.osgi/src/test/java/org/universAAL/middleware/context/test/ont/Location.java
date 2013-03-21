package org.universAAL.middleware.context.test.ont;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.Resource;

public class Location extends ManagedIndividual {

    public static final String MY_URI = TestOntology.NAMESPACE + "Location";

    public Location(String uri) {
	super(uri);
    }

    public int getPropSerializationType(String propURI) {
	return Resource.PROP_SERIALIZATION_FULL;
    }

    public String getClassURI() {
	return MY_URI;
    }
}
