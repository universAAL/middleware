/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.service.owls.process;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class ProcessOutput extends ProcessParameter {
	public static final String MY_URI = OWLS_PROCESS_NAMESPACE + "Output";
	
	public static ProcessOutput toOutput(Resource r) {
		if (r == null  ||  !MY_URI.equals(r.getType()))
			return null;
		ProcessOutput output = new ProcessOutput(r.getURI());
		for (Enumeration e = r.getPropertyURIs(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (!PROP_RDF_TYPE.equals(key))
				output.setProperty(key, r.getProperty(key.toString()));
		}	
		return output.isWellFormed()? output : null;
	}
	
	public static List checkParameterList(Object value) {
		if (value instanceof ProcessOutput
				&&  ((ProcessOutput) value).isWellFormed()) {
			ArrayList l = new ArrayList(1);
			l.add(value);
			return l;
		} else if (value instanceof List) {
			for (int i=0; i<((List) value).size(); i++) {
				Object o = ((List) value).get(i);
				if (!(value instanceof ProcessOutput)) {
					if (o instanceof Resource) {
						o = ProcessOutput.toOutput((Resource) o);
						if (o == null)
							return null;
						else
							((List) value).set(i, o);
					} else
						return null;
				} else if (!((ProcessOutput) o).isWellFormed())
					return null;
			}
			return (List) value;
		} else if (value instanceof Resource) {
			value = ProcessOutput.toOutput((Resource) value);
			if (value == null)
				return null;
			ArrayList l = new ArrayList(1);
			l.add(value);
			return l;
		} else
			return null;
	}
	
	public ProcessOutput(String uri) {
		super(uri, MY_URI);
	}
	
	public ProcessOutput(String uri, Object value) {
		super(uri, MY_URI);
		setParameterValue(value);
	}
}
