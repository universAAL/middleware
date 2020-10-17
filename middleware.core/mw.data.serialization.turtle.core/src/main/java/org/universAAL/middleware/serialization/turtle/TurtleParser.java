/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research

	Copyright Aduna (http://www.aduna-software.com/) 2001-2007

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
package org.universAAL.middleware.serialization.turtle;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.generic.GenericOntology;
import org.universAAL.middleware.rdf.ClosedCollection;
import org.universAAL.middleware.rdf.LangString;
import org.universAAL.middleware.rdf.OpenCollection;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.util.Specializer;

/**
 * Serialization and Deserialization of RDF graphs. This class implements the
 * interface
 * {@link org.universAAL.middleware.serialization.MessageContentSerializer} and
 * can be called to translate RDF graphs into <i>Terse RDF Triple Language
 * (Turtle)</i> and vice versa. While this class handles the deserialization,
 * the actual serialization is realized by {@link TurtleWriter}.
 *
 * @author mtazari
 * @author Carsten Stockloew
 */
public class TurtleParser {

	/** URI for an empty RDF List. */
	private static final Resource NIL = new Resource(Resource.RDF_EMPTY_LIST);

	/** The character stream to read from. */
	private PushbackReader reader;

	private Resource subject;

	private Resource firstResource = null;

	private List<Resource> rootNodes = new ArrayList<Resource>();

	private String predicate;

	private Object object;

	private boolean eofAfterImplicitBlankNodeAsSubject = false;

	private Hashtable namespaceTable = new Hashtable();

	/**
	 * The set of resources in the serialized String. This table is used in the
	 * first step of deserialization to store all resources as instances of
	 * {@link Resource}. In a second step, these resources are specialized as
	 * objects of subclasses of {@link Resource}.
	 */
	private Hashtable resources = new Hashtable();

	/**
	 * The set of blank nodes. Blank nodes are serialized with a prefix "_:" (in
	 * our case, this is followed by "BN" and a number). Those blank nodes are
	 * transformed in a {@link Resource} with an anonymous URI. If there are
	 * multiple references to this node, this table will store a mapping from
	 * the blank node identifier to the URI of the URI.
	 */
	private Hashtable blankNodes = new Hashtable();

	private static final String stringifiedPosInf = Double.toString(Double.POSITIVE_INFINITY);
	private static final String stringifiedNegInf = Double.toString(Double.NEGATIVE_INFINITY);

	public TurtleParser() {
		// Initialized the namespace table with the "rdf" namespace to prevent
		// failure in case of parsing turtle that includes this namespace only
		// implicitly through 'a' as abbreviation for rdf:type.
		// making sure that handling 'a' as 'rdf:type' will not cause any issues later
		namespaceTable.put("rdf", Resource.RDF_NAMESPACE);
	}

	public Object deserialize(String serialized, String resourceURI) {
		if (serialized == null)
			return null;

		Object parsed = deserialize(serialized, false, resourceURI);
		if (parsed == null)
			return null;

		if (parsed instanceof GenericOntology)
			return parsed;

		Resource r;
		boolean isList = false;
		if (parsed instanceof Resource) {
			r = (Resource) parsed;
		} else {
			// parsed must be an instance of List
			// we simply use the list as a dummy property for specialization
			r = new Resource();
			r.setProperty("propURI", parsed);
			isList = true;
		}

		r = new Specializer().specialize(r);

		if (isList) {
			return r.getProperty("propURI");
		} else {
			return r;
		}
	}

	private Object deserialize(String serialized, boolean wasXMLLiteral, String resourceURI) {
		try {
			parse(new StringReader(serialized), "");
			Resource result = finalizeAndGetRoot(resourceURI);
			if (result != null) {
				if (Ontology.TYPE_OWL_ONTOLOGY.equals(result.getType()))
					return new GenericOntology(result.getURI(), rootNodes);
				if (wasXMLLiteral)
					result = result.copy(true);
				else if (Resource.TYPE_RDF_LIST.equals(result.getType())) {
					return result.asList();
				}
			}
			return result;
		} catch (Exception e) {
			LogUtils.logError(TurtleUtil.moduleContext, TurtleParser.class, "deserialize",
					new Object[] { "Turtle-Serializer: Failed to parse\n    ", serialized, "\n    returning null!" },
					e);
			return null;
		}
	}

	/**
	 * Second step of deserialization. This method takes the list of resources
	 * (and references data) from the first step and specializes all resources.
	 *
	 * @param resourceURI
	 *            Can be used to select a root node
	 * @return the root node.
	 */
	private Resource finalizeAndGetRoot(String resourceURI) {
		Resource root = null;
		if (resourceURI != null)
			root = (Resource) resources.get(resourceURI);
		if (root == null)
			root = firstResource;
		return root;
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
					r = (Resource) blankNodes.get(uri);
					if (r == null) {
						r = new Resource();
						blankNodes.put(uri, r);
					}
				} else {
					r = new Resource(uri);
				}
				resources.put(r.getURI(), r);
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

		List l = new ClosedCollection();
		if (c == ')')
			// Empty list
			read();
		else {
			parseObject();
			l.add(object);

			while (skipWSC() != ')') {
				parseObject();
				l.add(object);
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
			throw new RuntimeException("Directive name is missing, expected @prefix or @base");
		} else {
			throw new RuntimeException("Unknown directive \"@" + directive + "\"");
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
			throw new RuntimeException("Expected a letter, found '" + (char) c + "'");
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

	/**
	 * Parse a number in abbreviated form.
	 *
	 * @see http://www.w3.org/TR/turtle/ , section 2.5.2
	 * @return an instance of either {@link BigInteger}, {@link Double}, or
	 *         {@link BigDecimal}.
	 */
	private Object parseNumber() {
		StringBuffer value = new StringBuffer(8);
		String datatype = TypeMapper.getDatatypeURI(BigInteger.class);

		int c = read();

		// read optional sign character
		if (c == '+') {
			value.append((char) c);
			c = read();
		} else if (c == '-') {
			value.append((char) c);
			c = read();
			if (c == 'I') {
				int c2 = read();
				if (c2 == 'N') {
					int c3 = read();
					if (c3 == 'F') {
						// special value '-INF'
						return TypeMapper.getJavaInstance(stringifiedNegInf, TypeMapper.getDatatypeURI(Double.class));
					} else {
						unread(c3);
						unread(c2);
					}
				} else {
					unread(c2);
				}
			}
		}

		while (StringUtils.isDigit((char) c)) {
			value.append((char) c);
			c = read();
		}

		if (c == '.' || c == 'e' || c == 'E') {
			// We're parsing a decimal or a double
			datatype = TypeMapper.getDatatypeURI(BigDecimal.class);

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
			object = parseValue(true);
		}
	}

	private void parseObjectList() {
		List l = new OpenCollection();
		parseObject();

		while (skipWSC() == ',') {
			l.add(object);
			read();
			skipWSC();
			parseObject();
		}

		if (!l.isEmpty()) {
			l.add(object);
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
		Object predicate = parseValue(false);
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
					c == ']') { // end of predicateObjectList inside blank node
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

	private Object parseQNameOrBoolean(boolean parseAsResource) {
		// First character should be a ':' or a letter
		int c = read();
		if (c == -1) {
			throw new RuntimeException("Unexpected end of file!");
		}
		if (c != ':' && !TurtleUtil.isPrefixStartChar(c)) {
			throw new RuntimeException("Expected a ':' or a letter, found '" + (char) c + "'");
		}

		String namespace = null;

		if (c == ':') {
			// qname using default namespace
			namespace = (String) namespaceTable.get("");
			if (namespace == null) {
				throw new RuntimeException("Default namespace used but not defined");
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
					return TypeMapper.getJavaInstance(value, TypeMapper.getDatatypeURI(Boolean.class));
				} else if (value.equals("NaN")) {
					return TypeMapper.getJavaInstance(value, TypeMapper.getDatatypeURI(Double.class));
				} else if (value.equals("INF")) {
					return TypeMapper.getJavaInstance(stringifiedPosInf, TypeMapper.getDatatypeURI(Double.class));
				}
			}

			verifyCharacter(c, ":");

			namespace = (String) namespaceTable.get(prefix.toString());
			if (namespace == null) {
				throw new RuntimeException("Namespace prefix '" + prefix.toString() + "' used but not defined");
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
		if (parseAsResource)
			return getResource(namespace + localName.toString());
		else
			return new Resource(namespace + localName.toString());
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
				throw new RuntimeException("Expected a letter, found '" + (char) c + "'");
			}

			lang.append((char) c);

			c = read();
			while (TurtleUtil.isLanguageChar(c)) {
				lang.append((char) c);
				c = read();
			}

			unread(c);

			return new LangString(label, lang.toString());
			// return TypeMapper.getJavaInstance(label, null);
		} else if (c == '^') {
			read();

			// next character should be another '^'
			verifyCharacter(read(), "^");

			// Read datatype
			Object datatype = parseValue(true);
			if (datatype instanceof Resource)
				datatype = datatype.toString();
			else if (!(datatype instanceof String))
				throw new RuntimeException("Illegal datatype value: " + datatype);

			if (datatype.equals(TurtleUtil.xmlLiteral)) {
				Object o = new TurtleParser().deserialize(label, true, null);
				if (o instanceof Resource) {
					Resource r = (Resource) o;
					r.literal();
				}
				return o;
			}

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
			Object value = parseValue(true);

			if (value instanceof Resource) {
				subject = (Resource) value;
			} else {
				throw new RuntimeException("Illegal subject value: " + value);
			}
		}
	}

	private void parseTriples() {
		int size = resources.size();
		parseSubject();
		if (resources.size() != size) {
			// we found a new root node
			// this is not generally true, since this subject could also appear
			// later as rdf:object
			rootNodes.add(subject);
			// System.out.println(" -- found " + subject.getURI());
		}
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

	/**
	 *
	 * @param parseAsResource
	 *            if the value is a Resource, this parameter determines whether
	 *            the value should be treated as a Resource for further
	 *            specialization. It is used to differentiate between rdf
	 *            predicate and rdf subject/object. In universAAL predicates are
	 *            used only as String; although they are in reality also
	 *            resources, they cannot be specialized and don't need to be
	 *            investigated further. If parseAsResource is true, the value is
	 *            treated as a Resource for further processing (i.e. rdf
	 *            subject/object). If it is false, the value is a rdf predicate
	 *            which cannot be specialized.
	 * @return
	 */
	private Object parseValue(boolean parseAsResource) {
		int c = peek();

		if (c == '<') {
			// uriref, e.g. <foo://bar>
			if (parseAsResource)
				return getResource(parseURI());
			else
				return new Resource(parseURI());
		} else if (c == ':' || TurtleUtil.isPrefixStartChar(c)) {
			// qname or boolean
			return parseQNameOrBoolean(parseAsResource);
		} else if (c == '_') {
			// node ID, e.g. _:n1
			return parseNodeID();
		} else if (c == '"') {
			// quoted literal, e.g. "foo" or """foo"""
			return parseQuotedLiteral();
		} else if (StringUtils.isDigit((char) c) || c == '.' || c == '+' || c == '-') {
			// integer or double, e.g. 123 or 1.2e3
			return parseNumber();
		} else if (c == -1) {
			// postpone an error if the subject is an implicit blank node used
			// as subject
			if (subject != null && subject.isAnon() && resources.get(subject.getURI()) != null) {
				eofAfterImplicitBlankNodeAsSubject = true;
				return null;
			}
			throw new RuntimeException("Unexpected end of file!");
		} else {
			throw new RuntimeException("Expected an RDF value here, found '" + (char) c + "'");
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
		subj.setProperty(pred, obj);
	}

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
