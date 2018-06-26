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

import org.universAAL.middleware.rdf.Resource;

/**
 * Counts and manages blank nodes.
 * 
 * @author amedrano
 * 
 */
public class BlankNodeAnalyzer implements TripleAnalyzer {

	Map<Resource, Integer> blankNodes = new HashMap<Resource, Integer>();

	/** {@inheritDoc} */
	public void analyseTriple(Resource r, String predicate, Object object) {
		if (object instanceof Resource) {
			process((Resource) object);
		}
	}

	/** {@inheritDoc} */
	public void analyseRoot(Resource root) {
		// check if root is anonymous
		process(root);
	}

	/**
	 * @param root
	 */
	private void process(Resource r) {
		if (r.isAnon() && !blankNodes.containsKey(r)) {
			blankNodes.put(r, Integer.valueOf(blankNodes.size()));
		}
	}

	public String getSerializedURI(Resource r) {
		int padding = 1 + log10(blankNodes.size() - 1);
		String format = String.format("%%0%dd", padding);
		return String.format(format, blankNodes.get(r));
	}

	public static int log10(int i) {
		return (int) (Math.log(i) / Math.log(10));
	}
}
