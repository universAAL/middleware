package org.universAAL.middleware.serialization.json.grammar;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.algorithms.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class ExpandJson {

	private JsonArray expandedJsonLD=null;
	private Object jsonToExpand=null;
	private JsonObject mainJson;
	private JsonParser parser = new JsonParser();
	private Context activeContext=null, localContext =null;
	Map<String, Boolean > defined = new HashMap<>();
	
	public  ExpandJson(Object jsonToExpand) {
		this.jsonToExpand=jsonToExpand;
		this.initJSONLDDoc(jsonToExpand);
		this.expandedJsonLD = new JsonArray();
	}
	
	private JsonElement expandElement (Context activeContext,String activePropertie, JsonElement value, boolean flagExpansion) {
		
		//active property = key of the element to be expanded
		//element to expand = value associated with the key
		if(value == null)
			return  null;
		
		if(activePropertie.equals("@default")){
			flagExpansion = false;
		}
		
		if(value.isJsonPrimitive()) {
			if(activePropertie.equals(JsonLdKeyword.GRAPH) || activePropertie == null) {
				return null;
			}else {
				//use Value Expansion algorithm
				return this.valueExpansion(activeContext, activePropertie, value);//element to expand as value (following documentation)
			}
		}
		
		if(value.isJsonArray()) {
			JsonArray aux_result = new JsonArray();
			JsonElement expanded_item = this.expandElement(activeContext, activePropertie, value, flagExpansion);
			if(activePropertie.equals(JsonLdKeyword.LIST.toString())) {
				
				//the expanded item must not be an array or list object (a list object is a JSONObject that has a @list member)
			}
			if(expanded_item.isJsonArray()) {
				//apend it items to result
			}
	
		}
		
		//if isnt any of above cases, its a json object
		if(value.isJsonObject()) {
			//element is a dictionary
			JsonObject aux = value.getAsJsonObject();
			if(aux.has(JsonLdKeyword.CONTEXT.toString())) {
				//update active context "merging" using the appropiate algorithm
			}else {
				Object result_dictionary = new Object();
			
				for (Map.Entry<String, JsonElement> i : value.getAsJsonObject().entrySet()) {
						String expanded_prop = this.iriExpansion(activeContext, i.getValue().getAsString(), false);
						//String expanded_prop = this.expandIRI(element);
						JsonElement  expaded_value = this.expandElement(activeContext, activePropertie, value, false);
						if(expanded_prop != null || expanded_prop.contains(":") || JsonLdKeyword.isKeyword(expanded_prop)) {
							if(JsonLdKeyword.isKeyword(expanded_prop)) {
								if(activePropertie.equals(JsonLdKeyword.REVERSE.toString())) {
									//TOOD throw error "invalid_reverse_rpoperty_map"
								}
								if(expanded_prop.equals(JsonLdKeyword.TYPE.toString()) && !i.getValue().isJsonPrimitive() && !i.getValue().isJsonArray() ) {
									//throw error
								}
								if(expanded_prop.equals(JsonLdKeyword.VALUE.toString()) && (!i.getValue().isJsonPrimitive() || !i.getValue().isJsonNull()) ) {
								//throw error	
								}
								if(expanded_prop.equals(JsonLdKeyword.LANG.toString())  &&  !i.getValue().isJsonPrimitive()) {
									//throw error
								}
								if(expanded_prop.equals(JsonLdKeyword.INDEX.toString()) && !i.getValue().isJsonPrimitive()) {
									//throw error
								}
								if(expanded_prop.equals(JsonLdKeyword.LIST.toString())) {
									if(activePropertie ==null || activePropertie.equals(JsonLdKeyword.GRAPH.toString())) {
										//remove the free floating flag and continue 
									}else {
										expaded_value=this.valueExpansion(activeContext, activePropertie, value);
										if(expaded_value.isJsonObject() && expaded_value.getAsJsonObject().has(JsonLdKeyword.LIST.toString())) {
											//throw list of lsit error
										}
									}
								}
								if(expanded_prop.equals(JsonLdKeyword.SET.toString())) {
									expaded_value = this.expandElement(activeContext, activePropertie, i.getValue(), flagExpansion);
 								}
								
								if(expanded_prop.equals(JsonLdKeyword.REVERSE.toString()) && !(expaded_value.isJsonObject() && expaded_value.getAsJsonObject().has(JsonLdKeyword.LIST.toString()))  ) {
									
								}
								
							
							}
						}
						
				}
			}
			
			
			
		}
			
		return null;
	}
	
	private void initJSONLDDoc(Object jsonToExpand) {
		
		if(jsonToExpand instanceof String) {
			if(parser.parse((String)jsonToExpand).isJsonObject()) {
				this.mainJson = parser.parse((String)jsonToExpand).getAsJsonObject();
			}
		}

		if(jsonToExpand instanceof JsonObject) {
			this.mainJson = parser.parse((String)jsonToExpand).getAsJsonObject();
		}

		if(jsonToExpand instanceof InputStream) {
			String jsonString = "";
			Scanner s = new Scanner( (InputStream)this.jsonToExpand );
			s.useDelimiter("\\A");
			jsonString = s.hasNext() ? s.next() : "";
			s.close();
			if(parser.parse(jsonString).isJsonObject()) {
				this.mainJson = parser.parse(jsonString).getAsJsonObject();
			}
		}
		
	}
	
	public String getContext() {
		return this.activeContext.getContext();
	}
 
	
	public JsonElement expandJsonDocument() {
		System.out.println("expandJsonDocument");
		if(this.mainJson.has(JsonLdKeyword.CONTEXT.toString())) {
			JsonObject obj =(JsonObject) this.mainJson.remove(JsonLdKeyword.CONTEXT.toString());
			System.out.println("el contexto a procesar es "+obj.toString());
			this.activeContext = new Context(obj);
			this.expandedJsonLD = new JsonArray();
			for (Map.Entry<String, JsonElement> elementToExpand : this.mainJson.entrySet()) {
				this.expandedJsonLD.add(this.expandElement(this.activeContext, elementToExpand.getKey(), elementToExpand.getValue() , true));
			}
		}else
			System.out.println("missing ctx");
		
		return this.expandedJsonLD;
	}
	
	
	
	//------------------------algorithms
	
	private String iriExpansion(Context activeContext, String term, boolean flag) {
	// passing active context, value, and true for document relative.
	//The required inputs are an active context and a value to be expanded
		
		//If value is a keyword or null, return value as is.
		JsonElement result=null;
		if(term.equals("") || term ==null || JsonLdKeyword.isKeyword(term) ) {
			return term;
		}
		
		if(this.localContext !=null) {
			//local context contains a key equals value
			JsonElement jse = this.localContext.hasTypeMapping(term);
			if(this.defined.containsKey(term)) {
				if(this.defined.get(term)==false) {
					this.createTermDefinition(activeContext, activeContext, term);
				}
			}
				
			
				
			}
		return "";
	}
	
	private JsonElement valueExpansion(Context activeContext, String activePropertie,JsonElement valueToExpand) {
		//The algorithm takes three required inputs: an active context, an active property, and a value to expand.
		JsonObject dictionary = new JsonObject();
		JsonElement aux = this.activeContext.hasTypeMapping(activePropertie);
		if(aux!=null) {
			if(aux.isJsonObject()) {
			
			if(aux.getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
				Entry<String, JsonElement> t = aux.getAsJsonObject().entrySet().iterator().next();
				if(t.getValue().isJsonPrimitive()) {
					dictionary.addProperty(JsonLdKeyword.ID.toString(), this.iriExpansion(activeContext, t.getValue().getAsString(), true));
				}
			}
			
			if(aux.getAsJsonObject().has(JsonLdKeyword.VOCAB.toString())) {
				Entry<String, JsonElement> t = aux.getAsJsonObject().entrySet().iterator().next();
				if(t.getValue().isJsonPrimitive()) {
					dictionary.addProperty(JsonLdKeyword.ID.toString(), this.iriExpansion(activeContext, t.getValue().getAsString(), true));
				}
			}

			Entry<String, JsonElement> k = aux.getAsJsonObject().entrySet().iterator().next();
			//If active property has a type mapping in active context, other than @id or @vocab,
			//add an @type member to result and set its value to the value associated with the type mapping.
			if(JsonLdKeyword.isKeyword(k.getKey())) {
				if(!k.equals(JsonLdKeyword.ID.toString()) || !k.equals(JsonLdKeyword.VOCAB.toString())) {
					dictionary.add(JsonLdKeyword.TYPE.toString(), valueToExpand);
				}
			}

			}
			if(valueToExpand.isJsonPrimitive()) {
				dictionary.add(JsonLdKeyword.VALUE.toString(), valueToExpand);

			}
			
		}
			return dictionary;	
	}
	
	private JsonElement createTermDefinition(Context activeContext, Context localContext, String term) {
		JsonElement value=null;
		JsonObject dictionary = new JsonObject();
	// an active context, a local context, a term, and a map defined.	
		if(JsonLdKeyword.isKeyword(term)){
			//TODO throw error
			return null;
		}
			
		if(this.defined.containsKey(term)) {
			if(this.defined.get(term).booleanValue()) {
				return value;
			}else {
				//throw error
			}
				
		}else {
			this.defined.put(term, false);
			value = this.activeContext.hasTypeMapping(term);
			//If value is null or value is a dictionary containing the key-value pair @id-null,
			//set the term definition in active context to null, set the value associated with defined's key term to true, and return.
			if(value.isJsonNull() ) {
				//TODO throw error
				return null;	
			}
			if(value.isJsonObject()) {
				
					Entry<String, JsonElement> aux = value.getAsJsonObject().entrySet().iterator().next();
					if(aux.getKey().equals(JsonLdKeyword.ID.toString()) && aux.getValue().isJsonNull()) {
						this.defined.put(term, true);
						return value;
		
					}else if(value.isJsonPrimitive()) {
						//Otherwise, if value is a string, convert it to a dictionary consisting of 
						//a single member whose key is @id and whose value is value. Set simple term to true
						
							JsonObject jso = new JsonObject();
							jso.addProperty(JsonLdKeyword.ID.toString(), value.getAsJsonPrimitive().getAsString());
							
					}				
				
			}else if(value.isJsonPrimitive()) {
				dictionary.addProperty(JsonLdKeyword.ID.toString(),term);
				return dictionary;
			}else {
				//TODO throw error 
				return null;
			}
				
			JsonObject term_definition = new JsonObject();
			if(value.getAsJsonObject().has(JsonLdKeyword.TYPE.toString())) {
				String type ="";
				if(!value.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()).isJsonPrimitive()) {
					//TODO throw error
					return null;
				}else {
					type = value.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()).getAsJsonPrimitive().getAsString();
					type = this.iriExpansion(activeContext, type, false);
				}
			}
			
			if(value.getAsJsonObject().has(JsonLdKeyword.REVERSE.toString())) {
				
			}
			
			if(value.getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
				
			}
			
			if(value.getAsJsonObject().has(JsonLdKeyword.CONTAINER.toString())) {
				
			}
		
		
		}
		return value;
	}

	
}
