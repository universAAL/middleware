package org.universAAL.middleware.rdf;

import java.util.Locale;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;

public class TypeMapperTest extends TestCase {
    public TypeMapperTest(String name) {
	super(name);
    }

    public void testTypeMapper() {
	DatatypeFactory df = TypeMapper.getDataTypeFactory();

	Boolean booleanT = new Boolean(true);
	XMLGregorianCalendar calendarT = df.newXMLGregorianCalendar(2008, 5, 4,
		3, 2, 1, 0, 0);
	Double doubleT = new Double(1.2);
	Duration durationT = df.newDuration(1234);
	Float floatT = new Float(1.3);
	Integer integerT = new Integer(42);
	Locale localeTshort = new Locale("it", "");
	Locale localeTlong = new Locale("it", "IT");
	Long longT = new Long(2345);
	String stringT = new String("string");

	String booleanS[] = TypeMapper.getXMLInstance(booleanT);
	String calendarS[] = TypeMapper.getXMLInstance(calendarT);
	String doubleS[] = TypeMapper.getXMLInstance(doubleT);
	String durationS[] = TypeMapper.getXMLInstance(durationT);
	String floatS[] = TypeMapper.getXMLInstance(floatT);
	String integerS[] = TypeMapper.getXMLInstance(integerT);
	String localeSshort[] = TypeMapper.getXMLInstance(localeTshort);
	String localeSlong[] = TypeMapper.getXMLInstance(localeTlong);
	String longS[] = TypeMapper.getXMLInstance(longT);
	String stringS[] = TypeMapper.getXMLInstance(stringT);

	assertEquals(booleanT, TypeMapper.getJavaInstance(booleanS[0],
		booleanS[1]));
	assertEquals(calendarT, TypeMapper.getJavaInstance(calendarS[0],
		calendarS[1]));
	assertEquals(doubleT, TypeMapper
		.getJavaInstance(doubleS[0], doubleS[1]));
	assertEquals(durationT, TypeMapper.getJavaInstance(durationS[0],
		durationS[1]));
	assertEquals(floatT, TypeMapper.getJavaInstance(floatS[0], floatS[1]));
	assertEquals(integerT, TypeMapper.getJavaInstance(integerS[0],
		integerS[1]));
	assertEquals(localeTshort, TypeMapper.getJavaInstance(localeSshort[0],
		localeSshort[1]));
	assertEquals(localeTlong, TypeMapper.getJavaInstance(localeSlong[0],
		localeSlong[1]));
	assertEquals(longT, TypeMapper.getJavaInstance(longS[0], longS[1]));
	assertEquals(stringT, TypeMapper
		.getJavaInstance(stringS[0], stringS[1]));

    }

}
