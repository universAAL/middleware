package org.persona.serialization.turtle;

import org.persona.middleware.PResource;
import org.persona.middleware.TypeMapper;
import org.persona.middleware.context.ContextEvent;
import org.persona.middleware.service.PropertyPath;
import org.persona.middleware.service.ServiceRequest;
import org.persona.middleware.util.ResourceComparator;
import org.persona.ontology.Rating;
import org.persona.ontology.expr.Enumeration;
import org.persona.ontology.expr.Restriction;
import org.persona.ontology.expr.TypeURI;
import org.persona.serialization.turtle.util.DummyService;

import junit.framework.TestCase;

public class InitialTest extends TestCase {
	private static String TEST_PROP = "http://ontology.aal-persona.org/Context.owl#entersLocation";
	
	TurtleParser s;
	
	public InitialTest(String name) {
		super(name);

		TurtleUtil.typeMapper = TypeMapper.getTypeMapper();
		s = new TurtleParser();
	}

	public void testContextEvent()
	{
		
		PResource subject = new PResource("urn:org.aal-persona.profiling:123456789:saied");
		subject.addType("http://ontology.aal-persona.org/User.owl#User", true);
		subject.setProperty(TEST_PROP, "Nowhere");
		ContextEvent ce = new ContextEvent(subject, TEST_PROP);

		ce.setAccuracy(Rating.good);
		ce.setConfidence(new Integer(42));
		ce.setExpirationTime(new Long(System.currentTimeMillis()+100000));
		
		String ceXML = s.serialize(ce);
		System.out.println(ceXML);
		new ResourceComparator().printDiffs(ce, (PResource) s.deserialize(ceXML));
		System.out.println();
		System.out.println();
	}
	
	public void testDataRange() {
		Enumeration e1 = new Enumeration();
		e1.addValue(new Integer(0));
		e1.addValue(new Integer(1));
		e1.addValue(new Integer(2));
		e1.addValue(new Integer(3));
		e1.addValue(new Integer(4));

		String str = s.serialize(e1);
		System.out.println(str);
		new ResourceComparator().printDiffs(e1, (PResource) s.deserialize(str));
		System.out.println();
		System.out.println();
	}
	
	public void testRestriction() {
		Integer one = new Integer(1);
		Enumeration e = new Enumeration();
		e.addValue(Rating.richSatisfying);
		e.addValue(Rating.almostGood);
		e.addValue(Rating.good);
        Restriction r = new Restriction();
        r.setProperty(Restriction.PROP_OWL_ON_PROPERTY, Restriction.PROP_OWL_HAS_VALUE);
        r.setProperty(Restriction.PROP_OWL_ALL_VALUES_FROM,
        		new TypeURI(Rating.MY_URI, false));
        r.setProperty(Restriction.PROP_OWL_CARDINALITY, one);
        r.setProperty(Restriction.PROP_OWL_SOME_VALUES_FROM, e);

		String str = s.serialize(r);
		System.out.println(str);
		new ResourceComparator().printDiffs(r, (PResource) s.deserialize(str));
	}

	public void testServiceRequest() {
		DummyService ds = new DummyService();
		ds.addInstanceLevelRestriction(
				Restriction.getFixedValueRestriction(
						DummyService.PROP_CONTROLS_BOILER,
						new PResource(DummyService.MY_NAMESPACE+"boiler1")),
				new String[]{DummyService.PROP_CONTROLS_BOILER});
		ServiceRequest sr = new ServiceRequest(ds, null);
		sr.addChangeEffect(
				new PropertyPath(null, false, new String[]{
						DummyService.PROP_CONTROLS_BOILER, DummyService.PROP_IS_ON}),
				Boolean.TRUE);
		String str = s.serialize(sr);
		System.out.println(str);
		new ResourceComparator().printDiffs(sr, (PResource) s.deserialize(str));
	}
}
