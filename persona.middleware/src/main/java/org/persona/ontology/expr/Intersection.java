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
package org.persona.ontology.expr;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.persona.ontology.PClassExpression;

/**
 * @author mtazari
 *
 */
public class Intersection extends PClassExpression {
	public static final String PROP_OWL_INTERSECTION_OF;
	static {
		PROP_OWL_INTERSECTION_OF = OWL_NAMESPACE + "intersectionOf";
		register(Intersection.class, null, PROP_OWL_INTERSECTION_OF, null);
	}
	
	private List types;
	
	public Intersection() {
		super();
		types = new ArrayList();
		props.put(PROP_OWL_INTERSECTION_OF, types);
	}
	
	public void addType(PClassExpression type) {
		if (type != null  &&  !(type instanceof Intersection))
			types.add(type);
	}
	
	public PClassExpression copy() {
		Intersection result = new Intersection();
		for (Iterator i=types.iterator(); i.hasNext();)
			result.types.add(((PClassExpression) i.next()) .copy());
		return result;
	}
	
	public String[] getNamedSuperclasses() {
		ArrayList l = new ArrayList();
		String[] tmp;
		for (Iterator i = types.iterator();  i.hasNext(); ) {
			tmp = ((PClassExpression) i.next()).getNamedSuperclasses();
			if (tmp != null)
				for (int j = 0;  j < tmp.length;  j++)
					collectTypesMinimized(tmp[j], l);
		}
		return (String[]) l.toArray(new String[l.size()]);
	}
	
	public Object[] getUpperEnumeration() {
		ArrayList l = new ArrayList();
		Object[] tmp;
		for (Iterator i = types.iterator();  i.hasNext(); ) {
			tmp = ((PClassExpression) i.next()).getUpperEnumeration();
			if (l.isEmpty())
				for (int j = 0;  j < tmp.length;  j++) {
					if (tmp[j] != null)
						l.add(tmp[j]);
				}
			else
				for (Iterator j = l.iterator();  j.hasNext(); ) {
					Object o = j.next();
					boolean found = false;
					for (int k = 0;  !found && k < tmp.length;  k++)
						if (o.equals(tmp[k]))
							found = true;
					if (!found)
						j.remove();
				}
		}
		return l.toArray();
	}

	/**
	 * @see org.persona.ontology.PClassExpression#hasMember(java.lang.Object, java.util.Hashtable)
	 */
	public boolean hasMember(Object value, Hashtable context) {
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		for (Iterator i = types.iterator();  i.hasNext(); ) {
			if (!((PClassExpression) i.next()).hasMember(value, cloned))
				return false;
		}
		synchronize(context, cloned);
		return true;
	}

	public boolean matches(PClassExpression subtype, Hashtable context) {
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		for (Iterator i = types.iterator();  i.hasNext(); ) {
			if (!((PClassExpression) i.next()).matches(subtype, cloned))
				return false;
		}
		synchronize(context, cloned);
		return true;
	}

	public boolean isDisjointWith(PClassExpression other, Hashtable context) {
		for (Iterator i = types.iterator();  i.hasNext(); ) {
			if (((PClassExpression) i.next()).isDisjointWith(other, context))
				return true;
		}
		Object[] members = (other == null)? null : other.getUpperEnumeration();
		if (members != null  &&  members.length > 0) {
			Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
			for (int i=0; i<members.length; i++) {
				if (hasMember(members[i], cloned))
					return false;
			}

			// TODO: if cloned.size() > context.size(),
			//       then under certain conditions it could still work
			return cloned == null  ||  cloned.size() == context.size();
		}
		// TODO: there is still chance to return true
		return false;
	}
	
	public boolean isWellFormed() {
		return types.size() > 1;
	}

	public void setProperty(String propURI, Object o) {
		if (PROP_OWL_INTERSECTION_OF.equals(propURI)
				&& types.isEmpty()
				&& o != null)
			if (o instanceof List)
				for (Iterator i = ((List) o).iterator();  i.hasNext(); ) {
					o = i.next();
					Object tmp = TypeURI.asTypeURI(o);
					if (tmp != null)
						o = tmp;
					if (o instanceof PClassExpression)
						addType((PClassExpression) o);
					else {
						types.clear();
						break;
					}
				}
			else {
				Object tmp = TypeURI.asTypeURI(o);
				if (tmp != null)
					o = tmp;
				if (o instanceof PClassExpression)
					addType((PClassExpression) o);
			}
	}
	
	public Iterator types() {
		return types.iterator();
	}
}
