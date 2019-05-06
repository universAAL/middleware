package org.universAAL.middleware.serialization.json.grammar;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Expansion {

	private ContextDefinition activeContext=null;
	private JsonElement candidate = null;
	private JsonParser jp = null;

	public Expansion (ContextDefinition activeContext,JsonElement candidate) {
		this.activeContext=activeContext;
		this.candidate=candidate;
		this.jp = new JsonParser();
		
	}
	
	public String compact() {
		if(this.activeContext==null ||   this.candidate==null)
			return null;
		
		//determine if the main json is array or object
		if(this.candidate.isJsonArray()) {
			
			for (JsonElement item : candidate.getAsJsonArray()) {
				if(item.isJsonPrimitive()) {
					
				}
			}
		}
		
		
		if(this.candidate.isJsonObject()) {
			
			for (Entry<String, JsonElement> item : candidate.getAsJsonObject().entrySet()) {
				
				if(item.getValue().isJsonNull()) {
					
				}
				
				if(item.getValue().isJsonPrimitive()) {
					//TODO  if element is a scalar, we expand it according to the Value Expansion algorithm.
				}	
			}
		}
		
		
		return "";
	}
}
