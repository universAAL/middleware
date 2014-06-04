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

import org.universAAL.middleware.rdf.RDFClassInfo;
import org.universAAL.middleware.rdf.RDFClassInfoSetup;

/**
 * Setup interface for creating new OWL classes. The creation is separated from
 * the usage; for every {@link OntClassInfo} there is exactly one
 * OntClassInfoSetup where all the characteristics of this class are defined.
 * 
 * To create a new {@link OntClassInfo}, define a subclass of {@link Ontology}
 * and overwrite the {@link Ontology#create()} method.
 * 
 * @author Carsten Stockloew
 * @see RDFClassInfo
 * @see OntClassInfo
 * @see RDFClassInfoSetup
 */
public interface OntClassInfoSetup extends RDFClassInfoSetup {

    /**
     * Add a restriction to a property. If a restriction was already set for
     * that property, an {@link IllegalAccessError} exception is thrown.
     * 
     * @param r
     *            The restriction to add.
     */
    public void addRestriction(MergedRestriction r);

    /**
     * Add an {@link ObjectProperty}. An object property in OWL connects an
     * instance of a class to an instance of class (instead of a literal).
     * 
     * @param propURI
     *            URI of the property.
     * @return A setup interface to set the characteristics of that property.
     * @see #addDatatypeProperty(String)
     */
    public ObjectPropertySetup addObjectProperty(String propURI);

    /**
     * Add a {@link DatatypeProperty}. A datatype property in OWL connects an
     * instance of a class to a literal (instead of an instance of a class).
     * 
     * @param propURI
     *            URI of the property.
     * @return A setup interface to set the characteristics of that property.
     * @see #addObjectProperty(String)
     */
    public DatatypePropertySetup addDatatypeProperty(String propURI);

    /**
     * Add an instance of this class.
     * 
     * @param instance
     *            The instance to add.
     */
    public void addInstance(ManagedIndividual instance);

    /**
     * Make this class an enumeration class by explicitly specifying all
     * instances of class. After calling this method, no additional instances
     * can be added.
     * 
     * @param individuals
     *            The set of instances of this class.
     */
    public void toEnumeration(ManagedIndividual[] individuals);

    /**
     * Set this class to be equivalent to the given class expression. This means
     * that this class is semantically equivalent to the given class expression
     * and that one can be replaced by the other.
     * 
     * @param eq
     *            The equivalent class expression.
     */
    public void addEquivalentClass(TypeExpression eq);

    /**
     * Set this class to be disjoint to the given class expression. This means
     * that the set of instances of this class is pairwise disjoint to the set
     * of instances defined by the given class expression.
     * 
     * @param dj
     *            The disjoint class expression.
     */
    public void addDisjointClass(TypeExpression dj);

    /**
     * Set this class to be the complement of the given class expression. This
     * means that all individuals are either in this class or in the given class
     * expression, but not in both.
     * 
     * @param complement
     *            The complement class expression.
     */
    public void setComplementClass(TypeExpression complement);
}
