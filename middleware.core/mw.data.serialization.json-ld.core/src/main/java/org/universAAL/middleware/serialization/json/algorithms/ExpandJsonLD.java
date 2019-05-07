package org.universAAL.middleware.serialization.json.algorithms;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.grammar.IRI;

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
	
	private JsonElement expandElement (String activePropertie, JsonElement value ) {
		//"name": "Mojito"
		//active property = key of the element to be expanded -->name
		//element to expand = value associated with the key -->Mojito
		if(value == null)
			return  null;
		
//		if(activePropertie.equals("@default")){
//			flagExpansion = false;
//		}
//		
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
			JsonElement expanded_item = this.expandElement(activePropertie, value);
			if(activePropertie.equals(JsonLdKeyword.LIST.toString())) {
				
				//the expanded item must not be an array or list object (a list object is a JSONObject that has a @list member)
			}
			if(expanded_item.isJsonArray()) {
				//apend it items to result
			}
	
		}
		
//		//if isnt any of above cases, its a json object
//		if(value.isJsonObject()) {
//			//element is a dictionary
//			JsonObject aux = value.getAsJsonObject();
//			if(aux.has(JsonLdKeyword.CONTEXT.toString())) {
//				//update active context "merging" using the appropiate algorithm
//			}else {
//				Object result_dictionary = new Object();
//			
//				for (Map.Entry<String, JsonElement> i : value.getAsJsonObject().entrySet()) {
//						String expanded_prop = this.iriExpansion( i.getValue());
//						//String expanded_prop = this.expandIRI(element);
//						JsonElement  expaded_value = this.expandElement(activeContext, activePropertie, value, false);
//						if(expanded_prop != null || expanded_prop.contains(":") || JsonLdKeyword.isKeyword(expanded_prop)) {
//							if(JsonLdKeyword.isKeyword(expanded_prop)) {
//								if(activePropertie.equals(JsonLdKeyword.REVERSE.toString())) {
//									//TOOD throw error "invalid_reverse_rpoperty_map"
//								}
//								if(expanded_prop.equals(JsonLdKeyword.TYPE.toString()) && !i.getValue().isJsonPrimitive() && !i.getValue().isJsonArray() ) {
//									//throw error
//								}
//								if(expanded_prop.equals(JsonLdKeyword.VALUE.toString()) && (!i.getValue().isJsonPrimitive() || !i.getValue().isJsonNull()) ) {
//								//throw error	
//								}
//								if(expanded_prop.equals(JsonLdKeyword.LANG.toString())  &&  !i.getValue().isJsonPrimitive()) {
//									//throw error
//								}
//								if(expanded_prop.equals(JsonLdKeyword.INDEX.toString()) && !i.getValue().isJsonPrimitive()) {
//									//throw error
//								}
//								if(expanded_prop.equals(JsonLdKeyword.LIST.toString())) {
//									if(activePropertie ==null || activePropertie.equals(JsonLdKeyword.GRAPH.toString())) {
//										//remove the free floating flag and continue 
//									}else {
//										expaded_value=this.valueExpansion(activeContext, activePropertie, value);
//										if(expaded_value.isJsonObject() && expaded_value.getAsJsonObject().has(JsonLdKeyword.LIST.toString())) {
//											//throw list of lsit error
//										}
//									}
//								}
//								if(expanded_prop.equals(JsonLdKeyword.SET.toString())) {
//									expaded_value = this.expandElement(activeContext, activePropertie, i.getValue(), flagExpansion);
// 								}
//								
//								if(expanded_prop.equals(JsonLdKeyword.REVERSE.toString()) && !(expaded_value.isJsonObject() && expaded_value.getAsJsonObject().has(JsonLdKeyword.LIST.toString()))  ) {
//									
//								}
//								
//							
//							}
//						}
//						
//				}
//			}
//			
//			
//			
//		}
			
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
 
	
	public void expandJsonDocument() {
		System.out.println("expandJsonDocument");
		if(this.mainJson.has(JsonLdKeyword.CONTEXT.toString())) {
			JsonObject obj =(JsonObject) this.mainJson.remove(JsonLdKeyword.CONTEXT.toString());
			System.out.println("el contexto a procesar es "+obj.toString());
			this.activeContext = new Context(obj);
			
			for (Map.Entry<String, JsonElement> elementToExpand : this.mainJson.entrySet()) {
				this.expandElement(elementToExpand.getKey(), elementToExpand.getValue() );
			}
		}else
			System.out.println("missing ctx");
	}
	
	
	
	//------------------------algorithms
	
	private JsonElement iriExpansion( JsonElement term) {
	
		//term is a jsonPrimitive inside a context, this means this term has another mapping and need to be expanded
		JsonElement result=null;

		if(term.isJsonPrimitive()) {
			if(term.isJsonNull() || term ==null || JsonLdKeyword.isKeyword(term.getAsJsonPrimitive().toString()) ) {
				return term;
			}
			if(this.activeContext.hasTypeMapping(term.getAsJsonPrimitive().toString()) != null) {
				if(this.defined.containsKey(term.getAsJsonPrimitive().toString())) {
					if(!this.defined.get(term.getAsJsonPrimitive().toString())) {
					this.createTermDefinition(term);	
					}
				}
			}

		}

	
		return null;
	}
	
	private JsonElement valueExpansion(Context activeContext, String activePropertie,JsonElement valueToExpand) {
		
		//"name": "Mojito"
		//activepropertie = name
		//value to expand "mojito"
		JsonObject dictionary = new JsonObject();
		JsonElement aux = this.activeContext.hasTypeMapping(activePropertie);
		if(aux!=null) {
			if(aux.isJsonObject()) {
			
			if(aux.getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
				Entry<String, JsonElement> t = aux.getAsJsonObject().entrySet().iterator().next();
				if(t.getValue().isJsonPrimitive()) {
					dictionary.add(JsonLdKeyword.ID.toString(), this.iriExpansion( t.getValue()));
				}
			}
			
			if(aux.getAsJsonObject().has(JsonLdKeyword.VOCAB.toString())) {
				Entry<String, JsonElement> t = aux.getAsJsonObject().entrySet().iterator().next();
				if(t.getValue().isJsonPrimitive()) {
					dictionary.add(JsonLdKeyword.ID.toString(), this.iriExpansion(t.getValue()));
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
	
	private JsonElement createTermDefinition(JsonElement term) {
		JsonElement value=null;
		JsonObject dictionary = new JsonObject();
		JsonElement definition;
		
		if(term.isJsonPrimitive()) {
		
			if(JsonLdKeyword.isKeyword(term.getAsJsonPrimitive().toString())){
				//TODO throw error
				return null;
			}
				
			if(this.defined.containsKey(term)) {
				if(this.defined.get(term).booleanValue()) {
					return value;
				}else {
					//throw error
				}
					
			}
			
			this.defined.put(term.getAsJsonPrimitive().toString(), false);
			//value has a copy of the value associated to term into local context
			value = this.activeContext.hasTypeMapping(term);
			if(value.isJsonPrimitive()) {
				dictionary.add(JsonLdKeyword.ID.toString(), value);
				return dictionary;
			}else if(value.isJsonObject()) {
				if(value.getAsJsonObject().has(JsonLdKeyword.TYPE.toString())) {
					JsonElement type = value.getAsJsonObject().get(JsonLdKeyword.TYPE.toString());
					if(type.isJsonPrimitive()) {
						type = this.iriExpansion(type);
						definition = type;
					}else {
						//TODO throw error
						return null;
					}
				}
				if(value.getAsJsonObject().has(JsonLdKeyword.REVERSE.toString())) {
					if(  value.getAsJsonObject().has(JsonLdKeyword.ID.toString()) || value.getAsJsonObject().has("@nest") ) {
						//TODO throw error
						return null;
					}
					if(!value.getAsJsonObject().get(JsonLdKeyword.REVERSE.toString()).isJsonPrimitive()) {
						//TODO throw error
						return null;
					}else {
						    definition=this.iriExpansion(value);
						    if(definition.isJsonPrimitive()) {
						    	if(IRI.isAbsolute(definition.getAsJsonPrimitive().toString()) ||
						    		definition.getAsJsonPrimitive().toString().startsWith(JsonLdKeyword.BLANK_NODE.toString())) {
						    		//TODO throw error
						    		return null;
						    	}
						    }else {
						    	//TODO throw error
						    	return null;
						    }
					}
					if(value.getAsJsonObject().has(JsonLdKeyword.CONTAINER.toString())) {
						//TODO  complete this. confusing documentation
					}
				}
				if(value.getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
					if(!value.getAsJsonObject().get(JsonLdKeyword.ID.toString()).equals(term)){
						if(!value.getAsJsonObject().get(JsonLdKeyword.ID.toString()).isJsonPrimitive()) {
							//TODO throw error
							return null;
						}else {
							definition=this.iriExpansion(value.getAsJsonObject().get(JsonLdKeyword.ID.toString()));
						}
					}
					
				}
				if(term.getAsJsonPrimitive().toString().contains(":")) {
					
				}
				if(term.getAsJsonPrimitive().toString().equals(JsonLdKeyword.TYPE.toString())){
					
				}
				
				
				
				
			}else {
				//TODO throw error
			}	
		}

			
			
		return value;
	}

}

