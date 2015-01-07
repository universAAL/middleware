package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.Resource;
import junit.framework.TestCase;

public class LengthRestrictionTest extends TestCase {

    protected void setUp() throws Exception {
	super.setUp();
    }

    private void testPatternHasMember(String pattern, String member,
	    boolean shouldMatch) {
	if ((!member.matches(pattern) && shouldMatch)
		|| (member.matches(pattern) && !shouldMatch)) {
	    System.out
		    .println("Pattern does not match -> Restrictions cannot match");
	    assertTrue(false);
	}

	URIRestriction ur = new URIRestriction();
	ur.setPattern(pattern);
	if (shouldMatch)
	    assertTrue(ur.hasMember(new Resource(member)));
	else
	    assertFalse(ur.hasMember(new Resource(member)));
    }

    public void testPatternHasMember() {
	testPatternHasMember("[0-9a-fA-F]*", "0adf", true);
	testPatternHasMember("[0-9a-zA-Z]*/[0-9a-zA-Z]*", "0adf/", true);
	testPatternHasMember("[0-9a-zA-Z]*/[0-9a-zA-Z]*", "/", true);
	testPatternHasMember("[0-9a-zA-Z]*/[0-9a-zA-Z]*", "0adf", false);
    }

    public void testLenHasMember() {
	URIRestriction ur = new URIRestriction();
	ur.setLen(3);

	assertTrue(ur.hasMember(new Resource("mem")));
	assertFalse(ur.hasMember(new Resource("1234")));
	assertFalse(ur.hasMember(new Resource("12")));
    }

    public void testHasMember() {
	URIRestriction ur = new URIRestriction();
	ur.setPattern("[0-9a-zA-Z]*");
	ur.setLen(3);

	assertTrue(ur.hasMember(new Resource("mem")));
	assertTrue(ur.hasMember(new Resource("123")));
	assertFalse(ur.hasMember(new Resource("1234")));
	assertFalse(ur.hasMember(new Resource("12")));
	assertFalse(ur.hasMember(new Resource("!!!")));
    }
}
