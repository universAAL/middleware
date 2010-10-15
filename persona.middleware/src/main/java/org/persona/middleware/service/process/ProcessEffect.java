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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.persona.middleware.PResource;
import org.persona.middleware.service.PropertyPath;
import org.persona.ontology.ManagedIndividual;

/**
 * Support for the PERSONA model of process effects. 
 * The PERSONA model of process effects allows three types of effects all affecting a reachable property
 * (see {@link PropertyPath}) from the data that has been selected through both the class-level and the
 * instance-level restrictions of the service at hand.
 * These three effect types are: (1) add effect, (2) change effect, and (3) remove effect, the first two of which
 * has an additional property, namely propertyValue.
 * As the type hierarchy of effects plays no specific role in ontological reasoning, they are not
 * defined as a subclasses of {@link org.persona.ontology.ManagedIndividual} but this class simply
 * provides support for the definition of the needed constant values as well as helps in
 * constructing a {@link PResource} representation of them.
 * 
 * @author mtazari
 *
 */
public abstract class ProcessEffect {
	public static final String PROP_PROCESS_AFFECTED_PROPERTY =
		PResource.PERSONA_SERVICE_NAMESPACE + "affectedProperty";
	public static final String PROP_PROCESS_PROPERTY_VALUE =
		PResource.PERSONA_SERVICE_NAMESPACE + "propertyValue";
	public static final String TYPE_PROCESS_ADD_EFFECT =
		PResource.PERSONA_SERVICE_NAMESPACE + "AddEffect";
	public static final String TYPE_PROCESS_CHANGE_EFFECT =
		PResource.PERSONA_SERVICE_NAMESPACE + "ChangeEffect";
	public static final String TYPE_PROCESS_REMOVE_EFFECT =
		PResource.PERSONA_SERVICE_NAMESPACE + "RemoveEffect";
	
	static boolean checkEffect(Object o) {
		if (o instanceof PResource
				&&  ((PResource) o).isAnon()) {
			int num = 0;
			Object type = null;
			for (Enumeration e=((PResource) o).getPropertyURIs(); e.hasMoreElements(); ) {
				Object key = e.nextElement();
				if (PResource.PROP_RDF_TYPE.equals(key))
					type = ((PResource) o).getType();
				else if (PROP_PROCESS_AFFECTED_PROPERTY.equals(key)) {
					key = ((PResource)  o).getProperty(PROP_PROCESS_AFFECTED_PROPERTY);
					if (key instanceof PResource) {
						if (!(key instanceof PropertyPath)) {
							key = PropertyPath.toPropertyPath((PResource) key);
							if (key == null)
								return false;
							((PResource) o).setProperty(PROP_PROCESS_AFFECTED_PROPERTY, key);
						}
						((PropertyPath) key).unliteral();
						num++;
					}
				} else if (PROP_PROCESS_PROPERTY_VALUE.equals(key)) {
					key = ((PResource)  o).getProperty(PROP_PROCESS_PROPERTY_VALUE);
					if (ProcessParameter.checkDeserialization(key))
						num++;
					else
						return false;
				} else
					return false;
			}
			return (num == 1  &&  TYPE_PROCESS_REMOVE_EFFECT.equals(type))
				|| (num == 2  &&  (TYPE_PROCESS_ADD_EFFECT.equals(type)
						||  TYPE_PROCESS_CHANGE_EFFECT.equals(type)));
		}
		return false;
	}
	
	static PResource constructAddEffect(PropertyPath ppath, Object value) {
		if (ppath == null  ||  value == null)
			return null;
		
		ppath.unliteral();
		PResource ae = new PResource();
		ae.addType(TYPE_PROCESS_ADD_EFFECT, true);
		ae.setProperty(PROP_PROCESS_AFFECTED_PROPERTY, ppath);
		ae.setProperty(PROP_PROCESS_PROPERTY_VALUE, value);
		return ae;
	}
	
	static PResource constructChangeEffect(PropertyPath ppath, Object value) {
		if (ppath == null  ||  value == null)
			return null;
		
		ppath.unliteral();
		PResource ce = new PResource();
		ce.addType(TYPE_PROCESS_CHANGE_EFFECT, true);
		ce.setProperty(PROP_PROCESS_AFFECTED_PROPERTY, ppath);
		ce.setProperty(PROP_PROCESS_PROPERTY_VALUE, value);
		return ce;
	}
	
	static PResource constructRemoveEffect(PropertyPath ppath) {
		if (ppath == null)
			return null;
		
		ppath.unliteral();
		PResource re = new PResource();
		re.addType(TYPE_PROCESS_REMOVE_EFFECT, true);
		re.setProperty(PROP_PROCESS_AFFECTED_PROPERTY, ppath);
		return re;
	}
	
	static boolean findMatchingEffect(PResource req, PResource[] offer, Hashtable context) {
		String effectType = req.getType();
		boolean isRemoveEffect = TYPE_PROCESS_REMOVE_EFFECT.equals(effectType);
		Object affectedProp = req.getProperty(PROP_PROCESS_AFFECTED_PROPERTY);
		Object effectValue = req.getProperty(PROP_PROCESS_PROPERTY_VALUE);
		if (effectValue == null) {
			if (!isRemoveEffect)
				return false;
		} else if (!(effectValue instanceof List)) {
			List aux = new ArrayList(1);
			aux.add(effectValue);
			effectValue = aux;
		}
		for (int i=0; i<offer.length; i++) {
			if (offer[i] == null)
				continue;
			if (effectType.equals(offer[i].getType())
					&& affectedProp.equals(offer[i].getProperty(PROP_PROCESS_AFFECTED_PROPERTY))) {
				if (isRemoveEffect) {
					offer[i] = null;
					return true;
				}
				Object o = ProcessParameter.resolveVarRef(offer[i].getProperty(PROP_PROCESS_PROPERTY_VALUE), context);
				offer[i] = null;
				if (o instanceof ProcessParameter) {
					int max = ((ProcessParameter) o).getMaxCardinality();
					if (((ProcessParameter) o).getMinCardinality() > ((List) effectValue).size()
							|| (max > 0  &&  ((List) effectValue).size() > max))
						return false;
					String pType = ((ProcessParameter) o).getParameterType();
					if (pType != null)
						for (Iterator j=((List) effectValue).iterator(); j.hasNext();)
							if (!ManagedIndividual.checkMembership(pType, j.next()))
								return false;
					if (((List) effectValue).size() == 1)
						effectValue = ((List) effectValue).get(0);
					context.put(((ProcessParameter) o).getURI(), effectValue);
					return true;
				} else {
					if (!(o instanceof List)) {
						List aux = new ArrayList(1);
						aux.add(o);
						o = aux;
					}
					return o.equals(effectValue);
				}
			}
		}
		return false;
	}
}
