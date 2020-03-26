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
 * An expanded term definition is used to describe the mapping between a term
 * and its expanded identifier, as well as other properties of the value
 * associated with the term when it is used as key in a node object.
 *
 * @author amedrano
 * @see <ahref=https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions>https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions</a>
 */
public class ExpandedTermDefinition implements JSONLDValidator {
	private JsonObject jsonToValidate;
	private ContextDefinition ctx;
	private static final String BLANK_NODE ="_:";

	/**
	 * 
	 * @param ctx Actual context  
	 * @param jsonToValidate {@link JsonElement} to validate
	 */
	public ExpandedTermDefinition(ContextDefinition ctx,JsonObject jsonToValidate) {
		//System.out.println("expanded term definition given "+jsonToValidate.toString());
		this.jsonToValidate=jsonToValidate;
		this.ctx=ctx;
	}

	public boolean validate() {
		//An expanded term definition MUST be a JSON object composed of zero or more keys from @id, @reverse, @type, @language or @container.
		//An expanded term definition SHOULD NOT contain any other keys
				boolean hasID=false,hasContainer=false,generalState ;
				for (Entry<String, JsonElement> element : this.jsonToValidate.getAsJsonObject().entrySet()) {
					if(JsonLdKeyword.isKeyword(element.getKey())) {
						if(!IRI.isAbsolute(element.getValue().getAsJsonPrimitive().getAsString())) {
							if(!IRI.isCompact(ctx,element.getValue().getAsJsonPrimitive().getAsString())) {
								return false;
							}
						}
					}
					
//					if(IRI.isAbsolute(element.getValue().getAsJsonPrimitive().getAsString())) return false;
//					if(IRI.isCompact(ctx,element.getValue().getAsJsonPrimitive().getAsString())) return false;
				}	
			
			return true;
	}



}
