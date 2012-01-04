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

import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;

/**
 * The default {@link Input} control.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class InputField extends Input {
	public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE
			+ "InputField";

	/**
	 * To be set by applications if the field value, e.g. a password, should be
	 * kept secret during the presentation of the form.
	 */
	public static final String PROP_IS_SECRET = Form.uAAL_DIALOG_NAMESPACE
			+ "isSecret";

	/**
	 * To be set by applications (recommended) to declare a general restriction
	 * on the number of characters in the string representation of any
	 * acceptable value for the corresponding input field.
	 */
	public static final String PROP_MAX_LENGTH = Form.uAAL_DIALOG_NAMESPACE
			+ "maxLength";

	/**
	 * For exclusive use by de-serializers.
	 */
	public InputField() {
		super();
	}

	/**
	 * Constructs a new input field.
	 * 
	 * @param parent
	 *            The mandatory parent group as the direct container of this
	 *            input field. See {@link FormControl#PROP_PARENT_CONTROL}.
	 * @param label
	 *            The optional {@link Label} to be associated with this input
	 *            field. See {@link FormControl#PROP_CONTROL_LABEL}.
	 * @param ref
	 *            See {@link FormControl#PROP_REFERENCED_PPATH}; mandatory.
	 * @param valueRestriction
	 *            See {@link Input#PROP_VALUE_RESTRICTION}; optional.
	 * @param initialValue
	 *            The optional initial value to be stored in form data under the
	 *            path given for the above <code>ref</code> parameter.
	 */
	public InputField(Group parent, Label label, PropertyPath ref,
		MergedRestriction valueRestriction, Object initialValue) {
		super(MY_URI, parent, label, ref, valueRestriction, initialValue);
	}

	/**
	 * Overrides the default implementation in
	 * {@link FormControl#getMaxLength()} in order to consider a possibly
	 * available value for {@link #PROP_MAX_LENGTH} at first.
	 */
	public int getMaxLength() {
		Object o = props.get(PROP_MAX_LENGTH);
		if (o instanceof Integer)
			return ((Integer) o).intValue();

		return super.getMaxLength();
	}

	/**
	 * @see #PROP_IS_SECRET
	 */
	public boolean isSecret() {
		return Boolean.TRUE.equals(props.get(PROP_IS_SECRET));
	}

	/**
	 * @see #PROP_MAX_LENGTH
	 */
	public void setMaxLength(int maxLength) {
		props.put(PROP_MAX_LENGTH, new Integer(maxLength));
	}

	/**
	 * @see Input#setProperty(String, Object)
	 */
	public void setProperty(String propURI, Object value) {
		if (PROP_IS_SECRET.equals(propURI)) {
			if (Boolean.TRUE.equals(value))
				props.put(propURI, value);
		} else if (PROP_MAX_LENGTH.equals(propURI)) {
			if (value instanceof Integer)
				props.put(propURI, value);
		} else
			super.setProperty(propURI, value);
	}

	/**
	 * @see #PROP_IS_SECRET
	 */
	public void setSecret() {
		props.put(PROP_IS_SECRET, Boolean.TRUE);
	}
}
