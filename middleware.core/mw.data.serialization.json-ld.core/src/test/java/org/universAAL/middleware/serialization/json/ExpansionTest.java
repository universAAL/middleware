package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class ExpansionTest {
	JsonParser parser;
	JsonArray expected_json;
	String expected;

	@Before
	public void init() {
		PropertyConfigurator.configure("src/test/resources/logj4ConfigFile/log4j.properties");
		 parser= new JsonParser();
	}
	
	
	@Test
	public void simpleMapping() {
		 expected ="[{'http://schema.org/name':[{'@value':'The Empire State Building'}]}]";
		try {
			expected_json = parser.parse(expected).getAsJsonArray();
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/simpleKV.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(expected_json,expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void complexMapping() {
		 expected ="[{\"http://schema.org/geo\":[{\"http://schema.org/latitude\":[{\"@value\":\"40.75\",\"@type\":\"xsd:float\"}],\"http://schema.org/longitude\":[{\"@value\":\"73.98\",\"@type\":\"xsd:float\"}]}]}]\r\n";
		try {
			expected_json= parser.parse(expected).getAsJsonArray();
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/complexKV.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(expected_json,expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	
	} 

	@Test
	public void typeAsIDTest() {
		expected ="[{\"http://schema.org/image\":[{\"@id\":\"http://www.civil.usherbrooke.ca/cours/gci215a/empire-state-building.jpg\"}]}]";
		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/typeID.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(parser.parse(expected).getAsJsonArray(),expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false); 
		}
	}
	
	
	@Test
	public void fullExpand() {
			expected ="[ { \"@id\": \"http://example.org/cars/for-sale#tesla\", \"@type\": [ \"http://purl.org/goodrelations/v1#Offering\" ], \"http://purl.org/goodrelations/v1#acceptedPaymentMethods\": [ { \"@id\": \"http://purl.org/goodrelations/v1#Cash\" } ], \"http://purl.org/goodrelations/v1#description\": [ { \"@value\": \"Need to sell fast and furiously\" } ], \"http://purl.org/goodrelations/v1#hasBusinessFunction\": [ { \"@id\": \"http://purl.org/goodrelations/v1#Sell\" } ], \"http://purl.org/goodrelations/v1#hasPriceSpecification\": [ { \"http://purl.org/goodrelations/v1#hasCurrency\": [ { \"@value\": \"USD\" } ], \"http://purl.org/goodrelations/v1#hasCurrencyValue\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#float\", \"@value\": \"85000\" } ] } ], \"http://purl.org/goodrelations/v1#includes\": [ { \"@type\": [ \"http://purl.org/goodrelations/v1#Individual\", \"http://www.productontology.org/id/Vehicle\" ], \"http://xmlns.com/foaf/0.1/page\": [ { \"@id\": \"http://www.teslamotors.com/roadster\" } ], \"http://purl.org/goodrelations/v1#name\": [ { \"@value\": \"Tesla Roadster\" } ] } ], \"http://purl.org/goodrelations/v1#name\": [ { \"@value\": \"Used Tesla Roadster\" } ] } ]";
		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/complex2.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(parser.parse(expected).getAsJsonArray(),expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void compactIRITest() {
		expected="[ { \"@id\": \"http://example.org/cars/for-sale#tesla\", \"@type\": [ \"http://purl.org/goodrelations/v1/Offering\" ], \"http://purl.org/goodrelations/v1/hasBusinessFunction\": [ { \"@id\": \"http://purl.org/goodrelations/v1/Sell\" } ] } ]";
		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/compactIRI.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons( parser.parse(expected).getAsJsonArray(),expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);

		}
		
	}
	
	@Test
	public void UAALMessajeExpandTest() {
		expected="[ { \"@id\": \"urn:org.universAAL.middleware.context.rdf:ContextEvent#_:c0a8012ac02c0b4c:58d\", \"@type\": [ \"http://ontology.universAAL.org/Measurement.owl#ContextEvent\" ], \"http://www.w3.org/1999/02/22-rdf-syntax-ns#object\": [ { \"@value\": \"2\" } ], \"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate\": [ { \"@id\": \"http://ontology.universAAL.org/Measurement.owl#value\" } ], \"http://www.w3.org/1999/02/22-rdf-syntax-ns#subject\": [ { \"@id\": \"https://github.com/soad03/middleware/tree#URIcompactor_update\", \"@type\": [ \"http://ontology.universAAL.org/Measurement.owl#Measurement\" ], \"http://ontology.universAAL.org/Measurement.owl#value\": [ { \"@value\": \"2\" } ] } ], \"http://ontology.universAAL.org/Measurement.owl#hasTimestamp\": [ { \"@value\": \"1562249774498\" } ] } ]";
		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/UAALmessageExample1.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(parser.parse(expected).getAsJsonArray(),expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
	}
	
	@Test
	public void multipleTypesTest() {
		this.expected="[ { \"http://purl.org/goodrelations/v1#includes\": [ { \"@type\": [ \"http://purl.org/goodrelations/v1#Individual\", \"http://www.productontology.org/id/Vehicle\" ] } ] } ]";
		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/multipleType.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(parser.parse(expected).getAsJsonArray(),expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);

		}
		
	}
	
	private boolean compareJsons(JsonArray expected,JsonArray given) {
		if(!expected.equals(given)) {
			System.out.println("expected \n"+expected);
			System.out.println("given \n"+given);	
		}
		return expected.equals(given);
	}
}
