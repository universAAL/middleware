
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
	private List<Resource> resources=null; 

	private JsonParser jp = null;

	/**	
	 * 
	 * @param jsonToBeProcessed {@link InputStream} of json to be analyzed
	 * @throws JsonParseException
	 * @throws JsonSyntaxException
	 */
	public JSONLDDocument(InputStream jsonToBeProcessed) throws JsonParseException, JsonSyntaxException,ClassCastException,NullPointerException,JsonSyntaxException{
		this.resources = new ArrayList<Resource>();
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
		JsonObject aux = this.mainJSON;
		ContextDefinition active=null;
		Boolean compacted=false;
		Resource subject=null, predicate=null,resource=null;
		
		if(this.mainJSON.has(JsonLdKeyword.CONTEXT.toString())) {
			compacted=true;
		}
	
		if(compacted) {
			active = new ContextDefinition( aux.get(JsonLdKeyword.CONTEXT.toString()));
			if (!active.validate()) 
				return false;
			aux.remove(JsonLdKeyword.CONTEXT.toString());
		}

		return this.validateBody(aux);
	}
	
	private boolean validateBody(JsonElement toValidate) {

		
		if(toValidate.isJsonObject()){
			
			if(this.resources.isEmpty()) {
			//validate key before generate resource(?)
				
			}
				
			for (Entry<String, JsonElement> item : toValidate.getAsJsonObject().entrySet()) {
				
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
		
		if(toValidate.isJsonObject()){
					
		}
		
		if(toValidate.isJsonObject()){
			
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
