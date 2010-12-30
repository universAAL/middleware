package org.universAAL.middleware.rdf;

import java.util.Locale;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.universAAL.middleware.rdf.TypeMapper;

import junit.framework.TestCase;

public class TypeMapperTest extends TestCase {
	public TypeMapperTest(String name) {
		super(name);
	}

	public void testTypeMapper()
	{
		TypeMapper tm = TypeMapper.getTypeMapper();
		DatatypeFactory df = TypeMapper.getDataTypeFactory();
	
		Boolean booleanT = new Boolean(true);
		XMLGregorianCalendar calendarT = df.newXMLGregorianCalendar(2008,5,4,3,2,1,0,0);
		Double doubleT = new Double(1.2);
		Duration durationT = df.newDuration(1234);
		Float floatT = new Float(1.3);
		Integer integerT = new Integer(42);
		Locale localeTshort = new Locale("it","");
		Locale localeTlong = new Locale("it", "IT");		
		Long longT = new Long(2345);
		String stringT = new String("string");
		
		String booleanS[] = tm.getXMLInstance(booleanT);
		String calendarS[] = tm.getXMLInstance(calendarT);
		String doubleS[] = tm.getXMLInstance(doubleT);
		String durationS[] = tm.getXMLInstance(durationT);
		String floatS[] = tm.getXMLInstance(floatT);
		String integerS[] = tm.getXMLInstance(integerT);
		String localeSshort[] = tm.getXMLInstance(localeTshort);
		String localeSlong[] = tm.getXMLInstance(localeTlong);		
		String longS[] = tm.getXMLInstance(longT);
		String stringS[] = tm.getXMLInstance(stringT);
		
		
		assertEquals(booleanT, tm.getJavaInstance(booleanS[0], booleanS[1]));
		assertEquals(calendarT, tm.getJavaInstance(calendarS[0], calendarS[1]));
		assertEquals(doubleT, tm.getJavaInstance(doubleS[0], doubleS[1]));
		assertEquals(durationT, tm.getJavaInstance(durationS[0], durationS[1]));
		assertEquals(floatT, tm.getJavaInstance(floatS[0], floatS[1]));
		assertEquals(integerT, tm.getJavaInstance(integerS[0], integerS[1]));
		assertEquals(localeTshort, tm.getJavaInstance(localeSshort[0], localeSshort[1]));
		assertEquals(localeTlong, tm.getJavaInstance(localeSlong[0], localeSlong[1]));
		assertEquals(longT, tm.getJavaInstance(longS[0], longS[1]));
		assertEquals(stringT, tm.getJavaInstance(stringS[0], stringS[1]));

	}

}
