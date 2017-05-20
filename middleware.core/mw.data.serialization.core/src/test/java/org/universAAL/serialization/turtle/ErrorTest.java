/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research 
	
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
package org.universAAL.serialization.turtle;

import junit.framework.TestCase;

import java.util.Enumeration;
import java.util.List;

import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;

public class ErrorTest extends TestCase {

    TurtleSerializer s;
    boolean isInitialized;
    JUnitModuleContext mc;

    public ErrorTest(String name) {
	super(name);

	if (isInitialized)
	    return;
	isInitialized = true;

	mc = new JUnitModuleContext();
	// uncomment the following line to get log messages
	//TurtleUtil.moduleContext = mc;
	s = new TurtleSerializer();
    }

    public void testWrongBlankNode() {
	// The subject as blind node should start with "_:"
	assertTrue(s.deserialize("__") == null);
    }
    
    public void testEOFinBlankNode() {
	// The subject as blind node should start with "_:"
	assertTrue(s.deserialize("_") == null);
    }
    
    public void testEOFinObject1() {
	assertTrue(s.deserialize("<A> a ") == null);
    }
    
    public void testEOFinObject2() {
	assertTrue(s.deserialize("<A> a <B") == null);
    }
    
    public void testEOFinObject3() {
	assertTrue(s.deserialize("<A> a <\\") == null);
    }
    
    public void testWrongObject1() {
	assertTrue(s.deserialize("<A> a B .") == null);
    }
    
    public void testWrongObject2() {
	assertTrue(s.deserialize("<A> a ! .") == null);
    }
    
    public void testStringObject1() {
	assertTrue(s.deserialize("<A> p \"\\a\" .") == null);
    }
    
    public void testStringObject2() {
	// EOF in string
	assertTrue(s.deserialize("<A> p \"\\") == null);
    }
    
    public void testWrongLangTag1() {
	assertTrue(s.deserialize("<A> p \"someString\"@! .") == null);
    }
    
    public void testWrongLangTag2() {
	assertTrue(s.deserialize("<A> p \"someString\"@") == null);
    }
    
    public void testWrongQuotedLiteral1() {
	assertTrue(s.deserialize("<A> p \"someString\"^^") == null);
    }
    
    public void testWrongQuotedLiteral2() {
	assertTrue(s.deserialize("<A> p \"someString\"^^xkjgj .") == null);
    }
    
    public void testNoDefaultNamespaceDefined() {
	assertTrue(s.deserialize("<A> a :B .") == null);
    }
    
    public void testEOFinNS() {
	assertTrue(s.deserialize("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-sy") == null);
    }
}
