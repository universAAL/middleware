
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SortingFocusTraversalPolicy;

import java.util.Scanner;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;

//import com.fasterxml.jackson.core.JsonGenerationException;
//import com.github.jsonldjava.core.JsonLdError;
//import com.github.jsonldjava.core.JsonLdOptions;
//import com.github.jsonldjava.core.JsonLdProcessor;
//import com.github.jsonldjava.utils.JsonUtils;
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
	private ContextDefinition mainContext=null;
	private JsonElement mainJSON = null;


	private List<Resource> resourcesList=null; 
	private Resource subject=null,predicate=null,object=null;
	private String mainJsonString;
	private JsonParser jp = null;
	private Hashtable resources = new Hashtable();

	/**	
	 * 
	 * @param jsonToBeProcessed {@link InputStream} of json to be analyzed
	 * @throws JsonParseException
	 * @throws JsonSyntaxException
	 */
	public JSONLDDocument(InputStream jsonToBeProcessed) throws JsonParseException, JsonSyntaxException,ClassCastException,NullPointerException,JsonSyntaxException{
		//maybe its necesary to use jsonld library to expand the json to process it 
		this.resourcesList = new ArrayList<Resource>();
		
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
		//datamodel especification	https://www.w3.org/TR/2014/REC-json-ld-20140116/#data-model
		/*
		A JSON-LD document serializes a generalized RDF Dataset [RDF11-CONCEPTS], 
		which is a collection of graphs that comprises exactly one default graph and zero or more named graphs.
		A JSON-LD document MUST be a single node object or an array whose elements are each node objects at the top level.
		*/
		//add the default graph as resource 
		
		if(this.mainJSON.isJsonArray()){
			System.out.println("validating json array..."+this.mainJSON);
			for (Entry<String, JsonElement> namedGraph: this.mainJSON .getAsJsonArray().iterator().next().getAsJsonObject().entrySet()) {
				//each entry element represents a named graph
				System.err.println("graph name----->"+namedGraph.getKey());
				if(namedGraph.getValue().isJsonArray()) {
					this.processGraph(namedGraph.getValue().getAsJsonArray());
				}else {
					System.out.println("not array");
				}

			}
						
				}
		
		if(this.mainJSON.isJsonObject()) {
			//TODO check multiple contexts
			if(this.mainJSON.getAsJsonObject().has(JsonLdKeyword.CONTEXT.toString())) {
				
				System.out.println("...validating context...");
				this.mainContext = new ContextDefinition(this.mainJSON.getAsJsonObject().get(JsonLdKeyword.CONTEXT.toString()));
				if(!this.mainContext.validate())
					return false;
			}else {
				//validate the rest of jsonLD...maybe is expanded and we need to check it with actual context
			}
		}

//		else if(this.mainJSON .isJsonObject()) {
//			//TODO interpret this case
//		}else if(this.mainJSON .isJsonPrimitive()) {
//			//TODO interpret this case			
//		} else
//			//TODO check documentation
//			return false;

		return true;
}
	/**
	 * after get the resource, the method check if the Json is valid 
	 * @param resourceURI
	 */
	public Resource getResource(String resourceURI){
		return (Resource) resources.get(resourceURI);
	}
	
	public Enumeration<Resource> getAllResources() {
		return this.resources.elements();
	}
	
	private void processGraph(JsonArray graph) {
		Resource resource=null;
		System.out.println("\n processing graph..."+graph.toString());
		
		for(int z =0; z<graph.size();z++) {
			if(graph.get(z).isJsonArray()) {
				processGraph(graph.get(z).getAsJsonArray());
			}
			if(graph.get(z).isJsonObject()) {
				processCollection(graph.get(z).getAsJsonObject());
			}
		}
		
	}
	
	private void processCollection(JsonObject list) {
		Resource r;
		System.out.println("processing collection..."+list);
		for (Entry<String, JsonElement> item : list.entrySet()) {
			if(item.getValue().isJsonArray()) {
				this.processGraph(item.getValue().getAsJsonArray());
			}else {
				System.err.println("build resource heare using resprop="+item.getKey()+" and resobj="+item.getValue());
				r= new Resource();
				this.resources.put(r.getURI(), r);
				r.setProperty(item.getKey(), item.getValue());
			}
		}
	}
	

	public JsonElement getMainJSON() {
		return mainJSON;
	}

	public ContextDefinition getActiveContext() {
		return this.mainContext;
	}


}
