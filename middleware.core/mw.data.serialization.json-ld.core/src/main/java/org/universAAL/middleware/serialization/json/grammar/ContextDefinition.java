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
public class ContextDefinition implements JSONLDValidator, KeyControl<Entry<String, JsonElement> > {
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
		System.out.println("loading from ContextDefinition class->"+jsonObjectOrReference);
	}


 
	

	


	public boolean validate() {
		boolean state = true;

		for (Entry<String, JsonElement> element : this.jsonToValidate.entrySet()) {
			state = this.keyControl(element);
			//The value of keys that are not keywords MUST be either an absolute IRI, a compact IRI, a term, a blank node identifier, a keyword, null, or an expanded term definition.
			if(element.getValue().isJsonPrimitive()) {
				if(! JsonLdKeyword.isKeyword(element.getValue().getAsString())) {
					state = IRI.isAbsolute(element.getValue().getAsString()) ||
							IRI.isCompact(this,element.getValue().getAsString()) ||
							element.getValue().getAsString().equals(this.BLANK_NODE) ||
							new ExpandedTermDefinition(this,element.getValue()).validate();	
				}
				
			}else {
				if( element.getValue().isJsonObject()) {
					state = new ExpandedTermDefinition(this, element.getValue()).validate();
				}
			}
		
		}

		return state;
	}


	public boolean keyControl(Entry<String, JsonElement> itemToControl) {
		//keys MUST either be terms, compact IRIs, absolute IRIs
		//for (Entry<String, JsonElement> element : this.jsonToValidate.entrySet()) {
		
			if(Term.isTerm(itemToControl.getKey())) {
				//keywords @language, @base, and @vocab.
				//If the context definition has an @language key, its value MUST have the lexical form described in [BCP47] or be null.
				if(itemToControl.getKey().equals(JsonLdKeyword.LANG)){
					return itemToControl.getValue().isJsonPrimitive();
				}
				//If the context definition has an @base key, its value MUST be an absolute IRI, a relative IRI, or null.
				if(itemToControl.getKey().equals(JsonLdKeyword.BASE)){
					return IRI.isAbsolute(itemToControl.getValue().getAsString()) || /*IRI.isRelative(null, element.getValue().toString()) ||*/ itemToControl.getValue().equals("null");
				}
				//If the context definition has an @vocab key, its value MUST be a absolute IRI, a compact IRI, a blank node identifier, a term, or null.
				if(itemToControl.getKey().equals(JsonLdKeyword.VOCAB)){
					return IRI.isAbsolute(itemToControl.getValue().toString()) || 
							IRI.isCompact(this, itemToControl.getValue().getAsString())  ||
							Term.isTerm(itemToControl.getValue().toString()) || 
							itemToControl.getValue().equals("null") ||
							itemToControl.getValue().equals(this.BLANK_NODE);
				}
				return true;
			}else {
				System.out.println("not a term");
					return IRI.isAbsolute(itemToControl.getValue().getAsString()) || IRI.isCompact(this,itemToControl.getValue().getAsString());
					
				}

	}


	

	

}
