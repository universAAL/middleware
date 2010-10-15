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

import org.persona.middleware.TypeMapper;
import org.persona.middleware.service.PropertyPath;
import org.persona.ontology.expr.Restriction;

/**
 * An input control for getting larger text input.
 * 
 * @author mtazari
 */
public class TextArea extends Input {
	public static final String MY_URI = Form.PERSONA_DIALOG_NAMESPACE + "TextArea";
	
	/**
	 * For exclusive use of de-serializers.
	 */
	public TextArea() {
		super();
	}
	
	/**
	 * For exclusive use of applications.
	 * 
	 * @param parent The mandatory parent group as the direct container of this text area. See {@link FormControl#PROP_PARENT_CONTROL}.
	 * @param label The optional {@link Label} to be associated with this text area. See {@link FormControl#PROP_CONTROL_LABEL}.
	 * @param ref See {@link FormControl#PROP_REFERENCED_PPATH}; mandatory.
	 * @param valueRestriction See {@link Input#PROP_VALUE_RESTRICTION}; optional.
	 * @param initialValue The optional initial value to be stored in form data under the path given for
	 * the above <code>ref</code> parameter.
	 */
	public TextArea(Group parent, Label label,
			PropertyPath ref, Restriction valueRestriction, String initialValue) {
		super(MY_URI, parent, label, ref, valueRestriction, initialValue);
	}

	/**
	 * Overrides the default implementation as a text area normally has no upper limit on number of characters.
	 */
	public int getMaxLength() {
		// not applicable
		return -1;
	}
	
	/**
	 * Overrides the default implementation by constantly returning xsd:string.
	 */
	public String getTypeURI() {
		return TypeMapper.getDatatypeURI(String.class);
	}
	
	/**
	 * Overrides the default implementation to accept only strings.
	 */
	public boolean storeUserInput(Object value) {
		return (value instanceof String)? super.storeUserInput(value) : false;
	}
}
