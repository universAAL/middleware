package org.universAAL.middleware.serialization.json.algorithms;

import java.io.InputStream;
import java.util.Scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Context {
	JsonParser parser = new JsonParser();
	String jsonToParse =null;
	JsonObject contextDefinition=null;
	
	public Context (String jsonToParse) {
		this.jsonToParse = jsonToParse;
		this.initContext();

	}
	
	public Context (InputStream jsonToParse) {
		String jsonString = "";
		Scanner s = new Scanner(jsonToParse );
		s.useDelimiter("\\A");
		jsonString = s.hasNext() ? s.next() : "";
		s.close();
		this.jsonToParse = jsonString;
		this.initContext();

	}
	
	public Context(JsonObject jsonToParse) {
		this.contextDefinition = jsonToParse;	
	}
	
	public JsonElement hasTypeMapping(String activeProperty) {
		if(this.contextDefinition !=null) {
			if(contextDefinition.has(activeProperty)) {
				return contextDefinition.get(activeProperty);
			}
		}
		return null;
	}
	
	public JsonElement hasTypeMapping(JsonElement activeProperty) {
		
		if(this.contextDefinition !=null) {
			if(activeProperty.isJsonPrimitive()) {
				if(contextDefinition.has(activeProperty.getAsJsonPrimitive().toString())) {
					return contextDefinition.get(activeProperty.getAsJsonPrimitive().toString());
				}
			}
		}
		return null;
	}
	
	private void initContext() {
		if(this.parser.parse(jsonToParse).isJsonObject()) {
			this.contextDefinition = this.parser.parse(jsonToParse).getAsJsonObject();
		}
		
	}
	
	public void remove(String itemToRemove) {
		this.contextDefinition.remove(itemToRemove);	
	}
	
	public String getContext() {
		return this.contextDefinition.toString();
	}
	
	private void updateContext(String context) {
		
	}
}
