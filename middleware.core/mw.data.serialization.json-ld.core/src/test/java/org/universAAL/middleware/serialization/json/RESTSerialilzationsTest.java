/**
 *
 */
package org.universAAL.middleware.serialization.json;

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;
import org.universAAL.middleware.bus.junit.OntTestCase;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.turtle.TurtleParser;
import org.universAAL.middleware.util.GraphIterator;
import org.universAAL.middleware.util.GraphIteratorElement;
import org.universAAL.ontology.lighting.LightSource;
import org.universAAL.ontology.measurement.Measurement;
import org.universAAL.ontology.unit.MeasurableDimension;
import org.universAAL.ontology.unit.Unit;

/**
 * @author edu
 *
 */
public class RESTSerialilzationsTest extends OntTestCase{

	JSONLDSerialization ser = new JSONLDSerialization();
	@Test
	public void testCreateContextEvent() {
		 // Create an instance of the subject

//		 LightSource light = new LightSource("https://github.com/soad03/middleware/tree/URIcompactor_update");
		 LightSource light = new LightSource("https://github.com/soad03/middleware/tree#URIcompactor_update");
		 //LightSource light = new LightSource("LightSource");
		 // Set the property to be used as predicate to a valid value
		 light.setBrightness(100);
		 // Create event with subject and predicate. Object is auto-set
		 ContextEvent ev = new ContextEvent(light, LightSource.PROP_SOURCE_BRIGHTNESS);
		String res = ser.serialize(ev);
		System.out.println(res);


//		Resource r = (Resource) ser.deserialize(res);
//		compare(ev,r);
	}

	
	public void testMeasurements() {
	
		Measurement m = new Measurement("https://github.com/soad03/middleware/tree#URIcompactor_update");
		m.setValue("2");
		ContextEvent ev = new ContextEvent(m,Measurement.PROP_VALUE);
		String res = ser.serialize(ev);
		String ser_turtle = serialize(ev);
		System.out.println(ser_turtle);
		System.out.println(res);
	}
	
	
	public void testUnits() {
	
		Unit u = new Unit("URI-postfix","name","Symbol",MeasurableDimension.Color);
		ContextEvent ev = new ContextEvent(u,Unit.PROP_NAME);
		String res = ser.serialize(ev);
		System.out.println(res);
	}
	
	
	public void testCreateContextProvider() {
		 ContextProvider myprov = new ContextProvider("https://www.w3.org/TR/rdf-primer/#rdfmodel");
		 // Set to type Gauge
		 myprov.setType(ContextProviderType.gauge);
		 // Set the provided events to “Unknown” with an empty Pattern
		 myprov.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });
		 System.out.println(ser.serialize(myprov));
	}
	
	public void testCreateContextEventPattern() {
		ContextEventPattern[] cep = new ContextEventPattern[2];
		 // This first pattern is for events about Lights from Gauge Providers. Notice how ContextEvent is the root for Restrictions
		 cep[0] = new ContextEventPattern();
		 cep[0].addRestriction(MergedRestriction.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT, LightSource.MY_URI));
		 cep[0].addRestriction(MergedRestriction.getFixedValueRestriction(ContextProvider.PROP_CONTEXT_PROVIDER_TYPE, ContextProviderType.gauge).appendTo(
		   MergedRestriction.getAllValuesRestriction(ContextEvent.PROP_CONTEXT_PROVIDER,ContextProvider.MY_URI),new String[] { ContextEvent.PROP_CONTEXT_PROVIDER,
		   ContextProvider.PROP_CONTEXT_PROVIDER_TYPE }));
		 // The second pattern is for events about any brightness change
		 cep[1].addRestriction(MergedRestriction.getAllValuesRestriction(ContextEvent.PROP_RDF_PREDICATE, LightSource.PROP_SOURCE_BRIGHTNESS));
		 // Create (and register) the Context Subscriber

		 System.out.println(ser.serialize(cep[0]));
	}

	public void testcreateServiceProfile() {

	}

	public void createServiceRequest() {

	}
	/*
	public static void compare(Resource r1, Resource r2) {
		Iterator<GraphIteratorElement> it;

		// the detailed test: test all triple values
		HashSet<String> triples = new HashSet<String>();
		it = GraphIterator.getIterator(r2);
		while (it.hasNext()) {
			GraphIteratorElement el = it.next();
			String s = getTriple(el);
			// System.out.println(s);
			// assertFalse(triples.contains(s));
			triples.add(s);
			// System.out.println(s);
		}
		// System.out.println("\n\n");
		it = GraphIterator.getIterator(r1);
		while (it.hasNext()) {
			GraphIteratorElement el = it.next();
			String s = getTriple(el);
			if (!triples.contains(s))
				System.out.println("ERROR: triple not available: " + s);
			assertTrue(triples.contains(s));
			// triples.remove(s);
			// System.out.println(s);
		}
		// assertTrue(triples.size() == 0);

		// test that the number of triples is the same in both ontology
		// representations
		int i1 = 0;
		it = GraphIterator.getIterator(r1);
		while (it.next() != null) {
			i1++;
		}
		int i2 = 0;
		it = GraphIterator.getIterator(r2);
		while (it.next() != null) {
			i2++;
		}
		if (i1 != i2)
			System.out.println("ERROR: number of triples do not match: " + i1 + " " + i2);
		assertTrue(i1 == i2);
}
	*/
}
