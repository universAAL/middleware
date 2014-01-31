package org.universAAL.middleware.owl.testont;

public class MyClass1Sub1 extends MyClass1 {
    public static final String MY_URI = MyOntology.NAMESPACE + "MySub1Class1";

    public MyClass1Sub1(String uri) {
	super(uri);
    }

    public String getClassURI() {
	return MY_URI;
    }
}
