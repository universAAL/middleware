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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * A union of a set of class expression <i>CE<sub>1</sub> ...
 * CE<sub>n</sub></i> contains all individuals that are instances of at least
 * one class expression <i>CE<sub>i</sub></i> for 1 &le; i &le; n.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * @author Carsten Stockloew
 */
public class Union extends ClassExpression {
	
	/** URI for owl:unionOf . */
	public static final String PROP_OWL_UNION_OF;
	
	static {
		PROP_OWL_UNION_OF = OWL_NAMESPACE + "unionOf";
		register(Union.class, null, PROP_OWL_UNION_OF, null);
	}
	
	/** The set of class expressions. */
	private ArrayList types;
	
	
	/** Constructor. */
	public Union() {
		super();
		types = new ArrayList();
		props.put(PROP_OWL_UNION_OF, types);
	}
	
	
	/**
	 * Add a new child class expression <i>CE<sub>i</sub></i>.
	 * @param type The class expression to add.
	 */
	public void addType(ClassExpression type) {
		if (type != null  &&  !(type instanceof Union))
			types.add(type);
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#copy() */
	public ClassExpression copy() {
		Union result = new Union();
		for (Iterator i=types.iterator(); i.hasNext();)
			result.types.add(((ClassExpression) i.next()) .copy());
		return result;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#getNamedSuperclasses() */
	public String[] getNamedSuperclasses() {
		ArrayList l = new ArrayList();
		String[] tmp;
		for (Iterator i = types.iterator();  i.hasNext(); ) {
			tmp = ((ClassExpression) i.next()).getNamedSuperclasses();
			if (tmp != null)
				for (int j = 0;  j < tmp.length;  j++)
					collectTypesMinimized(tmp[j], l);
		}
		return (String[]) l.toArray(new String[l.size()]);
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#getUpperEnumeration() */
	public Object[] getUpperEnumeration() {
		ArrayList l = new ArrayList();
		Object[] tmp;
		for (Iterator i = types.iterator();  i.hasNext(); ) {
			tmp = ((ClassExpression) i.next()).getUpperEnumeration();
			if (tmp.length == 0)
				return new Object[0];
			for (int j = 0;  j < tmp.length;  j++)
				if (tmp[j] != null  &&  !l.contains(tmp[j]))
					l.add(tmp[j]);
		}
		return l.toArray();
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#hasMember(Object, Hashtable) */
	public boolean hasMember(Object value, Hashtable context) {
		for (Iterator i = types.iterator();  i.hasNext(); )
			if (((ClassExpression) i.next()).hasMember(value, context))
				return true;
		return false;
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression, Hashtable) */
	public boolean matches(ClassExpression subtype, Hashtable context) {
		// first handle those cases that can be handled specifically
		if (subtype instanceof Enumeration) {
			((Enumeration) subtype).hasSupertype(this, context);
		}
		
		if (subtype instanceof Union) {
			Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
			for (Iterator i = ((Union) subtype).types();  i.hasNext(); )
				if (!matches((ClassExpression) i.next(), cloned))
						return false;
			synchronize(context, cloned);
			return true;
		}
		
		if (subtype instanceof Intersection) {
			for (Iterator i = ((Intersection) subtype).types(); i.hasNext();)
				if (matches((ClassExpression) i.next(), context))
					return true;
			// TODO: there is still a chance to return true...
			// now fall through to the general cases below to have more chance for correct answer
		}
		
		Object[] members = (subtype == null)? null : subtype.getUpperEnumeration();
		if (members != null  &&  members.length > 0) {
			Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
			for (int i=0; i<members.length; i++)
				if (!hasMember(members[i], cloned))
					return false;
			synchronize(context, cloned);
			return true;
		}
		// for all other cases, it's enough if one of the classes in the union is a superclass
		for (Iterator i = types.iterator();  i.hasNext(); )
			if (((ClassExpression) i.next()).matches(subtype, context))
				return true;
		// TODO: the case, where the whole union is really the supertype
		//       of a complement, an intersection, a TypeURI, or a Restriction
		//       is still open.
		return false;
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression, Hashtable) */
	public boolean isDisjointWith(ClassExpression other, Hashtable context) {
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		for (Iterator i = types();  i.hasNext(); )
			if (!((ClassExpression) i.next()).isDisjointWith(other, cloned))
					return false;
		synchronize(context, cloned);
		return true;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
	public boolean isWellFormed() {
		return types.size() > 1;
	}

	/** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
	public void setProperty(String propURI, Object o) {
		if (PROP_OWL_UNION_OF.equals(propURI)
				&& o != null
				&& types.isEmpty())
			if (o instanceof List)
				for (Iterator i = ((List) o).iterator();  i.hasNext(); ) {
					Object tmp = TypeURI.asTypeURI(o);
					if (tmp != null)
						o = tmp;
					if (o instanceof ClassExpression)
						addType((ClassExpression) o);
					else {
						types.clear();
						break;
					}
				}
			else {
				Object tmp = TypeURI.asTypeURI(o);
				if (tmp != null)
					o = tmp;
				if (o instanceof ClassExpression)
					addType((ClassExpression) o);
			}
	}
	
	/** Get an iterator for the added child class expressions. */
	public Iterator types() {
		return types.iterator();
	}
}
