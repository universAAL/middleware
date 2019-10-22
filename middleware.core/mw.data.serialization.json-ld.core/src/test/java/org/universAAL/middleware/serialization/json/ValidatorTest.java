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
import org.universAAL.middleware.serialization.json.analyzers.ExpandedJsonAnalyzer;
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
			//FIXME fix JsonLDkeywod match JsonLdKeyword.valueOf("@id")
			assertTrue(!doc.validate());	
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
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
			assertFalse(true);
		}
	}
	
	@Test
	public void UAALMessagesValidationTest() {
		try {
			InputStream is = this.getClass().getClassLoader().getResource("./expand/UAALMessageExample1.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			//FIXME fix JsonLDkeywod match JsonLdKeyword.valueOf("@id")
			assertTrue(doc.validate());	
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(false);
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
 			e.printStackTrace();
			assertFalse(false);

		}
	}
	
	//@Test
	public void mergeContextTest() {
		try {
			InputStream is = this.getClass().getClassLoader().getResource("multipleContext.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			assertTrue(doc.validate());
			System.out.println(doc.getActiveContext().getJsonToValidate());
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}
	
	@Test
	public void errorContext() {
		try {
			InputStream is = this.getClass().getClassLoader().getResource("errorContext.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			assertFalse(doc.validate());
			System.out.println(doc.getActiveContext().getJsonToValidate());
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}
	
	
	@Test
	public void remoteContextTest() {
		try {
			InputStream is = this.getClass().getClassLoader().getResource("remoteContext.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			assertTrue(doc.validate());
			System.out.println(doc.getMainJSON());
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}
	

	@Test
	public void expandedJsonValidationTest() {
		try {
			InputStream is = this.getClass().getClassLoader().getResource("./expandedJson.json").openStream();
			JSONLDDocument doc = new JSONLDDocument(is);
			is.close();
			ExpandedJsonAnalyzer exp = new ExpandedJsonAnalyzer(doc.getMainJSON());
			assertTrue(exp.validate());
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
