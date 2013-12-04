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
import java.util.List;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.model.matchable.UtilityAdvertisement;
import org.universAAL.middleware.owl.Enumeration;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.ui.owl.Modality;

/**
 * A profile of the {@link UIHandler} that describes its capabilites so that
 * they can be matched with {@link UIRequest} ( more specifically User
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
//    public static final String PROP_INPUT_MODALITY = UIRequest.uAAL_UI_NAMESPACE
//	    + "inputModality";

    // individual weights of matching criteria
    public static final int MATCH_ADDRESED_USER = 0x80; // 128
    public static final int MATCH_MAIN_MODALITY = 0x40; // 64
    public static final int MATCH_ALT_MODALITY = 0x20;// 32
    public static final int MATCH_USER_LOCATION = 0x10;// 16
    public static final int MATCH_USER_IMPAIRMENTS = 0x08;// 8
    public static final int MATCH_DIALOG_PRIVACY = 0x04;// 4
    public static final int MATCH_DIALOG_LANGUAGE = 0x02;// 2
    public static final int MATCH_DIALOG_FORM = 0x01;// 1
    public static final int MATCH_LEVEL_FAILED = 0x00;// 0

    private List<MergedRestriction> restrictions;

    /**
     * Instantiates a new {@link UIHandlerProfile}.
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
		|| UIRequest.PROP_PRESENTATION_MODALITY.equals(prop)
		|| UIRequest.PROP_PRESENTATION_LOCATION.equals(prop)
		|| UIRequest.PROP_HAS_ACCESS_IMPAIRMENT.equals(prop)
		|| UIRequest.PROP_DIALOG_PRIVACY_LEVEL.equals(prop)
		|| UIRequest.PROP_DIALOG_LANGUAGE.equals(prop)
		|| UIRequest.PROP_DIALOG_FORM.equals(prop)
		|| UIRequest.PROP_DIALOG_PRIORITY.equals(prop)
		|| UIRequest.PROP_HAS_PREFERENCE.equals(prop)) {
	    if (propRestrictionAllowed(prop)) {
		restrictions.add(mergedRestriction);
		return true;
	    }
	}
	return false;
    }

    /**
     * Determines whether the given {@link UIRequest} matches this profile and
     * returns the weighted matching degree. In {@link UIStrategy} (from there
     * this method is called when selecting the best {@link UIHandler} for
     * certain {@link UIRequest}) additional weight is added on this one that
     * reflects the lastly used {@link UIHandler}.
     * 
     * @param uiRequest
     *            the {@link UIRequest}
     * @return a value indicating to which degree the {@link UIRequest} matches
     *         the {@link UIHandlerProfile}
     */
    public int getMatchingDegree(UIRequest uiRequest) {
	if (uiRequest == null) {
	    return MATCH_LEVEL_FAILED;
	}
	int result = MATCH_LEVEL_FAILED;
	for (MergedRestriction r : restrictions) {
	    if (r.hasMember(uiRequest, null)) {
		// r Restriction matches all the criterion of UIRequest
		if (r.getOnProperty().equals(UIRequest.PROP_ADDRESSED_USER)
			&& (uiRequest
				.getProperty(UIRequest.PROP_ADDRESSED_USER) != null)) {
		    /*
		     * one of the restrictions is defined to restrict for the
		     * addressed user, the values have already been checked by
		     * the previous if. UIRequest.PROP_ADDRESSED_USER cannot be
		     * null since it is obligatory in UIRequest but still
		     */

		    result += MATCH_ADDRESED_USER;
		    System.out
			    .println("################################### addressed user match, result="
				    + result);
		}
		if (r.getOnProperty().equals(
			UIRequest.PROP_PRESENTATION_MODALITY)
			&& (uiRequest
				.getProperty(UIRequest.PROP_PRESENTATION_MODALITY) != null)) {
		    result += MATCH_MAIN_MODALITY;
		    System.out
			    .println("################################### modality match, result="
				    + result);
		}
		// ALT MODALITY is checked later.
		if (r.getOnProperty().equals(
			UIRequest.PROP_PRESENTATION_LOCATION)
			&& (uiRequest
				.getProperty(UIRequest.PROP_PRESENTATION_LOCATION) != null)) {
		    result += MATCH_USER_LOCATION;

		    System.out
			    .println("################################### location match, result="
				    + result);
		}
		if (r.getOnProperty().equals(
			UIRequest.PROP_HAS_ACCESS_IMPAIRMENT)
			&& (uiRequest
				.getProperty(UIRequest.PROP_HAS_ACCESS_IMPAIRMENT) != null)) {
		    result += MATCH_USER_IMPAIRMENTS;
		    System.out
			    .println("################################### impairment match, result="
				    + result);
		}
		if (r.getOnProperty().equals(
			UIRequest.PROP_DIALOG_PRIVACY_LEVEL)
			&& (uiRequest
				.getProperty(UIRequest.PROP_DIALOG_PRIVACY_LEVEL) != null)) {
		    result += MATCH_DIALOG_PRIVACY;
		    System.out
			    .println("################################### dialog privacy match, result="
				    + result);
		}
		if (r.getOnProperty().equals(UIRequest.PROP_DIALOG_LANGUAGE)
			&& (uiRequest
				.getProperty(UIRequest.PROP_DIALOG_LANGUAGE) != null)) {

		    result += MATCH_DIALOG_LANGUAGE;
		    System.out
			    .println("################################### dialog lang match, result="
				    + result);
		}
		if (r.getOnProperty().equals(UIRequest.PROP_DIALOG_FORM)
			&& (uiRequest.getProperty(UIRequest.PROP_DIALOG_FORM) != null)) {

		    result += MATCH_DIALOG_FORM;
		    System.out
			    .println("################################### dialog form match, result="
				    + result);
		}
	    } else {
		if (UIRequest.PROP_PRESENTATION_MODALITY.equals(r
			.getOnProperty())
			&& r.copyOnNewProperty(
				UIRequest.PROP_PRESENTATION_MODALITY_ALT)
				.hasMember(uiRequest, null))
		/*
		 * if the restriction r is about the modality property of
		 * UIReqest, then rename the restriction to alt-modality and see
		 * if it matches. if it matches then, this handler is matched as
		 * alternative modality.
		 */
		{
		    result += MATCH_ALT_MODALITY;
		    System.out
			    .println("################################### alt modality match, result="
				    + result);
		}
	    }
	}
	return result;
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
     * @see
     * org.universAAL.middleware.rdf.Resource#isClosedCollection(java.lang.String
     * )
     */
    @Override
    public boolean isClosedCollection(String propURI) {
	return !TypeExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)
		&& super.isClosedCollection(propURI);
    }

    /**
     * Prop restriction allowed.
     * 
     * @param prop
     *            the property
     * @return true, if successful; false if not
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
     * @see org.universAAL.middleware.rdf.Resource#setProperty(java.lang.String,
     * java.lang.Object)
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
			retVal = addRestriction((MergedRestriction) current)
				|| retVal;
		    }
		}
		return retVal;
	    }
	} 
//	else if (PROP_INPUT_MODALITY.equals(propURI)
//		&& value instanceof List) {
//	    props.put(PROP_INPUT_MODALITY, value);
//	    return true;
//	}
	return false;
    }

    /**
     * @see Matchable#matches(Matchable)
     */
    public boolean matches(Matchable subset) {
	if (subset instanceof UIRequest) {
	    return getMatchingDegree((UIRequest) subset) > MATCH_LEVEL_FAILED;
	} else if (subset instanceof UIHandlerProfile) {
	    UIHandlerProfile uiHandlerProfile = (UIHandlerProfile) subset;

	    for (MergedRestriction r : restrictions) {
		MergedRestriction subR = uiHandlerProfile.getRestriction(r
			.getOnProperty());
		if (subR == null || !r.matches(subR, null)) {
		    return false;
		}
	    }
	    return true;
	}
	return false;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#isWellFormed()
     */
    public boolean isWellFormed() {
	return true ;
//		&& hasProperty(PROP_INPUT_MODALITY);
    }

    /**
         * Sets the supported input modalities.
         * 
         * @param modalities
         *            the new supported input modalities
         */
        public boolean setSupportedInputModalities(Modality[] modalities) {
    //	if (modalities != null && modalities.length > 0
    //		&& !props.containsKey(PROP_INPUT_MODALITY)) {
    //	    props.put(PROP_INPUT_MODALITY, Arrays.asList(modalities));
    //	    return true;
    //	}
    //	return false;
	//	new AllValuesFromRestriction(UIRequest.PROP_HAS_ACCESS_IMPAIRMENT, 
	//			new Enumeration(new AccessImpairment[] {
	//					new HearingImpairment(LevelRating.low),
	//					new HearingImpairment(LevelRating.middle),
	//					new HearingImpairment(LevelRating.high),
	//					new HearingImpairment(LevelRating.full),
	//					new SightImpairment(LevelRating.low),
	//					new PhysicalImpairment(LevelRating.low)})));

            if (propRestrictionAllowed(UIRequest.PROP_PRESENTATION_MODALITY)) {
        	MergedRestriction mr = MergedRestriction.getAllValuesRestriction(
        		UIRequest.PROP_PRESENTATION_MODALITY, new Enumeration(
        			modalities));
        	return addRestriction(mr);
            }
            return false;
        }

    /**
     * Gets the supported input modalities.
     * 
     * @return the supported input modalities
     */
    public Modality[] getSupportedInputModalities() {
//	List l = (List) props.get(PROP_INPUT_MODALITY);
//	return l == null ? null : (Modality[]) l
//		.toArray(new Modality[l.size()]);
	for (Object r : restrictions) {
	    if (r instanceof MergedRestriction
		&& ((MergedRestriction)r).getOnProperty().equals(UIRequest.PROP_PRESENTATION_MODALITY)){
		return (Modality[]) ((MergedRestriction)r).getEnumeratedValues();
	    }
	}
	return new Modality[]{};
    }

    //    private boolean isRestrictionOnModality(UIRequest uiRequest,
    //	    MergedRestriction r) {
    //	return UIRequest.PROP_PRESENTATION_MODALITY.equals(r.getOnProperty())
    //		&& r.copyOnNewProperty(UIRequest.PROP_PRESENTATION_MODALITY_ALT)
    //			.hasMember(uiRequest, null);
    //    }
    
//        /**
//         * Gets the number of supported input modalities.
//         * 
//         * @return the number of supported input modalities
//         */
//        public int getNumberOfSupportedInputModalities() {
//    	List l = (List) props.get(PROP_INPUT_MODALITY);
//    	return l == null ? 0 : l.size();
//        }
}
