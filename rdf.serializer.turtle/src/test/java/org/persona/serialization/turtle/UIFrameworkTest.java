/**
 * 
 */
package org.persona.serialization.turtle;

import org.persona.middleware.MiddlewareConstants;
import org.persona.middleware.PResource;
import org.persona.middleware.TypeMapper;
import org.persona.middleware.input.InputEvent;
import org.persona.middleware.output.OutputEvent;
import org.persona.middleware.output.OutputEventPattern;
import org.persona.middleware.service.ServiceRequest;
import org.persona.middleware.util.ResourceComparator;
import org.persona.ontology.AccessImpairment;
import org.persona.ontology.InitialServiceDialog;
import org.persona.ontology.LevelRating;
import org.persona.ontology.Modality;
import org.persona.ontology.expr.Enumeration;
import org.persona.ontology.expr.Restriction;
import org.persona.serialization.turtle.util.HearingImpairment;
import org.persona.serialization.turtle.util.PhysicalImpairment;
import org.persona.serialization.turtle.util.SightImpairment;

import junit.framework.TestCase;

/**
 * @author mtazari
 *
 */
public class UIFrameworkTest extends TestCase {	
	
	TurtleParser s;
	
	public UIFrameworkTest(String name) {
		super(name);

		TurtleUtil.typeMapper = TypeMapper.getTypeMapper();
		s = new TurtleParser();
	}
	
	public void testMenuRequest() {
		InputEvent ie = new InputEvent(
				new PResource(
						MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX
						+ "saied"),
				null,
				InputEvent.PERSONA_MAIN_MENU_REQUEST);

		String str = s.serialize(ie);
		System.out.println(str);
		new ResourceComparator().printDiffs(ie, (PResource) s.deserialize(str));
		System.out.println();
		System.out.println();
	}
	
	public void testOBusSubscription() {
		OutputEventPattern oep = new OutputEventPattern();
		oep.addRestriction(Restriction.getAllValuesRestriction(
				OutputEvent.PROP_HAS_ACCESS_IMPAIRMENT, new Enumeration(
						new AccessImpairment[] {
								new HearingImpairment(LevelRating.low),
								new HearingImpairment(LevelRating.middle),
								new HearingImpairment(LevelRating.high),
								new HearingImpairment(LevelRating.full),
								new SightImpairment(LevelRating.low),
								new PhysicalImpairment(LevelRating.low)})));
		oep.addRestriction(Restriction.getFixedValueRestriction(
				OutputEvent.PROP_OUTPUT_MODALITY, Modality.gui));
		PResource pr = new PResource();
		pr.addType(PResource.PERSONA_VOCABULARY_NAMESPACE + "Subscription", true);
		pr.setProperty(PResource.PERSONA_VOCABULARY_NAMESPACE + "theSubscriber", "urn:org.persona.aal_space:tes_env#123cc35472e@PC1581+f2ed514f_1");
		pr.setProperty(PResource.PERSONA_VOCABULARY_NAMESPACE + "theSubscription", oep);
		
		String str = s.serialize(pr);
		System.out.println(str);
		new ResourceComparator().printDiffs(pr, (PResource) s.deserialize(str));
		System.out.println();
		System.out.println();
	}
	
	public void testInitialDialogRequest() {
		ServiceRequest sr = InitialServiceDialog.getInitialDialogRequest(
				"http://ontology.persona.ima.igd.fhg.de/Nutritional.owl#Nutritional",
				"http://www.tsb.upv.es",
				new PResource(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX + "saied"));

		String str = s.serialize(sr);
		System.out.println(str);
		new ResourceComparator().printDiffs(sr, (PResource) s.deserialize(str));
	}
}

