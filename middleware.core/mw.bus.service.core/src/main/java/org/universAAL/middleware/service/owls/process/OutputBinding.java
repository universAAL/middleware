/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research 
	
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

import java.util.Enumeration;
import java.util.HashMap;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.AggregatingFilter;
import org.universAAL.middleware.service.owl.Service;

/**
 * Support for constructing an OWL-S
 * http://www.daml.org/services/owl-s/1.1/Process.owl#OutputBinding that can be
 * used in the definition of process results according to the uAAL model. In the
 * uAAL model, an output parameter can reflect the value of a reachable property
 * using an instance of {@link PropertyPath} as proces:vaueForm or get a
 * converted form of such a reachable property using an instance of
 * {@link Conversion}s as process:valueFunction. As the type hierarchy of output
 * bindings plays no specific role in ontological reasoning, they are not
 * defined as a subclasses of {@link ManagedIndividual} but this class simply
 * provides support for the definition of the needed constant values as well as
 * helps in constructing a {@link Resource} representation of them.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class OutputBinding {
    public static final String PROP_OWLS_BINDING_TO_PARAM = ProcessOutput.OWLS_PROCESS_NAMESPACE
	    + "toParam";
    public static final String PROP_OWLS_BINDING_VALUE_FORM = ProcessOutput.OWLS_PROCESS_NAMESPACE
	    + "valueForm";
    public static final String PROP_OWLS_BINDING_VALUE_FUNCTION = ProcessOutput.OWLS_PROCESS_NAMESPACE
	    + "valueFunction";
    public static final String TYPE_OWLS_OUTPUT_BINDING = ProcessOutput.OWLS_PROCESS_NAMESPACE
	    + "OutputBinding";

    /**
     * Returns true or false depending if the object o contains the right
     * bindings
     * 
     * @param Object
     *            o The object that contains the binding to check
     * 
     * @return true if the object contains the right bindings, return false on
     *         contrary.
     */

    static boolean checkBinding(Object o) {
	if (o instanceof Resource && ((Resource) o).isAnon()) {
	    if (((Resource) o).numberOfProperties() != 3)
		return false;
	    for (Enumeration e = ((Resource) o).getPropertyURIs(); e
		    .hasMoreElements();) {
		Object key = e.nextElement();
		if (Resource.PROP_RDF_TYPE.equals(key)) {
		    if (!TYPE_OWLS_OUTPUT_BINDING.equals(((Resource) o)
			    .getType()))
			return false;
		} else if (PROP_OWLS_BINDING_TO_PARAM.equals(key)) {
		    key = ((Resource) o)
			    .getProperty(PROP_OWLS_BINDING_TO_PARAM);
		    if (!(key instanceof ProcessOutput))
			if (key instanceof Resource) {
			    key = ProcessOutput.toOutput((Resource) key);
			    if (key == null)
				return false;
			    ((Resource) o).setProperty(
				    PROP_OWLS_BINDING_TO_PARAM, key);
			} else
			    return false;
		    if (key == null)
			return false;
		} else if (PROP_OWLS_BINDING_VALUE_FORM.equals(key)) {
		    key = ((Resource) o)
			    .getProperty(PROP_OWLS_BINDING_VALUE_FORM);
		    if (key instanceof Resource) {
			if (!(key instanceof PropertyPath)) {
			    key = PropertyPath.toPropertyPath((Resource) key);
			    if (key == null)
				return false;
			    ((Resource) o).setProperty(
				    PROP_OWLS_BINDING_VALUE_FORM, key);
			}
		    } else
			return false;
		} else if (PROP_OWLS_BINDING_VALUE_FUNCTION.equals(key)) {
		    key = ((Resource) o)
			    .getProperty(PROP_OWLS_BINDING_VALUE_FUNCTION);
		    if (!(key instanceof AggregatingFilter)
			    && !Conversion.checkConversion(key))
			return false;
		} else
		    return false;
	    }
	    return true;
	}
	return false;
    }

    /**
     * Creates a binding on the output requested
     * 
     * @param AggregatingFilter
     *            Filter to define on the output.
     * @param ProcessOutput
     *            Process Output to bind
     * @param ProcessOutput
     *            aggregating filter to associate
     * @return return the resource with the new binding.
     */

    static Resource constructAggregatingBinding(ProcessOutput toParam,
	    AggregatingFilter filter) {
	Resource result = new Resource();
	result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
	result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
	result.setProperty(PROP_OWLS_BINDING_VALUE_FUNCTION,
		(filter.serializesAsXMLLiteral() ? filter : filter.toLiteral()));
	return result;
    }

    /**
     * Creates a conversion binding on the output requested
     * 
     * @param ProcessOutput
     *            Process Output to bind
     * @param PropertyPath
     *            Property path to set
     * @param TypeURI
     *            Type Uri of the conversion binding.
     * @return return the resource with the new binding.
     */

    static Resource constructClassConversionBinding(ProcessOutput toParam,
	    PropertyPath sourceProp, TypeURI targetClass) {
	Resource result = new Resource();
	result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
	result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
	result.setProperty(PROP_OWLS_BINDING_VALUE_FUNCTION,
		Conversion.constructClassConversion(sourceProp, targetClass));
	return result;
    }

    /**
     * Creates a language conversion binding on the output requested
     * 
     * @param ProcessOutput
     *            Process Output to bind
     * @param PropertyPath
     *            source associated to the language conversion
     * @param targetLang
     *            Target language to associate.
     * @return return the resource with the language conversion binding.
     */

    static Resource constructLanguageConversionBinding(ProcessOutput toParam,
	    PropertyPath sourceProp, String targetLang) {
	Resource result = new Resource();
	result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
	result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
	result.setProperty(PROP_OWLS_BINDING_VALUE_FUNCTION,
		Conversion.constructLanguageConversion(sourceProp, targetLang));
	return result;
    }

    /**
     * Creates a simple binding withe the property path of the argument
     * 
     * @param ProcessOutput
     *            Process Output to bind
     * @param PropertyPath
     *            Property path to associate.
     * @return return the resource with the new binding.
     */

    static Resource constructSimpleBinding(ProcessOutput toParam,
	    PropertyPath sourceProp) {
	Resource result = new Resource();
	result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
	result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
	result.setProperty(
		PROP_OWLS_BINDING_VALUE_FORM,
		(sourceProp.serializesAsXMLLiteral() ? sourceProp : sourceProp
			.toLiteral()));
	return result;
    }

    /**
     * Creates a language conversion binding on the output requested
     * 
     * @param ProcessOutput
     *            Process Output to bind
     * @param PropertyPath
     *            sourcePropo associated to the language conversion
     * @param targetUnit
     *            Target Unit to associate.
     * @return return the resource with the unit conversion binding binding.
     */

    static Resource constructUnitConversionBinding(ProcessOutput toParam,
	    PropertyPath sourceProp, String targetUnit) {
	Resource result = new Resource();
	result.addType(TYPE_OWLS_OUTPUT_BINDING, true);
	result.setProperty(PROP_OWLS_BINDING_TO_PARAM, toParam);
	result.setProperty(PROP_OWLS_BINDING_VALUE_FUNCTION,
		Conversion.constructUnitConversion(sourceProp, targetUnit));
	return result;
    }

    /**
     * Finds if the requested resource match with the context binding
     * 
     * @param Resource
     *            req. Resource requested to check.
     * @param Hastable
     *            context to check.
     * @return return true if it match and false on contrary.
     */

    static boolean findMatchingBinding(Resource req, Resource[] offer,
	    HashMap context) {
	PropertyPath srcPath = null;
	Object aux = req.getProperty(PROP_OWLS_BINDING_VALUE_FUNCTION);
	if (aux instanceof AggregatingFilter) {
	    // AggregatingBinding
	    srcPath = (PropertyPath) ((AggregatingFilter) aux)
		    .getFunctionParams().get(0);
	} else {
	    aux = req.getProperty(PROP_OWLS_BINDING_VALUE_FORM);
	    if (aux instanceof PropertyPath)
		// SimpleBinding
		srcPath = (PropertyPath) aux;
	}
	for (int i = 0; i < offer.length; i++) {
	    PropertyPath offeredValue = (PropertyPath) offer[i]
		    .getProperty(PROP_OWLS_BINDING_VALUE_FORM);
	    if (offeredValue != null && offeredValue.equals(srcPath)) {
		// we found an offer for the property path that was requested as
		// output value
		// -> we do not need to check the parameter type and cardinality
		// because this is done already by checking the instance level
		// restrictions

		ProcessOutput toParam = (ProcessOutput) offer[i]
			.getProperty(PROP_OWLS_BINDING_TO_PARAM);

		context.put(toParam.getURI(), req);
		return true;
	    }
	}
	return false;
    }
}
