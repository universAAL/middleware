/**
 * 
 */
package org.universAAL.middleware.ui.rdf;

import junit.framework.TestCase;

/**
 * @author mtazari
 * 
 */
public class RangeTest extends TestCase {

    public static final String MY_DUMMY_PROP = "urn:dummy#dummyProp";

    public RangeTest(String name) {
	super(name);
    }

    public void testRange() {
	// PropertyPath pp = new PropertyPath(null, false,
	// new String[]{MY_DUMMY_PROP});
	// // OrderingRestriction ordrRestr =
	// OrderingRestriction.newOrderingRestriction(
	// // new Integer(30), new Integer(0), true, true,
	// // Restriction.getAllValuesRestriction(MY_DUMMY_PROP,
	// // TypeMapper.getDatatypeURI(Integer.class)));
	// MergedRestriction mres =
	// MergedRestriction.getAllValuesRestriction(MY_DUMMY_PROP,
	// TypeMapper.getDatatypeURI(Integer.class)).addRestriction(
	// new BoundingValueRestriction(MY_DUMMY_PROP, new Integer(0), true,
	// new Integer(30), true));
	//		
	// Range rng = new Range(Form.newDialog("title", (Resource)
	// null).getIOControls(),
	// new Label("Dummy", null),
	// pp, mres, new Integer(10));
	// System.out.println(rng.getLabel().getText());
    }

}
