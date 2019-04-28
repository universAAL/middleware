package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.universAAL.middleware.rdf.Resource;
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
	public void CompleteValidationTest() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResource("example.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		doc.validate();
		//System.out.println(doc.getResource("http://rdf.data-vocabulary.org/#ingredients"));
		Enumeration<Resource> t = doc.getAllResources();
		
		while(t.hasMoreElements()) {
			Resource k =t.nextElement();
			System.out.println(k.getURI());
		}
		
		//assertNotNull(doc.getResource("http://rdf.data-vocabulary.org/#ingredients"));
	}
	
	/**
	 * to test if the json has not a correct structure (missing close bracket)
	 * @throws IOException
	 */
	@Test 
	public void InvalidJSONTest() throws IOException {
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
	public void ContextValidationTest() {
		
		try {
			InputStream is = this.getClass().getClassLoader().getResource("context.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			assertTrue(doc.validate());	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void expandedTermDefinitionTest() {
		try {
			InputStream is = this.getClass().getClassLoader().getResource("expandedTermContext.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			assertTrue(doc.validate());	
		} catch (Exception e) {
			assertFalse(Boolean.FALSE);
			e.printStackTrace();
		}
	}
	
	
//	@Test
//	public void libTest() {
//		try {
//			
//				// Open a valid json(-ld) input file
//				//InputStream inputStream = new FileInputStream("input.json");
//				// Read the file into an Object (The type of this object will be a List, Map, String, Boolean,
//				// Number or null depending on the root object in the file).
//				Object jsonObject = JsonUtils.fromInputStream(this.getClass().getClassLoader().getResource("example.json").openStream());
//				// Create a context JSON map containing prefixes and definitions
//				//Map context = new HashMap();
//				// Customise context...
//				// Create an instance of JsonLdOptions with the standard JSON-LD options
//				JsonLdOptions options = new JsonLdOptions();
//				// Customise options...
//				// Call whichever JSONLD function you want! (e.g. compact)
//				Object compact = JsonLdProcessor.compact(jsonObject,new HashMap(),options);
//				Object expand = JsonLdProcessor.expand(jsonObject, options);
//				// Print out the result (or don't, it's your call!)
//				//System.out.println("algorithm result compacted --> "+JsonUtils.toString(compact));
//				System.out.println("algorithm result expanded --> "+JsonUtils.toString(expand));
//				System.out.println("algorithm result compact --> "+JsonUtils.toString(compact));
//			
//		
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
