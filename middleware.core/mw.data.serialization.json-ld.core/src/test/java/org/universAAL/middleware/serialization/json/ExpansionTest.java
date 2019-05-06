package org.universAAL.middleware.serialization.json;

import java.io.InputStream;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.grammar.ExpandJsonLD;

import com.google.gson.JsonElement;


public class ExpansionTest {

	@Test
	public void expand() {

		try {
			InputStream json =  this.getClass().getClassLoader().getResource("example.json").openStream();
		ExpandJsonLD expansor = new ExpandJsonLD(json);
			JsonElement jse = expansor.expand();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
