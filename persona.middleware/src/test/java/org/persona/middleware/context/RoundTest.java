package org.persona.middleware.context;


import org.persona.middleware.PResource;
import org.persona.ontology.expr.Restriction;

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
				new PResource(ELLA)));

		cep.addRestriction(Restriction.getFixedValueRestriction(
				ContextEvent.PROP_RDF_PREDICATE,
				new PResource(HAS_LOCATION)));
		
		ContextEvent event;
		
		event = ContextEvent.constructSimpleEvent(
				ELLA,
				USER,
				HAS_LOCATION,
				"bathroom");

		
		assertEquals(cep.matches(event),true);
		
		
		
		//Using ContextProvider instead of SetTopBox ontology because the second is not available in mw
		// ContextProvider temp = new ContextProvider();
		cep = new ContextEventPattern();
		
		cep.addRestriction(Restriction.getAllValuesRestriction(
				ContextEvent.PROP_RDF_SUBJECT,
				SetTopBox.MY_URI));

		cep.addRestriction(Restriction.getFixedValueRestriction(
				ContextEvent.PROP_RDF_PREDICATE,
				new PResource(SetTopBox.HAS_ACTION)));

		
		event = ContextEvent.constructSimpleEvent(
				STB,
				SetTopBox.MY_URI,
				SetTopBox.HAS_ACTION,
				"sample action");
		
		assertEquals(cep.matches(event),true);
	}

}
