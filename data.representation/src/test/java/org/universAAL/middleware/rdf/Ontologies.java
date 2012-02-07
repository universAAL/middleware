package org.universAAL.middleware.rdf;

import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.impl.ResourceFactoryImpl;

import junit.framework.TestCase;

public class Ontologies extends TestCase {

    private class MyResource extends Resource {
	public static final String MY_URI = MyOntology.NAMESPACE + "MyResource";

	MyResource(String uri) {
	    super(uri);
	}
    }

    private class MyIndividual extends ManagedIndividual {
	public static final String MY_URI = MyOntology.NAMESPACE
		+ "MyIndividual";

	public MyIndividual(String uri) {
	    super(uri);
	}

	public String getClassURI() {
	    return MY_URI;
	}

	public int getPropSerializationType(String propURI) {
	    return 0;
	}
    }

    private class MySubIndividual extends MyIndividual {
	public static final String MY_URI = MyOntology.NAMESPACE
		+ "MySubIndividual";

	public MySubIndividual(String uri) {
	    super(uri);
	}

	public String getClassURI() {
	    return MY_URI;
	}
    }

    private class MyFactory extends ResourceFactoryImpl {
	public Resource createInstance(String classURI, String instanceURI,
		int factoryIndex) {
	    switch (factoryIndex) {
	    case 0:
		return new MyResource(instanceURI);
	    case 1:
		return new MyIndividual(instanceURI);
	    case 2:
		return new MySubIndividual(instanceURI);
	    }
	    return null;
	}
    }

    private class MyOntology extends Ontology {
	public static final String NAMESPACE = Resource.uAAL_NAMESPACE_PREFIX
		+ "Test.owl#";

	MyFactory factory = new MyFactory();

	public MyOntology() {
	    super(NAMESPACE);
	}

	public void create() {
	    createNewRDFClassInfo(MyResource.MY_URI, factory, 0);
	    createNewOntClassInfo(MyIndividual.MY_URI, factory, 1);
	    createNewOntClassInfo(MySubIndividual.MY_URI, factory, 2)
		    .addSuperClass(MyIndividual.MY_URI);
	}
    }

    private MyOntology ont;

    protected void setUp() throws Exception {
	super.setUp();
	OntologyManagement.getInstance().register(new DataRepOntology());
    }

    public void testSpecialization() {
	ont = new MyOntology();

	// trying before ontology registration
	assertFalse(MyResource.MY_URI.equals(OntologyManagement.getInstance()
		.getMostSpecializedClass(
			new String[] { MyResource.MY_URI,
				"somethingcompletelyDifferent",
				"somethingNotSoDifferent" })));

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
	assertTrue(MySubIndividual.MY_URI.equals(OntologyManagement
		.getInstance().getMostSpecializedClass(
			new String[] { MyIndividual.MY_URI,
				MySubIndividual.MY_URI,
				"somethingNotSoDifferent" })));
    }
}
