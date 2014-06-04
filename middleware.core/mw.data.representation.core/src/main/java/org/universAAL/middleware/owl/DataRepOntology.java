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

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.DataRepFactory;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.owl.supply.AbsLocation;
import org.universAAL.middleware.owl.supply.LevelRating;
import org.universAAL.middleware.owl.supply.Rating;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;

/**
 * 
 * @author Carsten Stockloew
 * 
 */
public final class DataRepOntology extends Ontology {

    public static final String NAMESPACE = Resource.uAAL_NAMESPACE_PREFIX
	    + "DataRepresentation.owl#";

    private DataRepFactory factory = new DataRepFactory();

    public DataRepOntology() {
	super(NAMESPACE);
	Resource r = getInfo();
	r.setResourceComment("Ontology of the universAAL Basic Data Representation Model");
	r.setResourceLabel("Basic Data Representation Model");
    }

    public void create() {
	OntClassInfoSetup oci;

	try {
	    // load PropertyPath
	    createNewRDFClassInfo(PropertyPath.TYPE_PROPERTY_PATH, factory, 0);

	    // load ManagedIndividual
	    oci = createNewAbstractOntClassInfo(ManagedIndividual.MY_URI);
	    oci.setResourceComment("The root of the whole class hierarchy in the uAAL ontology.");
	    oci.setResourceLabel("uAAL Ontology Root Class");

	    // load ComparableIndividual
	    oci = createNewAbstractOntClassInfo(ComparableIndividual.MY_URI);
	    oci.setResourceComment("The root class for all comparable individuals in the uAAL ontology.");
	    oci.setResourceLabel("Comparable Individual");
	    oci.addSuperClass(ManagedIndividual.MY_URI);

	    // load AbsLocation
	    oci = createNewAbstractOntClassInfo(AbsLocation.MY_URI);
	    oci.setResourceComment("The root class for all locations.");
	    oci.setResourceLabel("Abstract Location");
	    oci.addSuperClass(ComparableIndividual.MY_URI);

	    // load LevelRating
	    oci = createNewAbstractOntClassInfo(LevelRating.MY_URI);
	    oci.setResourceComment("An enumeration for specifying the level of appearance / availability of a phenomen.");
	    oci.setResourceLabel("Level Rating");
	    oci.addSuperClass(ComparableIndividual.MY_URI);
	    oci.toEnumeration(new ManagedIndividual[] { LevelRating.none,
		    LevelRating.low, LevelRating.middle, LevelRating.high,
		    LevelRating.full });

	    // load Rating
	    oci = createNewAbstractOntClassInfo(Rating.MY_URI);
	    oci.setResourceComment("An enumeration for rating the perceived quality of a service similar to the"
		    + " german marks system for students' work.");
	    oci.setResourceLabel("QoS Rating");
	    oci.addSuperClass(ComparableIndividual.MY_URI);
	    oci.toEnumeration(new ManagedIndividual[] { Rating.poor,
		    Rating.almostPoor, Rating.almostSufficient,
		    Rating.sufficient, Rating.richSufficient,
		    Rating.almostSatisfying, Rating.satisfying,
		    Rating.richSatisfying, Rating.almostGood, Rating.good,
		    Rating.richGood, Rating.almostExcellent, Rating.excellent });

	} catch (Exception e) {// IllegalAccessException e) {
	    e.printStackTrace();
	    LogUtils.logDebug(SharedResources.moduleContext, this.getClass(),
		    "create", new Object[] { "Exception: ", e.getMessage() },
		    null);
	}
    }
}
