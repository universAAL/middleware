package org.universAAL.middleware.context;

import junit.framework.TestCase;

import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;

public class RoundTest extends TestCase {

    public static String ELLA = "urn:org.universAAL.profiling:12345:ella";
    public static String STB = "urn:org.universAAL.profiling:12345:STB_LivingRoom";
    public static String HAS_LOCATION = "http://ontology.universAAL.org/fake.owl#hasIndoorLocation";
    public static String USER = "http://ontology.universAAL.org/profile.owl#User";

    public RoundTest(String name) {
	super(name);
    }

    public void testCA() {
	ContextEventPattern cep;
	cep = new ContextEventPattern();

	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_SUBJECT, new Resource(ELLA)));

	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, new Resource(HAS_LOCATION)));

	ContextEvent event;

	event = ContextEvent.constructSimpleEvent(ELLA, USER, HAS_LOCATION,
		"bathroom");

	assertEquals(cep.matches(event), true);

    }

}
