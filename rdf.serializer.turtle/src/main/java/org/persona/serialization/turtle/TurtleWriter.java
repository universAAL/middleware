package org.persona.serialization.turtle;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.persona.middleware.PResource;
import org.persona.middleware.TypeMapper;
import org.persona.middleware.util.LogUtils;
import org.persona.middleware.util.StringUtils;
import org.persona.ontology.ManagedIndividual;

import de.fhg.igd.ima.sodapop.msg.Message;

/**
 * @author mtazari
 * 
 */
public class TurtleWriter {
	static int NOT_SERIALIZED = 0;
	static int BEING_SERIALIZED = 1;
	static int SERIALIZED = 2;
	
	private class SerData {
		int refs = 0, redType = PResource.PROP_SERIALIZATION_UNDEFINED;
		int serialized = NOT_SERIALIZED;
		String nodeID = null;
		List types = null;
	}

	static String serialize(Object o, int embedLevel) {
		if (!(o instanceof PResource)) {
			LogUtils.logError(Activator.logger, "TurtleWriter", "serialize",
					new Object[]{"Cannot serialize objects other than instances of PResource!"}, null);
			return null;
		}

		StringWriter sw = new StringWriter(4096);
		try {
			new TurtleWriter(sw, embedLevel).serialize((PResource) o);
			return sw.toString();
		} catch (IOException e) {
			LogUtils.logError(Activator.logger, "TurtleWriter", "serialize", null, e);
			return null;
		}
	}

	private Writer writer;

	private Hashtable namespaceTable, bNodes, uriNodes;

	private boolean writingStarted;

	private boolean descriptionClosed, firstPropWritten;

	private PResource lastWrittenSubject;

	private int embedLevel;

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

	private void analyzeObject(Object o, Hashtable nsTable, int reduction) {
		if (o instanceof PResource)
			if (((PResource) o).serializesAsXMLLiteral())
				if (((PResource) o).numberOfProperties() == 0)
					countNs(TypeMapper.XSD_NAMESPACE, nsTable);
				else
					countNs(PResource.RDF_NAMESPACE, nsTable);
			else {
				SerData d = getData((PResource) o);
				if (d.redType < reduction)
					d.redType = reduction;
				if (PResource.PROP_SERIALIZATION_OPTIONAL < reduction)
					analyzeResource((PResource) o, nsTable);
			}
		else if (o instanceof List)
			// reduction is simply passed on, because rdf:first & rdf:rest have no specific values in this regard
			for (Iterator i = ((List) o).iterator(); i.hasNext();)
				analyzeObject(i.next(), nsTable, reduction);
		else if (!(o instanceof Integer)  &&  !(o instanceof Boolean)  &&  !(o instanceof Double))
			countNs(TypeMapper.XSD_NAMESPACE, nsTable);
	}

	private void analyzeResource(PResource r, Hashtable nsTable) {
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
			boolean isRDFType = prop.equals(PResource.PROP_RDF_TYPE);
			if (!isRDFType && StringUtils.isQualifiedName(prop))
				countNs(prop.substring(0, prop.lastIndexOf('#') + 1), nsTable);
			int reduction = isRDFType ? PResource.PROP_SERIALIZATION_REDUCED
					: r.getPropSerializationType(prop);
			Object o = r.getProperty(prop);
			if (o instanceof List) {
				if (isRDFType) {
					List aux = new ArrayList((List) o);
					for (Iterator i = ((List) o).iterator(); i.hasNext();) {
						String[] types = ManagedIndividual
								.getNonabstractSuperClasses(i.next().toString());
						if (types != null)
							for (int j = 0; j < types.length; j++) {
								o = new PResource(types[j]);
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
							Object oo = new PResource(types[j]);
							if (!aux.contains(oo))
								aux.add(oo);
						}
					getData(r).types = aux;
				}
				analyzeObject(o, nsTable, reduction);
			}
		}
	}

	private void closePreviousStatement() throws IOException {
		if (!descriptionClosed) {
			// The previous statement still needs to be closed:
			writer.write(" .");
			descriptionClosed = true;
			lastWrittenSubject = null;
			writeEOL();
		}
	}

	private void countNs(String ns, Hashtable countTable) {
		Integer aux = (Integer) countTable.get(ns);
		if (aux == null)
			aux = new Integer(1);
		else
			aux = new Integer(aux.intValue() + 1);
		countTable.put(ns, aux);
	}

	private boolean countResource(PResource r, Hashtable countTable) {
		SerData aux = (SerData) countTable.get(r);
		if (aux == null) {
			aux = new SerData();
			countTable.put(r, aux);
		}
		aux.refs++;
		return aux.refs > 1;
	}

	private void finalizeNodes(PResource root, Hashtable nsTable) {

		String prefix = "_:BN";
		int counter = 0;

		for (Iterator i = bNodes.keySet().iterator(); i.hasNext();) {
			PResource r = (PResource) i.next();
			SerData d = (SerData) bNodes.get(r);
			if (PResource.PROP_SERIALIZATION_REDUCED > d.redType)
				i.remove();
			else {
				if (d.refs > 1 || r == root) {
					d.nodeID = Message
							.createUniqueLocalID(prefix, counter++, 6);
					i.remove();
					uriNodes.put(r, d);
				}
			}
		}

		for (Iterator i = uriNodes.keySet().iterator(); i.hasNext();) {
			PResource r = (PResource) i.next();
			SerData d = getData(r);
			if (PResource.PROP_SERIALIZATION_REDUCED > d.redType)
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

	private SerData getData(PResource r) {
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

	private int getListReduction(PResource subj, String pred, List obj) {
		int result = subj.getPropSerializationType(pred);
		for (Iterator i = obj.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof PResource  &&  !((PResource) o).serializesAsXMLLiteral()) {
				SerData d = getData((PResource) o);
				if (d.redType > result)
					result = d.redType;
			}
		}
		return result;
	}

	private void handleNamespace(String ns) throws IOException {
		String prefix;
		if (ns.equals(PResource.OWL_NAMESPACE))
			prefix = "owl";
		else if (ns.equals(PResource.PERSONA_SERVICE_NAMESPACE))
			prefix = "psn";
		else if (ns.equals(PResource.PERSONA_VOCABULARY_NAMESPACE))
			prefix = "pvn";
		else if (ns.equals(PResource.RDF_NAMESPACE))
			prefix = "rdf";
		else if (ns.equals(PResource.RDFS_NAMESPACE))
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

	private void handleResourceProps(PResource res, List types) throws IOException {
		firstPropWritten = false;
		boolean force = PResource.PROP_SERIALIZATION_FULL == getData(res).redType;
		for (Enumeration e = res.getPropertyURIs(); e.hasMoreElements();)
			handleStatement(res, e.nextElement().toString(), types, force);
	}

	private void handleStatement(PResource subj, String pred, List types, boolean force)
			throws IOException {
		Object obj = pred.equals(PResource.PROP_RDF_TYPE)? types : subj.getProperty(pred);
		if (obj instanceof List)
			if (((List) obj).isEmpty())
				return;
//			else if (((List) obj).size() == 1)
//				obj = ((List) obj).get(0);

		int redType = (obj instanceof List) ? getListReduction(subj, pred,
				(List) obj)
				: (obj instanceof PResource  &&  !((PResource) obj).serializesAsXMLLiteral()) ? getData((PResource) obj).redType
						: subj.getPropSerializationType(pred);
		if (!force && redType < PResource.PROP_SERIALIZATION_REDUCED)
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

	void serialize(PResource root) throws IOException {
		if (writingStarted)
			throw new RuntimeException("Document writing has already started");

		// get prepared
		writingStarted = true;
		Hashtable nsTable = new Hashtable();
		SerData d = new SerData();
		d.redType = PResource.PROP_SERIALIZATION_FULL;
		(root.isAnon() ? bNodes : uriNodes).put(root, d);
		analyzeResource(root, nsTable);
		finalizeNodes(root, nsTable);
		d.refs--;

		// serialize
		writeEOL();
		writeNamespaces(nsTable);
		writeResource(root);

		// close
		writer.flush();
		writingStarted = false;
	}

	private void writeEOL() throws IOException {
		writer.write('\n');
		for (int i = 0; i < embedLevel; i++)
			writer.write("  ");
		if (!descriptionClosed)
			writer.write("  ");
	}

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

	private void writeNamespace(String prefix, String name) throws IOException {
		writer.write("@prefix ");
		writer.write(prefix);
		writer.write(": <");
		writer.write(TurtleUtil.encodeURIString(name));
		writer.write("> .");
		writeEOL();
	}

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

	private void writePredicate(String predicate) throws IOException {
		if (predicate.equals(PResource.PROP_RDF_TYPE))
			// Write short-cut for rdf:type
			writer.write("a");
		else
			writeURI(predicate);
	}

	private void writeResource(PResource res) throws IOException {
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
					res = (PResource) e.nextElement();
					if (getData(res).serialized == NOT_SERIALIZED)
						break;
					else
						res = null;
				}
				if (res != null)
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
			PResource tmp1 = lastWrittenSubject;
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

	private void writeValue(Object val, boolean closed) throws IOException {
		if (val instanceof List)
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
				writeValue(((List) val).get(last), false);
				embedLevel--;
			}
		else if (val instanceof PResource)
			if (((PResource) val).serializesAsXMLLiteral())
				if (((PResource) val).isAnon()
						|| ((PResource) val).numberOfProperties() > 0)
					writeLiteral(TurtleWriter.serialize(val, embedLevel+2),
							TurtleUtil.xmlLiteral);
				else
					writeLiteral(((PResource) val).getURI(), TypeMapper
							.getDatatypeURI(PResource.class));
			else
				writeResource((PResource) val);
		else if (val instanceof Boolean  ||  val instanceof Double  ||  val instanceof Integer)
			writer.write(val.toString());
		else {
			String[] pair = TurtleUtil.typeMapper.getXMLInstance(val);
			writeLiteral(pair[0], pair[1]);
		}
	}
}
