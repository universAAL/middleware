package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertFalse;

import java.io.InputStream;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.resourcesGeneartor.UAALResourcesGenerator;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class UaalResourcesTest {
private String turtle ="[{\"@id\":\"_:b0\",\"@type\":[\"http://ontology.universAAL.org/Context.owl#ContextEventPattern\"],\"http://www.w3.org/2000/01/rdf-schema#subClassOf\":[{\"@id\":\"_:b1\"},{\"@id\":\"_:b2\"}]},{\"@id\":\"_:b1\",\"@type\":[\"http://www.w3.org/2002/07/owl#Restriction\"],\"http://www.w3.org/2002/07/owl#allValuesFrom\":[{\"@id\":\"http://ontology.universAAL.org/Profile.owl#User\"}],\"http://www.w3.org/2002/07/owl#onProperty\":[{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#subject\"}]},{\"@id\":\"_:b2\",\"@type\":[\"http://www.w3.org/2002/07/owl#Restriction\"],\"http://www.w3.org/2002/07/owl#allValuesFrom\":[{\"@id\":\"http://ontology.universaal.org/Health.owl#PerformedSession\"}],\"http://www.w3.org/2002/07/owl#onProperty\":[{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#object\"}]},{\"@id\":\"_:b3\",\"http://www.w3.org/1999/02/22-rdf-syntax-ns#first\":[{\"@id\":\"_:b0\"}],\"http://www.w3.org/1999/02/22-rdf-syntax-ns#rest\":[{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"}]},{\"@id\":\"http://ontology.universAAL.org/Context.owl#ContextEventPattern\"},{\"@id\":\"http://ontology.universAAL.org/Context.owl#ContextProvider\"},{\"@id\":\"http://ontology.universAAL.org/Context.owl#ContextProviderType\"},{\"@id\":\"http://ontology.universAAL.org/Context.owl#gauge\",\"@type\":[\"http://ontology.universAAL.org/Context.owl#ContextProviderType\"]},{\"@id\":\"http://ontology.universAAL.org/Profile.owl#User\",\"@type\":[\"http://www.w3.org/2002/07/owl#Class\"]},{\"@id\":\"http://ontology.universaal.org/Health.owl#PerformedSession\",\"@type\":[\"http://www.w3.org/2002/07/owl#Class\"]},{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"},{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#object\"},{\"@id\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#subject\"},{\"@id\":\"http://www.w3.org/2002/07/owl#Class\"},{\"@id\":\"http://www.w3.org/2002/07/owl#Restriction\"},{\"@id\":\"https://rest.activage.lst.tfo.upm.es/uaal/spaces/equimetrix/context/publishers/backup\",\"http://ontology.universAAL.org/Context.owl#myClassesOfEvents\":[{\"@id\":\"_:b3\"}],\"@type\":[\"http://ontology.universAAL.org/Context.owl#ContextProvider\"],\"http://ontology.universAAL.org/Context.owl#hasType\":[{\"@id\":\"http://ontology.universAAL.org/Context.owl#gauge\"}]}]";


	
	@Test
	public void test() {
		try {
			JsonParser p = new JsonParser();
			JsonArray array = p.parse(turtle ).getAsJsonArray();
			UAALResourcesGenerator generator = new UAALResourcesGenerator(array);
			generator.generateResources();
			System.out.println(generator.getMainResource().toStringRecursive());
		} catch (Exception e) {
				e.printStackTrace();
				assertFalse(true);
		} 
	}
}
