/*******************************************************************************
 * Copyright 2018 2011 Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.middleware.serialization.json.analyzers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.universAAL.middleware.rdf.Resource;

/**
 * @author amedrano
 * 
 */
public class SerializationTypeAnalysis implements TripleAnalyzer {

	public static int REF_TYPE_FULL = 1;
	public static int REF_TYPE_OPT = 2;
	public static int REF_TYPE_RED = 4;
	public static int REF_TYPE_ALL = REF_TYPE_FULL | REF_TYPE_OPT
			| REF_TYPE_RED;

	private class SerialziationTypeRefs {
		int full = 0;
		int optional = 0;
		int reduced = 0;
		int total = 0;
	}

	Map<Object, SerialziationTypeRefs> serTypes = new HashMap<Object, SerialziationTypeRefs>();

	public void analyseRoot(Resource root) {
		// do nothing.
	}

	public void analyseTriple(Resource r, String p, Object obj) {
		// TODO: possible bug: using the same Primitive object in resources,
		// they should not be considered as the same node
		int serializationType = r.getPropSerializationType(p);
		if (!serTypes.containsKey(obj)) {
			serTypes.put(obj, new SerialziationTypeRefs());
		}
		if (serializationType == Resource.PROP_SERIALIZATION_FULL) {
			serTypes.get(obj).full++;
		}
		if (serializationType == Resource.PROP_SERIALIZATION_OPTIONAL) {
			serTypes.get(obj).optional++;
		}
		if (serializationType == Resource.PROP_SERIALIZATION_REDUCED) {
			serTypes.get(obj).reduced++;
		}
		serTypes.get(obj).total++;
	}

	public boolean isSerialized(Resource subject, String predicate) {
		return isSerialized(subject, predicate, subject.getProperty(predicate));
	}

	public boolean isSerialized(Resource subject, String predicate,
			Object object) {
		// return serTypes.get(object).full > 0
		// || serTypes.get(object).reduced > 0
		// || (serTypes.get(object).full == 0
		// && serTypes.get(object).reduced == 0 && serTypes
		// .get(subject).full > 0);
		// true if not true optional (marked as optional, and parent is reduced)
		return !(serTypes.get(object).full == 0
				&& serTypes.get(object).reduced == 0
				&& serTypes.get(object).optional > 0
				&& serTypes.get(subject).full == 0 && serTypes.get(subject).reduced > 0);
	}

	/**
	 * Get the number of references of the given type; multiple types can be
	 * added by bitwise ORing.
	 * 
	 * @param r
	 * @param ref_types
	 *            {@link SerializationTypeAnalysis#REF_TYPE_FULL},
	 *            {@link SerializationTypeAnalysis#REF_TYPE_OPT},
	 *            {@link SerializationTypeAnalysis#REF_TYPE_RED} as ORed switch.
	 */
	public int countRefs(Object r, int ref_types) {
		int refs = 0;
		SerialziationTypeRefs strs = serTypes.get(r);
		if ((ref_types & REF_TYPE_ALL) > 0) {
			return strs.total;
		}
		if ((ref_types & REF_TYPE_FULL) > 0) {
			refs += strs.full;
		}
		if ((ref_types & REF_TYPE_OPT) > 0) {
			refs += strs.optional;
		}
		if ((ref_types & REF_TYPE_RED) > 0) {
			refs += strs.reduced;
		}
		return refs;
	}

	/**
	 * Check if the graph is a tree. For a tree graph, all Resource nodes need
	 * to have 1 reference.
	 * 
	 * @return
	 */
	public boolean isTree() {
		for (Entry<Object, SerialziationTypeRefs> ent : serTypes.entrySet()) {
			SerialziationTypeRefs rs = ent.getValue();
			if (ent.getKey() instanceof Resource && rs.total > 1) {
				return false;
			}
		}
		return true;
	}
}
