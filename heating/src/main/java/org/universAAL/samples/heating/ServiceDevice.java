package org.universAAL.samples.heating;

import java.util.Hashtable;

import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.ontology.phThing.Device;

public class ServiceDevice extends Service {

	public static final String MY_URI; // my number plate like a car
	public static final String PROPERTY_CONTROLS;
	private static Hashtable serviceRestrictions = new Hashtable(1);

	static {

		// My URI its called
		// http://ontology.aal-persona.org/Device.owl#SAIL_DEVICE
		// and my PROPERTY_CONTROLS
		// http://ontology.aal-persona.org/Device.owl#controls_device
		// I define a service ontology not a service.

		MY_URI = Device.uAAL_DEVICE_NAMESPACE + "SAIL_DEVICE";
		PROPERTY_CONTROLS = Device.uAAL_DEVICE_NAMESPACE + "controls_device";

		// I register the service ontology
		register(ServiceDevice.class);

		// I config the restrictions with the following parameters:

		// addRestriction(Restriction r, String[] toPath, Hashtable
		// restrictions)
		// String[] toPath this an strings array with only one string.

		// you define an ontology:
		// that is a property_controls that points a device

		// device.myuri that means that the type is device

		// serviceRestrictions: this is just a hash table it doesn't matter the
		// name
		// we can add as many restrictions as we want!
		// and can be different kind of restrictions

		addRestriction(Restriction.getAllValuesRestriction(PROPERTY_CONTROLS,
				Device.MY_URI), new String[] { PROPERTY_CONTROLS },
				serviceRestrictions);

		// java classes with owl

	}

	public static Restriction getClassRestrictionsOnProperty(String propURI) {
		if (propURI == null)
			return null;
		Object r = serviceRestrictions.get(propURI);
		if (r instanceof Restriction)
			return (Restriction) r;
		return ServiceDevice.getClassRestrictionsOnProperty(propURI);
	}

	public static String getRDFSComment() {
		return "The class of services controling devices by SAIL.";
	}

	public static String getRDFSLabel() {
		return "SAIL";
	}

	public ServiceDevice(String uri) {
		super(uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.persona.ontology.Service#getClassLevelRestrictions()
	 */
	protected Hashtable getClassLevelRestrictions() {
		return serviceRestrictions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.persona.ontology.ManagedIndividual#getPropSerializationType(java.
	 * lang.String)
	 */
	public int getPropSerializationType(String propURI) {
		return PROPERTY_CONTROLS.equals(propURI) ? PROP_SERIALIZATION_FULL
				: super.getPropSerializationType(propURI);
	}

	public boolean isWellFormed() {
		return true;
	}
}
