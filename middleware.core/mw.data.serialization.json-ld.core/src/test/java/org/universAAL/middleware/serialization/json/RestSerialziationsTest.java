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

import org.universAAL.middleware.bus.junit.BusTestCase;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.turtle.TurtleParser;
/**
 * 
 * @author Eduardo Buhid
 *
 */
public class RestSerialziationsTest extends BusTestCase {
	JSONLDWriter jw = new JSONLDWriter();

	String pattern = "@prefix ns: <http://ontology.universaal.org/Health.owl#> .\r\n"
			+ "@prefix ns1: <http://ontology.universAAL.org/Profile.owl#> .\r\n"
			+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n"
			+ "@prefix ns2: <http://ontology.universAAL.org/Context.owl#> .\r\n"
			+ "@prefix : <http://www.w3.org/2002/07/owl#> .\r\n"
			+ "<https://rest.activage.lst.tfo.upm.es/uaal/spaces/equimetrix/context/publishers/backup> ns2:myClassesOfEvents (\r\n"
			+ "    [\r\n"
			+ "      a ns2:ContextEventPattern ;\r\n"
			+ "      <http://www.w3.org/2000/01/rdf-schema#subClassOf> [\r\n"
			+ "          a :Restriction ;\r\n"
			+ "          :allValuesFrom ns1:User ;\r\n"
			+ "          :onProperty rdf:subject\r\n"
			+ "        ] ,\r\n"
			+ "        [\r\n"
			+ "          a :Restriction ;\r\n"
			+ "          :allValuesFrom ns:PerformedSession ;\r\n"
			+ "          :onProperty rdf:object\r\n"
			+ "        ]\r\n"
			+ "    ]\r\n"
			+ "  ) ;\r\n"
			+ "  a ns2:ContextProvider ;\r\n"
			+ "  ns2:hasType ns2:gauge .\r\n"
			+ "ns:PerformedSession a :Class .\r\n"
			+ "ns2:gauge a ns2:ContextProviderType .\r\n"
			+ "ns1:User a :Class .";
	
	String pattern2="@prefix ns: <http://ontology.universaal.org/Health.owl#> .\r\n" + 
			"@prefix ns1: <http://ontology.universAAL.org/Profile.owl#> .\r\n" + 
			"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
			"@prefix ns2: <http://ontology.universAAL.org/Context.owl#> .\r\n" + 
			"@prefix : <http://www.w3.org/2002/07/owl#> .\r\n" + 
			"<https://rest.activage.lst.tfo.upm.es/uaal/spaces/equimetrix/context/publishers/backup>\r\n" + 
			"a ns2:ContextProvider ;\r\n" + 
			"ns2:hasType ns2:gauge .\r\n" + 
			"";
	String pattern3="@prefix ns: <http://ontology.universaal.org/Health.owl#> .\r\n" + 
			"@prefix ns1: <http://ontology.universAAL.org/Profile.owl#> .\r\n" + 
			"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
			"@prefix ns2: <http://ontology.universAAL.org/Context.owl#> .\r\n" + 
			"@prefix : <http://www.w3.org/2002/07/owl#> .\r\n" + 
			"<https://rest.activage.lst.tfo.upm.es/uaal/spaces/equimetrix/context/publishers/backup> ns2:myClassesOfEvents (\r\n" + 
			"    [\r\n" + 
			"      a ns2:ContextEventPattern ;\r\n" + 
			"      <http://www.w3.org/2000/01/rdf-schema#subClassOf> [\r\n" + 
			"          a :Restriction ;\r\n" + 
			"          :allValuesFrom ns1:User ;\r\n" + 
			"          :onProperty rdf:subject\r\n" + 
			"        ] ,\r\n" + 
			"        [\r\n" + 
			"          a :Restriction ;\r\n" + 
			"          :allValuesFrom ns:PerformedSession ;\r\n" + 
			"          :onProperty rdf:object\r\n" + 
			"        ]\r\n" + 
			"    ]\r\n" + 
			"  ) ;\r\n" + 
			"  a ns2:ContextProvider ;\r\n" + 
			"  ns2:hasType ns2:gauge .\r\n" + 
			"ns:PerformedSession a :Class .\r\n" + 
			"ns2:gauge a ns2:ContextProviderType .\r\n" + 
			"ns1:User a :Class .";
	public void testSerializationWorkFlow() {
		//String resourcePath="./expand/UAALMessageExample1.json";
		String resourcePath="./expand/jsonFromTurtle.json";
		ContextProvider sp=null;
		
		try {
			InputStream json =  this.getClass().getClassLoader().getResource(resourcePath).openStream();
			JSONLDSerialization ser = new JSONLDSerialization();
			Object serialized = ser.deserialize(json);
			Resource m = (Resource)serialized;
			System.out.println("\n specialized resource \n ");
			System.out.println(m.toStringRecursive());
			assertNotNull(m);
			sp = (ContextProvider)m;
			System.err.println("is well formed "+sp.isWellFormed());
			//http://ontology.universAAL.org/Context.owl#hasType
			assertNotNull(sp);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	
	public void testExpandedJson() {
		String resourcePath="./expandedJson.json";
		ContextProvider sp=null;
		try {
			InputStream json =  this.getClass().getClassLoader().getResource(resourcePath).openStream();
			JSONLDSerialization ser = new JSONLDSerialization();
			Object serialized = ser.deserialize(json);
			Resource m = (Resource)serialized;
			System.out.println("\n specialized resource \n ");
			System.out.println(m.toStringRecursive());
			assertNotNull(m);
			sp = (ContextProvider)m;
			assertNotNull(sp);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	public void testTurtleSerializer() {
		ContextProvider sp=null;
		System.err.println(pattern2);
		TurtleParser parser = new TurtleParser();
		Resource r = (Resource)parser.deserialize(pattern2, null);
		System.out.println(r.toStringRecursive());
		sp = (ContextProvider)r;
		System.out.println(this.jw.serialize(r));
		System.out.println("is well formed "+sp.isWellFormed());

	}

	public void testCompareSerializersResult() throws Exception{
		TurtleParser turtle_parser = new TurtleParser();
		JSONLDSerialization json_parser = new JSONLDSerialization();
		Resource fromTurtle,fromJson;
		fromTurtle = (Resource)turtle_parser.deserialize(pattern2 , null);
		String JsonFromTurtle = jw.serialize(fromTurtle);
		fromJson = (Resource)json_parser.deserialize(JsonFromTurtle);
		System.out.println("----------turtle----------");
		System.out.println(fromTurtle.toStringRecursive());
		System.out.println("----------json----------");
		System.out.println(fromJson.toStringRecursive());
		fromJson.equals(fromTurtle);
		
	}	

}
