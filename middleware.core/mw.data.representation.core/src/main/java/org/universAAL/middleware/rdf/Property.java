/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.rdf;

import java.util.ArrayList;

import org.universAAL.middleware.owl.ClassExpression;
import org.universAAL.middleware.owl.DatatypeProperty;
import org.universAAL.middleware.owl.ObjectProperty;
import org.universAAL.middleware.owl.OntClassInfo;
import org.universAAL.middleware.owl.Ontology;

/**
 * Definition of an RDF property. The creation is separated from the usage; for
 * every Property there is exactly one {@link PropertySetup} where all the
 * characteristics of this property are defined.
 * 
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ObjectProperty
 * @see org.universAAL.middleware.owl.ObjectPropertySetup
 * @see org.universAAL.middleware.owl.DatatypeProperty
 * @see org.universAAL.middleware.owl.DatatypePropertySetup
 * @see org.universAAL.middleware.rdf.PropertySetup
 */

public abstract class Property extends FinalizedResource {

    /**
     * URI of rdfs:domain that is used to state that any resource that has a
     * given property is an instance of one or more classes.
     */
    public static final String PROP_RDFS_DOMAIN = RDFS_NAMESPACE + "domain";

    /**
     * URI of rdfs:range that is used to state that the values of a property are
     * instances of one or more classes.
     */
    public static final String PROP_RDFS_RANGE = RDFS_NAMESPACE + "range";

    /** Determines whether this property is functional. */
    protected boolean isFunctional;

    /** The set of super properties. */
    private volatile ArrayList subPropertyOf = new ArrayList();

    /** The set of equivalent properties. */
    private volatile ArrayList equivalentProperties = new ArrayList();

    /** The rdf:domain */
    private ClassExpression domain = null;

    /** The ontology that defines this property. */
    protected OntClassInfo info;

    /** The setup interface. */
    protected PrivatePropertySetup setup;

    /**
     * Implementation of the setup interface. For security reasons, this is
     * realized as a protected nested class so that only the creator of an
     * {@link Ontology} has access to it and can make changes.
     */
    protected class PrivatePropertySetup implements PropertySetup {
	/** The property. */
	protected Property prop;

	/** Constructor. */
	public PrivatePropertySetup(Property prop) {
	    this.prop = prop;
	}

	/** Get the property for this set up. */
	public Property getProperty() {
	    return prop;
	}

	/** @see PropertySetup#setFunctional() */
	public void setFunctional() {
	    prop.isFunctional = true;
	}

	/** @see PropertySetup#addSuperProperty(String) */
	public synchronized void addSuperProperty(String superProperty) {
	    if (subPropertyOf.contains(superProperty))
		return;

	    ArrayList al = new ArrayList(subPropertyOf);
	    al.add(superProperty);
	    subPropertyOf = al;
	}

	/** @see PropertySetup#addEquivalentProperty(String) */
	public void addEquivalentProperty(String equivalentProperty) {
	    // we have to synchronize for all Property instances that may be in
	    // the
	    // set of equivalent properties
	    // -> just synch over all Properties (synch only blocks this method,
	    // and
	    // adding equivalent properties is assumed to happen not very often;
	    // mainly at the beginning)
	    // synchronized (equivalentPropertiesSync) {
	    // get the two sets of Properties
	    // ArrayList set1 = equivalentProperties;
	    // ArrayList set2 = equivalentProperty.equivalentProperties;
	    //
	    // // combine the two sets
	    // ArrayList comb = new ArrayList(set1.size() + set2.size());
	    // comb.addAll(set1);
	    // for (int i = 0; i < set2.size(); i++)
	    // if (!comb.contains(set2.get(i)))
	    // comb.add(set2.get(i));
	    //
	    // // set the combined set in all Properties
	    // for (int i = 0; i < comb.size(); i++)
	    // ((Property) comb.get(i)).equivalentProperties = comb;
	    // }
	}

	/** @see PropertySetup#addDisjointProperty(String) */
	public void addDisjointProperty(String disjointProperty) {
	    // TODO Auto-generated method stub
	}

	/** @see PropertySetup#setDomain(ClassExpression) */
	public void setDomain(ClassExpression dom) {
	    domain = dom;
	    setProperty(PROP_RDFS_DOMAIN, domain);
	}

	/** @see PropertySetup#setRange(ClassExpression) */
	public void setRange(ClassExpression range) {
	    // TODO Auto-generated method stub
	}
    }

    /**
     * Protected constructor, to create instances call either
     * {@link ObjectProperty#create(String, OntClassInfo)} or
     * {@link DatatypeProperty#create(String, OntClassInfo)}.
     * 
     * @param uri
     *            URI of this property.
     * @param info
     *            The class for which this property is defined.
     */
    protected Property(String uri, OntClassInfo info) {
	super(uri);
	if (info == null)
	    throw new NullPointerException(
		    "The ontology class for the property must be not null.");
	if (!info.checkPermission(uri))
	    throw new IllegalAccessError(
		    "The given property URI is not defined in the context of the given ontology class.");
	this.info = info;
    }

    /**
     * Determines whether this property is functional.
     * 
     * @see PropertySetup#setFunctional()
     */
    public boolean isFunctional() {
	return isFunctional;
    }
}
