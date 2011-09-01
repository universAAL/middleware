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
package org.universAAL.middleware.service.owl;

import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntClassInfoSetup;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.impl.ServiceBusFactory;

/**
 * 
 * @author Carsten Stockloew
 *
 */
public class ServiceBusOntology extends Ontology {

    public static final String NAMESPACE = Resource.uAAL_NAMESPACE_PREFIX
	    + "ServiceBus.owl#";

    private static ServiceBusFactory factory = new ServiceBusFactory();

    public ServiceBusOntology() {
	super(NAMESPACE);
	Resource r = getInfo();
	r.setResourceComment("Ontology of the universAAL Service Bus");
	r.setResourceLabel("Service Bus");
	addImport(DataRepOntology.NAMESPACE);
    }

    public void create() {
	OntClassInfoSetup oci;

	// load Service
	oci = createNewAbstractOntClassInfo(Service.MY_URI);
	oci
		.setResourceComment("The root of the hierarchy of service classes in universAAL.");
	oci.setResourceLabel("universAAL Service");
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci
		.addDatatypeProperty(Service.PROP_NUMBER_OF_VALUE_RESTRICTIONS,
			true);
	oci.addObjectProperty(Service.PROP_INSTANCE_LEVEL_RESTRICTIONS, false,
		false, false, false);
	// TODO: Restrictions?

	// load UserInterfaceService
	oci = createNewAbstractOntClassInfo(UserInterfaceService.MY_URI);
	oci
		.setResourceComment("The class of all services starting an initial dialog correlated to a specific service class");
	oci.setResourceLabel("Initial Service Dialog");
	oci.addSuperClass(Service.MY_URI);
	oci.addObjectProperty(
		UserInterfaceService.PROP_CORRELATED_SERVICE_CLASS, true,
		false, false, false);
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			UserInterfaceService.PROP_CORRELATED_SERVICE_CLASS,
			TypeMapper.getDatatypeURI(Resource.class), 1, 1));
	oci.addDatatypeProperty(UserInterfaceService.PROP_DESCRIPTION, true);
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			UserInterfaceService.PROP_DESCRIPTION, TypeMapper
				.getDatatypeURI(String.class), 1, 1));
	oci.addObjectProperty(
		UserInterfaceService.PROP_HAS_INFO_RETRIEVAL_PROCESS, true,
		false, false, false);
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			UserInterfaceService.PROP_HAS_INFO_RETRIEVAL_PROCESS,
			TypeMapper.getDatatypeURI(Resource.class), 1, 1));
	oci.addObjectProperty(UserInterfaceService.PROP_HAS_VENDOR, true,
		false, false, false);
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			UserInterfaceService.PROP_HAS_VENDOR, TypeMapper
				.getDatatypeURI(Resource.class), 1, 1));

	// load InitialServiceDialog
	oci = createNewOntClassInfo(InitialServiceDialog.MY_URI, factory, 5);
	oci
		.setResourceComment("The class of all services starting an initial dialog correlated to a specific service class");
	oci.setResourceLabel("Initial Service Dialog");
	oci.addSuperClass(UserInterfaceService.MY_URI);
    }
}
