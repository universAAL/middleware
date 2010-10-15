/**
 * 
 */
package org.persona.serialization.turtle.util;

import org.persona.ontology.AccessImpairment;
import org.persona.ontology.LevelRating;

/**
 * @author mtazari
 *
 */
public class PhysicalImpairment extends AccessImpairment {
	public static final String MY_URI;
	
	static {
		MY_URI = PERSONA_VOCABULARY_NAMESPACE + "PhysicalImpairment";
		register(PhysicalImpairment.class);
	}
	
	public static String getRDFSComment() {
		return "Represents the level of the user's difficulty in providing input by the hands, e.g. in using maus and keyboard.";
	}
	
	public static String getRDFSLabel() {
		return "Physical Impairment";
	}
	
	/**
	 * The constructor for (de-)serializers.
	 */
	public PhysicalImpairment() {
		super();
	}
	
	/**
	 * The constructor for use by applications.
	 */
	public PhysicalImpairment(LevelRating impairmentLevel) {
		//this("http://anon_PhysicalImpairment_"+ Calendar.getInstance().getTimeInMillis() + "#", impairmentLevel);
		super(impairmentLevel);
	}
	
	public static PhysicalImpairment loadInstance() {
		return new PhysicalImpairment(LevelRating.none);
	}
	
	public void setImpairment(LevelRating rating) {
		props.put(AccessImpairment.PROP_IMPAIRMENT_LEVEL, rating);
	}
	
	public String toString() {
		return "Physical Impairment: " + this.getImpaimentLevel().name();
	}
}
