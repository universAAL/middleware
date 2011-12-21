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
package org.universAAL.middleware.io.rdf;

/**
 * A subclass of {@link Submit} that does not finish the current dialog but
 * starts a subdialog, normally used for viewing or editing complex data that
 * was represented in the main dialog in a summarized way or in way not suitable
 * for editing.
 * 
 * @author mtazari
 */
public class SubdialogTrigger extends Submit {
	public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE
			+ "SubdialogTrigger";

	/**
	 * If a SubdialogTrigger is used in a column of a {@link Repeat} control,
	 * each occurrence of it in each row of the table represented by the
	 * {@link Repeat} control must have another
	 * {@link Submit#PROP_SUBMISSION_ID} in order to be able to distinguish
	 * between them. In such cases, applications must set the submission ID
	 * equal to {@link #VAR_REPEATABLE_ID} and specify a prefix as a string to
	 * be stored under this property. Consequently, a repeatable
	 * SubdialogTrigger that has {@link #VAR_REPEATABLE_ID} as value associated
	 * with {@link Submit#PROP_SUBMISSION_ID} will answer to the call of
	 * {@link #getID()} with a string constructed by appending the current
	 * selection index obtained from the nearest ancestor {@link Repeat} to the
	 * prefix stored here. The prefix is needed in order to be able to
	 * distinguish between different instances of SubdialogTrigger that appear
	 * in different {@link Repeat} controls or in different columns of the same
	 * {@link Repeat} control.
	 */
	public static final String PROP_REPEATABLE_ID_PREFIX = Form.uAAL_DIALOG_NAMESPACE
			+ "selectionXIDPrefix";

	/**
	 * @see #PROP_REPEATABLE_ID_PREFIX
	 */
	public static final String VAR_REPEATABLE_ID = Form.uAAL_DIALOG_NAMESPACE
			+ "repeatableSubmissionID";

	static {
		addResourceClass(MY_URI, SubdialogTrigger.class);
	}

	/**
	 * For exclusive use by de-serializers.
	 */
	public SubdialogTrigger() {
		super();
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
	 *            The mandatory submission ID. See
	 *            {@link Submit#PROP_SUBMISSION_ID}.
	 */
	public SubdialogTrigger(Group parent, Label label, String id) {
		super(MY_URI, parent, label, id);
	}

	/**
	 * Overrides {@link Submit#getID()}.
	 * 
	 * @see #PROP_REPEATABLE_ID_PREFIX
	 */
	public String getID() {
		if (needsSelection()) {
			Repeat r = getAncestorRepeat();
			if (r != null)
				return getRepeatableIDPrefix()
						+ Integer.toString(r.getSelectionIndex());
		}
		return super.getID();
	}

	/**
	 * @see #PROP_REPEATABLE_ID_PREFIX
	 */
	public String getRepeatableIDPrefix() {
		Object o = props.get(PROP_REPEATABLE_ID_PREFIX);
		;
		return (o instanceof String) ? (String) o : "";
	}

	/**
	 * Checks if the submission ID of this SubdialogTrigger equals to
	 * {@link #VAR_REPEATABLE_ID}, which means that there must be an ancestor
	 * {@link Repeat} control with a valid selection index in order to be able
	 * to construct the submission ID.
	 * 
	 * @see #PROP_REPEATABLE_ID_PREFIX
	 */
	public boolean needsSelection() {
		return VAR_REPEATABLE_ID.equals(props.get(PROP_SUBMISSION_ID));
	}

	/**
	 * For exclusive use by de-serializers.
	 */
	public void setProperty(String propURI, Object value) {
		if (PROP_REPEATABLE_ID_PREFIX.equals(propURI))
			if (value instanceof String
					&& !props.containsKey(PROP_REPEATABLE_ID_PREFIX))
				props.put(propURI, value);
			else
				return;
		else
			super.setProperty(propURI, value);
	}

	/**
	 * @see #PROP_REPEATABLE_ID_PREFIX
	 */
	public void setRepeatableIDPrefix(String prefix) {
		if (prefix != null && !props.containsKey(PROP_REPEATABLE_ID_PREFIX))
			props.put(PROP_REPEATABLE_ID_PREFIX, prefix);
	}
}
