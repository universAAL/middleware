package org.universAAL.middleware.serialization.json;

public class ExpansionTest {
	/*
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
	public void arrayTest() {
		expected = "[ { \"@type\": [ \"http://ontology.universAAL.org/Context.owl#ContextProvider\" ], \"http://rdf.data-vocabulary.org/#ingredients\": [ { \"@value\": \"12 fresh mint leaves\" }, { \"@value\": \"1/2 lime, juiced with pulp\" }, { \"@value\": \"1 tablespoons white sugar\" }, { \"@value\": \"1 cup ice cubes\" }, { \"@value\": \"2 fluid ounces white rum\" }, { \"@value\": \"1/2 cup club soda\" } ], \"http://rdf.data-vocabulary.org/#instructions\": [ { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Crush lime juice, mint and sugar together in glass.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 1 } ] }, { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Fill glass to top with ice cubes.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 2 } ] }, { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Pour white rum over ice.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 3 } ] }, { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Fill the rest of glass with club soda, stir.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 4 } ] }, { \"http://rdf.data-vocabulary.org/#description\": [ { \"@value\": \"Garnish with a lime wedge.\" } ], \"http://rdf.data-vocabulary.org/#step\": [ { \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\", \"@value\": 5 } ] } ], \"http://rdf.data-vocabulary.org/#name\": [ { \"@value\": \"Mojito\" } ], \"http://rdf.data-vocabulary.org/#yield\": [ { \"@value\": \"1 cocktail\" } ] } ] ";
	try {
		InputStream json =  this.getClass().getClassLoader().getResource("./example.json").openStream();
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
		expected="[ { \"@id\": \"urn:org.universAAL.middleware.context.rdf:ContextEvent#_:c0a8012ac02c0b4c:58d\", \"@type\": [ \"http://ontology.universAAL.org/Context.owl#ContextProvider\" ], \"http://ontology.universAAL.org/Context.owl#hasType\": [ { \"@id\": \"http://ontology.universAAL.org/Context.owl#gauge\", \"@type\": [ \"http://ontology.universAAL.org/Context.owl#ContextProviderType\" ] } ] } ] ";
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
	
	*/
}
