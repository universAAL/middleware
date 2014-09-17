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

import org.universAAL.middleware.owl.AllValuesFromRestriction;
import org.universAAL.middleware.owl.Enumeration;
import org.universAAL.middleware.owl.ExactCardinalityRestriction;
import org.universAAL.middleware.owl.HasValueRestriction;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.SomeValuesFromRestriction;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.util.ResourceComparator;

public class InitialTest extends TestCase {

    TurtleSerializer s;

    public InitialTest(String name) {
	super(name);

	s = new TurtleSerializer();
    }

    public void testDataRange() {
	// if (true)
	// return;
	Enumeration e1 = new Enumeration();
	e1.addValue(new Integer(0));
	e1.addValue(new Integer(1));
	e1.addValue(new Integer(2));
	e1.addValue(new Integer(3));
	e1.addValue(new Integer(4));

	String str = s.serialize(e1);
	Resource r = (Resource) s.deserialize(str);
	assertTrue(new ResourceComparator().areEqual(e1, r));
	System.out.println(str);
	new ResourceComparator().printDiffs(e1, r);
	System.out.println();
	System.out.println();
    }

    public void testRestriction() {
	if (true)
	    return;
	Integer one = new Integer(1);
	Enumeration e = new Enumeration();
	e.addValue(Boolean.FALSE);
	e.addValue(Boolean.TRUE);

	MergedRestriction r = new MergedRestriction(
		HasValueRestriction.PROP_OWL_HAS_VALUE);

	r.addRestriction(new AllValuesFromRestriction(
		HasValueRestriction.PROP_OWL_HAS_VALUE, new TypeURI(TypeMapper
			.getDatatypeURI(Boolean.class), true)));
	r.addRestriction(new ExactCardinalityRestriction(
		HasValueRestriction.PROP_OWL_HAS_VALUE, one.intValue()));
	r.addRestriction(new SomeValuesFromRestriction(
		HasValueRestriction.PROP_OWL_HAS_VALUE, e));

	// Restriction r = new Restriction();
	// r.setProperty(Restriction.PROP_OWL_ON_PROPERTY,
	// Restriction.PROP_OWL_HAS_VALUE);
	//
	// r.setProperty(Restriction.PROP_OWL_ALL_VALUES_FROM, new TypeURI(
	// TypeMapper.getDatatypeURI(Boolean.class), true));
	// r.setProperty(Restriction.PROP_OWL_CARDINALITY, one);
	// r.setProperty(Restriction.PROP_OWL_SOME_VALUES_FROM, e);

	String str = s.serialize(r);
	Resource r2 = (Resource) s.deserialize(str);
	System.out.println(str);
	new ResourceComparator().printDiffs(r, r2);
	assertTrue(new ResourceComparator().areEqual(r, r2));
    }
}
