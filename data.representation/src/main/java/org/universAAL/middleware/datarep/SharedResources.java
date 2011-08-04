/**
 * 
 */
package org.universAAL.middleware.datarep;

import java.util.Dictionary;
import java.util.Hashtable;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;

/**
 * @author mtazari
 * 
 */
public class SharedResources {
	// configuration parameters as property keys
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

	public static Dictionary middlewareProps = new Hashtable(4);;
	public static ModuleContext moduleContext;

	public static Container getContainer() {
		return moduleContext.getContainer();
	}

	public static String getMiddlewareProp(String key) {
		return (key == null || middlewareProps == null) ? null
				: (String) middlewareProps.get(key);
	}

	public static void loadReasoningEngine() throws ClassNotFoundException {
		// the subclasses (e.g., Restriction & ClassExpression) will be loaded
		// automatically
		Class.forName("org.universAAL.middleware.owl.Complement");
		Class.forName("org.universAAL.middleware.owl.Enumeration");
		Class.forName("org.universAAL.middleware.owl.Intersection");
		Class.forName("org.universAAL.middleware.owl.OrderingRestriction");
		Class.forName("org.universAAL.middleware.owl.supply.LevelRating");
		Class.forName("org.universAAL.middleware.owl.supply.Rating");
		Class.forName("org.universAAL.middleware.owl.TypeURI");
		Class.forName("org.universAAL.middleware.owl.Union");
		Class.forName("org.universAAL.middleware.rdf.PropertyPath");
	}

	public static void setDefaults() {
		middlewareProps.put(uAAL_AAL_SPACE_ID, System.getProperty(
				uAAL_AAL_SPACE_ID,
				"urn:org.universAAL.aal_space:test_environment"));
		middlewareProps.put(uAAL_IS_COORDINATING_PEER, System.getProperty(
				uAAL_IS_COORDINATING_PEER, "true"));
		if ("true".equals(System.getProperty(uAAL_IS_DEBUG_MODE)))
			middlewareProps.put(uAAL_IS_DEBUG_MODE, "true");
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
