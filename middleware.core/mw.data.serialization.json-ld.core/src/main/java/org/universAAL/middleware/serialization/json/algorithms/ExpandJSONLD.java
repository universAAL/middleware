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
				aux_obj.add(entry.getKey(),entry.getValue());
			}
			result.add(aux_obj);
		}else
			System.out.println("missing context");
		
	}
	
	private JsonElement expandElement(String key, JsonElement value, boolean array_state) {
		

		JsonElement aux_a =iriExpansion(new JsonPrimitive(key));
		JsonObject expanded_element = new JsonObject();
		if(value instanceof JsonPrimitive) {

					JsonElement expandedKey =this.iriExpansion(new JsonPrimitive(key));
					
					if(expandedKey instanceof JsonPrimitive) {

						if(!array_state) {
							
							JsonObject aux_obj = new JsonObject();
							JsonArray aux_array = new JsonArray();

							if(expandedKey.getAsJsonPrimitive().getAsString().startsWith("@")) {// is a keyword
							//if(JsonLdKeyword.isKeyword(expandedKey.getAsJsonPrimitive().getAsString())) {
								if(expandedKey.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.ID.toString())) {
									expanded_element.add(JsonLdKeyword.ID.toString(), value);
								}
								if(expandedKey.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.TYPE.toString())) {

									JsonArray local_aux = new JsonArray();
										if(value.isJsonArray()) {
											for (JsonElement jsonElement : value.getAsJsonArray()) {
												if(jsonElement.isJsonPrimitive()) {
													aux_array.add(jsonElement);
												}else {
													log.debug("not primitive");
													break;
												}
											}
											log.debug("aux_array_A "+local_aux);
										}else if(value.isJsonPrimitive()) {
											
											local_aux.add(value);
										}else {
											//TODO throw error. only can be a string or empty object
										}
										
										expanded_element.add(JsonLdKeyword.TYPE.toString(), local_aux);
										
								}
							}else {
								aux_obj.add(JsonLdKeyword.VALUE.toString(), value);
								aux_array.add(aux_obj);
								expanded_element.add(expandedKey.getAsJsonPrimitive().getAsString(), aux_array);	
							}
						}else{
								expanded_element.add(JsonLdKeyword.VALUE.toString(), value);
						}				
						return expanded_element ;
					}else if(expandedKey instanceof JsonObject) {

		
						JsonObject aux_obj = new JsonObject();
						JsonArray aux_array = new JsonArray();
						//aux_obj.add(JsonLdKeyword.VALUE.toString(), value);
						if(expandedKey.getAsJsonObject().has(JsonLdKeyword.TYPE.toString())) {

							if(expandedKey.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()).getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.ID.toString())) {
								aux_obj.add(JsonLdKeyword.ID.toString(), value);

							}else {
								aux_obj.add(JsonLdKeyword.VALUE.toString(), value);
								aux_obj.add(  JsonLdKeyword.TYPE.toString()  , expandedKey.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()));//expand TYPE member as IRI example xsd:string
							}
						}
						aux_array.add(aux_obj);
						expanded_element.add(key, aux_array);
					
					}else if(expandedKey instanceof JsonNull) {
						log.fatal("null expanded prop");
					}
					return expanded_element;
		}
		if(value instanceof JsonObject) {

			
			/*
			 * JsonElement expanded_value;
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
			
		*/
		//{"latitude":"40.75","longitude":"73.98"}
			log.debug("iterating over "+value);
			JsonArray aux_array = new JsonArray();
			JsonObject aux_obj = new JsonObject();
				for (Entry<String, JsonElement> iterable_element : value.getAsJsonObject().entrySet()) {
					aux_obj = this.expandElement(iterable_element.getKey(),iterable_element.getValue(),false).getAsJsonObject();
					aux_array.add(aux_obj);
					
				}
				
				expanded_element.add(aux_a.getAsString(), aux_array);
				
				//return expanded_element;
						 
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
		JsonElement expanded=null;


		if(key.getAsJsonPrimitive().getAsString().startsWith("@") || key.isJsonNull()) {

			return key;	
		}
		
		if(key.isJsonPrimitive()) {


			if(key.getAsJsonPrimitive().getAsString().contains(":")) {

				log.debug(key+"has :");
				String candidate = key.getAsJsonPrimitive().getAsString();
				prefix = key.getAsJsonPrimitive().getAsString().substring(0, key.getAsJsonPrimitive().getAsString().indexOf(":"));
				sufix = key.getAsJsonPrimitive().getAsString().substring(key.getAsJsonPrimitive().getAsString().indexOf(":")+1);
				
				if(prefix.contains("_") || sufix.startsWith("//")) {
					//return key;
					log.debug("b_node");
				}
				log.debug("prefix="+prefix+" sufix="+sufix);
				if(this.context.hasTerm(prefix)) {
					log.debug("context gas the prefix "+prefix);
					String generatedIRI= this.iriExpansion(new JsonPrimitive(prefix)).getAsString();
					log.debug("generatedIRI "+generatedIRI+sufix);
					expanded = new JsonPrimitive(generatedIRI+candidate.substring(candidate.lastIndexOf(":")+1));
					log.debug("expanded "+expanded);
				}else {
					log.debug("prefix inexistent in context="+prefix);//to the next key (?)
					expanded = null;
				}
			}else if(this.context.hasTerm(key)) {
					if(this.context.getTermValue(key).isJsonObject()) {
						/*
						//{"@id":"http://schema.org/latitude","@type":"xsd:float"}
						if(this.context.getTermValue(key).getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
							
							expanded = this.context.getTermValue(key).getAsJsonObject().get(JsonLdKeyword.ID.toString());
	
						}else {
							log.debug("missing id"); 
						}
						*/
						expanded = this.context.getTermValue(key);
					}else if(this.context.getTermValue(key).isJsonPrimitive()) {
							expanded = this.context.getTermValue(key);
						}
	
					}
		}else {
			System.out.println("missink key...skipping");
		}
		log.debug("returning "+expanded);
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
