package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.universAAL.middleware.serialization.json.algorithms.ExpandJSONLD;



public class ExpansionTest {

	
	@Before
	public void init() {
		PropertyConfigurator.configure("src/test/resources/logj4ConfigFile/log4j.properties");
		
	}
	
	@Test
	public void simpleMapping() {
		//testing "key":"value" in context
		String expected ="[{'http://schema.org/name':[{'@value':'TheEmpireStateBuilding'}]}]";
		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/simpleKV.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			assertTrue(expansor.getExpandedJson().toString().replaceAll("\"", "'").replaceAll(" " , "").equals(expected));
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void complexMapping() {

		

		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/complexKV.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	} 
	
	
	@Test
	public void typeAsIDTest() {
		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/typeID.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void fullExpand() {

		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/complex2.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void compactIRITest() {
		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/compactIRI.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	@Test
	public void multipleTypesTest() {
		try {
			InputStream json =  this.getClass().getClassLoader().getResource("./expand/multipleType.json").openStream();
			ExpandJSONLD expansor = new ExpandJSONLD(json);
			expansor.expand();
			System.out.println(expansor.getExpandedJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
