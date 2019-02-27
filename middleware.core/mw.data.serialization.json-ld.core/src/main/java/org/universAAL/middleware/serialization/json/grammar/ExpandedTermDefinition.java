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

/**
 * An expanded term definition is used to describe the mapping between a term
 * and its expanded identifier, as well as other properties of the value
 * associated with the term when it is used as key in a node object.
 *
 * @author amedrano
 * @see <ahref=https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions>https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions</a>
 */
public class ExpandedTermDefinition implements JSONLDValidator {
	private JsonElement jsonToValidate;
	private ContextDefinition ctx;
	private static final String BLANK_NODE ="_:";

	/**
	 *
	 */
	public ExpandedTermDefinition(ContextDefinition ctx,JsonElement jsonToValidate) {
		this.jsonToValidate=jsonToValidate;
		this.ctx=ctx;
	}

	@Override
	public boolean validate() {
	//An expanded term definition MUST be a JSON object composed of zero or more keys from @id, @reverse, @type, @language or @container.
	//An expanded term definition SHOULD NOT contain any other keys
		if(this.jsonToValidate.isJsonObject()) {
			boolean hasID=false,hasContainer=false,generalState ;
			for (Entry<String, JsonElement> element : this.jsonToValidate.getAsJsonObject().entrySet()) {
				//If the expanded term definition contains the @id keyword, its value MUST be null, an absolute IRI, a blank node identifier, a compact IRI, a term, or a keyword.
				if (element.getKey().equals(JsonLdKeyword.ID)) {
					hasID=true;
					//falso si todos los casos dan falso
					generalState=element.getValue().getAsString().equalsIgnoreCase("null") ||
								IRI.isAbsolute(element.getValue().getAsString()) ||
								element.getValue().getAsString().equals(this.BLANK_NODE) ||
								IRI.isCompact(this.ctx, element.getValue().getAsString()) ||
								Term.isTerm(element.getValue().getAsString()) || 
								JsonLdKeyword.isKeyword(element.getValue().getAsString());
				}
				/*
				 * If the expanded term definition contains the @container keyword, its value MUST be either @list, @set, @language, @index, or be null.
				 *  If the value is @language, when the term is used outside of the @context, the associated value MUST be a language map.
				 *  If the value is @index, when the term is used outside of the @context, the associated value MUST be an index map.
				 *  
				 * */	
				if (element.getKey().equals(JsonLdKeyword.CONTAINER)) {
					hasContainer = true;
					if(element.getValue().getAsString().equals(JsonLdKeyword.LIST)) {
						//TODO ...
						}
					if(element.getValue().getAsString().equals(JsonLdKeyword.SET)) {
						//TODO ...					
						}
					if(element.getValue().getAsString().equals(JsonLdKeyword.LANG)) {
						//TODO ...					
						}
					if(element.getValue().getAsString().equals(JsonLdKeyword.INDEX)) {
						//TODO ...
						}
					if(element.getValue().getAsString().equalsIgnoreCase("null")) {
						//TODO ...
						}	
				
					} 
					
				//If an expanded term definition has an @reverse member, it MUST NOT have an @id member at the same time. If an @container member exists, its value MUST be null, @set, or @index.
				if (element.getKey().equals(JsonLdKeyword.REVERSE)){
					if(hasID) {
						generalState= false;
					}
					if(hasContainer) {
						generalState= element.getValue().getAsString().equals(JsonLdKeyword.SET) || element.getValue().getAsString().equals(JsonLdKeyword.INDEX) || element.getValue().getAsString().equalsIgnoreCase("nullL");  
					}
					
				}
				//If the expanded term definition contains the @type keyword, its value MUST be an absolute IRI, a compact IRI, a term, null, or the one of the keywords @id or @vocab.
				if (element.getKey().equals(JsonLdKeyword.TYPE)){
					generalState = IRI.isAbsolute(element.getValue().getAsString()) ||
							IRI.isCompact(this.ctx,element.getValue().getAsString()) ||
							Term.isTerm(element.getValue().getAsString()) || 
							element.getValue().getAsString().equalsIgnoreCase("null") ||
							( element.getValue().getAsString().equals(JsonLdKeyword.ID) || element.getValue().getAsString().equals(JsonLdKeyword.VOCAB) );
				} 
				
				//If the expanded term definition contains the @language keyword, its value MUST have the lexical form described in [BCP47] or be null.
				if (element.getKey().equals(JsonLdKeyword.LANG)) {
					generalState = element.getValue().getAsString().equalsIgnoreCase("null") || element.getValue().isJsonPrimitive();
				}
				
				
			}	
		}else {
			return false;
		}
		return false;
	}

}
