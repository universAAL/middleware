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
	private Map<String, Boolean > defined = new HashMap<>();
	
	public  ExpandJson(Object jsonToExpand) {
		this.jsonToExpand=jsonToExpand;
		this.initJSONLDDoc(jsonToExpand);
		this.expandedJsonLD = new JsonArray();
	}
	
	private JsonElement expandElement (String activePropertie, JsonElement value) {
		
		//active property = key of the element to be expanded
		//element to expand = value associated with the key
		if(value == null)
			return  null;
		
		if(activePropertie.equals("@default")){
			//flagExpansion = false;
		}
		
		if(value.isJsonPrimitive()) {
			if(activePropertie.equals(JsonLdKeyword.GRAPH) || activePropertie == null) {
				return null;
			}else {
				//use Value Expansion algorithm
				//return the IRI to this prop too (?)
				return this.valueExpansion(activePropertie, value);
			}
		}
		
		if(value.isJsonArray()) {
			JsonArray array = new JsonArray();
			System.out.println("expande array content ");
			for (int i = 0; i < value.getAsJsonArray().size(); i++) {	
				
				if(value.getAsJsonArray().get(i).isJsonObject()) {
					for (Entry<String, JsonElement> jsonElement : value.getAsJsonArray().get(i).getAsJsonObject().entrySet()) {
						JsonElement aux = this.expandElement(jsonElement.getKey(),jsonElement.getValue());
					}
				}
				
				if(value.getAsJsonArray().get(i).isJsonPrimitive()) {
					
				}
				

			}
			
//			JsonArray aux_result = new JsonArray();
//			JsonElement expanded_item = this.expandElement( activePropertie, value);
//			if(activePropertie.equals(JsonLdKeyword.LIST.toString())) {
//				
//				//the expanded item must not be an array or list object (a list object is a JSONObject that has a @list member)
//			}
//			if(expanded_item.isJsonArray()) {
//				//apend it items to result
//			}
	
		}
		
		if(value.isJsonObject()) {
			System.out.println("expande object (dictionary) contet ");
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
				this.expandedJsonLD.add(this.expandElement( elementToExpand.getKey(), elementToExpand.getValue()));
			}
			
		}else
			System.out.println("missing ctx");
		
		return this.expandedJsonLD;
	}
	
	//------------------------algorithms
	
	
	//get from expandElenent the value as JsonElement from the main Json
	private JsonElement iriExpansion(String activePropertie,JsonElement term) {
		JsonObject term_definition = new JsonObject();
		if(this.localContext !=null) {
			
			JsonElement value = this.localContext.hasTypeMapping(activePropertie);
			
			if(value!=null) {
				if(value.isJsonObject()) {
					Entry<String, JsonElement> t = value.getAsJsonObject().entrySet().iterator().next();
					if(t.getKey().equals(JsonLdKeyword.ID) && t.getValue().isJsonNull()) {
						//TODO If value is null or value is a dictionary containing the key-value pair @id-null,
						//set the term definition in active context to null, set the value associated with defined's member term to true, and return. 
					}
					
					if(value.getAsJsonObject().has(JsonLdKeyword.TYPE.toString())) {
						
					}
					
					if(value.getAsJsonObject().has(JsonLdKeyword.REVERSE.toString())) {
						
					}
					
					

				}else if(value.isJsonPrimitive()) {
					JsonObject aux = new JsonObject();
					aux.add(JsonLdKeyword.ID.toString(), value);
				}else {
					//TODO throw error
				}
			}
			
			
				
			}
		return null;
	}
	
	private JsonElement valueExpansion(String activePropertie,JsonElement valueToExpand) {
		//The algorithm takes three required inputs: an active context, an active property, and a value to expand.
		System.out.println("valueExpansion| active prop "+activePropertie+"  to expand "+valueToExpand);
		JsonObject dictionary = new JsonObject();
		JsonElement aux = this.activeContext.hasTypeMapping(activePropertie);
		
		if(aux!=null) {
			//if the given propertie has type mapping
			if(aux.isJsonObject()) {
			
				if(aux.getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
					Entry<String, JsonElement> t = aux.getAsJsonObject().entrySet().iterator().next();
					if(t.getValue().isJsonPrimitive()) {
						dictionary.add(JsonLdKeyword.ID.toString(),this.iriExpansion(activePropertie, valueToExpand));
					} 
				}
				
				if(aux.getAsJsonObject().has(JsonLdKeyword.VOCAB.toString())) {
					Entry<String, JsonElement> t = aux.getAsJsonObject().entrySet().iterator().next();
					if(t.getValue().isJsonPrimitive()) {
						dictionary.add(JsonLdKeyword.ID.toString(), this.iriExpansion(activePropertie, t.getValue()));
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
			//if the propertie is mapped to a simple string
			if(valueToExpand.isJsonPrimitive()) {
				System.out.println("expand primitive ");
				dictionary.add(JsonLdKeyword.VALUE.toString(), valueToExpand);

			}
			
		}
			return dictionary;	
	}
	
	private JsonElement createTermDefinition( JsonElement term) {
		JsonElement generated=null;
		return generated;
	}

	
}
