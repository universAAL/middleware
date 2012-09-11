package org.universAAL.middleware.owl;

import org.universAAL.middleware.owl.testont.MyClass1;
import org.universAAL.middleware.owl.testont.MyClass2;
import org.universAAL.middleware.owl.testont.MyClass3;
import org.universAAL.middleware.owl.testont.MyOntology;
import junit.framework.TestCase;

public class MergedRestrictionTest extends TestCase {

    private MyOntology ont = new MyOntology();

    protected void setUp() throws Exception {
	super.setUp();

	OntologyManagement.getInstance().register(new DataRepOntology());
	OntologyManagement.getInstance().register(ont);
    }

    public void testAppend() {
	MergedRestriction root = null;
	MergedRestriction m1;
	PropertyRestriction p;
	TypeURI typeURI;

	// -----
	// adding type after value
	MyClass2 myClass2 = new MyClass2("MyClass2Instance");

	// prepare instance for hasMember
	MyClass1 test1 = new MyClass1("something1");
	MyClass2 test2 = new MyClass2(myClass2.getURI());
	MyClass3 test3 = new MyClass3("something3");

	for (int i = 0; i < 2; i++) {
	    root = null;
	    test1.setProperty(MyClass1.PROP_C1C2, test2);
	    test2.setProperty(MyClass2.PROP_C2C3, test3);

	    if (i == 0) {
		System.out.println("-- test: add type then value");
		/*-
		 * add type then value
		 *	--PROP_C1C2-->      --PROP_C2C3--> Type
		 *	--PROP_C1C2--> Val
		 */
		m1 = MergedRestriction.getAllValuesRestriction(
			MyClass2.PROP_C2C3, MyClass3.MY_URI);
		root = m1.appendTo(root, new String[] { MyClass1.PROP_C1C2,
			MyClass2.PROP_C2C3 });
		assertFalse(root == null);
		m1 = MergedRestriction.getFixedValueRestriction(
			MyClass1.PROP_C1C2, myClass2);
		root = m1.appendTo(root, new String[] { MyClass1.PROP_C1C2 });
		assertFalse(root == null);
	    } else {
		System.out.println("-- test: add value then type");
		/*-
		 * add value then type
		 *	--PROP_C1C2--> Val
		 *	--PROP_C1C2-->      --PROP_C2C3--> Type
		 */
		m1 = MergedRestriction.getFixedValueRestriction(
			MyClass1.PROP_C1C2, myClass2);
		root = m1.appendTo(root, new String[] { MyClass1.PROP_C1C2 });
		assertFalse(root == null);
		m1 = MergedRestriction.getAllValuesRestriction(
			MyClass2.PROP_C2C3, MyClass3.MY_URI);
		root = m1.appendTo(root, new String[] { MyClass1.PROP_C1C2,
			MyClass2.PROP_C2C3 });
		assertFalse(root == null);
	    }

	    // test the result: test value
	    assertTrue(root.size() == 2);
	    p = root.getRestriction(MergedRestriction.hasValueID);
	    assertFalse(p == null);
	    assertTrue(p.getConstraint() == myClass2);
	    // test the result: test type
	    p = root.getRestriction(MergedRestriction.allValuesFromID);
	    assertFalse(p == null);
	    assertTrue(p.getConstraint() instanceof AllValuesFromRestriction);
	    p = (PropertyRestriction) p.getConstraint();
	    assertTrue(p.getConstraint() instanceof TypeURI);
	    typeURI = (TypeURI) p.getConstraint();
	    assertTrue(MyClass3.MY_URI.equals(typeURI.getURI()));

	    // test hasMember
	    assertTrue(root.hasMember(test1, null));
	    test1.setProperty(MyClass1.PROP_C1C2, new MyClass2(myClass2
		    .getURI()));
	    assertTrue(root.hasMember(test1, null));
	}
    }
}
