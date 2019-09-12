/*******************************************************************************
 * Copyright 2019 Universidad Politécnica de Madrid UPM
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
package org.universAAL.middleware.serialization.json;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;
import org.universAAL.middleware.serialization.json.grammar.JSONLDDocument;
import org.universAAL.middleware.serialization.json.resourcesGeneartor.UAALResourcesGenerator;

/**
 * @author amedrano
 *
 */
public class JSONLDSerialization implements MessageContentSerializerEx {

	static public ModuleContext owner;

	static private String MIME_TYPE = "application/json";

	
	public Object deserialize(InputStream serialized) {
		JSONLDDocument jd = new JSONLDDocument(serialized);
		System.out.println("deserializing "+jd.getFullJsonAsString());
		
		if(!jd.validate()) {
			//ExpandJSONLD expand = new ExpandJSONLD(serialized);
			ExpandJSONLD expand = new ExpandJSONLD(jd.getFullJsonAsString());//TODO check why with inputstream not work
			expand.expand();
			UAALResourcesGenerator resFactory = new UAALResourcesGenerator(expand.getExpandedJson());
			resFactory.generateResources();
			return resFactory.getAllResources();
			//return expand.getExpandedJson();
		}else
			return null;
	}
	
	/* (non-Javadoc)
	 * @see org.universAAL.middleware.serialization.MessageContentSerializer#deserialize(java.lang.String)
	 */
	public Object deserialize(String serialized) {
		InputStream is = new ByteArrayInputStream(serialized.getBytes());
		return this.deserialize(is);
				
	}
	



	/* (non-Javadoc)
	 * @see org.universAAL.middleware.serialization.MessageContentSerializer#serialize(java.lang.Object)
	 */
	public String serialize(Object messageContent) {
		JSONLDWriter jw = new JSONLDWriter();
		if (messageContent instanceof Resource) {
			return jw.serialize((Resource) messageContent);
		}
		else
			return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.universAAL.middleware.serialization.MessageContentSerializerEx#deserialize(java.lang.String, java.lang.String)
	 */
	public Object deserialize(String serialized, String resourceURI) {
		System.out.println("deserializing");
		InputStream is = new ByteArrayInputStream(serialized.getBytes());
		JSONLDDocument jd = new JSONLDDocument(is);
		//jd.validate();//validating if the jsonLD is well formed
		//JsonElement jse =;
		//ExpandLD expand = new ExpandLD(jd.getMainJSON());
		ExpandJSONLD expand = new ExpandJSONLD(is);
		expand.expand();//merge context with the rest of the json.
		UAALResourcesGenerator res_gen = new UAALResourcesGenerator(expand.getExpandedJson());
		res_gen.generateResources();//walk over expanded json and generate Resources from the graph
		return res_gen.getAllResources();
	}

	public String getContentType() {
		return MIME_TYPE;
	}
}
