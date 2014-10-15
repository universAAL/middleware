package org.universAAL.middleware.owl.testont;

import org.universAAL.middleware.owl.ManagedIndividual;

public class MyClass2 extends ManagedIndividual {
    public static final String MY_URI = MyOntology.NAMESPACE + "MyClass2";
    public static final String PROP_C2C3 = MyOntology.NAMESPACE + "MyPropC2C3";

    public MyClass2(String uri) {
	super(uri);
    }

    public String getClassURI() {
	return MY_URI;
    }

    public int getPropSerializationType(String propURI) {
	return 0;
    }
}
