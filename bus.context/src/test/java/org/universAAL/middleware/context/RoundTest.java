package org.universAAL.middleware.context;


import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.owl.Restriction;

import junit.framework.TestCase;

public class RoundTest extends TestCase {

	public static String ELLA = "urn:org.aal-persona.profiling:12345:ella";
	public static String STB = "urn:org.aal-persona.profiling:12345:STB_LivingRoom";
	public static String HAS_LOCATION = "http://ontology.aal-persona.org/fake.owl#hasIndoorLocation";
	public static String USER = "http://ontology.aal-persona.org/PERSONA.owl#User";
	
	public RoundTest(String name){
		super (name);
	}
	
	public void testCA (){
		ContextEventPattern cep;
		cep = new ContextEventPattern();
		
		cep.addRestriction(Restriction.getFixedValueRestriction(
				ContextEvent.PROP_RDF_SUBJECT,
				new Resource(ELLA)));

		cep.addRestriction(Restriction.getFixedValueRestriction(
				ContextEvent.PROP_RDF_PREDICATE,
				new Resource(HAS_LOCATION)));
		
		ContextEvent event;
		
		event = ContextEvent.constructSimpleEvent(
				ELLA,
				USER,
				HAS_LOCATION,
				"bathroom");

		
		assertEquals(cep.matches(event), true);
		
		
		
		// a second test
		cep = new ContextEventPattern();
		
		cep.addRestriction(Restriction.getAllValuesRestriction(
				ContextEvent.PROP_RDF_SUBJECT,
				SetTopBox.MY_URI));

		cep.addRestriction(Restriction.getFixedValueRestriction(
				ContextEvent.PROP_RDF_PREDICATE,
				new Resource(SetTopBox.HAS_ACTION)));

		
		event = ContextEvent.constructSimpleEvent(
				STB,
				SetTopBox.MY_URI,
				SetTopBox.HAS_ACTION,
				"sample action");
		
		assertEquals(cep.matches(event), true);
	}

}
