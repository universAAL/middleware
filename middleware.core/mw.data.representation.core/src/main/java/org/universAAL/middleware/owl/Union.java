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

import org.universAAL.middleware.util.MatchLogEntry;

/**
 * A {@link TypeExpression} that contains all individuals/literals that are in
 * one of the given type expressions. Union corresponds to OWL ObjectUnionOf or
 * DataUnionOf.
 *
 * <p>
 * For example, <code>Union(Enumeration(ex:Peter), Enumeration(ex:Paul))</code>
 * contains the individuals ex:Peter and ex:Paul, and<br>
 * <code>Union(Enumeration("Peter"), Enumeration(1))</code> contains the String
 * "Peter" and the integer one.
 *
 * <p>
 * The given type expressions must not mix sets of individuals with sets of
 * literals. For example,
 * <code>Union(Enumeration(ex:Peter), Enumeration(1))</code> is not possible, as
 * it combines individuals (ex:Peter) with literals (1).
 *
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
// * A union of a set of class expression <i>CE<sub>1</sub> ...
// CE<sub>n</sub></i>
// * contains all individuals that are instances of at least one class
// expression
// * <i>CE<sub>i</sub></i> for 1 &le; i &le; n.
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "This is implemented in Resource based on URI and props.")
public final class Union extends TypeExpression {

	/** URI for owl:unionOf . */
	public static final String PROP_OWL_UNION_OF = OWL_NAMESPACE + "unionOf";

	/** The set of class expressions. */
	private ArrayList<TypeExpression> types;

	/** Constructor. */
	public Union() {
		super();
		types = new ArrayList<TypeExpression>();
		props.put(PROP_OWL_UNION_OF, types);
	}

	/**
	 * Add a new child type expression.
	 *
	 * @param type
	 *            The type expression to add.
	 * @return true (as specified by {@link Collection#add(Object)})
	 */
	public boolean addType(TypeExpression type) {
		if (type != null && !(type instanceof Union)) {
			return types.add(type);
		}
		return false;
	}

	/**
	 * Add a new Child {@link TypeExpression}, same as {@link Union#addType(TypeExpression)}, 
	 * but returns the union it self, for convenient inline definition.
	 * @param type
	 * @return This union.
	 */
	public Union of(TypeExpression type) {
		addType(type);
		return this;
	}
	
	@Override
	public TypeExpression copy() {
		Union result = new Union();
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();)
			result.types.add(i.next().copy());
		return result;
	}

	@Override
	public String[] getNamedSuperclasses() {
		ArrayList l = new ArrayList();
		String[] tmp;
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();) {
			tmp = i.next().getNamedSuperclasses();
			if (tmp != null)
				for (int j = 0; j < tmp.length; j++)
					collectTypesMinimized(tmp[j], l);
		}
		return (String[]) l.toArray(new String[l.size()]);
	}

	@Override
	public Object[] getUpperEnumeration() {
		ArrayList l = new ArrayList();
		Object[] tmp;
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();) {
			tmp = i.next().getUpperEnumeration();
			if (tmp.length == 0)
				return new Object[0];
			for (int j = 0; j < tmp.length; j++)
				if (tmp[j] != null && !l.contains(tmp[j]))
					l.add(tmp[j]);
		}
		return l.toArray();
	}

	@Override
	public boolean hasMember(Object value, HashMap context, int ttl, List<MatchLogEntry> log) {
		ttl = checkTTL(ttl);
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();)
			if (i.next().hasMember(value, context, ttl, log))
				return true;
		return false;
	}

	@Override
	public boolean matches(TypeExpression subtype, HashMap context, int ttl, List<MatchLogEntry> log) {
		ttl = checkTTL(ttl);
		// first handle those cases that can be handled specifically
		if (subtype instanceof Enumeration) {
			((Enumeration) subtype).hasSupertype(this, context, ttl, log);
		}

		if (subtype instanceof Union) {
			HashMap cloned = (context == null) ? null : (HashMap) context.clone();
			for (Iterator i = ((Union) subtype).types(); i.hasNext();)
				if (!matches((TypeExpression) i.next(), cloned, ttl, log))
					return false;
			synchronize(context, cloned);
			return true;
		}

		if (subtype instanceof Intersection) {
			for (Iterator i = ((Intersection) subtype).types(); i.hasNext();)
				if (matches((TypeExpression) i.next(), context, ttl, log))
					return true;
			// TODO: there is still a chance to return true...
			// now fall through to the general cases below to have more chance
			// for correct answer
		}

		Object[] members = (subtype == null) ? null : subtype.getUpperEnumeration();
		if (members != null && members.length > 0) {
			HashMap cloned = (context == null) ? null : (HashMap) context.clone();
			for (int i = 0; i < members.length; i++)
				if (!hasMember(members[i], cloned, ttl, log))
					return false;
			synchronize(context, cloned);
			return true;
		}
		// for all other cases, it's enough if one of the classes in the union
		// is a superclass
		for (Iterator<TypeExpression> i = types.iterator(); i.hasNext();)
			if (i.next().matches(subtype, context, ttl, log))
				return true;
		// TODO: the case, where the whole union is really the supertype
		// of a complement, an intersection, a TypeURI, or a Restriction
		// is still open.
		return false;
	}

	@Override
	public boolean isDisjointWith(TypeExpression other, HashMap context, int ttl, List<MatchLogEntry> log) {
		ttl = checkTTL(ttl);
		HashMap cloned = (context == null) ? null : (HashMap) context.clone();
		for (Iterator i = types(); i.hasNext();)
			if (!((TypeExpression) i.next()).isDisjointWith(other, cloned, ttl, log))
				return false;
		synchronize(context, cloned);
		return true;
	}

	@Override
	public boolean isWellFormed() {
		return types.size() > 1;
	}

	@Override
	public boolean setProperty(String propURI, Object o) {
		if (PROP_OWL_UNION_OF.equals(propURI) && o != null && types.isEmpty()) {
			if (o instanceof List) {
				boolean retVal = false;
				for (Iterator i = ((List) o).iterator(); i.hasNext();) {
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
}
