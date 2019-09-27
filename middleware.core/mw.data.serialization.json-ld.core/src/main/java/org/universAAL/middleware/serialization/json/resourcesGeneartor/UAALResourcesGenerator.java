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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
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
	private HashMap<String, Resource> resources = new HashMap<String, Resource>();
	private Hashtable blankNodes = new Hashtable();

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
			this.mainResource =this.genResource(element.getAsJsonObject());
			
		}
		
	}
	
	private Resource genResource(JsonObject candidate) {	
		String resourceID=null;
		Resource r=null;
		if(candidate.has(JsonLdKeyword.ID.toString()) ){
			resourceID = candidate.remove(JsonLdKeyword.ID.toString()).getAsJsonPrimitive().getAsString();
			r = this.getResource(resourceID);
		}else 
			r = this.getResource(null);
		
		if (candidate.has(JsonLdKeyword.TYPE.toString())) {
			Iterator<JsonElement> i = candidate.remove(JsonLdKeyword.TYPE.toString()).getAsJsonArray().iterator();
			while (i.hasNext()) {
				JsonElement t = i.next();
				boolean v = i.hasNext();
				r.addType(t.getAsJsonPrimitive().getAsString(), !v);
			}	
		}
		for (Entry<String, JsonElement> item : candidate.entrySet()) {
			String propURI = item.getKey();
			Resource aux = this.getResource(null);
			List l = parseCollection(item.getValue().getAsJsonArray(), false);
			aux.addType(Resource.TYPE_RDF_LIST, true);
			aux.setProperty(Resource.PROP_RDF_FIRST, l.remove(0));
			aux.setProperty(Resource.PROP_RDF_REST, l);
			r.setProperty(propURI, aux.asList());
		
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
	
	private Resource getResource(String uri) {
		Resource r;
		if (uri == null) {
			r = new Resource();
			resources.put(r.getURI(), r);
		} else {
			r = (Resource) resources.get(uri);
			if (r == null) {
				if (uri.startsWith("_:")) {
					// bNode ID
					r = (Resource) blankNodes.get(uri);
					if (r == null) {
						r = new Resource();
						blankNodes.put(uri, r);
					}
				} else {
					r = new Resource(uri);
				}
				resources.put(r.getURI(), r);
			}
		}
		return r;
	}

	private List parseCollection(JsonArray candidate, boolean parseAsTypeList) {
		List l = new ArrayList();
		Resource aux ;
		if(parseAsTypeList) {
			for (JsonElement item : candidate) {
				l.add(item.getAsJsonPrimitive().getAsString());
			}
			
		}else {
			for (JsonElement item : candidate) {
				if(item.getAsJsonObject().entrySet().size() == 1 && item.getAsJsonObject().entrySet().iterator().next().getKey().equals("@value")) {
					l.add(TypeMapper.getJavaInstance(item.getAsJsonObject().entrySet().iterator().next().getValue().getAsJsonPrimitive().getAsString(), null) );
				}else{
					aux = genResource(item.getAsJsonObject());
					l.add(aux);
				}
				
			}				
		}
		return l;
		
	}

}
