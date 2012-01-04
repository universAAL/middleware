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
 * A subclass of {@link Select} that allows only one selection.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class Select1 extends Select {
	public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE + "Select1";
	
	/**
	 * For exclusive usage by de-serializers.
	 */
	public Select1() {
		super();
	}

	/**
	 * For exclusive usage by the applications.
	 * 
	 * @param parent
	 *            The group to contain this select1 object.
	 * @param label
	 *            The label.
	 * @param ref
	 *            mandatory property path within the form data to which this
	 *            select1 object refers.
	 * @param valueRestriction
	 *            Optional local restrictions on the value of this select1
	 *            object.
	 * @param initialValue
	 *            Optional initial / default value that will be made available
	 *            in the form data.
	 */
	public Select1(Group parent, Label label, PropertyPath ref,
			MergedRestriction valueRestriction, Object initialValue) {
		super(MY_URI, parent, label, ref, valueRestriction, initialValue);
	}
}
