
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
package org.universAAL.middleware.serialization.json.algorithms;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.grammar.ContextDefinition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;



/**
 * Class to expand JsonLD to Json. 
 * 
 * @author Eduardo Buhid
 *
 */
public class ExpandJSONLD {
	private ContextDefinition context=null;
	private JsonObject jsonToExpand= null;
	JsonParser parser = new JsonParser();
	JsonArray result_array = null;

	public ExpandJSONLD(Object jsonToExpand) {
		
		
		if(jsonToExpand instanceof InputStream) {
			String jsonString = "";
			Scanner s = new Scanner((InputStream)jsonToExpand);
			s.useDelimiter("\\A");
			jsonString = s.hasNext() ? s.next() : "";
			s.close();
			this.jsonToExpand = parser.parse(jsonString).getAsJsonObject();
			JsonElement element = parser.parse(jsonString);
			this.setJsonType(element);
		}else if(jsonToExpand instanceof JsonObject) {
			this.jsonToExpand = (JsonObject) jsonToExpand;
		}else if(jsonToExpand instanceof String) {
			JsonElement e =parser.parse((String)jsonToExpand); 
			this.setJsonType(e);
		}
		
	}
	
	public ExpandJSONLD(Object jsonToExpand, Object context) {
		this(jsonToExpand);
		
		if(context instanceof InputStream) {
			
			this.context = new ContextDefinition((InputStream)context);		
		}
		
		if(context instanceof JsonObject) {
			this.context = new ContextDefinition((JsonObject)context);
		}
		
		if(context instanceof String) {
			this.context = new ContextDefinition(parser.parse((String)context).getAsJsonObject());		
		}
		
	}

	/**
	 * method to start expansion process
	 */
	public void expand() {
		if(result_array==null) {
			this.result_array = new  JsonArray();
			if(this.context == null) {
				if(this.jsonToExpand.has(JsonLdKeyword.CONTEXT.toString())) {
					this.context = new ContextDefinition(this.jsonToExpand.remove(JsonLdKeyword.CONTEXT.toString()).getAsJsonObject());
					JsonObject aux_obj = new JsonObject();
					for (Entry<String, JsonElement> element: this.jsonToExpand.entrySet()) {
						JsonElement e = this.expandElement(element.getKey(), element.getValue());
						if(e!=null) {
							Entry<String, JsonElement> entry = e.getAsJsonObject().entrySet().iterator().next();
							aux_obj.add(entry.getKey(),entry.getValue());	
						}
					}
					result_array.add(aux_obj);
				}	
			}
			
		}else {
			LogUtils.logDebug(JSONLDSerialization.owner, ExpandJSONLD.class, "expand", "missing context, json already expanded?");
		}
			
				
	}
	
	private JsonElement expandElement(String key, JsonElement value) {
		JsonObject expanded_result = new JsonObject();
		JsonElement expandedKey = this.iriExpansion(new JsonPrimitive(key));
		JsonElement result = null;
		JsonObject aux = null ;
		if(expandedKey == null) 
			return null;
		if(value instanceof JsonPrimitive ) {
			JsonElement expanded_value;
			boolean is_id = false;
						
			if(value.getAsJsonPrimitive().getAsString().contains(":")) {
				expanded_value =  this.iriExpansion(value);	
			}else 
				 expanded_value = value;

			if(expandedKey instanceof JsonPrimitive ) {
				JsonPrimitive expanded_key = (JsonPrimitive) expandedKey;
				if(expanded_key.getAsJsonPrimitive().getAsString().startsWith("@")) {
					if(expanded_key.getAsJsonPrimitive().getAsString().equals("@id")) {
						result = expanded_value.getAsJsonPrimitive();
					}else if(expanded_key.getAsJsonPrimitive().getAsString().equals("@type")) {
						result = new JsonArray();
						is_id = true;
						((JsonArray)result).add(expanded_value);
					}
				
				}else {
					result = new JsonArray();
					aux = new JsonObject();
					if(is_id) {
						aux.add("@id", expanded_value);
					}else {
						aux.add("@value", expanded_value);
					}
					((JsonArray)result).add(aux);
				}
			}else if(expandedKey instanceof JsonObject ) {
				
				result = new JsonArray(); 
					if(((JsonObject) expandedKey).has(JsonLdKeyword.TYPE.toString())) {
						JsonPrimitive type_value =((JsonObject) expandedKey).get(JsonLdKeyword.TYPE.toString()).getAsJsonPrimitive();
						if(type_value.getAsString().equals(JsonLdKeyword.ID.toString())) {
							aux = new JsonObject();
							aux.add(JsonLdKeyword.ID.toString(), this.iriExpansion(value));
							((JsonArray)result).add(aux);
						}else {
							aux = new JsonObject();
							aux.add(JsonLdKeyword.VALUE.toString(), value);
							aux.add(JsonLdKeyword.TYPE.toString()  , this.iriExpansion(type_value));
							((JsonArray)result).add(aux);
						}
					}else {
						aux = new JsonObject();
						aux.add(JsonLdKeyword.ID.toString(), value);
						((JsonArray)result).add(aux);
					}
			String ID=null;
				
			if(((JsonObject) expandedKey).has(JsonLdKeyword.ID.toString())) {
				ID = ((JsonObject) expandedKey).get(JsonLdKeyword.ID.toString()).getAsJsonPrimitive().getAsString();
			}else{
				ID = ((JsonObject) expandedKey).get("expanded_key").getAsJsonPrimitive().getAsString();
			}
			expandedKey= new JsonPrimitive(ID);
			
			}
			expanded_result.add(expandedKey.getAsJsonPrimitive().getAsString(), result);
		}else if(value instanceof JsonObject ) {
			expanded_result.add(expandedKey.getAsJsonPrimitive().getAsString(), this.expandObject(value));
		}else if(value instanceof JsonArray ) {
			JsonArray expanded_items = new JsonArray(); 			
			if(expandedKey.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.TYPE.toString())) {
				for (JsonElement array_item : ((JsonArray)value)) {
					JsonElement expanded_array_item = iriExpansion(array_item);
					expanded_items.add(expanded_array_item);
				}
				expanded_result.add(expandedKey.getAsJsonPrimitive().getAsString(), expanded_items);
			}else{
				for (JsonElement array_item : ((JsonArray)value)) {
					if(array_item instanceof JsonObject) {
						JsonArray a =this.expandObject(array_item).getAsJsonArray();
						if(a.iterator().hasNext()) {
							expanded_items.add(a.iterator().next());	
						}
						
					}else if (array_item instanceof JsonPrimitive) {
						aux = new JsonObject();
						aux.add("@value", array_item);
						expanded_items.add(aux);	
					}
				}
				expanded_result.add(expandedKey.getAsJsonPrimitive().getAsString(), expanded_items);
			}	
		}
		return expanded_result;
	
	}

	private JsonElement expandObject(JsonElement value) {

		JsonObject result_obj = new JsonObject();
		JsonArray result_array = new JsonArray();
		for (Entry<String, JsonElement> item :((JsonObject)value).entrySet()) {
			JsonElement expanded_item = this.expandElement(item.getKey(), item.getValue());
			if(expanded_item instanceof JsonObject) {
					result_obj.add(((JsonObject)expanded_item).entrySet().iterator().next().getKey(), ((JsonObject)expanded_item).entrySet().iterator().next().getValue());
			}
		}
	
		if(result_obj.entrySet().size()>0) {
			result_array.add(result_obj);
		}
		return result_array;
	}
	

	private JsonElement iriExpansion(JsonElement key) {
		String prefix="",sufix="";
		JsonElement expanded=null;
		if(key.getAsJsonPrimitive().getAsString().startsWith("@") || key.isJsonNull()) {
			return key;	
		}
		
		if(key.isJsonPrimitive()) {
			if(key.getAsJsonPrimitive().getAsString().contains(":")) {
				String candidate = key.getAsJsonPrimitive().getAsString();
				prefix = key.getAsJsonPrimitive().getAsString().substring(0, key.getAsJsonPrimitive().getAsString().indexOf(":"));
				sufix = key.getAsJsonPrimitive().getAsString().substring(key.getAsJsonPrimitive().getAsString().indexOf(":")+1);
				if(prefix.contains("_") || sufix.startsWith("//")) {
					return key;
				}
				if(this.context.hasTerm(key) ) {
					if(this.context.getTermValue(key).isJsonObject()) {
						JsonObject auxiliar = this.context.getTermValue(key).getAsJsonObject();
						 JsonElement y = this.iriExpansion(new JsonPrimitive(prefix));
						 String generated = y.getAsString()+sufix;
						auxiliar.add("expanded_key",new JsonPrimitive(generated));
						return auxiliar;	
					}
				}
				if(this.context.hasTerm(prefix)) {
					String generatedIRI= this.iriExpansion(new JsonPrimitive(prefix)).getAsString();
					expanded = new JsonPrimitive(generatedIRI+candidate.substring(candidate.indexOf(":")+1));
				}else {
					expanded = key.getAsJsonPrimitive();
				}
			}else if(this.context.hasTerm(key)) {
					if(this.context.getTermValue(key).isJsonObject()) {
						expanded = this.context.getTermValue(key);
					}else if(this.context.getTermValue(key).isJsonPrimitive()) {
							expanded = this.context.getTermValue(key);
						}
			}
		}
		return expanded;
	}
	
	public void setJsonToExpand(Object jsonToExpand) {
	}

	public JsonArray getExpandedJson() {
		return this.result_array;
	}

	private void setJsonType(JsonElement e) {
		if(e instanceof JsonArray )
			this.result_array = e.getAsJsonArray();
		if(e instanceof JsonObject)
			this.jsonToExpand = e.getAsJsonObject();
	}

}
