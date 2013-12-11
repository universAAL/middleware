package org.universAAL.middleware.rdf;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.owl.MaxCardinalityRestriction;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.MinCardinalityRestriction;
import org.universAAL.middleware.util.ResourceComparator;

import junit.framework.TestCase;

public class ResourceTest extends TestCase {

    protected void setUp() throws Exception {
	super.setUp();
    }

    public void test() {
	Resource r;

	r = new Resource("prefix", 10);
	assertTrue("prefix".length() < r.getURI().length());
	assertTrue(r.getURI().startsWith("prefix"));
	r = new Resource();
	assertTrue(Resource.isAnon(r.toString()));
	assertTrue(r.isAnon());
	assertFalse(r.serializesAsXMLLiteral());
	r = new Resource(true);
	assertTrue(r.serializesAsXMLLiteral());
	r = new Resource(null);
	assertTrue(Resource.isAnon(r.toString()));
	r = new Resource(Resource.PROP_RDF_FIRST); // just any valid URI
	assertFalse(Resource.isAnon(r.toString()));
	r = new Resource(null, true);
	assertTrue(Resource.isAnon(r.toString()));
	r = new Resource(Resource.PROP_RDF_FIRST, true); // just any valid URI
	assertFalse(Resource.isAnon(r.toString()));
	assertTrue(r.hasQualifiedName());
	assertTrue(r.numberOfProperties() == 0);
	r.setResourceComment("comment");
	assertTrue(r.numberOfProperties() == 1);
	r.setResourceLabel("label");
	assertTrue(r.numberOfProperties() == 2);
	assertTrue("comment".equals(r.getResourceComment()));
	assertTrue("label".equals(r.getResourceLabel()));
    }

    public void testGetOrConstructLabel() {
	Resource r = new Resource(Resource.uAAL_VOCABULARY_NAMESPACE
		+ "LightSource");
	// System.out.println(r.getOrConstructLabel(null));
	assertTrue("\"Light Source\"".equals(r.getOrConstructLabel(null)));
	// System.out.println(r.getOrConstructLabel("My LightSource"));
	assertTrue("My LightSource \"Light Source\"".equals(r
		.getOrConstructLabel("My LightSource")));
	r.setResourceLabel("mylabel");
	assertTrue("mylabel".equals(r.getOrConstructLabel(null)));
	assertTrue("mylabel".equals(r.getOrConstructLabel("ylksdf")));
    }

    public void testDeepCopy() {
	Resource r1 = new Resource();
	Resource r2 = r1.deepCopy();
	assertTrue((new ResourceComparator()).areEqual(r1, r2));

	r1.setProperty(
		"testprop",
		new MergedRestriction("onProperty").addRestriction(
			new MinCardinalityRestriction("onProperty", 32))
			.addRestriction(
				new MaxCardinalityRestriction("onProperty", 42)

			));
	r1.setProperty("testprop2", Integer.valueOf(1));
	r1.setProperty("testprop3", r1);
	r2 = r1.deepCopy();

	// System.out.println(r1.toStringRecursive());
	// System.out.println(r2.toStringRecursive());
	assertTrue((new ResourceComparator()).areEqual(r1, r2));
    }

    public void testList() {
	ArrayList l = new ArrayList();
	l.add("1");
	l.add("2");
	l.add("3");

	Resource rl = Resource.asRDFList(l, false);
	List l2 = rl.asList();

	assertTrue(l.size() == l2.size());

	int i = 0;
	for (Object o : l2) {
	    assertTrue(o.equals(l.get(i++)));
	}

	// System.out.println(rl.toStringRecursive());
	// System.out.println(l2);
    }

    public void testEmptyList() {
	ArrayList l = new ArrayList();

	Resource rl = Resource.asRDFList(l, false);
	List l2 = rl.asList();
	// System.out.println(rl.toStringRecursive());

	assertTrue(l.size() == l2.size());
    }

    public void testLangString() {
	String en = "hello";
	String de = "hallo";
	String p = Resource.PROP_RDFS_LABEL;
	Resource r = new Resource();
	r.addMultiLangProp(p, new LangString(en, LangString.LANG_ENGLISH));
	r.addMultiLangProp(p, new LangString(de, LangString.LANG_GERMAN));

	assertTrue(r.getProperty(p) instanceof List);
	assertTrue(((List) (r.getProperty(p))).size() == 2);
	assertTrue(en.equals(r.getMultiLangProp(p, LangString.LANG_ENGLISH,
		false).getString()));
	assertTrue(de.equals(r.getMultiLangProp(p, LangString.LANG_GERMAN,
		false).getString()));

	r.addMultiLangProp(p, new LangString("somevalue",
		LangString.LANG_GERMAN));

	assertTrue(r.getProperty(p) instanceof List);
	assertTrue(((List) (r.getProperty(p))).size() == 2);
	assertTrue(en.equals(r.getMultiLangProp(p, LangString.LANG_ENGLISH,
		false).getString()));
	assertTrue(de.equals(r.getMultiLangProp(p, LangString.LANG_GERMAN,
		false).getString()));
    }
}
