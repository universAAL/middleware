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

import java.io.InputStream;

//import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.turtle.TurtleParser;

import junit.framework.TestCase;
/**
 * 
 * @author Eduardo Buhid
 *
 */
public class RestSerialziationsTest extends TestCase {


	
	String pattern2="@prefix ns: <http://ontology.universaal.org/Health.owl#> .\r\n" + 
			"@prefix ns1: <http://ontology.universAAL.org/Profile.owl#> .\r\n" + 
			"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
			"@prefix ns2: <http://ontology.universAAL.org/Context.owl#> .\r\n" + 
			"@prefix : <http://www.w3.org/2002/07/owl#> .\r\n" + 
			"<https://rest.activage.lst.tfo.upm.es/uaal/spaces/equimetrix/context/publishers/backup>\r\n" + 
			"a ns2:ContextProvider ;\r\n" + 
			"ns2:hasType ns2:gauge .\r\n" + 
			"";

	//method to "test" the workflow of REST api 
	public void testSerializationWorkFlow() {
		String resourcePath="./expand/UAALMessageExample1.json";
		
		try {
			InputStream json =  this.getClass().getClassLoader().getResource(resourcePath).openStream();
			JSONLDSerialization ser = new JSONLDSerialization();
			Object serialized = ser.deserialize(json);
			Resource m = (Resource)serialized;
			assertNotNull(m);
			assertTrue(m.isWellFormed());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	//TODO check this case...maybe the resource generated with this json is wrong
	public void TestExpandedJson() {
		String resourcePath="./expandedJson.json";
		try {
			InputStream json =  this.getClass().getClassLoader().getResource(resourcePath).openStream();
			JSONLDSerialization ser = new JSONLDSerialization();
			Object serialized = ser.deserialize(json);
			Resource m = (Resource)serialized;
			System.out.println(m.toStringRecursive());
			assertNotNull(m);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	
	//test to compare the generated resource with both serializers, taking as example a Turtle resource
	public void testCompareSerializersResult() throws Exception{
		TurtleParser turtle_parser = new TurtleParser();
		JSONLDSerialization json_parser = new JSONLDSerialization();
		Resource fromTurtle,fromJson;
		fromTurtle = (Resource)turtle_parser.deserialize(pattern2 , null);
		String JsonFromTurtle = json_parser.serialize(fromTurtle);
		fromJson = (Resource)json_parser.deserialize(JsonFromTurtle);
		assertNotNull(fromTurtle);
		assertTrue(fromTurtle.isWellFormed());
		assertNotNull(fromJson);
		assertTrue(fromJson.isWellFormed());
		assertTrue(fromJson.equals(fromTurtle));
		System.out.println("----------turtle----------");
		System.out.println(fromTurtle.toStringRecursive());
		System.out.println("----------json----------");
		System.out.println(fromJson.toStringRecursive());
	}	

}
