/**
 * 
 */
package org.universAAL.middleware.io.rdf;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.owl.OrderingRestriction;
import org.universAAL.middleware.owl.Restriction;

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
		PropertyPath pp = new PropertyPath(null, false,
				new String[]{MY_DUMMY_PROP});
		OrderingRestriction ordrRestr = OrderingRestriction.newOrderingRestriction(
				new Integer(30), new Integer(0), true, true,
				Restriction.getAllValuesRestriction(MY_DUMMY_PROP,
						TypeMapper.getDatatypeURI(Integer.class)));
		Range rng = new Range(Form.newDialog("title", (Resource) null).getIOControls(),
				new Label("Dummy", null),
				pp, ordrRestr, new Integer(10));
		System.out.println(rng.getLabel().getText());
	}

}
