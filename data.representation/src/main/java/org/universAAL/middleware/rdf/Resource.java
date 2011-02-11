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
package org.universAAL.middleware.rdf;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.universAAL.middleware.util.ResourceComparator;
import org.universAAL.middleware.util.StringUtils;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class Resource {

	protected static final String ANON_URI_PREFIX = "urn:anonymous:";

	public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	public static final String RDFS_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";
	
	public static final String RDF_EMPTY_LIST = RDF_NAMESPACE + "nil";

	public static final String PROP_RDF_FIRST = RDF_NAMESPACE + "first";

	public static final String PROP_RDF_REST = RDF_NAMESPACE + "rest";

	public static final String PROP_RDF_TYPE = RDF_NAMESPACE + "type";

	public static final String PROP_RDFS_COMMENT = RDFS_NAMESPACE + "comment";

	public static final String PROP_RDFS_LABEL = RDFS_NAMESPACE + "label";

	public static final String TYPE_RDF_LIST = RDF_NAMESPACE + "List";

	public static final String uAAL_NAMESPACE_PREFIX = "http://ontology.universAAL.org/";
	
	public static final String uAAL_SERVICE_NAMESPACE = uAAL_NAMESPACE_PREFIX + "Service.owl#";
	
	public static final String uAAL_VOCABULARY_NAMESPACE = uAAL_NAMESPACE_PREFIX + "uAAL.owl#";
	
	public static final String PROP_uAAL_INVOLVED_HUMAN_USER = 
		uAAL_VOCABULARY_NAMESPACE + "theInvolvedHumanUser";
	
	/**
	 * Legal return values for {@link #getPropSerializationType(String)}.
	 * <code>PROP_SERIALIZATION_OPTIONAL</code> says that, when serializing an instance of
	 * this class in a minimized way, a property can be ignored.
	 * <code>PROP_SERIALIZATION_REDUCED</code> says that, when serializing an instance of
	 * this class in a minimized way, a property must be included but the value can be
	 * represented in its reduced form.
	 * <code>PROP_SERIALIZATION_FULL</code> says that, when serializing an instance of
	 * this class in a minimized way, a property must be included in its full form.
	 */
	public static final int PROP_SERIALIZATION_UNDEFINED = 0;
	public static final int PROP_SERIALIZATION_OPTIONAL = 1;
	public static final int PROP_SERIALIZATION_REDUCED = 2;
	public static final int PROP_SERIALIZATION_FULL = 3;

	private static Hashtable uriResource = new Hashtable();
	private static Hashtable uriRsrcClass = new Hashtable();
		
	protected final int ns_delim_index;
	protected final Hashtable props = new Hashtable();
	protected final String uri;
	protected boolean blockAddingTypes = false;
	protected boolean isXMLLiteral = false;
	
	public Resource() {
		uri = Resource.generateAnonURI();
		ns_delim_index = -1;
	}
	
	public Resource(boolean isXMLLiteral) {
		uri = Resource.generateAnonURI();
		ns_delim_index = -1;
		this.isXMLLiteral = isXMLLiteral;
	}
	
	public Resource(String uri) {
		if (uri == null) {
			this.uri = Resource.generateAnonURI();
			ns_delim_index = -1;
		} else {
			this.uri = uri;
			ns_delim_index = isQualifiedName(uri)? uri.lastIndexOf('#') : -1;
		}
	}
	
	public Resource(String uri, boolean isXMLLiteral) {
		if (uri == null) {
			this.uri = Resource.generateAnonURI();
			ns_delim_index = -1;
		} else {
			this.uri = uri;
			ns_delim_index = isQualifiedName(uri)? uri.lastIndexOf('#') : -1;
		}
		this.isXMLLiteral = isXMLLiteral;
	}
	
	/**
	 * Creates an instance of Resource with a URI that is created by appending a unique ID
	 * to the given 'uriPrefix'. This constructor has a pseudo parameter 'numProps' in order
	 * to make it distinct from the other constructor that also takes a string. Later versions
	 * of Resource may decide to make some use of numProps in some way, however.
	 */
	protected Resource(String uriPrefix, int numProps) {
		uri = uriPrefix + StringUtils.createUniqueID();
		ns_delim_index = isQualifiedName(uri)? uri.lastIndexOf('#') : -1;
	}

	protected static final void addResourceClass(String uri, Class clz) {
		if (StringUtils.isNonEmpty(uri)
				&&  clz != null
				&&  !uriRsrcClass.containsKey(uri))
			uriRsrcClass.put(uri, clz);
	}
	
	protected static final void addSpecialResource(Resource r) {
		if (r != null  &&  !r.isAnon())
			uriResource.put(r.uri, r);
	}

	public static final Resource asRDFList(List members, boolean isXMLLiteral) {
		if (members == null  ||  members.isEmpty())
			return new Resource(RDF_EMPTY_LIST, isXMLLiteral);
		Resource result = new Resource(isXMLLiteral);
		result.addType(TYPE_RDF_LIST, true);
		result.props.put(PROP_RDF_FIRST, members.remove(0));
		result.props.put(PROP_RDF_REST, members);
		return result;
	}
	
	public static final String generateAnonURI() {
		return ANON_URI_PREFIX + StringUtils.createUniqueID();
	}
	
	public static Resource getResource(String classURI, String instanceURI) {
		if (classURI == null)
			return null;
		
		Class clz = (Class) uriRsrcClass.get(classURI);
		if (clz == null)
			return null;
		
		try {
			if (Resource.isAnonymousURI(instanceURI))
				return (Resource) clz.newInstance();
			else {
				Object o = uriResource.get(instanceURI);
				if (o instanceof Resource  &&  o.getClass() ==  clz)
					return (Resource) o;
				return (Resource) clz.getConstructor(new Class[] {String.class})
						.newInstance(new Object[] {instanceURI});
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	public static final boolean isAnonymousURI(String uri) {
		return uri == null  ||  uri.startsWith(ANON_URI_PREFIX);
	}
	
	public static final boolean isQualifiedName(String uri) {
		return StringUtils.isQualifiedName(uri)
			&& !uri.startsWith(ANON_URI_PREFIX);
	}

	public final void addType(String typeURI, boolean blockFurtherTypes) {
		if (!this.blockAddingTypes) {
			if (typeURI != null) {
				Object o = props.get(PROP_RDF_TYPE);
				Resource type = new Resource(typeURI);
				if (o instanceof List  &&  !((List) o).contains(type))
					((List) o).add(type);
				else {
					List l = new ArrayList(2);
					if (o instanceof Resource  &&  !type.equals(o))
						l.add(o);
					l.add(type);
					props.put(PROP_RDF_TYPE, l);
				}
			}
			this.blockAddingTypes = blockFurtherTypes;
		}
	}
	
	public List asList() {
		String type = getType();
		if (type == null  ||  !type.equals(TYPE_RDF_LIST))
			return null;
		List result = new ArrayList();
		asList(result);
		return result;
	}
	
	public void asList(List l) {
		if (!uri.equals(RDF_EMPTY_LIST)) {
			Object o = props.get(PROP_RDF_FIRST);
			if (o != null) {
				l.add(o);
				o = props.get(PROP_RDF_REST);
				if (o instanceof Resource) {
					String type = ((Resource) o).getType();
					if (type != null  &&  type.equals(TYPE_RDF_LIST))
						((Resource) o).asList(l);
					// TODO: log that rest must be a list or rdf:nil
				} else if (o instanceof List)
					// the rest is already a list object
					l.addAll((List) o);
				// TODO: add a last 'else' with log that rest must be either a List, or a Resource of type rdf:List, or rdf:nil
			}
		}
	}

	public boolean changeProperty(String propURI, Object value) {
		if (propURI != null) {
			Object o = props.remove(propURI);
			if (value == null)
				return true;
			
			setProperty(propURI, value);
			if (props.get(propURI) == value)
				return true;
			if (o != null)
				props.put(propURI, o);
		}
		return false;
	}
	
	public Resource copyAsXMLLiteral() {
		Resource copy = new Resource(uri, true);
		for (Enumeration e = props.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			copy.props.put(key, props.get(key));
		}	
		return copy;
	}
	
	public Resource deepCopy() {
		Resource copy = new Resource(uri, isXMLLiteral);
		copy.blockAddingTypes = blockAddingTypes;
		for (Enumeration e = props.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object value = props.get(key);
			if (value instanceof Resource)
				value = ((Resource) value).deepCopy();
			copy.props.put(key, value);
		}	
		return copy;
	}
	
	public boolean equals(Object other) {
		return (this == other)?
				true : (other instanceof Resource)?
						((!isAnon()  &&  uri.equals(((Resource) other).uri))
								|| new ResourceComparator().areEqual(this, (Resource) other))
						: false;
	}
	
	public String getResourceComment() {
		Object val = props.get(PROP_RDFS_COMMENT);
		return (val instanceof String)? (String) val : null;
	}
	
	public String getResourceLabel() {
		Object val = props.get(PROP_RDFS_LABEL);
		return (val instanceof String)? (String) val : null;
	}
	
	public final String getLocalName() {
		return (ns_delim_index < 0)? null : uri.substring(ns_delim_index + 1);
	}
	
	public final String getNamespace() {
		return (ns_delim_index < 0)? null : uri.substring(0, ns_delim_index);
	}
	
	public final Object getProperty(String propURI) {
		return props.get(propURI);
	}
	
	public final Enumeration getPropertyURIs() {
		return props.keys();
	}
	
	/**
	 * Answers if the given property has to be considered when serializing this
	 * individual in a minimized way, and if not ignore-able, whether its value
	 * should be presented in its full form or can be reduced. The return value
	 * must be one of {@link #PROP_SERIALIZATION_OPTIONAL}, {@link
	 * #PROP_SERIALIZATION_REDUCED}, or {@link #PROP_SERIALIZATION_FULL}. It can
	 * be assumed that the given property is one of those returned by {@link
	 * #getPropertyURIs()}. Decision criterion should be if the value of this
	 * property is absolutely necessary when this resource is being sent to a
	 * remote node. If the subclass rates it as unlikely that the receiver side
	 * would need this info, the answer should be <code>PROP_SERIALIZATION_OPTIONAL</code>
	 * in favor of lower communication traffic and higher performance even at
	 * risk of a possible additional query on the receiver side for fetching
	 * this info. With the same rationale, if a property should be included in
	 * the process of serialization, it is preferable to include it in a reduced
	 * form; in this case the return value should be <code>PROP_SERIALIZATION_REDUCED</code>,
	 * otherwise <code>PROP_SERIALIZATION_FULL</code> can be returned.
	 * 
	 * Subclasses should normally overwrite this method as this default implementation
	 * returns always <code>PROP_SERIALIZATION_FULL</code>.
	 */
	public int getPropSerializationType(String propURI) {
		return PROP_SERIALIZATION_FULL;
	}
	
	public Object getStaticFieldValue(String fieldName, Object defaultValue) {
		try {
			Object o = getClass().getDeclaredField(fieldName).get(null);
			return (o == null)? defaultValue : o;
		} catch (Exception e) {
			return null;
		} 
	}
	
	/**
	 * Returns the URI of the first type added to the list of types of this resource.
	 */
	public final String getType() {
		Object o = props.get(PROP_RDF_TYPE);
		if (o instanceof List)
			if (((List) o).isEmpty())
				return null;
			else
				return ((List) o).get(0).toString();
		else if (o instanceof Resource)
			return o.toString();
		else
			return null;
	}
	
	/**
	 * Returns the URIs of all known types of this resource. 
	 */
	public final String[] getTypes() {
		Object o = props.get(PROP_RDF_TYPE);
		if (o instanceof List) {
			String[] types = new String[((List) o).size()];
			for (int i=0; i<((List) o).size(); i++)
				types[i] = ((List) o).get(i).toString();
			return types;
		} else if (o instanceof Resource)
			return new String[] {o.toString()};
		else
			return new String[0];
	}
	
	public final String getURI() {
		return uri;
	}
	
	public int hashCode() {
		return uri.hashCode();
	}
	
	public boolean hasProperty(String propURI) {
		return (propURI != null  &&  props.containsKey(propURI));
	}
	
	public final boolean hasQualifiedName() {
		return ns_delim_index >= 0;
	}
	
	public final boolean isAnon() {
		return uri.startsWith(ANON_URI_PREFIX);
	}
	
	/**
	 * Returns true if the value of the given property should be treated as an rdf:List.
	 * Serializers can use this to determine if a multi-valued property should be
	 * serialized using the concept of rdf:List or the property should appear as often
	 * as the number of values assigned to the property. The default behavior is that a
	 * property associated with an instance of {@link java.util.List} is assumed to be
	 * a closed collection. Subclasses can change this, if needed.
	 */
	public boolean isClosedCollection(String propURI) {
		if (PROP_RDF_TYPE.equals(propURI)  ||  propURI == null)
			return false;
		
		return (props.get(propURI) instanceof List);
	}
	
	/**
	 * Returns true, if the state of the resource is valid, otherwise false.
	 * <p>
	 * Subclasses should overwrite this methods as the default implementation returns always true.
	 */
	public boolean isWellFormed() {
		return true;
	}
	
	public final int numberOfProperties() {
		return props.size();
	}
	
	public boolean representsQualifiedURI() {
		return ns_delim_index > 0  &&  props.size() == 0;
	}
	
	/**
	 * Resources to be serialized and parsed as rdf:XMLLiteral must overwrite this method
	 * and return true. Serializers and parsers can use this as a hint.
	 */
	public boolean serializesAsXMLLiteral() {
		return isXMLLiteral;
	}
	
	public void setResourceComment(String comment) {
		if (comment != null  &&  !props.containsKey(PROP_RDFS_COMMENT))
			props.put(PROP_RDFS_COMMENT, comment);
	}
	
	public void setResourceLabel(String label) {
		if (label != null  &&  !props.containsKey(PROP_RDFS_LABEL))
			props.put(PROP_RDFS_LABEL, label);
	}
	
	/**
	 * Adds a statement with this resource as the subject, the given <code>propURI</code>
	 * as the predicate and the given value as the object. Subclasses must override this
	 * in order to decide if the statement to be added fits the general class constraints.
	 * If not, the call of this method should be ignored. For each property only one single
	 * call may be made to this method, unless subsequent calls to this method for setting the
	 * value of the same property are treated as an update for an update-able property.
	 * Multi-valued properties must be set using an instance of {@link java.util.List}.
	 * The differentiation, if a such list should be treated as an rdf:List, can be made
	 * with the help of {@link #isClosedCollection(String)}. The default implementation
	 * here accepts all property-value pairs blindly except for rdf:type which is handled
	 * if the value is a type URI, a Resource or a java.util.List of them.
	 * <p>Note: The setting of the property rdf:type is being handled by this class via
	 * the final methods {@link #addType(String, boolean)}, {@link #getType()}, and {@link #getTypes()}.
	 * Although these methods give the view of handling type URIs as strings, but in reality
	 * the types are stored as direct instances of this class.
	 * So, the subclasses should ignore calls for setting rdf:type; if not, then the subclass
	 * must pay attention that the value should be a {@link List} of direct instances of
	 * this class so that (1) the {@link #toString()} method returns just the URI and (2)
	 * the serializers get no problems with the value. Also, settings via subclasses
	 * may be overwritten by this class if a subsequent
	 * call to  {@link #addType(String, boolean)} is made.
	 */
	public void setProperty(String propURI, Object value) {
		if (propURI != null  &&  value != null)
			if (PROP_RDF_TYPE.equals(propURI)) {
				if (value instanceof String)
					addType((String) value, false);
				else if (value instanceof Resource)
					addType(((Resource) value).uri, false);
				else if (value instanceof List)
					for (int i=0; i<((List) value).size(); i++)
						setProperty(propURI, ((List) value).get(i));
			} else if (PROP_RDFS_COMMENT.equals(propURI) ||  PROP_RDFS_LABEL.equals(propURI)) {
				if (!props.containsKey(propURI)  &&  value instanceof String)
					props.put(propURI, value);
			} else
				props.put(propURI, value);
	}
	
	public boolean setPropertyPath(String[] propPath, Object value) {
		return setPropertyPathFromOffset(propPath, 0, value, false);
	}
	
	public boolean setPropertyPath(String[] propPath, Object value, boolean force) {
		return setPropertyPathFromOffset(propPath, 0, value, force);
	}
	
	public boolean setPropertyPathFromOffset(String[] propPath, int fromIndex, Object value, boolean force) {
		try {
			if (fromIndex == propPath.length-1) {
				if (force)
					return changeProperty(propPath[fromIndex], value);
				setProperty(propPath[fromIndex], value);
				return props.get(propPath[fromIndex]) == value;
			}
			
			Object tmp = props.get(propPath[fromIndex]);
			if (tmp == null) {
				if (value == null)
					return true;
				tmp = new Resource();
				props.put(propPath[fromIndex], tmp);
			} else if (!(tmp instanceof Resource))
				return false;
			
			return ((Resource) tmp).setPropertyPathFromOffset(propPath, fromIndex+1, value, force);
		} catch (Exception e) {
			return false;
		}
	}

	public String toString() {
		return uri;
	}
}
