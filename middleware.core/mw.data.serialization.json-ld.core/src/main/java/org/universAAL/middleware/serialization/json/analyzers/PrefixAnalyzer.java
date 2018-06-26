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

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.serialization.json.URICompactor;

/**
 * @author amedrano
 * 
 */
public class PrefixAnalyzer implements TripleAnalyzer {

	private SerializationTypeAnalysis sta;
	private URICompactor cmpr;

	/**
	 * 
	 */
	public PrefixAnalyzer(SerializationTypeAnalysis sta, URICompactor cmpr) {
		this.sta = sta;
		this.cmpr = cmpr;
	}

	/** {@inheritDoc} */
	public void analyseTriple(Resource r, String p, Object obj) {
		if (sta.isSerialized(r, p, obj)) {
			if (obj instanceof Resource) {
				Resource o = (Resource) obj;
				cmpr.addURI(p);
				cmpr.addURI(o.getURI());
				String[] ots = o.getTypes();
				for (int j = 0; j < ots.length; j++) {
					cmpr.addURI(ots[j]);
				}
			} else {
				cmpr.addURI(p);
				cmpr.addURI(TypeMapper.getDatatypeURI(obj));
			}
		}

	}

	/** {@inheritDoc} */
	public void analyseRoot(Resource root) {
		// process Root's information
		cmpr.addURI(root.getURI());
		String[] ts = root.getTypes();
		for (int i = 0; i < ts.length; i++) {
			cmpr.addURI(ts[i]);
		}

	}
}
