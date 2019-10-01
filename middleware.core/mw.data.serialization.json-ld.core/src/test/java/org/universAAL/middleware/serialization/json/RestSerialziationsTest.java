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
import org.universAAL.middleware.util.Specializer;
/**
 * 
 * @author Eduardo Buhid
 *
 */
public class RestSerialziationsTest extends BusTestCase {
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

	
	public void testSerializationWorkFlow() {
		String resourcePath="./expand/UAALMessageExample1.json";
		ContextProvider sp=null;
		//String resourcePath="./expand/simple.json";
		try {
			System.out.println(ContextProvider.MY_URI);
			InputStream json =  this.getClass().getClassLoader().getResource(resourcePath).openStream();
			JSONLDSerialization ser = new JSONLDSerialization();
			Object serialized = ser.deserialize(json);
			Resource m = (Resource)serialized;
			System.out.println("not specialized ");
			System.out.println(m.toStringRecursive());
			Resource specialized = new Specializer().specialize(m);
			System.out.println("specialized ");
			System.out.println(specialized.toStringRecursive());
			sp = (ContextProvider)specialized;
			assertNotNull(m);
			assertNotNull(sp);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	public void testSerializeWithTurtle() {
		 //String jsonFromturtle ="[{\"@id\":\"_:b0\",\"@type\":[\"http://ontology.universAAL.org/Context.owl#ContextEventPattern\"],\"http://www.w3.org/2000/01/rdf-schema#subClassOf\":[{\"@id\":\"_:b1\"},{\"@id\":\"_:b2\"}]},{\"@id\":\"_:b1\",\"@type\":[\"http://www.w3.org/2002/07/owl#Restriction\"],\"http://www.w3.org/2002/07/owl#allValuesFrom\":[{\"@id\":\"http://ontology.universAAL.org/Profile.owl#User\"}],\"http://www.w3.org/2002/07/owl#onProperty\":[{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#subject\"}]},{\"@id\":\"_:b2\",\"@type\":[\"http://www.w3.org/2002/07/owl#Restriction\"],\"http://www.w3.org/2002/07/owl#allValuesFrom\":[{\"@id\":\"http://ontology.universaal.org/Health.owl#PerformedSession\"}],\"http://www.w3.org/2002/07/owl#onProperty\":[{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#object\"}]},{\"@id\":\"_:b3\",\"http://www.w3.org/1999/02/22-rdf-syntax-ns#first\":[{\"@id\":\"_:b0\"}],\"http://www.w3.org/1999/02/22-rdf-syntax-ns#rest\":[{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"}]},{\"@id\":\"http://ontology.universAAL.org/Context.owl#ContextEventPattern\"},{\"@id\":\"http://ontology.universAAL.org/Context.owl#ContextProvider\"},{\"@id\":\"http://ontology.universAAL.org/Context.owl#ContextProviderType\"},{\"@id\":\"http://ontology.universAAL.org/Context.owl#gauge\",\"@type\":[\"http://ontology.universAAL.org/Context.owl#ContextProviderType\"]},{\"@id\":\"http://ontology.universAAL.org/Profile.owl#User\",\"@type\":[\"http://www.w3.org/2002/07/owl#Class\"]},{\"@id\":\"http://ontology.universaal.org/Health.owl#PerformedSession\",\"@type\":[\"http://www.w3.org/2002/07/owl#Class\"]},{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"},{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#object\"},{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#subject\"},{\"@id\":\"http://www.w3.org/2002/07/owl#Class\"},{\"@id\":\"http://www.w3.org/2002/07/owl#Restriction\"},{\"@id\":\"https://rest.activage.lst.tfo.upm.es/uaal/spaces/equimetrix/context/publishers/backup\",\"http://ontology.universAAL.org/Context.owl#myClassesOfEvents\":[{\"@id\":\"_:b3\"}],\"@type\":[\"http://ontology.universAAL.org/Context.owl#ContextProvider\"],\"http://ontology.universAAL.org/Context.owl#hasType\":[{\"@id\":\"http://ontology.universAAL.org/Context.owl#gauge\"}]}]";		
		//String JJ ="@prefix ns: <http://ontology.universaal.org/Health.owl#> . @prefix ns1: <http://ontology.universAAL.org/Profile.owl#> . @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . @prefix ns2: <http://ontology.universAAL.org/Context.owl#> . @prefix : <http://www.w3.org/2002/07/owl#> . <https://rest.activage.lst.tfo.upm.es/uaal/spaces/equimetrix/context/publishers/backup> ns2:myClassesOfEvents ( [ a ns2:ContextEventPattern ; <http://www.w3.org/2000/01/rdf-schema#subClassOf> [ a :Restriction ; :allValuesFrom ns1:User ; :onProperty rdf:subject ] , [ a :Restriction ; :allValuesFrom ns:PerformedSession ; :onProperty rdf:object ] ] ) ; a ns2:ContextProvider ; ns2:hasType ns2:gauge . ns:PerformedSession a :Class . ns2:gauge a ns2:ContextProviderType . ns1:User a :Class . ";
		ContextProvider sp=null;
		try {
			TurtleParser turtleParser = new TurtleParser();
			Resource FROMturtle = (Resource)turtleParser.deserialize(pattern, null);
			Resource m = (Resource)FROMturtle;
			Resource specialized = new Specializer().specialize(m);
			sp = (ContextProvider)specialized;
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	


}
