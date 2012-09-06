package org.universAAL.middleware.owl.testont;

public class MySub1Class1 extends MyClass1 {
    public static final String MY_URI = MyOntology.NAMESPACE + "MySub1Class1";

    public MySub1Class1(String uri) {
	super(uri);
    }

    public String getClassURI() {
	return MY_URI;
    }
}
