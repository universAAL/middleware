/**
 * 
 */
package org.persona.serialization.turtle;

import java.util.Locale;

import org.persona.middleware.MiddlewareConstants;
import org.persona.middleware.PResource;
import org.persona.middleware.TypeMapper;
import org.persona.middleware.dialog.ChoiceItem;
import org.persona.middleware.dialog.Form;
import org.persona.middleware.dialog.Group;
import org.persona.middleware.dialog.Label;
import org.persona.middleware.dialog.MediaObject;
import org.persona.middleware.dialog.Range;
import org.persona.middleware.dialog.Select;
import org.persona.middleware.dialog.SubdialogTrigger;
import org.persona.middleware.dialog.Submit;
import org.persona.middleware.dialog.TextArea;
import org.persona.middleware.output.OutputEvent;
import org.persona.middleware.service.PropertyPath;
import org.persona.middleware.util.ResourceComparator;
import org.persona.ontology.LevelRating;
import org.persona.ontology.PrivacyLevel;
import org.persona.ontology.expr.OrderingRestriction;
import org.persona.ontology.expr.Restriction;

import junit.framework.TestCase;

/**
 * @author mtazari
 * 
 */
public class FormTest extends TestCase {
	
	private static final String DUMMY_PROP_1 = "urn:dummy#dummyProp1";
	private static final String DUMMY_PROP_2 = "urn:dummy#dummyProp2";
	private static final String DUMMY_PROP_3 = "urn:dummy#dummyProp3";
	static final PResource testUser = 
		new PResource(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + "saied");

	public FormTest(String name) {
		super(name);
	}

	public void testForm() {
		PropertyPath pp1 = new PropertyPath(null, false,
				new String[] { DUMMY_PROP_1 });
		PropertyPath pp2 = new PropertyPath(null, false,
				new String[] { DUMMY_PROP_2 });
		PropertyPath pp3 = new PropertyPath(null, false,
				new String[] { DUMMY_PROP_3 });
		
		Restriction restr = Restriction.getAllValuesRestriction(
				DUMMY_PROP_2, TypeMapper.getDatatypeURI(String.class));
		Restriction selectRestr = Restriction.getAllValuesRestriction(
				DUMMY_PROP_3, TypeMapper.getDatatypeURI(Integer.class));
		OrderingRestriction ordrRestr = OrderingRestriction.newOrderingRestriction(
				new Integer(30), new Integer(0), true, true,
				Restriction.getAllValuesRestriction(DUMMY_PROP_1,
						TypeMapper.getDatatypeURI(Integer.class)));

		Form f = Form.newDialog("Test Form", testUser);
		f.setDialogCreator("Test Component");
		Group controls = f.getIOControls();
		
		Group controls2 = new Group(controls, new Label("child group1", null),
				null, null, null);
		MediaObject mo1 = new MediaObject(controls2, null, "image", "/img/meal.jpg");
		mo1.setPreferredResolution(100, 100);
		new SubdialogTrigger(controls2, new Label("Edit", null), "edit1");

		Group controls3 = new Group(controls, new Label("child group2", null),
				null, null, null);
		MediaObject mo2 = new MediaObject(controls3, null, "image", "/img/meal.jpg");
		mo2.setPreferredResolution(100, 100);
		new SubdialogTrigger(controls3, new Label("Edit", null), "edit2");
		
		Group controls4 = new Group(controls, new Label("child group3", null),
				null, null, null);
		MediaObject mo3 = new MediaObject(controls4, null, "image", "/img/meal.jpg");
		mo3.setPreferredResolution(100, 100);
		new SubdialogTrigger(controls4, new Label("Edit", null), "edit3");

		MediaObject mo = new MediaObject(controls, null, "image", "/img/meal.jpg");
		mo.setPreferredResolution(100, 100);
		new TextArea(controls, new Label("Dummy2", null), pp2, restr,
				"Coffee with milk \nToast with butter and marmelade\nFruit or juice");
		Select testSelect = new Select(controls, new Label("Dummy3", null),
				pp3, selectRestr, new Integer(0));
		for (int t = 0; t < 7; t++)
			testSelect.addChoiceItem(new ChoiceItem("myLabel"+t, null, new Integer(t)));
		new Range(controls, new Label("Dummy", null), pp1,
				ordrRestr, new Integer(10));
		
		Group mySubmits = f.getSubmits();
		new Submit(mySubmits, new Label("TODAY", null), "todayMenu");
		new Submit(mySubmits, new Label("WEEK", null), "weekMenu");
		new Submit(mySubmits, new Label("My PROFILE", null), "myProfile");
		new Submit(mySubmits, new Label("HOME", null), "startPage");

		OutputEvent oe = new OutputEvent(testUser, f,
				LevelRating.middle, Locale.ENGLISH, PrivacyLevel.insensible);

		TurtleUtil.typeMapper = TypeMapper.getTypeMapper();
		TurtleParser s = new TurtleParser();

		String str = s.serialize(oe);
		System.out.println(str);
		new ResourceComparator().printDiffs(oe, (PResource) s.deserialize(str));
	}
}
