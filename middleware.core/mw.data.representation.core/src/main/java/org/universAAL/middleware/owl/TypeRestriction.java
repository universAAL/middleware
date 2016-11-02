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

/**
 * Base class for all {@link TypeExpression}s that impose a restriction on a
 * data type. As data types in OWL are taken from XML specification, the
 * restrictions are defined as <i>constraining facets</i>.
 * <p>
 * Most of the type restrictions are defined for literals, with only a few
 * exceptions that define a restriction on an individual or a URI.
 * 
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#defn-coss">XML Schema
 *      Datatypes</a>
 * 
 * @author Carsten Stockloew
 */
/*-
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

    /**
     * URI for owl:onDatatype; it holds the data type for which this restriction
     * is defined.
     */
    public static final String PROP_OWL_ON_DATATYPE = OWL_NAMESPACE
	    + "onDatatype";

    /**
     * URI for owl:withRestrictions; it holds the list of restrictions (facets).
     */
    public static final String PROP_OWL_WITH_RESTRICTIONS = OWL_NAMESPACE
	    + "withRestrictions";

    /** URI for the facet xsd:pattern. */
    protected static final String XSD_FACET_PATTERN = TypeMapper.XSD_NAMESPACE
	    + "pattern";

    /**
     * The pattern (regular expression) if the facet 'pattern' is defined for
     * this restriction, or null if no pattern is defined.
     */
    private Pattern pattern = null;

    /**
     * The list of restrictions. This list is set as
     * {@link #PROP_OWL_WITH_RESTRICTIONS}.
     */
    protected ArrayList<Resource> restrictions = new ArrayList<Resource>();

    /** Internal representation of a facet. */
    protected class Facet {
	/** URI of the facet. */
	String facetURI;

	/** Value of the facet. */
	Object value;
    }

    /**
     * Standard constructor.
     * 
     * @param datatypeURI
     *            URI of the data type for which this restriction is defined.
     *            Must be one of the supported data types.
     * @see TypeMapper
     */
    protected TypeRestriction(String datatypeURI) {
	super.setProperty(PROP_OWL_ON_DATATYPE, new Resource(datatypeURI));
	super.setProperty(PROP_OWL_WITH_RESTRICTIONS, restrictions);
    }

    /**
     * Get the data type for which this restriction is defined.
     * 
     * @return URI of the data type.
     */
    public String getTypeURI() {
	return ((Resource) getProperty(PROP_OWL_ON_DATATYPE)).getURI();
    }

    /**
     * Iterate over a list of facets while checking the value; invalid elements
     * are skipped. An element of the list is not a valid facet if it is not a
     * resource or if it has not exactly one property. This one property is the
     * facet URI.
     * 
     * @param it
     *            An iterator for a list of facets.
     * @return the {@link Facet}.
     */
    protected Facet iterate(ListIterator<?> it) {
	while (it.hasNext()) {
	    Object o = it.next();
	    if (!(o instanceof Resource))
		// TODO: log message?
		continue;
	    Resource r = (Resource) o;
	    if (r.numberOfProperties() != 1)
		// TODO: log message?
		continue;
	    java.util.Enumeration<?> e = r.getPropertyURIs();
	    String propURI = (String) e.nextElement();

	    Facet f = new Facet();
	    f.facetURI = propURI;
	    f.value = r.getProperty(propURI);

	    return f;
	}
	return null;
    }

    /**
     * Add a new facet to the list of facets for this restriction.
     * 
     * @param facetURI
     *            URI of the facet.
     * @param value
     *            Value of the facet.
     */
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
	if (PROP_OWL_ON_DATATYPE.equals(propURI)) {
	    if (o instanceof Resource
		    && getTypeURI().equals(((Resource) o).getURI()))
		return true;
	    return false;
	} else if (PROP_OWL_WITH_RESTRICTIONS.equals(propURI))
	    return false;

	return super.setProperty(propURI, o);
    }

    /**
     * Set a pattern (regular expression). This pattern is added to the list of
     * constraining facets for this restriction.
     * 
     * @param pattern
     *            The pattern as defined in {@link Pattern}.
     * @return true, if the pattern could be set, i.e. the pattern must be valid
     *         and no other pattern was set before.
     */
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
    /**
     * @see org.universAAL.middleware.owl.TypeExpression#hasMember(Object,
     *      HashMap, int, List)
     */
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

    /**
     * Set a facet. For this class, only 'pattern' is allowed.
     * 
     * @param facet
     *            The facet.
     */
    protected void setFacet(Facet facet) {
	if (XSD_FACET_PATTERN.equals(facet.facetURI)) {
	    setPattern((String) facet.value);
	}
    }
}
