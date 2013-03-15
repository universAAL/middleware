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
package org.universAAL.middleware.datarep;

import java.util.Dictionary;
import java.util.Hashtable;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.OntologyManagement;

/**
 * @author mtazari
 * 
 */
public class SharedResources {
    // configuration parameters as property keys
    /**
     * My config file
     */
    public static final String uAAL_MW_SHARED_PROPERTY_FILE = "org.universAAL.mw.data.representation";
    /**
     * The URI prefix for the middleware.
     */
    public static final String uAAL_AAL_SPACE_ID = "org.universAAL.middleware.peer.member_of";

    /**
     * True, if this peer is the coordinator.
     */
    public static final String uAAL_IS_COORDINATING_PEER = "org.universAAL.middleware.peer.is_coordinator";

    /**
     * True, if debug mode is turned on.
     */
    public static final String uAAL_IS_DEBUG_MODE = "org.universAAL.middleware.debugMode";

    public static final Dictionary middlewareProps = new Hashtable(4);
    public static final Dictionary helpOnMiddlewareProps = new Hashtable(4);
    public static ModuleContext moduleContext;

    public static final DataRepFactory factory = new DataRepFactory();
    public static final DataRepOntology dataRepOntology = new DataRepOntology();

    private SharedResources() {
    }

    public static Container getContainer() {
	return moduleContext.getContainer();
    }

    public static String getMiddlewareProp(String key) {
	return (key == null || middlewareProps == null) ? null
		: (String) middlewareProps.get(key);
    }

    public static void loadReasoningEngine() throws ClassNotFoundException {
	OntologyManagement.getInstance().register(dataRepOntology);
	// the subclasses (e.g., Restriction & ClassExpression) will be loaded
	// automatically
	Class.forName("org.universAAL.middleware.owl.Complement");
	Class.forName("org.universAAL.middleware.owl.Enumeration");
	Class.forName("org.universAAL.middleware.owl.Intersection");
	// Class.forName("org.universAAL.middleware.owl.OrderingRestriction");
	Class.forName("org.universAAL.middleware.owl.supply.LevelRating");
	Class.forName("org.universAAL.middleware.owl.supply.Rating");
	Class.forName("org.universAAL.middleware.owl.TypeURI");
	Class.forName("org.universAAL.middleware.owl.Union");
	// Class.forName("org.universAAL.middleware.rdf.PropertyPath");
	Class.forName("org.universAAL.middleware.owl.AllValuesFromRestriction");
	Class.forName("org.universAAL.middleware.owl.BoundedValueRestriction");
	Class
		.forName("org.universAAL.middleware.owl.ExactCardinalityRestriction");
	Class.forName("org.universAAL.middleware.owl.HasValueRestriction");
	Class
		.forName("org.universAAL.middleware.owl.MaxCardinalityRestriction");
	Class
		.forName("org.universAAL.middleware.owl.MinCardinalityRestriction");
	Class.forName("org.universAAL.middleware.owl.MergedRestriction");
    }

    public static void unloadReasoningEngine() {
	OntologyManagement.getInstance().unregister(dataRepOntology);
    }

    public static void setDefaults() {
	middlewareProps.put(uAAL_AAL_SPACE_ID, System.getProperty(
		uAAL_AAL_SPACE_ID,
		"urn:org.universAAL.aal_space:test_environment"));
	middlewareProps.put(uAAL_IS_COORDINATING_PEER, System.getProperty(
		uAAL_IS_COORDINATING_PEER, "true"));
	if ("true".equals(System.getProperty(uAAL_IS_DEBUG_MODE)))
	    middlewareProps.put(uAAL_IS_DEBUG_MODE, "true");

	helpOnMiddlewareProps
		.put(uAAL_AAL_SPACE_ID,
			"A URI identifying the AAL Space to which this instance of middleware belongs.");
	helpOnMiddlewareProps
		.put(
			uAAL_IS_COORDINATING_PEER,
			"If set to 'true', then buses that need a coordinator instance are recommended to make the instance on this node to the coordinator. Only one instance per AAL Space is allowed to have this prop set.");
	helpOnMiddlewareProps
		.put(
			uAAL_IS_DEBUG_MODE,
			"If set to 'true', then buses are recommended to produce more log messages as in production mode (when this flag is not set, we assume production mode).");

	moduleContext
		.registerConfigFile(new Object[] {
			uAAL_MW_SHARED_PROPERTY_FILE,
			"Contains configuration parameters shared by all concrete buses.",
			helpOnMiddlewareProps });
    }

    public static void updateProps(Dictionary newPropValues) {
	if (newPropValues == null)
	    setDefaults();
	else {
	    Object val = newPropValues.remove(uAAL_AAL_SPACE_ID);
	    if (val instanceof String)
		middlewareProps.put(uAAL_AAL_SPACE_ID, val);

	    val = newPropValues.remove(uAAL_IS_COORDINATING_PEER);
	    if (val instanceof String)
		middlewareProps.put(uAAL_IS_COORDINATING_PEER, val);

	    val = newPropValues.remove(uAAL_IS_DEBUG_MODE);
	    if (val instanceof String)
		middlewareProps.put(uAAL_IS_DEBUG_MODE, val);
	}
    }
}
