/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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

import org.universAAL.middleware.owl.Enumeration;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.util.ResourceComparator;
import org.universAAL.serialization.turtle.TurtleParser;
import org.universAAL.serialization.turtle.TurtleUtil;

import junit.framework.TestCase;

public class InitialTest extends TestCase {
	
	TurtleParser s;
	
	public InitialTest(String name) {
		super(name);

		TurtleUtil.typeMapper = TypeMapper.getTypeMapper();
		s = new TurtleParser();
	}
	
	public void testDataRange() {
		Enumeration e1 = new Enumeration();
		e1.addValue(new Integer(0));
		e1.addValue(new Integer(1));
		e1.addValue(new Integer(2));
		e1.addValue(new Integer(3));
		e1.addValue(new Integer(4));

		String str = s.serialize(e1);
		System.out.println(str);
		new ResourceComparator().printDiffs(e1, (Resource) s.deserialize(str));
		System.out.println();
		System.out.println();
	}
	
	public void testRestriction() {
		Integer one = new Integer(1);
		Enumeration e = new Enumeration();
		e.addValue(Boolean.FALSE);
		e.addValue(Boolean.TRUE);
        Restriction r = new Restriction();
        r.setProperty(Restriction.PROP_OWL_ON_PROPERTY, Restriction.PROP_OWL_HAS_VALUE);
        r.setProperty(Restriction.PROP_OWL_ALL_VALUES_FROM,
        		new TypeURI(TypeMapper.getDatatypeURI(Boolean.class), true));
        r.setProperty(Restriction.PROP_OWL_CARDINALITY, one);
        r.setProperty(Restriction.PROP_OWL_SOME_VALUES_FROM, e);

		String str = s.serialize(r);
		System.out.println(str);
		new ResourceComparator().printDiffs(r, (Resource) s.deserialize(str));
	}
}
