/*******************************************************************************
 * Copyright 2018 Universidad Politécnica de Madrid UPM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.middleware.serialization.json.resourcesGeneartor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.json.JsonLdKeyword;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Class to generate universAAl {@link Resource}s from given Json. 
 * After any process, the given Json need to be expanded from JsonLD to Json using expansion agorithms.
 * see {@link ExpandJSONLD}   
 * @author Eduardo Buhid
 *
 */

public class UAALResourcesGenerator {
	private JsonArray mainjJson;
	private JsonArray expandedJson;
	private JsonParser parser = new JsonParser();
	private HashMap<String , Resource> generatedResources = new HashMap<String, Resource>();
	private Resource mainResource=null;
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
			this.mainjJson = parser.parse(jsonString).getAsJsonArray();
		}
		
		
		
		if(jsonToExpand instanceof JsonArray) {
			this.mainjJson = (JsonArray)jsonToExpand;
		}
		
		
	}
	
	
	/**
	 * method to generate {@link Resource} from loaded expanded JsonLD
	 */
	public void generateResources() {
		//this.expandedJson=this.expandJson(mainjJson);
		for (JsonElement element : mainjJson) {
			this.mainResource =this.genResource(element);
			
		}
		
	}
	
	private Resource genResource(JsonElement candidate) {	
		String resourceID=null;
		System.out.println("candidate "+candidate);
		//ArrayList<String> resourceTypes=new ArrayList<String>();
		Resource r=null;
		for (Entry<String, JsonElement> item : candidate.getAsJsonObject().entrySet()) {
			if(item.getKey().equals(JsonLdKeyword.ID.toString()) && item.getValue() instanceof JsonPrimitive){
				//resource ID given
				resourceID = item.getValue().getAsJsonPrimitive().getAsString();
				r = new Resource(resourceID);
			}else if (item.getKey().equals(JsonLdKeyword.TYPE.toString()) && item.getValue() instanceof JsonArray) {
				Iterator<JsonElement> i = item.getValue().getAsJsonArray().iterator();
				
				while (i.hasNext()) {
					JsonElement t = i.next();
					boolean v = i.hasNext();
					r.addType(t.getAsJsonPrimitive().getAsString(), !v);
				}	
			}
			else
				r.setProperty(item.getKey(), item.getValue());

		}
	return r;	
	}

	private JsonArray expandJson(JsonObject toExpand) {
		ExpandJSONLD jsonExpansor = new ExpandJSONLD(toExpand);
		jsonExpansor.expand();
		return jsonExpansor.getExpandedJson();
	}

	public Resource getSpecificResource (String resourceClass,String resourceURI) {
		if(this.mainResource ==null) {
			this.generateResources();
		}
		return this.mainResource.getResource(resourceClass, resourceURI);
		
	}
	public Resource getMainResource() {
		return this.mainResource;
	}
}
