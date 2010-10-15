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

import java.util.Enumeration;
import java.util.Hashtable;

import org.persona.middleware.PResource;
import org.persona.middleware.service.AggregatingFilter;
import org.persona.middleware.service.PropertyPath;
import org.persona.ontology.expr.TypeURI;

/**
 * Support for constructing an OWL-S http://www.daml.org/services/owl-s/1.1/Process.owl#OutputBinding
 * that can be used in the definition of process results according to the PERSONA model.
 * In the PERSONA model, an output parameter can reflect the value of a reachable property using
 * an instance of {@link PropertyPath} as proces:vaueForm or get a converted form of such a reachable
 * property using an instance of {@link Conversion}s as process:valueFunction.
 * As the type hierarchy of output bindings plays no specific role in ontological reasoning, they are not
 * defined as a subclasses of {@link org.persona.ontology.ManagedIndividual} but this class simply
 * provides support for the definition of the needed constant values as well as helps in
 * constructing a {@link PResource} representation of them.
 * 
 * @author mtazari
 *
 */
public class OutputBinding {
	public static final String PROP_OWLS_BINDING_TO_PARAM = 
		ProcessOutput.OWLS_PROCESS_NAMESPACE + "toParam";
	public static final String PROP_OWLS_BINDING_VALUE_FORM = 
		ProcessOutput.OWLS_PROCESS_NAMESPACE + "valueForm";
	public static final String PROP_OWLS_BINDING_VALUE_FUNCTION = 
		ProcessOutput.OWLS_PROCESS_NAMESPACE + "valueFunction";
	public static final String TYPE_OWLS_OUTPUT_BINDING = 
		ProcessOutput.OWLS_PROCESS_NAMESPACE + "OutputBinding";
	
	static boolean checkBinding(Object o) {
		if (o instanceof PResource  &&  ((PResource) o).isAnon()) {
			if (((PResource) o).numberOfProperties() != 3)
				return false;
			for (Enumeration e=((PResource) o).getPropertyURIs(); e.hasMoreElements(); ) {
				Object key = e.nextElement();
				if (PResource.PROP_RDF_TYPE.equals(key)) {
					if (!TYPE_OWLS_OUTPUT_BINDING.equals(((PResource) o).getType()))
						return false;
				} else if (PROP_OWLS_BINDING_TO_PARAM.equals(key)) {
					key = ((PResource)  o).getProperty(PROP_OWLS_BINDING_TO_PARAM);
					if (!(key instanceof ProcessOutput))
						if (key instanceof PResource) {
							key = ProcessOutput.toOutput((PResource) key);
							if (key == null)
								return false;
							((PResource) o).setProperty(PROP_OWLS_BINDING_TO_PARAM, key);
						} else
							return false;
					if (key == null)
						return false;
				} else if (PROP_OWLS_BINDING_VALUE_FORM.equals(key)) {
					key = ((PResource)  o).getProperty(PROP_OWLS_BINDING_VALUE_FORM);
					if (key instanceof PResource) {
						if (!(key instanceof PropertyPath)) {
							key = PropertyPath.toPropertyPath((PResource) key);
							if (key == null)
								return false;
							((PResource) o).setProperty(PROP_OWLS_BINDING_VALUE_FORM, key);
						}
					} else
						return false;
				} else if (PROP_OWLS_BINDING_VALUE_FUNCTION.equals(key)) {
					key = ((PResource)  o).getProperty(PROP_OWLS_BINDING_VALUE_FUNCTION);
					if (!(key instanceof AggregatingFilter)
							&&  !Conversion.checkConversion(key))
						return false;
				} else
					return false;
			}
			return true;
		}
		return false;
	}
	
	static PResource constructAggregatingBinding(ProcessOutput toParam, AggregatingFilter filter) {
		PResource result = new PResource();
		result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
		result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
		result.setProperty(PROP_OWLS_BINDING_VALUE_FUNCTION,
				(filter.serializesAsXMLLiteral()? filter : filter.toLiteral()));
		return result;
	}
	
	static PResource constructClassConversionBinding(ProcessOutput toParam,
			PropertyPath sourceProp, TypeURI targetClass) {
		PResource result = new PResource();
		result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
		result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
		result.setProperty(PROP_OWLS_BINDING_VALUE_FUNCTION,
				Conversion.constructClassConversion(sourceProp, targetClass));
		return result;
	}
	
	static PResource constructLanguageConversionBinding(ProcessOutput toParam,
			PropertyPath sourceProp, String targetLang) {
		PResource result = new PResource();
		result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
		result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
		result.setProperty(PROP_OWLS_BINDING_VALUE_FUNCTION,
				Conversion.constructLanguageConversion(sourceProp, targetLang));
		return result;
	}
	
	static PResource constructSimpleBinding(ProcessOutput toParam, PropertyPath sourceProp) {
		PResource result = new PResource();
		result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
		result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
		result.setProperty(PROP_OWLS_BINDING_VALUE_FORM,
				(sourceProp.serializesAsXMLLiteral()? sourceProp : sourceProp.toLiteral()));
		return result;
	}
	
	static PResource constructUnitConversionBinding(ProcessOutput toParam,
			PropertyPath sourceProp, String targetUnit) {
		PResource result = new PResource();
		result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
		result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
		result.setProperty(PROP_OWLS_BINDING_VALUE_FUNCTION,
				Conversion.constructUnitConversion(sourceProp, targetUnit));
		return result;
	}
	
	static boolean findMatchingBinding(PResource req, PResource[] offer, Hashtable context) {
		PropertyPath srcPath = null;
		Object aux = req.getProperty(PROP_OWLS_BINDING_VALUE_FUNCTION);
		if (aux instanceof AggregatingFilter) {
			// AggregatingBinding
			srcPath = (PropertyPath) ((AggregatingFilter) aux).getFunctionParams().get(0);
		} else {
			aux = req.getProperty(PROP_OWLS_BINDING_VALUE_FORM);
			if (aux instanceof PropertyPath)
				// SimpleBinding
				srcPath = (PropertyPath) aux;
		}
		for (int i=0; i<offer.length; i++) {
			PropertyPath offeredValue = (PropertyPath) offer[i].getProperty(PROP_OWLS_BINDING_VALUE_FORM);
			if (offeredValue != null  &&  offeredValue.equals(srcPath)) {
				context.put(((ProcessOutput) offer[i].getProperty(PROP_OWLS_BINDING_TO_PARAM)).getURI(), req);
				return true;
			}
		}
		return false;
	}
}
