package org.universAAL.middleware.owl;

import org.universAAL.middleware.rdf.Resource;

import junit.framework.TestCase;

public class LengthRestrictionTest extends TestCase {

    protected void setUp() throws Exception {
	super.setUp();
    }

    public void testSettingValue1() {
	URIRestriction u = new URIRestriction();
	assertTrue(u.setLen(3));
	assertFalse(u.setLen(4));
	assertFalse(u.setMin(3));
	assertFalse(u.setMax(3));
    }

    public void testSettingValue2() {
	URIRestriction u = new URIRestriction();
	assertTrue(u.setMin(3));
	try {
	    u.setMax(2);
	    assertTrue(false);
	} catch (IllegalArgumentException e) {
	}
	assertFalse(u.setMin(4));
	assertTrue(u.setMax(4));
    }
    
    public void testSettingValue3() {
	URIRestriction u = new URIRestriction();
	assertTrue(u.setMax(3));
	try {
	    u.setMin(4);
	    assertTrue(false);
	} catch (IllegalArgumentException e) {
	}
	assertFalse(u.setMax(4));
	assertTrue(u.setMin(2));
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
    
    public void testMatches1() {
	URIRestriction u1 = new URIRestriction();
	URIRestriction u2 = new URIRestriction();
	
	u1.setMin(4);
	u1.setMax(6);
	u2.setLen(5);
	assertFalse(u2.matches(u1));
	assertTrue(u1.matches(u2));
    }
    
    public void testMatches2() {
	URIRestriction u1 = new URIRestriction();
	URIRestriction u2 = new URIRestriction();
	
	u1.setMin(5);
	u1.setMax(5);
	u2.setLen(5);
	assertTrue(u2.matches(u1));
	assertTrue(u1.matches(u2));
    }
}
