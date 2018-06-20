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
package org.universAAL.middleware.serialization.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.GraphIterator;
import org.universAAL.middleware.util.GraphIteratorElement;

/**
 * @author amedrano
 * 
 */
public class SerializationTypeAnalysis {
	private class SerialziationTypeRefs {
		int full = 0;
		int optional = 0;
		int reduced = 0;
	}

	Map<Object, SerialziationTypeRefs> serTypes = new HashMap<Object, SerialziationTypeRefs>();

	/**
	 * @param root
	 */
	public SerializationTypeAnalysis(Resource root) {
		analyze(root);
	}

	public void analyze(Resource root) {
		// process Serialization Types
		Iterator<GraphIteratorElement> it = GraphIterator.getIterator(root);
		while (it.hasNext()) {
			GraphIteratorElement e = it.next();

			Object obj = e.getObject();

			int serializationType = e.getSubject().getPropSerializationType(
					e.getPredicate());
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
		}
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
}
