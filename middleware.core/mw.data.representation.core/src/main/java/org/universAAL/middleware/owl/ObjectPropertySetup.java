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

import org.universAAL.middleware.rdf.PropertySetup;

/**
 * <p>
 * Setup interface for creating new object properties. The creation is separated
 * from the usage; for every {@link ObjectProperty} there is exactly one
 * ObjectPropertySetup where all the characteristics of this property are
 * defined.
 * </p>
 * <p>
 * To create a new {@link ObjectProperty}, define a subclass of {@link Ontology}
 * , overwrite the {@link Ontology#create()} method, create an
 * {@link OntClassInfo} and call
 * {@link OntClassInfoSetup#addObjectProperty(String)}.
 * </p>
 * 
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ObjectProperty
 * @see org.universAAL.middleware.owl.DatatypeProperty
 * @see org.universAAL.middleware.owl.DatatypePropertySetup
 * @see org.universAAL.middleware.rdf.Property
 * @see org.universAAL.middleware.rdf.PropertySetup
 */
public interface ObjectPropertySetup extends PropertySetup {

    /**
     * Set the inverse property. If an individual <i>x</i> is connected by this
     * property to an individual <i>y</i>, then <i>y</i> is connected by the
     * inverse property to <i>x</i>. For example, the inverse of the property
     * <i>a:hasFather</i> could be the property <i>a:fatherOf</i>.
     * 
     * @param inverseOf
     *            URI of the inverse property.
     */
    public void setInverseOf(String inverseOf);

    /**
     * Set this property to be inverse-functional. This means that for each
     * individual <i>x</i>, there can be at most one individual <i>y</i> such
     * that <i>y</i> is connected by this property with <i>x</i>. For example,
     * the property <i>a:fatherOf</i> is inverse-functional since each person
     * can have only one father.
     */
    public void setInverseFunctional();

    /**
     * Set this property to be transitive. This means that if an individual
     * <i>x</i> is connected to the individual <i>y</i> by this property, and
     * <i>y</i> is connected to an individual <i>z</i> by this property, then
     * <i>x</i> is also connected to <i>z</i> by this property. For example, the
     * property <i>a:ancestorOf</i> is transitive: if <i>x</i> is an ancestor of
     * <i>y</i> and <i>y</i> is an ancestor of <i>z</i>, then <i>x</i> is an
     * ancestor of <i>z</i>.
     */
    public void setTransitive();

    /**
     * <p>
     * Set this property to be symmetric. This means that if an individual
     * <i>x</i> is connected to the individual <i>y</i> by this property, then
     * <i>y</i> is also connected to <i>x</i> by this property. for example, the
     * property <i>a:sisterOf</i> is symmetric.
     * </p>
     * <p>
     * A property can not be both, symmetric and asymmetric, but it can be
     * neither of it. For example, the property <i>a:loves</i> is neither
     * symmetric nor asymmetric because if one person loves another person, this
     * does not mean that this feeling is mutual.
     * </p>
     * 
     * @see #setAsymmetric()
     */
    public void setSymmetric();

    /**
     * Set this property to be asymmetric. This means that if an individual
     * <i>x</i> is connected to the individual <i>y</i> by this property, then
     * <i>y</i> cannot be connected to <i>x</i> by this property. for example,
     * the property <i>a:parentOf</i> is asymmetric.
     * 
     * @see #setSymmetric()
     */
    public void setAsymmetric();

    /**
     * Set this property to be reflexive. This means that each individual is
     * connected by this property to itself. For example, the property
     * <i>a:knows</i> is reflexive since everybody knows themselves.
     * 
     * @see #setIrreflexive()
     */
    public void setReflexive();

    /**
     * Set this property to be irreflexive. This means that no individual is
     * connected by this property to itself. For example, the property
     * <i>a:marriedTo</i> is irreflexive since nobody can be married to
     * themselves.
     * 
     * @see #setReflexive()
     */
    public void setIrreflexive();
}
