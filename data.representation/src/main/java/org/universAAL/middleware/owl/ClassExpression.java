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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public abstract class ClassExpression extends Resource {
	
	public static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";

	public static final String TYPE_OWL_THING = OWL_NAMESPACE + "Thing";
	
	private class RegParams {
		Class clz;
		String hasSuperClass, hasProperty;
		boolean supportsAnonClass, supportsNamedClass;
	}
	
	public static final String OWL_CLASS = OWL_NAMESPACE + "Class";
	
	public static final String PROP_RDFS_SUB_CLASS_OF = RDFS_NAMESPACE + "subClassOf";
	
	private static ArrayList registry = new ArrayList(16); 
	private static Hashtable expressionTypes = new Hashtable(8);
	
	protected static final void register(Class clz,
			String hasSuperClass, String hasProperty, String expressionTypeURI) {
		if (expressionTypeURI != null  &&  !expressionTypeURI.equals(OWL_CLASS))
			expressionTypes.put(expressionTypeURI, clz);
		
		if (hasSuperClass != null  ||  hasProperty != null
				||  expressionTypeURI == null) {
			RegParams rp = null;
			// this is also a test that the given class is a valid subclass
			ClassExpression pseudo = getInstance(clz);
			if (pseudo == null) {
				pseudo = getNamedInstance(clz, ManagedIndividual.MY_URI);
				if (pseudo == null)
					return;
				rp = pseudo.new RegParams();
				rp.supportsAnonClass = false;
				rp.supportsNamedClass = true;
			} else {
				rp = pseudo.new RegParams();
				rp.supportsAnonClass = true;
				rp.supportsNamedClass = (null != getNamedInstance(clz, ManagedIndividual.MY_URI));
			}
			rp.clz = clz;
			rp.hasSuperClass = hasSuperClass;
			rp.hasProperty = hasProperty;
			registry.add(rp);
		}
	}
	
	private static ClassExpression getInstance(Class clz) {
		try {
			return (ClassExpression) clz.newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	private static ClassExpression getNamedInstance(Class clz, String classURI) {
		try {
			return (ClassExpression) clz.getConstructor(new Class[] {String.class}
							).newInstance(new Object[] {classURI});
		} catch (Exception e) {
			return null;
		}
	}
	
	public static final ClassExpression getClassExpressionInstance(String expressionTypeURI, String classURI) {
		if (expressionTypeURI == null)
			return null;
		
		Class c = (Class) expressionTypes.get(expressionTypeURI);
		if (c == null)
			return ((OWL_CLASS.equals(expressionTypeURI)
					&&  ManagedIndividual.isRegisteredClassURI(classURI))
							||  (expressionTypeURI == null
									&& TypeMapper.isRegisteredDatatypeURI(classURI)))?
					ClassExpression.getClassExpressionInstance(null, null, classURI) :  null;

		return isAnonymousURI(classURI)? getInstance(c) : getNamedInstance(c, classURI);
	}
	
	public static final ClassExpression getClassExpressionInstance(
			String superClassURI, String propURI, String expressionURI) {
		Class c = null;
		boolean isAnon = isAnonymousURI(expressionURI);
		for (Iterator i = registry.iterator();  i.hasNext(); ) {
			RegParams rp = (RegParams) i.next();
			if ((superClassURI == rp.hasSuperClass
					|| (superClassURI != null && superClassURI.equals(rp.hasSuperClass)))
			 && (propURI == rp.hasProperty
					|| (propURI != null  &&  propURI.equals(rp.hasProperty)))
			 && ((isAnon && rp.supportsAnonClass)  ||  (!isAnon && rp.supportsNamedClass))) {
				c = rp.clz;
				break;
			}
		}
		return isAnon? getInstance(c) : getNamedInstance(c, expressionURI);
	}
	
	protected ClassExpression() {
		super();
		addType(OWL_CLASS, true);
	}
	
	protected ClassExpression(String uri) {
		super(uri);
		addType(OWL_CLASS, true);
	}
	
	/**
	 * Provided that the given list is already a minimized list of type URIs, i.e.
	 * no two members have any hierarchical relationships with each other, adds the
	 * given typeURI to the list so that the above condition continues to hold and
	 * no information is lost.
	 */
	protected void collectTypesMinimized(String typeURI, List l) {
		if (typeURI != null) {
			boolean toAdd = true;
			for (Iterator j = l.iterator();  j.hasNext(); ) {
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
	
	public abstract ClassExpression copy();
	
	public abstract String[] getNamedSuperclasses();
	
	public abstract Object[] getUpperEnumeration();
	
	/**
	 * Returns true if the given object is a member of the class represented by this
	 * class expression, otherwise false. The <code>context</code> table maps the URIs
	 * of certain variables onto values currently assigned to them. The variables are
	 * either standard variables managed by the PERSONA middleware or parameters of a
	 * specific service in whose context this method is called. Both the object whose
	 * membership is going to be checked and this class expression may contain references
	 * to such variables. If there is already a value assigned to such a referenced
	 * variable, it must be replaced by the associated value, otherwise this method may
	 * expand the <code>context</code> table by deriving a value for such unassigned but
	 * referenced variables with which the membership can be asserted. In case of returning
	 * true, the caller must check the size of the <code>context</code> table to see if
	 * new conditions are added in order for the membership to be asserted. If the
	 * <code>context</code> table is null, the method does a global and unconditional check.
	 * 
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_BUS_MEMBER
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_HUMAN_USER
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_CURRENT_DATETIME
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_SERVICE_TO_SELECT
	 */
	public abstract boolean hasMember(Object member, Hashtable context);
	
	/**
	 * Returns true if the given class expression has no member in common with the class
	 * represented by this class expression, otherwise false. The <code>context</code>
	 * table maps the URIs of certain variables onto values currently assigned to them.
	 * The variables are either standard variables managed by the PERSONA middleware or
	 * parameters of a specific service in whose context this method is called. Both of the
	 * class expressions may contain references to such variables. If there is already a
	 * value assigned to such a referenced variable, it must be replaced by the associated
	 * value, otherwise this method may expand the <code>context</code> table by deriving
	 * a value for such unassigned but referenced variables with which the disjointness
	 * of the two classes can be asserted. In case of returning true, the caller must check
	 * the size of the <code>context</code> table to see if new conditions are added in
	 * order for the disjointness to be asserted. If the <code>context</code> table is null,
	 * the method does a global and unconditional check.
	 * 
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_BUS_MEMBER
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_HUMAN_USER
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_CURRENT_DATETIME
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_SERVICE_TO_SELECT
	 */
	public abstract boolean isDisjointWith(ClassExpression other, Hashtable context);
	
	public abstract boolean isWellFormed();
	
	/**
	 * Returns true if the given class expression is a subset of the class
	 * represented by this class expression, otherwise false. The <code>context</code>
	 * table maps the URIs of certain variables onto values currently assigned to them.
	 * The variables are either standard variables managed by the PERSONA middleware or
	 * parameters of a specific service in whose context this method is called. Both of the
	 * class expressions may contain references to such variables. If there is already a
	 * value assigned to such a referenced variable, it must be replaced by the associated
	 * value, otherwise this method may expand the <code>context</code> table by deriving
	 * a value for such unassigned but referenced variables with which the compatibility
	 * of the two classes can be asserted. In case of returning true, the caller must check
	 * the size of the <code>context</code> table to see if new conditions are added in
	 * order for the compatibility to be asserted. If the <code>context</code> table is null,
	 * the method does a global and unconditional check.
	 * 
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_BUS_MEMBER
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_ACCESSING_HUMAN_USER
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_CURRENT_DATETIME
	 * @see org.universAAL.middleware.util.Constants#VAR_uAAL_SERVICE_TO_SELECT
	 */
	public abstract boolean matches(ClassExpression subset, Hashtable context);
	
	protected void synchronize(Hashtable context, Hashtable cloned) {
		if (cloned != null  &&  cloned.size() > context.size()) 
			for (Iterator i = cloned.keySet().iterator();  i.hasNext();) {
				Object key = i.next();
				if (!context.containsKey(key))
					context.put(key, cloned.get(key));
			}
	}

}
