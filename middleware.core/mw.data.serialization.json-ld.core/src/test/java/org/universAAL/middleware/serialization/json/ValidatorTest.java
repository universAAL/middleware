package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.grammar.JSONLDDocument;

public class ValidatorTest {

	@Test
	public void test1() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResource("JSONLDexample1.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		assertTrue(doc.validate());
	}
}
