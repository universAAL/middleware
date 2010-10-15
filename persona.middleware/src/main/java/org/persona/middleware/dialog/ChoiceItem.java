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

/**
 * A special case of {@link Label labels} with an associated value suitable for usage as a leaf
 * entry in a hierarchy of possible choices. Selecting a choice item in such a hierarchy is equivalent
 * to selecting the value associated with that item. 
 * 
 * @author mtazari
 *
 */
public class ChoiceItem extends Label {

	public static final String MY_URI = Form.PERSONA_DIALOG_NAMESPACE + "ChoiceItem";
	
	/**
	 * Property for accessing the value associated with a choice item.
	 */
	public static final String PROP_ITEM_VALUE = Form.PERSONA_DIALOG_NAMESPACE + "choiceItemValue";
	
	/**
	 * For use by de-serializers only.
	 */
	public ChoiceItem() {
		super();
	}
	
	/**
	 * Constructs a new choice item.
	 * 
	 * @param labelText see {@link Label#Label(String, String)}
	 * @param iconURL see {@link Label#Label(String, String)}
	 * @param value The value to be associated with this choice item.
	 */
	public ChoiceItem(String labelText, String iconURL, Object value) {
		super(labelText, iconURL);
		props.put(PROP_ITEM_VALUE, value);
	}

	/**
	 * Returns the value associated with this choice item.
	 */
	public Object getValue() {
		Object o = props.get(PROP_ITEM_VALUE);;
		return (o == null)? toString() : o;
	}
}
