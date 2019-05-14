package org.universAAL.middleware.serialization.json.algorithms;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import javax.print.attribute.HashAttributeSet;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.grammar.ContextDefinition;
import org.universAAL.middleware.serialization.json.grammar.IRI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class ExpandJSONLD {
	private ContextDefinition context=null;
	private JsonObject jsonToExpand= null; 
	private Map<JsonElement, Boolean> defined = new HashMap<JsonElement, Boolean>(); 
	JsonParser parser = new JsonParser();
	JsonArray result = new JsonArray();
	public ExpandJSONLD(Object jsonToExpand) {
		
		if(jsonToExpand instanceof InputStream) {
			String jsonString = "";
			Scanner s = new Scanner((InputStream)jsonToExpand);
			s.useDelimiter("\\A");
			jsonString = s.hasNext() ? s.next() : "";
			s.close();
			this.jsonToExpand = parser.parse(jsonString).getAsJsonObject();
		}
		
		if(jsonToExpand instanceof JsonObject) {
			this.jsonToExpand = (JsonObject) jsonToExpand;
		}
		
		if(jsonToExpand instanceof String) {
			this.jsonToExpand = parser.parse((String)jsonToExpand).getAsJsonObject();
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
			this.context = new ContextDefinition(parser.parse((String)context));		
		}
		
	}
	
		public void expand() {
		
		if(this.context ==null) {
			if(this.jsonToExpand.has(JsonLdKeyword.CONTEXT.toString())) {
				this.context = new ContextDefinition(this.jsonToExpand.remove(JsonLdKeyword.CONTEXT.toString()));	
			}
			
		}
		if(this.context !=null) {
			for (Entry<String, JsonElement> element: this.jsonToExpand.entrySet()) {
				System.out.println("next main item");
				result.add(this.expandElement(element.getKey(), element.getValue()));
			}
		}else
			System.out.println("missing context");
		
	}
	
	private JsonElement expandElement(String key, JsonElement value) {
		JsonObject expanded_element = new JsonObject();
		if(value instanceof JsonPrimitive) {
			JsonElement expandedKey =this.expandKey(key);
			if(expandedKey instanceof JsonPrimitive) {
				expanded_element.add(JsonLdKeyword.VALUE.toString(), value);
				return expanded_element ;
			}
			if(expandedKey instanceof JsonObject) {
				expanded_element.add(JsonLdKeyword.TYPE.toString(), expandedKey.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()));//TODO control if value has :
				expanded_element.add(JsonLdKeyword.VALUE.toString(),value);//TODO control if value has :
			}
			return expanded_element;
		}
		if(value instanceof JsonObject) {
				for (Entry<String, JsonElement> iterable_element : value.getAsJsonObject().entrySet()) {
					JsonElement exp_key =this.expandKey(iterable_element.getKey());
					JsonArray aux_array = new JsonArray();
					aux_array.add(this.expandElement(iterable_element.getKey(), iterable_element.getValue()));
					
					if(exp_key.isJsonPrimitive()) {
						expanded_element.add(exp_key.getAsJsonPrimitive().toString(),aux_array);
					}
					
					if(exp_key.isJsonObject()){
						expanded_element.add(exp_key.getAsJsonObject().get(JsonLdKeyword.ID.toString()).toString(),aux_array);
					}
					
				}
				
				return expanded_element;
						 
		}
		
		if(value instanceof JsonArray) {
			
			JsonArray res = new JsonArray();
			JsonElement key_expanded = this.expandKey(key);
			
			if(key_expanded instanceof JsonPrimitive) {
				
				for (int i = 0; i < value.getAsJsonArray().size(); i++) {
					JsonElement t = this.expandElement(key, value.getAsJsonArray().get(i));
					res.add(t);
				}
				
				expanded_element.add(key_expanded.getAsJsonPrimitive().toString(), res);
			}
		}
		
		return expanded_element;
	}
	
	private JsonElement expandKey(String key) {
		
		JsonElement expanded=null;
		//check if the key has :
		if(this.context.hasTerm(key)) {
			expanded = this.context.getTermValue(key);
		}else
		System.out.println("missink key...skipping");
		
		return expanded;
	}

	public JsonArray getExpandedJson() {
		return this.result;
	}
}
