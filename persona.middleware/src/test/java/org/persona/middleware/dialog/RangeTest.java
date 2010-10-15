/**
 * 
 */
package org.persona.middleware.dialog;

import org.persona.middleware.PResource;
import org.persona.middleware.TypeMapper;
import org.persona.middleware.service.PropertyPath;
import org.persona.ontology.expr.OrderingRestriction;
import org.persona.ontology.expr.Restriction;

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
		Range rng = new Range(Form.newDialog("title", (PResource) null).getIOControls(),
				new Label("Dummy", null),
				pp, ordrRestr, new Integer(10));
		System.out.println(rng.getLabel().getText());
	}

}
