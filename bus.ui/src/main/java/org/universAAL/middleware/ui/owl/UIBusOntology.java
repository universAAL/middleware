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
package org.universAAL.middleware.ui.owl;

import org.universAAL.middleware.owl.ComparableIndividual;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntClassInfoSetup;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.supply.LevelRating;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.ui.impl.UIBusFactory;

public class UIBusOntology extends Ontology {

    public static final String NAMESPACE = Resource.uAAL_NAMESPACE_PREFIX
	    + "UIBus.owl#";

    private static UIBusFactory factory = new UIBusFactory();

    public UIBusOntology() {
	super(NAMESPACE);
    }

    public void create() {
	Resource r = getInfo();
	r.setResourceComment("Ontology of the universAAL UI Bus");
	r.setResourceLabel("UI Bus");
	addImport(DataRepOntology.NAMESPACE);

	OntClassInfoSetup oci;

	// load AccessImpairment
	oci = createNewOntClassInfo(AccessImpairment.MY_URI, factory, 0);
	oci
		.setResourceComment("General concept for representing impairments of the users in accessing the uAAL system.");
	oci.setResourceLabel("Access Impairment");
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci.addObjectProperty(AccessImpairment.PROP_IMPAIRMENT_LEVEL)
		.setFunctional();
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			AccessImpairment.PROP_IMPAIRMENT_LEVEL,
			LevelRating.MY_URI, 1, 1));

	// load DialogType
	oci = createNewAbstractOntClassInfo(DialogType.MY_URI);
	oci
		.setResourceComment("An enumeration for specifying the type of a dialog published to the output bus.");
	oci.setResourceLabel("Dialog Type");
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci
		.toEnumeration(new ManagedIndividual[] { DialogType.sysMenu,
			DialogType.message, DialogType.subdialog,
			DialogType.stdDialog });

	// load Gender
	oci = createNewAbstractOntClassInfo(Gender.MY_URI);
	oci
		.setResourceComment("An enumeration for specifying the gender in different contexts");
	oci.setResourceLabel("Gender");
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci
		.toEnumeration(new ManagedIndividual[] { Gender.female,
			Gender.male });

	// load Modality
	oci = createNewAbstractOntClassInfo(Modality.MY_URI);
	oci
		.setResourceComment("An enumeration for specifying the modality of information.");
	oci.setResourceLabel("Modality");
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci.toEnumeration(new ManagedIndividual[] { Modality.voice,
		Modality.gui, Modality.gesture, Modality.sms, Modality.web });

	// load PrivacyLevel
	oci = createNewAbstractOntClassInfo(PrivacyLevel.MY_URI);
	oci
		.setResourceComment("An enumeration for specifying the privacy level of information.");
	oci.setResourceLabel("Privacy Level");
	oci.addSuperClass(ComparableIndividual.MY_URI);
	oci.toEnumeration(new ManagedIndividual[] { PrivacyLevel.personal,
		PrivacyLevel.homeMatesOnly, PrivacyLevel.intimatesOnly,
		PrivacyLevel.knownPeopleOnly, PrivacyLevel.insensible });
    }
}
