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
import org.universAAL.middleware.owl.Ontology;

/**
 * Setup interface for creating new RDF classes. The creation is separated from
 * the usage; for every {@link RDFClassInfo} there is exactly one
 * RDFClassInfoSetup where all the characteristics of this class are defined.
 * 
 * To create a new {@link RDFClassInfo}, define a subclass of {@link Ontology}
 * and overwrite the {@link Ontology#create()} method.
 * 
 * @author Carsten Stockloew
 * @see {@link RDFClassInfo}, {@link OntClassInfo}, {@link OntClassInfoSetup}
 */
public interface RDFClassInfoSetup {

    /**
     * Add a super class. Roughly speaking, this states that this class is more
     * specific than the given class expression.
     * 
     * @param superClass
     *            The super class.
     */
    public void addSuperClass(ClassExpression superClass);

    /**
     * Add a super class. Instances of this class are also instances of the
     * class specified by <code>superClassURI</code>. A class can have multiple
     * super classes. Properties that are defined in the super class are also
     * available in the sub class.
     * 
     * @param superClassURI
     *            URI of the super class
     */
    public void addSuperClass(String superClassURI);

    /**
     * Add an instance of this class.
     * 
     * @param instance
     *            The instance to add.
     */
    public void addInstance(Resource instance);

    /**
     * Get the {@link RDFClassInfo} for this setup.
     */
    public RDFClassInfo getInfo();

    /**
     * Set the resource comment which may be used to provide a human-readable
     * description of a resource to clarify the meaning of this class.
     * 
     * @param comment
     *            The comment of the resource.
     */
    public void setResourceComment(String comment);

    /**
     * Set the resource label which may be used to provide a human-readable
     * version of a resource's name.
     * 
     * @param label
     *            The label of the resource.
     */
    public void setResourceLabel(String label);
}