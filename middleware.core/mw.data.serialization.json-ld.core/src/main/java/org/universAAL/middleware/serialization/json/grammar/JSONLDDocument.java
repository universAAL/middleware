
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

	private ContextDefinition activeContext = null;
	private JsonObject mainJSON = null;
	private JsonParser jp = null;

	/**	
	 * 
	 * @param jsonToBeProcessed {@link InputStream} of json to be analyzed
	 * @throws JsonParseException
	 * @throws JsonSyntaxException
	 */
	public JSONLDDocument(InputStream jsonToBeProcessed) throws JsonParseException, JsonSyntaxException,ClassCastException,NullPointerException{

		String jsonString = "";
		Scanner s = new Scanner(jsonToBeProcessed);
		s.useDelimiter("\\A");
		jsonString = s.hasNext() ? s.next() : "";
		s.close();
		this.jp = new JsonParser();
		//throws MalformedJsonException if the json is mal formed
		try {
			
			this.mainJSON = (JsonObject) jp.parse(jsonString);
		}catch (Exception e) {
			//TODO log error
			System.out.println(e.getMessage());
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
		
//		// ¿has member called context (key equal context)?
//		if (this.mainJSON.has(this.CONTEXT)) {
//			
//			
//			System.out.println(mainJSON.toString());
//			LogUtils.logDebug(JSONLDSerialization.owner, this.getClass(), "validate", mainJSON.toString());
//			if (mainJSON.isJsonArray()) {
//				
//				boolean allNodeObject = true;
//				for (JsonElement je : mainJSON.getAsJsonArray()) {
//					allNodeObject &= je.isJsonObject();
//					allNodeObject &= new NodeObject(this, je.getAsJsonObject() ).validate();
//				}
//				return (mainJSON.isJsonObject() & allNodeObject);
//			} else {
//				return (this.mainJSON.isJsonObject() & (new NodeObject(this, this.mainJSON.getAsJsonObject()).validate()));
//			}
//		} else {
//			LogUtils.logDebug(JSONLDSerialization.owner, this.getClass(), "validate",
//					"the given Json document has not any context to process");
//			// TODO: log if the json document has not any context?
//			System.out.println("the given Json document has not any context to process");
//		}
		//-------------------------------------------------------
		
		for (Entry<String, JsonElement> item : this.mainJSON.entrySet()) {
			//analyze only elements with context key
			if (item.getKey().equals(JsonLdKeyword.CONTEXT.toString())) {
					//simple context
					if(item.getValue().isJsonObject()) {
						this.activeContext = new ContextDefinition(item.getValue());	
						if (!this.activeContext.validate()) 
							return false;
					
					}
					if(item.getValue().isJsonPrimitive()) {
						if (!IRI.isAbsolute(item.getValue().getAsString())){
							return false;
						}
					}
					//multiple contexts
					if(item.getValue().isJsonArray()) {
						//if the element has a context key and is an array...will be interpreted as an array of contexts
						for (int i = 0; i < item.getValue().getAsJsonArray().size(); i++) {
							if(item.getValue().getAsJsonArray().get(i).isJsonObject()) {
								if (  !(new NodeObject(this.activeContext, item.getValue().getAsJsonArray().get(i)).validate()) ) {
									return false;
								}
							}else {
								//TODO throw error
								return false;
							}
								
						}
					}
				
				
			}else {

				if(item.getValue().isJsonArray()) {

				}
				
				if(item.getValue().isJsonObject()) {
					//node object analyze
					if(! new NodeObject(activeContext, item.getValue()).validate())
						return false;
				}				
				if(item.getValue().isJsonPrimitive()) {
					//validate prmitive as IRI...
					
				}

				if(item.getValue().isJsonNull()) {
					//TODO: throw error
				}
				

				
			}

			
		}

		return true;
	}

}