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
import org.universAAL.middleware.util.Specializer;

/**
 * @author amedrano
 *
 */
public class JSONLDSerialization implements MessageContentSerializerEx {
	private ExpandJSONLD expand=null;
	static public ModuleContext owner;

	static private String MIME_TYPE = "application/json";

	
	public Object deserialize(InputStream serialized) {
		JSONLDDocument jd = new JSONLDDocument(serialized);
		if(jd.validate()) {
			jd.expand();
			System.out.println(jd.getExpandedJson());
			UAALResourcesGenerator resFactory = new UAALResourcesGenerator(jd.getExpandedJson());
			resFactory.generateResources();
			Resource r = resFactory.getMainResource();
			Resource k =new Specializer().specialize(r);
			return k;
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
		Resource r =null;
		Object t = this.deserialize(serialized);
		if(t instanceof Resource) {
			r = (Resource)t;
		}else 
			return null;
		return r.getProperty(resourceURI);
	}

	public String getContentType() {
		return MIME_TYPE;
	}
}
