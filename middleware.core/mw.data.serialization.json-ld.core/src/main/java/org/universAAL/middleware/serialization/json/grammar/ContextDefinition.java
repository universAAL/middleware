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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author amedrano
 * @see <a href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions>https://www.w3.org/TR/2014/REC-json-ld-20140116/#context-definitions</a>
 */
public class ContextDefinition implements JSONLDValidator, KeyControl<Entry<String, JsonElement> > {
	private JsonObject jsonToValidate = null;
	private JsonArray contextToMerge = null;
	private JsonParser parser = new JsonParser();
	private static final String BLANK_NODE ="_:";
	private boolean state = false;
	private ContextDefinition lastContext=null;

/**
 * 
 * @param {@link JsonElement} jsonObjectOrReference  to be analyzed
 */
	//A context definition MUST be a JSON object whose keys MUST either be terms, compact IRIs, absolute IRIs, or the keywords @language, @base, and @vocab.

	public ContextDefinition (JsonObject context) {
			this.jsonToValidate = context;
	}

	
/**
 * Receive a String as {@link InputStream}
 * @param context
 */
	public ContextDefinition (InputStream context) {
		String jsonString = "";
		Scanner s = new Scanner(context);
		s.useDelimiter("\\A");
		jsonString = s.hasNext() ? s.next() : "";
		s.close();
		JsonParser jsp = new JsonParser();
		this.jsonToValidate = jsp.parse(jsonString).getAsJsonObject();
	}
/**
 * Receive a {@link URL} to read context from URL
 * @param cotextURL
 */
	public ContextDefinition(URL cotextURL) {
		try {
			URLConnection request = cotextURL.openConnection();
		    request.connect();
		    JsonParser jp = new JsonParser(); //from gson
		    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); 
		    if(root instanceof JsonObject)
		    	this.jsonToValidate = root.getAsJsonObject().remove("@context").getAsJsonObject();
		} catch (Exception e) {
			LogUtils.logDebug(JSONLDSerialization.owner, ContextDefinition.class, "ContextDefinition constructor", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
/**
 * method to validate the JsonElement given in {@link ContextDefinition} constructor.
 * Return <code>true</code> if the context is valid, otherwise return <code>false</code>
 */
	public boolean validate() {
		
		if(this.contextToMerge!=null) {
			
			LogUtils.logDebug(JSONLDSerialization.owner, ContextDefinition.class, "keyControl", "merging contexts");

			this.jsonToValidate = mergeContexts(this.contextToMerge);
		}
		 
		if(this.jsonToValidate!=null) {
					for (Entry<String, JsonElement> element : this.jsonToValidate.entrySet()) {
						//keyword control
						//A context definition MUST be a JSON object whose keys MUST either be terms, compact IRIs, absolute IRIs, or the keywords @language, @base, and @vocab.
						if( !this.keyControl(element) ) return false;
						if( !this.valueOfKeyControl(element) ) return false;
					}	
					
				

		}else {
			//TODO use logging system 
			LogUtils.logDebug(JSONLDSerialization.owner, ContextDefinition.class, "keyControl", "null json to validate...");
			return false;
		}
		return true;
	}


	public boolean valueOfKeyControl(Entry<String, JsonElement> itemToControl) {
		
			if(JsonLdKeyword.isKeyword(itemToControl.getValue().toString())) {
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
				//The value of keys that are NOT keywords MUST be either an absolute IRI, a compact IRI, a term, a blank node identifier, a keyword, null, or an expanded term definition.
					if(!IRI.isAbsolute(itemToControl.getValue().toString())) {
						if(!IRI.isCompact(this,itemToControl.getValue().toString())) {
							if(!JsonLdKeyword.isKeyword(itemToControl.getValue().toString())) {
								if(!itemToControl.getValue().toString().equals(JsonLdKeyword.BLANK_NODE.toString())) {
									if(!itemToControl.getValue().isJsonNull()) {
										if( itemToControl.getValue().isJsonObject()) {
											return new ExpandedTermDefinition(this, itemToControl.getValue().getAsJsonObject()).validate();
										}else {
											if(itemToControl.getValue().toString().startsWith("@")) {
												return false;
											}
										}
									}
								}
							}
						}
					}
					//return IRI.isAbsolute(itemToControl.getValue().toString()) || IRI.isCompact(this,itemToControl.getValue().toString());
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
		if( ! IRI.isAbsolute(element.getKey().toString())) {
			if(! IRI.isCompact(this, element.getKey().toString())) {
				if(element.getKey().toString().startsWith("@")){
					if( !(element.getKey().toString().equals(JsonLdKeyword.BASE) || element.getKey().toString().equals(JsonLdKeyword.LANG) || element.getKey().toString().equals(JsonLdKeyword.VOCAB)) ) {
						
						LogUtils.logDebug(JSONLDSerialization.owner, ContextDefinition.class, "keyControl", "error, term must not start with @ if it isn't a keyword. Given="+element.getKey().toString());

						return false;
					}
				}else {
					return true;
				}
			}
		}
		return true;
	}
	/**
	 * method to merge contexts grouped in array
	 * @param ToMerge
	 * @return {@link JsonObject} representing the merged context
	 */
	public static  JsonObject mergeContexts(JsonArray ToMerge) {
		JsonObject obj = new JsonObject();
		if(ToMerge ==null) return null;
		for (JsonElement item : ToMerge) {
			if(item.isJsonObject()) {
				for (Entry<String, JsonElement> jsonElement : item.getAsJsonObject().entrySet()) {
					obj.add(jsonElement.getKey(),jsonElement.getValue());
				}
			}else
				return null;
		}

		return obj;
	}

	/**
	 * 
	 * @return the base IRI defined in this context
	 */
	public String getBaseIRI() {
		return this.jsonToValidate.get(JsonLdKeyword.BASE.toString()).toString();
	}

	/**
	 * to check if this term is defined into the context
	 * @param term
	 * @return true if the context contains this term
	 */
	public boolean hasTerm(String term) {
		return this.jsonToValidate.has(term);
	}
	
	public boolean hasTerm(JsonElement term) {
		String aux = term.getAsJsonPrimitive().getAsString();
	 return this.jsonToValidate.has(aux);
	}

	/**
	 * method to get the value associates to given term
	 * @param term
	 * @return the value of term, or null if the term not exists
	 */
	public JsonElement getTerm(String term) {
		return this.jsonToValidate.get(term);
	}
	

	public void updateElement(String key, JsonElement value) {
		this.jsonToValidate.add(key, value);
	}

	public JsonElement getTermValue(String term) {
		
		return this.jsonToValidate.get(term);
	} 
	
	public JsonElement getTermValue(JsonElement term) {
		if(term.isJsonPrimitive()) {
			return this.jsonToValidate.get(term.getAsJsonPrimitive().getAsString());	
		}
		return null;
	} 
	public JsonObject getJsonToValidate() {
		return jsonToValidate;
	}

	

}
