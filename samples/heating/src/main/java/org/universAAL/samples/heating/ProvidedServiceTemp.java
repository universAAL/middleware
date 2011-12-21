package org.universAAL.samples.heating;

import java.util.Hashtable;

import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.ontology.weather.TempSensor;

public class ProvidedServiceTemp extends ServiceDevice {
	public static final String SERVER_NAMESPACE = "http://ontology.tsbtecnologias.es/TemperatureServer.owl#";

	public static final String MY_URI = SERVER_NAMESPACE + "TemperatureService";

	public static final String SERVICE_GET_VALUE = SERVER_NAMESPACE
			+ "getValue";

	public static final String OUTPUT_VALUE = SERVER_NAMESPACE + "value";

	public static final ServiceProfile[] profiles = new ServiceProfile[1];

	private static Hashtable serverRestrictions = new Hashtable();

	static {

		// I make an instance of the service
		// that is, I extended the service

		// I'm going to create a serviceprovided that will give me the home
		// temperature

		// I register it.

		register(ProvidedServiceTemp.class);

		// difference with service ontology: now we can't just put device but a
		// concrete one

		// that is TempSensor.MY_URI instead of Device.MY_URI

		// serverrestriction: this is just a hashtable it doesn't matter the
		// name.
		// it doesn’t matter the name as long as it’s a hashtable.

		addRestriction(Restriction.getAllValuesRestriction(PROPERTY_CONTROLS,
				TempSensor.MY_URI), new String[] { PROPERTY_CONTROLS },
				serverRestrictions);

		// PropertyPath(String uri, boolean isXMLLiteral, String[] thePath)

		// SERVICE_GET_VALUE=SERVER_NAMESPACE + "getValue"
		ProvidedServiceTemp getValue = new ProvidedServiceTemp(
				SERVICE_GET_VALUE);
		// We initialize the profile.
		profiles[0] = getValue.getProfile();

		ProcessOutput output = new ProcessOutput(OUTPUT_VALUE);

		// How it works output.setCardinality(,) ?
		//				  
		// We are defining the output data.
		// setCardinality(max,min)
		//				  
		// Min: minimum number of numbers
		// Max: maximum number of numbers.
		//				  
		// Example:
		// (1,0) means that it's optional.
		// (100,1) at least one value to one hundred

		output.setCardinality(1, 1); // output config only 1 value.sometimes
										// we'll put 1-100;

		profiles[0].addOutput(output);
		// I put the output and the path to the endpoint
		// the path to get the output
		profiles[0].addSimpleOutputBinding(output,
				new String[] { ServiceDevice.PROPERTY_CONTROLS,
						TempSensor.PROP_MEASURED_VALUE });

		// Why do we use the addOutputbinding?
		// We are relating the output with the path. So we are saying that you
		// can find the output value in this path.

	}

	private ProvidedServiceTemp(String uri) {
		super(uri);
	}
}
