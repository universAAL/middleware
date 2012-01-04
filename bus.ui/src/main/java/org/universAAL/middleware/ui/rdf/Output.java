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

import org.universAAL.middleware.rdf.PropertyPath;

/**
 * The abstract class for all types of form controls that bear information to be
 * presented to human users.
 * 
 * @author mtazari
 */
public abstract class Output extends FormControl {
	protected Output() {
		super();
	}

	protected Output(String typeURI, Group parent, Label label,
			PropertyPath ref, Object initialValue) {
		super(typeURI, parent, label, ref, null, initialValue);
	}
}
