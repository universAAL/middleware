package org.universAAL.middleware.owl.ont;

import java.util.Set;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.testont.*;

import junit.framework.TestCase;

public class OntologiesTest extends TestCase {

    private MyOntology ont = new MyOntology();

    protected void setUp() throws Exception {
	super.setUp();
	OntologyManagement.getInstance().register(
		SharedResources.moduleContext, new DataRepOntology());
	OntologyManagement.getInstance().register(
		SharedResources.moduleContext, ont);
    }

    /** Helper method for logging. */
    protected void logInfo(Object args) {
	StackTraceElement callingMethod = Thread.currentThread()
		.getStackTrace()[2];
	LogUtils.logInfo(SharedResources.moduleContext, getClass(),
		callingMethod.getMethodName(), new Object[] { args }, null);
    }

    public void testTestOnt() {
	logInfo("- unit test - ");
	Ontology o = OntologyManagement.getInstance().getOntology(
		ont.getInfo().getURI());
	assertFalse(o == null);
	// assertTrue(o == ont);

	Set subs = OntologyManagement.getInstance().getNamedSubClasses(
		ManagedIndividual.MY_URI, true, false);
	assertFalse(subs == null);
	assertTrue(subs.size() == o.getOntClassInfo().length
	// + o.getRDFClassInfo().length
	);
    }

    public void testSpecialization() {
	logInfo("- unit test - ");
	// trying before ontology registration
	// assertFalse(MyResource.MY_URI.equals(OntologyManagement.getInstance()
	// .getMostSpecializedClass(
	// new String[] { MyResource.MY_URI,
	// "somethingcompletelyDifferent",
	// "somethingNotSoDifferent" })));

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
