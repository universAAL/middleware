package org.universAAL.middleware.serialization.json;

import java.io.InputStream;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;


public class ExpansionTest {

	@Test
	public void expand() {

		try {
			InputStream json =  this.getClass().getClassLoader().getResource("example.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			System.out.println("result="+expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	

}
