package org.universAAL.middleware.owl;

import junit.framework.TestCase;

public class IntersectionTest extends TestCase {
    EnumerationTest et = new EnumerationTest();

    protected void setUp() throws Exception {
	super.setUp();
    }

    public void testHasMember() {
	Intersection i;

	i = new Intersection();
	i.addType(et.e0);
	assertTrue(i.hasMember(Integer.valueOf(0), null));
	assertFalse(i.hasMember(Integer.valueOf(1), null));
	i.addType(et.e2);
	assertFalse(i.hasMember(Integer.valueOf(0), null));
	assertFalse(i.hasMember(Integer.valueOf(2), null));

	i = new Intersection();
	i.addType(et.e0);
	i.addType(et.e01);
	assertTrue(i.hasMember(Integer.valueOf(0), null));
	assertFalse(i.hasMember(Integer.valueOf(1), null));
    }

    public void testMatching() {
	Intersection i0 = new Intersection();
	i0.addType(et.e0);
	Intersection i1 = new Intersection();
	i1.addType(et.e1);
	Intersection i01 = new Intersection();
	i01.addType(et.e01);
	Intersection i2 = new Intersection();
	i2.addType(et.e2);

	assertTrue(i01.matches(i0, null));
	assertFalse(i0.matches(i01, null));
	assertFalse(i0.matches(i2, null));
	assertFalse(i2.matches(i01, null));
	assertFalse(i2.matches(i01, null));
	assertTrue(i0.matches(i0, null));
	assertTrue(i01.matches(i01, null));

	assertTrue(i0.matches(et.e0, null));
	assertFalse(i0.matches(et.e1, null));

	assertTrue(i01.matches(et.e0, null));
	assertTrue(i01.matches(et.e01, null));
	assertFalse(i01.matches(et.e2, null));

	i01.addType(et.e1);
	assertTrue(i01.matches(i1, null));
	assertFalse(i01.matches(i0, null));

	assertFalse(i01.matches(et.e01, null));
	assertFalse(i01.matches(et.e2, null));
    }

    public void testDisjoint() {
	Intersection i0 = new Intersection();
	i0.addType(et.e0);
	Intersection i1 = new Intersection();
	i1.addType(et.e1);
	Intersection i01 = new Intersection();
	i01.addType(et.e01);
	Intersection i2 = new Intersection();
	i2.addType(et.e2);
	
	assertTrue(i0.isDisjointWith(et.e1, null));
	assertTrue(i0.isDisjointWith(i1, null));
	assertFalse(i01.isDisjointWith(et.e1, null));
	assertFalse(i01.isDisjointWith(i1, null));
    }
}
