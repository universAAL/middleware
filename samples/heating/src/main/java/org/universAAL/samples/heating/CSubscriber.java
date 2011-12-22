package org.universAAL.samples.heating;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.ontology.weather.TempSensor;

public class CSubscriber extends ContextSubscriber {

	protected CSubscriber(ModuleContext context,
			ContextEventPattern[] initialSubscriptions) {
		super(context, getPermanentSubscriptions());
		// TODO Auto-generated constructor stub
	}

	public void communicationChannelBroken() {
		// TODO Auto-generated method stub

	}

	public void handleContextEvent(ContextEvent event) {
		System.out.println("");

		System.out.println("----------------sub=" + event.getSubjectURI()
				+ "\n pred=" + event.getRDFPredicate()
				+ "\n ****THE TEMPERATURE IS :**** obj=" + event.getRDFObject()
				+ "\n **** extra=" + event.getTimestamp());

	}

	// only one conctructor usually there are 2

	private static ContextEventPattern[] getPermanentSubscriptions() {
		ContextEventPattern[] ceps = new ContextEventPattern[1];

		ceps[0] = new ContextEventPattern();
		ceps[0].addRestriction(Restriction.getAllValuesRestriction(
				ContextEvent.PROP_RDF_SUBJECT, TempSensor.MY_URI));
		ceps[0].addRestriction(Restriction
				.getFixedValueRestriction(ContextEvent.PROP_RDF_PREDICATE,
						TempSensor.PROP_MEASURED_VALUE));

		return ceps;
	}

}