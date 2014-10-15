/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
	Copyright 2013-2014 Ericsson Nikola Tesla d.d., www.ericsson.com/hr/
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.model.matchable.Request;
import org.universAAL.middleware.owl.ComparableIndividual;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.owl.supply.LevelRating;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ScopedResource;
import org.universAAL.middleware.ui.owl.AccessImpairment;
import org.universAAL.middleware.ui.owl.DialogType;
import org.universAAL.middleware.ui.owl.Modality;
import org.universAAL.middleware.ui.owl.Preference;
import org.universAAL.middleware.ui.owl.PrivacyLevel;
import org.universAAL.middleware.ui.rdf.Form;

/**
 * Instances of this class can be used to user interaction requests.
 * Applications just need to provide the dialog's {@link Form} object along with
 * the addressed {@link User}, the priority of the dialog, and the language and
 * privacy level of the content. The {@link IUIBus} will then add the current
 * adaptation parameters to the call (by asking the Dialog Manager) before
 * selecting the appropriate {@link UIHandler} and forwarding the call to it.
 * 
 * @author mtazari
 * @author eandgrg
 * @author Carsten Stockloew
 */
public class UIRequest extends ScopedResource implements Request {

    /** The Constant uAAL_UI_NAMESPACE. */
    public static final String uAAL_UI_NAMESPACE = uAAL_NAMESPACE_PREFIX
	    + "UI.owl#";

    /** The Constant MY_URI. */
    public static final String MY_URI = uAAL_UI_NAMESPACE + "UIRequest";

    // ////////////////////////////////////////////////////////////////
    // Data added by applications when creating UIRequest
    // ////////////////////////////////////////////////////////////////
    /** The Constant PROP_ADDRESSED_USER. */
    public static final String PROP_ADDRESSED_USER = uAAL_UI_NAMESPACE
	    + "addressedUser";

    /** The Constant PROP_DIALOG_FORM. */
    public static final String PROP_DIALOG_FORM = uAAL_UI_NAMESPACE
	    + "dialogForm";

    /** The Constant PROP_DIALOG_PRIORITY. */
    public static final String PROP_DIALOG_PRIORITY = uAAL_UI_NAMESPACE
	    + "dialogPriority";

    /** The Constant PROP_DIALOG_LANGUAGE. */
    public static final String PROP_DIALOG_LANGUAGE = uAAL_UI_NAMESPACE
	    + "dialogLanguage";

    /** The Constant PROP_DIALOG_PRIVACY_LEVEL. */
    public static final String PROP_DIALOG_PRIVACY_LEVEL = uAAL_UI_NAMESPACE
	    + "dialogPrivacyLevel";

    // ///////////////////////////////////////////////////////////////
    // Additional data (added by ui.dm)
    // ///////////////////////////////////////////////////////////////

    /** The Constant PROP_PRESENTATION_LOCATION. */
    public static final String PROP_PRESENTATION_LOCATION = uAAL_UI_NAMESPACE
	    + "presentationLocation";

    /** The Constant PROP_HAS_ACCESS_IMPAIRMENT. */
    public static final String PROP_HAS_ACCESS_IMPAIRMENT = uAAL_UI_NAMESPACE
	    + "hasAccessImpairment";

    /** The Constant PROP_HAS_PREFERENCE. */
    public static final String PROP_HAS_PREFERENCE = uAAL_UI_NAMESPACE
	    + "hasPreference";

    // also obtainable by accessing
    // InteractionPreferences.preferredModality contained within
    // uiRequest.getProperty(
    // UIPreferencesSubProfile.PROP_INTERACTION_PREFERENCES) but this eases the
    // checks in UIHandlerProfile.getMatchingDegree()
    public static final String PROP_PRESENTATION_MODALITY = uAAL_UI_NAMESPACE
	    + "presentationModality";

    public static final String PROP_PRESENTATION_MODALITY_ALT = uAAL_UI_NAMESPACE
	    + "altPresentationModality";

    /**
     * This constructor is for the exclusive usage by deserializers.
     */
    public UIRequest() {
	super();
    }

    /**
     * Instantiates a new {@link UIRequest}.
     * 
     * @param user
     *            the {@link User}. It is declared as Resource because the type
     *            User is defined in the Profiling Ontology. The type is not
     *            needed for for matchmaking Either.
     * @param dialogForm
     *            The dialog form.
     * @param dialogPriority
     *            The dialog priority.
     * @param dialogLang
     *            the dialog {@link Language}.
     * @param dialogPrivacy
     *            the dialog privacy.
     */
    public UIRequest(Resource user, Form dialogForm,
	    LevelRating dialogPriority, Locale dialogLang,
	    PrivacyLevel dialogPrivacy) {
	super();

	addType(MY_URI, true);
	props.put(PROP_ADDRESSED_USER, user);
	props.put(PROP_DIALOG_FORM, dialogForm);
	props.put(PROP_DIALOG_PRIORITY,
		dialogPriority == null ? LevelRating.low : dialogPriority);
	props.put(PROP_DIALOG_LANGUAGE, dialogLang);
	props.put(PROP_DIALOG_PRIVACY_LEVEL, dialogPrivacy);
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#getPropSerializationType(java.lang.String)
     */
    @Override
    public int getPropSerializationType(String propURI) {
	return PROP_DIALOG_FORM.equals(propURI) ? PROP_SERIALIZATION_FULL
		: PROP_SERIALIZATION_REDUCED;
    }

    /**
     * Gets the addressed {@link User}.
     * 
     * @return the addressed {@link User}, It is declared as Resource because
     *         the type User is defined in the Profiling Ontology. The type is
     *         not needed for for matchmaking Either.
     */
    public Resource getAddressedUser() {
	return (Resource) props.get(PROP_ADDRESSED_USER);
    }

    /**
     * Gets the dialog {@link Form}.
     * 
     * @return the dialog {@link Form}
     */
    public Form getDialogForm() {
	return (Form) props.get(PROP_DIALOG_FORM);
    }

    /**
     * Gets the dialog id.
     * 
     * @return the dialog id
     */
    public String getDialogID() {
	Form f = getDialogForm();
	return f == null ? null : f.getDialogID();
    }

    /**
     * Gets the dialog language.
     * 
     * @return the dialog language
     */
    public Locale getDialogLanguage() {
	return (Locale) props.get(PROP_DIALOG_LANGUAGE);
    }

    /**
     * Gets the dialog priority.
     * 
     * @return the dialog priority
     */
    public LevelRating getDialogPriority() {
	return (LevelRating) props.get(PROP_DIALOG_PRIORITY);
    }

    /**
     * Gets the dialog privacy level.
     * 
     * @return the dialog privacy level
     */
    public PrivacyLevel getDialogPrivacyLevel() {
	return (PrivacyLevel) props.get(PROP_DIALOG_PRIVACY_LEVEL);
    }

    /**
     * Gets the dialog type.
     * 
     * @return the dialog type
     */
    public DialogType getDialogType() {
	Form f = getDialogForm();
	return f == null ? null : f.getDialogType();
    }

    /**
     * Gets the impairments.
     * 
     * @return the impairments
     */
    public AccessImpairment[] getImpairments() {
	List l = (List) props.get(PROP_HAS_ACCESS_IMPAIRMENT);
	return l == null ? null : (AccessImpairment[]) l
		.toArray(new AccessImpairment[l.size()]);
    }

    /**
     * Gets the Preferences.
     * 
     * @return the preferences
     */
    public Preference[] getPreferences() {
	List l = (List) props.get(PROP_HAS_PREFERENCE);
	return l == null ? null : (Preference[]) l.toArray(new Preference[l
		.size()]);
    }

    /**
     * Gets the presentation location.
     * 
     * @return the presentation location
     */
    public AbsLocation getPresentationLocation() {
	return (AbsLocation) props.get(PROP_PRESENTATION_LOCATION);
    }

    /**
     * Sets the collected input.
     * 
     * @param data
     *            the new collected input, The Resource set as form Root data.
     */
    public void setCollectedInput(Resource data) {
	Form f = getDialogForm();
	if (f != null) {
	    f.substituteData(data);
	}
    }

    /**
     * Sets the impairments.
     * 
     * @param impairments
     *            the new impairments
     */
    public void setImpairments(AccessImpairment[] impairments) {
	if (impairments != null && impairments.length > 0
		&& !props.containsKey(PROP_HAS_ACCESS_IMPAIRMENT)) {
	    props.put(PROP_HAS_ACCESS_IMPAIRMENT, Arrays.asList(impairments));
	}
    }

    /**
     * Sets the preferences.
     * 
     * @param preferences
     *            the new preferences
     */
    public void setPreferences(Preference[] preferences) {
	if (preferences != null && preferences.length > 0
		&& !props.containsKey(PROP_HAS_PREFERENCE)) {
	    props.put(PROP_HAS_PREFERENCE, Arrays.asList(preferences));
	}
    }

    /**
     * Sets the presentation location.
     * 
     * @param presentationLocation
     *            the new presentation location
     */
    public void setPresentationLocation(AbsLocation presentationLocation) {
	if (presentationLocation != null
		&& !props.containsKey(PROP_PRESENTATION_LOCATION)) {
	    props.put(PROP_PRESENTATION_LOCATION, presentationLocation);
	}
    }

    /**
     * Sets the presentation modality.
     * 
     * @param modality
     *            the new presentation modality
     */
    public void setPresentationModality(Modality modality) {
	if (modality != null && !props.containsKey(PROP_PRESENTATION_MODALITY)) {
	    props.put(PROP_PRESENTATION_MODALITY, modality);
	}
    }

    /**
     * Gets the alternative presentation modality.
     * 
     * @return the alternative presentation modality
     */
    public Modality getAltPresentationModality() {
	return (Modality) props.get(PROP_PRESENTATION_MODALITY_ALT);
    }

    /**
     * Sets the alternative presentation modality.
     * 
     * @param outputModality
     *            the new alternative presentation modality
     */
    public void setAltPresentationModality(Modality outputModality) {
	if (outputModality != null
		&& !props.containsKey(PROP_PRESENTATION_MODALITY_ALT)) {
	    props.put(PROP_PRESENTATION_MODALITY_ALT, outputModality);
	}
    }

    /**
     * @see Matchable#matches(Matchable)
     */
    public boolean matches(Matchable subset) {
	if (!(subset instanceof UIRequest))
	    return false;
	UIRequest sub = (UIRequest) subset;
	Object o1, o2;

	o1 = getAddressedUser();
	if (o1 != null) {
	    o2 = sub.getAddressedUser();
	    if (o2 != null && !(o1.equals(o2)))
		return false;
	}

	o1 = getDialogPriority();
	if (o1 != null) {
	    o2 = sub.getDialogPriority();
	    if (o2 != null
		    && !(((LevelRating) o1).greaterEqual((LevelRating) o2)))
		return false;
	}

	o1 = getDialogLanguage();
	if (o1 != null) {
	    o2 = sub.getDialogLanguage();
	    if (o2 != null && !(o1.equals(o2)))
		return false;
	}

	o1 = getDialogPrivacyLevel();
	if (o1 != null) {
	    o2 = sub.getDialogPrivacyLevel();
	    if (o2 != null
		    && !(((ComparableIndividual) o1)
			    .greaterEqual((ComparableIndividual) o2)))
		return false;
	}

	return true;
    }
}
