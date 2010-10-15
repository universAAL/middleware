/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.persona.middleware.input;

import org.persona.middleware.PResource;
import org.persona.middleware.dialog.Form;
import org.persona.middleware.dialog.SubdialogTrigger;
import org.persona.middleware.dialog.Submit;
import org.persona.ontology.Location;

/**
 * Instances of this class can be used to exchange info about user input.
 * 
 * @author mtazari
 * 
 */
public class InputEvent extends PResource {
	public static final String PERSONA_INPUT_NAMESPACE = 
		PERSONA_NAMESPACE_PREFIX + "Input.owl#";
	
	public static final String MY_URI = PERSONA_INPUT_NAMESPACE + "InputEvent";
	
	public static final String PERSONA_MAIN_MENU_REQUEST = PERSONA_INPUT_NAMESPACE + "mainMenuRequest";

	public static final String PROP_DIALOG_DATA = PERSONA_INPUT_NAMESPACE + "dialogData";
	public static final String PROP_DIALOG_ID = PERSONA_INPUT_NAMESPACE + "dialogID";
	public static final String PROP_INPUT_LOCATION = PERSONA_INPUT_NAMESPACE + "inputLocation";
	public static final String PROP_INPUT_SENTENCE = PERSONA_INPUT_NAMESPACE + "inputSentence";
	public static final String PROP_IS_SUBDIALOG_CALL = PERSONA_INPUT_NAMESPACE + "isSubdialogCall";
	public static final String PROP_SUBMISSION_ID = PERSONA_INPUT_NAMESPACE + "submissionID";
	
	/**
	 * This constructor is for the exclusive usage by deserializers.
	 */
	public InputEvent() {
		super();
	}
	
	public InputEvent(PResource user, Location inputLocation, String userRequest) {
		super();
		
		if (userRequest == null)
			throw new IllegalArgumentException("Invalid dialog data!");
			
		addType(MY_URI, true);
		props.put(PROP_PERSONA_INVOLVED_HUMAN_USER, user);
		if (inputLocation != null)
			props.put(PROP_INPUT_LOCATION, inputLocation);
		if (userRequest.equals(PERSONA_MAIN_MENU_REQUEST))
			props.put(PROP_DIALOG_ID, new PResource(userRequest));
		else
			props.put(PROP_INPUT_SENTENCE, userRequest);
	}
	
	public InputEvent(PResource user, Location inputLocation, Submit submit) {
		super();

		addType(MY_URI, true);
		props.put(PROP_PERSONA_INVOLVED_HUMAN_USER, user);
		props.put(PROP_DIALOG_ID, new PResource(submit.getDialogID()));
		props.put(PROP_SUBMISSION_ID, submit.getID());
		Form f = submit.getFormObject();
		props.put(PROP_DIALOG_DATA, f.getData());
		PResource parentDialog = f.getParentDialogResource();
		if (parentDialog != null)
			props.put(Form.PROP_PARENT_DIALOG_URI, parentDialog);
		if (inputLocation != null)
			props.put(PROP_INPUT_LOCATION, inputLocation);
		if (submit instanceof SubdialogTrigger)
			props.put(PROP_IS_SUBDIALOG_CALL, Boolean.TRUE);
	}
	
	public String getDialogID() {
		Object id = props.get(PROP_DIALOG_ID);
		return (id instanceof PResource)? id.toString() : null;
	}
	
	public Location getInputLocation() {
		Object loc = props.get(PROP_INPUT_LOCATION);
		return (loc instanceof Location)? (Location) loc : null;
	}
	
	public String getInputSentence() {
		Object s = props.get(PROP_INPUT_SENTENCE);
		return (s instanceof String)? (String) s : null;
	}
	
	public String getParentDialogURI() {
		Object o = props.get(Form.PROP_PARENT_DIALOG_URI);
		return (o instanceof PResource)? o.toString() : null;
	}
	
	public int getPropSerializationType(String propURI) {
		return (PROP_PERSONA_INVOLVED_HUMAN_USER.equals(propURI)
				||  PROP_INPUT_LOCATION.equals(propURI))?
				PROP_SERIALIZATION_REDUCED : PROP_SERIALIZATION_FULL;
	}
	
	public String getSubmissionID() {
		Object s = props.get(PROP_SUBMISSION_ID);
		return (s instanceof String)? (String) s : null;
	}
	
	public PResource getSubmittedData() {
		return (PResource) props.get(PROP_DIALOG_DATA);
	}
	
	public PResource getUser() {
		Object user = props.get(PROP_PERSONA_INVOLVED_HUMAN_USER);
		return (user instanceof PResource)? (PResource) user : null;
	}
	
	public Object getUserInput(String[] propPath) {
		if (propPath == null  ||  propPath.length == 0)
			return null;
		PResource pr = (PResource) props.get(PROP_DIALOG_DATA);
		Object o = pr.getProperty(propPath[0]);
		for (int i=1; o != null && i<propPath.length; i++) {
			if (!(o instanceof PResource))
				return null;
			pr = (PResource) o;
			o = pr.getProperty(propPath[i]);
		}
		
		return o;
	}
	
	public boolean hasDialogInput() {
		return props.containsKey(PROP_DIALOG_ID)
	    && props.containsKey(PROP_DIALOG_DATA);
	}
	
	public boolean isServiceSearch() {
		return !props.containsKey(PROP_DIALOG_ID)
		    && props.containsKey(PROP_INPUT_SENTENCE);
	}
	
	public boolean isSubdialogCall() {
		return Boolean.TRUE.equals(props.get(PROP_IS_SUBDIALOG_CALL));
	}
	
	public boolean isSubdialogSubmission() {
		return props.containsKey(Form.PROP_PARENT_DIALOG_URI);
	}

	public boolean isWellFormed() {
		return hasDialogInput()  ||  isServiceSearch();
	}
}
