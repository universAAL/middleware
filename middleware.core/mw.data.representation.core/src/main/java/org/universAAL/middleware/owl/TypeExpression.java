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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.MatchLogEntry;

/**
 * A class for the concept of OWL class expressions and OWL data ranges, which
 * represent sets of individuals by formally specifying conditions on the
 * individuals' properties. Example conditions are intersection of individuals,
 * or restrictions. Each type expression represents a set of individuals (just
 * like a class in an object-oriented programming language represents a set of
 * objects). There are three main methods to compare type expression:
 * <ul>
 * <li>
 * hasMember: determines if the given object is a member of the class
 * represented by this type expression</li>
 * <li>
 * matches: determines if the given type expression is a subset of the class
 * represented by this type expression</li>
 * <li>
 * isDisjointWith: determines if the given type expression has no member in
 * common with the class represented by this type expression</li>
 * </ul>
 * 
 * All three methods have three parameters in common:
 * <ul>
 * <li>
 * context: context of the matchmaking. The <code>context</code> table maps the
 * URIs of certain variables onto values currently assigned to them. The
 * variables are either standard variables managed by the universAAL middleware
 * or parameters of a specific service in whose context this method is called.
 * If there is already a value assigned to such a referenced variable, it must
 * be replaced by the associated value, otherwise this method may expand the
 * <code>context</code> table by deriving a value for such unassigned but
 * referenced variables with which the goal of the method can be asserted. In
 * case of returning true, the caller must check the size of the
 * <code>context</code> table to see if new conditions are added in. If the
 * <code>context</code> map is null, the method does a global and unconditional
 * check.</li>
 * <li>
 * ttl: time-to-live parameter. For each subsequent call to one of these methods
 * the ttl parameter is decreased by one. If it reaches zero, an
 * {@link IllegalArgumentException} is thrown with the text provided in
 * {@link #EXCEPTION_TTL}. This can be used to avoid cycles in the type
 * expression.</li>
 * <li>
 * log: list that can be filled with log entries and that shows at which point
 * the matchmaking failed. May be null.</li>
 * </ul>
 * 
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public abstract class TypeExpression extends Resource {

    /** URI namespace for OWL. */
    public static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";

    /** URI for owl:Thing. */
    public static final String TYPE_OWL_THING = OWL_NAMESPACE + "Thing";

    /** URI for rdfs:Datatype. */
    public static final String RDFS_DATATYPE = Resource.RDFS_NAMESPACE
	    + "Datatype";

    /** URI for owl:class. */
    public static final String OWL_CLASS = OWL_NAMESPACE + "Class";

    /** URI for rdfs:subClassOf. */
    public static final String PROP_RDFS_SUB_CLASS_OF = Resource.RDFS_NAMESPACE
	    + "subClassOf";

    private static final int TTL = 100;

    public static final String EXCEPTION_TTL = "TTL exceeded. Maybe the TypeExpression has a cycle!";

    /** Constructor for use with serializers. */
    protected TypeExpression() {
	super();
	addType(OWL_CLASS, true);
    }

    /** Constructor to create a new instance with the given URI. */
    protected TypeExpression(String uri) {
	super(uri);
	addType(OWL_CLASS, true);
    }

    /** Constructor. */
    protected TypeExpression(String[] additionalTypes) {
	super();
	addType(OWL_CLASS, false);
	for (int i = 0; i < additionalTypes.length - 1; i++)
	    addType(additionalTypes[i], false);
	if (additionalTypes.length - 1 >= 0)
	    addType(additionalTypes[additionalTypes.length - 1], true);
    }

    /**
     * Provided that the given list is already a minimized list of type URIs,
     * i.e. no two members have any hierarchical relationships with each other,
     * adds the given typeURI to the list so that the above condition continues
     * to hold and no information is lost. In case of a hierarchical
     * relationship, the given typeURI is added if it is more specialized, i.e.
     * a sub class, than any existing type (the less spcialized, i.e. a super
     * class, is then removed from the list).
     */
    protected void collectTypesMinimized(String typeURI, List l) {
	if (typeURI != null) {
	    boolean toAdd = true;
	    for (Iterator j = l.iterator(); j.hasNext();) {
		String uri = (String) j.next();
		if (ManagedIndividual.checkCompatibility(uri, typeURI)) {
		    j.remove();
		    break;
		} else if (ManagedIndividual.checkCompatibility(typeURI, uri)) {
		    toAdd = false;
		    break;
		}
	    }
	    if (toAdd)
		l.add(typeURI);
	}
    }

    /**
     * Create a copy of this object, i.e. create a new object of this class and
     * copy the necessary properties.
     * 
     * @return The newly created copy.
     */
    public abstract TypeExpression copy();

    /**
     * Get the set of class URIs for all super classes of the individuals of
     * this class expression.
     */
    public abstract String[] getNamedSuperclasses();

    /**
     * Each type expression can contain multiple objects; this method returns
     * this set of objects.
     */
    public abstract Object[] getUpperEnumeration();

    /**
     * Returns <tt>true</tt> if the given object is a member of the class
     * represented by this type expression, otherwise false.
     * 
     * @param member
     *            the object whose membership is going to be checked
     * @return <tt>true</tt> if the specified member is a member of this type
     *         expression
     */
    public final boolean hasMember(Object member) {
	return hasMember(member, null, TTL, null);
    }

    /**
     * @deprecated replaced by {@link #hasMember(Object, HashMap, int, List)}.
     */
    public final boolean hasMember(Object member, Hashtable context) {
	HashMap map = context == null ? null : new HashMap(context);
	boolean res = hasMember(member, map, TTL, null);
	if (map != null) {
	    context.clear();
	    context.putAll(map);
	}
	return res;
    }

    /**
     * Returns <tt>true</tt> if the given object is a member of the class
     * represented by this type expression, otherwise false. The
     * <code>context</code> table maps the URIs of certain variables onto values
     * currently assigned to them. The variables are either standard variables
     * managed by the universAAL middleware or parameters of a specific service
     * in whose context this method is called. Both the object whose membership
     * is going to be checked and this type expression may contain references to
     * such variables. If there is already a value assigned to such a referenced
     * variable, it must be replaced by the associated value, otherwise this
     * method may expand the <code>context</code> table by deriving a value for
     * such unassigned but referenced variables with which the membership can be
     * asserted. In case of returning true, the caller must check the size of
     * the <code>context</code> table to see if new conditions are added in
     * order for the membership to be asserted. If the <code>context</code>
     * table is null, the method does a global and unconditional check.
     * 
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_BUS_MEMBER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_HUMAN_USER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_CURRENT_DATETIME
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_SERVICE_TO_SELECT
     * 
     * @param member
     *            the object whose membership is going to be checked
     * @param context
     *            context of the matchmaking. May be null.
     * @param ttl
     *            time-to-live parameter
     * @param log
     *            list that can be filled with log entries. May be null.
     * @return <tt>true</tt> if the specified member is a member of this type
     *         expression
     */
    public abstract boolean hasMember(Object member, HashMap context, int ttl,
	    List<MatchLogEntry> log);

    /**
     * Returns <tt>true</tt> if the given type expression has no member in
     * common with the class represented by this type expression, otherwise
     * false.
     * 
     * @param other
     *            the type expression with which the disjointness is going to be
     *            checked
     * @return <tt>true</tt> if the given type expression has no member in
     *         common with the class represented by this type expression,
     *         otherwise false
     */
    public final boolean isDisjointWith(TypeExpression other) {
	return isDisjointWith(other, null, TTL, null);
    }

    /**
     * @deprecated replaced by
     *             {@link #isDisjointWith(Object, HashMap, int, List)}.
     */
    public final boolean isDisjointWith(TypeExpression other, Hashtable context) {
	HashMap map = context == null ? null : new HashMap(context);
	boolean res = isDisjointWith(other, map, TTL, null);
	if (map != null) {
	    context.clear();
	    context.putAll(map);
	}
	return res;
    }

    /**
     * Returns <tt>true</tt> if the given type expression has no member in
     * common with the class represented by this type expression, otherwise
     * false. The <code>context</code> table maps the URIs of certain variables
     * onto values currently assigned to them. The variables are either standard
     * variables managed by the universAAL middleware or parameters of a
     * specific service in whose context this method is called. Both of the type
     * expressions may contain references to such variables. If there is already
     * a value assigned to such a referenced variable, it must be replaced by
     * the associated value, otherwise this method may expand the
     * <code>context</code> table by deriving a value for such unassigned but
     * referenced variables with which the disjointness of the two classes can
     * be asserted. In case of returning true, the caller must check the size of
     * the <code>context</code> table to see if new conditions are added in
     * order for the disjointness to be asserted. If the <code>context</code>
     * table is null, the method does a global and unconditional check.
     * 
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_BUS_MEMBER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_HUMAN_USER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_CURRENT_DATETIME
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_SERVICE_TO_SELECT
     * 
     * @param other
     *            the type expression with which the disjointness is going to be
     *            checked
     * @param context
     *            context of the matchmaking. May be null.
     * @param ttl
     *            time-to-live parameter
     * @param log
     *            list that can be filled with log entries. May be null.
     * @return <tt>true</tt> if the given type expression has no member in
     *         common with the class represented by this type expression,
     *         otherwise false
     */
    public abstract boolean isDisjointWith(TypeExpression other,
	    HashMap context, int ttl, List<MatchLogEntry> log);

    /**
     * Returns <tt>true</tt>, if the state of the resource is valid, otherwise
     * false. Redefined in this class as abstract to force subclasses to
     * override it.
     * 
     * @see org.universAAL.middleware.rdf.Resource#isWellFormed()
     */
    public abstract boolean isWellFormed();

    /**
     * Returns <tt>true</tt> if the given type expression is a subset of the
     * class represented by this type expression, otherwise false.
     * 
     * @param subset
     *            the type expression with which the compatibility is going to
     *            be checked
     * @return <tt>true</tt> if the given type expression is a subset of the
     *         class represented by this type expression, otherwise false
     */
    public final boolean matches(TypeExpression subset) {
	return matches(subset, null, TTL, null);
    }

    /**
     * @deprecated replaced by {@link #matches(Object, HashMap, int, List)}.
     */
    public final boolean matches(TypeExpression subset, Hashtable context) {
	HashMap map = context == null ? null : new HashMap(context);
	boolean res = matches(subset, map, TTL, null);
	if (map != null) {
	    context.clear();
	    context.putAll(map);
	}
	return res;
    }

    /**
     * Returns true if the given type expression is a subset of the class
     * represented by this type expression, otherwise false. The
     * <code>context</code> table maps the URIs of certain variables onto values
     * currently assigned to them. The variables are either standard variables
     * managed by the universAAL middleware or parameters of a specific service
     * in whose context this method is called. Both of the type expressions may
     * contain references to such variables. If there is already a value
     * assigned to such a referenced variable, it must be replaced by the
     * associated value, otherwise this method may expand the
     * <code>context</code> table by deriving a value for such unassigned but
     * referenced variables with which the compatibility of the two classes can
     * be asserted. In case of returning true, the caller must check the size of
     * the <code>context</code> table to see if new conditions are added in
     * order for the compatibility to be asserted. If the <code>context</code>
     * table is null, the method does a global and unconditional check.
     * 
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_BUS_MEMBER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_HUMAN_USER
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_CURRENT_DATETIME
     * @see org.universAAL.middleware.util.Constants#VAR_uAAL_SERVICE_TO_SELECT
     * @param subset
     *            the type expression with which the compatibility is going to
     *            be checked
     * @param context
     *            context of the matchmaking. May be null.
     * @param ttl
     *            time-to-live parameter
     * @param log
     *            list that can be filled with log entries. May be null.
     * @return <tt>true</tt> if the given type expression is a subset of the
     *         class represented by this type expression, otherwise false
     */
    public abstract boolean matches(TypeExpression subset, HashMap context,
	    int ttl, List<MatchLogEntry> log);

    /**
     * Synchronize two HashMap to ensure that the first HashMap contains a value
     * for each key of the second HashMap. The values themselves are not
     * checked; if 'cloned' contains a key which is not contained in 'context',
     * then the according (key/value)-pair is added to 'context'. The second
     * HashMap, 'cloned', is not changed.
     * 
     * @param context
     *            The HashMap to be extended by (key/value)-pairs from the
     *            second HashMap.
     * @param cloned
     *            The second HashMap.
     */
    protected void synchronize(HashMap context, HashMap cloned) {
	if (cloned != null && cloned.size() > context.size()) {
	    for (Iterator i = cloned.keySet().iterator(); i.hasNext();) {
		Object key = i.next();
		if (!context.containsKey(key))
		    context.put(key, cloned.get(key));
	    }
	}
    }

    /**
     * Check the time-to-live parameter. If this value is zero, the method will
     * throw an {@link IllegalArgumentException}, otherwise the parameter will
     * be decreased by one and returned.
     * 
     * @throws IllegalArgumentException
     *             if the specified value is zero.
     * @param ttl
     *            the time-to-live value
     * @return the time-to-live value decreased by one
     */
    protected int checkTTL(int ttl) {
	if (ttl < 0)
	    ttl = getDefaultMatchmakingTTL();
	ttl--;
	if (ttl < 0)
	    throw new IllegalArgumentException(EXCEPTION_TTL);
	return ttl;
    }

    /**
     * Returns the default value for time-to-live.
     * 
     * @return the default value for time-to-live
     */
    public final static int getDefaultMatchmakingTTL() {
	return TTL;
    }
}
