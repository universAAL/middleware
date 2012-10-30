package org.universAAL.serialization.turtle;

import junit.framework.TestCase;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.rdf.LangString;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.ResourceComparator;

public class TurtleTest extends TestCase {

    TurtleParser s;
    ResourceComparator rc = new ResourceComparator();

    protected void setUp() throws Exception {
	super.setUp();
	// s = new TurtleParser();
    }

    private void log(String s) {
	LogUtils.logDebug(SharedResources.moduleContext, TurtleTest.class,
		"log", new Object[] { s }, null);
    }

    private boolean check(Resource r1) {
	Resource r2; // resulting resource (after serialize-deserialize)
	String str; // serialized String

	s = new TurtleParser();
	str = s.serialize(r1);
	s = new TurtleParser();
	r2 = (Resource) s.deserialize(str);

	if (!rc.areEqual(r1, r2)) {
	    log("-- error found in serialization: ");
	    log(str);
	    rc.printDiffs(r1, r2);
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
	check(r1);
	r1.addMultiLangProp(Resource.PROP_RDFS_LABEL, new LangString("myLabel",
		""));
	check(r1);
	r1.addMultiLangProp(Resource.PROP_RDFS_LABEL, new LangString(
		"myLabel_de", "de"));
	check(r1);
    }
}
