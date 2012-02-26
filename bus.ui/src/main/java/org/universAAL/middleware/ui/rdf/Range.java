/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either.ss or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.ui.rdf;

import org.universAAL.middleware.owl.AllValuesFromRestriction;
import org.universAAL.middleware.owl.BoundedValueRestriction;
import org.universAAL.middleware.owl.ComparableIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.PropertyPath;

/**
 * A range control should be used as placeholder for such user input that must
 * belong to an ordered set of values between a known minimum value and a known
 * maximum value. These characteristics of the expected user input must be
 * derivable from the value restrictions, either the model-based restrictions
 * (those that can be extracted from form data) or those explicitly passed as
 * parameter to the constructor. Additionally, a "step" (a number - defaults to
 * 1) can be specified for determining a next or previous value relative to a
 * given value.
 * <p>
 * The type of the values (which will be derived from the value restrictions)
 * must be a subclass of either
 * {@link org.universAAL.middleware.owl.ComparableIndividual} (only those with
 * ordinal characteristics that do not return null in the implementation of
 * related methods) or a number (double, float, int or long). However, numbers
 * can be used only if the value restrictions are specified with the help of
 * {@link org.universAAL.middleware.owl.OrderingRestriction}. Other classes that
 * implement {@link java.lang.Comparable} are not supported because there is no
 * straightforward solution to determine the next or previous values based on a
 * step.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class Range extends Input {
    public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE + "Range";

    /**
     * a positive number (defaults to 1) for determining a next or previous
     * value relative to a given value.
     */
    public static final String PROP_STEP = Form.uAAL_DIALOG_NAMESPACE
	    + "rangeChangeStep";

    private Comparable max = null, min = null;

    /**
     * For exclusive use of de-serializers.
     */
    public Range() {
	super();
    }

    /**
     * Constructs a new range control.
     * 
     * @param parent
     *            The mandatory parent group as the direct container of this
     *            input field. See {@link FormControl#PROP_PARENT_CONTROL}.
     * @param label
     *            The optional {@link Label} to be associated with this input
     *            field. See {@link FormControl#PROP_CONTROL_LABEL}.
     * @param ref
     *            See {@link FormControl#PROP_REFERENCED_PPATH}; mandatory.
     * @param valueRestriction
     *            See {@link Input#PROP_VALUE_RESTRICTION}; optional.
     * @param initialValue
     *            The optional initial value to be stored in form data under the
     *            path given for the above <code>ref</code> parameter.
     */
    public Range(Group parent, Label label, PropertyPath ref,
	    MergedRestriction valueRestriction, Object initialValue) {
	super(MY_URI, parent, label, ref, valueRestriction, initialValue);
	if (getMaxValue() == null || getMinValue() == null)
	    throw new IllegalArgumentException(
		    "The lower- / upper-bound is not defined!");
	else if (max.getClass() != min.getClass()
		|| (!(max instanceof Number) && !(max instanceof ComparableIndividual))
		|| (max instanceof ComparableIndividual && ((ComparableIndividual) max)
			.ord() == Integer.MIN_VALUE))
	    throw new IllegalArgumentException("Type restrictions violated!");
    }

    private Comparable getBound(MergedRestriction r, boolean upper) {
	if (r == null)
	    return null;

	Comparable res = null;
	// BoundingValueRestriction boundRes =
	// (BoundingValueRestriction)
	// r.getRestriction(MergedRestriction.boundingValueID);

	BoundedValueRestriction boundRes = null;
	AllValuesFromRestriction allRes = (AllValuesFromRestriction) r
		.getRestriction(MergedRestriction.allValuesFromID);
	if (allRes != null) {
	    boundRes = (BoundedValueRestriction) allRes.getConstraint();
	}
	if (boundRes != null) {
	    res = upper ? boundRes.getUpperbound() : boundRes.getLowerbound();
	    if (res != null)
		return res;
	}

	Object o = OntologyManagement.getInstance().getResource(
		r.getPropTypeURI(), null);
	if (o == null)
	    return null;
	Class claz = o.getClass();
	return (ComparableIndividual.class.isAssignableFrom(claz)) ? (upper ? ComparableIndividual
		.getClassMaximum(claz)
		: ComparableIndividual.getClassMinimum(claz))
		: null;
    }

    /**
     * Overrides {@link FormControl#getMaxLength()} by returning always -1,
     * because no standard string representation of a range control exists.
     */
    public int getMaxLength() {
	// not applicable
	return -1;
    }

    public Comparable getMaxValue() {
	if (max == null)
	    max = getBound(getRestrictions(), true);
	return max;
    }

    public Comparable getMinValue() {
	if (min == null)
	    min = getBound(getRestrictions(), false);
	return min;
    }

    /**
     * Using the current value stored in this range control and the value of
     * {@link #PROP_STEP}, tries to calculate a new value that is then stored to
     * substitute the current value.
     * 
     * @param numberOfSteps
     *            Number of sttp. Will be multiplied by the value of
     *            {@link #PROP_STEP} in oer to calculate theamount o requied
     *            shift. If positive, the shift will be towards max value;
     *            otherwise towards min value.
     * @return the newly calculated and stored value if everything goes well,
     *         otherwise null.
     */
    public Comparable shiftValue(int numberOfSteps) {
	Comparable curVal = (Comparable) getValue();
	if (numberOfSteps == 0 || curVal == null)
	    return curVal;

	Number step = getStep();
	Comparable newVal = null;
	if (curVal instanceof ComparableIndividual) {
	    ComparableIndividual ci = (ComparableIndividual) curVal;
	    if (numberOfSteps > 0)
		for (int i = numberOfSteps * step.intValue(); ci != null
			&& i > 0; i--)
		    ci = ci.getNext();
	    else
		for (int i = -numberOfSteps * step.intValue(); ci != null
			&& i > 0; i--)
		    ci = ci.getPrevious();
	    newVal = ci;
	} else if (curVal instanceof Double) {
	    newVal = new Double(((Double) curVal).doubleValue()
		    + step.doubleValue() * numberOfSteps);
	} else if (curVal instanceof Float) {
	    newVal = new Float(((Float) curVal).floatValue()
		    + step.floatValue() * numberOfSteps);
	} else if (curVal instanceof Integer) {
	    newVal = new Integer(((Integer) curVal).intValue()
		    + step.intValue() * numberOfSteps);
	} else if (curVal instanceof Long) {
	    newVal = new Long(((Long) curVal).longValue() + step.longValue()
		    * numberOfSteps);
	} else
	    return null;

	if (newVal != null && storeUserInput(newVal))
	    return newVal;
	return null;
    }

    /**
     * Using the value of {@link #PROP_STEP}, returns the number of steps
     * between the min and max values.
     */
    public int getRangeLength() {
	// make sure, max & min are set
	getMaxValue();
	getMinValue();

	Number step = getStep();
	if (max instanceof ComparableIndividual)
	    return (((ComparableIndividual) max).ord() - ((ComparableIndividual) min)
		    .ord())
		    / step.intValue();

	if (step instanceof Double)
	    return (int) ((((Number) max).doubleValue() - ((Number) min)
		    .doubleValue()) / step.doubleValue());

	if (step instanceof Float)
	    return (int) ((((Number) max).doubleValue() - ((Number) min)
		    .doubleValue()) / step.floatValue());

	if (step instanceof Integer)
	    return (int) ((((Number) max).longValue() - ((Number) min)
		    .longValue()) / step.intValue());

	if (step instanceof Long)
	    return (int) ((((Number) max).longValue() - ((Number) min)
		    .longValue()) / step.longValue());

	return Integer.MAX_VALUE;
    }

    /**
     * @see #PROP_STEP
     */
    public Number getStep() {
	Object o = props.get(PROP_STEP);
	return (o instanceof Number) ? (Number) o : new Integer(1);
    }

    /**
     * @see #PROP_STEP
     */
    public void setStep(Number step) {
	if (step != null && !props.containsKey(PROP_STEP)
		&& (step instanceof Integer || max instanceof Number)
		&& step.doubleValue() > 0)
	    props.put(PROP_STEP, step);
    }

    /**
     * @see Input#setProperty(String, Object)
     */
    public void setProperty(String propURI, Object value) {
	if (PROP_STEP.equals(propURI)) {
	    if (value instanceof Number)
		setStep((Number) value);
	} else
	    super.setProperty(propURI, value);
    }
}
