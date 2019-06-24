package org.universAAL.middleware.serialization.json.resourcesGeneartor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UAALResourcesGenerator {
	private JsonObject mainjJson;
	private JsonArray expandedJson;
	private JsonParser parser = new JsonParser();
	private HashMap<String , Resource> generatedResources = new HashMap<String, Resource>();
	/**
	 * get {@link InputStream} or {@link JsonObject} or {@link String} input json to extract {@link Resource} objects
	 * @param jsonToExpand
	 */
	public UAALResourcesGenerator(Object jsonToExpand) {
		
		if(jsonToExpand instanceof InputStream) {
			String jsonString = "";
			Scanner s = new Scanner((InputStream)jsonToExpand);
			s.useDelimiter("\\A");
			jsonString = s.hasNext() ? s.next() : "";
			s.close();
			this.mainjJson = parser.parse(jsonString).getAsJsonObject();
		}
		
		if(jsonToExpand instanceof JsonObject) {
			this.mainjJson = (JsonObject) jsonToExpand;
		}
		
		if(jsonToExpand instanceof JsonArray) {
			
		}
		
		if(jsonToExpand instanceof String) {
			this.mainjJson = parser.parse((String)jsonToExpand).getAsJsonObject();
		}
	}
	
	public void generateResources() {
		this.expandedJson=this.expandJson(mainjJson);
		for (Entry<String, JsonElement> element : expandedJson.iterator().next().getAsJsonObject().entrySet()) {
			Resource r =this.walkGraph(element);
			
		}
		
	}
	
	private Resource walkGraph(Entry<String, JsonElement> toAnalyze) {
		ArrayList<String> resourceTypesArray = new ArrayList<String>();	
		String resourceID="";
		
		if(toAnalyze.getValue().isJsonArray()) {
			JsonArray jsa = new JsonArray();
			for (JsonElement item : jsa) {
				//if this array has more than 1 element, it is a resource list
				//view 	public static final Resource asRDFList(List members, boolean isXMLLiteral) {} Resource class

				walkGraph(item.getAsJsonObject().entrySet().iterator().next());
			}
		}
		if(toAnalyze.getValue().isJsonObject()) {
			resourceID = toAnalyze.getKey();
			JsonObject aux =toAnalyze.getValue().getAsJsonObject();
			Resource res = new Resource();
			
			//"@type": "http://www.w3.org/2001/XMLSchema#dateTime",
			//"@value": "2011-04-09T20:00:00Z"
			
		}
		
		
		String resource_id=null;
		if(toAnalyze.getValue().isJsonObject()) {
			JsonElement resource_uri;
			Resource local_res;
			JsonObject aux =toAnalyze.getValue().getAsJsonObject();
			
			for (Entry<String, JsonElement> iterable_element : toAnalyze.getValue().getAsJsonObject().entrySet()) {
				if(iterable_element.getValue().isJsonObject()) {
					local_res=this.walkGraph(iterable_element);
				}else {

					if(iterable_element.getValue().isJsonPrimitive()) {
						String primitive = iterable_element.getKey();
						
						if(primitive.equals(JsonLdKeyword.ID.toString())) {
							local_res = new Resource(iterable_element.getValue().getAsJsonPrimitive().getAsString());
						}
						
						if(primitive.equals(JsonLdKeyword.VALUE.toString())) {

						}
						
						if(primitive.equals(JsonLdKeyword.TYPE.toString())) {

						}

					}
					
				}
			}
			
		}
		
		if(toAnalyze.getValue().isJsonArray()) {
			//list detected
			
			if(toAnalyze.getKey().equals(JsonLdKeyword.TYPE.toString())) {
				//add list to local Resource
				for (JsonElement element : toAnalyze.getValue().getAsJsonArray()) {
					resourceTypesArray.add(element.getAsJsonPrimitive().getAsString());
				}
			}else {
				
				for (JsonElement element : toAnalyze.getValue().getAsJsonArray()) {
					for (Entry<String, JsonElement> item : element.getAsJsonObject().entrySet()) {
						Resource list_resource =this.walkGraph(item);
						
					}
					
				}
				
			}
			
		}
		
		if(toAnalyze.getValue().isJsonPrimitive()) {

			if(toAnalyze.getKey().equals(JsonLdKeyword.ID.toString())) {
				//element ID detected
				resource_id = toAnalyze.getValue().getAsJsonPrimitive().getAsString();
			}
		
		}
	return null;	
	}

	private JsonArray expandJson(JsonObject toExpand) {
		ExpandJSONLD jsonExpansor = new ExpandJSONLD(toExpand);
		jsonExpansor.expand();
		return jsonExpansor.getExpandedJson();
	}

	public Resource getSpecificResource (String resourceURI) {
		return null;
		
	}
}
