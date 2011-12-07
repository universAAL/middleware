/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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

import java.util.ArrayList;
import java.util.ListIterator;

import org.universAAL.middleware.rdf.Resource;





/*
 * Hierarchy:
 * 	TypeRestriction (abstract)
 * 		(Facets: pattern, enumeration(?), whiteSpace)
 * 		BooleanRestriction
 * 		ComparableRestriction (abstract)
 * 			(Facets: maxInclusive, maxExclusive, minInclusive, ..)
 * 			FloatRestriction
 * 			TimeRestriction
 * 			DateRestriction
 * 			...
 * 		LengthRestriction (abstract)
 * 			(Facets: length, minLength, maxLength)
 * 			StringRestriction
 * 			Base64BinaryRestriction
 * 			...
 */
public abstract class TypeRestriction extends ClassExpression {
    
    public static final String OWL_ON_DATATYPE;
    public static final String OWL_WITH_RESTRICTIONS;
    
    protected ArrayList restrictions = new ArrayList();
    
    static {
	OWL_ON_DATATYPE = OWL_NAMESPACE + "onDatatype";
	OWL_WITH_RESTRICTIONS = OWL_NAMESPACE + "withRestrictions";
	register(TypeRestriction.class, null, OWL_ON_DATATYPE, null);
    }
    
    protected class Facet {
	String facetURI;
	Object value;
    }
    
    /** Standard constructor. */
    protected TypeRestriction(String datatypeURI) {
	super.setProperty(OWL_ON_DATATYPE, datatypeURI);
	super.setProperty(OWL_WITH_RESTRICTIONS, restrictions);
    }
    

    
    protected Facet iterate(ListIterator it) {
	while (it.hasNext()) {
	    Object o = it.next();
	    if (!(o instanceof Resource))
		// TODO: log message?
		continue;
	    Resource r = (Resource) o;
	    if (r.numberOfProperties() != 1)
		// TODO: log message?
		continue;
	    java.util.Enumeration e = r.getPropertyURIs();
	    String propURI = (String) e.nextElement();

	    Facet f = new Facet();
	    f.facetURI = propURI;
	    f.value = r.getProperty(propURI);
	    
	    return f;
	}
	return null;
    }
    
    protected void addConstrainingFacet(String facetURI, Object value) {
	Resource r = new Resource();
	r.setProperty(facetURI, value);
	restrictions.add(r);
    }
    
    /** @see org.universAAL.middleware.owl.ClassExpression#getNamedSuperclasses() */
    public String[] getNamedSuperclasses() {
	return new String[0];
    }

    /** @see org.universAAL.middleware.owl.ClassExpression#getUpperEnumeration() */
    public Object[] getUpperEnumeration() {
	return new Object[0];
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object o) {
	if (o == null || propURI == null)
	    return;
	
	// do not handle our properties
	if (OWL_ON_DATATYPE.equals(propURI))
	    return;
	if (OWL_WITH_RESTRICTIONS.equals(propURI))
	    return;
	
	super.setProperty(propURI, o);
    }
}
