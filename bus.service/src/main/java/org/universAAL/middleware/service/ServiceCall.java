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
package org.universAAL.middleware.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.owls.process.OutputBinding;
import org.universAAL.middleware.service.owls.process.ProcessInput;

/**
 * Operations of {@link ServiceCallee}s will be called by passing an instance of this class.
 * The {@link ServiceCallee} must then identify the operation called using the the return
 * value of {@link #getProcessURI()}. In a next step, if the operation called needs input
 * values, they can be collected by several calls to {@link #getInputValue(String)}. In case
 * of optional input parameters, the operation should be "executed" with the default value
 * of the optional parameters only if {@link #getInputValue(String)} returns null; otherwise
 * the passed value must be used.
 *  
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class ServiceCall extends Resource {

	/**
	 * A resource URI that specifies the resource as a service call.
	 */
	public static final String MY_URI = ProcessInput.OWLS_PROCESS_NAMESPACE + "Perform";

	/**
	 * A ServiceCall that is a realization of the OWL-S process:ThisPerform.
	 */
	public static final ServiceCall THIS_SERVICE_CALL;

	static {
		THIS_SERVICE_CALL = new ServiceCall(null, ProcessInput.OWLS_PROCESS_NAMESPACE + "ThisPerform");
	}
	
	/**
	 * A property key for the actual input value within an input resource.
	 */
	public static final String PROP_OWLS_BINDING_VALUE_DATA = ProcessInput.OWLS_PROCESS_NAMESPACE + "valueData";

	/**
	 * A property key that corresponds to the URI of the OWL-S perform process.
	 */
	public static final String PROP_OWLS_PERFORM_PROCESS = ProcessInput.OWLS_PROCESS_NAMESPACE + "process";

	/**
	 * A property key that points to the <code>List</code> containing all of the input resources.
	 */
	public static final String PROP_OWLS_PERFORM_HAS_DATA_FROM = ProcessInput.OWLS_PROCESS_NAMESPACE + "hasDataFrom";

	/**
	 * A type that identifies a resource as an input resource of a call.
	 */
	public static final String TYPE_OWLS_INPUT_BINDING = ProcessInput.OWLS_PROCESS_NAMESPACE + "InputBinding";
	
	private ServiceCall(Object dummy, String uri) {
		super(uri);
		addType(MY_URI, true);
	}
	
	/**
	 * Default constructor of the class. Does not set anything besides the resource identification URI <code>MY_URI</code>. 
	 */
	public ServiceCall() {
		super();
		addType(MY_URI, true);
	}

	/**
	 * A constructor that besides the  resource identification URI <code>MY_URI</code>,
	 * sets the the URI of the OWL-S perform process.
	 * @param processURI the URI of the OWL-S perform process.
	 */
	public ServiceCall(String processURI) {
		super();
		if (processURI == null)
			throw new NullPointerException();
		addType(MY_URI, true);
		props.put(PROP_OWLS_PERFORM_PROCESS, new Resource(processURI));
	}

	/**
	 * Adds an input parameter to the call.
	 * @param inputURI the URI of the input parameter.
	 * @param value the actual value of the input parameter.
	 * @return <code>true</code> if the parameter was successfully set, or <code>false</code> otherwise, for example if null values were passed as arguments.
	 */
	public boolean addInput(String inputURI, Object value) {
		value = TypeMapper.asLiteral(value);
		if (inputURI != null  &&  value != null) {
			Resource binding = new Resource();
			binding.addType(TYPE_OWLS_INPUT_BINDING, true);
			binding.setProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM, new ProcessInput(inputURI));
			binding.setProperty(PROP_OWLS_BINDING_VALUE_DATA, value);
			inputs().add(binding);
			return true;
		}
		return false;
	}

	/**
	 * Retrieves the actual value of an input parameter. 
	 * @param inputURI the URI of the input parameter.
	 * @return the value of the parameter.
	 */
	public Object getInputValue(String inputURI) {
		List inputs =(List) props.get(PROP_OWLS_PERFORM_HAS_DATA_FROM);
		if (inputs == null)
			return null;
		for (Iterator i = inputs.iterator(); i.hasNext();) {
			Resource binding = (Resource) i.next(),
						   in = (Resource) binding.getProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM);
			if (in != null  &&  in.getURI().equals(inputURI)) {
				Object o = binding.getProperty(PROP_OWLS_BINDING_VALUE_DATA);
				if (o instanceof Resource) {
					List aux = ((Resource) o).asList();
					if (aux != null)
						return aux;
				}
				return o;
			}
		}
		return null;
	}
	
	/**
	 * Retrieves the user involved in the call, if there is such.
	 * @return the involved user or null, if no human user is involved.
	 */
	public Resource getInvolvedUser() {
		Object o = props.get(PROP_uAAL_INVOLVED_HUMAN_USER);
		return (o instanceof Resource)? (Resource) o : null;
	}

	/**
	 * Retrieves the URI of the OWL-S perform process.
	 * @return the process URI , or null if no process is set.
	 */
	public String getProcessURI() {
		Resource pr = (Resource) props.get(PROP_OWLS_PERFORM_PROCESS);
		return (pr == null)? null : pr.getURI();
	}

	/* (non-Javadoc)
	 * @see org.universAAL.middleware.rdf.Resource#getPropSerializationType(java.lang.String)
	 */
	public int getPropSerializationType(String propURI) {
		return (PROP_OWLS_PERFORM_PROCESS.equals(propURI)
				|| PROP_OWLS_BINDING_VALUE_DATA.equals(propURI)
				|| PROP_OWLS_PERFORM_HAS_DATA_FROM.equals(propURI))?
						PROP_SERIALIZATION_FULL
						: PROP_uAAL_INVOLVED_HUMAN_USER.equals(propURI)?
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

	/**
	 * Checks whether the URI of the OWL-S perform process is properly set.
	 */
	public boolean isWellFormed() {
		return props.containsKey(PROP_OWLS_PERFORM_PROCESS);
	}
	
	/**
	 * Sets the human user involved in the call. This method is usually invoked by the bus.
	 * @param user the new involved user.
	 */
	public void setInvolvedUser(Resource user) {
		if (user != null  &&  !props.containsKey(PROP_uAAL_INVOLVED_HUMAN_USER))
			props.put(PROP_uAAL_INVOLVED_HUMAN_USER, user);
	}
	
	/**
	 * Sets the URI of the OWL-S perform process. This method is usually invoked by the bus.
	 * @param processURI the new process URI.
	 */
	public void setProcessURI(String processURI) {
		if (processURI != null
				&& !props.containsKey(PROP_OWLS_PERFORM_PROCESS))
			props.put(PROP_OWLS_PERFORM_PROCESS, new Resource(processURI));
	}

	/** 
	 * This method inherits the superclass behavior, but performs some additional checks for
	 * correctness of the property values, specific for the <code>ServiceCall</code>.
	 * @see org.universAAL.middleware.rdf.Resource#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String propURI, Object o) {
		if (propURI != null  &&  o != null  &&  !props.containsKey(propURI)) {
			if (PROP_OWLS_PERFORM_PROCESS.equals(propURI)
					&& o instanceof Resource
					&& !((Resource) o).isAnon()
					&& ((Resource) o).numberOfProperties() == 0)
				props.put(PROP_OWLS_PERFORM_PROCESS, o);
			else if (PROP_OWLS_PERFORM_HAS_DATA_FROM.equals(propURI)) {
				if (o instanceof List
						&& !((List) o).isEmpty())
					for (Iterator i = ((List) o).iterator(); i.hasNext();) {
						Object binding = i.next();
						if (binding instanceof Resource
								&& TYPE_OWLS_INPUT_BINDING.equals(((Resource) binding).getType())) {
							Object toParam = ((Resource) binding).getProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM);
							if (toParam instanceof Resource
									&&  !((Resource) toParam).isAnon()
									&&  ProcessInput.MY_URI.equals(((Resource) toParam).getType())) {
								if (!addInput(toParam.toString(), ((Resource) binding).getProperty(PROP_OWLS_BINDING_VALUE_DATA))) {
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
				else if (o instanceof Resource
						&& TYPE_OWLS_INPUT_BINDING.equals(((Resource) o).getType())) {
					Object toParam = ((Resource) o).getProperty(OutputBinding.PROP_OWLS_BINDING_TO_PARAM);
					if (toParam instanceof Resource
							&&  !((Resource) toParam).isAnon()
							&&  ProcessInput.MY_URI.equals(((Resource) toParam).getType()))
						addInput(toParam.toString(), ((Resource) o).getProperty(PROP_OWLS_BINDING_VALUE_DATA));
				}
			} else if (PROP_uAAL_INVOLVED_HUMAN_USER.equals(propURI)
					&&  o instanceof Resource)
				props.put(PROP_uAAL_INVOLVED_HUMAN_USER, o);
		}
	}
}
