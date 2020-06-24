package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class ExpansionTest {
	private InputStream json;
	private JsonParser parser;
	private JsonArray expected_json;
	private String expected;
	private ExpandJSONLD expansor;

	@Before
	public void init() {
		//PropertyConfigurator.configure("src/test/resources/logj4ConfigFile/log4j.properties");
		 parser= new JsonParser();
	}
	
	
	@Test
	public void simpleMapping() {
		 expected ="[{'http://schema.org/name':[{'@value':'The Empire State Building'}]}]";
		try {
			expected_json = parser.parse(expected).getAsJsonArray();
			json =  this.loadJson("./expand/simpleKV.json");
			expansor = new ExpandJSONLD(json);
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
			json = this.loadJson("./expand/complexKV.json");
			expansor = new ExpandJSONLD(json);
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
			json =  this.loadJson("./expand/typeID.json");
			expansor = new ExpandJSONLD(json);
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
			json =  this.loadJson("./expand/complex2.json");
			expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(parser.parse(expected).getAsJsonArray(),expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void arrayTest() {
		expected = "[ { \"@type\": [ \"http://ontology.universAAL.org/Context.owl#ContextProvider\" ], \"http://rdf.data-vocabulary.org/#ingredients\": [ { \"@value\": \"12 fresh mint leaves\" }, { \"@value\": \"1/2 lime, juiced with pulp\" }, { \"@value\": \"1 tablespoons white sugar\" }, { \"@value\": \"1 cup ice cubes\" }, { \"@value\": \"2 fluid ounces white rum\" }, { \"@value\": \"1/2 cup club soda\" } ], \"http://rdf.data-vocabulary.org/#instructions\": [ { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Crush lime juice, mint and sugar together in glass.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 1 } ] }, { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Fill glass to top with ice cubes.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 2 } ] }, { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Pour white rum over ice.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 3 } ] }, { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Fill the rest of glass with club soda, stir.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 4 } ] }, { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Garnish with a lime wedge.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 5 } ] } ], \"http://rdf.data-vocabulary.org/#name\": [ { \"@value\": \"Mojito\" } ], \"http://rdf.data-vocabulary.org/#yield\": [ { \"@value\": \"1 cocktail\" } ] } ] ";
	try {
		json =  this.loadJson("./example.json");
		expansor = new ExpandJSONLD(json);
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
			json =  this.loadJson("./expand/compactIRI.json");
			expansor = new ExpandJSONLD(json);
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
		expected="[ { \"@id\": \"urn:org.universAAL.middleware.context.rdf:ContextEvent#_:c0a8012ac02c0b4c:58d\", \"@type\": [ \"http://ontology.universAAL.org/Context.owl#ContextProvider\" ], \"http://ontology.universAAL.org/Context.owl#hasType\": [ { \"@id\": \"http://ontology.universAAL.org/Context.owl#gauge\", \"@type\": [ \"http://ontology.universAAL.org/Context.owl#ContextProviderType\" ] } ] } ] ";
		try {
			json =  this.loadJson("./expand/UAALMessageExample1.json");
			expansor = new ExpandJSONLD(json);
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
			json =  this.loadJson("./expand/multipleType.json");
			expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(parser.parse(expected).getAsJsonArray(),expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);

		}
		
	}
	
	@Test
	public void objectReferencesTest() {
		this.expected="[{\"@id\": \"urn:org.universAAL.middleware.context.rdf:ContextEvent#_:9e2aa729ac420ba3:182a\",\"@type\": [\"http://ontology.universAAL.org/Context.owl#ContextEvent\"],\"http://www.w3.org/1999/02/22-rdf-syntax-ns#object\": [{\"@id\": \"http://www.some.example.id.com\"}],\"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate\": [{\"@id\": \"http://ontology.universAAL.org/Context.owl#hasValue\"}],\"http://www.w3.org/1999/02/22-rdf-syntax-ns#subject\": [{\"http://ontology.universAAL.org/Context.owl#hasValue\": [{\"@id\": \"http://www.some.example.id.com\",\"@type\": [\"http://ontology.universAAL.org/Context.owl#gameSession\"],\"http://aha-ontology.activageproject.eu/games.owl#max_rt\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#float\",\"@value\": 963}],\"http://aha-ontology.activageproject.eu/games.owl#min_rt\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#float\",\"@value\": 593}],\"http://aha-ontology.activageproject.eu/games.owl#solved\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\": true}],\"http://aha-ontology.activageproject.eu/games.owl#start\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\"@value\": 1543956968}]}]}]}]";
		try {
			json =  this.loadJson("./expand/ContextEvent.json");
			expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(parser.parse(expected).getAsJsonArray(),expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);

		}
	}
	
	@Test
	public void uaalGamesCETest() {
		this.expected="[{\"@id\": \"urn:org.universAAL.middleware.context.rdf:ContextEvent#_:9e2aa729ac420ba3:182a\",\"@type\": [\"http://ontology.universAAL.org/Context.owl#ContextEvent\"],\"http://www.w3.org/1999/02/22-rdf-syntax-ns#object\": [{\"@id\": \"_:object\",\"@type\": [\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#GameSession\",\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#Error\",\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#Performance\",\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#RealTime\"],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#difficulty\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\"@value\": 4}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#duration\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#duration\",\"@value\": \"-PT1S\"}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#errorsInSession\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\"@value\": 3}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#hasPlayer\": [{\"@id\": \"http://www.some.example.id.com\"}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#level\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\"@value\": \"2\"}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#max_rt\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#float\",\"@value\": 8606}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#media_rt\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#float\",\"@value\": 6364.5}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#min_rt\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#float\",\"@value\": 4204}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#moves\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\"@value\": 3}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#name\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#string\",\"@value\": \"series\"}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#solved\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\": true}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#standard_rt\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#float\",\"@value\": 1649.8}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#start\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\"@value\": \"2018-11-27T19:00:33\"}],\"http://www.semanticweb.org/activage/ontologies/2018/4/activage-core#timeout\": [{\"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\"@value\": 0}]}],\"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate\": [{\"@id\": \"http://ontology.universAAL.org/Context.owl#hasValue\"}],\"http://www.w3.org/1999/02/22-rdf-syntax-ns#subject\": [{\"@id\": \"http://www.some.example.id.com\",\"@type\": [\"http://ontology.universAAL.org/Profile.owl#User\",\"http://inter-iot.eu/GOIoTP#User\"],\"http://ontology.universAAL.org/Context.owl#hasValue\": [{\"@id\": \"_:object\"}]}]}]";
		try {
			json =  this.loadJson("./expand/UAALGamesCE.json");
			expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(compareJsons(parser.parse(expected).getAsJsonArray(),expansor.getExpandedJson()));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);

		}
	}
	
	

	
	private InputStream loadJson(String path) throws Exception {
		return this.getClass().getClassLoader().getResource(path).openStream();
	}
	
	private boolean compareJsons(JsonArray expected,JsonArray given) {
		boolean v =expected.equals(given);
	
		if(!v) {
			System.out.println("expected \n"+expected);
			System.out.println("given \n"+given);	
		}
		return v;
	}
	
	
}
