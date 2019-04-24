
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SortingFocusTraversalPolicy;

import java.util.Scanner;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
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
	private JsonElement mainJSON = null;
	private List<Resource> resources=null; 
	private Resource firstResource=null;
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
		this.resources = new ArrayList<Resource>();
		
		String jsonString = "";
		Scanner s = new Scanner(jsonToBeProcessed);
		s.useDelimiter("\\A");
		jsonString = s.hasNext() ? s.next() : "";
		s.close();
		this.mainJsonString=jsonString;
		this.jp = new JsonParser();
		//throws MalformedJsonException if the json is mal formed
		try {
			this.mainJSON = jp.parse(this.processJsonLDFormat("expand"));
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
		//datamodel especification	https://www.w3.org/TR/2014/REC-json-ld-20140116/#data-model
		/*
		A JSON-LD document serializes a generalized RDF Dataset [RDF11-CONCEPTS], 
		which is a collection of graphs that comprises exactly one default graph and zero or more named graphs.
		A JSON-LD document MUST be a single node object or an array whose elements are each node objects at the top level.
		*/
		//add the default graph as resource 
		if(this.mainJSON.isJsonArray()){
			//elements are each node objects at the top level.
			
			//JsonObject objAux = this.mainJSON .getAsJsonArray().iterator().next().getAsJsonObject();
					//node object check
					//iterate over internal graphs. Named graphs
					for (Entry<String, JsonElement> namedGraph: this.mainJSON .getAsJsonArray().iterator().next().getAsJsonObject().entrySet()) {
						//each entry element represents a named graph	
						if(IRI.isAbsolute(namedGraph.getKey())) {
							//check the child nodes
							//build a resource with the key of this node
							//check the child nodes
							new Resource(namedGraph.getKey());
							
							if(namedGraph.getValue().isJsonArray() || namedGraph.getValue().isJsonArray()) {
								//parseCollection()
							}
						}else if(namedGraph.getKey().equals(JsonLdKeyword.BLANK_NODE)){
							new Resource();
						}else
							return false;
					}
				}

		else if(this.mainJSON .isJsonObject()) {
			//TODO interpret this case
		}else if(this.mainJSON .isJsonPrimitive()) {
			//TODO interpret this case			
		} else
			//TODO check documentation
			return false;

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
	
	private  String processJsonLDFormat(String out)  {
		try {
			// Open a valid json(-ld) input file
			//InputStream inputStream = new FileInputStream("input.json");
			// Read the file into an Object (The type of this object will be a List, Map, String, Boolean,
			// Number or null depending on the root object in the file).
			Object jsonObject = JsonUtils.fromString(this.mainJsonString);
			// Create a context JSON map containing prefixes and definitions
			//Map context = new HashMap();
			// Customise context...
			// Create an instance of JsonLdOptions with the standard JSON-LD options
			JsonLdOptions options = new JsonLdOptions();
			// Customise options...
			// Call whichever JSONLD function you want! (e.g. compact)
			Object compact=null; 
			switch (out) {
				case "expand":
					compact = JsonLdProcessor.expand(jsonObject, options);
					break;
				case "compact":
					compact = JsonLdProcessor.compact(jsonObject, new HashMap(), options);
					break;
			}
			
			// Print out the result (or don't, it's your call!)
			System.out.println("algorithm result  " +out+" --> "+JsonUtils.toString(compact));
			return JsonUtils.toString(compact);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

}
