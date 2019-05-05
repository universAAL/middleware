package org.universAAL.middleware.serialization.json.algorithms;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class to expand JsonLD.
 * @author Eduardo
 *
 */
public class ExpandJsonLD {
	private JsonArray expandedJsonLD=null;
	private Object jsonToExpand=null;
	private JsonObject mainJson;
	private JsonParser parser = new JsonParser();
	private Context activeContext=null, localContext =null;
	Map<String, Boolean > defined = new HashMap<>();
	
	public ExpandJsonLD(Object jsonToExpand) {
		this.jsonToExpand=jsonToExpand;
		this.initJSONLDDoc(jsonToExpand);
		this.expandedJsonLD = new JsonArray();
	}
	
	private String expandElement (Context activeContext,String activePropertie, JsonElement elementToExpand, boolean flagExpansion) {
		
		//active property = key of the element to be expanded
		//element to expand = value associated with the key
		if(elementToExpand == null)
			return  null;
		
		if(activePropertie.equals("@default")){
			flagExpansion = false;
		}
		
		if(elementToExpand.isJsonPrimitive()) {
			if(activePropertie.equals(JsonLdKeyword.GRAPH) || activePropertie == null) {
				return null;
			}else {
				//use Value Expansion algorithm
				ValueExpansion expansion = new ValueExpansion();
				expansion.expand(activeContext, activePropertie, elementToExpand);//element to expand as value (following documentation)
			}
		}
		
		if(elementToExpand.isJsonArray()) {
			JsonArray aux_result = new JsonArray();
			String expanded_item = this.expandElement(activeContext, activePropertie, elementToExpand, flagExpansion);
			if(activePropertie.equals(JsonLdKeyword.LIST.toString())) {
				
				//the expanded item must not be an array or list object (a list object is a JSONObject that has a @list member)
			}
			if(parser.parse(expanded_item).isJsonArray()) {
				//apend it items to result
			}
	
		}
		
		//if isnt any of above cases, its a json object
		if(elementToExpand.isJsonObject()) {
			//element is a dictionary
			JsonObject aux = elementToExpand.getAsJsonObject();
			if(aux.has(JsonLdKeyword.CONTEXT.toString())) {
				//update active context "merging" using the appropiate algorithm
			}else {
				Object result_dictionary = new Object();
			
				for (Map.Entry<String, JsonElement> element : elementToExpand.getAsJsonObject().entrySet()) {
						String expanded_prop = IRIExpansion.expand(activeContext, element);
						//String expanded_prop = this.expandIRI(element);
						String  expaded_value = this.expandValue();
						if(expanded_prop != null || expanded_prop.contains(":") || JsonLdKeyword.isKeyword(expanded_prop)) {
							if(JsonLdKeyword.isKeyword(expanded_prop)) {
								if(activePropertie.equals(JsonLdKeyword.REVERSE.toString())) {
									//TOOD throw error "invalid_reverse_rpoperty_map"
								}
								if(expanded_prop.equals(JsonLdKeyword.TYPE.toString()) && !element.getValue().isJsonPrimitive() && !element.getValue().isJsonArray() ) {
									//throw error
								}
								if(expanded_prop.equals(JsonLdKeyword.VALUE.toString()) && (!element.getValue().isJsonPrimitive() || !element.getValue().isJsonNull()) ) {
								//throw error	
								}
								if(expanded_prop.equals(JsonLdKeyword.LANG.toString())  &&  !element.getValue().isJsonPrimitive()) {
									//throw error
								}
								if(expanded_prop.equals(JsonLdKeyword.INDEX.toString()) && !element.getValue().isJsonPrimitive()) {
									//throw error
								}
								if(expanded_prop.equals(JsonLdKeyword.LIST.toString())) {
									if(activePropertie ==null || activePropertie.equals(JsonLdKeyword.GRAPH.toString())) {
										//remove the free floating flag and continue 
									}else {
										expaded_value=this.expandValue();
										if(parser.parse(expaded_value).isJsonObject() && parser.parse(expaded_value).getAsJsonObject().has(JsonLdKeyword.LIST.toString())) {
											//throw list of lsit error
										}
									}
								}
								if(expanded_prop.equals(JsonLdKeyword.SET.toString())) {
									expaded_value = this.expandElement(activeContext, activePropertie, elementToExpand, flagExpansion);
 								}
								
								if(expanded_prop.equals(JsonLdKeyword.REVERSE.toString()) && !(parser.parse(expaded_value).isJsonObject() && parser.parse(expaded_value).getAsJsonObject().has(JsonLdKeyword.LIST.toString()))  ) {
									
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
 
	
	private String expandValue() {
		return "";
	}
	public void expandJsonDocument() {
		System.out.println("expandJsonDocument");
		if(this.mainJson.has(JsonLdKeyword.CONTEXT.toString())) {
			JsonObject obj =(JsonObject) this.mainJson.remove(JsonLdKeyword.CONTEXT.toString());
			System.out.println("el contexto a procesar es "+obj.toString());
			this.activeContext = new Context(obj);
			for (Map.Entry<String, JsonElement> elementToExpand : this.mainJson.entrySet()) {
				this.expandElement(this.activeContext, elementToExpand.getKey(), elementToExpand.getValue() , true);
			}
		}else
			System.out.println("missing ctx");
	}
	
	
	
	//------------------------algorithms
	
	private String iriExpansion(Context activeContext, String value, boolean flag) {
	// passing active context, value, and true for document relative.
	//The required inputs are an active context and a value to be expanded
		
		//If value is a keyword or null, return value as is.
		
		if(value.equals("") || value ==null || JsonLdKeyword.isKeyword(value) ) {
			return value;
		}
		
		if(this.localContext !=null) {
			//local context contains a key equals value
			}
		return "";
	}
	
	private void valueExpansion(Context activeContext, String activePropertie,JsonElement valueToExpand) {
		//The algorithm takes three required inputs: an active context, an active property, and a value to expand.
		JsonObject dictionary = new JsonObject();
		JsonElement aux = activeContext.hasTypeMapping(activePropertie);
		if(aux!=null) {
			if(aux.getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
				Entry<String, JsonElement> t = aux.getAsJsonObject().entrySet().iterator().next();
				if(t.getValue().isJsonPrimitive()) {
					dictionary.addProperty(JsonLdKeyword.ID.toString(), this.iriExpansion(activeContext, t.getValue().getAsString(), true));
				}
			}
			
		}
				
	}
	
	private JsonElement createTermDefinition(Context activeContext, Context localContext, String term,Map<String, Boolean> def) {
		JsonElement value=null;
	// an active context, a local context, a term, and a map defined.	
		if(JsonLdKeyword.isKeyword(term)){
			//throw error
		}
			
		if(this.defined.containsKey(term)) {
			if(this.defined.get(term).booleanValue()) {
				return value;
			}else {
				//throw error
			}
				
		}else {
			this.defined.put(term, false);
			activeContext.remove(term);
			value = localContext.hasTypeMapping(term);
			//If value is null or value is a dictionary containing the key-value pair @id-null,
			//set the term definition in active context to null, set the value associated with defined's key term to true, and return.
			if(value.isJsonNull() || value ==null) {
				Entry<String, JsonElement> aux = value.getAsJsonObject().entrySet().iterator().next();
				if(aux.getKey().equals(JsonLdKeyword.ID.toString()) && aux.getValue().isJsonNull()) {
					this.defined.put(term, true);
					return value;
	
				}else if(value.isJsonPrimitive()) {
					//Otherwise, if value is a string, convert it to a dictionary consisting of 
					//a single member whose key is @id and whose value is value. Set simple term to true
					
						JsonObject jso = new JsonObject();
						jso.addProperty(JsonLdKeyword.ID.toString(), value.getAsJsonPrimitive().getAsString());
						
				}else if(!value.isJsonObject()) {
					//TODO throw error
				}
			}
			JsonObject term_definition = new JsonObject();
			if(value.getAsJsonObject().has(JsonLdKeyword.TYPE.toString())) {
				
				if(!value.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()).isJsonPrimitive()) {
					//TODO throw error
					return null;
				}else {
					String type = this.iriExpansion(activeContext, value.getAsJsonObject().entrySet().iterator().next().getValue().getAsString(), false);
				}
			}
			
			if(value.getAsJsonObject().has(JsonLdKeyword.REVERSE.toString())) {
				
			}
			
			if(value.getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
				
			}
		
		
		}
		return value;
	}

}

