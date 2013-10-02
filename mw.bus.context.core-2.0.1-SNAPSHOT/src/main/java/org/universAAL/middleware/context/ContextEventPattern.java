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
package org.universAAL.middleware.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.universAAL.middleware.bus.model.matchable.Advertisement;
import org.universAAL.middleware.bus.model.matchable.Event;
import org.universAAL.middleware.bus.model.matchable.EventAdvertisement;
import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.model.matchable.Requirement;
import org.universAAL.middleware.bus.model.matchable.Subscription;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.PropertyRestriction;
import org.universAAL.middleware.owl.TypeExpression;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.Resource;

/**
 * Defines the patterns used to match which events must be forwarded to which
 * subscribers.
 * 
 * Patterns are basically a collection of Restrictions upon a generic Context
 * Event that delimit and therefore narrow the specific Events a Subscriber will
 * be interested in.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class ContextEventPattern extends FinalizedResource implements
	EventAdvertisement, Subscription {
    public static final String MY_URI = ContextEvent.uAAL_CONTEXT_NAMESPACE
	    + "ContextEventPattern";

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

    // the list of restrictions as set as property of the resource
    private List restrictions;

    // additional internal management of restrictions
    // maps the URI of the onProperty (String) to a MergedRestriction
    private HashMap mergedRestrictions;

    private Indices indices;

    public ContextEventPattern() {
	super();
	addType(MY_URI, true);
	indices = new Indices();
	restrictions = new ArrayList(5);
	props.put(TypeExpression.PROP_RDFS_SUB_CLASS_OF, restrictions);
	mergedRestrictions = new HashMap();
    }

    public ContextEventPattern(String instanceURI) {
	super(instanceURI);
	addType(MY_URI, true);
	indices = new Indices();
	restrictions = new ArrayList(5);
	props.put(TypeExpression.PROP_RDFS_SUB_CLASS_OF, restrictions);
	mergedRestrictions = new HashMap();
    }

    /**
     * Add a restriction to the pattern, thus narrowing the events that will
     * match the pattern.
     * 
     * @param r
     *            The Restriction to add
     */
    public boolean addRestriction(MergedRestriction r) {
	if (r == null)
	    return false;

	String prop = r.getOnProperty();
	if (// deprecated -> ContextEvent.PROP_CONTEXT_ACCURACY.equals(prop) ||
	ContextEvent.PROP_CONTEXT_CONFIDENCE.equals(prop)
		|| ContextEvent.PROP_CONTEXT_PROVIDER.equals(prop)
		|| ContextEvent.PROP_CONTEXT_EXPIRATION_TIME.equals(prop)
		|| ContextEvent.PROP_CONTEXT_TIMESTAMP.equals(prop)
		|| ContextEvent.PROP_RDF_OBJECT.equals(prop)
		|| ContextEvent.PROP_RDF_PREDICATE.equals(prop)
		|| ContextEvent.PROP_RDF_SUBJECT.equals(prop))
	    if (propRestrictionAllowed(prop)) {
		mergedRestrictions.put(r.getOnProperty(), r);
		restrictions.addAll(r.getRestrictions());
		if (prop.equals(ContextEvent.PROP_RDF_SUBJECT)) {
		    TypeExpression type = (TypeExpression) r
			    .getConstraint(MergedRestriction.allValuesFromID);
		    Object value = r
			    .getConstraint(MergedRestriction.hasValueID);
		    indices.subjectTypes = (type == null) ? null : type
			    .getNamedSuperclasses();

		    indices.subjects = (value instanceof Resource) ? new String[] { ((Resource) value)
			    .getURI() }
			    : null;
		    if (indices.subjects == null) {
			Object[] elems = type.getUpperEnumeration();
			if (elems != null) {
			    int num = 0;
			    for (int i = 0; i < elems.length; i++)
				if (elems[i] instanceof Resource)
				    num++;
			    if (num > 0) {
				indices.subjects = new String[num];
				for (int i = 0; i < elems.length; i++)
				    if (elems[i] instanceof Resource)
					indices.subjects[i] = ((Resource) elems[i])
						.getURI();
			    }
			}
		    }
		} else if (prop.equals(ContextEvent.PROP_RDF_PREDICATE)) {
		    Object value = r
			    .getConstraint(MergedRestriction.hasValueID);
		    indices.props = (value instanceof Resource) ? new String[] { value
			    .toString() }
			    : null;
		    if (indices.props == null) {
			TypeExpression type = (TypeExpression) r
				.getConstraint(MergedRestriction.allValuesFromID);
			Object[] elems = (type == null) ? null : type
				.getUpperEnumeration();
			if (elems != null) {
			    int num = 0;
			    for (int i = 0; i < elems.length; i++)
				if (elems[i] instanceof Resource)
				    num++;
			    if (num > 0) {
				indices.props = new String[num];
				for (int i = 0; i < elems.length; i++)
				    if (elems[i] instanceof Resource)
					indices.props[i] = elems[i].toString();
			    }
			}
		    }
		}
		return true;
	    }
	return false;
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

    /**
     * @see org.universAAL.middleware.rdf.Resource#isClosedCollection(java.lang.String)
     */
    public boolean isClosedCollection(String propURI) {
	return !TypeExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)
		&& super.isClosedCollection(propURI);
    }

    public boolean isWellFormed() {
	return true;
    }

    private boolean propRestrictionAllowed(String prop) {
	return !mergedRestrictions.containsKey(prop);
    }

    public boolean setProperty(String propURI, Object property) {
	if (TypeExpression.PROP_RDFS_SUB_CLASS_OF.equals(propURI)) {
	    if (mergedRestrictions.isEmpty()) {
		if (property instanceof PropertyRestriction) {
		    // a single restriction
		    PropertyRestriction res = (PropertyRestriction) property;
		    MergedRestriction m = new MergedRestriction(res
			    .getOnProperty());
		    return addRestriction(m);
		} else if (property instanceof List) {
		    ArrayList l = MergedRestriction.getFromList((List) property);
		    boolean retVal = false;
		    for (int i = 0; i < l.size(); i++) {
			retVal = addRestriction((MergedRestriction) l.get(i)) || retVal;
		    }
		    return retVal;
		}
	    }
	} else {
	    return super.setProperty(propURI, property);
	}
	return false;
    }

    /**
     * @see #matches(Advertisement)
     */
    public boolean matches(Matchable other) {
	return false;
    }

    /**
     * This method will be called if advertisement is no
     * {@link EventAdvertisement}. Therefore it will never match this
     * {@link ContextEventPattern}, so <tt>false</tt> is returned.
     * 
     * @param advertisement
     *            the advertisement to be matched against
     * @return <tt>false</tt> as mentioned above
     */
    public boolean matches(Advertisement advertisement) {
	return false;
    }

    /**
     * If the advertisement is of the possibly matching type
     * {@link EventAdvertisement}, this method is called. It switches over
     * possible types of advertisement and calls the appropriate methods.
     * 
     * @param advertisement
     *            the advertisement to be matched against
     * @return <tt>true</tt> if the advertisement matches this
     *         {@link ContextEventPattern}, <tt>false</tt> if not
     */
    public boolean matches(EventAdvertisement advertisement) {
	if (advertisement instanceof ContextEventPattern) {
	    return isMatchingContextEventPattern((ContextEventPattern) advertisement);
	} else {
	    return false;
	}
    }

    /**
     * @see #matches(Advertisement)
     */
    public boolean matches(Requirement d) {
	return false;
    }

    /**
     * Switches over different types of {@link Event} to call appropriate
     * methods for them.
     * 
     * @param e
     *            the Event to match
     * @return <tt>true</tt> if matching, <tt>false</tt> if not
     */
    public boolean matches(Event e) {
	if (e instanceof ContextEvent) {
	    return isMatchingContextEvent((ContextEvent) e);
	} else {
	    return false;
	}
    }

    private boolean isMatchingContextEvent(ContextEvent ce) {
	if (ce == null)
	    return false;

	Iterator it = mergedRestrictions.values().iterator();
	while (it.hasNext())
	    if (!((MergedRestriction) it.next()).hasMember(ce, null))
		return false;

	return true;
    }

    /**
     * As with {@link #matches(Event)}, this method switches over
     * {@link Subscription}s, calling the appropriate methods for each.
     * 
     * @param s
     *            the Subscription to match
     * @return <tt>true</tt> if s matches, <tt>false</tt> if not
     */
    public boolean matches(Subscription s) {
	if (s instanceof ContextEventPattern) {
	    return isMatchingContextEventPattern((ContextEventPattern) s);
	} else {
	    return false;
	}
    }

    /**
     * As both {@link EventAdvertisement} and {@link Subscription} are
     * implemented by {@link ContextEventPattern}, both redirect here. TODO this
     * could be a problem in the future
     * 
     * @param pattern
     *            the ContextEventPattern to match
     * @return <tt>true</tt> if the pattern matches, <tt>false</tt> if not
     */
    private boolean isMatchingContextEventPattern(ContextEventPattern pattern) {
	// TODO method not implemented
	return false;
    }
}
