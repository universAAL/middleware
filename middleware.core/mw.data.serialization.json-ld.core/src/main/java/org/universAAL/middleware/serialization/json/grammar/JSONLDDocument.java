
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;
import org.universAAL.middleware.serialization.json.analyzers.ExpandedJsonAnalyzer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
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
	private boolean isAlreadyExpanded = false,isValid=false;
	private ExpandJSONLD expand=null;
	private JsonArray expandedJson=null;


	/**	
	 * 
	 * @param jsonToBeProcessed {@link InputStream} of json to be analyzed
	 * @throws JsonParseException
	 * @throws JsonSyntaxException
	 */
	public JSONLDDocument(InputStream jsonToBeProcessed) {
 

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
		LogUtils.logDebug(JSONLDSerialization.owner, JSONLDDocument.class, "expand", "validating json");
		//datamodel especification	https://www.w3.org/TR/2014/REC-json-ld-20140116/#data-model
		/*
		A JSON-LD document serializes a generalized RDF Dataset [RDF11-CONCEPTS], 
		which is a collection of graphs that comprises exactly one default graph and zero or more named graphs.
		A JSON-LD document MUST be a single node object or an array whose elements are each node objects at the top level.
		*/
		//add the default graph as resource 
		if(this.mainJSON instanceof JsonObject) {
			if(this.mainJSON.getAsJsonObject().has(JsonLdKeyword.CONTEXT.toString())) {
				if(!this.mainJSON.getAsJsonObject().get(JsonLdKeyword.CONTEXT.toString()).isJsonNull()) {
					JsonElement ctx = this.mainJSON.getAsJsonObject().remove(JsonLdKeyword.CONTEXT.toString());
					if(ctx instanceof JsonPrimitive) {
						try {
							URL contextURL = new URL(ctx.getAsJsonPrimitive().getAsString());
							this.mainContext = new ContextDefinition(contextURL);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else if(ctx instanceof JsonObject) {
						this.mainContext = new ContextDefinition(ctx.getAsJsonObject());
					}
					this.isValid=this.mainContext.validate();
					this.mainJSON.getAsJsonObject().add(JsonLdKeyword.CONTEXT.toString(), this.mainContext.getJsonToValidate());
				}
				
			}else {
				LogUtils.logDebug(JSONLDSerialization.owner, ExpandJSONLD.class, "validate", "invaid JsonLD format. Missing JsonLD context");
			}
				
		}else if(this.mainJSON instanceof JsonArray) {
			//if a expanded jsonLD is given
			ExpandedJsonAnalyzer expanded = new ExpandedJsonAnalyzer(this.mainJSON);
			this.isValid=expanded.validate();
			this.isAlreadyExpanded = true;

		}else {
			LogUtils.logDebug(JSONLDSerialization.owner, ExpandJSONLD.class, "validate", "invaid json format. Expected JsonObject or JsonArray (expanded JsonLD)");
		}
		
		LogUtils.logDebug(JSONLDSerialization.owner, JSONLDDocument.class,"validate","is valid? "+isValid);
		return this.isValid;
	}
	
	public void expand() {
		LogUtils.logDebug(JSONLDSerialization.owner, JSONLDDocument.class, "expand", "expanding json");

		if(this.isValid) {
			if(this.isAlreadyExpanded) {
				this.expandedJson = this.mainJSON.getAsJsonArray();
				LogUtils.logDebug(JSONLDSerialization.owner, JSONLDDocument.class, "expand", "already expanded json");

			}else {
				this.expand = new ExpandJSONLD(this.mainJSON);
				this.expand.expand();
				this.expandedJson=this.expand.getExpandedJson();
			}	
		}else {
			LogUtils.logDebug(JSONLDSerialization.owner, ExpandJSONLD.class, "validate", "JsonLD is invalid");
			
		}
		
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

	
	public JsonArray getExpandedJson() {
		return this.expandedJson;
	}

	
}
