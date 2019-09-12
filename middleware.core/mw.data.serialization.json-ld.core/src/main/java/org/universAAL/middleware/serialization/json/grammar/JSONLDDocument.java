
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
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
	private ContextDefinition mainContext=null;
	private JsonElement mainJSON = null;
	private String mainJsonString;
	private JsonParser jp = null;
	

	/**	
	 * 
	 * @param jsonToBeProcessed {@link InputStream} of json to be analyzed
	 * @throws JsonParseException
	 * @throws JsonSyntaxException
	 */
	public JSONLDDocument(InputStream jsonToBeProcessed) throws JsonParseException, JsonSyntaxException,ClassCastException,NullPointerException,JsonSyntaxException{
		//maybe its necesary to use jsonld library to expand the json to process it 

		
		String jsonString = "";
		Scanner s = new Scanner(jsonToBeProcessed);
		s.useDelimiter("\\A");
		jsonString = s.hasNext() ? s.next() : "";
		s.close();
		this.mainJsonString=jsonString;
		this.jp = new JsonParser();
		//throws MalformedJsonException if the json is mal formed
		try {
			this.mainJSON = jp.parse(this.mainJsonString);
		}catch (JsonSyntaxException e) {
			e.printStackTrace();
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
		NodeObject nodeObject=null;
		//datamodel especification	https://www.w3.org/TR/2014/REC-json-ld-20140116/#data-model
		/*
		A JSON-LD document serializes a generalized RDF Dataset [RDF11-CONCEPTS], 
		which is a collection of graphs that comprises exactly one default graph and zero or more named graphs.
		A JSON-LD document MUST be a single node object or an array whose elements are each node objects at the top level.
		*/
		//add the default graph as resource 
		if(this.mainJSON instanceof JsonObject) {
			if(this.mainJSON.getAsJsonObject().has(JsonLdKeyword.CONTEXT.toString())) {
				this.mainContext = new ContextDefinition(this.mainJSON.getAsJsonObject().remove(JsonLdKeyword.CONTEXT.toString()));
				if(!this.mainContext.validate())
					return false;
				for (Entry<String, JsonElement> element : this.mainJSON.getAsJsonObject().entrySet()) {
					
					if(element.getKey().equals(JsonLdKeyword.CONTEXT.toString())) {
						//merge contexts
					}
					
					if(element.getKey().startsWith("@")) {
						if(!JsonLdKeyword.isKeyword(element.getKey())) 
							return false;
					}else{
						if(element.getKey().isEmpty())
							return false;
					}
					
					if(element.getValue() instanceof JsonObject) {
						nodeObject = new NodeObject(this.mainContext, element.getValue());
						if(!nodeObject.validate()) return false;
					}

				}
			}else
				return false;
		}else {
			//Json array of expanded JsonLD
			for (JsonElement item: this.mainJSON.getAsJsonArray()) {
				if(item instanceof JsonPrimitive) {
					
				}
				
				if(item instanceof JsonObject) {
					for (Entry<String, JsonElement> element : item.getAsJsonObject().entrySet()) {
						if(!analyze(element))
							return false;
					}
				}
				
				if(item instanceof JsonArray) {
					
				}
			}
		} 
		return true;
	}
	
	private boolean analyze(Entry<String, JsonElement> candidate) {
		if(candidate.getKey().equals(JsonLdKeyword.CONTEXT.toString())) {
			//merge contexts
		}
		
		if(candidate.getKey().startsWith("@")) {
			if(!JsonLdKeyword.isKeyword(candidate.getKey())) 
				return false;
		}else{
			if(candidate.getKey().isEmpty())
				return false;
		}
		
		if(candidate.getValue() instanceof JsonObject) {
			NodeObject nodeObject = new NodeObject(this.mainContext, candidate.getValue());
			if(!nodeObject.validate()) return false;
		}
		return true;
	}
	/**
	 * Return {@link JsonElement} main Json 
	 * 
	 */
	public JsonElement getMainJSON() {
		return mainJSON;
	}

	/**
	 * Return {@link String} main Json 
	 * 
	 */
	public String getFullJsonAsString() {
		return this.mainJsonString;
	}
	
	/**
	 * Return a Java representation of JsonLD Context into given Json
	 * @return {@link ContextDefinition} 
	 */
	public ContextDefinition getActiveContext() {
		return this.mainContext;
	}


}
