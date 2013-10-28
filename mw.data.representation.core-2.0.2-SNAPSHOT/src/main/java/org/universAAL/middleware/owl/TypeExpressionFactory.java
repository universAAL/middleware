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
package org.universAAL.middleware.owl;

import java.util.HashMap;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

public final class TypeExpressionFactory {

    private static HashMap propMap = new HashMap();
    private static HashMap datatypeMap = new HashMap();

    static {
	propMap.put(Intersection.PROP_OWL_INTERSECTION_OF, new Integer(0));
	propMap.put(Union.PROP_OWL_UNION_OF, new Integer(1));
	propMap.put(Complement.PROP_OWL_COMPLEMENT_OF, new Integer(2));
	propMap.put(Enumeration.PROP_OWL_ONE_OF, new Integer(3));
	propMap.put(SomeValuesFromRestriction.PROP_OWL_SOME_VALUES_FROM,
		new Integer(4));
	propMap.put(AllValuesFromRestriction.PROP_OWL_ALL_VALUES_FROM,
		new Integer(5));
	propMap.put(HasValueRestriction.PROP_OWL_HAS_VALUE, new Integer(6));
	propMap.put(MinCardinalityRestriction.PROP_OWL_MIN_CARDINALITY,
		new Integer(7));
	propMap.put(MaxCardinalityRestriction.PROP_OWL_MAX_CARDINALITY,
		new Integer(8));
	propMap.put(ExactCardinalityRestriction.PROP_OWL_CARDINALITY,
		new Integer(9));
	propMap
		.put(TypeRestriction.PROP_OWL_WITH_RESTRICTIONS,
			new Integer(10));

	datatypeMap.put(IntRestriction.DATATYPE_URI, new Integer(0));
	datatypeMap.put(FloatRestriction.DATATYPE_URI, new Integer(1));
	datatypeMap.put(DoubleRestriction.DATATYPE_URI, new Integer(2));
	datatypeMap.put(IndividualRestriction.DATATYPE_URI, new Integer(3));
	datatypeMap.put(LongRestriction.DATATYPE_URI, new Integer(4));
    }

    private TypeExpressionFactory() {
    }

    public static TypeExpression specialize(Resource r) {
	Integer idx = null;
	for (java.util.Enumeration e = r.getPropertyURIs(); e.hasMoreElements();) {
	    idx = (Integer) propMap.get((String) e.nextElement());
	    if (idx != null)
		break;
	}

	if (idx == null) {
	    // none of the properties matches, so it may be a TypeURI
	    String uri = r.getURI();
	    if (Resource.isAnon(uri))
		return null;

	    if (OntologyManagement.getInstance().isRegisteredClass(uri, false))
		return new TypeURI(uri, false);
	    if (TypeMapper.isRegisteredDatatypeURI(uri))
		return new TypeURI(uri, true);

	    return null;
	}

	switch (idx.intValue()) {
	case 0:
	    String[] types = r.getTypes();
	    for (int i = 0; i < types.length; i++)
		if (MergedRestriction.MY_URI.equals(types[i]))
		    return new MergedRestriction();
	    return new Intersection();
	case 1:
	    return new Union();
	case 2:
	    return new HasValueRestriction();
	case 3:
	    return new Enumeration();
	case 4:
	    return new SomeValuesFromRestriction();
	case 5:
	    return new AllValuesFromRestriction();
	case 6:
	    return new HasValueRestriction();
	case 7:
	    return new MinCardinalityRestriction();
	case 8:
	    return new MaxCardinalityRestriction();
	case 9:
	    return new ExactCardinalityRestriction();
	case 10:
	    Resource datatypeURI = (Resource) r
		    .getProperty(TypeRestriction.PROP_OWL_ON_DATATYPE);
	    if (datatypeURI != null) {
		idx = (Integer) datatypeMap.get(datatypeURI);

		if (idx == null)
		    return null;

		switch (idx.intValue()) {
		case 0:
		    return new IntRestriction();
		case 1:
		    return new FloatRestriction();
		case 2:
		    return new DoubleRestriction();
		case 3:
		    return new IndividualRestriction();
		case 4:
		    return new LongRestriction();
		}
	    }
	}

	return null;
    }
}
