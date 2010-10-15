/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
	Copyright 2008-2010 Vodafone Italy, http://www.vodafone.it
	Vodafone Omnitel N.V.
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package org.persona.middleware;

import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.datatype.*;

import org.persona.ontology.ManagedIndividual;

/**
 * @author mtazari
 * @author mfreddi
 *
 */
public class TypeMapper {
	
	public static final String XSD_NAMESPACE = XMLConstants.W3C_XML_SCHEMA_NS_URI + "#";

	private static Hashtable javaXSD = new Hashtable(), xsdJava = new Hashtable();
	
	private static DatatypeFactory df;
	
	private static TypeMapper tm = null;
	
	static {
		//Create hashtables for classes and xsd types
		javaXSD.put(PResource.class, "anyURI");
		javaXSD.put(Boolean.class, "boolean");
		javaXSD.put(XMLGregorianCalendar.class, "dateTime");
		javaXSD.put(Double.class, "double");
		javaXSD.put(Duration.class, "duration");
		javaXSD.put(Float.class, "float");
		javaXSD.put(Integer.class, "int");
		javaXSD.put(Locale.class, "language");
		javaXSD.put(Long.class, "long");
		javaXSD.put(String.class, "string");
		
		xsdJava.put("anyURI",PResource.class);
		xsdJava.put("boolean",Boolean.class);
		xsdJava.put("dateTime",XMLGregorianCalendar.class);
		xsdJava.put("double",Double.class);
		xsdJava.put("duration",Duration.class);
		xsdJava.put("float",Float.class);
		xsdJava.put("int",Integer.class);
		xsdJava.put("language",Locale.class);
		xsdJava.put("long",Long.class);
		xsdJava.put("string",String.class);
		
		//Retrieve instance of datatype factory which is needed for calendar and duration objects
		try {
			df = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public static XMLGregorianCalendar getCurrentDateTime() {
		return df.newXMLGregorianCalendar(new GregorianCalendar());
	}

	private TypeMapper(){}
	
	/**
	 * Return the only allowed instance of TypeMapper
	 * @return {@link TypeMapper}
	 */
	public static TypeMapper getTypeMapper() {
		if (tm == null)
			tm = new TypeMapper();
		
		return tm;
	}

	/**
	 * Return an instance of DatatypeFactory
	 * @return {@link DatatypeFactory}
	 */
	public static DatatypeFactory getDataTypeFactory() {
		return df;
	}

	/**
	 * Return the lexical string and XSD type from an object which can be mapped to XSD type
	 * @param o The Object to be serialized 
	 * @return An array of 2 strings with the lexical XML form and associated XSD type
	 */
	public String[] getXMLInstance(Object o){
		
		if (o instanceof Locale){
			Locale l = (Locale) o;
			if (l.getCountry()=="")
				return new String[] {l.getLanguage(), getDatatypeURI(o)};
			else
				return new String[] {l.getLanguage()+"-"+l.getCountry(), getDatatypeURI(o)};
		}
		
		return new String[] {o.toString(), getDatatypeURI(o)};
	}
	
	/**
	 * Return a Java object from an XML literal and its type
	 * @param lexicalForm The serialized string
	 * @param xsdType The XSD type of the object
	 * @return A Java object or null if it can not be mapped to an appropriate object
	 */
	public Object getJavaInstance(String lexicalForm, String xsdType){
		if (xsdType == null  ||  lexicalForm == null)
			return lexicalForm;
		
		Class c = (Class) xsdJava.get(xsdType.substring(XSD_NAMESPACE.length())); 
		if (c == null)
			return null;
		
		try {
			// XMLGregorianCalendar and Duration have a private constructor, so it is needed to use the appropriate factory for creation
			if (c.equals(XMLGregorianCalendar.class))
				return df.newXMLGregorianCalendar(lexicalForm);
			else if (c.equals(Duration.class))
				return df.newDuration(lexicalForm);
			else if (c.equals(Locale.class)){
				int pos = lexicalForm.indexOf('-');
				if (pos==-1)
					return new Locale(lexicalForm, "");
				else
					return new Locale(lexicalForm.substring(0,pos), lexicalForm.substring(pos+1));
			} else if (c.equals(PResource.class)) {
				return new PResource(lexicalForm, true);
			}
	
			return c.getConstructor(new Class[] {String.class}).newInstance(new Object[] {lexicalForm});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Object asLiteral(Object o) {
		if (o instanceof List)
			return PResource.asRDFList((List) o, true);
		if (o instanceof PResource) {
			if (!((PResource) o).serializesAsXMLLiteral())
				if (o instanceof ManagedIndividual)
					return ((ManagedIndividual) o).copyAsXMLLiteral();
				else
					return ((PResource) o).copyAsXMLLiteral();
		} else if (getDatatypeURI(o) == null)
			return null;
		return o;
	}
	
	/**
	 * Return the XSD type given a java class, according to the list of predefined types
	 * @param c The Java class
	 * @return the XSD String associated with class or null if it can not be mapped to an appropriate object
	 */
	public static String getDatatypeURI(Class c) {
		if (c == null)
			return null;
		
		Object o = javaXSD.get(c);
		
		// If the class is not present in the hashtable, check if it is a subclass of an existent class in the hashtable
		if (o == null)
		{
			Class tmpClass;
			Iterator i = javaXSD.keySet().iterator();
			while (i.hasNext()) {
				tmpClass = (Class) i.next();
				if (tmpClass.isAssignableFrom(c)) {
					o = javaXSD.get(tmpClass);
					break;
				}
			}
		}
		
		if (o instanceof String)		
			return XSD_NAMESPACE + o;

		return null;
	}
	
	/**
	 * Return the XSD type given a java object, according to the list of predefined types
	 * @param o The Java instance
	 * @return the XSD String associated with class or null if it can not be mapped to an appropriate object
	 */
	public static String getDatatypeURI(Object o) {
		return (o == null)? null : getDatatypeURI(o.getClass());
	}
	
	/**
	 * Return the Java class given an XSD String, according to the list of predefined types
	 * @param datatypeURI The XSD String
	 * @return The Java class associated to the XSD String or null if it can not be mapped to an appropriate object
	 */
	public static Class getJavaClass(String datatypeURI) {
		if (datatypeURI != null  &&  datatypeURI.startsWith(XSD_NAMESPACE))
			return (Class) xsdJava.get(datatypeURI.substring(XSD_NAMESPACE.length()));
		return null;
	}
	
	public static boolean isCompatible(String supertypeURI, String subtypeURI) {
		if (supertypeURI == null  ||  !supertypeURI.startsWith(XSD_NAMESPACE))
			return false;
		
		if (supertypeURI.equals(subtypeURI))
			return true;
		
		Class sup = getJavaClass(supertypeURI);
		if (sup == null)
			return false;
		
		if (subtypeURI == null  ||  !subtypeURI.startsWith(XSD_NAMESPACE))
			return false;
		
		Class sub = getJavaClass(subtypeURI);
		if (sub == null)
			return false;
		
		return sup.isAssignableFrom(sub);
	}
	
	public static boolean isLiteral(Object o) {
		if (o instanceof PResource)
			return ((PResource) o).serializesAsXMLLiteral();
		return (getDatatypeURI(o) != null);
	}
	
	public static boolean isRegisteredDatatypeURI(String datatypeURI) {
		return datatypeURI != null
		    && datatypeURI.startsWith(XSD_NAMESPACE)
		    && xsdJava.containsKey(datatypeURI.substring(XSD_NAMESPACE.length()));
	}
}
