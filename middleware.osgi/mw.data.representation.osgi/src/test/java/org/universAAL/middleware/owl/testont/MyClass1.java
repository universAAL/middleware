package org.universAAL.middleware.owl.testont;

import org.universAAL.middleware.owl.ManagedIndividual;

public class MyClass1 extends ManagedIndividual {
    public static final String MY_URI = MyOntology.NAMESPACE + "MyClass1";
    public static final String PROP_C1C2 = MyOntology.NAMESPACE + "MyPropC1C2";

    public MyClass1(String uri) {
	super(uri);
    }

    public String getClassURI() {
	return MY_URI;
    }

    public int getPropSerializationType(String propURI) {
	return 0;
    }
}
