package org.universAAL.middleware.owl.testont;

public class MyClass3Sub1 extends MyClass3 {
    public static final String MY_URI = MyOntology.NAMESPACE + "MyClass3Sub1";

    public MyClass3Sub1(String uri) {
	super(uri);
    }

    public String getClassURI() {
	return MY_URI;
    }
}
