
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.SortingFocusTraversalPolicy;

import java.util.Scanner;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Class to represent a entire Json document
 *
 * @author amedrano
 * @see <a
 *      href=https://www.w3.org/TR/2014/REC-json-ld-20140116/#json-ld-grammar>https://www.w3.org/TR/2014/REC-json-ld-20140116/#json-ld-grammar</a>
 */
public class JSONLDDocument implements JSONLDValidator {
	private ArrayList<ContextDefinition> contexts = new ArrayList<ContextDefinition>(2) ; 
	private ContextDefinition activeContext=null, lastContext= null;
	private JsonObject mainJSON = null;
	

	private JsonParser jp = null;

	/**	
	 * 
	 * @param jsonToBeProcessed {@link InputStream} of json to be analyzed
	 * @throws JsonParseException
	 * @throws JsonSyntaxException
	 */
	public JSONLDDocument(InputStream jsonToBeProcessed) throws JsonParseException, JsonSyntaxException,ClassCastException,NullPointerException,JsonSyntaxException{

		String jsonString = "";
		Scanner s = new Scanner(jsonToBeProcessed);
		s.useDelimiter("\\A");
		jsonString = s.hasNext() ? s.next() : "";
		s.close();
		this.jp = new JsonParser();
		//throws MalformedJsonException if the json is mal formed
		try {
			
			this.mainJSON = (JsonObject) jp.parse(jsonString);
		}catch (JsonSyntaxException e) {
			throw e;
		}
		

	}

	/**
	 * Method to start the Json validation process. A JSON-LD document must be a
	 * valid JSON document as described in [RFC4627]. A JSON-LD document must be a
	 * single node object or an array whose elements are each node objects at the
	 * top level.
	 * @return {@link Boolean} value indicating the status of the process
	 */
	public boolean validate() {

		for (Entry<String, JsonElement> item : this.mainJSON.entrySet()) {
			
			
			//analyze only elements with context key
			if (item.getKey().equals(JsonLdKeyword.CONTEXT.toString())) {
				
					//simple context
					if(item.getValue().isJsonObject()) {
						this.activeContext = new ContextDefinition(item.getValue());	
						if (!this.activeContext.validate()) 
							return false;
					}
					//if the value associated to Context key is a json primitive...it must be a valid IRI
					if(item.getValue().isJsonPrimitive()) {
						if (!IRI.isAbsolute(item.getValue().getAsString())){
							return false;
						}
					}
					//multiple contexts
					if(item.getValue().isJsonArray()) {
//						//if the element has a context key and is an array...will be interpreted as an array of contexts
//						for (int i = 0; i < item.getValue().getAsJsonArray().size(); i++) {
//							if(item.getValue().getAsJsonArray().get(i).isJsonObject()) {
//								if (  !(new NodeObject(this.activeContext, item.getValue().getAsJsonArray().get(i)).validate()) ) {
//									return false;
//								}
//							}else {
//								//TODO throw error
//								return false;
//							}			
//						}
					}					
				
	
			}
			else {

				if(item.getValue().isJsonArray()) {

				}
				
				if(item.getValue().isJsonObject()) {
				/*
				1.0 
				A node object represents zero or more properties of a node in the graph serialized by the JSON-LD document. 
				A JSON object is a node object if it exists outside of a JSON-LD context and:
				it does not contain the @value, @list, or @set keywords, and
				it is not the top-most JSON object in the JSON-LD document consisting of no other members than @graph and @context.
				*/
					for (Entry<String, JsonElement> element : item.getValue().getAsJsonObject().entrySet()) {
						if(
							element.getKey().equals(JsonLdKeyword.VALUE) ||
							element.getKey().equals(JsonLdKeyword.LIST) ||
							element.getKey().equals(JsonLdKeyword.SET)
							) {
							return false;
						}
					} 
					if(item.getKey().equals(JsonLdKeyword.GRAPH)) {

						//walk over json and build Resource  object
						
//						if(! new NodeObject(activeContext, item.getValue()).validate() ) {
//							return false;
//						}
					}else
						return false;
					
				}				
				if(item.getValue().isJsonPrimitive()) {
					//validate prmitive as IRI...
					
				}

				if(item.getValue().isJsonNull()) {
					return false;
				}
				

				
			}

			
		}

		return true;
	}
	public JsonObject getMainJSON() {
		return mainJSON;
	}
	
}