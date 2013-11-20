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
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.rdf.LangString;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * Serialization of RDF graphs to <i>Terse RDF Triple Language (Turtle)</i>.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class TurtleWriter {
    /** Serialization state of a Resource. */
    static int NOT_SERIALIZED = 0;

    /** Serialization state of a Resource. */
    static int BEING_SERIALIZED = 1;

    /** Serialization state of a Resource. */
    static int SERIALIZED = 2;

    /**
     * Serialization information for Resources. For each Resource, an according
     * instance of this class is created.
     */
    private class SerData {
	/**
	 * Number of references for this object. Counts how often this Resource
	 * appears in this RDF graph (more precisely: how many properties are
	 * leading to it).
	 */
	int refs = 0;

	/**
	 * Reduction type: how much of the property should be serialized?
	 * 
	 * @see org.universAAL.middleware.rdf.Resource#getPropSerializationType(String)
	 */
	int redType = Resource.PROP_SERIALIZATION_UNDEFINED;

	/** Determines the serialization state for this Resource. */
	int serialized = NOT_SERIALIZED;

	/**
	 * For anonymous Resources, a unique ID is created and stored here as
	 * the URI of the Resource.
	 */
	String nodeID = null;

	/**
	 * The list of types of this Resource. Contains all elements of the
	 * property rdf:type as well as all non-abstract super classes.
	 */
	List types = null;
    }

    /** The character stream to write to. */
    private Writer writer;

    /** The set of namespaces and their abbreviation, maps String to String. */
    private Hashtable namespaceTable;

    /**
     * The set of serialization data for <i>anonymous</i> Resources; maps
     * Resources to {@link SerData}.
     */
    private Hashtable bNodes;

    /**
     * The set of serialization data for <i>non-anonymous</i> Resources; maps
     * Resources to {@link SerData}.
     */
    private Hashtable uriNodes;

    /**
     * True, iff the serialization has already started (to avoid multi-threading
     * problems).
     */
    private boolean writingStarted;

    /** True, iff the description of an RDF statement is closed. */
    private boolean descriptionClosed;

    /** True, iff the first property of a Resource has already been written. */
    private boolean firstPropWritten;

    /** The last Resource that was written. */
    private Resource lastWrittenSubject;

    /** Indentation: denotes the number of spaces in front of the current line. */
    private int embedLevel;

    /** Counter for URIs of blind nodes. */
    private int counter = 0;

    private boolean isOntology = false;

    /** Create a new instance with the specified output stream and indentation. */
    private TurtleWriter(Writer writer, int embedLevel) {
	this.writer = writer;
	namespaceTable = new Hashtable();
	bNodes = new Hashtable();
	uriNodes = new Hashtable();
	writingStarted = false;
	descriptionClosed = true;
	lastWrittenSubject = null;
	this.embedLevel = embedLevel;
    }

    /**
     * Serialize the specified object. All lines of the output stream start with
     * an indentation.
     * 
     * @param o
     *            The object to serialize, must be an instance of
     *            {@link org.universAAL.middleware.rdf.Resource}.
     * @param embedLevel
     *            The indentation.
     * @return The serialized String.
     */
    static String serialize(Object o, int embedLevel) {
	if (!(o instanceof Resource || o instanceof Ontology)) {
	    LogUtils.logError(
		    TurtleUtil.moduleContext,
		    TurtleWriter.class,
		    "serialize",
		    new Object[] { "Cannot serialize objects other than instances of Resource or Ontology!" },
		    null);
	    return null;
	}

	StringWriter sw = new StringWriter(4096);
	try {
	    if (o instanceof Resource)
		new TurtleWriter(sw, embedLevel).serialize((Resource) o);
	    else
		new TurtleWriter(sw, embedLevel).serialize((Ontology) o);
	    return sw.toString();
	} catch (IOException e) {
	    LogUtils.logError(TurtleUtil.moduleContext, TurtleWriter.class,
		    "serialize", null, e);
	    return null;
	}
    }

    /**
     * Analyze the RDF graph and create information (like the namespaces) for
     * serialization.
     */
    private void analyzeObject(Object o, Hashtable nsTable, int reduction) {
	if (o instanceof Resource)
	    if (((Resource) o).serializesAsXMLLiteral())
		if (((Resource) o).numberOfProperties() == 0)
		    countNs(TypeMapper.XSD_NAMESPACE, nsTable);
		else
		    countNs(Resource.RDF_NAMESPACE, nsTable);
	    else {
		SerData d = getData((Resource) o);
		if (d.redType < reduction)
		    d.redType = reduction;
		if (Resource.PROP_SERIALIZATION_OPTIONAL < reduction)
		    analyzeResource((Resource) o, nsTable);
	    }
	else if (o instanceof List)
	    // reduction is simply passed on, because rdf:first & rdf:rest have
	    // no specific values in this regard
	    for (Iterator i = ((List) o).iterator(); i.hasNext();)
		analyzeObject(i.next(), nsTable, reduction);
	else if (!(o instanceof BigInteger) && !(o instanceof Boolean)
		&& !(o instanceof Double) && !(o instanceof BigDecimal))
	    // count the namespace if it is not an abbreviated namespace (see
	    // http://www.w3.org/TR/turtle/, section 2.5.2 & 2.5.3)
	    countNs(TypeMapper.XSD_NAMESPACE, nsTable);
    }

    /**
     * Analyze the RDF graph and create information (like the namespaces) for
     * serialization.
     */
    private void analyzeResource(Resource r, Hashtable nsTable) {
	if (r.isAnon()) {
	    if (countResource(r, bNodes))
		// already handled
		return;
	} else {
	    boolean handled = countResource(r, uriNodes);

	    String uri = r.getURI();
	    if (StringUtils.isQualifiedName(uri))
		countNs(uri.substring(0, uri.lastIndexOf('#') + 1), nsTable);

	    if (handled)
		// already handled
		return;
	}

	for (Enumeration e = r.getPropertyURIs(); e.hasMoreElements();) {
	    String prop = (String) e.nextElement();
	    boolean isRDFType = prop.equals(Resource.PROP_RDF_TYPE);
	    if (!isRDFType && StringUtils.isQualifiedName(prop))
		countNs(prop.substring(0, prop.lastIndexOf('#') + 1), nsTable);
	    int reduction = isRDFType ? Resource.PROP_SERIALIZATION_REDUCED : r
		    .getPropSerializationType(prop);
	    Object o = r.getProperty(prop);
	    if (o instanceof List) {
		if (isRDFType) {
		    List aux = new ArrayList((List) o);
		    for (Iterator i = ((List) o).iterator(); i.hasNext();) {
			String[] types = ManagedIndividual
				.getNonabstractSuperClasses(i.next().toString());
			if (types != null)
			    for (int j = 0; j < types.length; j++) {
				o = new Resource(types[j]);
				if (!aux.contains(o))
				    aux.add(o);
			    }
		    }
		    o = getData(r).types = aux;
		}
		for (Iterator i = ((List) o).iterator(); i.hasNext();)
		    analyzeObject(i.next(), nsTable, reduction);
	    } else {
		if (isRDFType) {
		    List aux = new ArrayList();
		    aux.add(o);
		    String[] types = ManagedIndividual
			    .getNonabstractSuperClasses(o.toString());
		    if (types != null)
			for (int j = 0; j < types.length; j++) {
			    Object oo = new Resource(types[j]);
			    if (!aux.contains(oo))
				aux.add(oo);
			}
		    getData(r).types = aux;
		}
		analyzeObject(o, nsTable, reduction);
	    }
	}
    }

    /**
     * Close the previous RDF statement by writing a specialized finisher to the
     * output stream (" .").
     */
    private void closePreviousStatement() throws IOException {
	if (!descriptionClosed) {
	    // The previous statement still needs to be closed:
	    writer.write(" .");
	    descriptionClosed = true;
	    lastWrittenSubject = null;
	    writeEOL();
	}
    }

    /**
     * Increase the number of the given namespace prefix. Each namespace that is
     * not a known namespace (like <i>rdf</i> or <i>xsd</i>), is abbreviated
     * with the namespace definition <i>ns</i>, <i>ns1</i>, <i>ns2</i>, ... This
     * method checks if a namespace is already available. If this is the case,
     * the number of that namespace is increased.
     * 
     * @param ns
     *            The namespace.
     * @param countTable
     *            Storage for the number for all namespaces; maps the namespace
     *            (String) to the current number (Integer).
     */
    private void countNs(String ns, Hashtable countTable) {
	Integer aux = (Integer) countTable.get(ns);
	if (aux == null)
	    aux = new Integer(1);
	else
	    aux = new Integer(aux.intValue() + 1);
	countTable.put(ns, aux);
    }

    /**
     * For the given Resource, increase the reference counter contained in the
     * serialization data in countTable.
     * 
     * @param r
     *            The Resource.
     * @param countTable
     *            The table holding the serialization data, maps from Resource
     *            to {@link SerData}.
     * @return true, if there is already some serialization data available for
     *         the specified Resource.
     */
    private boolean countResource(Resource r, Hashtable countTable) {
	SerData aux = (SerData) countTable.get(r);
	if (aux == null) {
	    aux = new SerData();
	    countTable.put(r, aux);
	}
	aux.refs++;
	return aux.refs > 1;
    }

    /**
     * Last step of the analyze phase for clean up and creation of unique IDs
     * for anonymous Resources.
     */
    private void finalizeNodes(Resource root, Hashtable nsTable) {

	String prefix = "_:BN";

	for (Iterator i = bNodes.keySet().iterator(); i.hasNext();) {
	    Resource r = (Resource) i.next();
	    SerData d = (SerData) bNodes.get(r);
	    if (Resource.PROP_SERIALIZATION_REDUCED > d.redType)
		i.remove();
	    else {
		if (d.refs > 1 || r == root) {
		    d.nodeID = createUniqueLocalID(prefix, counter++, 6);
		    i.remove();
		    uriNodes.put(r, d);
		}
	    }
	}

	for (Iterator i = uriNodes.keySet().iterator(); i.hasNext();) {
	    Resource r = (Resource) i.next();
	    SerData d = getData(r);
	    if (Resource.PROP_SERIALIZATION_REDUCED > d.redType)
		i.remove();
	    else if (r.numberOfProperties() == 0) {
		d.serialized = SERIALIZED;
		d.refs--;
	    } else if (r != root) {
		String uri = r.getURI();
		if (StringUtils.isQualifiedName(uri))
		    countNs(uri.substring(0, uri.lastIndexOf('#') + 1), nsTable);
	    }
	}
    }

    /**
     * generates a locally unique ID
     * 
     * @param localPrefix
     * @param counter
     * @param counterLen
     * @return String - the created unique local ID
     */
    private static synchronized String createUniqueLocalID(String localPrefix,
	    int counter, int counterLen) {
	return localPrefix + fill0(Long.toHexString(counter), counterLen);
    }

    /**
     * Padd the string passed as the first parameter with zeros up to the length
     * passed as the second parameter
     * 
     * @param arg
     *            - the string to padd
     * @param len
     *            - the required length of the padded result
     * @return the padded string
     */
    private static String fill0(String arg, int len) {
	int diff = len - arg.length();
	if (diff == 0)
	    return arg;
	if (diff < 0)
	    return arg.substring(diff);
	StringBuffer b = new StringBuffer(len);
	while (diff > 0) {
	    b.append("0");
	    diff--;
	}
	b.append(arg);
	return b.toString();
    }

    /**
     * Get the serialization data associated with the specified Resource. If
     * there is no data available yet, a new instance is created and returned.
     */
    private SerData getData(Resource r) {
	SerData d = (SerData) uriNodes.get(r);
	if (d == null) {
	    d = (SerData) bNodes.get(r);
	    if (d == null) {
		d = new SerData();
		(r.isAnon() ? bNodes : uriNodes).put(r, d);
	    }
	}
	return d;
    }

    /**
     * For a given RDF subject, predicate, and the list of objects, get the
     * maximum serialization type.
     */
    private int getListReduction(Resource subj, String pred, List obj) {
	int result = subj.getPropSerializationType(pred);
	for (Iterator i = obj.iterator(); i.hasNext();) {
	    Object o = i.next();
	    if (o instanceof Resource
		    && !((Resource) o).serializesAsXMLLiteral()) {
		SerData d = getData((Resource) o);
		if (d.redType > result)
		    result = d.redType;
	    }
	}
	return result;
    }

    private void handleNamespace(String ns) throws IOException {
	String prefix;
	if (ns.equals(TypeExpression.OWL_NAMESPACE))
	    prefix = "owl";
	else if (ns.equals(Resource.uAAL_SERVICE_NAMESPACE))
	    prefix = "psn";
	else if (ns.equals(Resource.uAAL_VOCABULARY_NAMESPACE))
	    prefix = "pvn";
	else if (ns.equals(Resource.RDF_NAMESPACE))
	    prefix = "rdf";
	else if (ns.equals(Resource.RDFS_NAMESPACE))
	    prefix = "rdfs";
	else if (ns.equals(TypeMapper.XSD_NAMESPACE))
	    prefix = "xsd";
	else
	    prefix = "ns";

	if (namespaceTable.containsValue(prefix)) {
	    int number = 1;
	    while (namespaceTable.containsValue(prefix + number))
		number++;
	    prefix += number;
	}

	namespaceTable.put(ns, prefix);

	closePreviousStatement();
	writeNamespace(prefix, ns);
    }

    private void handleResourceProps(Resource res, List types)
	    throws IOException {
	firstPropWritten = false;
	boolean force = Resource.PROP_SERIALIZATION_FULL == getData(res).redType;
	for (Enumeration e = res.getPropertyURIs(); e.hasMoreElements();)
	    handleStatement(res, e.nextElement().toString(), types, force);
    }

    /** Write an RDF statement to the output stream. */
    private void handleStatement(Resource subj, String pred, List types,
	    boolean force) throws IOException {
	Object obj = pred.equals(Resource.PROP_RDF_TYPE) ? types : subj
		.getProperty(pred);
	if (obj instanceof List)
	    if (((List) obj).isEmpty())
		return;
	// else if (((List) obj).size() == 1)
	// obj = ((List) obj).get(0);

	int redType = (obj instanceof List) ? getListReduction(subj, pred,
		(List) obj) : (obj instanceof Resource && !((Resource) obj)
		.serializesAsXMLLiteral()) ? getData((Resource) obj).redType
		: subj.getPropSerializationType(pred);
	if (!force && redType < Resource.PROP_SERIALIZATION_REDUCED)
	    return;

	if (subj == lastWrittenSubject && firstPropWritten) {
	    writer.write(" ;");
	    writeEOL();
	}

	// Write the statement
	writePredicate(pred);
	writer.write(" ");
	writeValue(obj, subj.isClosedCollection(pred));
	firstPropWritten = true;
    }

    // private void printSerData() {
    // for (Iterator it = bNodes.keySet().iterator(); it.hasNext();) {
    // Object o = it.next();
    // SerData d1 = (SerData) bNodes.get(o);
    // System.out.println("bNodes: " + o + " nodeid: " + d1.nodeID + " types: "
    // + d1.types + " refs: " + d1.refs);
    // }
    // for (Iterator it = uriNodes.keySet().iterator(); it.hasNext();) {
    // Object o = it.next();
    // SerData d1 = (SerData) uriNodes.get(o);
    // System.out.println("uriNodes: " + o + " nodeid: " + d1.nodeID +
    // " types: " + d1.types + " refs: " + d1.refs);
    // }
    // }

    /**
     * Serialization of an ontology. This method first analyzes the graph (e.g.
     * to get the namespaces) and then writes the data to the output stream.
     */
    void serialize(Ontology ont) throws IOException {
	// Note: not the most performant realization, but ontologies are assumed
	// to be serialized not very often
	if (writingStarted)
	    throw new RuntimeException("Document writing has already started");

	// get prepared
	writingStarted = true;
	isOntology = true;
	Hashtable nsTable = new Hashtable();
	Resource[] infos = ont.getResourceList();
	SerData d[] = new SerData[infos.length];
	Resource res;

	// analyze all classes
	for (int i = 0; i < infos.length; i++) {
	    d[i] = new SerData();
	    d[i].redType = Resource.PROP_SERIALIZATION_FULL;
	    res = infos[i];
	    (res.isAnon() ? bNodes : uriNodes).put(res, d[i]);
	    // System.out.println("Analyzing Resource: " +
	    // res.toStringRecursive());
	    analyzeResource(res, nsTable);
	    finalizeNodes(res, nsTable);
	    d[i].refs = 0;
	    bNodes.clear();
	    uriNodes.clear();
	}

	// printSerData();

	// serialize
	counter = 0;
	Hashtable dummyNsTable = new Hashtable();
	writeEOL();
	writeNamespaces(nsTable);
	for (int i = 0; i < infos.length; i++) {
	    res = infos[i];
	    // System.out.println("Writing Resource: " +
	    // res.toStringRecursive());
	    // writer.write("--- Writing Resource: " + res.toString() + "\n");

	    (res.isAnon() ? bNodes : uriNodes).put(res, d[i]);

	    analyzeResource(res, dummyNsTable);
	    finalizeNodes(res, dummyNsTable);
	    d[i].refs--;

	    writeResource(res);

	    bNodes.clear();
	    uriNodes.clear();
	}

	// close
	writer.flush();
	writingStarted = false;
    }

    /**
     * Serialization of the RDF graph. This method first analyzes the graph
     * (e.g. to get the namespaces) and then writes the data to the output
     * stream.
     */
    void serialize(Resource root) throws IOException {
	if (writingStarted)
	    throw new RuntimeException("Document writing has already started");

	// get prepared
	writingStarted = true;
	Hashtable nsTable = new Hashtable();
	SerData d = new SerData();
	d.redType = Resource.PROP_SERIALIZATION_FULL;
	(root.isAnon() ? bNodes : uriNodes).put(root, d);
	analyzeResource(root, nsTable);
	finalizeNodes(root, nsTable);
	d.refs--;

	// printSerData();

	// serialize
	writeEOL();
	writeNamespaces(nsTable);
	writeResource(root);

	// close
	writer.flush();
	writingStarted = false;
    }

    /** Write the <i>End Of Line</i> and the indentation of the next line. */
    private void writeEOL() throws IOException {
	writer.write('\n');
	for (int i = 0; i < embedLevel; i++)
	    writer.write("  ");
	if (!descriptionClosed)
	    writer.write("  ");
    }

    /**
     * Write an RDF Literal. For example, for the literal <code>"15"^^xsd:byte
     * </code>, the lexical form is <code>15</code> and the datatype is
     * <code>xsd:byte</code>.
     * 
     * @param lexicalForm
     *            The value if the literal in lexical form.
     * @param datatype
     *            The data type definition.
     * @throws IOException
     */
    private void writeLiteral(String lexicalForm, String datatype)
	    throws IOException {
	if (lexicalForm.indexOf('\n') > -1 || lexicalForm.indexOf('\r') > -1
		|| lexicalForm.indexOf('\t') > -1) {
	    // Write label as long string
	    writer.write("\"\"\"");
	    writer.write(TurtleUtil.encodeLongString(lexicalForm));
	    writer.write("\"\"\"");
	} else {
	    // Write label as normal string
	    writer.write("\"");
	    writer.write(TurtleUtil.encodeString(lexicalForm));
	    writer.write("\"");
	}

	if (datatype != null) {
	    // Append the literal's datatype (possibly written as an abbreviated
	    // URI)
	    writer.write("^^");
	    writeURI(datatype);
	}
    }

    /**
     * Write a namespace to be used as prefix in the serialization.
     * 
     * @param prefix
     *            The prefix to be used as abbreviation, e.g. <code>rdf</code>.
     * @param name
     *            The namespace the prefix stands for, e.g.
     *            <code>http://www.w3.org/1999/02/22-rdf-syntax-ns#</code>.
     * @throws IOException
     */
    private void writeNamespace(String prefix, String name) throws IOException {
	writer.write("@prefix ");
	writer.write(prefix);
	writer.write(": <");
	writer.write(TurtleUtil.encodeURIString(name));
	writer.write("> .");
	writeEOL();
    }

    /**
     * Write the beginning of the Turtle output (the namespaces) to the output
     * stream.
     * 
     * @param nsTable
     *            Table with the namespaces.
     * @throws IOException
     */
    private void writeNamespaces(Hashtable nsTable) throws IOException {
	int max = 0, curVal;
	Integer cur;
	String dfltNs = null;

	for (Enumeration e = nsTable.keys(); e.hasMoreElements();) {
	    String ns = (String) e.nextElement();
	    cur = (Integer) nsTable.get(ns);
	    if (cur == null)
		continue;
	    curVal = cur.intValue();
	    if (curVal < 2)
		continue;
	    if (dfltNs == null) {
		max = curVal;
		dfltNs = ns;
	    } else if (curVal > max) {
		handleNamespace(dfltNs);
		dfltNs = ns;
		max = curVal;
	    } else
		handleNamespace(ns);
	}

	if (dfltNs != null) {
	    namespaceTable.put(dfltNs, "");
	    closePreviousStatement();
	    writeNamespace("", dfltNs);
	}
    }

    /**
     * Write an RDF predicate (a Resource property). If the predicate is
     * rdf:type, the shorter Turtle version "a" is used.
     */
    private void writePredicate(String predicate) throws IOException {
	if (predicate.equals(Resource.PROP_RDF_TYPE))
	    // Write short-cut for rdf:type
	    writer.write("a");
	else
	    writeURI(predicate);
    }

    /** Write a Resource to the output stream. */
    private void writeResource(Resource res) throws IOException {
	// System.out.println("writeResource: " + res);
	SerData d = (SerData) bNodes.get(res);
	if (d == null) {
	    d = (SerData) uriNodes.get(res);
	    if (descriptionClosed && d.serialized != NOT_SERIALIZED)
		throw new RuntimeException("Attempt to serialize " + res
			+ " twice!");

	    if (d.refs < 0)
		throw new RuntimeException("Resource " + res
			+ " has no open references!");

	    writeURI(res.isAnon() ? d.nodeID : res.getURI());
	    d.refs--;

	    if (descriptionClosed) {
		// the above writeURI starts a new subject description
		writer.write(" ");

		lastWrittenSubject = res;
		descriptionClosed = false;

		d.serialized = BEING_SERIALIZED;
		handleResourceProps(res, d.types);

		if (d.refs == -1)
		    uriNodes.remove(res);
		else
		    d.serialized = SERIALIZED;
		closePreviousStatement();

		res = null;
		for (Enumeration e = uriNodes.keys(); e.hasMoreElements();) {
		    res = (Resource) e.nextElement();
		    if (getData(res).serialized == NOT_SERIALIZED)
			break;
		    else
			res = null;
		}
		if (res != null)
		    if (!isOntology || embedLevel != 0)
			writeResource(res);
	    } else if (d.refs == -1)
		uriNodes.remove(res);
	} else {
	    bNodes.remove(res);
	    writer.write("[");
	    if (res.numberOfProperties() == 0) {
		writer.write("]");
		return;
	    }
	    if (!descriptionClosed)
		embedLevel++;
	    Resource tmp1 = lastWrittenSubject;
	    boolean tmp2 = descriptionClosed;
	    lastWrittenSubject = res;
	    descriptionClosed = false;
	    writeEOL();

	    handleResourceProps(res, d.types);

	    descriptionClosed = true;
	    writeEOL();
	    writer.write("]");
	    if (!tmp2)
		embedLevel--;

	    descriptionClosed = tmp2;
	    lastWrittenSubject = tmp1;
	}
    }

    /**
     * Write the specified URI to the output stream. This method tries to split
     * the URI; if the prefix is a known namespace, an abbreviated URI is
     * written.
     * 
     * @param uri
     *            The URI to write.
     * @throws IOException
     */
    private void writeURI(String uri) throws IOException {
	if (uri.startsWith("_:")) {
	    writer.write(uri);
	    return;
	}

	String prefix = null;

	int splitIdx = TurtleUtil.findURISplitIndex(uri);
	if (splitIdx > 0) {
	    String namespace = uri.substring(0, splitIdx);
	    prefix = (String) namespaceTable.get(namespace);
	}

	if (prefix != null) {
	    // Namespace is mapped to a prefix; write abbreviated URI
	    writer.write(prefix);
	    writer.write(":");
	    writer.write(uri.substring(splitIdx));
	} else {
	    // Write full URI
	    writer.write("<");
	    writer.write(TurtleUtil.encodeURIString(uri));
	    writer.write(">");
	}
    }

    /** Write a new value (Resource or Literal) to the output stream. */
    private void writeValue(Object val, boolean closed) throws IOException {
	if (val instanceof List) {
	    if (closed) {
		writer.write("(");
		embedLevel++;
		writeEOL();
		for (Iterator i = ((List) val).iterator(); i.hasNext();) {
		    writeValue(i.next(), true);
		    if (i.hasNext())
			writeEOL();
		}
		embedLevel--;
		writeEOL();
		writer.write(")");
	    } else {
		embedLevel++;
		int last = ((List) val).size() - 1;
		for (int i = 0; i < last; i++) {
		    writeValue(((List) val).get(i), false);
		    writer.write(" ,");
		    writeEOL();
		}
		if (last >= 0) {
		    // if this happens, then the list is empty
		    // TODO: should we handle this differently?
		    writeValue(((List) val).get(last), false);
		}
		embedLevel--;
	    }
	    return;
	} else if (val instanceof Resource) {
	    if (((Resource) val).serializesAsXMLLiteral()) {
		if (((Resource) val).isAnon()
			|| ((Resource) val).numberOfProperties() > 0) {
		    writeLiteral(TurtleWriter.serialize(val, embedLevel + 2),
			    TurtleUtil.xmlLiteral);
		} else {
		    writeLiteral(((Resource) val).getURI(),
			    TypeMapper.getDatatypeURI(Resource.class));
		}
	    } else {
		writeResource((Resource) val);
	    }
	    return;
	} else if (val instanceof Boolean) {
	    writer.write(val.toString());
	    return;
	} else if (val instanceof Double) {
	    if (((Double) val) == Double.POSITIVE_INFINITY) {
		writer.write("INF");
		return;
	    } else if (((Double) val) == Double.NEGATIVE_INFINITY) {
		writer.write("-INF");
		return;
	    } else {
		String strval = val.toString();
		// Value must have 'e' or 'E', we just check this here. There is
		// also the possibility to force a formatting (should we do
		// this?)
		if (strval.indexOf('e') >= 0 || strval.indexOf('E') >= 0) {
		    writer.write(strval);
		    return;
		}
		// else: use standard formatting with lexical form and datatype
	    }
	} else if (val instanceof BigInteger) {
	    writer.write(((BigInteger) val).toString());
	    return;
	} else if (val instanceof BigDecimal) {
	    String strval = val.toString();
	    // Value must not have 'e' or 'E' and must have '.', we just check
	    // this here. There is
	    // also the possibility to force a formatting (should we do
	    // this?)
	    if (strval.indexOf('e') == -1 && strval.indexOf('E') == -1
		    && strval.indexOf('.') >= 0) {
		writer.write(strval);
		return;
	    }
	    // else: use standard formatting with lexical form and datatype
	} else if (val instanceof LangString) {
	    LangString ls = (LangString) val;
	    writer.write("\"");
	    writer.write(ls.getString());
	    if (((LangString) val).getLang().equals("")) {
		writer.write("\"^^");
		writeURI(TypeMapper.getDatatypeURI(String.class));
	    } else {
		writer.write("\"@");
		writer.write(ls.getLang());
	    }
	    return;
	}

	// everything else as quoted literal with lexical form and datatype
	String[] pair = TypeMapper.getXMLInstance(val);
	writeLiteral(pair[0], pair[1]);
    }
}
