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

import java.util.Enumeration;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;

/**
 * A uAAL-specific value function for binding output parameters stating that the given output
 * parameter represents a converted representation of a reachable property.
 * There are three types of conversions: (1) UnitConversion, e.g. when the value of a reachable
 * property that is originally represented in, say, meters is put out in, say, feet; (2) 
 * LanguageConversion, e.g. when the value of a reachable* property that is originally
 * represented in, say, English is put out in, say, German; and (3) ClassConversion, e.g. when
 * the value of a reachable property that is originally represented as an instance of, say, 
 * myOnt:Person is put out in, say, youOnt:Human.
 * As the type hierarchy of conversions plays no specific role in ontological reasoning, they are not
 * defined as subclasses of {@link ManagedIndividual} but this class simply
 * provides support for the definition of the needed constant values as well as helps in
 * constructing a {@link Resource} representation of them.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class Conversion {
	public static final String PROP_OUTPUT_CONVERSION_SOURCE =
		Resource.uAAL_SERVICE_NAMESPACE + "sourceProperty";
	public static final String PROP_OUTPUT_CONVERSION_TARGET_CLASS =
		Resource.uAAL_SERVICE_NAMESPACE + "targetClass";
	public static final String PROP_OUTPUT_CONVERSION_TARGET_LANGUAGE =
		Resource.uAAL_SERVICE_NAMESPACE + "targetLanguage";
	public static final String PROP_OUTPUT_CONVERSION_TARGET_UNIT =
		Resource.uAAL_SERVICE_NAMESPACE + "targetUnit";
	public static final String TYPE_OUTPUT_CLASS_CONVERSION =
		Resource.uAAL_SERVICE_NAMESPACE + "ClassConversion";
	public static final String TYPE_OUTPUT_LANGUAGE_CONVERSION =
		Resource.uAAL_SERVICE_NAMESPACE + "LanguageConversion";
	public static final String TYPE_OUTPUT_UNIT_CONVERSION =
		Resource.uAAL_SERVICE_NAMESPACE + "UnitConversion";
	
	static boolean checkConversion(Object o) {
		if (o instanceof Resource
				&&  ((Resource) o).isAnon()
				&&  ((Resource) o).serializesAsXMLLiteral()) {
			int num = 0;
			for (Enumeration e=((Resource) o).getPropertyURIs(); e.hasMoreElements(); e.nextElement())
				num++;
			if (num != 3)
				return false;
			Object t = ((Resource)  o).getProperty(PROP_OUTPUT_CONVERSION_SOURCE);
			if (!(t instanceof Resource))
				return false;
			if (!(t instanceof PropertyPath)) {
				t = PropertyPath.toPropertyPath((Resource) t);
				if (t == null)
					return false;
				((Resource) o).setProperty(PROP_OUTPUT_CONVERSION_SOURCE, t);
			}
			t = ((Resource) o).getProperty(Resource.PROP_RDF_TYPE);
			return (TYPE_OUTPUT_CLASS_CONVERSION.equals(t)
					&&  ((Resource) o).getProperty(PROP_OUTPUT_CONVERSION_TARGET_CLASS) instanceof TypeURI)
				|| (TYPE_OUTPUT_LANGUAGE_CONVERSION.equals(t)
					&&  ((Resource) o).getProperty(PROP_OUTPUT_CONVERSION_TARGET_LANGUAGE) instanceof String)
				|| (TYPE_OUTPUT_UNIT_CONVERSION.equals(t)
					&&  ((Resource) o).getProperty(PROP_OUTPUT_CONVERSION_TARGET_UNIT) instanceof String);
		}
		return false;
	}
	
	static Resource constructClassConversion(PropertyPath sourceProp, TypeURI targetClass) {
		Resource result = new Resource(true);
		result.addType(TYPE_OUTPUT_CLASS_CONVERSION, true);
		result.setProperty(PROP_OUTPUT_CONVERSION_SOURCE, sourceProp);
		result.setProperty(PROP_OUTPUT_CONVERSION_TARGET_CLASS, targetClass);
		return result;
	}
	
	static Resource constructLanguageConversion(PropertyPath sourceProp, String targetLang) {
		Resource result = new Resource(true);
		result.addType(TYPE_OUTPUT_LANGUAGE_CONVERSION, true);
		result.setProperty(PROP_OUTPUT_CONVERSION_SOURCE, sourceProp);
		result.setProperty(PROP_OUTPUT_CONVERSION_TARGET_LANGUAGE, targetLang);
		return result;
	}
	
	static Resource constructUnitConversion(PropertyPath sourceProp, String targetUnit) {
		Resource result = new Resource(true);
		result.addType(TYPE_OUTPUT_UNIT_CONVERSION, true);
		result.setProperty(PROP_OUTPUT_CONVERSION_SOURCE, sourceProp);
		result.setProperty(PROP_OUTPUT_CONVERSION_TARGET_UNIT, targetUnit);
		return result;
	}
}
