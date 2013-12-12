package org.universAAL.middleware.owl;

import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.owl.testont.MyClass1;
import org.universAAL.middleware.owl.testont.MyClass2;
import org.universAAL.middleware.owl.testont.MyClass3;
import org.universAAL.middleware.owl.testont.MyClass3Sub1;
import org.universAAL.middleware.owl.testont.MyOntology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.UnmodifiableResource;
import org.universAAL.middleware.rdf.UnmodifiableResourceList;

import junit.framework.TestCase;

public class MergedRestrictionTest extends TestCase {

    private MyOntology ont = new MyOntology();
    private static ModuleContext mc = null;

    protected void setUp() throws Exception {
	super.setUp();

	if (mc != null)
	    return;

	mc = new JUnitModuleContext();

	// init data representation
	SharedResources.moduleContext = mc;
	SharedResources.loadReasoningEngine();
	OntologyManagement.getInstance().register(
		SharedResources.moduleContext, ont);
    }

    public void testAppend1() {
	MergedRestriction root = null;
	MergedRestriction m1;
	MergedRestriction m2 = null;
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
	    assertTrue(root.hasMember(test1));
	    test1.setProperty(MyClass1.PROP_C1C2,
		    new MyClass2(myClass2.getURI()));
	    assertTrue(root.hasMember(test1));

	    // test matching
	    if (i == 0) {
		m2 = root;
	    } else {
		assertTrue(m2.matches(root));
		assertFalse(m1.matches(m2));
		assertFalse(m2.matches(m1));
	    }
	}

	// test matching: create a new root as m2, a specialized version
	m2 = null;
	m1 = MergedRestriction.getFixedValueRestriction(MyClass1.PROP_C1C2,
		myClass2);
	m2 = m1.appendTo(m2, new String[] { MyClass1.PROP_C1C2 });
	assertFalse(m2 == null);
	m1 = MergedRestriction.getAllValuesRestriction(MyClass2.PROP_C2C3,
		MyClass3Sub1.MY_URI);
	m2 = m1.appendTo(m2, new String[] { MyClass1.PROP_C1C2,
		MyClass2.PROP_C2C3 });
	assertFalse(m2 == null);

	assertTrue(root.matches(m2));
	assertFalse(m2.matches(root));

	// test matching: create a new root as m2, but with different type at
	// the end
	m2 = null;
	m1 = MergedRestriction.getFixedValueRestriction(MyClass1.PROP_C1C2,
		myClass2);
	m2 = m1.appendTo(m2, new String[] { MyClass1.PROP_C1C2 });
	assertFalse(m2 == null);
	m1 = MergedRestriction.getAllValuesRestriction(MyClass2.PROP_C2C3,
		MyClass2.MY_URI);
	m2 = m1.appendTo(m2, new String[] { MyClass1.PROP_C1C2,
		MyClass2.PROP_C2C3 });
	assertFalse(m2 == null);

	assertFalse(root.matches(m2));
	assertFalse(m2.matches(root));
    }

    public void testAppend2() {
	MergedRestriction root = null;
	MergedRestriction m1;
	PropertyRestriction p;

	// -----
	// adding value after type
	MyClass3 myClass3 = new MyClass3("MyClass3Instance");

	/*-
	 * add value then type
	 *	--PROP_C1C2--> Type
	 *	--PROP_C1C2-->      --PROP_C2C3--> Val
	 */
	m1 = MergedRestriction.getAllValuesRestriction(MyClass1.PROP_C1C2,
		MyClass2.MY_URI);
	root = m1.appendTo(root, new String[] { MyClass1.PROP_C1C2 });
	assertFalse(root == null);

	m1 = MergedRestriction.getFixedValueRestriction(MyClass2.PROP_C2C3,
		myClass3);
	root = m1.appendTo(root, new String[] { MyClass1.PROP_C1C2,
		MyClass2.PROP_C2C3 });
	assertFalse(root == null);

	// test the result
	assertTrue(root.size() == 1);
	p = root.getRestriction(MergedRestriction.allValuesFromID);
	assertFalse(p == null);
	assertTrue(p.getConstraint() instanceof Intersection);
	Intersection i = (Intersection) p.getConstraint();
	assertFalse(i instanceof MergedRestriction);
	assertTrue(i.size() == 2);
	UnmodifiableResourceList l = (UnmodifiableResourceList) i.elements();
	// the list has a TypURI and a HasValue
	// test TypeURI
	UnmodifiableResource ur = null;
	for (int j = 0; j < 2; j++)
	    if (((UnmodifiableResource) (l.get(j))).instanceOf(TypeURI.class))
		ur = (UnmodifiableResource) l.get(j);
	assertFalse(ur == null);
	assertTrue(MyClass2.MY_URI.equals(ur.getURI()));
	// test HasValue
	for (int j = 0; j < 2; j++)
	    if (((UnmodifiableResource) (l.get(j)))
		    .instanceOf(HasValueRestriction.class))
		ur = (UnmodifiableResource) l.get(j);
	assertFalse(ur == null);
	assertTrue(((Resource) (ur
		.getProperty(HasValueRestriction.PROP_OWL_HAS_VALUE))).getURI() == myClass3
		.getURI());
    }
}
