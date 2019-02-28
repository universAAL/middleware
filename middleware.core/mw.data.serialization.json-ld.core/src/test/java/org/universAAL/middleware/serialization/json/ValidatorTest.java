package org.universAAL.middleware.serialization.json;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.universAAL.middleware.serialization.json.grammar.JSONLDDocument;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

public class ValidatorTest {

	@Test
	public void contextTest() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResource("JSONLDexample1.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		assertTrue(doc.validate());
	}
	@Test
	public void multipleContextTest() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResource("JSONLDexample3.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		assertTrue(doc.validate());
	}
	
	//este test es para probarel caso de tener un array con context dentro
	//nota: el json que esta testeando NO ES UN JSON-LD VALIDO, esta asi solamente para comprobar si es correcta mi interpretacion de la documentacion
	//este caso es solo para probar la clase NodeObject y como la planteo Ale	
	//TODO el analisis de contextos dentro de arrays no esta correcto. ¿bajo que clave se guarda el array de contextos? no puede existir un array sin clave
	//a menos que sea el unico elemento del json
	/*
	 * 			{
	 * 				[ "@context": {},"@context": {},"@context": {},"@context": {}],
	 * 					"":"",
	 * 					"":"",
	 * 					"key":{...}
	 * 			}
	 * 
	 * */

	
	//---------------to test bad syntax of json-ld
	
	/*
	 * test case if the main son is not an object..is an array
	 * */
	@Test (expected = ClassCastException.class)
	public void errors1() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResource("JSONLDexample3.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		assertTrue(doc.validate());
	}
	
	/**
	 * to test if the json has not a correct structure (missing close bracket)
	 * @throws IOException
	 */
	@Test (expected = JsonSyntaxException.class)
	public void errors2() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResource("JSONLDexample4.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		assertTrue(doc.validate());
	}
	@Test 
	public void errors3() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResource("JSONLDexample2.json").openStream();
		JSONLDDocument doc = new JSONLDDocument(is);
		is.close();
		assertTrue(doc.validate());
	}
	


}
