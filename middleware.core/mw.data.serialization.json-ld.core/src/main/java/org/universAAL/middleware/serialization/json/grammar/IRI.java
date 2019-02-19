/*******************************************************************************
 * Copyright 2019 Universidad Politécnica de Madrid UPM
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
package org.universAAL.middleware.serialization.json.grammar;

/**
 * @author amedrano
 *
 */
public class IRI {

	public static final String BLANK_NODE_IDENTIFIER="_:";

	/**
	 * An absolute IRI is defined in [RFC3987] as containing a scheme along with path and optional query and fragment segments.
	 * @param candidateIRI
	 * @return
	 */
	public static boolean isAbsolute(String candidateIRI) {
		//TODO
		return false;
	}

	/**
	 * A relative IRI is an IRI that is relative to some other absolute IRI. In JSON-LD all relative IRIs are resolved relative to the base IRI.
	 * @param candidate
	 * @return
	 */
	public static boolean isRelative(String baseIRI, String candidate) {
		if (baseIRI == null) {
			// TODO
			return false;
		}
		else {
			return isAbsolute(baseIRI+candidate);
		}
	}


}
