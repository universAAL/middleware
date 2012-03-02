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
package org.universAAL.middleware.rdf;

import org.universAAL.middleware.owl.ClassExpression;
import org.universAAL.middleware.owl.MaxCardinalityRestriction;

/**
 * Setup interface for creating new RDF properties. The creation is separated
 * from the usage; for every {@link Property} there is exactly one PropertySetup
 * where all the characteristics of this property are defined.
 * 
 * @author Carsten Stockloew
 * @see org.universAAL.middleware.owl.ObjectProperty
 * @see org.universAAL.middleware.owl.ObjectPropertySetup
 * @see org.universAAL.middleware.owl.DatatypeProperty
 * @see org.universAAL.middleware.owl.DatatypePropertySetup
 * @see Property
 */
public interface PropertySetup {

    /** Get the {@link Property} for this setup. */
    public Property getProperty();

    /**
     * Set the domain that is used to state that any resource that has a given
     * property is an instance of one or more classes.
     */
    public void setDomain(ClassExpression dom);

    /**
     * Set the range that is used to state that the values of a property are
     * instances of one or more classes.
     */
    public void setRange(ClassExpression range);

    /**
     * Set this property to be functional. This means that each individual is
     * connected to at most one individual or literal by this property. This can
     * be seen as a syntactic shortcut to defining a
     * {@link MaxCardinalityRestriction} for this property with a maximum
     * cardinality of one. For example, the property <i>a:hasWife</i> is a
     * functional property because one can have none or one wife.
     */
    public void setFunctional();

    /**
     * Add a super property. This means that all resources related by this
     * property are also related by the super property.
     * 
     * @param superProperty
     *            URI of the super property.
     */
    public void addSuperProperty(String superProperty);

    /**
     * Add an equivalent property. This means that this property is semantically
     * equivalent to the given property and that one can be replaced by the
     * other. Setting two properties <i>x</i> and <i>y</i> to be equivalent is
     * equivalent to saying that <i>x</i> is a sub property of <i>y</i> and
     * <i>y</i> is a sub property of <i>x</i>. For example, the two properties
     * <i>a:hasBrother</i> and <i>a:hasMaleSibling</i> are equivalent.
     * 
     * @param equivalentProperty
     *            URI of the equivalent property.
     */
    public void addEquivalentProperty(String equivalentProperty);

    /**
     * Add a disjoint property. This means that if an individual is connected to
     * another individual or literal by this property, they can not be connected
     * by the disjoint property. For example, the properties <i>a:hasName</i>
     * and <i>a:hasAddress</i> are disjoint because someone's name must be
     * different from his address.
     * 
     * @param disjointProperty
     *            URI of the disjoint property.
     */
    public void addDisjointProperty(String disjointProperty);
}
