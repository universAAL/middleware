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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.GraphIterator;
import org.universAAL.middleware.util.GraphIteratorElement;

/**
 * @author amedrano
 * 
 */
public class GraphAnalyzer {

	private List<TripleAnalyzer> analyzers = new ArrayList<TripleAnalyzer>();

	public boolean addAnalyzer(TripleAnalyzer analyzer) {
		return analyzers.add(analyzer);
	}

	public void analyze(Resource root) {
		// TODO: don't process Resources marked as literals
		for (TripleAnalyzer a : analyzers) {
			a.analyseRoot(root);
		}
		Iterator<GraphIteratorElement> it = GraphIterator.getIterator(root);
		while (it.hasNext()) {
			GraphIteratorElement e = it.next();
			for (TripleAnalyzer a : analyzers) {
				a.analyseTriple(e.getSubject(), e.getPredicate(), e.getObject());
			}

		}
	}

}
