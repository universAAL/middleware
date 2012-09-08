package org.universAAL.middleware.owl;

import org.universAAL.middleware.owl.testont.MyClass1;
import org.universAAL.middleware.rdf.TypeMapper;

import junit.framework.TestCase;

public class EnumerationTest extends TestCase {

    Integer i0 = new Integer(0);
    Integer i1 = new Integer(1);
    Integer i2 = new Integer(2);

    Object[] a0 = { i0 };
    Object[] a1 = { i1 };
    Object[] a2 = { i2 };
    Object[] a01 = { i0, i1 };
    Object[] a012 = { i0, i1, i2 };

    public Enumeration e0 = new Enumeration(a0);
    public Enumeration e1 = new Enumeration(a1);
    public Enumeration e2 = new Enumeration(a2);
    public Enumeration e01 = new Enumeration(a01);
    public Enumeration e012 = new Enumeration(a012);

    protected void setUp() throws Exception {
	super.setUp();
    }

    public void testMethods() {
	// --
	assertTrue(e0.getNamedSuperclasses().length == 1);
	assertTrue(e0.getNamedSuperclasses()[0].equals(TypeMapper
		.getDatatypeURI(i0)));
	assertTrue(e0.getNamedSuperclasses()[0].equals(TypeMapper
		.getDatatypeURI(Integer.class)));
	assertTrue(e0.getUpperEnumeration().length == 1);
	assertTrue(e0.getUpperEnumeration()[0] == i0);

	assertTrue(e01.getNamedSuperclasses().length == 1);
	assertTrue(e01.getNamedSuperclasses()[0].equals(TypeMapper
		.getDatatypeURI(i0)));
	assertTrue(e01.getNamedSuperclasses()[0].equals(TypeMapper
		.getDatatypeURI(Integer.class)));
	assertTrue(e01.getUpperEnumeration().length == 2);
	assertTrue(e01.getUpperEnumeration()[0] == i0);
	assertTrue(e01.getUpperEnumeration()[1] == i1);

	// --
	assertTrue(e012.getMinValue() == i0);
	assertTrue(e012.getMaxValue() == i2);
	assertFalse(e012.getMinValue() == i1);

	// -- this may change --
	Enumeration e_1 = new Enumeration(new Object[] { Integer.valueOf(0),
		MyClass1.MY_URI });
	assertTrue(e_1.size() == 2);
	e_1.addValue(Integer.valueOf(1));
	assertTrue(e_1.size() == 3);
    }

    public void testHasMember() {
	assertTrue(e0.hasMember(i0, null));
	assertTrue(e1.hasMember(i1, null));

	assertFalse(e0.hasMember(i1, null));
	assertFalse(e1.hasMember(i0, null));

	assertTrue(e01.hasMember(i0, null));
	assertTrue(e01.hasMember(i1, null));
    }

    public void testMatching() {
	assertTrue(e01.matches(e0, null));
	assertTrue(e01.matches(e1, null));
	assertTrue(e01.matches(e01, null));
	assertFalse(e01.matches(e2, null));
	assertFalse(e1.matches(e01, null));
    }

    public void testDisjoint() {
	assertTrue(e0.isDisjointWith(e1, null));
	assertTrue(e1.isDisjointWith(e0, null));
	assertFalse(e01.isDisjointWith(e0, null));
	assertFalse(e01.isDisjointWith(e1, null));
	assertFalse(e0.isDisjointWith(e01, null));
	assertFalse(e1.isDisjointWith(e01, null));
    }
}
