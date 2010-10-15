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
package org.persona.middleware.context;

import java.util.ArrayList;
import java.util.List;

import org.persona.middleware.PResource;
import org.persona.ontology.PClassExpression;
import org.persona.ontology.expr.Restriction;

/**
 * @author mtazari
 *
 */
public class ContextEventPattern extends PResource {
	public static final String MY_URI = ContextEvent.PERSONA_CONTEXT_NAMESPACE + "ContextEventPattern";
	
	public class Indices {
		private String[] subjects = null, props = null;
		private String[] subjectTypes = null;
		
		public String[] getProperties() {
			return props;
		}
		
		public String[] getSubjectTypes() {
			return subjectTypes;
		}
		
		public String[] getSubjects() {
			return subjects;
		}
	}
	
	private List restrictions;
	private Indices indices;
	
	public ContextEventPattern() {
		super();
		addType(MY_URI, true);
		indices = new Indices();
		restrictions = new ArrayList(5);
		props.put(PClassExpression.PROP_RDFS_SUB_CLASS_OF, restrictions);
	}
	
	public void addRestriction(Restriction r) {
		if (r == null)
			return;
		
		String prop = r.getOnProperty();
		if (ContextEvent.PROP_CONTEXT_ACCURACY.equals(prop)
				|| ContextEvent.PROP_CONTEXT_CONFIDENCE.equals(prop)
				|| ContextEvent.PROP_CONTEXT_PROVIDER.equals(prop)
				|| ContextEvent.PROP_CONTEXT_EXPIRATION_TIME.equals(prop)
				|| ContextEvent.PROP_CONTEXT_TIMESTAMP.equals(prop)
				|| ContextEvent.PROP_RDF_OBJECT.equals(prop)
				|| ContextEvent.PROP_RDF_PREDICATE.equals(prop)
				|| ContextEvent.PROP_RDF_SUBJECT.equals(prop))
			if (propRestrictionAllowed(prop)) {
				restrictions.add(r);
				if (prop.equals(ContextEvent.PROP_RDF_SUBJECT)) {
					PClassExpression type = (PClassExpression)
							r.getProperty(Restriction.PROP_OWL_ALL_VALUES_FROM);
					Object value =r.getProperty(Restriction.PROP_OWL_HAS_VALUE); 
					indices.subjectTypes = (type == null)? null
							: type.getNamedSuperclasses();
					
					indices.subjects = (value instanceof PResource)? new String[] {((PResource) value).getURI()}
							: null;
					if (indices.subjects == null) {
						Object[] elems = type.getUpperEnumeration();
						if (elems != null) {
							int num = 0;
							for (int i=0; i<elems.length; i++)
								if (elems[i] instanceof PResource)
									num++;
							if (num > 0) {
								indices.subjects = new String[num];
								for (int i=0; i<elems.length; i++)
									if (elems[i] instanceof PResource)
										indices.subjects[i] = ((PResource) elems[i]).getURI();
							}
						}
					}
				} else if (prop.equals(ContextEvent.PROP_RDF_PREDICATE)) {
					Object value =r.getProperty(Restriction.PROP_OWL_HAS_VALUE);
					indices.props = (value instanceof PResource)?
							new String[] {value.toString()} : null;
					if (indices.props == null) {
						PClassExpression type = (PClassExpression)
								r.getProperty(Restriction.PROP_OWL_ALL_VALUES_FROM);
						Object[] elems = (type == null)? null : type.getUpperEnumeration();
						if (elems != null) {
							int num = 0;
							for (int i=0; i<elems.length; i++)
								if (elems[i] instanceof PResource)
									num++;
							if (num > 0) {
								indices.props = new String[num];
								for (int i=0; i<elems.length; i++)
									if (elems[i] instanceof PResource)
										indices.props[i] = elems[i].toString();
							}
						}
					}
				}
			}
	}
	
	public Indices getIndices() {
		String[] empty = new String[0];
		if (indices.props == null)
			indices.props = empty;
		if (indices.subjects == null)
			indices.subjects = empty;
		if (indices.subjectTypes == null)
			indices.subjectTypes = empty;
		return indices;
	}
	
	public boolean matches(ContextEvent ce) {
		if (ce == null)
			return false;
		
		for (int i = 0;  i < restrictions.size();  i++)
			if (!((Restriction) restrictions.get(i)).hasMember(ce, null))
				return false;

		return true;
	}

	/**
	 * @see org.persona.middleware.PResource#isClosedCollection(java.lang.String)
	 */
	public boolean isClosedCollection(String propURI) {
		return !PClassExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)
		    && super.isClosedCollection(propURI);
	}
	
	public boolean isWellFormed() {
		return true;
	}
	
	private boolean propRestrictionAllowed(String prop) {
		for (int i = 0;  i < restrictions.size();  i++) {
			if (prop.equals(((Restriction) restrictions.get(i)).getOnProperty()))
					return false;
		}
		return true;
	}

	public void setProperty(String propURI, Object o) {
		if (PClassExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)) {
			if (restrictions.isEmpty()) {
				if (o instanceof Restriction)
					addRestriction((Restriction) o);
				else if (o instanceof List)
					for (int i = 0;  i < ((List) o).size();  i++)
						if (((List) o).get(i) instanceof Restriction)
							addRestriction((Restriction) ((List) o).get(i));
			}
		} else
			super.setProperty(propURI, o);
	}
}
