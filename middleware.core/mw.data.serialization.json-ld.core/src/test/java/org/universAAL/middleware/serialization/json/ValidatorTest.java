package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.grammar.ContextDefinition;
import org.universAAL.middleware.serialization.json.grammar.JSONLDDocument;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

public class ValidatorTest {

	@Test
	public void SimpleContext() throws IOException {
		boolean status;
		//ReferencedContext.json
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
	

	


}
