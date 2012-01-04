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

import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.rdf.SubdialogTrigger;
import org.universAAL.middleware.ui.rdf.Submit;

/**
 * Instances of this class can be used to exchange info about user input.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class UIResponse extends Resource {

    public static final String MY_URI = UIRequest.uAAL_UI_NAMESPACE + "UIResponse";

    public static final String PROP_DIALOG_DATA = UIRequest.uAAL_UI_NAMESPACE
	    + "dialogData";
    public static final String PROP_DIALOG_ID = UIRequest.uAAL_UI_NAMESPACE
	    + "dialogID";
    public static final String PROP_SUBMISSION_LOCATION = UIRequest.uAAL_UI_NAMESPACE
	    + "submissionLocation";
    public static final String PROP_IS_SUBDIALOG_CALL = UIRequest.uAAL_UI_NAMESPACE
	    + "isSubdialogCall";
    public static final String PROP_SUBMISSION_ID = UIRequest.uAAL_UI_NAMESPACE
	    + "submissionID";

    /**
     * This constructor is for the exclusive usage by deserializers.
     */
    public UIResponse() {
	super();
    }

    /**
     * This constructor is used in the context of a running dialog.
     * 
     * @param user
     *            reference to the user
     * @param inputLocation
     *            location of the user
     * @param submit
     *            instance of a submit button that has finished the dialog
     */
    public UIResponse(Resource user, AbsLocation inputLocation, Submit submit) {
	super();

	addType(MY_URI, true);
	props.put(PROP_uAAL_INVOLVED_HUMAN_USER, user);
	props.put(PROP_DIALOG_ID, new Resource(submit.getDialogID()));
	props.put(PROP_SUBMISSION_ID, submit.getID());
	Form f = submit.getFormObject();
	props.put(PROP_DIALOG_DATA, f.getData());
	Resource parentDialog = f.getParentDialogResource();
	if (parentDialog != null)
	    props.put(Form.PROP_PARENT_DIALOG_URI, parentDialog);
	if (inputLocation != null)
	    props.put(PROP_SUBMISSION_LOCATION, inputLocation);
	if (submit instanceof SubdialogTrigger)
	    props.put(PROP_IS_SUBDIALOG_CALL, Boolean.TRUE);
    }

    /**
     * 
     * @return ID of the dialog in which this input has been provided
     */
    public String getDialogID() {
	Object id = props.get(PROP_DIALOG_ID);
	return (id instanceof Resource) ? id.toString() : null;
    }

    public AbsLocation getSubmissionLocation() {
	Object loc = props.get(PROP_SUBMISSION_LOCATION);
	return (loc instanceof AbsLocation) ? (AbsLocation) loc : null;
    }

    /**
     * 
     * @return the ID of the parent dialog in case this event is about dialog
     *         being finished (which can be checked by calling
     *         isSubdialogSubmission() method)
     */
    public String getParentDialogURI() {
	Object o = props.get(Form.PROP_PARENT_DIALOG_URI);
	return (o instanceof Resource) ? o.toString() : null;
    }

    public int getPropSerializationType(String propURI) {
	return (PROP_uAAL_INVOLVED_HUMAN_USER.equals(propURI) || PROP_SUBMISSION_LOCATION
		.equals(propURI)) ? PROP_SERIALIZATION_REDUCED
		: PROP_SERIALIZATION_FULL;
    }

    /**
     * 
     * @return ID of the submit button selected by the user when finishing the
     *         dialog
     */
    public String getSubmissionID() {
	Object s = props.get(PROP_SUBMISSION_ID);
	return (s instanceof String) ? (String) s : null;
    }

    /**
     * 
     * @return root of the tree with form data submitted
     */
    public Resource getSubmittedData() {
	return (Resource) props.get(PROP_DIALOG_DATA);
    }

    public Resource getUser() {
	Object user = props.get(PROP_uAAL_INVOLVED_HUMAN_USER);
	return (user instanceof Resource) ? (Resource) user : null;
    }

    /**
     * 
     * @param propPath
     *            array of property URIs, path of a certain expected user input
     * @return input from the tree with the form data
     */
    public Object getUserInput(String[] propPath) {
	if (propPath == null || propPath.length == 0)
	    return null;
	Resource pr = (Resource) props.get(PROP_DIALOG_DATA);
	if (pr == null)
	    return null;
	Object o = pr.getProperty(propPath[0]);
	for (int i = 1; o != null && i < propPath.length; i++) {
	    if (!(o instanceof Resource))
		return null;
	    pr = (Resource) o;
	    o = pr.getProperty(propPath[i]);
	}

	return o;
    }

    public boolean isSubdialogCall() {
	return Boolean.TRUE.equals(props.get(PROP_IS_SUBDIALOG_CALL));
    }

    public boolean isSubdialogSubmission() {
	return props.containsKey(Form.PROP_PARENT_DIALOG_URI);
    }
}
