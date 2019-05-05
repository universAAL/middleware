package org.universAAL.middleware.serialization.json;

import java.io.InputStream;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.algorithms.Context;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJsonLD;

public class ExpansionTest {

	@Test
	public void expand() {

		try {
			InputStream json =  this.getClass().getClassLoader().getResource("example.json").openStream();
		
			ExpandJsonLD expand = new ExpandJsonLD(json);
			expand.expandJsonDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
