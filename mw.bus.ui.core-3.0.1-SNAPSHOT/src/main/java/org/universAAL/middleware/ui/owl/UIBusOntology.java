/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
	Copyright 2013-2014 Ericsson Nikola Tesla d.d., www.ericsson.com/hr/
	
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
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.middleware.ui.impl.UIBusFactory;
import org.universAAL.middleware.ui.rdf.ChoiceItem;
import org.universAAL.middleware.ui.rdf.ChoiceList;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.rdf.Group;
import org.universAAL.middleware.ui.rdf.InputField;
import org.universAAL.middleware.ui.rdf.Label;
import org.universAAL.middleware.ui.rdf.MediaObject;
import org.universAAL.middleware.ui.rdf.Range;
import org.universAAL.middleware.ui.rdf.Repeat;
import org.universAAL.middleware.ui.rdf.Select;
import org.universAAL.middleware.ui.rdf.Select1;
import org.universAAL.middleware.ui.rdf.SimpleOutput;
import org.universAAL.middleware.ui.rdf.SubdialogTrigger;
import org.universAAL.middleware.ui.rdf.Submit;
import org.universAAL.middleware.ui.rdf.TextArea;

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

	// load RDF resources (no ManagedIndividuals)
	createNewRDFClassInfo(UIHandlerProfile.MY_URI, factory, 0);
	createNewRDFClassInfo(UIRequest.MY_URI, factory, 1);
	createNewRDFClassInfo(UIResponse.MY_URI, factory, 2);
	createNewRDFClassInfo(Label.MY_URI, factory, 4);
	createNewRDFClassInfo(ChoiceItem.MY_URI, factory, 5);
	createNewRDFClassInfo(ChoiceList.MY_URI, factory, 6);
	createNewRDFClassInfo(Form.MY_URI, factory, 7);
	createNewRDFClassInfo(Group.MY_URI, factory, 8);
	createNewRDFClassInfo(Select.MY_URI, factory, 9);
	createNewRDFClassInfo(Select1.MY_URI, factory, 10);
	createNewRDFClassInfo(InputField.MY_URI, factory, 11);
	createNewRDFClassInfo(SimpleOutput.MY_URI, factory, 12);
	createNewRDFClassInfo(TextArea.MY_URI, factory, 13);
	createNewRDFClassInfo(Submit.MY_URI, factory, 14);
	createNewRDFClassInfo(SubdialogTrigger.MY_URI, factory, 15);
	createNewRDFClassInfo(Repeat.MY_URI, factory, 16);
	createNewRDFClassInfo(MediaObject.MY_URI, factory, 17);
	createNewRDFClassInfo(Range.MY_URI, factory, 18);

	// load AccessImpairment
	oci = createNewOntClassInfo(AccessImpairment.MY_URI, factory, 3);
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
		.setResourceComment("An enumeration for specifying the type of a dialog published to the UI bus.");
	oci.setResourceLabel("Dialog Type");
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci
		.toEnumeration(new ManagedIndividual[] { DialogType.sysMenu,
			DialogType.message, DialogType.subdialog,
			DialogType.stdDialog });

	// load Modality
	oci = createNewAbstractOntClassInfo(Modality.MY_URI);
	oci
		.setResourceComment("An enumeration for specifying the modality of information.");
	oci.setResourceLabel("Modality");
	oci.addSuperClass(ManagedIndividual.MY_URI);
	oci.toEnumeration(new ManagedIndividual[] { Modality.voice,
		Modality.gui, Modality.gesture, Modality.sms, Modality.web,
		Modality.mobile });

	// load PrivacyLevel
	oci = createNewAbstractOntClassInfo(PrivacyLevel.MY_URI);
	oci
		.setResourceComment("An enumeration for specifying the privacy level of information.");
	oci.setResourceLabel("Privacy Level");
	oci.addSuperClass(ComparableIndividual.MY_URI);
	oci.toEnumeration(new ManagedIndividual[] { PrivacyLevel.personal,
		PrivacyLevel.homeMatesOnly, PrivacyLevel.intimatesOnly,
		PrivacyLevel.knownPeopleOnly, PrivacyLevel.insensible });
	
	// load Recommendation
	oci = createNewAbstractOntClassInfo(Recommendation.MY_URI);
	oci
		.setResourceComment("General concept for representing modality recommendations for any FormElements.");
	oci.setResourceLabel("Recommendation");
	oci.addSuperClass(ManagedIndividual.MY_URI);
    }
}
