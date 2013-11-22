package org.universAAL.serialization.turtle;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.universAAL.middleware.owl.Enumeration;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.LangString;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.util.ResourceComparator;

public class TurtleTest extends TestCase {

    TurtleSerializer s;

    class MyOntClass extends ManagedIndividual {
	public static final String MY_URI = Resource.uAAL_NAMESPACE_PREFIX
		+ "TurtleTest.owl#MyOntClass";

	@Override
	public String getClassURI() {
	    return MY_URI;
	}

	public MyOntClass(String instanceURI) {
	    super(instanceURI);
	}

	@Override
	public int getPropSerializationType(String propURI) {
	    return Resource.PROP_SERIALIZATION_FULL;
	}
    }

    protected void setUp() throws Exception {
	super.setUp();
	// s = new TurtleParser();
	OntologyManagement.getInstance().register(
		null,
		new SimpleOntology(MyOntClass.MY_URI, ManagedIndividual.MY_URI,
			new ResourceFactory() {
			    public Resource createInstance(String classURI,
				    String instanceURI, int factoryIndex) {
				return new MyOntClass(instanceURI);
			    }
			}));
    }

    private void log(String s) {
	// LogUtils.logDebug(SharedResources.moduleContext, TurtleTest.class,
	// "log", new Object[] { s }, null);
	System.out.println(s);
    }

    private boolean check(Resource r1) {
	Resource r2; // resulting resource (after serialize-deserialize)
	String str; // serialized String

	s = new TurtleSerializer();
	str = s.serialize(r1);
	System.out.println("Serialized String:\n" + str);
	s = new TurtleSerializer();
	r2 = (Resource) s.deserialize(str);
	ResourceComparator rc = new ResourceComparator();
	// System.out.println("-- r1:\n" + r1.toStringRecursive());
	// System.out.println("-- r2:\n" + r2.toStringRecursive());

	if (!rc.areEqual(r1, r2)) {
	    log("-- error found in serialization: ");
	    log(str);
	    System.out.println(rc.getDiffsAsString(r1, r2));
	    System.out.println("-- r1:\n" + r1.toStringRecursive());
	    System.out.println("-- r2:\n" + r2.toStringRecursive());
	    return false;
	}
	return true;
    }

    public void testEnumeration() {
	Enumeration e = new Enumeration(new Object[] { new Resource("value1") });
	Resource r = MergedRestriction.getAllValuesRestriction("propURI", e);
	assertTrue(check(r));
    }

    public void testTypeURI() {
	String propURI = Resource.uAAL_NAMESPACE_PREFIX
		+ "TurtleTest.owl#myProperty";
	Resource r = new Resource("testResource");
	ArrayList al = new ArrayList();
	al.add(new MyOntClass("instanceURI"));
	al.add(new TypeURI(MyOntClass.MY_URI, false));
	r.setProperty(propURI, al);
	assertTrue(check(r));
    }

    public void testSimpleCycle() {
	Resource r1 = new Resource();
	Resource r2 = new Resource();
	r1.setProperty("prop12", r2);
	r2.setProperty("prop21", r1);
	assertTrue(check(r1));
    }

    public void testEnumeration2() {
	Enumeration e = new Enumeration(new Object[] { new Resource("value1") });
	assertTrue(check(e));
    }

    public void testLanguagedLabel() {
	Resource r1 = new Resource(); // input resource

	r1.addMultiLangProp(Resource.PROP_RDFS_LABEL, new LangString(
		"myLabel_en", "en"));
	// log(r1.toStringRecursive());
	// System.out.println(r1.toStringRecursive());
	// assertTrue(check(r1));
	r1.addMultiLangProp(Resource.PROP_RDFS_LABEL, new LangString("myLabel",
		LangString.LANG_LATIN));
	assertTrue(check(r1));
	r1.addMultiLangProp(Resource.PROP_RDFS_LABEL, new LangString(
		"myLabel_de", "de"));
	assertTrue(check(r1));
    }

    public void testRootURI() {
	// test the deserialization with a serialized String containing
	// multiple root resources
	String serialized = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n"
		+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\r\n"
		+ "@prefix sesame: <http://www.openrdf.org/schema/sesame#> .\r\n"
		+ "@prefix owl: <http://www.w3.org/2002/07/owl#> .\r\n"
		+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n"
		+ "@prefix fn: <http://www.w3.org/2005/xpath-functions#> .\r\n"
		+ "\r\n"
		+ "<http://ontology.itaca.upv.es/Test.owl#panic10> a rdfs:Resource , <http://ontology.universAAL.org/uAAL.owl#PhysicalThing> , <http://ontology.universAAL.org/Device.owl#Device> , <http://ontology.universAAL.org/Risk.owl#PanicButton> , <http://ontology.universAAL.org/uAAL.owl#ManagedIndividual> , _:node17954gsa9x306 , _:node17954gsa9x307 , _:node17954gsa9x308 , _:node17954gsa9x309 , _:node17954gsa9x371 , _:node17954gsa9x372 , _:node17954gsa9x374 , _:node17954gsa9x375 , _:node17954gsa9x376 , _:node17954gsa9x377 , _:node17954gsa9x378 , _:node17954gsa9x379 , _:node17954gsa9x380 , _:node17954gsa9x381 , _:node17954gsa9x382 , _:node17954gsa9x383 , _:node17954gsa9x384 , _:node17954gsa9x385 , _:node17954gsa9x386 , _:node17954gsa9x387 ;\r\n"
		+ "	<http://ontology.universAAL.org/uAAL.owl#hasLocation> <http://ontology.itaca.upv.es/Test.owl#location10> ;\r\n"
		+ "	<http://ontology.universAAL.org/uAAL.owl#carriedBy> <http://ontology.itaca.upv.es/Test.owl#user5> ;\r\n"
		+ "	<http://ontology.universAAL.org/uAAL.owl#isPortable> \"true\"^^xsd:boolean .\r\n"
		+ "\r\n"
		+ "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b1d> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
		+ "\r\n"
		+ "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b19> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
		+ "\r\n"
		+ "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b1b> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
		+ "\r\n"
		+ "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b1a> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
		+ "\r\n"
		+ "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b1c> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
		+ "\r\n"
		+ "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b16> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
		+ "";

	TurtleSerializer t = new TurtleSerializer();

	String uri = "http://ontology.itaca.upv.es/Test.owl#panic10";
	Object o = t.deserialize(serialized, uri);
	assertFalse(o == null);
	assertTrue(o instanceof Resource);
	Resource r = (Resource) o;
	System.out.println(r.getURI());
	assertTrue(uri.equals(r.getURI()));

	uri = "urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b1a";
	o = t.deserialize(serialized, uri);
	assertFalse(o == null);
	assertTrue(o instanceof Resource);
	r = (Resource) o;
	System.out.println(r.getURI());
	assertTrue(uri.equals(r.getURI()));
    }

    public void testNaN() {
	Resource r1 = new Resource(); // input resource
	r1.setProperty("test", Double.NaN);
	assertTrue(check(r1));
    }

    public void testPosINF() {
	Resource r1 = new Resource(); // input resource
	r1.setProperty("test", Double.POSITIVE_INFINITY);
	assertTrue(check(r1));
    }

    public void testNegINF() {
	Resource r1 = new Resource(); // input resource
	r1.setProperty("test", Double.NEGATIVE_INFINITY);
	assertTrue(check(r1));
    }

    public void testInteger() {
	// issue from Bug report #280 Incorrect deserialization of Integer
	Resource r1 = new Resource(); // input resource
	r1.setProperty("testint", Integer.valueOf(100));
	r1.setProperty("testinteger", BigInteger.valueOf(200));
	r1.setProperty("testdec", BigDecimal.valueOf(3.3));
	r1.setProperty("testdec2", BigDecimal.valueOf(40000000.00000001));
	r1.setProperty("testfloat", Float.valueOf("0.0000001"));
	assertTrue(check(r1));
    }

    // public void testQuotedLiteral() {
    // Resource r1 = new Resource("testResource"); // input resource
    // r1.setProperty("testProp", new Integer(100));
    //
    // Resource r2; // resulting resource (after serialize-deserialize)
    // String str; // serialized String
    //
    // s = new TurtleSerializer();
    // str = s.serialize(r1);
    // str = str.replace("100", "\"100\"^^xsd:int");
    // str = "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n" + str;
    //
    // System.out.println("Serialized String:\n" + str);
    // s = new TurtleSerializer();
    // r2 = (Resource) s.deserialize(str);
    // ResourceComparator rc = new ResourceComparator();
    //
    // if (!rc.areEqual(r1, r2)) {
    // log("-- error found in serialization: ");
    // log(str);
    // // rc.printDiffs(r1, r2);
    // System.out.println("-- r1:\n" + r1.toStringRecursive());
    // System.out.println("-- r2:\n" + r2.toStringRecursive());
    // }
    // }
}
