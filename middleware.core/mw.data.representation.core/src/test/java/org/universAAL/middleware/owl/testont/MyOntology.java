package org.universAAL.middleware.owl.testont;

import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntClassInfoSetup;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.Resource;

public class MyOntology extends Ontology {
    public static final String NAMESPACE = Resource.uAAL_NAMESPACE_PREFIX
	    + "Test.owl#";

    MyFactory factory = new MyFactory();

    public MyOntology() {
	super(NAMESPACE);
    }

    public void create() {
	OntClassInfoSetup oci1;
	OntClassInfoSetup oci2;

	createNewRDFClassInfo(MyResource.MY_URI, factory, 0);
	oci1 = createNewOntClassInfo(MyClass1.MY_URI, factory, 1);
	oci2 = createNewOntClassInfo(MyClass2.MY_URI, factory, 2);
	createNewOntClassInfo(MyClass3.MY_URI, factory, 3);
	createNewOntClassInfo(MyClass1Sub1.MY_URI, factory, 4).addSuperClass(
		MyClass1.MY_URI);

	oci1.addObjectProperty(MyClass1.PROP_C1C2);
	oci1.addRestriction(MergedRestriction.getAllValuesRestriction(
		MyClass1.PROP_C1C2, MyClass2.MY_URI));

	oci2.addObjectProperty(MyClass2.PROP_C2C3);
	oci2.addRestriction(MergedRestriction.getAllValuesRestriction(
		MyClass2.PROP_C2C3, new TypeURI(MyClass3.MY_URI, false)));
    }
}
