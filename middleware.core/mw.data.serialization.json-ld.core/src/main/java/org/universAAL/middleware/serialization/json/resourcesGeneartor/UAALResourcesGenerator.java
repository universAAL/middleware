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
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Popup;

import java.util.Scanner;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.serialization.json.JSONLDSerialization;
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
	private Resource mainResource=null;
	private HashMap<String, Resource> resources = new HashMap<String, Resource>();
	private Hashtable blankNodes = new Hashtable();

	/**
	 * gets {@link InputStream} or {@link JsonArray} input json to extract {@link Resource} objects
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
		}else if(jsonToExpand instanceof JsonArray) {
			this.mainjJson = (JsonArray)jsonToExpand;
		}else if( jsonToExpand == null) {
			LogUtils.logDebug(JSONLDSerialization.owner, ExpandJSONLD.class, "UAALResourcesGenerator constructor", "Given Null Json");
		}
		
		
	}
	
	
	/**
	 * method to generate {@link Resource} from loaded expanded JsonLD
	 */
	public void generateResources() {
		if(this.mainjJson!=null) {
			for (JsonElement element : mainjJson) {
				this.mainResource =this.genResource(element.getAsJsonObject());
			}	
		}else {
			LogUtils.logDebug(JSONLDSerialization.owner, ExpandJSONLD.class, "generateResources", "Given Null Json");
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
		
			if(candidate.get(JsonLdKeyword.TYPE.toString()) instanceof JsonArray) {
				Iterator<JsonElement> i = candidate.remove(JsonLdKeyword.TYPE.toString()).getAsJsonArray().iterator();
				while (i.hasNext()) {
					JsonElement t = i.next();
					boolean v = i.hasNext();
					r.addType(t.getAsJsonPrimitive().getAsString(), !v);
				}	
			}else if(candidate.get(JsonLdKeyword.TYPE.toString()) instanceof JsonPrimitive){
				r.addType(candidate.remove(JsonLdKeyword.TYPE.toString()).getAsJsonPrimitive().getAsString(), true);
			}
				
		}
		
		for (Entry<String, JsonElement> item : candidate.entrySet()) {
			String propURI = item.getKey();
			Resource aux = this.getResource(null);
			if(item.getValue() instanceof JsonArray ) {
				if(item.getValue().getAsJsonArray().size() > 1) {
					List l = parseCollection(item.getValue().getAsJsonArray(), false);
					aux.addType(Resource.TYPE_RDF_LIST, true);
					aux.setProperty(Resource.PROP_RDF_FIRST, l.remove(0));
					aux.setProperty(Resource.PROP_RDF_REST, l);
					r.setProperty(propURI, aux.asList());	
				}else {
					
					JsonElement array_item  =item.getValue().getAsJsonArray().iterator().next();
					if( array_item instanceof JsonPrimitive) {
						r.setProperty(propURI, item.getValue().getAsJsonArray().iterator().next().getAsJsonPrimitive().getAsString());	
					}else if(array_item instanceof JsonObject) {
						if(array_item.getAsJsonObject().has(JsonLdKeyword.TYPE.toString()) &&  array_item.getAsJsonObject().has(JsonLdKeyword.VALUE.toString())){
							if(array_item.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()) instanceof JsonArray) {
								List l = parseCollection(array_item.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()).getAsJsonArray(), true);
								r.setProperty(propURI, l);
							}else if(array_item.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()) instanceof JsonPrimitive) {
								if(array_item.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()).getAsJsonPrimitive().getAsString().startsWith("http://www.w3.org/2001/XMLSchema#")) {
									Object mapped_type = TypeMapper.getJavaInstance(array_item.getAsJsonObject().get(JsonLdKeyword.VALUE.toString()).getAsJsonPrimitive().getAsString(), array_item.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()).getAsJsonPrimitive().getAsString());
									r.setProperty(propURI, mapped_type);		
								}else {
									r.setProperty(propURI, array_item.getAsJsonObject().get(JsonLdKeyword.TYPE.toString()).getAsJsonPrimitive().getAsString());
								}
							}
							
						}else if(array_item.getAsJsonObject().has(JsonLdKeyword.VALUE.toString())){
							r.setProperty(propURI, array_item.getAsJsonObject().get(JsonLdKeyword.VALUE.toString()).getAsJsonPrimitive().getAsString());	
						}else {
							aux =this.genResource(item.getValue().getAsJsonArray().iterator().next().getAsJsonObject());
							r.setProperty(propURI, aux);	
						}
						
					}else if(array_item instanceof JsonArray) {
						System.out.println("array of arrays");
					}
				}
			}else if(item.getValue() instanceof JsonPrimitive) {
				if(item.getKey().equals(JsonLdKeyword.VALUE.toString())) {
					r.setProperty(propURI, TypeMapper.getJavaInstance(item.getValue().getAsJsonPrimitive().getAsString(),r.getType() ));
				}
			}
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
		System.out.println("parse colleciton "+candidate);
		List l = new ArrayList();
		Resource aux ;
		if(parseAsTypeList) {
			for (JsonElement item : candidate) {
				l.add(item.getAsJsonPrimitive().getAsString());
			}
			
		}else {
			for (JsonElement item : candidate) {
				
				if(item.getAsJsonObject().entrySet().size() == 1 && item.getAsJsonObject().entrySet().iterator().next().getKey().equals("@value")) {
					l.add(TypeMapper.getJavaInstance(item.getAsJsonObject().entrySet().iterator().next().getValue().getAsJsonPrimitive().getAsString(), "http://www.w3.org/2001/XMLSchema#string") );
				}else{
					aux = genResource(item.getAsJsonObject());
					l.add(aux);
				}
				
				//aux = genResource(item.getAsJsonObject());
				//l.add(aux);
			}				
		}
		return l;
	}
	
	private Object valueTypeSelector(String value) {
		if(value.endsWith("boolean")) {
			return Boolean.valueOf(value);
		}
	
		
		return null;
	}

	

}
