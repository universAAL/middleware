
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
import org.universAAL.middleware.rdf.Resource;
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
		ContextDefinition active=null;
		Boolean compacted=false;
		
		Resource subject=null, predicate=null,resource=null;
		if(this.mainJSON.has(JsonLdKeyword.CONTEXT.toString())) {
			compacted=true;
		}
	
		if(compacted) {
			active = new ContextDefinition( this.mainJSON.get(JsonLdKeyword.CONTEXT.toString()));
			if (!active.validate()) 
				return false;
		}else {
			//missing context. expanded JsonLD
			for (Entry<String, JsonElement> item : this.mainJSON.entrySet()) {
					if(item.getValue().isJsonArray()) {
						//TODO control the item key to check if it is valid
						//the key must be a valid URI
						if(!IRI.isAbsolute(item.getKey())) return false; //TODO if URI is relative
						
						
						//may be parse as collection
					}
					if(item.getValue().isJsonObject()) {
						//parse as triple(?)
						//TODO check the jsonLD format
					}
					if(item.getValue().isJsonPrimitive()) {
						//parse as triple(?)
						//TODO check the jsonLD format
					}
			}
		}
		
		for (Entry<String, JsonElement> item : this.mainJSON.entrySet()) {
			
			//if the json is espanded or compacted
			//TODO appear gson just use the last context into the json, so is not needed to merge context. Check documenation
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

					}					
				
				
			}else {
				
				/*
				 * if hasn't context,treat the json in the compacted form?
				 * 
				 * */
				
				
				if(item.getValue().isJsonArray()) {

				}
				
				if(item.getValue().isJsonObject()) {
					/*
					 * step 1: check if this node is a collection 
					 * step 2: check if this is the first object in the document
					 * step 3: add this node as a first resource (initialize it with its IRI and value)
					 * 
					 * 
					 * store key, check if is a collection
					 * */
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

	/**
	 * after get the resource, the method check if the Json is valid 
	 * @param resourceURI
	 */
	public Resource getResource(String resourceURI){
		if(this.validate()) {
			//TODO search the resource matching with resourceURI
			return new Resource();
		}else
			return null;
	}
	
}
