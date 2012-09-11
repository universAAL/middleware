package org.universAAL.middleware.owl.ont;

import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.testont.*;

import junit.framework.TestCase;

public class OntologiesTest extends TestCase {

    private MyOntology ont;

    protected void setUp() throws Exception {
	super.setUp();
	OntologyManagement.getInstance().register(new DataRepOntology());
    }

    public void testSpecialization() {
	ont = new MyOntology();

	// trying before ontology registration
	// assertFalse(MyResource.MY_URI.equals(OntologyManagement.getInstance()
	// .getMostSpecializedClass(
	// new String[] { MyResource.MY_URI,
	// "somethingcompletelyDifferent",
	// "somethingNotSoDifferent" })));

	// register ontology
	OntologyManagement.getInstance().register(ont);

	// try again
	assertTrue(MyResource.MY_URI.equals(OntologyManagement.getInstance()
		.getMostSpecializedClass(
			new String[] { MyResource.MY_URI,
				"somethingcompletelyDifferent",
				"somethingNotSoDifferent" })));
	assertTrue(MyResource.MY_URI
		.equals(OntologyManagement.getInstance()
			.getMostSpecializedClass(
				new String[] { "somethingcompletelyDifferent",
					MyResource.MY_URI,
					"somethingNotSoDifferent" })));
	assertTrue(MyResource.MY_URI
		.equals(OntologyManagement.getInstance()
			.getMostSpecializedClass(
				new String[] { "somethingcompletelyDifferent",
					"somethingNotSoDifferent",
					MyResource.MY_URI })));

	// trying with ManagedIndividuals
	assertTrue(MyClass1Sub1.MY_URI.equals(OntologyManagement.getInstance()
		.getMostSpecializedClass(
			new String[] { MyClass1.MY_URI, MyClass1Sub1.MY_URI,
				"somethingNotSoDifferent" })));
    }
}
