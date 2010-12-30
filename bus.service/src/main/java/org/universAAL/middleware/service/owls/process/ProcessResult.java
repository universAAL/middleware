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
import java.util.Hashtable;
import java.util.List;

import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.AggregatingFilter;
import org.universAAL.middleware.service.PropertyPath;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class ProcessResult extends Resource {
	public static final String PROP_OWLS_RESULT_HAS_EFFECT = 
		ProcessOutput.OWLS_PROCESS_NAMESPACE + "hasEffect";
	public static final String PROP_OWLS_RESULT_WITH_OUTPUT = 
		ProcessOutput.OWLS_PROCESS_NAMESPACE + "withOutput";
	public static final String TYPE_OWLS_RESULT = 
		ProcessOutput.OWLS_PROCESS_NAMESPACE + "Result";
	
	public static boolean checkEffects(Resource[] req, Resource[] offer, Hashtable context) {
		if (req == null  ||  req.length == 0)
			return (offer == null  ||  offer.length == 0);
		if (offer == null  ||  offer.length != req.length)
			return false;
		for (int i=0; i<req.length; i++) {
			if (!ProcessEffect.findMatchingEffect(req[i], offer, context))
				return false;
		}
		return true;
	}
	
	public static boolean checkOutputBindings(Resource[] req, Resource[] offer, Hashtable context) {
		if (req == null  ||  req.length == 0)
			return true;
		if (offer == null  ||  offer.length == 0)
			return false;
		for (int i=0; i<req.length; i++) {
			if (!OutputBinding.findMatchingBinding(req[i], offer, context))
				return false;
		}
		return true;
	}
	
	public static ProcessResult toResult(Resource pr) {
		if (pr == null)
			return null;
		
		Object effects = pr.getProperty(PROP_OWLS_RESULT_HAS_EFFECT);
		if (effects instanceof Resource) {
			ArrayList l = new ArrayList(1);
			l.add(effects);
			effects = l;
		}
		Object bindings = pr.getProperty(PROP_OWLS_RESULT_WITH_OUTPUT);
		if (bindings instanceof Resource) {
			ArrayList l = new ArrayList(1);
			l.add(bindings);
			bindings = l;
		}
		if ((bindings == null  &&  effects == null)
				|| (bindings != null  &&  !(bindings instanceof List))
				|| (effects != null  &&  !(effects instanceof List)))
			return null;

		ProcessResult result = new ProcessResult();
		
		if (effects != null) {
			for (int i=0; i<((List) effects).size(); i++)
				if (!ProcessEffect.checkEffect(((List) effects).get(i)))
					return null;
			result.setProperty(PROP_OWLS_RESULT_HAS_EFFECT, effects);
		}
		
		if (bindings != null) {
			for (int i=0; i<((List) bindings).size(); i++)
				if (!OutputBinding.checkBinding(((List) bindings).get(i)))
					return null;
			result.setProperty(PROP_OWLS_RESULT_WITH_OUTPUT, bindings);
		}
		
		return result;
	}
	
	public ProcessResult() {
		super();
		addType(TYPE_OWLS_RESULT, true);
	}
	
	public void addAddEffect(PropertyPath ppath, Object value) {
		effects().add(ProcessEffect.constructAddEffect(ppath, value));
	}
	
	public void addAggregatingOutputBinding(ProcessOutput toParam, AggregatingFilter filter) {
		bindings().add(OutputBinding.constructAggregatingBinding(toParam, filter));
	}
	
	public void addChangeEffect(PropertyPath ppath, Object value) {
		effects().add(ProcessEffect.constructChangeEffect(ppath, value));
	}
	
	public void addRemoveEffect(PropertyPath ppath) {
		effects().add(ProcessEffect.constructRemoveEffect(ppath));
	}
	
	public void addClassConversionOutputBinding(ProcessOutput toParam,
			PropertyPath sourceProp, TypeURI targetClass) {
		bindings().add(OutputBinding.constructClassConversionBinding(toParam, sourceProp, targetClass));
	}
	
	public void addLangConversionOutputBinding(ProcessOutput toParam,
			PropertyPath sourceProp, String targetLang) {
		bindings().add(OutputBinding.constructLanguageConversionBinding(toParam, sourceProp, targetLang));
	}
	
	public void addUnitConversionOutputBinding(ProcessOutput toParam,
			PropertyPath sourceProp, String targetUnit) {
		bindings().add(OutputBinding.constructUnitConversionBinding(toParam, sourceProp, targetUnit));
	}
	
	public void addSimpleOutputBinding(ProcessOutput toParam, PropertyPath sourceProp) {
		bindings().add(OutputBinding.constructSimpleBinding(toParam, sourceProp));
	}
	
	private List bindings() {
		List result = (List) props.get(PROP_OWLS_RESULT_WITH_OUTPUT);
		if (result == null) {
			result = new ArrayList();
			props.put(PROP_OWLS_RESULT_WITH_OUTPUT, result);
		}
		return result;
	}
	
	private List effects() {
		List result = (List) props.get(PROP_OWLS_RESULT_HAS_EFFECT);
		if (result == null) {
			result = new ArrayList();
			props.put(PROP_OWLS_RESULT_HAS_EFFECT, result);
		}
		return result;
	}
	
	public List getBindings() {
		return (List) props.get(PROP_OWLS_RESULT_WITH_OUTPUT);
	}
	
	public List getEffects() {
		return (List) props.get(PROP_OWLS_RESULT_HAS_EFFECT);
	}
	
	public boolean isWellFormed() {
		return props.containsKey(PROP_OWLS_RESULT_WITH_OUTPUT)
				|| props.containsKey(PROP_OWLS_RESULT_HAS_EFFECT);
	}
}
