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
	private boolean state = false;
	public JsonObject getJsonToValidate() {
		return jsonToValidate;
	}

/**
 * 
 * @param {@link JsonElement} jsonObjectOrReference  to be analyzed
 */
	//A context definition MUST be a JSON object whose keys MUST either be terms, compact IRIs, absolute IRIs, or the keywords @language, @base, and @vocab.
	public ContextDefinition(JsonElement jsonObjectOrReference) {

		if (jsonObjectOrReference.isJsonObject()) {
			this.jsonToValidate = jsonObjectOrReference.getAsJsonObject();
		}
		
		
		// to read context from website and validate it
		 
		if (jsonObjectOrReference.isJsonPrimitive() &&  !this.state) {
			this.state = true;
			  if (IRI.isAbsolute(jsonObjectOrReference.getAsString())) {
				// TODO read Context from reference.openStream() and validate online context and control how to get the correct flag	  
			  }
			
		}

		
	}


/**
 * method to validate the JsonElement given in {@link ContextDefinition} constructor
 */
	public boolean validate() {
		
		 
		if(this.jsonToValidate!=null) {
			
				if(jsonToValidate.isJsonArray()) {
					//can have 2 types of context: referenced or expanded			
					}
				
				if(jsonToValidate.isJsonObject()) {

					for (Entry<String, JsonElement> element : this.jsonToValidate.getAsJsonObject().entrySet()) {
		
						//keyword control
						//A context definition MUST be a JSON object whose keys MUST either be terms, compact IRIs, absolute IRIs, or the keywords @language, @base, and @vocab.
						return this.keyControl(element);	
					}	
					
				}

		}else {
			//TODO use logging system 
			System.out.println("null json to validate");
			return false;
		}
		return true;
	}


	public boolean valueOfKeyControl(Entry<String, JsonElement> itemToControl) {
		
		//The value of keys that are not keywords MUST be either an absolute IRI, a compact IRI, a term,
		//a blank node identifier, a keyword, null, or an expanded term definition.


			if(Term.isTerm(itemToControl.getValue().getAsString())) {
				//keywords @language, @base, and @vocab.
				//If the context definition has an @language key, its value MUST have the lexical form described in [BCP47] or be null.
				if(itemToControl.getValue().equals(JsonLdKeyword.LANG)){
					return itemToControl.getValue().isJsonPrimitive();
				}
				//If the context definition has an @base key, its value MUST be an absolute IRI, a relative IRI, or null.
				if(itemToControl.getValue().equals(JsonLdKeyword.BASE)){
					return IRI.isAbsolute(itemToControl.getValue().getAsString()) || /*IRI.isRelative(null, element.getValue().toString()) ||*/ itemToControl.getValue().equals("null");
				}
				//If the context definition has an @vocab key, its value MUST be a absolute IRI, a compact IRI, a blank node identifier, a term, or null.
				if(itemToControl.getValue().equals(JsonLdKeyword.VOCAB)){
					return IRI.isAbsolute(itemToControl.getValue().toString()) || 
							IRI.isCompact(this, itemToControl.getValue().getAsString())  ||
							Term.isTerm(itemToControl.getValue().toString()) || 
							itemToControl.getValue().equals("null") ||
							itemToControl.getValue().equals(this.BLANK_NODE);
				}
				
				if(itemToControl.getValue().isJsonObject()) {
					return new ExpandedTermDefinition(this, itemToControl.getValue().getAsJsonObject()).validate();
				}
				
			}else {
					return IRI.isAbsolute(itemToControl.getValue().getAsString()) || IRI.isCompact(this,itemToControl.getValue().getAsString());
					
				}
			return true;
	}
	//TODO a term defined in the active context expanding into an absolute IRI, or an array of any of these.
	public boolean ActiveContextTermControl(JsonElement candidate) {
		for (Entry<String, JsonElement> element : this.jsonToValidate.entrySet()) {
			if(candidate.isJsonPrimitive() || candidate.isJsonArray()) {
				//this.jsonToValidate.has(element.getKey());
			}else {
				//throw error
				return false;
			}
		}
		return true;
	}


	public boolean keyControl(Entry<String, JsonElement> element) {
		
		
		if(!Term.isTerm(element.getKey())) return false;
		
		if(! IRI.isCompact(this, element)) return false;
		
		if(!IRI.isAbsolute(element.getKey())) return false;
	
		//keyword control
		if((JsonLdKeyword.isKeyword(element.getKey()))) {

			
			if(element.getKey().equals(JsonLdKeyword.LANG)){
				return element.getValue().isJsonPrimitive();
			}
			//If the context definition has an @base key, its value MUST be an absolute IRI, a relative IRI, or null.
			if(element.getKey().equals(JsonLdKeyword.BASE)){
				return IRI.isAbsolute(element.getValue().getAsString()) || /*IRI.isRelative(null, element.getValue().toString()) ||*/ element.getValue().equals("null");
			}
			//If the context definition has an @vocab key, its value MUST be a absolute IRI, a compact IRI, a blank node identifier, a term, or null.
			if(element.getKey().equals(JsonLdKeyword.VOCAB)){
				return IRI.isAbsolute(element.getValue().toString()) || 
						IRI.isCompact(this, element.getValue().getAsString())  ||
						Term.isTerm(element.getValue().toString()) || 
						element.getValue().isJsonNull() ||
						element.getValue().equals(this.BLANK_NODE);
			}
			
		}else {
			if(!this.valueOfKeyControl(element)) return false;
		}
		return true;
	}



	


	

	

}
