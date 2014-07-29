package org.universAAL.middleware.owl;

import junit.framework.TestCase;

public class BoundedValueRestrictionTest extends TestCase {

    protected void setUp() throws Exception {
	super.setUp();
    }

    public void testHasMember() {
	// BoundingValueRestriction r = new BoundingValueRestriction("propURI",
	// new Integer(0), true, new Integer(100), true);
	// System.out.println(r.hasMember(new Integer(50), null));
	// assertTrue(r.hasMember(new Integer(50), null));

	IntRestriction ir = new IntRestriction(0, true, 100, false);

	assertTrue(ir.hasMember(new Integer(50)));
	assertTrue(ir.hasMember(new Integer(0)));
	assertFalse(ir.hasMember(new Integer(100)));
	assertFalse(ir.hasMember(new Integer(-1)));
	assertFalse(ir.hasMember(new Integer(101)));
	
	assertFalse(ir.hasMember("test"));
    }
}
