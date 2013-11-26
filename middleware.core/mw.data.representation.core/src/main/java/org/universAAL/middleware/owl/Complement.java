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
import java.util.List;

import org.universAAL.middleware.util.MatchLogEntry;

/**
 * A complement class expression of a class expression <i>CE</i> contains all
 * individuals that are not instances of the class expression <i>CE</i>.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public class Complement extends TypeExpression {

    /** URI for owl:complementOf. */
    public static final String PROP_OWL_COMPLEMENT_OF = OWL_NAMESPACE
	    + "complementOf";

    /** Constructor. */
    public Complement() {
	super();
    }

    /** Constructor. */
    public Complement(TypeExpression toComplement) {
	if (toComplement == null)
	    throw new NullPointerException();
	if (toComplement instanceof Complement)
	    throw new IllegalArgumentException();
	props.put(PROP_OWL_COMPLEMENT_OF, toComplement);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	return new Complement(getComplementedClass().copy());
    }

    /** Get the complement class. */
    public TypeExpression getComplementedClass() {
	return (TypeExpression) props.get(PROP_OWL_COMPLEMENT_OF);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getNamedSuperclasses() */
    public String[] getNamedSuperclasses() {
	return new String[0];
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#getUpperEnumeration() */
    public Object[] getUpperEnumeration() {
	return new Object[0];
    }

    public boolean hasMember(Object member, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	ttl = checkTTL(ttl);
	HashMap cloned = (context == null) ? null : (HashMap) context.clone();
	if (!getComplementedClass().hasMember(member, cloned, ttl, log))
	    if (cloned == null || cloned.size() == context.size())
		return true;
	// TODO: all values different from those in the changed hashtable would
	// cause a match
	return false;
    }

    public boolean matches(TypeExpression subtype, HashMap context, int ttl,
	    List<MatchLogEntry> log) {
	ttl = checkTTL(ttl);
	return getComplementedClass()
		.isDisjointWith(subtype, context, ttl, log);
    }

    public boolean isDisjointWith(TypeExpression other, HashMap context,
	    int ttl, List<MatchLogEntry> log) {
	ttl = checkTTL(ttl);
	return getComplementedClass().matches(other, context, ttl, log);
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#isWellFormed() */
    public boolean isWellFormed() {
	return getComplementedClass() != null;
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public boolean setProperty(String propURI, Object o) {
	Object tmp = TypeURI.asTypeURI(o);
	if (tmp != null)
	    o = tmp;
	if (PROP_OWL_COMPLEMENT_OF.equals(propURI)
		&& o instanceof TypeExpression && !(o instanceof Complement)
		&& !props.containsKey(PROP_OWL_COMPLEMENT_OF)) {
	    props.put(PROP_OWL_COMPLEMENT_OF, o);
	    return true;
	}
	return false;
    }
}
