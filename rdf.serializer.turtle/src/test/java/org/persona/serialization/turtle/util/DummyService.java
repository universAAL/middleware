/**
 * 
 */
package org.persona.serialization.turtle.util;

import java.util.Hashtable;

import org.persona.ontology.Service;
import org.persona.ontology.expr.Restriction;

/**
 * @author mtazari
 *
 */
public class DummyService extends Service {
	public static final String MY_NAMESPACE = "http://ontology.persona.ratio.it/DummyServiceProvider.owl#";
	
	public static final String MY_URI;
	public static final String PROP_CONTROLS_BOILER;
	public static final String PROP_IS_ON;
	
	private static Hashtable locationRestrictions = new Hashtable(1);
	static {
		MY_URI = MY_NAMESPACE + "DummyService";
		PROP_CONTROLS_BOILER = MY_NAMESPACE + "controlsBoiler";
		PROP_IS_ON = MY_NAMESPACE + "isOn";
		register(DummyService.class);
	}
	
	public static Restriction getClassRestrictionsOnProperty(String propURI) {
		if (propURI == null)
			return null;
		Object r = locationRestrictions.get(propURI);
		if (r instanceof Restriction)
			return (Restriction) r;
		return Service.getClassRestrictionsOnProperty(propURI);
	}
	
	public static String getRDFSComment() {
		return "The class of services controlling boilers.";
	}
	
	public static String getRDFSLabel() {
		return "BoilerControlService";
	}
	
	public DummyService() {
		super();
	}
	
	public DummyService(String uri) {
		super(uri);
	}

	/* (non-Javadoc)
	 * @see org.persona.ontology.Service#getClassLevelRestrictions()
	 */
	protected Hashtable getClassLevelRestrictions() {
		return locationRestrictions;
	}

	/* (non-Javadoc)
	 * @see org.persona.ontology.ManagedIndividual#getPropSerializationType(java.lang.String)
	 */
	public int getPropSerializationType(String propURI) {
		return PROP_SERIALIZATION_FULL;
	}

	public boolean isWellFormed() {
		return true;
	}
}
