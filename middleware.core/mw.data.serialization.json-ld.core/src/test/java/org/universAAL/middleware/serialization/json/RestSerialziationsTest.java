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
package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Enumeration;

import org.junit.Test;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.util.Specializer;
/**
 * 
 * @author Eduardo Buhid
 *
 */
public class RestSerialziationsTest {
	
	@Test
	public void SerializationWorkFlowTest() {
		String resourcePath="./expand/UAALMessageExample1.json";
		//String resourcePath="./expand/simple.json";
		try {
			InputStream json =  this.getClass().getClassLoader().getResource(resourcePath).openStream();
			JSONLDSerialization ser = new JSONLDSerialization();
			Object serialized = ser.deserialize(json);
			Resource m = (Resource)serialized;
			//m = m.copy(true);
			m = new Specializer().specialize(m);
			//ServiceProfile sp = (ServiceProfile) m;
			System.out.println(m);
			assertNotNull(m);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void SimpleTest() {
		Resource r = new Resource("http://ontology.universAAL.org/Device.owl#"); 
		r.setProperty("http://ontology.universAAL.org/Device.owl#hasValue", "123");
		r = new Specializer().specialize(r);
		//ServiceProfile sp = (ServiceProfile) r;

	}

}
