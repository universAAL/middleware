/*******************************************************************************
 * Copyright 2018 Universidad Politécnica de Madrid UPM
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

import org.universAAL.middleware.serialization.json.JsonLdKeyword;

/**
 * @author amedrano
 * @see <a href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#terms>https://www.w3.org/TR/2014/REC-json-ld-20140116/#terms</a>
 *
 */
public class Term {

	/**
	 *
	 */
	public Term(String term) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Test if a {@link String} complies with Term restrictions.
	 * @param candidate
	 * @return true if the term is valid, false if it is not valid
	 * @see <a href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#terms>https://www.w3.org/TR/2014/REC-json-ld-20140116/#terms</a>
	 */
	static public boolean isTerm(String candidate) {
		System.out.println("JsonLdKeyword.isKeyword(candidate)  "+JsonLdKeyword.isKeyword(candidate) );
		System.out.println("candidate.startsWith(\"@\") "+candidate.startsWith("@"));
	
		return !(candidate.startsWith("@"));
	}
	
	/**	
	 * test if the full tem (key and value) is valid
	 * @param candidateKey key to be checked
	 * @param candidateIRI value to check if is a valid URI
	 * @return true if the term is valid, false if it is not valid
	 */
	static public boolean isTerm(String candidateKey,String candidateURI) {
		return ! ((JsonLdKeyword.isKeyword(candidateKey) || candidateKey.startsWith("@")) && IRI.isAbsolute(candidateURI));
	}
 
	//TODO add expand method with active context
}
