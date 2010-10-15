/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.persona.middleware.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.persona.middleware.PResource;

/**
 * A PropertyPath is a closed list of property URIs that relates a resource to a set of other
 * resources or literal values as it could be reached by conventional join operations over
 * an RDF database. The following example should help to understand this concept better: Assuming
 * that an RDF database contains the following triples:
 * <p>
 * <code>(a p1 b)<br/>(a p1 c)<br/>(b p2 d)<br/>(c p2 e)<br/>(c p2 f)<br/>(d p3 g)<br/>
 * (d p3 h)<br/>(e p3 i)<br/>(f p3 j)<br/>(f p3 k)</code>
 * <p>
 * Then the following relations can be deduced using the property path <code>{p1, p2, p3}</code>:
 * <p>
 * <code>(a {p1, p2, p3} g)<br/>(a {p1, p2, p3} h)<br/>(a {p1, p2, p3} i)<br/>(a {p1, p2, p3} j)
 * <br/>(a {p1, p2, p3} k)<code/>.
 * <p>As the type hierarchy of PropertyPath plays no specific role in ontological reasoning, it is not
 * defined as subclass of {@link org.persona.ontology.ManagedIndividual} but just as a
 * {@link org.persona.middleware.PResource}.
 * 
 * @author mtazari
 */
public class PropertyPath extends PResource {
	
	/**
	 * The only property of a property path is the one pointing to the list of properties
	 * that build up the path.
	 */
	public static final String PROP_PROPERTY_PATH = PERSONA_SERVICE_NAMESPACE + "thePath";
	public static final String TYPE_PROPERTY_PATH = PERSONA_SERVICE_NAMESPACE + "PropertyPath";
	
	public static String[] getSubpath(String[] path, int i) {
		if (path == null  ||  i == 0)
			return path;
		
		if (i < 1  ||  i >= path.length)
			return null;

		int l = path.length - i;
		String[] aux = new String[l--];
		for (int j=path.length-1; j >= i; j--,l--)
			aux[l] = path[j];
		return aux;
	}
	
	public static boolean pathHasPrefix(String[] path, String[] prefix) {
		if (path == null  ||  prefix == null  ||  prefix.length == 0  ||  path.length < prefix.length)
			return false;
		
		for (int i=0; i<prefix.length; i++)
			if (prefix[i] == null  ||  !prefix[i].equals(path[i]))
				return false;
		
		return true;
	}
	
	public static PropertyPath toPropertyPath(PResource pr) {
		if (pr instanceof PropertyPath)
			return (PropertyPath) pr;
		if (pr == null  ||  !TYPE_PROPERTY_PATH.equals(pr.getType()))
			return null;
		if (pr.numberOfProperties() != 2)
			return null;
		PropertyPath result = pr.isAnon()? new PropertyPath(pr.serializesAsXMLLiteral())
				: new PropertyPath(pr.getURI(), pr.serializesAsXMLLiteral());
		result.setProperty(PROP_PROPERTY_PATH, pr.getProperty(PROP_PROPERTY_PATH));
		return result.props.containsKey(PROP_PROPERTY_PATH)? result : null;
	}
	
	public PropertyPath() {
		super();
		addType(TYPE_PROPERTY_PATH, true);
	}
	
	public PropertyPath(String uri) {
		super(uri);
		addType(TYPE_PROPERTY_PATH, true);
	}
	
	public PropertyPath(boolean isXMLLiteral) {
		super(isXMLLiteral);
		addType(TYPE_PROPERTY_PATH, true);
	}
	
	public PropertyPath(String uri, boolean isXMLLiteral) {
		super(uri, isXMLLiteral);
		addType(TYPE_PROPERTY_PATH, true);
	}
	
	public PropertyPath(String uri, boolean isXMLLiteral, String[] thePath) {
		super(uri, isXMLLiteral);
		addType(TYPE_PROPERTY_PATH, true);
		setThePath(thePath);
	}
	
	public boolean equals(Object other) {
		return (other instanceof PropertyPath
				&& props.get(PROP_PROPERTY_PATH) != null
				&& props.get(PROP_PROPERTY_PATH).equals(
						((PropertyPath) other).props.get(PROP_PROPERTY_PATH)));
	}
	
	public String getLastPathElement() {
		List l = (List) props.get(PROP_PROPERTY_PATH);
		if (l == null  ||  l.isEmpty())
			return null;
		
		Object o = l.get(l.size() -1 );
		return (o == null)? null : o.toString();
	}

	public String[] getThePath() {
		List l = (List) props.get(PROP_PROPERTY_PATH);
		if (l == null)
			return null;
		
		String[] result = new String[l.size()];
		for (int i=0; i<l.size(); i++)
			result[i] = l.get(i).toString();
		return result;
	}

	/**
	 * @see org.persona.middleware.PResource#isClosedCollection(java.lang.String)
	 */
	public boolean isClosedCollection(String propURI) {
		return PROP_PROPERTY_PATH.equals(propURI);
	}

	/**
	 * @see org.persona.middleware.PResource#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String propURI, Object o) {
		if (PROP_PROPERTY_PATH.equals(propURI)  &&  !props.containsKey(PROP_PROPERTY_PATH)) {
			ArrayList l = new ArrayList();
			if (o instanceof List) {
				for (Iterator i=((List) o).iterator(); i.hasNext();) {
					o = i.next();
					if (o instanceof PResource  &&  ((PResource) o).representsQualifiedURI())
						l.add(o);
					else if (o instanceof String  &&  PResource.isQualifiedName((String) o))
						l.add(new PResource((String) o));
					else
						return;
				}
			} else if (o instanceof PResource  &&  ((PResource) o).representsQualifiedURI())
				l.add(o);
			else if (o instanceof String  &&  PResource.isQualifiedName((String) o))
				l.add(new PResource((String) o));
			else
				return;
			props.put(PROP_PROPERTY_PATH, l);
		}
	}
	
	public void setThePath(String[] propPath) {
		if (!props.containsKey(PROP_PROPERTY_PATH)
				&& propPath != null  &&  propPath.length > 0) {
			List l = new ArrayList(propPath.length);
			for (int i=0; i<propPath.length; i++)
				if (PResource.isQualifiedName(propPath[i]))
					l.add(new PResource(propPath[i]));
				else
					return;
			props.put(PROP_PROPERTY_PATH, l);
		}
	}
	
	public PropertyPath toLiteral() {
		if (serializesAsXMLLiteral())
			return this;
		
		PropertyPath result = isAnon()? new PropertyPath(true)
				: new PropertyPath(getURI(), true);
		result.props.put(PROP_PROPERTY_PATH, getProperty(PROP_PROPERTY_PATH));
		return result;
	}
	
	public void unliteral() {
		isXMLLiteral = false;
	}
}
