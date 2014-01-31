package org.universAAL.middleware.owl.testont;

import org.universAAL.middleware.owl.ManagedIndividual;

public class MyClass3 extends ManagedIndividual {
    public static final String MY_URI = MyOntology.NAMESPACE + "MyClass3";

    public MyClass3(String uri) {
	super(uri);
    }

    public String getClassURI() {
	return MY_URI;
    }

    public int getPropSerializationType(String propURI) {
	return 0;
    }
}
