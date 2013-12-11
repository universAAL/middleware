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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.Variable;
import org.universAAL.middleware.util.MatchLogEntry;

/**
 * Represents the URI of the <i>type</i> of an ontology class.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public class TypeURI extends TypeExpression {

    public TypeURI(String uri, boolean isDatatypeURI) {
	super(uri);
	if (isDatatypeURI)
	    props.remove(PROP_RDF_TYPE);
    }

    /**
     * Creates a new TypeURI instance according to the given object.
     */
    public static TypeURI asTypeURI(Object o) {
	if (o == null || o instanceof TypeURI)
	    return (TypeURI) o;

	if (o instanceof Resource && !((Resource) o).isAnon()) {
	    java.util.Enumeration e = ((Resource) o).getPropertyURIs();
	    if (e != null && e.hasMoreElements()) {
		if (PROP_RDF_TYPE.equals(e.nextElement())
			&& !e.hasMoreElements()) {
		    Object tmp = ((Resource) o).getProperty(PROP_RDF_TYPE);
		    if (tmp instanceof List && ((List) tmp).size() == 1)
			tmp = ((List) tmp).get(0);
		    if (tmp instanceof Resource)
			tmp = ((Resource) tmp).getURI();
		    if (OWL_CLASS.equals(tmp))
			return new TypeURI(((Resource) o).getURI(), false);
		    else if (tmp == null)
			if (OntologyManagement.getInstance().isRegisteredClass(
				((Resource) o).getURI(), true))
			    return new TypeURI(((Resource) o).getURI(), false);
			else if (TypeMapper
				.isRegisteredDatatypeURI(((Resource) o)
					.getURI()))
			    return new TypeURI(((Resource) o).getURI(), true);
		}
	    } else if (TypeMapper.isRegisteredDatatypeURI(((Resource) o)
		    .getURI()))
		return new TypeURI(((Resource) o).getURI(), true);
	    else if (OntologyManagement.getInstance().isRegisteredClass(
		    ((Resource) o).getURI(), true))
		return new TypeURI(((Resource) o).getURI(), false);
	} else if (o instanceof String)
	    if (TypeMapper.isRegisteredDatatypeURI((String) o))
		return new TypeURI((String) o, true);
	    else if (OntologyManagement.getInstance().isRegisteredClass(
		    (String) o, true))
		return new TypeURI((String) o, false);

	return null;
    }

    /**
     * No {@link TypeExpression} instances are stored in this class, so we do
     * not need to clone.
     * 
     * @see org.universAAL.middleware.owl.TypeExpression#copy()
     */
    public TypeExpression copy() {
	return this;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getNamedSuperclasses() */
    public String[] getNamedSuperclasses() {
	return new String[] { getURI() };
    }

    /**
     * Get the restrictions for the given property.
     * 
     * @see org.universAAL.middleware.owl.ManagedIndividual#getClassRestrictionsOnProperty(String,
     *      String)
     */
    public TypeExpression getRestrictionOnProperty(String propURI) {
	return ManagedIndividual.getClassRestrictionsOnProperty(uri, propURI);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getUpperEnumeration() */
    public Object[] getUpperEnumeration() {
	OntClassInfo info = OntologyManagement.getInstance().getOntClassInfo(
		getURI());
	Resource[] answer = info == null ? null : info.getInstances();
	return (answer == null) ? new Object[0] : answer;
    }

    public boolean hasMember(Object value, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	// ttl =
	checkTTL(ttl);
	if (uri.equals(TYPE_OWL_THING))
	    return true;

	// TODO: 1. could variables be used in constructing class names?
	// 2. what if variables are used not only as values but also within
	// values
	if (value instanceof Collection) {
	    for (Iterator i = ((Collection) value).iterator(); i.hasNext();) {
		Object val = Variable.resolveVarRef(i.next(), context);
		if (val == null || !ManagedIndividual.checkMembership(uri, val))
		    return false;
	    }
	    return true;
	} else {
	    value = Variable.resolveVarRef(value, context);
	    return value != null
		    && ManagedIndividual.checkMembership(uri, value);
	}
    }

    public boolean matches(TypeExpression subtype, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	ttl = checkTTL(ttl);
	if (uri.equals(TYPE_OWL_THING))
	    return subtype != null;

	if (subtype instanceof Enumeration)
	    return ((Enumeration) subtype)
		    .hasSupertype(this, context, ttl, log);

	if (subtype instanceof TypeURI)
	    return ManagedIndividual.checkCompatibility(uri,
		    ((TypeURI) subtype).uri);

	if (subtype instanceof Union) {
	    HashMap cloned = (context == null) ? null : (HashMap) context
		    .clone();
	    for (Iterator i = ((Union) subtype).types(); i.hasNext();)
		if (!matches((TypeExpression) i.next(), cloned, ttl, log))
		    return false;
	    synchronize(context, cloned);
	    return true;
	}

	if (subtype instanceof Intersection
		&& !(subtype instanceof MergedRestriction)) {
	    for (Iterator i = ((Intersection) subtype).types(); i.hasNext();)
		if (matches((TypeExpression) i.next(), context, ttl, log))
		    return true;
	    // TODO: there is still a chance to return true...
	    // so fall through to the general case at the end
	} else if (subtype instanceof PropertyRestriction) {
	    if (subtype instanceof HasValueRestriction) {
		HasValueRestriction has = (HasValueRestriction) subtype;

		if (Resource.PROP_RDF_TYPE.equals(has.getOnProperty())) {
		    Object o = has.getConstraint();
		    if (Variable.isVarRef(o)) {
			Object val = Variable.resolveVarRef(o, context);
			if (val instanceof Variable) {
			    Variable var = (Variable) val;
			    if (var.getMinCardinality() < 2
				    && TypeMapper
					    .getDatatypeURI(Resource.class)
					    .equals(var.getParameterType())) {
				// special case: we are matching a ype filter
				// the hasValue is a Variable with the type
				// information
				// and we are matching it with a TypeURI
				// -> we have to store the TypeURI in context
				if (context != null) {
				    context.put(var.getURI(), copy());
				}
				return true;
			    }
			}
		    }
		}
	    }

	    MergedRestriction r = ManagedIndividual
		    .getClassRestrictionsOnProperty(uri,
			    ((PropertyRestriction) subtype).getOnProperty());
	    return r == null || r.matches(subtype, context, ttl, log);
	} else if (subtype instanceof MergedRestriction) {
	    MergedRestriction r = ManagedIndividual
		    .getClassRestrictionsOnProperty(uri,
			    ((MergedRestriction) subtype).getOnProperty());
	    return r == null || r.matches(subtype, context, ttl, log);
	}
	// a last try
	Object[] members = (subtype == null) ? null : subtype
		.getUpperEnumeration();
	if (members != null && members.length > 0) {
	    HashMap cloned = (context == null) ? null : (HashMap) context
		    .clone();
	    for (int i = 0; i < members.length; i++)
		if (!hasMember(members[i], cloned, ttl, log))
		    return false;
	    synchronize(context, cloned);
	    return true;
	}
	// in case of complements, it is unlikely and otherwise difficult to
	// decide
	return false;
    }

    public boolean isDisjointWith(TypeExpression other, HashMap context,
	    int ttl, List<MatchLogEntry> log) {
	ttl = checkTTL(ttl);
	if (uri.equals(TYPE_OWL_THING))
	    return false;

	if (other instanceof Complement)
	    return ((Complement) other).getComplementedClass().matches(this,
		    context, ttl, log);

	if (other instanceof TypeURI)
	    return !ManagedIndividual.checkCompatibility(uri,
		    ((TypeURI) other).uri)
		    && !ManagedIndividual.checkCompatibility(
			    ((TypeURI) other).uri, uri);

	if (other instanceof PropertyRestriction) {
	    MergedRestriction r = ManagedIndividual
		    .getClassRestrictionsOnProperty(uri,
			    ((PropertyRestriction) other).getOnProperty());
	    return r != null
		    && ((PropertyRestriction) other).isDisjointWith(r, context,
			    ttl, log);
	} else if (other instanceof MergedRestriction) {
	    MergedRestriction r = ManagedIndividual
		    .getClassRestrictionsOnProperty(uri,
			    ((MergedRestriction) other).getOnProperty());
	    return r != null
		    && ((MergedRestriction) other).isDisjointWith(r, context,
			    ttl, log);
	}

	if (other != null)
	    return other.isDisjointWith(this, context, ttl, log);

	return false;
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#isWellFormed() */
    public boolean isWellFormed() {
	return true;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public boolean setProperty(String propURI, Object o) {
	// ignore
	return false;
    }
}
