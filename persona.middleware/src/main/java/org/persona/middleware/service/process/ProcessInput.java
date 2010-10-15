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
package org.persona.middleware.service.process;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.persona.middleware.PResource;

/**
 * @author mtazari
 *
 */
public class ProcessInput extends ProcessParameter {
	public static final String MY_URI = OWLS_PROCESS_NAMESPACE + "Input";
	
	public static ProcessInput toInput(PResource r) {
		if (r instanceof ProcessInput)
			return ((ProcessInput) r).isWellFormed()? (ProcessInput) r : null;
		
		if (r == null  ||  !MY_URI.equals(r.getType()))
			return null;
		
		ProcessInput input = new ProcessInput(r.getURI());
		for (Enumeration e = r.getPropertyURIs(); e.hasMoreElements();) {
			Object key = e.nextElement();
			input.props.put(key, r.getProperty(key.toString()));
		}	
		return input.isWellFormed()? input : null;
	}
	
	public static List checkParameterList(Object value) {
		if (value instanceof ProcessInput
				&&  ((ProcessInput) value).isWellFormed()) {
			ArrayList l = new ArrayList(1);
			l.add(value);
			return l;
		} else if (value instanceof List) {
			for (int i=0; i<((List) value).size(); i++) {
				Object o = ((List) value).get(i);
				if (!(value instanceof ProcessInput)) {
					if (o instanceof PResource) {
						o = ProcessInput.toInput((PResource) o);
						if (o == null)
							return null;
						else
							((List) value).set(i, o);
					} else
						return null;
				} else if (!((ProcessParameter) o).isWellFormed())
					return null;
			}
			return (List) value;
		} else if (value instanceof PResource) {
			value = ProcessInput.toInput((PResource) value);
			if (value == null)
				return null;
			ArrayList l = new ArrayList(1);
			l.add(value);
			return l;
		} else
			return null;
	}
	
	public ProcessInput(String uri) {
		super(uri, MY_URI);
	}
}
