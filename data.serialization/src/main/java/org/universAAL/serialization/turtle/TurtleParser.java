/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
	Copyright Aduna (http://www.aduna-software.com/) © 2001-2007
	
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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.owl.AbstractRestriction;
import org.universAAL.middleware.owl.ClassExpression;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

/**
 * Serialization and Deserialization of RDF graphs. This class implements the
 * interface
 * {@link org.universAAL.middleware.sodapop.msg.MessageContentSerializer} and
 * can be called to translate RDF graphs into <i>Terse RDF Triple Language
 * (Turtle)</i> and vice versa. While this class handles the deserialization,
 * the actual serialization is realized by {@link TurtleWriter}.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class TurtleParser implements MessageContentSerializer {

    /** URI for an empty RDF List. */
    private static final Resource NIL = new Resource(Resource.RDF_EMPTY_LIST);

    private class RefData {
	int i;
	List l;
	String prop;
	Resource src;
    }

    private class ParseData {
	String label = null;
	List refs = new ArrayList(3);
    }

    // private class TypeComparator implements Comparator {
    //
    // public int compare(Object arg0, Object arg1) {
    // if (arg0 == arg1)
    // return 0;
    //			
    // if (arg0 == null)
    // return 1;
    //			
    // if (arg1 == null)
    // return -1;
    //			
    // Class c1 = ManagedIndividual.getRegisteredClass(arg0.toString()), c2 =
    // ManagedIndividual
    // .getRegisteredClass(arg1.toString());
    // if (c1 == c2)
    // return 0;
    //			
    // if (c1 == null)
    // return 1;
    //			
    // if (c2 == null)
    // return -1;
    //			
    // return c1.isAssignableFrom(c2) ? 1 : -1;
    // }
    //
    // }

    /** The character stream to read from. */
    private PushbackReader reader;

    private Resource subject;

    private Resource firstResource = null;

    private String predicate;

    private Object object;

    private boolean eofAfterImplicitBlankNodeAsSubject = false;

    private Hashtable namespaceTable = new Hashtable();

    private Hashtable resources = new Hashtable();

    private Hashtable parseTable = new Hashtable();

    public TurtleParser() {
    }

    private void addRef(Resource referred, Resource referredBy, String prop,
	    List l, int i) {
	// we do not need to keep book on references to resources representing a
	// type
	// they are handled directly in reportStatement()
	if (referred.serializesAsXMLLiteral()
		|| Resource.PROP_RDF_TYPE.equals(prop))
	    return;

	RefData rd = new RefData();
	rd.src = referredBy;
	rd.prop = prop;
	rd.l = l;
	rd.i = i;
	getData(referred).refs.add(rd);
    }

    private boolean containsResource(List l) {
	for (int i = 0; i < l.size(); i++)
	    if (l.get(i) instanceof Resource
		    && !((Resource) l.get(i)).serializesAsXMLLiteral())
		return true;
	return false;
    }

    public synchronized Object deserialize(String serialized) {
	if (serialized == null)
	    return null;

	firstResource = null;

	return deserialize(serialized, false);
    }

    private Object deserialize(String serialized, boolean wasXMLLiteral) {
	try {
	    parse(new StringReader(serialized), "");
	    Resource result = finalizeAndGetRoot();
	    if (wasXMLLiteral)
		result = result.copyAsXMLLiteral();
	    else if (Resource.TYPE_RDF_LIST.equals(result.getType())) {
		// Object first = result.getProperty(Resource.PROP_RDF_FIRST);
		// if (first == null)
		// return null;
		// Object rest = result.getProperty(Resource.PROP_RDF_REST);
		// if (rest != null
		// && rest.toString().equals(Resource.RDF_EMPTY_LIST))
		// rest = new ArrayList(1);
		// else if (!(rest instanceof List))
		// return null;
		// ((List) rest).add(0, first);
		// return rest;
		return result.asList();
	    }
	    return result;
	} catch (Exception e) {
	    LogUtils.logError(TurtleUtil.moduleContext, TurtleParser.class,
		    "deserialize", new Object[] {
			    "Turtle-Serializer: Failed to parse\n    ",
			    serialized, "\n    returning null!" }, e);
	    return null;
	}
    }

    private Resource finalizeAndGetRoot() {
	Resource aux, specialized, result = null;
	Hashtable openItems = new Hashtable(), specializedResources = new Hashtable();
	// Comparator c = new TypeComparator();
	for (Iterator i = resources.values().iterator(); i.hasNext();) {
	    aux = (Resource) i.next();
	    i.remove();
	    ParseData pd = (ParseData) parseTable.remove(aux);
	    specialized = (aux.numberOfProperties() == 0) ? aux : specialize(
		    aux, specializedResources, openItems);
	    if (firstResource == aux)
		firstResource = specialized;
	    if (aux.numberOfProperties() > 0
		    && (pd == null || pd.refs.isEmpty()))
		if (result == null)
		    result = specialized;
		else
		    throw new RuntimeException("Root resource not unique!");
	    else if (pd != null && pd.refs != null)
		for (int j = 0; j < pd.refs.size(); j++) {
		    RefData rd = (RefData) pd.refs.get(j);
		    boolean srcSpecialized = true;
		    aux = (Resource) specializedResources.get(rd.src.getURI());
		    if (aux == null) {
			aux = rd.src;
			srcSpecialized = false;
		    }
		    if (rd.l == null) {
			aux.setProperty(rd.prop, specialized);
			if (!specialized.equals(aux.getProperty(rd.prop)))
			    openItems.put(specialized, rd);
			else if (!srcSpecialized
				&& (ClassExpression.OWL_CLASS.equals(aux
					.getType()) || AbstractRestriction.MY_URI
					.equals(aux.getType())))
			    specialize(aux, specializedResources, openItems);
		    } else {
			rd.l.set(rd.i, specialized);
			openItems.put(rd.prop + rd.src.getURI(), rd);
		    }
		}
	}

	int size = Integer.MAX_VALUE;
	while (!openItems.isEmpty() && size > openItems.size()) {
	    size = openItems.size();
	    for (Iterator i = openItems.keySet().iterator(); i.hasNext();) {
		Object o = i.next();
		RefData rd = (RefData) openItems.get(o);
		if (rd == null) {
		    // maybe it is a bug in the JVM because this shouldn't be
		    // possible to occur, but it happens!!
		    i.remove();
		    continue;
		}
		boolean srcSpecialiazed = true;
		aux = (Resource) specializedResources.get(rd.src.getURI());
		if (aux == null) {
		    aux = rd.src;
		    srcSpecialiazed = false;
		}
		if (o instanceof String) {
		    o = rd.l;
		    for (int j = 0; j < rd.l.size(); j++)
			if (rd.l.get(j) instanceof Resource) {
			    specialized = (Resource) specializedResources
				    .get(((Resource) rd.l.get(j)).getURI());
			    if (specialized != null)
				rd.l.set(j, specialized);
			}
		} else if (o instanceof Resource) {
		    specialized = (Resource) specializedResources
			    .get(((Resource) o).getURI());
		    if (specialized != null)
			o = specialized;
		}
		aux.setProperty(rd.prop, o);
		if (o.equals(aux.getProperty(rd.prop))) {
		    i.remove();
		    if (!srcSpecialiazed
			    && (ClassExpression.OWL_CLASS.equals(aux.getType()) || AbstractRestriction.MY_URI
				    .equals(aux.getType())))
			specialize(aux, specializedResources, openItems);
		}
	    }
	}

	if (!openItems.isEmpty()) {
	    Object[] msgParts = new Object[openItems.size() + 1];
	    msgParts[0] = "There are relationships not resolved:";
	    int ind = 1;
	    for (Iterator i = openItems.values().iterator(); i.hasNext();) {
		RefData rd = (RefData) i.next();
		msgParts[ind++] = "\n        " + (rd == null ? null : rd.prop);
		i.remove();
	    }
	    LogUtils.logDebug(TurtleUtil.moduleContext, TurtleParser.class,
		    "finalizeAndGetRoot", msgParts, null);
	}

	if (result == null)
	    result = firstResource;
	resources.clear();
	parseTable.clear();
	return result;
    }

    private ClassExpression getClassExpression(Resource r, String uri) {
	ClassExpression result = null;
	Object o = r.getProperty((ClassExpression.PROP_RDFS_SUB_CLASS_OF));
	if (o instanceof Resource) {
	    if (!((Resource) o).isAnon()) {
		result = ClassExpression.getClassExpressionInstance(
			((Resource) o).getURI(), null, uri);
		if (result != null)
		    return result;
	    }
	} else if (o instanceof List) {
	    for (Iterator i = ((List) o).iterator(); i.hasNext();) {
		o = i.next();
		if (o instanceof Resource) {
		    if (!((Resource) o).isAnon()) {
			result = ClassExpression.getClassExpressionInstance(
				((Resource) o).getURI(), null, uri);
			if (result != null)
			    return result;
		    }
		}
	    }
	}

	int num = 0;
	for (Enumeration e = r.getPropertyURIs(); e.hasMoreElements();) {
	    String p = (String) e.nextElement();
	    if (!Resource.PROP_RDF_TYPE.equals(p)) {
		result = ClassExpression.getClassExpressionInstance(null, p,
			uri);
		num++;
	    }
	    if (result != null)
		return result;
	}

	return (r.isAnon() || num > 0) ? null : ClassExpression
		.getClassExpressionInstance(null, null, uri);
    }

    private ParseData getData(Resource r) {
	ParseData d = (ParseData) parseTable.get(r);
	if (d == null) {
	    d = new ParseData();
	    parseTable.put(r, d);
	}
	return d;
    }

    private Resource getResource(String uri) {
	Resource r;
	if (uri == null) {
	    r = new Resource();
	    resources.put(r.getURI(), r);
	} else {
	    r = (Resource) resources.get(uri);
	    if (r == null) {
		if (uri.startsWith("_:")) {
		    // bNode ID
		    r = new Resource();
		    getData(r).label = uri;
		} else
		    r = new Resource(uri);
		resources.put(uri, r);
	    }
	}
	return r;
    }

    private void parse(Reader reader, String baseURI) {
	if (reader == null) {
	    throw new IllegalArgumentException("Reader must not be 'null'");
	}
	if (baseURI == null) {
	    throw new IllegalArgumentException("base URI must not be 'null'");
	}

	// Allow at most 2 characters to be pushed back:
	this.reader = new PushbackReader(reader, 2);

	int c = skipWSC();
	while (c != -1) {
	    parseStatement();
	    if (eofAfterImplicitBlankNodeAsSubject)
		break;
	    c = skipWSC();
	}
    }

    private List parseCollection() {
	verifyCharacter(read(), "(");

	int c = skipWSC();

	List l = new ArrayList();
	if (c == ')')
	    // Empty list
	    read();
	else {
	    parseObject();
	    l.add(object);
	    if (object instanceof Resource)
		addRef((Resource) object, subject, predicate, l, 0);

	    int i = 1;
	    while (skipWSC() != ')') {
		parseObject();
		l.add(object);
		if (object instanceof Resource)
		    addRef((Resource) object, subject, predicate, l, i);
		i++;
	    }

	    // Skip ')'
	    read();
	}

	return l;
    }

    private void parseDirective() {
	// Verify that the first characters form the string "prefix"
	verifyCharacter(read(), "@");

	StringBuffer sb = new StringBuffer(8);

	int c = read();
	while (c != -1 && !TurtleUtil.isWhitespace(c)) {
	    sb.append((char) c);
	    c = read();
	}

	String directive = sb.toString();
	if (directive.equals("prefix")) {
	    parsePrefixID();
	} else if (directive.equals("base")) {
	    throw new RuntimeException("Base not supported!");
	} else if (directive.length() == 0) {
	    throw new RuntimeException(
		    "Directive name is missing, expected @prefix or @base");
	} else {
	    throw new RuntimeException("Unknown directive \"@" + directive
		    + "\"");
	}
    }

    private Resource parseImplicitBlank() {
	verifyCharacter(read(), "[");

	Resource bNode = getResource(null);

	int c = read();
	if (c != ']') {
	    unread(c);

	    // Remember current subject and predicate
	    Resource oldSubject = subject;
	    String oldPredicate = predicate;

	    // generated bNode becomes subject
	    subject = bNode;

	    // Enter recursion with nested predicate-object list
	    skipWSC();

	    parsePredicateObjectList();

	    skipWSC();

	    // Read closing bracket
	    verifyCharacter(read(), "]");

	    // Restore previous subject and predicate
	    subject = oldSubject;
	    predicate = oldPredicate;
	}

	return bNode;
    }

    private String parseLongString() {
	StringBuffer sb = new StringBuffer(1024);

	int doubleQuoteCount = 0;
	int c;

	while (doubleQuoteCount < 3) {
	    c = read();

	    if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    } else if (c == '"') {
		doubleQuoteCount++;
	    } else {
		doubleQuoteCount = 0;
	    }

	    sb.append((char) c);

	    if (c == '\\') {
		// This escapes the next character, which might be a '"'
		c = read();
		if (c == -1) {
		    throw new RuntimeException("Unexpected end of file!");
		}
		sb.append((char) c);
	    }
	}

	return sb.substring(0, sb.length() - 3);
    }

    private Resource parseNodeID() {
	// Node ID should start with "_:"
	verifyCharacter(read(), "_");
	verifyCharacter(read(), ":");

	// Read the node ID
	int c = read();
	if (c == -1) {
	    throw new RuntimeException("Unexpected end of file!");
	} else if (!TurtleUtil.isNameStartChar(c)) {
	    throw new RuntimeException("Expected a letter, found '" + (char) c
		    + "'");
	}

	StringBuffer name = new StringBuffer(32).append("_:");
	name.append((char) c);

	// Read all following letter and numbers, they are part of the name
	c = read();
	while (TurtleUtil.isNameChar(c)) {
	    name.append((char) c);
	    c = read();
	}

	unread(c);

	return getResource(name.toString());
    }

    private Object parseNumber() {
	StringBuffer value = new StringBuffer(8);
	String datatype = TypeMapper.getDatatypeURI(Integer.class);

	int c = read();

	// read optional sign character
	if (c == '+' || c == '-') {
	    value.append((char) c);
	    c = read();
	}

	while (StringUtils.isDigit((char) c)) {
	    value.append((char) c);
	    c = read();
	}

	if (c == '.' || c == 'e' || c == 'E') {
	    // We're parsing a decimal or a double
	    datatype = TypeMapper.getDatatypeURI(Double.class);

	    // read optional fractional digits
	    if (c == '.') {
		value.append((char) c);

		c = read();
		while (StringUtils.isDigit((char) c)) {
		    value.append((char) c);
		    c = read();
		}

		if (value.length() == 1) {
		    // We've only parsed a '.'
		    throw new RuntimeException("Object for statement missing");
		}
	    } else {
		if (value.length() == 0) {
		    // We've only parsed an 'e' or 'E'
		    throw new RuntimeException("Object for statement missing");
		}
	    }

	    // read optional exponent
	    if (c == 'e' || c == 'E') {
		datatype = TypeMapper.getDatatypeURI(Double.class);
		value.append((char) c);

		c = read();
		if (c == '+' || c == '-') {
		    value.append((char) c);
		    c = read();
		}

		if (!StringUtils.isDigit((char) c)) {
		    throw new RuntimeException("Exponent value missing");
		}

		value.append((char) c);

		c = read();
		while (StringUtils.isDigit((char) c)) {
		    value.append((char) c);
		    c = read();
		}
	    }
	}

	// Unread last character, it isn't part of the number
	unread(c);

	return TypeMapper.getJavaInstance(value.toString(), datatype);
    }

    private void parseObject() {
	int c = peek();

	if (c == '(') {
	    object = parseCollection();
	} else if (c == '[') {
	    object = parseImplicitBlank();
	} else {
	    object = parseValue();
	    // if (object instanceof Resource && ((Resource)
	    // object).serializesAsXMLLiteral())
	    // resources.put(((Resource) object).getURI(), object);
	}
    }

    private void parseObjectList() {
	List l = new ArrayList(3);
	parseObject();

	int i = 0;
	while (skipWSC() == ',') {
	    l.add(object);
	    if (object instanceof Resource)
		addRef((Resource) object, subject, predicate, l, i);
	    i++;
	    read();
	    skipWSC();
	    parseObject();
	}

	if (l.isEmpty()) {
	    if (object instanceof Resource)
		addRef((Resource) object, subject, predicate, null, -1);
	} else {
	    l.add(object);
	    if (object instanceof Resource)
		addRef((Resource) object, subject, predicate, l, i);
	    object = l;
	}

	reportStatement(subject, predicate, object);
    }

    private String parsePredicate() {
	// Check if the short-cut 'a' is used
	int c1 = read();

	if (c1 == 'a') {
	    int c2 = read();

	    if (TurtleUtil.isWhitespace(c2)) {
		// Short-cut is used, return the rdf:type URI
		return Resource.PROP_RDF_TYPE;
	    }

	    // Short-cut is not used, unread all characters
	    unread(c2);
	}
	unread(c1);

	// Predicate is a normal resource
	Object predicate = parseValue();
	if (predicate instanceof Resource) {
	    return predicate.toString();
	} else if (predicate != null || !eofAfterImplicitBlankNodeAsSubject)
	    throw new RuntimeException("Illegal predicate value: " + predicate);
	else
	    return null;
    }

    private void parsePredicateObjectList() {
	predicate = parsePredicate();
	if (eofAfterImplicitBlankNodeAsSubject)
	    return;

	skipWSC();

	parseObjectList();

	while (skipWSC() == ';') {
	    read();

	    int c = skipWSC();

	    if (c == '.' || // end of triple
		    c == ']') // end of predicateObjectList inside blank node
	    {
		break;
	    }

	    predicate = parsePredicate();

	    skipWSC();

	    parseObjectList();
	}
    }

    private void parsePrefixID() {
	skipWSC();

	// Read prefix ID (e.g. "rdf:" or ":")
	StringBuffer prefixID = new StringBuffer(8);

	while (true) {
	    int c = read();

	    if (c == ':') {
		unread(c);
		break;
	    } else if (TurtleUtil.isWhitespace(c)) {
		break;
	    } else if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    }

	    prefixID.append((char) c);
	}

	skipWSC();

	verifyCharacter(read(), ":");

	skipWSC();

	// Read the namespace URI
	String namespace = parseURI();

	namespaceTable.put(prefixID.toString(), namespace.toString());
    }

    private Object parseQNameOrBoolean() {
	// First character should be a ':' or a letter
	int c = read();
	if (c == -1) {
	    throw new RuntimeException("Unexpected end of file!");
	}
	if (c != ':' && !TurtleUtil.isPrefixStartChar(c)) {
	    throw new RuntimeException("Expected a ':' or a letter, found '"
		    + (char) c + "'");
	}

	String namespace = null;

	if (c == ':') {
	    // qname using default namespace
	    namespace = (String) namespaceTable.get("");
	    if (namespace == null) {
		throw new RuntimeException(
			"Default namespace used but not defined");
	    }
	} else {
	    // c is the first letter of the prefix
	    StringBuffer prefix = new StringBuffer(8);
	    prefix.append((char) c);

	    c = read();
	    while (TurtleUtil.isPrefixChar(c)) {
		prefix.append((char) c);
		c = read();
	    }

	    if (c != ':') {
		// prefix may actually be a boolean value
		String value = prefix.toString();

		if (value.equals("true") || value.equals("false")) {
		    return TypeMapper.getJavaInstance(value, TypeMapper
			    .getDatatypeURI(Boolean.class));
		}
	    }

	    verifyCharacter(c, ":");

	    namespace = (String) namespaceTable.get(prefix.toString());
	    if (namespace == null) {
		throw new RuntimeException("Namespace prefix '"
			+ prefix.toString() + "' used but not defined");
	    }
	}

	// c == ':', read optional local name
	StringBuffer localName = new StringBuffer(16);
	c = read();
	if (TurtleUtil.isNameStartChar(c)) {
	    localName.append((char) c);

	    c = read();
	    while (TurtleUtil.isNameChar(c)) {
		localName.append((char) c);
		c = read();
	    }
	}

	// Unread last character
	unread(c);

	// Note: namespace has already been resolved
	return getResource(namespace + localName.toString());
    }

    private Object parseQuotedLiteral() {
	String label = parseQuotedString();

	// Check for presence of a language tag or datatype
	int c = peek();

	if (c == '@') {
	    read();

	    // Read language
	    StringBuffer lang = new StringBuffer(8);

	    c = read();
	    if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    }
	    if (!TurtleUtil.isLanguageStartChar(c)) {
		throw new RuntimeException("Expected a letter, found '"
			+ (char) c + "'");
	    }

	    lang.append((char) c);

	    c = read();
	    while (TurtleUtil.isLanguageChar(c)) {
		lang.append((char) c);
		c = read();
	    }

	    unread(c);

	    return TypeMapper.getJavaInstance(label, null);
	} else if (c == '^') {
	    read();

	    // next character should be another '^'
	    verifyCharacter(read(), "^");

	    // Read datatype
	    Object datatype = parseValue();
	    if (datatype instanceof Resource)
		datatype = datatype.toString();
	    else if (!(datatype instanceof String))
		throw new RuntimeException("Illegal datatype value: "
			+ datatype);

	    if (datatype.equals(TurtleUtil.xmlLiteral))
		return new TurtleParser().deserialize(label, true);

	    return TypeMapper.getJavaInstance(label, (String) datatype);
	} else {
	    return TypeMapper.getJavaInstance(label, null);
	}
    }

    private String parseQuotedString() {
	String result = null;

	// First character should be '"'
	verifyCharacter(read(), "\"");

	// Check for long-string, which starts and ends with three double quotes
	int c2 = read();
	int c3 = read();

	if (c2 == '"' && c3 == '"') {
	    // Long string
	    result = parseLongString();
	} else {
	    // Normal string
	    unread(c3);
	    unread(c2);

	    result = parseString();
	}

	return TurtleUtil.decodeString(result);
    }

    private void parseStatement() {
	int c = peek();

	if (c == '@') {
	    parseDirective();
	    skipWSC();
	    verifyCharacter(read(), ".");
	} else {
	    parseTriples();
	    if (!eofAfterImplicitBlankNodeAsSubject) {
		skipWSC();
		verifyCharacter(read(), ".");
	    }
	}
    }

    private String parseString() {
	StringBuffer sb = new StringBuffer(32);

	while (true) {
	    int c = read();

	    if (c == '"') {
		break;
	    } else if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    }

	    sb.append((char) c);

	    if (c == '\\') {
		// This escapes the next character, which might be a '"'
		c = read();
		if (c == -1) {
		    throw new RuntimeException("Unexpected end of file!");
		}
		sb.append((char) c);
	    }
	}

	return sb.toString();
    }

    private void parseSubject() {
	int c = peek();

	if (c == '(') {
	    List l = parseCollection();
	    if (l == null || l.isEmpty())
		subject = NIL;
	    else {
		subject = getResource(null);
		subject.addType(Resource.TYPE_RDF_LIST, true);
		subject.setProperty(Resource.PROP_RDF_FIRST, l.remove(0));
		subject.setProperty(Resource.PROP_RDF_REST, l);
	    }
	} else if (c == '[') {
	    subject = parseImplicitBlank();
	} else {
	    Object value = parseValue();

	    if (value instanceof Resource) {
		subject = (Resource) value;
	    } else {
		throw new RuntimeException("Illegal subject value: " + value);
	    }
	}
    }

    private void parseTriples() {
	parseSubject();
	if (firstResource == null)
	    firstResource = subject;

	skipWSC();
	parsePredicateObjectList();

	subject = null;
	predicate = null;
	object = null;
    }

    private String parseURI() {
	StringBuffer uriBuf = new StringBuffer(100);

	// First character should be '<'
	int c = read();
	verifyCharacter(c, "<");

	// Read up to the next '>' character
	while (true) {
	    c = read();

	    if (c == '>') {
		break;
	    } else if (c == -1) {
		throw new RuntimeException("Unexpected end of file!");
	    }

	    uriBuf.append((char) c);

	    if (c == '\\') {
		// This escapes the next character, which might be a '>'
		c = read();
		if (c == -1) {
		    throw new RuntimeException("Unexpected end of file!");
		}
		uriBuf.append((char) c);
	    }
	}

	try {
	    return TurtleUtil.decodeString(uriBuf.toString());
	} catch (Exception e) {
	    throw new RuntimeException(e.getMessage());
	}
    }

    private Object parseValue() {
	int c = peek();

	if (c == '<') {
	    // uriref, e.g. <foo://bar>
	    return getResource(parseURI());
	} else if (c == ':' || TurtleUtil.isPrefixStartChar(c)) {
	    // qname or boolean
	    return parseQNameOrBoolean();
	} else if (c == '_') {
	    // node ID, e.g. _:n1
	    return parseNodeID();
	} else if (c == '"') {
	    // quoted literal, e.g. "foo" or """foo"""
	    return parseQuotedLiteral();
	} else if (StringUtils.isDigit((char) c) || c == '.' || c == '+'
		|| c == '-') {
	    // integer or double, e.g. 123 or 1.2e3
	    return parseNumber();
	} else if (c == -1) {
	    // postpone an error if the subject is an implicit blank node used
	    // as subject
	    if (subject != null && subject.isAnon()
		    && resources.get(subject.getURI()) != null) {
		eofAfterImplicitBlankNodeAsSubject = true;
		return null;
	    }
	    throw new RuntimeException("Unexpected end of file!");
	} else {
	    throw new RuntimeException("Expected an RDF value here, found '"
		    + (char) c + "'");
	}
    }

    /**
     * Returns the next character of input stream without changing the position
     * in the stream.
     */
    private int peek() {
	int result = read();
	unread(result);
	return result;
    }

    /** Read a single character from the input stream. */
    private int read() {
	try {
	    return reader.read();
	} catch (Exception e) {
	    throw new RuntimeException(e.getMessage());
	}
    }

    private void reportStatement(Resource subj, String pred, Object obj) {
	if (obj == null || (obj instanceof List && ((List) obj).isEmpty()))
	    obj = NIL;
	else if (!Resource.PROP_RDF_TYPE.equals(pred)
		&& ((obj instanceof Resource && !((Resource) obj)
			.serializesAsXMLLiteral()) || (obj instanceof List && containsResource((List) obj))))
	    // postpone for later -> see finalizeAndGetRoot
	    return;

	subj.setProperty(pred, obj);
    }

    /** @see org.universAAL.middleware.sodapop.msg.MessageContentSerializer#serialize(Object) */
    public String serialize(Object messageContent) {
	return TurtleWriter.serialize(messageContent, 0);
    }

    // private void setSpecializedValue(Resource r, String prop, Object newVal,
    // Object oldVal) {
    // r.setProperty(prop, newVal);
    // Object o = r.getProperty(prop);
    // if (o == null)
    // throw new RuntimeException("TurtleParser: could not set " + prop);
    //		
    // if (o instanceof List)
    // for (int i=0; i<((List) o).size(); i++) {
    //				
    // }
    // else if (o != newVal) {
    //			
    // }
    //		
    //		
    // }

    /** Skip the rest of the line. */
    private void skipLine() {
	int c = read();
	while (c != -1 && c != 0xD && c != 0xA) {
	    c = read();
	}

	// c is equal to -1, \r or \n.
	// In case c is equal to \r, we should also read a following \n.
	if (c == 0xD) {
	    c = read();

	    if (c != 0xA) {
		unread(c);
	    }
	}
    }

    /** Skip white space characters. */
    private int skipWSC() {
	int c = read();
	while (TurtleUtil.isWhitespace(c) || c == '#') {
	    if (c == '#') {
		skipLine();
	    }

	    c = read();
	}

	unread(c);

	return c;
    }

    private Resource specialize(Resource r, Hashtable specialized,
	    Hashtable openItems) {
	// check if it has already been handled
	Resource substitution = (Resource) specialized.get(r.getURI());
	if (substitution != null)
	    return substitution;

	String[] types = r.getTypes();
	if (types == null || types.length == 0) {
	    specialized.put(r.getURI(), r);
	    return r;
	} else {
	    String uri = r.getURI();
	    String type = OntologyManagement.getInstance()
		    .getMostSpecializedClass(types);
	    if (type == null) {
		type = types[0];
		substitution = ClassExpression.getClassExpressionInstance(type,
			uri);
		if (substitution == null
			&& (ClassExpression.OWL_CLASS.equals(type) || AbstractRestriction.MY_URI
				.equals(type))) {
		    substitution = getClassExpression(r, uri);
		    if (substitution == null)
			// postpone the specialization until all props are
			// set
			return r;
		}
	    } else
		substitution = OntologyManagement.getInstance().getResource(
			type, uri);
	    if (substitution == null) {
		LogUtils.logDebug(TurtleUtil.moduleContext, TurtleParser.class,
			"specialize", new Object[] {
				"Resource not specialized: type = ", type },
			null);
		specialized.put(r.getURI(), r);
		return r;
	    }
	}

	specialized.put(r.getURI(), substitution);

	for (Enumeration e = r.getPropertyURIs(); e.hasMoreElements();) {
	    String prop = (String) e.nextElement();
	    Object val = r.getProperty(prop);
	    substitution.setProperty(prop, val);
	    if (!val.equals(substitution.getProperty(prop))) {
		RefData rd = new RefData();
		rd.src = substitution;
		rd.prop = prop;
		if (val instanceof Resource)
		    openItems.put(val, rd);
		else if (val instanceof List) {
		    rd.l = (List) val;
		    openItems.put(rd.prop + rd.src.getURI(), rd);
		} else
		    LogUtils.logWarn(TurtleUtil.moduleContext,
			    TurtleParser.class, "specialize", new Object[] {
				    "Property '", prop,
				    "' could not be set for a resource!" },
			    null);
	    }
	}

	return substitution;
    }

    /** Pushes a previously read character back to the input stream. */
    private void unread(int c) {
	if (c != -1) {
	    try {
		reader.unread(c);
	    } catch (IOException e) {
		throw new RuntimeException(e.getMessage());
	    }
	}
    }

    /**
     * Tests, if the given character is contained in the set of expected
     * characters.
     */
    private void verifyCharacter(int c, String expected) {
	if (c == -1) {
	    throw new RuntimeException("Unexpected end of file!");
	} else if (expected.indexOf((char) c) < 0) {
	    StringBuffer msg = new StringBuffer(32);
	    msg.append("Expected ");
	    for (int i = 0; i < expected.length(); i++) {
		if (i > 0) {
		    msg.append(" or ");
		}
		msg.append('\'');
		msg.append(expected.charAt(i));
		msg.append('\'');
	    }
	    msg.append(", found '");
	    msg.append((char) c);
	    msg.append("'");

	    throw new RuntimeException(msg.toString());
	}
    }
}
