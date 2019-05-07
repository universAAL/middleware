package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.universAAL.middleware.serialization.json.grammar.JSONLDDocument;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

public class ResourcesGeneratorTest {

	JSONLDDocument toParse = null;
	
	
	@Before
	public void init() {
		try {
			this.toParse = new JSONLDDocument(this.getClass().getClassLoader().getResource("expandedFullJson.json").openStream());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	@Test
	public void generateResource() {
	}
}
