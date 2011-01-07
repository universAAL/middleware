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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.universAAL.middleware.io.owl.AccessImpairment;
import org.universAAL.middleware.io.owl.DialogType;
import org.universAAL.middleware.io.owl.Gender;
import org.universAAL.middleware.io.owl.Modality;
import org.universAAL.middleware.io.owl.PrivacyLevel;
import org.universAAL.middleware.io.rdf.Form;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.owl.supply.LevelRating;
import org.universAAL.middleware.rdf.Resource;

/**
 * Instances of this class can be used to exchange info about system output. The
 * output publishers just need to provide the dialog script in XForms along with
 * the addressed user, the type of the dialog, and the language and privacy
 * level of the content. The output bus will then add the adaptation parameters
 * to the event (by asking the profiling component) before selecting the
 * appropriate I/O handler and forwarding the event to it.
 * 
 * @author mtazari
 * 
 */
public class OutputEvent extends Resource {
	public static final String uAAL_OUTPUT_NAMESPACE = uAAL_NAMESPACE_PREFIX
			+ "Output.owl#";
	public static final String MY_URI = uAAL_OUTPUT_NAMESPACE + "OutputEvent";

	public static final String PROP_ADDRESSED_USER = uAAL_OUTPUT_NAMESPACE
			+ "addressedUser";
	public static final String PROP_DIALOG_FORM = uAAL_OUTPUT_NAMESPACE
			+ "dialogForm";
	public static final String PROP_DIALOG_PRIORITY = uAAL_OUTPUT_NAMESPACE
			+ "dialogPriority";
	public static final String PROP_HAS_ACCESS_IMPAIRMENT = uAAL_OUTPUT_NAMESPACE
			+ "hasAccessImpairment";
	public static final String PROP_OUTPUT_LANGUAGE = uAAL_OUTPUT_NAMESPACE
			+ "outputLanguage";
	public static final String PROP_OUTPUT_MODALITY = uAAL_OUTPUT_NAMESPACE
			+ "outputModality";
	public static final String PROP_OUTPUT_MODALITY_ALT = uAAL_OUTPUT_NAMESPACE
			+ "altOutputModality";
	public static final String PROP_PRESENTATION_LOCATION = uAAL_OUTPUT_NAMESPACE
			+ "presentationLocation";
	public static final String PROP_PRIVACY_LEVEL = uAAL_OUTPUT_NAMESPACE
			+ "privacyLevel";
	public static final String PROP_SCREEN_RESOLUTION_MAX_X = uAAL_OUTPUT_NAMESPACE
			+ "screenResolutionMaxX";
	public static final String PROP_SCREEN_RESOLUTION_MAX_Y = uAAL_OUTPUT_NAMESPACE
			+ "screenResolutionMaxY";
	public static final String PROP_SCREEN_RESOLUTION_MIN_X = uAAL_OUTPUT_NAMESPACE
			+ "screenResolutionMinX";
	public static final String PROP_SCREEN_RESOLUTION_MIN_Y = uAAL_OUTPUT_NAMESPACE
			+ "screenResolutionMinY";
	public static final String PROP_VOICE_GENDER = uAAL_OUTPUT_NAMESPACE
			+ "voiceGander";
	public static final String PROP_VOICE_LEVEL = uAAL_OUTPUT_NAMESPACE
			+ "voiceLevel";

	static {
		addResourceClass(MY_URI, OutputEvent.class);
	}

	/**
	 * This constructor is for the exclusive usage by deserializers.
	 */
	public OutputEvent() {
		super();
	}

	public OutputEvent(Resource user, Form dialogForm,
			LevelRating dialogPriority, Locale dialogLang,
			PrivacyLevel dialogPrivacy) {
		super();

		addType(MY_URI, true);
		props.put(PROP_ADDRESSED_USER, user);
		props.put(PROP_DIALOG_FORM, dialogForm);
		props.put(PROP_DIALOG_PRIORITY,
				(dialogPriority == null) ? LevelRating.low : dialogPriority);
		props.put(PROP_OUTPUT_LANGUAGE, dialogLang);
		props.put(PROP_PRIVACY_LEVEL, dialogPrivacy);
	}

	public Resource getAddressedUser() {
		return (Resource) props.get(PROP_ADDRESSED_USER);
	}

	public Modality getAltOutputModality() {
		return (Modality) props.get(PROP_OUTPUT_MODALITY_ALT);
	}

	public String getDialogID() {
		Form f = getDialogForm();
		return (f == null) ? null : f.getDialogID();
	}

	public Locale getDialogLanguage() {
		return (Locale) props.get(PROP_OUTPUT_LANGUAGE);
	}

	public LevelRating getDialogPriority() {
		return (LevelRating) props.get(PROP_DIALOG_PRIORITY);
	}

	public PrivacyLevel getDialogPrivacyLevel() {
		return (PrivacyLevel) props.get(PROP_PRIVACY_LEVEL);
	}

	public Form getDialogForm() {
		return (Form) props.get(PROP_DIALOG_FORM);
	}

	public DialogType getDialogType() {
		Form f = getDialogForm();
		return (f == null) ? null : f.getDialogType();
	}

	public AccessImpairment[] getImpairments() {
		List l = (List) props.get(PROP_HAS_ACCESS_IMPAIRMENT);
		return (l == null) ? null : (AccessImpairment[]) l
				.toArray(new AccessImpairment[l.size()]);
	}

	public Modality getOutputModality() {
		return (Modality) props.get(PROP_OUTPUT_MODALITY);
	}

	public AbsLocation getPresentationAbsLocation() {
		return (AbsLocation) props.get(PROP_PRESENTATION_LOCATION);
	}

	public int getPropSerializationType(String propURI) {
		return PROP_DIALOG_FORM.equals(propURI) ? PROP_SERIALIZATION_FULL
				: PROP_SERIALIZATION_REDUCED;
	}

	public int getScreenResolutionMaxX() {
		Integer i = (Integer) props.get(PROP_SCREEN_RESOLUTION_MAX_X);
		return (i == null) ? -1 : i.intValue();
	}

	public int getScreenResolutionMaxY() {
		Integer i = (Integer) props.get(PROP_SCREEN_RESOLUTION_MAX_Y);
		return (i == null) ? -1 : i.intValue();
	}

	public int getScreenResolutionMinX() {
		Integer i = (Integer) props.get(PROP_SCREEN_RESOLUTION_MIN_X);
		return (i == null) ? -1 : i.intValue();
	}

	public int getScreenResolutionMinY() {
		Integer i = (Integer) props.get(PROP_SCREEN_RESOLUTION_MIN_Y);
		return (i == null) ? -1 : i.intValue();
	}

	public Gender getVoiceGender() {
		return (Gender) props.get(PROP_VOICE_GENDER);
	}

	public int getVoiceLevel() {
		Integer i = (Integer) props.get(PROP_VOICE_LEVEL);
		return (i == null) ? -1 : i.intValue();
	}

	public void setAltOutputModality(Modality outputModality) {
		if (outputModality != null
				&& !props.containsKey(PROP_OUTPUT_MODALITY_ALT))
			props.put(PROP_OUTPUT_MODALITY_ALT, outputModality);
	}

	public void setCollectedInput(Resource data) {
		Form f = getDialogForm();
		if (f != null)
			f.substituteData(data);
	}

	public void setImpairments(AccessImpairment[] impairments) {
		if (impairments != null && impairments.length > 0
				&& !props.containsKey(PROP_HAS_ACCESS_IMPAIRMENT))
			props.put(PROP_HAS_ACCESS_IMPAIRMENT, Arrays.asList(impairments));
	}

	public void setOutputModality(Modality outputModality) {
		if (outputModality != null && !props.containsKey(PROP_OUTPUT_MODALITY))
			props.put(PROP_OUTPUT_MODALITY, outputModality);
	}

	public void setPresentationAbsLocation(AbsLocation presentationLocation) {
		if (presentationLocation != null
				&& !props.containsKey(PROP_PRESENTATION_LOCATION))
			props.put(PROP_PRESENTATION_LOCATION, presentationLocation);
	}

	public void setPrivacyMapping(PrivacyLevel pl) {
		if (props.containsKey(PROP_PRIVACY_LEVEL)
				&& (pl == PrivacyLevel.insensible || pl == PrivacyLevel.personal))
			props.put(PROP_PRIVACY_LEVEL, pl);
	}

	public void setScreenResolutionMaxX(int x) {
		if (x > 0 && !props.containsKey(PROP_SCREEN_RESOLUTION_MAX_X))
			props.put(PROP_SCREEN_RESOLUTION_MAX_X, new Integer(x));
	}

	public void setScreenResolutionMaxY(int y) {
		if (y > 0 && !props.containsKey(PROP_SCREEN_RESOLUTION_MAX_Y))
			props.put(PROP_SCREEN_RESOLUTION_MAX_Y, new Integer(y));
	}

	public void setScreenResolutionMinX(int x) {
		if (x > 0 && !props.containsKey(PROP_SCREEN_RESOLUTION_MIN_X))
			props.put(PROP_SCREEN_RESOLUTION_MIN_X, new Integer(x));
	}

	public void setScreenResolutionMinY(int y) {
		if (y > 0 && !props.containsKey(PROP_SCREEN_RESOLUTION_MIN_Y))
			props.put(PROP_SCREEN_RESOLUTION_MIN_Y, new Integer(y));
	}

	public void setVoiceGender(Gender g) {
		if (g != null && !props.containsKey(PROP_VOICE_GENDER))
			props.put(PROP_VOICE_GENDER, g);
	}

	public void setVoiceLevel(int loudnessPercentage) {
		if (loudnessPercentage > -1 && loudnessPercentage < 101
				&& !props.containsKey(PROP_VOICE_LEVEL))
			props.put(PROP_VOICE_LEVEL, new Integer(loudnessPercentage));
	}
}
