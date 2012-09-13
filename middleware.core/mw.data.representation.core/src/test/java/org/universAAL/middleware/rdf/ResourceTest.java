package org.universAAL.middleware.rdf;

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
	assertTrue(Resource.isAnonymousURI(r.toString()));
	assertTrue(r.isAnon());
	assertFalse(r.serializesAsXMLLiteral());
	r = new Resource(true);
	assertTrue(r.serializesAsXMLLiteral());
	r = new Resource(null);
	assertTrue(Resource.isAnonymousURI(r.toString()));
	r = new Resource(Resource.PROP_RDF_FIRST); // just any valid URI
	assertFalse(Resource.isAnonymousURI(r.toString()));
	r = new Resource(null, true);
	assertTrue(Resource.isAnonymousURI(r.toString()));
	r = new Resource(Resource.PROP_RDF_FIRST, true); // just any valid URI
	assertFalse(Resource.isAnonymousURI(r.toString()));
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
	//System.out.println(r.getOrConstructLabel(null));
	assertTrue("\"Light Source\"".equals(r.getOrConstructLabel(null)));
	//System.out.println(r.getOrConstructLabel("My LightSource"));
	assertTrue("My LightSource \"Light Source\"".equals(r
		.getOrConstructLabel("My LightSource")));
	r.setResourceLabel("mylabel");
	assertTrue("mylabel".equals(r.getOrConstructLabel(null)));
	assertTrue("mylabel".equals(r.getOrConstructLabel("ylksdf")));
    }
}
