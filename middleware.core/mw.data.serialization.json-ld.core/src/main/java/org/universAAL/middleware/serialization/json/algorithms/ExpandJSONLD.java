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
		if(this.context !=null && this.jsonToExpand.has(JsonLdKeyword.CONTEXT.toString())) {
			//TODO merge contexts???
		}
		
		if(this.context !=null) {
			for (Entry<String, JsonElement> element: this.jsonToExpand.entrySet()) {
				//System.out.println("element "+element);
				this.valueExpansion(element.getKey(), element.getValue());
			}
		}else
			System.out.println("missing context");

		
		
		
		
	}
	
	private JsonElement valueExpansion(String activePropertie,JsonElement toExpand) {
		//check if active property has a mapping in context
		//expand the key and them iterate over given item
		
		JsonObject result_obj = new JsonObject();
		JsonElement expandedKey =this.iriExpansion(new JsonPrimitive(activePropertie),true);
		
		if(this.context.hasTerm(activePropertie)) {
			
			
		
			if(toExpand.isJsonPrimitive()) {
				JsonElement aux = this.context.getTermValue(activePropertie);
				
				if(aux.isJsonPrimitive()) {
					
					result_obj.add(JsonLdKeyword.VALUE.toString(), toExpand);
					return result_obj;
				}else if(aux.isJsonObject()) {
					for (Entry<String, JsonElement> iterable_element : aux.getAsJsonObject().entrySet()) {
						if(iterable_element.getKey().equals(JsonLdKeyword.ID.toString())){
							
							result_obj.add(JsonLdKeyword.ID.toString(), this.iriExpansion(toExpand,false));
							
						}else if(iterable_element.getKey().equals(JsonLdKeyword.VOCAB.toString())){
							
							result_obj.add(JsonLdKeyword.ID.toString(), this.iriExpansion(toExpand,true));
						}else {
							
							result_obj.add(JsonLdKeyword.TYPE.toString(), iterable_element.getValue());
						}
					}
					
					return result_obj;
				}else
					return result_obj;
			}else if(toExpand.isJsonObject()) {
	
				JsonElement expanded_property =null;				
				for (Entry<String, JsonElement> item : toExpand.getAsJsonObject().entrySet()) {
					//"step": 1
					this.valueExpansion(item.getKey(), item.getValue());
					JsonObject jso = new JsonObject();
							
					if(!item.getKey().equals(JsonLdKeyword.CONTEXT.toString())) {
						
						expanded_property = this.iriExpansion(new JsonPrimitive(item.getKey()),true);	
						if(expanded_property.isJsonObject()) {
							JsonArray auxiliar_array = new JsonArray();
							jso.add(JsonLdKeyword.TYPE.toString(), expanded_property.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()));
							jso.add(JsonLdKeyword.VALUE.toString(), item.getValue());
							auxiliar_array.add(jso);
							
							result_obj.add(expanded_property.getAsJsonObject().get(JsonLdKeyword.ID.toString()).toString(), auxiliar_array);
						}
						
						if(expanded_property.isJsonArray() ) {
							
						}
						if(expanded_property.isJsonPrimitive() ) {
							
							jso.add(JsonLdKeyword.VALUE.toString(), item.getValue());
							JsonArray auxiliar_array = new JsonArray();
							auxiliar_array.add(jso);
							result_obj.add(expanded_property.getAsString(), auxiliar_array);
						}
					}
					
				}
				return result_obj;
			}else if(toExpand.isJsonArray()) {
				JsonArray auxiliar_array = new JsonArray(); 			
				for (int i = 0; i < toExpand.getAsJsonArray().size(); i++) {
					JsonElement expanded_item = this.valueExpansion(activePropertie, toExpand.getAsJsonArray().get(i));
					auxiliar_array.add(expanded_item);
				}
				result_obj.add(expandedKey.getAsString(), auxiliar_array);
			
			}
		}
	result.add(result_obj);	
	return result;
	}
	
	private JsonElement iriExpansion(JsonElement toExpand,boolean vocabState) {
		

		
		if(toExpand.isJsonPrimitive()) {
			if(vocabState){
				if(this.context.hasTerm(toExpand)) {
		
					//return the associated IRI mapping
					JsonElement element =this.context.getTermValue(toExpand.getAsJsonPrimitive().getAsString());//return jsonPrimitive with the iri
		
					return element;
				}
		
		
			}
			
			if(this.context.hasTerm(toExpand.getAsJsonPrimitive().toString()) && this.defined.get(toExpand).booleanValue()!=true) {
				//invoke the Create Term Definition algorithm
				this.createTermDefinition(toExpand);
			}
			
			if(toExpand.getAsJsonPrimitive().toString().contains(":")) {
				
			}
	
						
		}else
			System.out.println("somethig wrong");
				
		
		
		return null;
	}
	
	private JsonElement createTermDefinition(JsonElement toExpand){

		JsonElement definition ;
		
		if(toExpand.isJsonPrimitive()) {
			if(this.defined.containsKey(toExpand)) {
				if(this.defined.get(toExpand)) {
					return toExpand;
				}else {
					//TODO review doc
					return null;
				}
			}
			this.defined.put(toExpand, false);
			JsonElement value = this.context.getTermValue(toExpand.getAsJsonPrimitive().toString());
			
			if(JsonLdKeyword.isKeyword(toExpand.getAsJsonPrimitive().toString())) {
				//TOOD throw err
				return null;
			}
			
			if(value.isJsonNull()) {
				System.out.println("json null");
			}
			
			if(value.isJsonObject()) {
				Entry<String, JsonElement> t = value.getAsJsonObject().entrySet().iterator().next();
				if(t.getKey().equals(JsonLdKeyword.ID.toString()) && t.getValue().isJsonNull()) {
					this.context.updateElement(t.getKey(), new JsonNull());
				}

			}
			
			if(value.isJsonPrimitive()) {
				JsonObject dictionary = new JsonObject();
				dictionary.add(JsonLdKeyword.ID.toString(), value);
				value = dictionary;
			}
			
			if(value.isJsonObject()) {
					JsonObject aux = value.getAsJsonObject();
					
					if(aux.has(JsonLdKeyword.TYPE.toString())) {
						JsonElement type = aux.get(JsonLdKeyword.TYPE.toString());
						if(!type.isJsonPrimitive()) {
							//TODO throw error 
							return null;
						}else {
							type = this.iriExpansion(type,true);
							definition = type;
							//TODO check this 
							/*
							 * neither @id, nor @vocab, nor, if processing mode is json-ld-1.1,
							 *  @json nor @none, nor an absolute IRI, an invalid type mapping error has been detected and processing is */
						}
					}
					
					if(aux.has(JsonLdKeyword.REVERSE.toString())) {
							if(aux.has(JsonLdKeyword.ID.toString()) || aux.has("@nest")) {
								//TODO throw invalid nested propertie
								return null;
							}
							
							if(!aux.get(JsonLdKeyword.REVERSE.toString()).isJsonPrimitive()) {
								//TODO throw invalid IRI mapping
								return null;
							}else{
								definition=this.iriExpansion(aux.get(JsonLdKeyword.REVERSE.toString()),true);
								if(definition.isJsonPrimitive()) {
									if(!( IRI.isAbsolute(definition.getAsJsonPrimitive().toString()) || 
										definition.getAsJsonPrimitive().toString().startsWith(JsonLdKeyword.BLANK_NODE.toString()) )) {
										//TODO throw invalid IRI mapping
										return null;
										} 
								}
								
								
								}
							if(aux.has(JsonLdKeyword.CONTAINER.toString())) {
							definition = aux.get(JsonLdKeyword.CONTAINER.toString());
								/*
								 * if its value is neither @set, nor @index, nor null, 
								 * an invalid reverse property error has been detected (reverse properties only support set- and index-containers) 
								 * and processing is aborted.*/
								if(definition.isJsonPrimitive()) {
									if(definition.getAsJsonPrimitive().equals(JsonLdKeyword.SET.toString())  ||
											definition.getAsJsonPrimitive().equals(JsonLdKeyword.INDEX.toString())) {
										//TODO return reverse propertie error 
										return null;
									}
								}else if(definition.isJsonNull()) {
									//TODO return reverse propertie error 
									return null;
								}
							}
							
						this.context.updateElement(toExpand.getAsJsonPrimitive().toString(), value);
						this.defined.put(toExpand, true);
						return definition;
						}
					
					if(aux.has(JsonLdKeyword.ID.toString())) {
						if(!aux.get(JsonLdKeyword.ID.toString()).equals(toExpand)) {
							if(!aux.get(JsonLdKeyword.ID.toString()).isJsonPrimitive()) {
								//TODO throw  invalid IRI mapping 
								return null;
							}else {
								definition = this.iriExpansion(aux.get(JsonLdKeyword.ID.toString()),true);
								if(definition.isJsonPrimitive()) {
									if( !(JsonLdKeyword.isKeyword(definition.getAsJsonPrimitive().toString()) ||
											IRI.isAbsolute(definition.getAsJsonPrimitive().toString())) ||
											definition.getAsJsonPrimitive().toString().startsWith(JsonLdKeyword.BLANK_NODE.toString())) {
										//TODO  throw invalid IRI mapping
										return null;
									}
								}
								
							}
						}
					}
					
					if(toExpand.getAsJsonPrimitive().toString().contains(":")) {
						if(IRI.isCompact(this.context, toExpand.getAsJsonPrimitive().toString())) {
							//TODO complete this step
							//this.createTermDefinition(toExpand);
						}
						
					}
						
						
				}
		}
					
			
		
		
		
		
		return null;
	}
	
	
public JsonArray getExpandedJson() {
	return this.result; 
}

}
