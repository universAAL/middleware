package org.universAAL.middleware.serialization.json.resourcesGeneartor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UAALResourcesGenerator {
	private JsonObject jsonToExpand;
	private JsonParser parser = new JsonParser();
	private HashMap<String , Resource> generatedResources = new HashMap<>();
	
	public UAALResourcesGenerator(Object jsonToExpand) {
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
	
	public void generateResources() {
		
		for (Entry<String, JsonElement> element : jsonToExpand.entrySet()) {
			
			this.walkGraph(element);
			
		}
		
	}
	
	private Resource walkGraph(Entry<String, JsonElement> toAnalyze) {
		
		JsonElement main_resource_uri=null;
		if(toAnalyze.getValue().isJsonObject()) {
			JsonElement resource_uri;
			Resource local_res;
			JsonObject aux =toAnalyze.getValue().getAsJsonObject();
			resource_uri = aux.get(JsonLdKeyword.ID.toString());//maybe hasnt ID key
			for (Entry<String, JsonElement> iterable_element : toAnalyze.getValue().getAsJsonObject().entrySet()) {
				if(iterable_element.getValue().isJsonObject()) {
					local_res=this.walkGraph(iterable_element);
					
				}
			}
			
		}
		
		if(toAnalyze.getValue().isJsonArray()) {
			JsonElement resource_uri;
			for (int i = 0; i < toAnalyze.getValue().getAsJsonArray().size(); i++) {
				
				for (Entry<String, JsonElement> iterable_element : toAnalyze.getValue().getAsJsonObject().entrySet()) {
					this.walkGraph(iterable_element);
				}
			}
		}
		
		if(toAnalyze.getValue().isJsonPrimitive()) {
			JsonElement resource_uri;
			//create resource
			//resource IRI: the ID key contained into the object
			Resource res = new Resource(main_resource_uri.getAsJsonPrimitive().getAsString());
			res.setProperty(toAnalyze.getKey(),toAnalyze.getValue().getAsJsonPrimitive().getAsString());
		}
	return null;	
	}

}
