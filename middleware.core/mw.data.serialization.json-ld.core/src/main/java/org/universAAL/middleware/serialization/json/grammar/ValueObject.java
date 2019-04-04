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


import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
/**
 * @author amedrano
 * @see <a href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#value-objects>https://www.w3.org/TR/2014/REC-json-ld-20140116/#value-objects</a>
 */
public class ValueObject implements  JSONLDValidator{
	private  JsonElement candidate=null;
	private ContextDefinition activeContext=null;
	
	public ValueObject(ContextDefinition activeContext,JsonElement candidate) {	
		this.candidate = candidate;
		this.activeContext = activeContext;
	}
	/*
	A value object is used to explicitly associate a type or a language with a value to create a typed value or a language-tagged string.	
	A value object MUST be a JSON object containing the @value key. 
	It MAY also contain an @type, an @language, an @index
 	or an @context key but MUST NOT contain both an @type and an @language key at the same time.
 	A value object MUST NOT contain any other keys that expand to an absolute IRI or keyword.
	*/
	public boolean validate() {
		Set<Entry<String, JsonElement>> aux = this.candidate.getAsJsonObject().entrySet();
			if(this.candidate !=null && this.candidate.isJsonObject()) {
				if(aux.contains(JsonLdKeyword.VALUE.toString())){
					if(aux.contains(JsonLdKeyword.LANG.toString()) && aux.contains(JsonLdKeyword.INDEX.toString()) ) {
						return false;
					}else {
						for (Entry<String, JsonElement> entry : aux) {
							//The value associated with the @value key MUST be either a string, a number, true, false or null.
							if( entry.getKey().equals(JsonLdKeyword.VALUE.toString() )) {
								if( !(entry.getValue().isJsonPrimitive() || entry.getValue().isJsonNull())   ) return false;
							}
							//The value associated with the @type key MUST be a term, a compact IRI, an absolute IRI, a relative IRI, or null.
							if(entry.getKey().equals(JsonLdKeyword.TYPE.toString())) {
								if( !(IRI.isCompact(this.activeContext, entry.getValue().getAsString())) ||
										IRI.isAbsolute(entry.getValue().getAsString()) ||
										entry.getValue().isJsonNull() ||
										IRI.isRelative("",entry.getValue().getAsString())
										) return false;
							}
							//The value associated with the @language key MUST have the lexical form described in [BCP47], or be null.
							if(entry.getKey().equals(JsonLdKeyword.LANG.toString())) {
								if( !(entry.getValue().isJsonPrimitive() || entry.getValue().isJsonNull() )   ) return false;
							}
							//The value associated with the @index key MUST be a string.
							if(entry.getKey().equals(JsonLdKeyword.INDEX.toString())) {
								if( !(entry.getValue().isJsonPrimitive())   ) return false;
							}
						}	
						
					}

			}else {
				//TODO log null reference error and ...
				return false;
				}
			}
		return true;
	}	
	


}
