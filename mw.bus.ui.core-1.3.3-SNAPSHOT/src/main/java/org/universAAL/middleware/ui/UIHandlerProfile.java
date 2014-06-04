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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.model.matchable.Request;
import org.universAAL.middleware.bus.model.matchable.Requirement;
import org.universAAL.middleware.bus.model.matchable.UtilityAdvertisement;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.owl.Modality;

/**
 * A profile of the {@link UIHandler} that describes its capabilites so that
 * they can be matched with {@link UIRequest} ( more specifically {@link User}
 * preferences and abilities added by the {@link IDialogManager}) and possibly
 * some additional parameters.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @author eandgrg
 * 
 */
public class UIHandlerProfile extends FinalizedResource implements
	UtilityAdvertisement {

    public static final String MY_URI = UIRequest.uAAL_UI_NAMESPACE
	    + "UIHandlerProfile";
    public static final String PROP_INPUT_MODALITY = UIRequest.uAAL_UI_NAMESPACE
	    + "inputModality";

    public static final int MATCH_LEVEL_FAILED = 0;
    public static final int MATCH_LEVEL_ALT = 1;
    public static final int MATCH_LEVEL_SUCCESS = 2;

    private List<MergedRestriction> restrictions;

    /**
     * Instantiates a new UI handler profile.
     */
    public UIHandlerProfile() {
	super();
	addType(MY_URI, true);
	restrictions = new ArrayList<MergedRestriction>(12);
	props.put(TypeExpression.PROP_RDFS_SUB_CLASS_OF, restrictions);
    }

    /**
     * Adds the restriction.
     * 
     * @param mergedRestriction
     *            the restriction
     */
    public boolean addRestriction(MergedRestriction mergedRestriction) {
	if (mergedRestriction == null) {
	    return false;
	}

	String prop = mergedRestriction.getOnProperty();
	if (UIRequest.PROP_ADDRESSED_USER.equals(prop)
		|| UIRequest.PROP_DIALOG_FORM.equals(prop)
		|| UIRequest.PROP_DIALOG_PRIORITY.equals(prop)
		|| UIRequest.PROP_DIALOG_LANGUAGE.equals(prop)
		|| UIRequest.PROP_DIALOG_PRIVACY_LEVEL.equals(prop)
		|| UIRequest.PROP_HAS_ACCESS_IMPAIRMENT.equals(prop)
		|| UIRequest.PROP_HAS_PREFERENCE.equals(prop)
		|| UIRequest.PROP_PRESENTATION_LOCATION.equals(prop)
		|| UIRequest.PROP_PRESENTATION_MODALITY.equals(prop)) {
	    if (propRestrictionAllowed(prop)) {
		restrictions.add(mergedRestriction);
		return true;
	    }
	}
	return false;
    }

    /**
     * Gets the number of supported input modalities.
     * 
     * @return the number of supported input modalities
     */
    public int getNumberOfSupportedInputModalities() {
	List l = (List) props.get(PROP_INPUT_MODALITY);
	return l == null ? 0 : l.size();
    }

    /**
     * Gets the restriction.
     * 
     * @param onProp
     *            the on prop
     * @return the restriction
     */
    private MergedRestriction getRestriction(String onProp) {
	for (int i = 0; i < restrictions.size(); i++) {
	    MergedRestriction r = restrictions.get(i);
	    if (r.getOnProperty().equals(onProp)) {
		return r;
	    }
	}
	return null;
    }

    /**
     * Gets the supported input modalities.
     * 
     * @return the supported input modalities
     */
    public Modality[] getSupportedInputModalities() {
	List l = (List) props.get(PROP_INPUT_MODALITY);
	return l == null ? null : (Modality[]) l
		.toArray(new Modality[l.size()]);
    }

    /**
     * Determines whether the given {@link UIRequest} matches this profile.
     * 
     * @param uiRequest
     *            the {@link UIRequest}
     * @return a value indicating to which degree the {@link UIRequest} matches:
     *         <ul>
     *         <li>{@link #MATCH_LEVEL_SUCCESS} if all restrictions match the
     *         request,</li>
     *         <li>{@link #MATCH_LEVEL_ALT} if at least one of the restrictions
     *         does not match the request, but this non-matching restriction is
     *         on the modality ( {@link UIRequest#PROP_PRESENTATION_MODALITY})
     *         and the alternative modality (
     *         {@link UIRequest#PROP_PRESENTATION_MODALITY_ALT}) matches, or</li>
     *         <li>{@link #MATCH_LEVEL_FAILED} if the restrictions do not match
     *         or the given {@link UIRequest} is null.</li>
     *         </ul>
     */
    public int getMatchingDegree(UIRequest uiRequest) {
	if (uiRequest == null) {
	    return MATCH_LEVEL_FAILED;
	}

	int result = MATCH_LEVEL_SUCCESS;
	for (MergedRestriction r : restrictions) {
	    // TODO added matching on the addressed user. Revise necessary on
	    // follow me scenario, we should have location of the user known
	    // for best UIstrategy but question is how do we get it and if this
	    // will be feasible in most cases.
	    if (!r.hasMember(uiRequest, null)) {
		// If there is a location of the user than match on the
		// location; if there is no location - see logged in location
		// if (UIRequest.PROP_ADDRESSED_USER.equals(r.getOnProperty()))
		// {
		// continue;
		// } else

		if (UIRequest.PROP_PRESENTATION_MODALITY.equals(r
			.getOnProperty())
			&& r.copyOnNewProperty(
				UIRequest.PROP_PRESENTATION_MODALITY_ALT)
				.hasMember(uiRequest, null)) {
		    result = MATCH_LEVEL_ALT;

		    continue;
		} else {
		    return MATCH_LEVEL_FAILED;
		}
	    }

	}

	return result;
    }

    private boolean isRestrictionOnModality(UIRequest uiRequest, MergedRestriction r) {
	return UIRequest.PROP_PRESENTATION_MODALITY.equals(r.getOnProperty())
		&& r
			.copyOnNewProperty(
				UIRequest.PROP_PRESENTATION_MODALITY_ALT)
			.hasMember(uiRequest, null);
    }

    /**
     * Determines whether this profile matches the given profile.
     * 
     * @param uiHandlerProfile
     *            the subtype
     * @return <tt>true</tt>, if successful
     */
    public boolean matches(UIHandlerProfile uiHandlerProfile) {
	if (uiHandlerProfile == null) {
	    return false;
	}

	for (MergedRestriction r : restrictions) {
	    MergedRestriction subR = uiHandlerProfile.getRestriction(r.getOnProperty());
	    if (subR == null || !r.matches(subR, null)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * @see Resource#isClosedCollection(java.lang.String)
     */
    @Override
    public boolean isClosedCollection(String propURI) {
	return !TypeExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)
		&& super.isClosedCollection(propURI);
    }

    /**
     * @see Resource#isWellFormed()
     */
    @Override
    public boolean isWellFormed() {
	return true && hasProperty(PROP_INPUT_MODALITY);
    }

    /**
     * Prop restriction allowed.
     * 
     * @param prop
     *            the prop
     * @return true, if successful
     */
    private boolean propRestrictionAllowed(String prop) {
	for (int i = 0; i < restrictions.size(); i++) {
	    if (prop.equals(restrictions.get(i).getOnProperty())) {
		return false;
	    }
	}
	return true;
    }

    /**
     * @see Resource#setProperty(String, Object)
     */
    @Override
    public boolean setProperty(String propURI, Object value) {
	if (TypeExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)) {
	    if (value instanceof MergedRestriction) {
		return addRestriction((MergedRestriction) value);
	    } else if (value instanceof List) {
		List<?> property = (List) value;
		boolean retVal = false;
		for (Object current : property) {
		    if (current instanceof MergedRestriction) {
			retVal = addRestriction((MergedRestriction) current) || retVal;
		    }
		}
		return retVal;
	    }
	} else if (PROP_INPUT_MODALITY.equals(propURI)
		&& value instanceof Modality[]) {
	    return setSupportedInputModalities((Modality[]) value);
	}
	return false;
    }

    /**
     * Sets the supported input modalities.
     * 
     * @param modalities
     *            the new supported input modalities
     */
    public boolean setSupportedInputModalities(Modality[] modalities) {
	if (modalities != null && modalities.length > 0
		&& !props.containsKey(PROP_INPUT_MODALITY)) {
	    props.put(PROP_INPUT_MODALITY, Arrays.asList(modalities));
	    return true;
	}
	return false;
    }

    /**
     * @see #matches(Requirement)
     */
    public boolean matches(Matchable other) {
	return false;
    }

    /**
     * Only called if d is not of type {@link Requirement}. Therefore no match
     * is possible and <tt>false</tt> is returned always.
     * 
     * @param requirement
     *            the Requirement to be matched against
     * @return <tt>false</tt> as described above
     */
    public boolean matches(Requirement requirement) {
	return false;
    }

    /**
     * Switches over possible types of {@link Requirement}. Calls appropriate
     * methods for the different types.
     * 
     * @param request
     *            the Requirement to be matched
     * @return <tt>true</tt> if the Requirement matches, <tt>false</tt> if not
     */
    public boolean matches(Request request) {
	if (request instanceof UIRequest) {
	    return isMatchingUIRequest((UIRequest) request);
	} else {
	    return false;
	}
    }

    /**
     * Determines whether this profile matches the given {@link UIRequest}.
     * 
     * @param uiRequest
     *            the {@link UIRequest} to match.
     * @return true if the matching level is higher than failed
     */
    private boolean isMatchingUIRequest(UIRequest uiRequest) {
	return getMatchingDegree(uiRequest) > MATCH_LEVEL_FAILED;
    }
}