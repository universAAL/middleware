package org.universAAL.middleware.context.test.ont;

import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntClassInfoSetup;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.impl.ResourceFactoryImpl;

public class TestOntology extends Ontology {
    public static final String NAMESPACE = Resource.uAAL_NAMESPACE_PREFIX
	    + "Test.owl#";

    private class MyFactory extends ResourceFactoryImpl {
	public Resource createInstance(String classURI, String instanceURI,
		int factoryIndex) {
	    switch (factoryIndex) {
	    case 0:
		return new User(instanceURI);
	    case 1:
		return new Location(instanceURI);
	    }
	    return null;
	}
    }

    MyFactory factory = new MyFactory();

    public TestOntology() {
	super(NAMESPACE);
    }

    public void create() {
	OntClassInfoSetup oci;

	createNewOntClassInfo(Location.MY_URI, factory, 1);

	oci = createNewOntClassInfo(User.MY_URI, factory, 0);
	oci.addObjectProperty(User.PROP_PHYSICAL_LOCATION).setFunctional();
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			User.PROP_PHYSICAL_LOCATION, Location.MY_URI, 0, 1));
    }
}
