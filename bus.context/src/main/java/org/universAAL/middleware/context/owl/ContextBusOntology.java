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
package org.universAAL.middleware.context.owl;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.impl.ContextBusFactory;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntClassInfoSetup;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.rdf.Resource;

/**
 * 
 * @author Carsten Stockloew
 * 
 */
public class ContextBusOntology extends Ontology {

    public static final String NAMESPACE = Resource.uAAL_NAMESPACE_PREFIX
	    + "ContextBus.owl#";

    private static ContextBusFactory factory = new ContextBusFactory();

    public ContextBusOntology() {
	super(NAMESPACE);
    }

    public void create() {
	Resource r = getInfo();
	r.setResourceComment("Ontology of the universAAL Context Bus");
	r.setResourceLabel("Context Bus");
	addImport(DataRepOntology.NAMESPACE);

	OntClassInfoSetup oci;

	// load RDF resources (no ManagedIndividuals)
	createNewRDFClassInfo(ContextEvent.MY_URI, factory, 0);
	createNewRDFClassInfo(ContextEventPattern.MY_URI, factory, 1);

	
	// load ContextProviderType
	oci = createNewAbstractOntClassInfo(ContextProviderType.MY_URI);
	oci
		.setResourceComment("An enumeration for the type of context providers.");
	oci.setResourceLabel("Context Provider Type");
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci.toEnumeration(new ManagedIndividual[] {
		ContextProviderType.controller, ContextProviderType.gauge,
		ContextProviderType.reasoner });

	// load ContextProvider
	oci = createNewOntClassInfo(ContextProvider.MY_URI, factory, 2);
	oci
		.setResourceComment("Represents the set of components that may publish context events.");
	oci.setResourceLabel("Context Provider");
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci.addObjectProperty(ContextProvider.PROP_CONTEXT_PROVIDER_TYPE)
		.setFunctional();
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			ContextProvider.PROP_CONTEXT_PROVIDER_TYPE,
			ContextProviderType.MY_URI, 1, 1));
	oci.addObjectProperty(ContextProvider.PROP_CONTEXT_SOURCE);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextProvider.PROP_CONTEXT_SOURCE, ManagedIndividual.MY_URI));
	oci.addObjectProperty(ContextProvider.PROP_CONTEXT_PROVIDED_EVENTS);
	// TODO: ContextEventPattern is not a ManagedIndividual!
	// oci.addRestriction(password,
	// Restriction.getAllValuesRestrictionWithCardinality(ContextProvider.PROP_CONTEXT_PROVIDED_EVENTS,
	// ContextEventPattern.MY_URI, -1, 1));
    }
}
