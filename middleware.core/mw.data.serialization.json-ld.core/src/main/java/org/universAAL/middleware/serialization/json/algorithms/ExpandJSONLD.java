package org.universAAL.middleware.serialization.json.algorithms;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import javax.print.attribute.HashAttributeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.grammar.ContextDefinition;
import org.universAAL.middleware.serialization.json.grammar.IRI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.apache.log4j.PropertyConfigurator;


public class ExpandJSONLD {
	private ContextDefinition context=null;
	private JsonObject jsonToExpand= null; 
	private Map<JsonElement, Boolean> defined = new HashMap<JsonElement, Boolean>(); 
	JsonParser parser = new JsonParser();
	JsonArray result = new JsonArray();
	private final static Logger log = Logger.getLogger(ExpandJSONLD.class);
	

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
		log.debug("expanding");
		if(this.context ==null) {
			if(this.jsonToExpand.has(JsonLdKeyword.CONTEXT.toString())) {
				this.context = new ContextDefinition(this.jsonToExpand.remove(JsonLdKeyword.CONTEXT.toString()));	
			}
			
		}
		if(this.context !=null) {
			JsonObject aux_obj = new JsonObject();
			for (Entry<String, JsonElement> element: this.jsonToExpand.entrySet()) {
				
				Entry<String, JsonElement> entry = this.expandElement(element.getKey(), element.getValue(),false).getAsJsonObject().entrySet().iterator().next();
				if(entry.getValue().isJsonPrimitive()) {
					
				}
				aux_obj.add(entry.getKey(),entry.getValue());
			}
			result.add(aux_obj);
		}else
			System.out.println("missing context");
		
	}
	
	private JsonElement expandElement(String key, JsonElement value, boolean array_state) {
		log.debug("expandElement key="+key+" value="+value);
		JsonObject expanded_element = new JsonObject();
		 //"name": "Mojito"
		if(value instanceof JsonPrimitive) {
			JsonElement elemet_id =null;
			JsonElement expandedKey =this.iriExpansion(new JsonPrimitive(key));
			//expandedKey = http://rdf.data-vocabulary.org/#name
			if(expandedKey instanceof JsonPrimitive) {
				
				if(!array_state) {
					JsonObject aux_obj = new JsonObject();
					JsonArray aux_array = new JsonArray();
					aux_obj.add(JsonLdKeyword.VALUE.toString(), value);
					aux_array.add(aux_obj);
					expanded_element.add(expandedKey.getAsJsonPrimitive().getAsString(), aux_array);
				}else{
					expanded_element.add(JsonLdKeyword.VALUE.toString(), value);
				}
				
				
				return expanded_element ;
			}
			if(expandedKey instanceof JsonObject) {
				expanded_element.add(JsonLdKeyword.TYPE.toString(), this.iriExpansion(expandedKey.getAsJsonObject().get(JsonLdKeyword.TYPE.toString())) );//TODO control if value has :
				expanded_element.add(JsonLdKeyword.VALUE.toString(),value);//TODO control if value has :
			}
			return expanded_element;
		}
		if(value instanceof JsonObject) {
		log.debug("processing object");
			JsonElement expanded_value;
			JsonElement expanded_property= this.iriExpansion(new JsonPrimitive(key));
			
			if(!key.equals("@context")) {
				if(!expanded_property.isJsonNull() ) {
					if(JsonLdKeyword.isKeyword(expanded_property.getAsString()) ) {
						
						if(key.equals(JsonLdKeyword.REVERSE.toString())) {
							//TODO throw error
						}
						if(expanded_element.getAsString().equals(JsonLdKeyword.ID.toString()) && !value.isJsonPrimitive()) {
							//TODO throw error
						}else {
							 expanded_value = this.iriExpansion(value);
						}
						if(expanded_element.getAsString().equals(JsonLdKeyword.TYPE.toString())) {
							boolean state=true;
							if(value.isJsonArray()) {
								for (JsonElement jsonElement : value.getAsJsonArray()) {
									if(!jsonElement.isJsonPrimitive()) {
										state = false;	
										break;
									}
								}
							}else if(!value.isJsonPrimitive()) {
								//TODO throw error
							}else {
								expanded_value = this.iriExpansion(value);
							}
						}
						if(expanded_element.getAsString().equals(JsonLdKeyword.GRAPH.toString())) {
							//expanded_value = this.expandElement(key, key, array_state);
						}
						if(expanded_element.getAsString().equals(JsonLdKeyword.VALUE.toString()) && value.isJsonPrimitive()) {
							//TODO throw error (and abort process)
						}else {
							expanded_value = value;
							if(expanded_value.isJsonNull()) {
								//TODO interpret doc and implement
							}
						}
						if(expanded_element.getAsString().equals(JsonLdKeyword.LANG.toString()) && !value.isJsonPrimitive()) {
							//TODO throw error invalid language-tagged string
							log.fatal("invalid language-tagged string");
						}
						if(expanded_element.getAsString().equals(JsonLdKeyword.INDEX.toString())) {
							if( value.isJsonPrimitive())
								expanded_value = value;
							else {
								//TODO throw error
							}
						}
						if(expanded_element.getAsString().equals(JsonLdKeyword.LIST.toString())) {
							
							if( key.equals(JsonLdKeyword.GRAPH.toString()) || new JsonPrimitive(key).isJsonNull()) {
								//TODO continue with the next key
							}else {
								expanded_value = this.expandElement(key, value,false);
								if(expanded_value.isJsonObject()) {
									if(expanded_value.getAsJsonObject().has(JsonLdKeyword.LIST.toString())) {
										log.fatal("list of lists");
									}
								}
							}
						}
						if(expanded_element.getAsString().equals(JsonLdKeyword.SET.toString())) {
							expanded_value = this.expandElement(key,value, false);
							
						}
						//9.4.11 item in doc
						if(expanded_element.getAsString().equals(JsonLdKeyword.REVERSE.toString()) && !value.isJsonObject()) {
							//TODO throw invalid @reverse
							log.fatal("invalid @reverse");
						}else {
							expanded_value = this.expandElement(key, value, array_state);
							//if(expanded_value.)
						}
					}
				}
			}
			
		
				for (Entry<String, JsonElement> iterable_element : value.getAsJsonObject().entrySet()) {
					JsonElement exp_key =this.iriExpansion(new JsonPrimitive(iterable_element.getKey()));
					JsonArray aux_array = new JsonArray();
					aux_array.add(this.expandElement(iterable_element.getKey(), iterable_element.getValue(),false));
					
					if(exp_key.isJsonPrimitive()) {
						expanded_element.add(exp_key.getAsJsonPrimitive().getAsString(),aux_array);
					}
					
					if(exp_key.isJsonObject()){
						expanded_element.add(exp_key.getAsJsonObject().get(JsonLdKeyword.ID.toString()).toString(),aux_array);
					}
					
				}
				
				return expanded_element;
						 
		}
		
		if(value instanceof JsonArray) {
			JsonElement elementID;
			JsonArray res = new JsonArray();
			
			JsonElement key_expanded = this.iriExpansion(new JsonPrimitive(key));
			if(key_expanded instanceof JsonPrimitive) {
				JsonElement result_id = null ;
				for (int i = 0; i < value.getAsJsonArray().size(); i++) {
					
					if(value.getAsJsonArray().get(i).isJsonObject()) {
						//attach id into result
						result_id=value.getAsJsonArray().get(i).getAsJsonObject().get(JsonLdKeyword.ID.toString());
					}
					JsonObject aux =this.expandElement(key, value.getAsJsonArray().get(i),true).getAsJsonObject();
					if(result_id!=null) {
						aux.add(JsonLdKeyword.ID.toString(), result_id);
					}
					
					//JsonElement t = this.expandElement(key, value.getAsJsonArray().get(i),true);
					res.add(aux);
				}

				expanded_element.add(key_expanded.getAsJsonPrimitive().getAsString(), res);
			}
		}
		
		return expanded_element;
	}
	
	private JsonElement iriExpansion(JsonElement key) {
		String prefix="",sufix="";
		
		if(this.context.hasTerm(key)) {
			if(this.defined.containsValue( this.context.getTermValue(key)) ) {
				if(this.defined.get(this.context.getTermValue(key)).booleanValue()!=true){
					//this.createTermDefinition(key);
				}
			}else {
				
			}
				
			
		}
		
		
		if(key.getAsJsonPrimitive().getAsString().contains(":")) {
			prefix = key.getAsJsonPrimitive().getAsString().substring(0, key.getAsJsonPrimitive().getAsString().indexOf(":"));
			sufix = key.getAsJsonPrimitive().getAsString().substring(key.getAsJsonPrimitive().getAsString().indexOf(":")+1);
			log.debug("prefix="+prefix);
			log.debug("sufix="+sufix);
			
		}
		
		
		if(prefix.contains("_") || sufix.startsWith("//")) {
			return key;
		}
		//BUG fix JsonLdKeyword.isKeyword(key.getAsJsonPrimitive().getAsString()); 
		
		if(JsonLdKeyword.isKeyword(key.getAsJsonPrimitive().getAsString())|| key.isJsonNull()) {
			return key;	
		}
		
		JsonElement expanded=null;
		if(key.isJsonPrimitive()) {
			if(IRI.isCompact(context, key.getAsJsonPrimitive().getAsString())) {
				String candidate = key.getAsJsonPrimitive().getAsString();
				String aux = candidate.substring(0, candidate.lastIndexOf(":"));
				if(this.context.hasTerm(aux)) {
					String generatedIRI= this.iriExpansion(new JsonPrimitive(aux)).getAsString();
					expanded = new JsonPrimitive(generatedIRI+candidate.substring(candidate.lastIndexOf(":")));
					
				}else {
					//to the next key (?)
				}
				
			}else {
				expanded = this.context.getTermValue(key);
			}
		}else {
			System.out.println("missink key...skipping");
		}
		return expanded;
	}

	
	
	
	private JsonElement createTermDefinition(JsonElement term) {
		JsonElement value;
		if(this.defined.containsKey(term)) {
			if(this.defined.get(term).booleanValue()) {
				//return
			}else {
				log.fatal("a cyclic IRI mapping");
			}
		}else{
			this.defined.put(term, false);
			//Initialize value to a copy of the value associated with the member term in local context.
			value = this.context.getTermValue(term);
			
			if(value.isJsonNull()) {
				this.defined.put(term, true);
				//return
			}else if(value.isJsonObject()) {
				if( value.getAsJsonObject().has("@id") && value.getAsJsonObject().get(JsonLdKeyword.ID.toString()).isJsonNull()  ) {
					this.defined.put(term, true);
					//return
				}else {
					if(value.isJsonPrimitive()) {
						JsonObject obj = new JsonObject();
						obj.add(JsonLdKeyword.ID.toString(), value);
						value = obj;
					}else if(value.isJsonObject()){
						JsonElement definition;
						//is dictionary
						if(value.getAsJsonObject().has(JsonLdKeyword.TYPE.toString())) {
							JsonElement type= value.getAsJsonObject().get(JsonLdKeyword.TYPE.toString());
							if(type.isJsonPrimitive()) {
								type = this.iriExpansion(type);
								if(type.isJsonPrimitive()) {
									if(type.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.ID.toString()) || type.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.VOCAB.toString()) || IRI.isAbsolute(type.getAsJsonPrimitive().getAsString())){
										definition = type;
									}else {
										log.fatal(" invalid type mapping ");
									}
								}
							}else {
								log.fatal(" invalid type mapping ");
							}
						}
						
						if(value.getAsJsonObject().has(JsonLdKeyword.REVERSE.toString())) {
							if(value.getAsJsonObject().has(JsonLdKeyword.ID.toString() ) || value.getAsJsonObject().has("@nest")) {
								log.fatal("invalid reverse property...abort process");
							}
							if(!value.getAsJsonObject().get(JsonLdKeyword.REVERSE.toString()).isJsonPrimitive()) {
								log.fatal("invalid IRI mapping ...abort process");
							}else {
								definition = this.iriExpansion(value.getAsJsonObject().get(JsonLdKeyword.REVERSE.toString()));
								
								if(IRI.isAbsolute(definition.getAsJsonPrimitive().getAsString()) ||  definition.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.BLANK_NODE) ) {
									log.fatal("invalid IRI mapping..abort process");
								}
							}
							if(value.getAsJsonObject().has(JsonLdKeyword.CONTAINER.toString())) {
								definition =value.getAsJsonObject().get(JsonLdKeyword.CONTAINER.toString());
								if(!definition.getAsJsonPrimitive().isJsonNull() ) {
									if(definition.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.SET.toString()) || definition.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.INDEX.toString()) ) {
										log.fatal("invalid reverse property...processing is aborted");
									}
								}else {
									log.fatal("invalid reverse property...processing is aborted");
								}
							}
							
							//TODO implement 14.5 and 14.6
						}
						
						if(value.getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
							if(!value.getAsJsonObject().get(JsonLdKeyword.ID.toString()).equals(term)) {
								if(!value.getAsJsonObject().get(JsonLdKeyword.ID.toString()).isJsonPrimitive()) {
									log.fatal(" invalid IRI mapping error has been detected and processing is aborted");
								}else {
									definition = this.iriExpansion(value.getAsJsonObject().get(JsonLdKeyword.ID.toString()));
									if(JsonLdKeyword.isKeyword(definition.getAsJsonPrimitive().getAsString()) || 
											IRI.isAbsolute(definition.getAsJsonPrimitive().getAsString()) ||
											value.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.BLANK_NODE)){
										log.fatal("invalid IRI mapping error has been detected and processing is aborted.");
									}
								}
							}
						}
						
						if(term.getAsJsonPrimitive().getAsString().contains(":")) {
							String prefix =term.getAsJsonPrimitive().getAsString().substring(0,term.getAsJsonPrimitive().getAsString().lastIndexOf(":"));
							this.createTermDefinition(new JsonPrimitive(prefix));
						}
					}else {
						log.fatal("error");
					}
				}
			}
			
		}
		return new JsonPrimitive("r");
	}



	public JsonArray getExpandedJson() {
		return this.result;
	}
}