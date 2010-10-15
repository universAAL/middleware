/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.persona.ontology.expr;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.persona.middleware.PResource;
import org.persona.ontology.ManagedIndividual;
import org.persona.ontology.PClassExpression;


/**
 * @author mtazari
 *
 */
public class Enumeration extends PClassExpression {
	public static final String PROP_OWL_ONE_OF;
	public static final String TYPE_OWL_DATA_RANGE;
	static {
		PROP_OWL_ONE_OF = OWL_NAMESPACE + "oneOf";
		TYPE_OWL_DATA_RANGE = OWL_NAMESPACE + "DataRange";
		register(Enumeration.class, null, PROP_OWL_ONE_OF, TYPE_OWL_DATA_RANGE);
	}
	
	private ArrayList values = new ArrayList();
	private boolean datarange = false; 
	
	public Enumeration() {
		super();
		props.put(PROP_OWL_ONE_OF, values);
	}
	
	public Enumeration(Object[] values) {
		super();
		props.put(PROP_OWL_ONE_OF, this.values);
		if (values != null)
			for (int i=0; i<values.length; i++)
				if (values[i] != null) {
					if (values[i] instanceof String  &&  isQualifiedName((String) values[i]))
						values[i] = new PResource((String) values[i]);
					if (!datarange  &&  !(values[i] instanceof PResource)) {
						props.put(PROP_RDF_TYPE, new PResource(TYPE_OWL_DATA_RANGE));
						datarange = true;
					}
					this.values.add(values[i]);
				}
	}
	
	public void addValue(Object o) {
		// TODO: what if o is a list?
		if (o != null) {
			if (!datarange  &&  !(o instanceof PResource)) {
				props.put(PROP_RDF_TYPE, new PResource(TYPE_OWL_DATA_RANGE));
				datarange = true;
			}
			values.add(o);
		}
	}
	
	public PClassExpression copy() {
		Enumeration result = new Enumeration();
		for (Iterator i = values.iterator(); i.hasNext();)
			result.values.add(i.next());
		if (datarange) {
			result.datarange = true;
			result.props.put(PROP_RDF_TYPE, new PResource(TYPE_OWL_DATA_RANGE));
		}
		return result;
	}
	
	public Comparable getMaxValue() {
		try {
			Comparable result = null;
			for (Iterator i = values.iterator();  i.hasNext(); ) {
				Comparable o = (Comparable) i.next();
				if (result == null  ||  result.compareTo(o) < 0)
					result = o;
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	public Comparable getMinValue() {
		try {
			Comparable result = null;
			for (Iterator i = values.iterator();  i.hasNext(); ) {
				Comparable o = (Comparable) i.next();
				if (result == null  ||  result.compareTo(o) > 0)
					result = o;
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	public String[] getNamedSuperclasses() {
		ArrayList l = new ArrayList();
		for (Iterator i = values.iterator();  i.hasNext(); )
			collectTypesMinimized(ManagedIndividual.getTypeURI(i.next()), l);
		return (String[]) l.toArray(new String[l.size()]);
	}
	
	public Object[] getUpperEnumeration() {
		Object[] answer = new Object[values.size()];
		for (int i = 0;  i < values.size();  i++)
			answer[i] = values.get(i);
		return answer;
	}

	/**
	 * @see org.persona.ontology.PClassExpression#hasMember(Object, Hashtable)
	 */
	public boolean hasMember(Object value, Hashtable context) {
		if (value == null  || values.contains(value))
			return true;
		// TODO: what if variables had to be replaced using context
		return false;
	}

	public boolean matches(PClassExpression subtype, Hashtable context) {
		if (subtype == null)
			return false;
		
		if (subtype instanceof Enumeration)
			// TODO: what if variables had to be replaced using context
			return values.containsAll(((Enumeration) subtype).values);

		if (subtype instanceof Union) {
			for (Iterator i = ((Union) subtype).types();  i.hasNext(); ) {
				if (!matches((PClassExpression) i.next(), context))
					return false;
			}
			return true;
		} else if (subtype instanceof Intersection) {
			for (Iterator i = ((Intersection) subtype).types(); i.hasNext();) {
				if (matches((PClassExpression) i.next(), context))
					return true;
			}
			// TODO: there is still a chance to return true...
		}
		
		// a last try
		Object[] upperEnum = subtype.getUpperEnumeration();
		if (upperEnum != null  &&  upperEnum.length > 0) {
			// TODO: what if variables had to be replaced using context
			for (int i=0; i<upperEnum.length; i++)
				if (!values.contains(upperEnum[i]))
					return false;
			return true;
		}
		// TODO: for complement and restriction difficult to decide but also very unlikly 
		return false;
	}
	
	public boolean hasSupertype(PClassExpression supertype, Hashtable context) {
		if (supertype == null)
			return false;
		
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		for (Iterator i = values.iterator();  i.hasNext(); )
			if (!supertype.hasMember(i.next(), cloned))
				return false;

		// TODO: if cloned.size() != context.size(),
		//       then under certain conditions it could still work
		return cloned == null  ||  cloned.size() == context.size();
	}

	public boolean isDisjointWith(PClassExpression other, Hashtable context) {
		if (other == null)
			return false;
		
		Hashtable cloned = (context == null)? null : (Hashtable) context.clone();
		for (Iterator i = values.iterator();  i.hasNext(); ) {
			if (other.hasMember(i.next(), cloned))
				// TODO: if cloned.size() != context.size(),
				//       then under certain conditions it could still work
				return false;
		}
		
		return true;
	}
	
	public boolean isWellFormed() {
		return !values.isEmpty();
	}

	public void setProperty(String propURI, Object o) {
		if (PROP_OWL_ONE_OF.equals(propURI)  &&  values.isEmpty()  &&  o != null)
			if (o instanceof List)
				for (Iterator i = ((List) o).iterator();  i.hasNext(); )
					addValue(i.next());
			else
				addValue(o);
	}
	
	public Iterator values() {
		return values.iterator();
	}
}
