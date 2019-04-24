package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.grammar.ContextDefinition;
import org.universAAL.middleware.serialization.json.grammar.JSONLDDocument;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

public class ValidatorTest {

	@Test
	public void SimpleContext() throws IOException {
		boolean status;
		// FIXME bug into context key validation
		InputStream is = this.getClass().getClassLoader().getResource("SimpleContext.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		status=doc.validate();
		System.out.println(status);
		assertTrue(status);
	}
//	@Test
//	public void multipleContextTest() throws IOException {
//		boolean status;
//		InputStream is = this.getClass().getClassLoader().getResource("MultipleContext.json").openStream();
//		JSONLDDocument doc = new JSONLDDocument(is);
//		is.close();
//		status=doc.validate();
//		System.out.println(status);
//		assertTrue(status);
//	}
	@Test
	public void referencedContext() throws IOException {
		boolean status;
		InputStream is = this.getClass().getClassLoader().getResource("ReferencedContext.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		status=doc.validate();
		System.out.println(status);
		assertTrue(status);
	}

	/**
	 * to test if the json has not a correct structure (missing close bracket)
	 * @throws IOException
	 */
	@Test 
	public void InvalidJSON() throws IOException {
		try {
			InputStream is = this.getClass().getClassLoader().getResource("ErrorJSON.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			assertTrue(doc.validate());	
		} catch (Exception e) {
			
			assertFalse(Boolean.FALSE);
			e.printStackTrace();
		}
		
	}
	@Test
	public void GsonTest() throws Exception {
		JsonParser parser = new JsonParser();
		JsonObject obj ;
		String json = "{\r\n" + 
				"  \"@context\": {\r\n" + 
				"    \"ical\": \"http://www.w3.org/2002/12/cal/ical#\",\r\n" + 
				"    \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",\r\n" + 
				"    \"ical:dtstart\": {\r\n" + 
				"      \"@type\": \"xsd:dateTime\"\r\n" + 
				"    },\r\n" + 
				"    \"chango\":{\"@id\":\"http://cango.com.ar\",\"@type\":\"@id\"}\r\n" + 
				"    \r\n" + 
				"  },\r\n" + 
				"  \"chango\":{ \r\n" + 
				"    \"ical:summary\": \"Lady Gaga Concert\",\r\n" + 
				"  \"ical:location\": \"New Orleans Arena, New Orleans, Louisiana, USA\",\r\n" + 
				"  \"ical:dtstart\": \"2011-04-09T20:00:00Z\"\r\n" + 
				"  }\r\n" + 
				"  \r\n" + 
				"}";
		obj = (JsonObject)parser.parse(json);
		System.out.println(obj);
		obj.remove("@context");
		System.out.println(obj);
		System.out.println(obj.isJsonObject());
		System.out.println(obj.get("chango"));
		obj.entrySet().stream().forEach(l->System.out.println(l+"\n"));
	}
	
	@Test
	public void libTest() {
		try {
			
				// Open a valid json(-ld) input file
				//InputStream inputStream = new FileInputStream("input.json");
				// Read the file into an Object (The type of this object will be a List, Map, String, Boolean,
				// Number or null depending on the root object in the file).
				Object jsonObject = JsonUtils.fromInputStream(this.getClass().getClassLoader().getResource("example.json").openStream());
				// Create a context JSON map containing prefixes and definitions
				//Map context = new HashMap();
				// Customise context...
				// Create an instance of JsonLdOptions with the standard JSON-LD options
				JsonLdOptions options = new JsonLdOptions();
				// Customise options...
				// Call whichever JSONLD function you want! (e.g. compact)
				Object compact = JsonLdProcessor.compact(jsonObject,new HashMap(),options);
				Object expand = JsonLdProcessor.expand(jsonObject, options);
				// Print out the result (or don't, it's your call!)
				//System.out.println("algorithm result compacted --> "+JsonUtils.toString(compact));
				System.out.println("algorithm result expanded --> "+JsonUtils.toString(expand));
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void compactedTest() {
		try {
			InputStream is = this.getClass().getClassLoader().getResource("compacted.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			assertTrue(doc.validate());	
		} catch (Exception e) {
			
			assertFalse(Boolean.FALSE);
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void expandedTest() {
		try {
			InputStream is = this.getClass().getClassLoader().getResource("expanded.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			assertTrue(doc.validate());	
		} catch (Exception e) {
			
			assertFalse(Boolean.FALSE);
			e.printStackTrace();
		}
	}
}
