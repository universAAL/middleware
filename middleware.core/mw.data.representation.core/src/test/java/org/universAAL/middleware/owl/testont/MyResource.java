package org.universAAL.middleware.owl.testont;

import org.universAAL.middleware.rdf.FinalizedResource;

public class MyResource extends FinalizedResource {
    public static final String MY_URI = MyOntology.NAMESPACE + "MyResource";

    MyResource(String uri) {
	super(uri);
    }
}
