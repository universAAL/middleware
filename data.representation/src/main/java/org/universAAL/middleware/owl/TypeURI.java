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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.Variable;

/**
 * Represents the URI of the <i>type</i> of an ontology class.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * @author Carsten Stockloew
 */
public class TypeURI extends ClassExpression {
	
	static {
		register(TypeURI.class, null, null, null);
	}
	
	public TypeURI(String uri, boolean isDatatypeURI) {
		super(uri);
		if (isDatatypeURI)
			props.remove(PROP_RDF_TYPE);
	}
	
	/**
	 * Creates a new TypeURI instance according to the given object. 
	 * @param o
	 * @return
	 */
	public static TypeURI asTypeURI(Object o) {
		if (o == null  ||  o instanceof TypeURI)
			return (TypeURI) o;
		
		if (o instanceof Resource  &&  !((Resource) o).isAnon()) {
			java.util.Enumeration e = ((Resource) o).getPropertyURIs();
			if (e != null  &&  e.hasMoreElements()) {
				if (PROP_RDF_TYPE.equals(e.nextElement())  &&  !e.hasMoreElements()) {
					Object tmp = ((Resource) o).getProperty(PROP_RDF_TYPE);
					if (tmp instanceof List  &&  ((List) tmp).size() == 1)
						tmp = ((List) tmp).get(0);
					if (tmp instanceof Resource)
						tmp = ((Resource) tmp).getURI();
					if (OWL_CLASS.equals(tmp))
						return new TypeURI(((Resource) o).getURI(), false);
					else if (tmp == null)
						if (ManagedIndividual.isRegisteredClassURI(((Resource) o).getURI()))
							return new TypeURI(((Resource) o).getURI(), false);
						else if (TypeMapper.isRegisteredDatatypeURI(((Resource) o).getURI()))
							return new TypeURI(((Resource) o).getURI(), true);
				}		
			} else if (TypeMapper.isRegisteredDatatypeURI(((Resource) o).getURI()))
				return new TypeURI(((Resource) o).getURI(), true);
			else if (ManagedIndividual.isRegisteredClassURI(((Resource) o).getURI()))
				return new TypeURI(((Resource) o).getURI(), false);
		} else if (o instanceof String)
			if (TypeMapper.isRegisteredDatatypeURI((String) o))
				return new TypeURI((String) o, true);
			else if (ManagedIndividual.isRegisteredClassURI((String) o))
				return new TypeURI((String) o, false);
		
		return null;
	}

	/**
	 * No {@link ClassExpression} instances are stored in this class, so we do not need to clone.
	 * @see org.universAAL.middleware.owl.ClassExpression#copy()
	 */
	public ClassExpression copy() {
		return this;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#getNamedSuperclasses() */
	public String[] getNamedSuperclasses() {
		return new String[] {getURI()};
	}
	
	/**
	 * Get the restrictions for the given property.
	 * @see org.universAAL.middleware.owl.ManagedIndividual#getClassRestrictionsOnProperty(String, String)
	 */
	public ClassExpression getRestrictionOnProperty(String propURI) {
		return ManagedIndividual.getClassRestrictionsOnProperty(uri, propURI);
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#getUpperEnumeration() */
	public Object[] getUpperEnumeration() {
		ManagedIndividual[] answer = ManagedIndividual.getEnumerationMembers(getURI());
		return (answer == null)? new Object[0] : answer;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#hasMember(Object, Hashtable) */
	public boolean hasMember(Object value, Hashtable context) {
		if (uri.equals(TYPE_OWL_THING))
			return true;
		
		// TODO: 1. could variables be used in constructing class names?
		//       2. what if variables are used not only as values but also within values
		if (value instanceof Collection) {
			for (Iterator i = ((Collection) value).iterator();  i.hasNext();) {
				Object val = Variable.resolveVarRef(i.next(), context);
				if (val == null  ||  !ManagedIndividual.checkMembership(uri, val))
					return false;
			}
			return true;
		} else {
			value = Variable.resolveVarRef(value, context);
			return value != null  &&  ManagedIndividual.checkMembership(uri, value);
		}
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression, Hashtable) */
	public boolean matches(ClassExpression subtype, Hashtable context) {
		if (uri.equals(TYPE_OWL_THING))
			return subtype != null;
		
		if (subtype instanceof Enumeration)
			return ((Enumeration) subtype).hasSupertype(this, context);

		if (subtype instanceof TypeURI)
			return ManagedIndividual.checkCompatibility(uri, ((TypeURI) subtype).uri);

		if (subtype instanceof Union) {
			Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
			for (Iterator i = ((Union) subtype).types(); i.hasNext();)
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
			// so fall through to the general case at the end
		} else if (subtype instanceof Restriction) {
			Restriction r = ManagedIndividual.getClassRestrictionsOnProperty(uri,
					((Restriction) subtype).getOnProperty());
			return r == null  ||  r.matches(subtype, context);
		}
		// a last try
		Object[] members = (subtype == null)? null : subtype.getUpperEnumeration();
		if (members != null  &&  members.length > 0) {
			Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
			for (int i=0; i<members.length; i++)
				if (!hasMember(members[i], cloned))
					return false;
			synchronize(context, cloned);
			return true;
		}
		// in case of complements, it is unlikely and otherwise difficult to decide
		return false;
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression, Hashtable) */
	public boolean isDisjointWith(ClassExpression other, Hashtable context) {
		if (uri.equals(TYPE_OWL_THING))
			return false;
		
		if (other instanceof Complement)
			return ((Complement) other).getComplementedClass().matches(this, context);
		
		if (other instanceof TypeURI)
			return !ManagedIndividual.checkCompatibility(uri, ((TypeURI) other).uri)
			    && !ManagedIndividual.checkCompatibility(((TypeURI) other).uri, uri);
		
		if (other instanceof Restriction) {
			Restriction r = ManagedIndividual.getClassRestrictionsOnProperty(uri,
					((Restriction) other).getOnProperty());
			return r != null  &&  ((Restriction) other).isDisjointWith(r, context);
		}
		
		if (other != null)
			return other.isDisjointWith(this, context);

		return false;
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
	public boolean isWellFormed() {
		return true;
	}

	/** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
	public void setProperty(String propURI, Object o) {
		// ignore
	}
}
