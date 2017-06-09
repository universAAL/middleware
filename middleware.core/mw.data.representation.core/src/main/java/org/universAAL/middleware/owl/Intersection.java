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
import java.util.Iterator;
import java.util.List;
import org.universAAL.middleware.rdf.UnmodifiableResourceList;
import org.universAAL.middleware.util.MatchLogEntry;

/**
 * A {@link TypeExpression} that contains all individuals/literals that are in
 * all of the given type expressions. Intersection corresponds to OWL
 * ObjectIntersectionOf or DataIntersectionOf.
 *
 * <p>
 * For example,
 * <code>Intersection(Enumeration(ex:Peter, a:Lois), Enumeration(ex:Paul, a:Lois))</code>
 * contains the individual a:Lois, and<br>
 * <code>Intersection(Enumeration(1,2), Enumeration(2,3))</code> contains the
 * integer two.
 *
 * <p>
 * The given type expressions must not mix sets of individuals with sets of
 * literals. For example,
 * <code>Intersection(Enumeration(ex:Peter), Enumeration(1))</code> is not
 * possible, as it combines individuals (ex:Peter) with literals (1).
 *
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
// * An intersection of a set of class expressions <i>CE<sub>1</sub> ...
// * CE<sub>n</sub></i> contains all individuals that are instances of all class
// * expressions <i>CE<sub>i</sub></i> for 1 &le; i &le; n.
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "This is implemented in Resource based on URI and props.")
public class Intersection extends TypeExpression {

	/** URI for owl:intersectionOf. */
	public static final String PROP_OWL_INTERSECTION_OF = OWL_NAMESPACE + "intersectionOf";

	/** The list of child type expressions. */
	protected List<TypeExpression> types;

	/** Constructor. */
	public Intersection() {
		super();
		types = new ArrayList<TypeExpression>();
		props.put(PROP_OWL_INTERSECTION_OF, types);
	}

	/**
	 * Constructor to create a new instance with some additional URIs to set as
	 * rdf:type.
	 *
	 * @param additionalTypes
	 *            The additional type URIs to set as rdf:type.
	 */
	protected Intersection(String[] additionalTypes) {
		super(additionalTypes);
		types = new ArrayList<TypeExpression>();
		props.put(PROP_OWL_INTERSECTION_OF, types);
	}

	/**
	 * Add a new child type expression. If the given argument is an instance of
	 * Intersection, then the elements of that Intersection are added instead of
	 * the Intersection itself.
	 *
	 * @param type
	 *            The type expression to add.
	 *
	 * @return true, if the given type expression could be added.
	 */
	public boolean addType(TypeExpression type) {
		if (type == null)
			return false;

		if (type instanceof Intersection) {
			for (int i = 0; i < ((Intersection) type).types.size(); i++) {
				types.add(((Intersection) type).types.get(i));
			}
		} else {
			types.add(type);
		}
		return true;
	}

	@Override
	public TypeExpression copy() {
		Intersection result = new Intersection();
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();)
			result.types.add(i.next().copy());
		return result;
	}

	@Override
	public String[] getNamedSuperclasses() {
		ArrayList<String> l = new ArrayList<String>();
		String[] tmp;
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();) {
			tmp = i.next().getNamedSuperclasses();
			if (tmp != null)
				for (int j = 0; j < tmp.length; j++)
					collectTypesMinimized(tmp[j], l);
		}
		return l.toArray(new String[l.size()]);
	}

	@Override
	public Object[] getUpperEnumeration() {
		ArrayList<Object> l = new ArrayList<Object>();
		Object[] tmp;
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();) {
			tmp = i.next().getUpperEnumeration();
			if (l.isEmpty())
				for (int j = 0; j < tmp.length; j++) {
					if (tmp[j] != null)
						l.add(tmp[j]);
				}
			else
				for (Iterator<Object> j = l.iterator(); j.hasNext();) {
					Object o = j.next();
					boolean found = false;
					for (int k = 0; !found && k < tmp.length; k++)
						if (o.equals(tmp[k]))
							found = true;
					if (!found)
						j.remove();
				}
		}
		return l.toArray();
	}

	@Override
	public boolean hasMember(Object value, HashMap context, int ttl, List<MatchLogEntry> log) {
		ttl = checkTTL(ttl);
		HashMap cloned = (context == null) ? null : (HashMap) context.clone();
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();) {
			if (!i.next().hasMember(value, cloned, ttl, log))
				return false;
		}
		synchronize(context, cloned);
		return true;
	}

	@Override
	public boolean matches(TypeExpression subtype, HashMap context, int ttl, List<MatchLogEntry> log) {
		ttl = checkTTL(ttl);
		HashMap cloned = (context == null) ? null : (HashMap) context.clone();
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();) {
			if (!i.next().matches(subtype, cloned, ttl, log))
				return false;
		}
		synchronize(context, cloned);
		return true;
	}

	@Override
	public boolean isDisjointWith(TypeExpression other, HashMap context, int ttl, List<MatchLogEntry> log) {
		ttl = checkTTL(ttl);
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();) {
			if (i.next().isDisjointWith(other, context, ttl, log))
				return true;
		}
		Object[] members = (other == null) ? null : other.getUpperEnumeration();
		if (members != null && members.length > 0) {
			HashMap cloned = (context == null) ? null : (HashMap) context.clone();
			for (int i = 0; i < members.length; i++) {
				if (hasMember(members[i], cloned, ttl, log))
					return false;
			}

			// TODO: if cloned.size() > context.size(),
			// then under certain conditions it could still work
			return cloned == null || cloned.size() == context.size();
		}
		// TODO: there is still chance to return true
		return false;
	}

	@Override
	public boolean isWellFormed() {
		return types.size() > 1;
	}

	@Override
	public boolean setProperty(String propURI, Object o) {
		if (PROP_OWL_INTERSECTION_OF.equals(propURI) && types.isEmpty() && o != null) {
			if (o instanceof List) {
				boolean retVal = false;
				for (Iterator i = ((List) o).iterator(); i.hasNext();) {
					o = i.next();
					Object tmp = TypeURI.asTypeURI(o);
					if (tmp != null)
						o = tmp;
					if (o instanceof TypeExpression)
						retVal = addType((TypeExpression) o) || retVal;
					else {
						types.clear();
						break;
					}
				}
				return retVal;
			} else {
				Object tmp = TypeURI.asTypeURI(o);
				if (tmp != null)
					o = tmp;
				if (o instanceof TypeExpression)
					return addType((TypeExpression) o);
			}
		}
		return false;
	}

	/**
	 * Get an iterator for the child type expressions.
	 *
	 * @return an iterator for the child type expressions.
	 */
	public Iterator<TypeExpression> types() {
		return types.iterator();
	}

	/**
	 * Get an unmodifiable list of the child type expressions.
	 *
	 * @return an unmodifiable list of the child type expressions.
	 */
	public UnmodifiableResourceList elements() {
		return new UnmodifiableResourceList(types);
	}

	/**
	 * Returns the number of elements in this intersection.
	 *
	 * @return the number of elements in this intersection.
	 */
	public int size() {
		return types.size();
	}
}
