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
package org.persona.middleware.dialog;

import java.util.Iterator;
import java.util.List;

import org.persona.middleware.service.PropertyPath;
import org.persona.ontology.expr.Restriction;

/**
 * The abstract class for all types of form controls that serve as a placeholder for possible
 * user input.
 * 
 * @author mtazari
 */
public abstract class Input extends FormControl {
	public static final String MY_URI = Form.PERSONA_DIALOG_NAMESPACE + "Input";

	/**
	 * A mandatory (in the sense of "best practice") property for defining a message to be
	 * communicated with human users if the provided input by them is erroneous.
	 */
	public static final String PROP_INPUT_ALERT = Form.PERSONA_DIALOG_NAMESPACE + "inputAlert";

	/**
	 * A property that is set automatically by the dialog package as soon as an input control
	 * is added to a {@link Submit} control as mandatory input. By default, input controls
	 * are optional.
	 */
	public static final String PROP_IS_MANDATORY = Form.PERSONA_DIALOG_NAMESPACE + "isMandatory";
	
	protected Input() {
		super();
	}
	
	protected Input(String typeURI, Group parent, Label label,
			PropertyPath ref, Restriction valueRestriction, Object initialValue) {
		super(typeURI, parent, label, ref, valueRestriction, initialValue);
		if (parent instanceof Repeat)
			return;
		if (ref == null)
			throw new IllegalArgumentException("The property path for input controls must be not null!");
	}
	
	boolean checkSubmission() {
		Repeat r = getAncestorRepeat();
		if (r != null) {
			if (isMandatory()) {
				List values = r.getAllValues(getReferencedPPath());
				if (values != null)
					for (Iterator i=values.iterator(); i.hasNext(); )
						if (i.next() == null)
							return false;
			}
			return r.checkSubmission();
		}
		
		Object o = getValue();
		if (o == null)
			return !isMandatory();
		
		return !(o instanceof List)  ||  this.getClass() == Select.class;
	}
	
	/**
	 * @see #PROP_INPUT_ALERT
	 */
	public String getAlertString() {
		return (String) props.get(PROP_INPUT_ALERT);
	}
	
//	public Object[] getAllowedValues() {
//		Restriction r = getControlRestrictions();
//		Object[] res = (r == null)? null : r.getEnumeratedValues();
//		if (res == null) {
//			r = getModelBasedRestrictions();
//			res = (r == null)? null : r.getEnumeratedValues();
//		}
//		return res;
//	}
	
	/**
	 * @see #PROP_IS_MANDATORY
	 */
	public boolean isMandatory() {
		return Boolean.TRUE.equals(props.get(PROP_IS_MANDATORY));
	}
	
	/**
	 * @see #PROP_INPUT_ALERT
	 */
	public void setAlertString(String value) {
		if (value != null  &&  !props.containsKey(PROP_INPUT_ALERT))
			props.put(PROP_INPUT_ALERT, value);
	}
	
	 void setMandatory() {
		 props.put(PROP_IS_MANDATORY, Boolean.TRUE);
	 }
	
	/**
	 * @see FormControl#setProperty(String, Object)
	 */
	public void setProperty(String propURI, Object value) {
		if (PROP_INPUT_ALERT.equals(propURI)) {
			if (value instanceof String  &&  !props.containsKey(propURI))
				props.put(propURI, value);
		} else
			super.setProperty(propURI, value);
	}
	
	/**
	 * Tries to store the given value as user input and returns true if it passes all the known restrictions,
	 * false otherwise.
	 */
	public boolean storeUserInput(Object value) {
		if (isMandatory()  &&  value == null)
			return false;
		
		Group g = getParentGroup();
		if (g == null)
			return false;
		
		PropertyPath pp = getReferencedPPath();
		return g.setValue((pp==null? null : pp.getThePath()), value, getLocalRestrictions());
	}
}
