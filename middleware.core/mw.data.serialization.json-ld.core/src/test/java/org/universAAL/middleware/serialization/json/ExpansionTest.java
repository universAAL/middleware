package org.universAAL.middleware.serialization.json;

import java.io.InputStream;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.grammar.ExpandJson;

import com.google.gson.JsonElement;


public class ExpansionTest {

	@Test
	public void expand() {

		try {
			InputStream json =  this.getClass().getClassLoader().getResource("example.json").openStream();
		ExpandJson expansor = new ExpandJson(json);
			JsonElement jse = expansor.expandJsonDocument();
			System.out.println(jse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
