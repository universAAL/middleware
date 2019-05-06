package org.universAAL.middleware.serialization.json.grammar;

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
import com.google.gson.JsonPrimitive;

public class ExpandJsonLD {

	private JsonObject jsonToExpand=null;
	private Object toExpand=null;
	private ContextDefinition activeContext=null;
	private ContextDefinition mainContext=null;
	private JsonParser parser = new JsonParser();
	
	public ExpandJsonLD(Object candidate) {
		this.toExpand = candidate;
		
		if(candidate instanceof JsonObject) {
			this.jsonToExpand = (JsonObject)candidate;
		}
		
		if(candidate instanceof InputStream) {
			String jsonString = "";
			Scanner s = new Scanner( (InputStream)candidate );
			s.useDelimiter("\\A");
			jsonString = s.hasNext() ? s.next() : "";
			s.close();
			this.jsonToExpand = parser.parse(jsonString).getAsJsonObject();
			
		}
		
		if(candidate instanceof String) {
			this.jsonToExpand = parser.parse((String)candidate).getAsJsonObject();
		}
		
	}
	
	public ExpandJsonLD(Object candidate,ContextDefinition contextDefinition) {
		
	}
	
	public JsonElement expand() {
		JsonArray expanded=new JsonArray();
		
		if(!this.jsonToExpand.has(JsonLdKeyword.CONTEXT.toString()) && this.mainContext==null) {
			return null;
		}
		
		if(this.mainContext ==null && this.jsonToExpand.has(JsonLdKeyword.CONTEXT.toString()) ) {
			this.mainContext = new ContextDefinition(this.jsonToExpand.get(JsonLdKeyword.CONTEXT.toString()));
		}
		
		//remove context from json before expand
		this.jsonToExpand.remove(JsonLdKeyword.CONTEXT.toString());
		
		for (Entry<String, JsonElement> element_to_be_expanded : this.jsonToExpand.entrySet()) {
			JsonObject item_to_add =new  JsonObject();
			//if the term has not key mapping into context, it will be ignored 
			if(this.mainContext.hasTerm(element_to_be_expanded.getKey())) {
				if(element_to_be_expanded.getValue().isJsonPrimitive()) {
					if(element_to_be_expanded.getValue().isJsonNull() || element_to_be_expanded.getValue().equals(JsonLdKeyword.GRAPH.toString())) {
						
						//TODO return null
						//if active property is null or @graph, drop the free-floating scalar by returning null.
					}else {
						//Return the result of the Value Expansion algorithm, passing the active context, active property, and element as value.
						System.out.println(this.jsonToExpand.get(element_to_be_expanded.getKey()).toString());
						 Map<String, String> h = this.valueExpand(element_to_be_expanded);
						item_to_add.addProperty(h.keySet().iterator().next(),h.get(h.keySet().iterator().next()));
					}
					
					
				}
			}else {
				System.out.println("inexistent element...ignoring "+element_to_be_expanded.getKey());
			}
			//at the end add the "item_to_add" to expanded result json
		}
		
		
		
		
		return expanded;
	}
	
	private Map<String,String> valueExpand(Entry<String, JsonElement> candidate) {
		HashMap<String, String> aux = new HashMap<>();
		
		if(this.mainContext.hasTerm(candidate.getKey())) {
			if(candidate.getValue().isJsonObject()) {
				if(candidate.getValue().getAsJsonObject().has(JsonLdKeyword.ID.toString()) && candidate.getValue().getAsJsonObject().get(JsonLdKeyword.ID.toString()).isJsonPrimitive() ) {
					aux.put(JsonLdKeyword.ID.toString(),this.iriExpand(candidate.getValue().getAsJsonObject().get(JsonLdKeyword.ID.toString()).getAsJsonPrimitive().getAsString(),false));
				}else if(candidate.getValue().getAsJsonObject().has(JsonLdKeyword.VOCAB.toString()) && candidate.getValue().getAsJsonObject().get(JsonLdKeyword.ID.toString()).isJsonPrimitive() ) {
					aux.put(JsonLdKeyword.ID.toString(),this.iriExpand(candidate.getValue().getAsJsonObject().get(JsonLdKeyword.ID.toString()).getAsJsonPrimitive().getAsString(),true) );
				}else {
					aux.put(JsonLdKeyword.TYPE.toString(), this.mainContext.getTermValue(candidate.getKey()));
				}
			}else if(candidate.getValue().isJsonPrimitive()) {
				aux.put(JsonLdKeyword.VALUE.toString(),candidate.getValue().getAsJsonPrimitive().toString());

			}

		}
				
		if(candidate.getValue() instanceof JsonPrimitive) {
			//TODO
			/*
			 * Otherwise, if value is a string
    If a language mapping is associated with active property in active context, add an @language to result and set its value to the language code
     associated with the language mapping; unless the language mapping is set to null in which case no member is added.
    Otherwise, if the active context has a default language, add an @language to result and set its value to the default language.
*/
		}
		
		return aux;
	}
	
	private String iriExpand(String candidate,boolean vocabState) {
		 return "not iplemented yet";
	}
}
