package org.universAAL.middleware.serialization.json.algorithms;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import javax.print.attribute.HashAttributeSet;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
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
//	private final static Logger log = Logger.getLogger(ExpandJSONLD.class);
	

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
//		log.debug("expanding");
		LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","expanding");
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
		System.out.println("key "+key+" value "+value);
		LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","expanding");
		JsonElement aux_a =iriExpansion(new JsonPrimitive(key));
		JsonObject expanded_element = new JsonObject();
		if(value instanceof JsonPrimitive) {

					JsonElement expandedKey =this.iriExpansion(new JsonPrimitive(key));
					
					if(expandedKey instanceof JsonPrimitive) {
//						log.debug("primitive expandedKey "+expandedKey);
						LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","expanding");
						if(!array_state) {
							
							JsonObject aux_obj = new JsonObject();
							JsonArray aux_array = new JsonArray();

							if(expandedKey.getAsJsonPrimitive().getAsString().startsWith("@")) {// is a keyword
								
							//if(JsonLdKeyword.isKeyword(expandedKey.getAsJsonPrimitive().getAsString())) {
								if(expandedKey.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.ID.toString())) {
									
									expanded_element.add(JsonLdKeyword.ID.toString(), value);//TODO may be need to be expanded as IRI
								}
								if(expandedKey.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.TYPE.toString())) {
									
									JsonArray local_aux = new JsonArray();
										if(value.isJsonArray()) {
											
											for (JsonElement jsonElement : value.getAsJsonArray()) {
												if(jsonElement.isJsonPrimitive()) {
													//aux_array.add(jsonElement);
													aux_array.add(this.iriExpansion(jsonElement));
												}else {
//													log.debug("not primitive");
													LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","not primitive");

													break;
												}
											}
											//log.debug("aux_array_A "+local_aux);
											LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","local_aux");
										}else if(value.isJsonPrimitive()) {
											
											local_aux.add(this.iriExpansion(value));
											//log.debug("local_aux="+ local_aux);
											LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","local_aux");
											//local_aux.add(value);
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
//						log.debug("the expansion for ke "+key+" is object="+expandedKey);
						LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","is object="+expandedKey);

						JsonObject aux_obj = new JsonObject();
						JsonArray aux_array = new JsonArray();
						//aux_obj.add(JsonLdKeyword.VALUE.toString(), value);
						if(expandedKey.getAsJsonObject().has(JsonLdKeyword.TYPE.toString())) {
							
							JsonPrimitive js_p =expandedKey.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()).getAsJsonPrimitive();
							if(js_p.getAsString().equals(JsonLdKeyword.ID.toString())) {
								aux_obj.add(JsonLdKeyword.ID.toString(), value);
							}else {
								aux_obj.add(JsonLdKeyword.VALUE.toString(), value);
								//aux_obj.add(  JsonLdKeyword.TYPE.toString()  , expandedKey.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()));//expand TYPE member as IRI example xsd:string
								aux_obj.add(  JsonLdKeyword.TYPE.toString()  , this.iriExpansion(js_p));//expand TYPE member as IRI example xsd:string
							}
						}
						aux_array.add(aux_obj);
						
						expanded_element.add(key, aux_array);
					
					}else if(expandedKey instanceof JsonNull) {
						//log.fatal("null expanded prop");
						LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","null expanded prop");
					}
					return expanded_element;
		}
		if(value instanceof JsonObject) {

			LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","iterating over "+value);
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
			System.out.println("array");
			JsonElement elementID;
			JsonArray res = new JsonArray();
			JsonElement key_expanded = this.iriExpansion(new JsonPrimitive(key));
			//in type case , key_expanded is the same key...and it is evaluated as primitive (string)
			if(key_expanded instanceof JsonPrimitive) {
				JsonElement result_id = null ;
				for (int i = 0; i < value.getAsJsonArray().size(); i++) {
					
					if(value.getAsJsonArray().get(i).isJsonObject()) {
						result_id=value.getAsJsonArray().get(i).getAsJsonObject().get(JsonLdKeyword.ID.toString());
						JsonObject aux =this.expandElement(key, value.getAsJsonArray().get(i),true).getAsJsonObject();
						if(result_id!=null) {
							aux.add(JsonLdKeyword.ID.toString(), result_id);
						}
						res.add(aux);

					}else if(value.getAsJsonArray().get(i).isJsonPrimitive()) {
						res.add(this.iriExpansion(value.getAsJsonArray().get(i)));						
						//result_id=value.getAsJsonArray().get(i).getAsJsonObject().get(JsonLdKeyword.ID.toString());
					}
					

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
				String candidate = key.getAsJsonPrimitive().getAsString();
				prefix = key.getAsJsonPrimitive().getAsString().substring(0, key.getAsJsonPrimitive().getAsString().indexOf(":"));
				sufix = key.getAsJsonPrimitive().getAsString().substring(key.getAsJsonPrimitive().getAsString().indexOf(":")+1);
				
				if(prefix.contains("_") || sufix.startsWith("//")) {
					
					LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","b_node");

					return key;
				}

				if(this.context.hasTerm(prefix)) {

					String generatedIRI= this.iriExpansion(new JsonPrimitive(prefix)).getAsString();

					expanded = new JsonPrimitive(generatedIRI+candidate.substring(candidate.lastIndexOf(":")+1));
					LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","null expanded prop");

				}else {
					LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","null expanded prop");

					expanded = key.getAsJsonPrimitive();
				}
			}else if(this.context.hasTerm(key)) {
					if(this.context.getTermValue(key).isJsonObject()) {
	
						expanded = this.context.getTermValue(key);
					}else if(this.context.getTermValue(key).isJsonPrimitive()) {
							expanded = this.context.getTermValue(key);
						}
	
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
//				log.fatal("a cyclic IRI mapping");
				LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","cyclic IRI mapping");

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
//										log.fatal(" invalid type mapping ");
										LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","invalid type mapping ");
									}
								}
							}else {
								LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","invalid type mapping ");
								//log.fatal(" invalid type mapping ");
							}
						}
						
						if(value.getAsJsonObject().has(JsonLdKeyword.REVERSE.toString())) {
							if(value.getAsJsonObject().has(JsonLdKeyword.ID.toString() ) || value.getAsJsonObject().has("@nest")) {
								LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","invalid reverse property");
//								log.fatal("invalid reverse property...abort process");
							}
							if(!value.getAsJsonObject().get(JsonLdKeyword.REVERSE.toString()).isJsonPrimitive()) {
								LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","invalid IRI mapping");
//								log.fatal("invalid IRI mapping ...abort process");
								
							}else {
								definition = this.iriExpansion(value.getAsJsonObject().get(JsonLdKeyword.REVERSE.toString()));
								
								if(IRI.isAbsolute(definition.getAsJsonPrimitive().getAsString()) ||  definition.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.BLANK_NODE) ) {
//									log.fatal("invalid IRI mapping..abort process");
									LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","invalid IRI mapping");
								}
							}
							if(value.getAsJsonObject().has(JsonLdKeyword.CONTAINER.toString())) {
								definition =value.getAsJsonObject().get(JsonLdKeyword.CONTAINER.toString());
								if(!definition.getAsJsonPrimitive().isJsonNull() ) {
									if(definition.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.SET.toString()) || definition.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.INDEX.toString()) ) {
//										log.fatal("invalid reverse property...processing is aborted");
										LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","invalid reverse property");
									}
								}else {
//									log.fatal("invalid reverse property...processing is aborted");
									LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","invalid reverse property");
								}
							}
							
							//TODO implement 14.5 and 14.6
						}
						
						if(value.getAsJsonObject().has(JsonLdKeyword.ID.toString())) {
							if(!value.getAsJsonObject().get(JsonLdKeyword.ID.toString()).equals(term)) {
								if(!value.getAsJsonObject().get(JsonLdKeyword.ID.toString()).isJsonPrimitive()) {
//									log.fatal(" invalid IRI mapping error has been detected and processing is aborted");
									LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","invalid IRI mapping");
								}else {
									definition = this.iriExpansion(value.getAsJsonObject().get(JsonLdKeyword.ID.toString()));
									if(JsonLdKeyword.isKeyword(definition.getAsJsonPrimitive().getAsString()) || 
											IRI.isAbsolute(definition.getAsJsonPrimitive().getAsString()) ||
											value.getAsJsonPrimitive().getAsString().equals(JsonLdKeyword.BLANK_NODE)){
//										log.fatal("invalid IRI mapping error has been detected and processing is aborted.");
										LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","invalid IRI mapping");
									}
								}
							}
						}
						
						if(term.getAsJsonPrimitive().getAsString().contains(":")) {
							String prefix =term.getAsJsonPrimitive().getAsString().substring(0,term.getAsJsonPrimitive().getAsString().lastIndexOf(":"));
							this.createTermDefinition(new JsonPrimitive(prefix));
						}
					}else {
						//log.fatal("error");
						LogUtils.logDebug(JSONLDSerialization.owner,this.getClass(),"expand","error");
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
