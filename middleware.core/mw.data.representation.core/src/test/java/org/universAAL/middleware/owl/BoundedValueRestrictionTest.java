package org.universAAL.middleware.owl;

import java.util.HashMap;

import org.universAAL.middleware.test.util.ProcessParameter;

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
    
    public void testMatchesVar1a() {
	ProcessParameter p1 = new ProcessParameter("param1");
	IntRestriction i1 = new IntRestriction(p1.asVariableReference(), true, 100, true);
	IntRestriction i2 = new IntRestriction(0, true, 100, true);
	
	HashMap<Object, Object> map = new HashMap<Object, Object>();
	boolean b = i2.matches(i1, map, -1, null);
	assertTrue(b);
	assertTrue(map.size() == 1);
	assertTrue(map.get(p1.getURI()) == Integer.valueOf(0));
    }
    
    public void testMatchesVar1b() {
	ProcessParameter p1 = new ProcessParameter("param1");
	IntRestriction i1 = new IntRestriction(p1.asVariableReference(), true, 100, true);
	IntRestriction i2 = new IntRestriction(0, true, 101, true);
	
	HashMap<Object, Object> map = new HashMap<Object, Object>();
	boolean b = i2.matches(i1, map, -1, null);
	assertTrue(b);
	assertTrue(map.size() == 1);
	assertTrue(map.get(p1.getURI()) == Integer.valueOf(0));
    }
    
    public void testMatchesVar1c() {
	ProcessParameter p1 = new ProcessParameter("param1");
	IntRestriction i1 = new IntRestriction(p1.asVariableReference(), true, 100, true);
	IntRestriction i2 = new IntRestriction(0, true, 99, true);
	
	HashMap<Object, Object> map = new HashMap<Object, Object>();
	boolean b = i2.matches(i1, map, -1, null);
	assertFalse(b);
    }
    
    public void testMatchesVar2() {
	ProcessParameter p1 = new ProcessParameter("param1");
	ProcessParameter p2 = new ProcessParameter("param2");
	IntRestriction i1 = new IntRestriction(p1.asVariableReference(), true, p2.asVariableReference(), true);
	IntRestriction i2 = new IntRestriction(0, true, 100, true);
	
	HashMap<Object, Object> map = new HashMap<Object, Object>();
	boolean b = i2.matches(i1, map, -1, null);
	assertTrue(b);
	assertTrue(map.size() == 2);
	assertTrue(map.get(p1.getURI()) == Integer.valueOf(0));
	assertTrue(map.get(p2.getURI()) == Integer.valueOf(100));
    }
}
