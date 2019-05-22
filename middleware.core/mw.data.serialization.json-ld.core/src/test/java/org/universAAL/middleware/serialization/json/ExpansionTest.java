package org.universAAL.middleware.serialization.json;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;



public class ExpansionTest {

	@Test
	public void expand() {
		
		PropertyConfigurator.configure("src/test/resources/logj4ConfigFile/log4j.properties");

		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/simple.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	

}
