
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
package org.universAAL.middleware.serialization.json.analyzers;

import java.io.InputStream;
import java.util.Map.Entry;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;
import org.universAAL.middleware.serialization.json.grammar.IRI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ExpandedJsonAnalyzer {

	private JsonArray toAnalyze=null;
	
	/**
	 * receive {@link InputStream} or {@link JsonArray} to analyze the expanded json structure   
	 * @param candidate
	 */
	public ExpandedJsonAnalyzer(Object candidate) {
		if(candidate instanceof InputStream) {
			//TODO implement
		}
		if(candidate instanceof JsonArray) {
			this.toAnalyze = (JsonArray)candidate;
		}
	}
	
	public boolean validate() {
		if(this.toAnalyze.size() == 1 ) {
			if(this.toAnalyze.iterator().next().isJsonObject() ) {
				for ( Entry<String, JsonElement> element : this.toAnalyze.iterator().next().getAsJsonObject().entrySet()) {
					if(element.getKey().startsWith("@")) {
						if( !(element.getKey().equals("@type") || element.getKey().equals("@value") || element.getKey().equals("@id")) ) {
							return false;
						}
					}else{
						if(!process(new JsonPrimitive(element.getKey()))) {
							return false;
						}else {
							if(!process(element.getValue())) {
								return false;
							}
						}	
					}
				}
			}else {
				LogUtils.logDebug(JSONLDSerialization.owner, ExpandJSONLD.class, "validate", "JsonArray of expanded cant have anoter element than JsonObject");
			}
		}else {
			LogUtils.logDebug(JSONLDSerialization.owner, ExpandJSONLD.class, "validate", "JsonArray of expanded JsonLD cant have more of one element");
		}
			
	return true;	
	}
	
	private boolean process(JsonElement candidate) {
		if(candidate instanceof JsonPrimitive) {
			return IRI.isAbsolute(candidate.getAsJsonPrimitive().getAsString());
		}else if(candidate instanceof JsonObject) {
			for (Entry<String, JsonElement> element : candidate.getAsJsonObject().entrySet()) {
				//validate key
				if(element.getKey().startsWith("@")) {
					if( !(element.getKey().equals("@type") || element.getKey().equals("@value")) ) {
						return false;
					}
				}else {
					if(!IRI.isAbsolute(element.getKey())) return false;
				}
					

			}
		}else if(candidate instanceof JsonArray) {
			for (JsonElement element : candidate.getAsJsonArray()) {
				if(element instanceof JsonObject) {
					if(! process(element.getAsJsonObject())) return false;
				}
			}
		}
		
		return true;
	}

	public JsonArray getJson() {
		return this.toAnalyze;
	}
	

}
