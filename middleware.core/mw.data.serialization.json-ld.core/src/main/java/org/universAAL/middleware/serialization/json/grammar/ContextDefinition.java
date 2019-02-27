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

import java.util.Map.Entry;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author amedrano
 * @see <a href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions>https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions</a>
 */
public class ContextDefinition implements JSONLDValidator, KeyControl<String> {
	private JsonObject jsonToValidate = null;
	private static final String BLANK_NODE ="_:";
	
	public JsonObject getJsonToValidate() {
		return jsonToValidate;
	}


	//A context definition MUST be a JSON object whose keys MUST either be terms, compact IRIs, absolute IRIs, or the keywords @language, @base, and @vocab.
	public ContextDefinition(JsonObject jsonObjectOrReference) {

//		if (jsonObjectOrReference.isJsonObject()) {
//			this.jsonToValidate = jsonObjectOrReference.getAsJsonObject();
//		}
//		
//		if (jsonObjectOrReference.isJsonPrimitive()) {
//			jsonObjectOrReference.getAsString();
//			// TODO read Context from reference.openStream()
//		}
		this.jsonToValidate = jsonObjectOrReference;
	}


 
	

	public boolean keyControl(String element) {
		//keys MUST either be terms, compact IRIs, absolute IRIs
		//for (Entry<String, JsonElement> element : this.jsonToValidate.entrySet()) {
			
			if(Term.isTerm(element)) {
				
				//keywords @language, @base, and @vocab.
				//If the context definition has an @language key, its value MUST have the lexical form described in [BCP47] or be null.
				if(element.equals(JsonLdKeyword.LANG)){
					return element.equals("null");
				}
				//If the context definition has an @base key, its value MUST be an absolute IRI, a relative IRI, or null.
				if(element.equals(JsonLdKeyword.BASE)){
					return IRI.isAbsolute(element) || /*IRI.isRelative(null, element.getValue().toString()) ||*/ element.equals("null");
				}
				//If the context definition has an @vocab key, its value MUST be a absolute IRI, a compact IRI, a blank node identifier, a term, or null.
				if(element.equals(JsonLdKeyword.VOCAB)){
					return IRI.isAbsolute(element.toString()) || 
							IRI.isCompact(this, element)  ||
							Term.isTerm(element.toString()) || 
							element.equals("null") ||
							element.equals(this.BLANK_NODE);
				}
				return true;
			}else {
					return IRI.isAbsolute(element) || IRI.isCompact(this, element);
					
				}

	}


	public boolean validate() {
		boolean state = false;;
		for (Entry<String, JsonElement> element : this.jsonToValidate.entrySet()) {
			state = this.keyControl(element.getValue().getAsString());
			//The value of keys that are not keywords MUST be either an absolute IRI, a compact IRI, a term, a blank node identifier, a keyword, null, or an expanded term definition.
			if(! JsonLdKeyword.isKeyword(element.getValue().getAsString())) {
				return IRI.isAbsolute(element.getValue().getAsString()) ||
						IRI.isCompact(this,element.getValue().getAsString()) ||
						element.getValue().getAsString().equals(this.BLANK_NODE) ||
						new ExpandedTermDefinition(this,element.getValue()).validate(); 
			}
		}

		return state;
	}


	

	

}
