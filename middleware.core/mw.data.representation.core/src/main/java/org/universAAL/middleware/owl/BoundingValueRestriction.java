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
package org.universAAL.middleware.owl;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;

/**
 * Implementation of XSD Value Restrictions: it contains all individuals that
 * are connected by the specified property to a value that meets the specified
 * conditions. These conditions are either:
 * <ol>
 * <li>min inclusive</li>
 * <li>min exclusive</li>
 * <li>max inclusive</li>
 * <li>max exclusive</li>
 * </ol>
 * 
 * It is possible to define a condition on the min value and on the max value in
 * the same {@link BoundingValueRestriction}.
 * 
 * BoundingValueRestriction will soon be replaced by
 * {@link BoundedValueRestriction} and its subclasses.
 * 
 * @author Carsten Stockloew
 */
public class BoundingValueRestriction extends AllValuesFromRestriction {

    /** Standard constructor for exclusive use by serializers. */
    public BoundingValueRestriction() {
    }

    public BoundingValueRestriction(String propURI, Object min,
	    boolean minInclusive, Object max, boolean maxInclusive) {
	setOnProperty(propURI);

	Object o = min;
	if (o == null)
	    o = max;
	if (o == null)
	    throw new IllegalArgumentException(
		    "Either min or max must be not null.");

	if (o instanceof Integer)
	    setProperty(PROP_OWL_ALL_VALUES_FROM, new IntRestriction(
		    (Integer) min, minInclusive, (Integer) max, maxInclusive));
	else if (o instanceof Double)
	    setProperty(PROP_OWL_ALL_VALUES_FROM, new DoubleRestriction(
		    (Double) min, minInclusive, (Double) max, maxInclusive));
	else if (o instanceof Float)
	    setProperty(PROP_OWL_ALL_VALUES_FROM, new FloatRestriction(
		    (Float) min, minInclusive, (Float) max, maxInclusive));
	else if (o instanceof Long)
	    setProperty(PROP_OWL_ALL_VALUES_FROM, new LongRestriction(
		    (Long) min, minInclusive, (Long) max, maxInclusive));
	else if (o instanceof ComparableIndividual)
	    setProperty(PROP_OWL_ALL_VALUES_FROM, new IndividualRestriction(
		    (ComparableIndividual) min, minInclusive,
		    (ComparableIndividual) max, maxInclusive));

	LogUtils
		.logWarn(
			SharedResources.moduleContext,
			BoundingValueRestriction.class,
			"BoundingValueRestriction",
			new String[] { "BoundingValueRestriction is deprecated. Please use an AllValuesFromRestriction of a TypeRestriction"
				+ "(to be more precise: a ~sublass~ of TypeRestriction, like IntRestriction or FloatRestriction)." },
			null);
    }

    public Comparable getLowerbound() {
	Object o = getProperty(PROP_OWL_ALL_VALUES_FROM);
	return o == null ? null : ((BoundedValueRestriction) o).getLowerbound();
    }

    public Comparable getUpperbound() {
	Object o = getProperty(PROP_OWL_ALL_VALUES_FROM);
	return o == null ? null : ((BoundedValueRestriction) o).getUpperbound();
    }
}
