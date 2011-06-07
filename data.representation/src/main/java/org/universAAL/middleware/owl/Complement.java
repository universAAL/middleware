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

import java.util.Hashtable;

/**
 * A complement class expression of a class expression <i>CE</i> contains all
 * individuals that are not instances of the class expression <i>CE</i>.
 *  
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * @author Carsten Stockloew
 */
public class Complement extends ClassExpression {
	
	/** URI for owl:complementOf. */
	public static final String PROP_OWL_COMPLEMENT_OF;
	
	static {
		PROP_OWL_COMPLEMENT_OF = OWL_NAMESPACE + "complementOf";
		register(Complement.class, null, PROP_OWL_COMPLEMENT_OF, null);
	}
	
	
	/** Constructor. */
	public Complement() {
		super();
	}
	
	/** Constructor. */
	public Complement(ClassExpression toComplement) {
		if (toComplement == null)
			throw new NullPointerException();
		if (toComplement instanceof Complement)
			throw new IllegalArgumentException();
		props.put(PROP_OWL_COMPLEMENT_OF, toComplement);
	}
	
	
	/** @see org.universAAL.middleware.owl.ClassExpression#copy() */
	public ClassExpression copy() {
		return new Complement(getComplementedClass().copy());
	}
	
	/** Get the complement class. */
	public ClassExpression getComplementedClass() {
		return (ClassExpression) props.get(PROP_OWL_COMPLEMENT_OF);
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#getNamedSuperclasses() */
	public String[] getNamedSuperclasses() {
		return new String[0];
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#getUpperEnumeration() */
	public Object[] getUpperEnumeration() {
		return new Object[0];
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#hasMember(Object, Hashtable) */
	public boolean hasMember(Object member, Hashtable context) {
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		if (!getComplementedClass().hasMember(member, cloned))
			if (cloned == null  ||  cloned.size() == context.size())
				return true;
		// TODO: all values different from those in the changed hashtable would cause a match
		return false;
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#matches(ClassExpression, Hashtable) */
	public boolean matches(ClassExpression subtype, Hashtable context) {
		return getComplementedClass().isDisjointWith(subtype, context);
	}

	/** @see org.universAAL.middleware.owl.ClassExpression#isDisjointWith(ClassExpression, Hashtable) */
	public boolean isDisjointWith(ClassExpression other, Hashtable context) {
		return getComplementedClass().matches(other, context);
	}
	
	/** @see org.universAAL.middleware.owl.ClassExpression#isWellFormed() */
	public boolean isWellFormed() {
		return getComplementedClass() != null;
	}

	/** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
	public void setProperty(String propURI, Object o) {
		Object tmp = TypeURI.asTypeURI(o);
		if (tmp != null)
			o = tmp;
		if (PROP_OWL_COMPLEMENT_OF.equals(propURI)
				&& o instanceof ClassExpression
				&& !(o instanceof Complement)
				&& !props.containsKey(PROP_OWL_COMPLEMENT_OF))
			props.put(PROP_OWL_COMPLEMENT_OF, o);
	}

}
