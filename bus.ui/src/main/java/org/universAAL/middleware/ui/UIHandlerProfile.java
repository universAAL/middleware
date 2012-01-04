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
package org.universAAL.middleware.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.universAAL.middleware.owl.ClassExpression;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.owl.Modality;

/**
 * @author mtazari
 * @author Carsten Stockloew
 */
public class UIHandlerProfile extends Resource {
    public static final String MY_URI = UIRequest.uAAL_UI_NAMESPACE
	    + "UIHandlerProfile";
    public static final String PROP_INPUT_MODALITY = UIRequest.uAAL_UI_NAMESPACE
	    + "inputModality";

    public static final int MATCH_LEVEL_FAILED = 0;
    public static final int MATCH_LEVEL_ALT = 1;
    public static final int MATCH_LEVEL_SUCCESS = 2;

    private List restrictions;

    public UIHandlerProfile() {
	super();
	addType(MY_URI, true);
	restrictions = new ArrayList(12);
	props.put(ClassExpression.PROP_RDFS_SUB_CLASS_OF, restrictions);
    }

    public void addRestriction(MergedRestriction r) {
	if (r == null)
	    return;

	String prop = r.getOnProperty();
	if (UIRequest.PROP_HAS_ACCESS_IMPAIRMENT.equals(prop)
		|| UIRequest.PROP_DIALOG_LANGUAGE.equals(prop)
		|| UIRequest.PROP_PRESENTATION_MODALITY.equals(prop)
		|| UIRequest.PROP_PRESENTATION_LOCATION.equals(prop)
		|| UIRequest.PROP_DIALOG_PRIVACY_LEVEL.equals(prop)
		|| UIRequest.PROP_SCREEN_RESOLUTION_MAX_X.equals(prop)
		|| UIRequest.PROP_SCREEN_RESOLUTION_MAX_Y.equals(prop)
		|| UIRequest.PROP_SCREEN_RESOLUTION_MIN_X.equals(prop)
		|| UIRequest.PROP_SCREEN_RESOLUTION_MIN_Y.equals(prop)
		|| UIRequest.PROP_VOICE_GENDER.equals(prop)
		|| UIRequest.PROP_VOICE_LEVEL.equals(prop))
	    if (propRestrictionAllowed(prop))
		restrictions.add(r);
    }

    public int getNumberOfSupportedInputModalities() {
	List l = (List) props.get(PROP_INPUT_MODALITY);
	return (l == null) ? 0 : l.size();
    }

    private MergedRestriction getRestriction(String onProp) {
	for (int i = 0; i < restrictions.size(); i++) {
	    MergedRestriction r = (MergedRestriction) restrictions.get(i);
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

    public int matches(UIRequest oe) {
	if (oe == null)
	    return MATCH_LEVEL_FAILED;

	int result = MATCH_LEVEL_SUCCESS;
	for (int i = 0; i < restrictions.size(); i++) {
	    MergedRestriction r = (MergedRestriction) restrictions.get(i);
	    if (!r.hasMember(oe, null))
		if (UIRequest.PROP_PRESENTATION_MODALITY.equals(r.getOnProperty())
			&& r.copyOnNewProperty(
				UIRequest.PROP_PRESENTATION_MODALITY_ALT)
				.hasMember(oe, null)) {
		    result = MATCH_LEVEL_ALT;
		    continue;
		} else
		    return MATCH_LEVEL_FAILED;
	}

	return result;
    }

    public boolean matches(UIHandlerProfile subtype) {
	if (subtype == null)
	    return false;

	for (int i = 0; i < restrictions.size(); i++) {
	    MergedRestriction r = (MergedRestriction) restrictions.get(i), subR = subtype
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
		    .equals(((MergedRestriction) restrictions.get(i)).getOnProperty()))
		return false;
	return true;
    }

    public void setProperty(String propURI, Object o) {
	if (ClassExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)) {
	    if (o instanceof MergedRestriction)
		addRestriction((MergedRestriction) o);
	    else if (o instanceof List)
		for (int i = 0; i < ((List) o).size(); i++)
		    if (((List) o).get(i) instanceof MergedRestriction)
			addRestriction((MergedRestriction) ((List) o).get(i));
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
