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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.util.MatchLogEntry;

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
public abstract class TypeRestriction extends TypeExpression {

    public static final String PROP_OWL_ON_DATATYPE = OWL_NAMESPACE
	    + "onDatatype";

    public static final String PROP_OWL_WITH_RESTRICTIONS = OWL_NAMESPACE
	    + "withRestrictions";

    protected static final String XSD_FACET_PATTERN = TypeMapper.XSD_NAMESPACE
	    + "pattern";

    private Pattern pattern = null;

    protected ArrayList restrictions = new ArrayList();

    protected class Facet {
	String facetURI;
	Object value;
    }

    /** Standard constructor. */
    protected TypeRestriction(String datatypeURI) {
	super.setProperty(PROP_OWL_ON_DATATYPE, new Resource(datatypeURI));
	super.setProperty(PROP_OWL_WITH_RESTRICTIONS, restrictions);
    }

    public String getTypeURI() {
	return ((Resource) getProperty(PROP_OWL_ON_DATATYPE)).getURI();
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

    /** @see org.universAAL.middleware.owl.TypeExpression#getNamedSuperclasses() */
    @Override
    public String[] getNamedSuperclasses() {
	return new String[0];
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getUpperEnumeration() */
    @Override
    public Object[] getUpperEnumeration() {
	return new Object[0];
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    @Override
    public boolean setProperty(String propURI, Object o) {
	if (o == null || propURI == null)
	    return false;

	// do not handle our properties
	if (PROP_OWL_ON_DATATYPE.equals(propURI))
	    return false;
	if (PROP_OWL_WITH_RESTRICTIONS.equals(propURI))
	    return false;

	return super.setProperty(propURI, o);
    }

    public boolean setPattern(String pattern) {
	if (this.pattern != null)
	    return false;

	Pattern compiledPattern;
	try {
	    compiledPattern = Pattern.compile(pattern);
	} catch (PatternSyntaxException e) {
	    // TODO: log message?
	    return false;
	}

	addConstrainingFacet(XSD_FACET_PATTERN, pattern);
	this.pattern = compiledPattern;
	return true;
    }

    @Override
    public boolean hasMember(Object member, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	// test only the pattern constraining facet, everything else should be
	// checked in subclasses

	if (pattern == null) {
	    // there is no pattern defined
	    return true;
	}

	String m = member == null ? "" : member.toString();
	Matcher matcher = pattern.matcher(m);
	if (matcher.matches())
	    return true;

	return false;
    }

    protected void setFacet(Facet facet) {
	if (XSD_FACET_PATTERN.equals(facet.facetURI)) {
	    setPattern((String) facet.value);
	}
    }
}
