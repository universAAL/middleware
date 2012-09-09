package org.universAAL.middleware.owl.testont;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.impl.ResourceFactoryImpl;

class MyFactory extends ResourceFactoryImpl {
    public Resource createInstance(String classURI, String instanceURI,
	    int factoryIndex) {
	switch (factoryIndex) {
	case 0:
	    return new MyResource(instanceURI);
	case 1:
	    return new MyClass1(instanceURI);
	case 2:
	    return new MyClass2(instanceURI);
	case 3:
	    return new MyClass3(instanceURI);
	case 4:
	    return new MyClass1Sub1(instanceURI);
	}
	return null;
    }
}
