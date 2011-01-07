/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either.ss or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.universAAL.middleware.io.owl.Modality;
import org.universAAL.middleware.owl.ClassExpression;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.middleware.rdf.Resource;

/**
 * @author mtazari
 * 
 */
public class OutputEventPattern extends Resource {
	public static final String MY_URI = OutputEvent.uAAL_OUTPUT_NAMESPACE
			+ "OutputEventPattern";
	public static final String PROP_INPUT_MODALITY = OutputEvent.uAAL_OUTPUT_NAMESPACE
			+ "inputModality";

	public static final int MATCH_LEVEL_FAILED = 0;
	public static final int MATCH_LEVEL_ALT = 1;
	public static final int MATCH_LEVEL_SUCCESS = 2;

	static {
		addResourceClass(MY_URI, OutputEventPattern.class);
	}

	private List restrictions;

	public OutputEventPattern() {
		super();
		addType(MY_URI, true);
		restrictions = new ArrayList(12);
		props.put(ClassExpression.PROP_RDFS_SUB_CLASS_OF, restrictions);
	}

	public void addRestriction(Restriction r) {
		if (r == null)
			return;

		String prop = r.getOnProperty();
		if (OutputEvent.PROP_HAS_ACCESS_IMPAIRMENT.equals(prop)
				|| OutputEvent.PROP_OUTPUT_LANGUAGE.equals(prop)
				|| OutputEvent.PROP_OUTPUT_MODALITY.equals(prop)
				|| OutputEvent.PROP_PRESENTATION_LOCATION.equals(prop)
				|| OutputEvent.PROP_PRIVACY_LEVEL.equals(prop)
				|| OutputEvent.PROP_SCREEN_RESOLUTION_MAX_X.equals(prop)
				|| OutputEvent.PROP_SCREEN_RESOLUTION_MAX_Y.equals(prop)
				|| OutputEvent.PROP_SCREEN_RESOLUTION_MIN_X.equals(prop)
				|| OutputEvent.PROP_SCREEN_RESOLUTION_MIN_Y.equals(prop)
				|| OutputEvent.PROP_VOICE_GENDER.equals(prop)
				|| OutputEvent.PROP_VOICE_LEVEL.equals(prop))
			if (propRestrictionAllowed(prop))
				restrictions.add(r);
	}

	public int getNumberOfSupportedInputModalities() {
		List l = (List) props.get(PROP_INPUT_MODALITY);
		return (l == null) ? 0 : l.size();
	}

	private Restriction getRestriction(String onProp) {
		for (int i = 0; i < restrictions.size(); i++) {
			Restriction r = (Restriction) restrictions.get(i);
			if (r.getOnProperty().equals(onProp))
				return r;
		}
		return null;
	}

	public Modality[] getSupportedInputModalities() {
		List l = (List) props.get(PROP_INPUT_MODALITY);
		return (l == null) ? null : (Modality[]) l.toArray(new Modality[l
				.size()]);
	}

	public int matches(OutputEvent oe) {
		if (oe == null)
			return MATCH_LEVEL_FAILED;

		int result = MATCH_LEVEL_SUCCESS;
		for (int i = 0; i < restrictions.size(); i++) {
			Restriction r = (Restriction) restrictions.get(i);
			if (!r.hasMember(oe, null))
				if (OutputEvent.PROP_OUTPUT_MODALITY.equals(r.getOnProperty())
						&& r.copyOnNewProperty(
								OutputEvent.PROP_OUTPUT_MODALITY_ALT)
								.hasMember(oe, null)) {
					result = MATCH_LEVEL_ALT;
					continue;
				} else
					return MATCH_LEVEL_FAILED;
		}

		return result;
	}

	public boolean matches(OutputEventPattern subtype) {
		if (subtype == null)
			return false;

		for (int i = 0; i < restrictions.size(); i++) {
			Restriction r = (Restriction) restrictions.get(i), subR = subtype
					.getRestriction(r.getOnProperty());
			if (subR == null || !r.matches(subR, null))
				return false;
		}

		return true;
	}

	/**
	 * @see org.universAAL.middleware.rdf.Resource#isClosedCollection(java.lang.String)
	 */
	public boolean isClosedCollection(String propURI) {
		return !ClassExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)
				&& super.isClosedCollection(propURI);
	}

	public boolean isWellFormed() {
		return true;
	}

	private boolean propRestrictionAllowed(String prop) {
		for (int i = 0; i < restrictions.size(); i++)
			if (prop
					.equals(((Restriction) restrictions.get(i)).getOnProperty()))
				return false;
		return true;
	}

	public void setProperty(String propURI, Object o) {
		if (ClassExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)) {
			if (o instanceof Restriction)
				addRestriction((Restriction) o);
			else if (o instanceof List)
				for (int i = 0; i < ((List) o).size(); i++)
					if (((List) o).get(i) instanceof Restriction)
						addRestriction((Restriction) ((List) o).get(i));
		} else if (PROP_INPUT_MODALITY.equals(propURI)
				&& o instanceof Modality[])
			setSupportedInputModalities((Modality[]) o);
	}

	public void setSupportedInputModalities(Modality[] modalities) {
		if (modalities != null && modalities.length > 0
				&& !props.containsKey(PROP_INPUT_MODALITY))
			props.put(PROP_INPUT_MODALITY, Arrays.asList(modalities));
	}
}
