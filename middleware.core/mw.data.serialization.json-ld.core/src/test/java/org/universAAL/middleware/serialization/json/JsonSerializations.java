package org.universAAL.middleware.serialization.json;

import org.universAAL.middleware.bus.junit.OntTestCase;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;

public class JsonSerializations extends OntTestCase {
	JSONLDSerialization ser = new JSONLDSerialization();
	
	public void testCreateContextProvider() {
		 ContextProvider myprov = new ContextProvider("https://www.w3.org/TR/rdf-primer/#rdfmodel");
		 // Set to type Gauge
		 myprov.setType(ContextProviderType.gauge);
		 // Set the provided events to “Unknown” with an empty Pattern
		 myprov.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });
		 
//		   ContextEventPattern cep = new ContextEventPattern();
//		   cep.addRestriction(MergedRestriction.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT,LightActuator.MY_URI));
//		   cep.addRestriction(MergedRestriction.getFixedValueRestriction(ContextEvent.PROP_RDF_PREDICATE, ValueDevice.PROP_HAS_VALUE));
//		   myprov.setProvidedEvents(new ContextEventPattern[] { cep });
		 System.out.println(ser.serialize(myprov));
	}
}
