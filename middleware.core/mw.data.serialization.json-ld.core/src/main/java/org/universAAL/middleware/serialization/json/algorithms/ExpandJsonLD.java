package org.universAAL.middleware.serialization.json.algorithms;

import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.grammar.ContextDefinition;

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
	private ContextDefinition activeContext=null;
	private JsonArray expandedJsonLD=null;
	private Object jsonToExpand=null;
	private JsonObject mainJson;
	private JsonParser parser = new JsonParser();
	
	public ExpandJsonLD(Object jsonToExpand) {
		this.jsonToExpand=jsonToExpand;
		this.expandedJsonLD = new JsonArray();
	}
	
	private String expandElement (String activeContext,String activePropertie, JsonElement elementToExpand, Object flagExpansion) {
		if(elementToExpand == null)
			return  null;
		if(elementToExpand.isJsonPrimitive()) {
			if(activePropertie.equals(JsonLdKeyword.GRAPH) || activePropertie ==null ) {
				return null;
			}
		}
		
		if(elementToExpand.isJsonArray()) {
			JsonArray aux_result = new JsonArray();
			Object expanded_item = this.expandElement(activeContext, activePropertie, elementToExpand, flagExpansion);
			if(activePropertie.equals(JsonLdKeyword.LIST.toString())) {
				//the expanded item must not be an array or list object (a list object is a JSONObject that has a @list member)
			}
			if(expanded_item instanceof JsonArray) {
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
					
						String expanded_prop = this.expandIRI(element);
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
			
		this.initJSONLDDoc();
		return null;
	}
	
	/**
	 * this method add the expanded term to resultant json (expanded)
	 * @param context
	 * @param toExpand
	 */
	private Boolean termExpansion(ContextDefinition context,Map.Entry<String, JsonElement> toExpand ) {

		return true;
	}
	
	
	private void initJSONLDDoc() {
		
		if(this.jsonToExpand instanceof String) {
			if(parser.parse((String)jsonToExpand).isJsonObject()) {
				this.mainJson = parser.parse((String)jsonToExpand).getAsJsonObject();
			}
		}

		if(this.jsonToExpand instanceof JsonObject) {
			this.mainJson = parser.parse((String)jsonToExpand).getAsJsonObject();
		}

		if(this.jsonToExpand instanceof InputStream) {
			String jsonString = "";
			Scanner s = new Scanner( (InputStream)this.jsonToExpand );
			s.useDelimiter("\\A");
			jsonString = s.hasNext() ? s.next() : "";
			s.close();
			if(parser.parse((String)jsonToExpand).isJsonObject()) {
				this.mainJson = parser.parse((String)jsonToExpand).getAsJsonObject();
			}
		}
		
	}
	/**
	 * method to expand value to absoluteIRI using activeContext
	 * @return
	 */
	private String expandIRI(Entry<String, JsonElement>  jse) {
		return null;
	}
	
	private String expandValue() {
		return null;
	}
	
}
