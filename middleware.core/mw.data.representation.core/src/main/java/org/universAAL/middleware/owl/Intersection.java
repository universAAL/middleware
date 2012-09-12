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

import org.universAAL.middleware.rdf.UnmodifiableResource;
import org.universAAL.middleware.rdf.UnmodifiableResourceList;

/**
 * An intersection of a set of class expressions <i>CE<sub>1</sub> ...
 * CE<sub>n</sub></i> contains all individuals that are instances of all class
 * expressions <i>CE<sub>i</sub></i> for 1 &le; i &le; n.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public class Intersection extends TypeExpression {

    /** URI for owl:intersectionOf. */
    public static final String PROP_OWL_INTERSECTION_OF;

    static {
	PROP_OWL_INTERSECTION_OF = OWL_NAMESPACE + "intersectionOf";
	register(Intersection.class, null, PROP_OWL_INTERSECTION_OF, null);
    }

    /** The list of child class expressions. */
    protected List types;

    /** Constructor. */
    public Intersection() {
	super();
	types = new ArrayList();
	props.put(PROP_OWL_INTERSECTION_OF, types);
    }

    /**
     * Add a new child class expression <i>CE<sub>i</sub></i>. If the given
     * argument is an instance of Intersection, then the elements of that
     * Intersection are added instead of the Intersection itself.
     * 
     * @param type
     *            The class expression to add.
     */
    public void addType(TypeExpression type) {
	if (type != null) {
	    if (type instanceof Intersection) {
		for (int i = 0; i < ((Intersection) type).types.size(); i++)
		    types.add(((Intersection) type).types.get(i));
	    } else {
		types.add(type);
	    }
	}
    }

    // TODO: remove when increasing version
    public void addType(Intersection type) {
	addType((TypeExpression) type);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	Intersection result = new Intersection();
	for (Iterator i = types.iterator(); i.hasNext();)
	    result.types.add(((TypeExpression) i.next()).copy());
	return result;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getNamedSuperclasses() */
    public String[] getNamedSuperclasses() {
	ArrayList l = new ArrayList();
	String[] tmp;
	for (Iterator i = types.iterator(); i.hasNext();) {
	    tmp = ((TypeExpression) i.next()).getNamedSuperclasses();
	    if (tmp != null)
		for (int j = 0; j < tmp.length; j++)
		    collectTypesMinimized(tmp[j], l);
	}
	return (String[]) l.toArray(new String[l.size()]);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getUpperEnumeration() */
    public Object[] getUpperEnumeration() {
	ArrayList l = new ArrayList();
	Object[] tmp;
	for (Iterator i = types.iterator(); i.hasNext();) {
	    tmp = ((TypeExpression) i.next()).getUpperEnumeration();
	    if (l.isEmpty())
		for (int j = 0; j < tmp.length; j++) {
		    if (tmp[j] != null)
			l.add(tmp[j]);
		}
	    else
		for (Iterator j = l.iterator(); j.hasNext();) {
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

    /**
     * @see org.universAAL.middleware.owl.TypeExpression#hasMember(Object,
     *      Hashtable)
     */
    public boolean hasMember(Object value, Hashtable context) {
	Hashtable cloned = (context == null) ? null : (Hashtable) context
		.clone();
	for (Iterator i = types.iterator(); i.hasNext();) {
	    if (!((TypeExpression) i.next()).hasMember(value, cloned))
		return false;
	}
	synchronize(context, cloned);
	return true;
    }

    /**
     * @see org.universAAL.middleware.owl.TypeExpression#matches(TypeExpression,
     *      Hashtable)
     */
    public boolean matches(TypeExpression subtype, Hashtable context) {
	Hashtable cloned = (context == null) ? null : (Hashtable) context
		.clone();
	for (Iterator i = types.iterator(); i.hasNext();) {
	    if (!((TypeExpression) i.next()).matches(subtype, cloned))
		return false;
	}
	synchronize(context, cloned);
	return true;
    }

    /**
     * @see org.universAAL.middleware.owl.TypeExpression#isDisjointWith(TypeExpression,
     *      Hashtable)
     */
    public boolean isDisjointWith(TypeExpression other, Hashtable context) {
	for (Iterator i = types.iterator(); i.hasNext();) {
	    if (((TypeExpression) i.next()).isDisjointWith(other, context))
		return true;
	}
	Object[] members = (other == null) ? null : other.getUpperEnumeration();
	if (members != null && members.length > 0) {
	    Hashtable cloned = (context == null) ? null : (Hashtable) context
		    .clone();
	    for (int i = 0; i < members.length; i++) {
		if (hasMember(members[i], cloned))
		    return false;
	    }

	    // TODO: if cloned.size() > context.size(),
	    // then under certain conditions it could still work
	    return cloned == null || cloned.size() == context.size();
	}
	// TODO: there is still chance to return true
	return false;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#isWellFormed() */
    public boolean isWellFormed() {
	return types.size() > 1;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object o) {
	if (PROP_OWL_INTERSECTION_OF.equals(propURI) && types.isEmpty()
		&& o != null)
	    if (o instanceof List)
		for (Iterator i = ((List) o).iterator(); i.hasNext();) {
		    o = i.next();
		    Object tmp = TypeURI.asTypeURI(o);
		    if (tmp != null)
			o = tmp;
		    if (o instanceof TypeExpression)
			addType((TypeExpression) o);
		    else {
			types.clear();
			break;
		    }
		}
	    else {
		Object tmp = TypeURI.asTypeURI(o);
		if (tmp != null)
		    o = tmp;
		if (o instanceof TypeExpression)
		    addType((TypeExpression) o);
	    }
    }

    /** Get an iterator for the added child class expressions. */
    public Iterator types() {
	return types.iterator();
    }

    /** Get an unmodifiable list of the added child class expressions. */
    public List elements() {
	return new UnmodifiableResourceList(types);
    }

    /** Returns the number of elements in this intersection. */
    public int size() {
	return types.size();
    }
}
