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

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.MatchLogEntry;

/**
 * An enumeration of the individuals <i>a<sub>1</sub> ... a<sub>n</sub></i>
 * contains exactly the individuals <i>a<sub>i</sub></i> for 1 &le; i &le; n.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public class Enumeration extends TypeExpression {

    /** URI for owl:oneOf. */
    public static final String PROP_OWL_ONE_OF = OWL_NAMESPACE + "oneOf";

    /** URI for owl:DataRange. */
    public static final String TYPE_OWL_DATA_RANGE = OWL_NAMESPACE
	    + "DataRange";

    /** The set of individuals. */
    private ArrayList values = new ArrayList();

    private boolean datarange = false;

    /** Constructor. */
    public Enumeration() {
	super();
	props.put(PROP_OWL_ONE_OF, values);
    }

    /** Constructor with initial values. */
    public Enumeration(Object[] values) {
	super();
	props.put(PROP_OWL_ONE_OF, this.values);
	if (values != null)
	    for (int i = 0; i < values.length; i++)
		if (values[i] != null) {
		    if (values[i] instanceof String
			    && isQualifiedName((String) values[i]))
			values[i] = new Resource((String) values[i]);
		    if (!datarange && !(values[i] instanceof Resource)) {
			props.put(PROP_RDF_TYPE, new Resource(
				TYPE_OWL_DATA_RANGE));
			datarange = true;
		    }
		    this.values.add(values[i]);
		}
    }

    /** Add a new individual. */
    public boolean addValue(Object o) {
	if (o != null) {
	    if (o instanceof List) {
		for (Object el : (List) o)
		    addValue(el);
	    } else {
		if (!datarange && !(o instanceof Resource)) {
		    props.put(PROP_RDF_TYPE, new Resource(TYPE_OWL_DATA_RANGE));
		    datarange = true;
		}
		values.add(o);
		return true;
	    }
	}
	return false;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	Enumeration result = new Enumeration();
	for (Iterator i = values.iterator(); i.hasNext();)
	    result.values.add(i.next());
	if (datarange) {
	    result.datarange = true;
	    result.props.put(PROP_RDF_TYPE, new Resource(TYPE_OWL_DATA_RANGE));
	}
	return result;
    }

    /**
     * Get the maximum value from all individuals. The individuals have to
     * implement the {@link java.lang.Comparable} interface.
     * 
     * @return The maximum value, or null if an individual does not implement
     *         the {@link java.lang.Comparable} interface.
     */
    public Comparable getMaxValue() {
	try {
	    Comparable result = null;
	    for (Iterator i = values.iterator(); i.hasNext();) {
		Comparable o = (Comparable) i.next();
		if (result == null || result.compareTo(o) < 0)
		    result = o;
	    }
	    return result;
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Get the minimum value from all individuals. The individuals have to
     * implement the {@link java.lang.Comparable} interface.
     * 
     * @return The minimum value, or null if an individual does not implement
     *         the {@link java.lang.Comparable} interface.
     */
    public Comparable getMinValue() {
	try {
	    Comparable result = null;
	    for (Iterator i = values.iterator(); i.hasNext();) {
		Comparable o = (Comparable) i.next();
		if (result == null || result.compareTo(o) > 0)
		    result = o;
	    }
	    return result;
	} catch (Exception e) {
	    return null;
	}
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getNamedSuperclasses() */
    public String[] getNamedSuperclasses() {
	ArrayList l = new ArrayList();
	for (Iterator i = values.iterator(); i.hasNext();)
	    collectTypesMinimized(ManagedIndividual.getTypeURI(i.next()), l);
	return (String[]) l.toArray(new String[l.size()]);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getUpperEnumeration() */
    public Object[] getUpperEnumeration() {
	Object[] answer = new Object[values.size()];
	for (int i = 0; i < values.size(); i++)
	    answer[i] = values.get(i);
	return answer;
    }

    public boolean hasMember(Object value, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	// ttl =
	checkTTL(ttl);
	if (value == null || values.contains(value))
	    return true;
	// TODO: what if variables had to be replaced using context
	return false;
    }

    public boolean matches(TypeExpression subtype, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	ttl = checkTTL(ttl);
	if (subtype == null)
	    return false;

	if (subtype instanceof Enumeration)
	    // TODO: what if variables had to be replaced using context
	    return values.containsAll(((Enumeration) subtype).values);

	if (subtype instanceof Union) {
	    for (Iterator i = ((Union) subtype).types(); i.hasNext();) {
		if (!matches((TypeExpression) i.next(), context, ttl, log))
		    return false;
	    }
	    return true;
	} else if (subtype instanceof Intersection) {
	    for (Iterator i = ((Intersection) subtype).types(); i.hasNext();) {
		if (matches((TypeExpression) i.next(), context, ttl, log))
		    return true;
	    }
	    // TODO: there is still a chance to return true...
	}

	// a last try
	Object[] upperEnum = subtype.getUpperEnumeration();
	if (upperEnum != null && upperEnum.length > 0) {
	    // TODO: what if variables had to be replaced using context
	    for (int i = 0; i < upperEnum.length; i++)
		if (!values.contains(upperEnum[i]))
		    return false;
	    return true;
	}
	// TODO: for complement and restriction difficult to decide but also
	// very unlikely
	return false;
    }

    /**
     * Determines if all individuals of this Enumeration are members of the
     * given <code>supertype</code>.
     */
    public boolean hasSupertype(TypeExpression supertype, HashMap context,
	    int ttl, List<MatchLogEntry> log) {
	if (supertype == null)
	    return false;

	HashMap cloned = (context == null) ? null : (HashMap) context.clone();
	for (Iterator i = values.iterator(); i.hasNext();)
	    if (!supertype.hasMember(i.next(), cloned, ttl, log))
		return false;

	// TODO: if cloned.size() != context.size(),
	// then under certain conditions it could still work
	return cloned == null || cloned.size() == context.size();
    }

    public boolean isDisjointWith(TypeExpression other, HashMap context,
	    int ttl, List<MatchLogEntry> log) {
	ttl = checkTTL(ttl);
	if (other == null)
	    return false;

	HashMap cloned = (context == null) ? null : (HashMap) context.clone();
	for (Iterator i = values.iterator(); i.hasNext();) {
	    if (other.hasMember(i.next(), cloned, ttl, log))
		// TODO: if cloned.size() != context.size(),
		// then under certain conditions it could still work
		return false;
	}

	return true;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#isWellFormed() */
    public boolean isWellFormed() {
	return !values.isEmpty();
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public boolean setProperty(String propURI, Object o) {
	if (PROP_OWL_ONE_OF.equals(propURI) && values.isEmpty() && o != null) {
	    if (o instanceof List) {
		boolean retVal = false;
		for (Iterator i = ((List) o).iterator(); i.hasNext();) {
		    retVal = addValue(i.next()) || retVal;
		}
		return retVal;
	    } else {
		return addValue(o);
	    }
	}
	return false;
    }

    /** Get an iterator for the individuals. */
    public Iterator values() {
	return values.iterator();
    }

    /** Returns the number of elements in this enumeration. */
    public int size() {
	return values.size();
    }
}
