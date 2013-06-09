package org.universAAL.serialization.turtle;

import junit.framework.TestCase;

import org.universAAL.middleware.rdf.LangString;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.util.ResourceComparator;
//import org.universAAL.middleware.container.utils.LogUtils;
//import org.universAAL.middleware.datarep.SharedResources;

public class TurtleTest extends TestCase {

    TurtleSerializer s;

    protected void setUp() throws Exception {
	super.setUp();
	// s = new TurtleParser();
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
	System.out.println("Serialized String:\n"+str);
	s = new TurtleSerializer();
	r2 = (Resource) s.deserialize(str);
	ResourceComparator rc = new ResourceComparator();
	
	if (!rc.areEqual(r1, r2)) {
	    log("-- error found in serialization: ");
	    log(str);
	    // rc.printDiffs(r1, r2);
	    System.out.println("-- r1:\n" + r1.toStringRecursive());
	    System.out.println("-- r2:\n" + r2.toStringRecursive());
	    return false;
	}
	return true;
    }

    public void testLanguagedLabel() {
	Resource r1 = new Resource(); // input resource

	r1.addMultiLangProp(Resource.PROP_RDFS_LABEL, new LangString(
		"myLabel_en", "en"));
	// log(r1.toStringRecursive());
	// System.out.println(r1.toStringRecursive());
	//assertTrue(check(r1));
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
	 String serialized =
	 "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n"
	 + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\r\n"
	 + "@prefix sesame: <http://www.openrdf.org/schema/sesame#> .\r\n"
	 + "@prefix owl: <http://www.w3.org/2002/07/owl#> .\r\n"
	 + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n"
	 + "@prefix fn: <http://www.w3.org/2005/xpath-functions#> .\r\n"
	 + "\r\n"
	 +
	 "<http://ontology.itaca.upv.es/Test.owl#panic10> a rdfs:Resource , <http://ontology.universAAL.org/uAAL.owl#PhysicalThing> , <http://ontology.universAAL.org/Device.owl#Device> , <http://ontology.universAAL.org/Risk.owl#PanicButton> , <http://ontology.universAAL.org/uAAL.owl#ManagedIndividual> , _:node17954gsa9x306 , _:node17954gsa9x307 , _:node17954gsa9x308 , _:node17954gsa9x309 , _:node17954gsa9x371 , _:node17954gsa9x372 , _:node17954gsa9x374 , _:node17954gsa9x375 , _:node17954gsa9x376 , _:node17954gsa9x377 , _:node17954gsa9x378 , _:node17954gsa9x379 , _:node17954gsa9x380 , _:node17954gsa9x381 , _:node17954gsa9x382 , _:node17954gsa9x383 , _:node17954gsa9x384 , _:node17954gsa9x385 , _:node17954gsa9x386 , _:node17954gsa9x387 ;\r\n"
	 +
	 "	<http://ontology.universAAL.org/uAAL.owl#hasLocation> <http://ontology.itaca.upv.es/Test.owl#location10> ;\r\n"
	 +
	 "	<http://ontology.universAAL.org/uAAL.owl#carriedBy> <http://ontology.itaca.upv.es/Test.owl#user5> ;\r\n"
	 +
	 "	<http://ontology.universAAL.org/uAAL.owl#isPortable> \"true\"^^xsd:boolean .\r\n"
	 + "\r\n"
	 +
	 "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b1d> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
	 + "\r\n"
	 +
	 "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b19> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
	 + "\r\n"
	 +
	 "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b1b> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
	 + "\r\n"
	 +
	 "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b1a> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
	 + "\r\n"
	 +
	 "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b1c> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
	 + "\r\n"
	 +
	 "<urn:org.universAAL.middleware.context.rdf:ContextEvent#13a4a4845a0@VAIOTSBAL+a871fc3f+985e8d8c:b16> rdf:subject <http://ontology.itaca.upv.es/Test.owl#panic10> .\r\n"
	 + "";
	
	 String uri = "http://ontology.itaca.upv.es/Test.owl#panic10";
	 TurtleSerializer t = new TurtleSerializer();
	 Object o = t.deserialize(serialized, uri);
	 assertFalse(o == null);
	 assertTrue(o instanceof Resource);
	 Resource r = (Resource) o;
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
}
