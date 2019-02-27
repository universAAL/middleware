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

import java.net.URL;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
		try {
			URL url = new URL(candidateIRI);
		} catch (Exception e) {
			LogUtils.logDebug(JSONLDSerialization.owner, IRI.class, "validate", e.toString());
			return false;
			
		}
		return true;
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
	
	/**
	 * 
	 * https://www.w3.org/TR/2014/REC-json-ld-20140116/#dfn-compact-iri
	 * 
	 * 
	 * A compact IRI is a way of expressing an IRI using a prefix and suffix separated by a colon (:).
	 *  The prefix is a term taken from the active context and is a short string identifying a particular IRI in a JSON-LD document. For example, the prefix foaf may be used as a short hand for the Friend-of-a-Friend vocabulary, which is identified using the IRI http://xmlns.com/foaf/0.1/. A developer may append any of the FOAF vocabulary terms to the end of the prefix to specify a short-hand version of the absolute IRI for the vocabulary term.
	 *  For example, foaf:name would be expanded to the IRI http://xmlns.com/foaf/0.1/name.
	 * @param activeContext
	 * @param candidate
	 * @return
	 */
	public static boolean isCompact(ContextDefinition activeContext, Entry<String, JsonElement> candidate) {
		if(!candidate.getValue().getAsString().contains(":")) return false;
		/*
		boolean flag;
		for (Entry<String, JsonElement> iterable_element : activeContext.getJsonToValidate().entrySet() ) {
			if(candidate.getKey().toString().contains(iterable_element.getKey().toString())) {
				return true;
			}
		}
		return false;
	}
	
	*/
		return activeContext.getJsonToValidate().entrySet().stream().map(h->h.getKey().toString()).collect(Collectors.toList()).contains(candidate.getKey().substring(0, candidate.getKey().indexOf(":")));
	}
	
	public static boolean isCompact(ContextDefinition activeContext, String candidate) {
		if(!candidate.contains(":")) return false;
		 return activeContext.getJsonToValidate().entrySet().stream().map(h->h.getKey().toString()).collect(Collectors.toList()).contains(candidate.substring(0, candidate.indexOf(":")));
	}
	
	


}
