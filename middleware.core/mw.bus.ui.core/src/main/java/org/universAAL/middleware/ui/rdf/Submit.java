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
package org.universAAL.middleware.ui.rdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a button in the form that finishes the dialog represented by that
 * form. Each instance of Submit must be associated with a unique ID, called the
 * submission ID in order to keep it decidable how the dialog was finished (i.e.
 * by pressing which button). Critical submits may be associated with a
 * confirmation message to be shown to users in order to make sure that the
 * button was not pressed by mistake. Two types of confirmation messages are
 * supported, either OK/Cancel or Yes/No; the type specifies which buttons
 * should be added by a UI handler to the confirmation dialog.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @navassoc - "mandatoryInput" * Input
 */
public class Submit extends FormControl {
    public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE + "Submit";

    /**
     * Critical submits may be associated with a confirmation message to be
     * shown to users in order to make sure that the button was not pressed by
     * mistake. Two types of confirmation messages are supported, either
     * OK/Cancel or Yes/No; the type specifies which buttons should be added by
     * a UI handler to the confirmation dialog.
     */
    public static final int CONFIRMATION_TYPE_OK_CANCEL = 0;

    /**
     * Critical submits may be associated with a confirmation message to be
     * shown to users in order to make sure that the button was not pressed by
     * mistake. Two types of confirmation messages are supported, either
     * OK/Cancel or Yes/No; the type specifies which buttons should be added by
     * a UI handler to the confirmation dialog.
     */
    public static final int CONFIRMATION_TYPE_YES_NO = 1;

    /**
     * Critical submits may be associated with a confirmation message to be
     * shown to users in order to make sure that the button was not pressed by
     * mistake. Two types of confirmation messages are supported, either
     * OK/Cancel or Yes/No; the type specifies which buttons should be added by
     * a UI handler to the confirmation dialog.
     */
    public static final String PROP_CONFIRMATION_MESSAGE = uAAL_VOCABULARY_NAMESPACE
	    + "confirmationMessage";

    /**
     * Critical submits may be associated with a confirmation message to be
     * shown to users in order to make sure that the button was not pressed by
     * mistake. Two types of confirmation messages are supported, either
     * OK/Cancel or Yes/No; the type specifies which buttons should be added by
     * a UI handler to the confirmation dialog.
     */
    public static final String PROP_CONFIRMATION_TYPE = uAAL_VOCABULARY_NAMESPACE
	    + "confirmationType";

    /**
     * For maintaining a list of mandatory input controls associated with each
     * submit control that have to be filled by the user before submitting a
     * form via that submit control.
     */
    public static final String PROP_MANDATORY_INPUT = uAAL_VOCABULARY_NAMESPACE
	    + "mandatoryInput";

    /**
     * The unique ID that helps to identify with pressing which button a dialog
     * was finished.
     */
    public static final String PROP_SUBMISSION_ID = uAAL_VOCABULARY_NAMESPACE
	    + "submissionID";

    protected List l = null;

    /**
     * For exclusive use by de-serializers.
     */
    public Submit() {
	super();
	l = new ArrayList();
	props.put(PROP_MANDATORY_INPUT, l);
    }

    /**
     * For exclusive use by applications.
     * 
     * @param parent
     *            The mandatory parent group as the direct container of this
     *            input field. See {@link FormControl#PROP_PARENT_CONTROL}.
     * @param label
     *            The optional {@link Label} to be associated with this input
     *            field. See {@link FormControl#PROP_CONTROL_LABEL}.
     * @param id
     *            The mandatory submission ID. See {@link #PROP_SUBMISSION_ID}.
     */
    public Submit(Group parent, Label label, String id) {
	super(MY_URI, parent, label, null, null, null);
	props.put(PROP_SUBMISSION_ID, id);
	l = new ArrayList();
	props.put(PROP_MANDATORY_INPUT, l);
    }

    protected Submit(String typeURI, Group parent, Label label, String id) {
	super(typeURI, parent, label, null, null, null);
	props.put(PROP_SUBMISSION_ID, id);
	l = new ArrayList();
	props.put(PROP_MANDATORY_INPUT, l);
    }

    /**
     * For use by applications.
     * 
     * @see #PROP_MANDATORY_INPUT
     */
    public void addMandatoryInput(Input in) {
	if (in != null) {
	    l.add(in);
	    in.setMandatory();
	}
    }

    /**
     * @see #PROP_CONFIRMATION_MESSAGE
     */
    public String getConfirmationMessage() {
	return (String) props.get(PROP_CONFIRMATION_MESSAGE);
    }

    /**
     * @see #PROP_CONFIRMATION_TYPE
     */
    public int getConfirmationType() {
	Object o = props.get(PROP_CONFIRMATION_TYPE);
	return (o instanceof Integer) ? ((Integer) o).intValue() : -1;
    }

    /**
     * For use by UI handlers.
     * 
     * @return The ID of the dialog being finished by pressing this button.
     */
    public String getDialogID() {
	Form f = getFormObject();
	if (f == null)
	    return null;

	Group stdButtons = f.getStandardButtons();
	if (stdButtons != null) {
	    Group parent = getParentGroup();
	    while (parent != null && parent != stdButtons)
		parent = parent.getParentGroup();
	    if (parent == stdButtons)
		return f.getStandardButtonsDialogID();
	}

	return f.getDialogID();
    }

    /**
     * @see #PROP_SUBMISSION_ID
     */
    public String getID() {
	return (String) props.get(PROP_SUBMISSION_ID);
    }

    /**
     * The length of a button is the length of its label. Returns -1, if no
     * label text is set.
     */
    public int getMaxLength() {
	Label l = getLabel();
	return (l == null) ? -1 : l.getMaxLength();
    }

    /**
     * @see #PROP_MANDATORY_INPUT
     */
    public Input[] getMandatoryInputControls() {
	if (l == null || l.isEmpty())
	    return new Input[0];

	return (Input[]) l.toArray(new Input[l.size()]);
    }

    /**
     * UI handlers must call this method as soon as the user decides to submit
     * the form using this submit in oder to make sure if all necessary input
     * has been collected.
     * 
     * @return The input control that is missing user input or null if the form
     *         is ready to be submitted.
     */
    public Input getMissingInputControl() {
	if (l == null)
	    return null;

	for (Iterator i = l.iterator(); i.hasNext();) {
	    Input in = (Input) i.next();
	    if (!in.checkSubmission())
		return in;
	}
	return null;
    }

    /**
     * Supports UI handlers that process a form by breaking it into several
     * "subdialog"s, one for each alternative submission, by returning all of
     * the UI controls that are somehow related to this submit. An UI control is
     * relevant if it is either a mandatory input for this submit or it has the
     * same parent group as a mandatory input. For answering the demanded array,
     * it first finds the least common parent group among the mandatory inputs
     * and then the whole subtree of that group is traversed based on a
     * depth-first search. The elements of the returned array will be instances
     * of {@link Input}, {@link Output}, {@link Repeat} or
     * {@link SubdialogTrigger}.
     */
    public FormControl[] getRelatedControls() {
	if (l == null || l.isEmpty())
	    return new FormControl[0];

	// find the least common parent
	Group[] superGroups = ((Input) l.get(0)).getSuperGroups();
	int lcpIndex = superGroups.length - 1;
	for (int i = 1; i < l.size(); i++) {
	    FormControl fc = (FormControl) l.get(i);
	    while (fc != null) {
		fc = fc.getParentGroup();
		for (int j = 0; j < lcpIndex; j++)
		    if (fc == superGroups[j]) {
			lcpIndex = j;
			break;
		    }
	    }
	    if (lcpIndex == 0)
		break;
	}

	return superGroups[lcpIndex].getSubtree(this);
    }

    /**
     * Checks if the given input control belongs to the list of mandatory inputs
     * of this submit.
     */
    public boolean hasMandatoryInput(Input fc) {
	return l != null && l.contains(fc);
    }

    /**
     * @see #CONFIRMATION_TYPE_OK_CANCEL
     * @see #PROP_CONFIRMATION_MESSAGE
     * @see #PROP_CONFIRMATION_TYPE
     */
    public void setConfirmationOkCancel(String msg) {
	if (msg != null) {
	    props.put(PROP_CONFIRMATION_MESSAGE, msg);
	    props.put(PROP_CONFIRMATION_TYPE, new Integer(
		    CONFIRMATION_TYPE_OK_CANCEL));
	}
    }

    /**
     * @see #CONFIRMATION_TYPE_YES_NO
     * @see #PROP_CONFIRMATION_MESSAGE
     * @see #PROP_CONFIRMATION_TYPE
     */
    public void setConfirmationYesNo(String msg) {
	if (msg != null) {
	    props.put(PROP_CONFIRMATION_MESSAGE, msg);
	    props.put(PROP_CONFIRMATION_TYPE, new Integer(
		    CONFIRMATION_TYPE_YES_NO));
	}
    }

    /**
     * For use by de-serializers.
     */
    public boolean setProperty(String propURI, Object value) {
	if (PROP_MANDATORY_INPUT.equals(propURI)) {
	    if (l.isEmpty()) {
		if (value instanceof List) {
		    boolean retVal = true;
		    for (Iterator i = ((List) value).iterator(); i.hasNext();) {
			value = i.next();
			if (!(value instanceof Input)) {
			    l.clear();
			    return false;
			} else
			    retVal = retVal && l.add(value);
		    }
		    return retVal;
		} else if (value instanceof Input)
		    return l.add(value);
		else
		    return false;
	    }
	} else if (PROP_SUBMISSION_ID.equals(propURI)) {
	    if (value instanceof String && !"".equals(value)) {
		props.put(propURI, value);
		return true;
	    }
	} else if (PROP_CONFIRMATION_MESSAGE.equals(propURI)) {
	    if (value instanceof String && !"".equals(value)) {
		props.put(propURI, value);
		return true;
	    }
	} else if (PROP_CONFIRMATION_TYPE.equals(propURI)) {
	    if (value instanceof Integer) {
		props.put(propURI, value);
		return true;
	    }
	} else
	    return super.setProperty(propURI, value);
	return false;
    }
}
