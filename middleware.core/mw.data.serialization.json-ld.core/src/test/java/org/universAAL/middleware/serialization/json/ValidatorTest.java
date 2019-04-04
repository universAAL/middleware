package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.grammar.JSONLDDocument;

import com.google.gson.stream.MalformedJsonException;

public class ValidatorTest {

	@Test
	public void SimpleContext() throws IOException {
		boolean status;
		//ReferencedContext.json
		InputStream is = this.getClass().getClassLoader().getResource("SImpleContext.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		status=doc.validate();
		System.out.println(status);
		assertTrue(status);
	}
	@Test
	public void multipleContextTest() throws IOException {
		boolean status;
		InputStream is = this.getClass().getClassLoader().getResource("MultipleContext.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		status=doc.validate();
		System.out.println(status);
		assertTrue(status);
	}
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
	@Test (expected = IOException.class)
	public void InvalidJSON() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResource("rrrr.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		assertTrue(doc.validate());
	}

	


}
