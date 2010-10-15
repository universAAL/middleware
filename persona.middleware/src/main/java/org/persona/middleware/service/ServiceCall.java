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
package org.persona.middleware.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.persona.middleware.PResource;
import org.persona.middleware.TypeMapper;
import org.persona.middleware.service.process.OutputBinding;
import org.persona.middleware.service.process.ProcessInput;

/**
 * Operations of {@link ServiceCallee}s will be called by passing an instance of this class.
 * The {@link ServiceCallee} must then identify the operation called using the the return
 * value of {@link #getProcessURI()}. In a next step, if the operation called needs input
 * values, they can be collected by several calls to {@link #getInputValue(String)}. In case
 * of optional input parameters, the operation should be "executed" with the default value
 * of the optional parameters only if {@link #getInputValue(String)} returns null; otherwise
 * the passed value must be used.
 *  
 * @author mtazari
 *
 */
public class ServiceCall extends PResource {
	public static final String MY_URI = ProcessInput.OWLS_PROCESS_NAMESPACE + "Perform";
	public static final ServiceCall THIS_SERVICE_CALL;
	static {
		THIS_SERVICE_CALL = new ServiceCall(null, ProcessInput.OWLS_PROCESS_NAMESPACE + "ThisPerform");
	}
	
	public static final String PROP_OWLS_BINDING_VALUE_DATA = ProcessInput.OWLS_PROCESS_NAMESPACE + "valueData";
	public static final String PROP_OWLS_PERFORM_PROCESS = ProcessInput.OWLS_PROCESS_NAMESPACE + "process";
	public static final String PROP_OWLS_PERFORM_HAS_DATA_FROM = ProcessInput.OWLS_PROCESS_NAMESPACE + "hasDataFrom";
	public static final String TYPE_OWLS_INPUT_BINDING = ProcessInput.OWLS_PROCESS_NAMESPACE + "InputBinding";
	
	private ServiceCall(Object dummy, String uri) {
		super(uri);
		addType(MY_URI, true);
	}
	
	public ServiceCall() {
		super();
		addType(MY_URI, true);
	}

	public ServiceCall(String processURI) {
		super();
		if (processURI == null)
			throw new NullPointerException();
		addType(MY_URI, true);
		props.put(PROP_OWLS_PERFORM_PROCESS, new PResource(processURI));
	}

	public boolean addInput(String inputURI, Object value) {
		value = TypeMapper.asLiteral(value);
		if (inputURI != null  &&  value != null) {
			PResource binding = new PResource();
			binding.addType(TYPE_OWLS_INPUT_BINDING, true);
			binding.setProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM, new ProcessInput(inputURI));
			binding.setProperty(PROP_OWLS_BINDING_VALUE_DATA, value);
			inputs().add(binding);
			return true;
		}
		return false;
	}

	public Object getInputValue(String inputURI) {
		List inputs =(List) props.get(PROP_OWLS_PERFORM_HAS_DATA_FROM);
		if (inputs == null)
			return null;
		for (Iterator i = inputs.iterator(); i.hasNext();) {
			PResource binding = (PResource) i.next(),
						   in = (PResource) binding.getProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM);
			if (in != null  &&  in.getURI().equals(inputURI)) {
				Object o = binding.getProperty(PROP_OWLS_BINDING_VALUE_DATA);
				if (o instanceof PResource) {
					List aux = ((PResource) o).asList();
					if (aux != null)
						return aux;
				}
				return o;
			}
		}
		return null;
	}
	
	public PResource getInvolvedUser() {
		Object o = props.get(PROP_PERSONA_INVOLVED_HUMAN_USER);
		return (o instanceof PResource)? (PResource) o : null;
	}

	public String getProcessURI() {
		PResource pr = (PResource) props.get(PROP_OWLS_PERFORM_PROCESS);
		return (pr == null)? null : pr.getURI();
	}

	public int getPropSerializationType(String propURI) {
		return (PROP_OWLS_PERFORM_PROCESS.equals(propURI)
				|| PROP_OWLS_BINDING_VALUE_DATA.equals(propURI)
				|| PROP_OWLS_PERFORM_HAS_DATA_FROM.equals(propURI))?
						PROP_SERIALIZATION_FULL
						: PROP_PERSONA_INVOLVED_HUMAN_USER.equals(propURI)?
								PROP_SERIALIZATION_REDUCED
								: PROP_SERIALIZATION_OPTIONAL;
	}
	
	private List inputs() {
		List answer =(List) props.get(PROP_OWLS_PERFORM_HAS_DATA_FROM);
		if (answer == null) {
			answer = new ArrayList();
			props.put(PROP_OWLS_PERFORM_HAS_DATA_FROM, answer);
		}
		return answer;
	}

	public boolean isWellFormed() {
		return props.containsKey(PROP_OWLS_PERFORM_PROCESS);
	}
	
	public void setInvolvedUser(PResource user) {
		if (user != null  &&  !props.containsKey(PROP_PERSONA_INVOLVED_HUMAN_USER))
			props.put(PROP_PERSONA_INVOLVED_HUMAN_USER, user);
	}
	
	public void setProcessURI(String processURI) {
		if (processURI != null
				&& !props.containsKey(PROP_OWLS_PERFORM_PROCESS))
			props.put(PROP_OWLS_PERFORM_PROCESS, new PResource(processURI));
	}

	public void setProperty(String propURI, Object o) {
		if (propURI != null  &&  o != null  &&  !props.containsKey(propURI)) {
			if (PROP_OWLS_PERFORM_PROCESS.equals(propURI)
					&& o instanceof PResource
					&& !((PResource) o).isAnon()
					&& ((PResource) o).numberOfProperties() == 0)
				props.put(PROP_OWLS_PERFORM_PROCESS, o);
			else if (PROP_OWLS_PERFORM_HAS_DATA_FROM.equals(propURI)) {
				if (o instanceof List
						&& !((List) o).isEmpty())
					for (Iterator i = ((List) o).iterator(); i.hasNext();) {
						Object binding = i.next();
						if (binding instanceof PResource
								&& TYPE_OWLS_INPUT_BINDING.equals(((PResource) binding).getType())) {
							Object toParam = ((PResource) binding).getProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM);
							if (toParam instanceof PResource
									&&  !((PResource) toParam).isAnon()
									&&  ProcessInput.MY_URI.equals(((PResource) toParam).getType())) {
								if (!addInput(toParam.toString(), ((PResource) binding).getProperty(PROP_OWLS_BINDING_VALUE_DATA))) {
									inputs().clear();
									return;
								}
							} else {
								inputs().clear();
								return;
							}
						} else {
							inputs().clear();
							return;
						}
					}
				else if (o instanceof PResource
						&& TYPE_OWLS_INPUT_BINDING.equals(((PResource) o).getType())) {
					Object toParam = ((PResource) o).getProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM);
					if (toParam instanceof PResource
							&&  !((PResource) toParam).isAnon()
							&&  ProcessInput.MY_URI.equals(((PResource) toParam).getType()))
						addInput(toParam.toString(), ((PResource) o).getProperty(PROP_OWLS_BINDING_VALUE_DATA));
				}
			} else if (PROP_PERSONA_INVOLVED_HUMAN_USER.equals(propURI)
					&&  o instanceof PResource)
				props.put(PROP_PERSONA_INVOLVED_HUMAN_USER, o);
		}
	}
}
