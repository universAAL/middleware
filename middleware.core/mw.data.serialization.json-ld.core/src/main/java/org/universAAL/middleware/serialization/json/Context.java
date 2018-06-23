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

import java.util.List;

import org.universAAL.middleware.serialization.json.URICompactor.URIPrefix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author amedrano
 * 
 */
public class Context {

	public enum Keyword {
		ID("@id"), VALUE("@value"), LANG("@language"), TYPE("@type"), CONTAINER(
				"@container"), LIST("@list"), SET("@set"), REVERSE("@reverse"), INDEX(
				"@index"), BASE("@base"), VOCAB("@vocab"), GRAPH("@graph"), ;
		private final String text;

		/**
		 * @param text
		 */
		Keyword(final String text) {
			this.text = text;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return text;
		}

		static public boolean isKeyword(String challenge) {
			try {
				Keyword.valueOf(challenge);
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		}
	}

	private JsonObject representation;

	/**
	 * 
	 */
	public Context() {
		representation = new JsonObject();
	}

	public Context(URICompactor prefixes) {
		this();
		List<URIPrefix> pxs = prefixes.getPrefixes();
		for (URIPrefix uriPrefix : pxs) {
			addTerm(uriPrefix.getCompactedPrefix(), uriPrefix.fullPrefix);
		}
	}

	public String expand(String compactedURI) {
		// XXX check if there can be more than one colon in a compacted URI
		int delim = compactedURI.lastIndexOf(':');
		if (delim < 0) {
			// no pregix, compactedURI is already expanded
			return compactedURI;
		} else {
			String prefix = compactedURI.substring(0, delim);
			JsonElement term = representation.get(prefix);
			if (term.isJsonObject()) {
				return ((JsonObject) term).get(Keyword.ID.toString())
						.getAsString() + compactedURI.substring(delim + 1);
			} else if (term.isJsonPrimitive()) {
				return term.getAsString() + compactedURI.substring(delim + 1);
			}
			// TODO log warn the possible error, the term is not a JSONObjer nor
			// a string
		}
		return compactedURI;
	}

	public void addTerm(String term, String value) {
		representation.addProperty(term, value);
	}
}
