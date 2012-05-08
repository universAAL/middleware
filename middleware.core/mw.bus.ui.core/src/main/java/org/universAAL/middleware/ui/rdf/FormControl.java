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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * The structural unit of forms that may bear information to be presented to
 * human users and / or serve as a placeholder for user input.
 * 
 * @see <a
 *      href="ftp://ftp.igd.fraunhofer.de/outgoing/mtazari/persona/dialogPackage.jpg">
 *      ftp://ftp.igd.fraunhofer.de/outgoing/mtazari/persona/dialogPackage.jpg</a>
 * @author mtazari
 * @author Carsten Stockloew
 * @navassoc - "label" 1 Label
 * @navassoc - "parentGroup" 1 Group
 * @navassoc - "formObject" 1 Form
 */
public abstract class FormControl extends Resource {

    /**
     * Form controls may have a {@link Label}.
     */
    public static final String PROP_CONTROL_LABEL = Form.uAAL_DIALOG_NAMESPACE
	    + "controlLabel";

    /**
     * Form controls may have a help string to be presented to human users when
     * they need more info about the role of a form control.
     */
    public static final String PROP_HELP = Form.uAAL_DIALOG_NAMESPACE
	    + "ctrlHelp";

    /**
     * Form controls may have a hint string as a short hint about the role of a
     * form control. Confer the concept of tool-tips in graphical user
     * interfaces.
     */
    public static final String PROP_HINT = Form.uAAL_DIALOG_NAMESPACE
	    + "ctrlHint";

    /**
     * Apart from the three standard groups described in {@link Form}, all other
     * form controls are contained in a {@link Group}.
     */
    public static final String PROP_PARENT_CONTROL = Form.uAAL_DIALOG_NAMESPACE
	    + "parentControl";

    /**
     * If a form control is allowed to have associated data (initial value or
     * user input) it must have a property path that is used to access related
     * data within {@link Form#PROP_DIALOG_DATA_ROOT}.
     */
    public static final String PROP_REFERENCED_PPATH = Form.uAAL_DIALOG_NAMESPACE
	    + "controlRef";

    /**
     * Can be used to define local restrictions on the value of form controls in
     * addition to restrictions possibly derivable from possibly available form
     * data (the latter are called model-based restrictions).
     */
    public static final String PROP_VALUE_RESTRICTION = Form.uAAL_DIALOG_NAMESPACE
	    + "valueRestrictions";

    protected FormControl() {
	super();
    }

    protected FormControl(String typeURI, Object parent, Label label,
	    PropertyPath ref, MergedRestriction valueRestriction,
	    Object initialValue) {
	super();
	addType(typeURI, true);
	props.put(PROP_PARENT_CONTROL, parent);
	if (parent instanceof Group)
	    ((Group) parent).addChild(this);
	else if (!(this instanceof Group) || !(parent instanceof Form)
		|| ((Form) parent).getRootGroup() != null)
	    throw new IllegalArgumentException(
		    "Parent now allowed for this control!");
	if (label != null)
	    props.put(PROP_CONTROL_LABEL, label);
	if (parent instanceof Repeat)
	    return;
	if (ref != null) {
	    props.put(PROP_REFERENCED_PPATH, ref);
	    if (valueRestriction != null)
		props.put(PROP_VALUE_RESTRICTION, valueRestriction);
	    if (initialValue != null) {
		Form f = getFormObject();
		if (f == null
			|| !f.setValue(ref.getThePath(), initialValue,
				valueRestriction))
		    throw new IllegalArgumentException(
			    "Initial value doesn not match the restrictions!");
	    }
	} else if (initialValue != null || valueRestriction != null)
	    throw new IllegalArgumentException(
		    "Cannot store an initial value or a value restriction without any reference path!");
    }

    /**
     * Returns the nearest {@link Repeat} control containing this control if
     * any. Identical with {@link #getParentRepeat()} if this control is a
     * column in a {@link Repeat} control.
     */
    public Repeat getAncestorRepeat() {
	Group g = getParentGroup();
	while (g != null && !(g instanceof Repeat))
	    g = g.getParentGroup();
	return (Repeat) g;
    }

    /**
     * Returns the form object containing this form control.
     */
    public Form getFormObject() {
	Object cur = this;
	while (cur instanceof FormControl)
	    cur = ((FormControl) cur).props.get(PROP_PARENT_CONTROL);
	if (cur instanceof Form)
	    return (Form) cur;
	return null;
    }

    /**
     * Returns the help text for this control.
     * 
     * @see #PROP_HELP
     */
    public String getHelpString() {
	return (String) props.get(PROP_HELP);
    }

    /**
     * Returns the hint string for this control.
     * 
     * @see #PROP_HINT
     */
    public String getHintString() {
	return (String) props.get(PROP_HINT);
    }

    /**
     * Returns the {@link Label} of this control.
     * 
     * @see #PROP_CONTROL_LABEL
     */
    public Label getLabel() {
	return (Label) props.get(PROP_CONTROL_LABEL);
    }

    MergedRestriction getLocalRestrictions() {
	return (MergedRestriction) props.get(PROP_VALUE_RESTRICTION);
    }

    /**
     * Tries to find out the largest number of characters in the string
     * representation of the values associated with this control. The string
     * representation is determined simply by calling
     * {@link java.lang.Object#toString()} on each value. The values are
     * determined the following way:
     * <ul>
     * <li>if value restrictions can be drawn based on available form data, then
     * possible upper enumeration (see
     * {@link org.universAAL.middleware.owl.ClassExpression#getUpperEnumeration()
     * ()} is used
     * <li>if this control is a direct part of a {@link Repeat} object (i.e.,
     * assuming that a {@link Repeat} object can be rendered as a table, then a
     * direct part of it would be rendered as a column of the table), all values
     * in that column are drawn.
     * <li>as the last possibility, the value(s) possibly returned by
     * {@link #getValue()} is used.
     * </ul>
     * If no value can be found, -1 is returned.
     */
    public int getMaxLength() {
	int res = -1;
	MergedRestriction r = getRestrictions();
	Object[] value = (r == null) ? null : r.getEnumeratedValues();
	if (value != null) {
	    for (int i = value.length - 1; i > -1; i--) {
		if (value[i] != null) {
		    int aux = value[i].toString().length();
		    if (aux > res)
			res = aux;
		}
	    }
	    if (res > -1)
		return res;
	}

	Repeat rp = getParentRepeat();
	if (rp != null) {
	    List values = rp.getAllValues(getReferencedPPath());
	    if (values != null) {
		for (Iterator i = values.iterator(); i.hasNext();) {
		    Object o = i.next();
		    if (o != null) {
			int aux = o.toString().length();
			if (aux > res)
			    res = aux;
		    }
		}
	    }
	    if (res > -1)
		return res;
	}

	Object o = getValue();
	if (o instanceof List) {
	    for (Iterator i = ((List) o).iterator(); i.hasNext();) {
		o = i.next();
		if (o != null) {
		    int aux = o.toString().length();
		    if (aux > res)
			res = aux;
		}
	    }
	    return res;
	}

	return (o == null) ? -1 : getValue().toString().length();
    }

    /**
     * Returns possible value restrictions by merging any local or model-based
     * (defined over available form data) restrictions. Returns null, if both
     * types of restrictions were null.
     */
    public MergedRestriction getRestrictions() {
	PropertyPath pp = getReferencedPPath();
	MergedRestriction r1 = getLocalRestrictions(), r2 = getParentGroup()
		.getPPathRestriction((pp == null ? null : pp.getThePath()));
	return (r1 == null) ? r2 : r1.merge(r2);
    }

    /**
     * Returns the Group control that contains this form control as a direct
     * child.
     */
    public Group getParentGroup() {
	Object o = props.get(PROP_PARENT_CONTROL);
	if (o instanceof Group)
	    return (Group) o;
	return null;
    }

    /**
     * If this control is a column in a {@link Repeat} control, that
     * {@link Repeat} control is returned, otherwise null.
     */
    public Repeat getParentRepeat() {
	Group g = getParentGroup();
	if (g != null && !(g instanceof Repeat))
	    g = g.getParentGroup();
	return (g instanceof Repeat) ? (Repeat) g : null;
    }

    /**
     * @see #PROP_REFERENCED_PPATH
     */
    public PropertyPath getReferencedPPath() {
	return (PropertyPath) props.get(PROP_REFERENCED_PPATH);
    }

    /**
     * Returns the hierarchy of {@link Group}s containing this form control. The
     * first element in the returned array (index 0) will be one of the three
     * standard groups of the form containing this control (see the
     * documentation of class {@link Form}) and the last element will be its
     * direct parent group.
     */
    public Group[] getSuperGroups() {
	ArrayList al = new ArrayList();
	for (Group fc = getParentGroup(); fc != null; fc = fc.getParentGroup())
	    al.add(fc);
	// reverse the order
	Group[] result = new Group[al.size()];
	for (int i = al.size() - 2; i > -1; i--)
	    result[i] = (Group) al.get(i);
	return result;
    }

    /**
     * Returns the URI of the type of values that are / can be associated with
     * this control if it is decidable, null otherwise.
     */
    public String getTypeURI() {
	MergedRestriction r = getLocalRestrictions();
	if (r != null) {
	    String result = r.getPropTypeURI();
	    if (result != null)
		return result;
	}

	Group g = getParentGroup();
	if (g != null) {
	    PropertyPath pp = getReferencedPPath();
	    return g.getTypeURI((pp == null) ? null : pp.getThePath());
	} else
	    return null;
    }

    /**
     * Returns the value(s) currently associated with this control. If this
     * control represents a column in a {@link Repeat} control, the returned
     * value will be taken from the currently selected row in the {@link Repeat}
     * control.
     */
    public Object getValue() {
	Group g = getParentGroup();
	if (g == null)
	    return null;

	PropertyPath pp = getReferencedPPath();
	return g.getValue((pp == null ? null : pp.getThePath()));
    }

    /**
     * Checks if {@link Form#PROP_DIALOG_CURRENT_FOCUSED_CONTROL} has this
     * control as value.
     */
    public boolean hasFocus() {
	return getFormObject().getCurrentFocusedControl() == this;
    }

    /**
     * Checks if there is any value associated with this control in the form
     * data.
     */
    public boolean hasValue() {
	Group g = getParentGroup();
	if (g == null)
	    return false;

	PropertyPath pp = getReferencedPPath();
	return g.hasValue((pp == null ? null : pp.getThePath()));
    }

    /**
     * Checks if the value returned by {@link #getTypeURI()} is equal to
     * xsd:boolean.
     */
    public boolean isOfBooleanType() {
	return TypeMapper.getJavaClass(getTypeURI()) == Boolean.class;
    }

    /**
     * Checks if the value returned by {@link #getTypeURI()} is one of those
     * supported by the {@link org.universAAL.middleware.rdf.TypeMapper}.
     */
    public boolean isOfPrimitiveType() {
	return TypeMapper.isRegisteredDatatypeURI(getTypeURI());
    }

    /**
     * Checks if this control is a column in a {@link Repeat} control.
     */
    public boolean isRepeatable() {
	Group g = getParentGroup();
	if (g == null)
	    return false;
	if (g instanceof Repeat)
	    return true;

	return (g.getParentGroup() instanceof Repeat);
    }

    /**
     * @see #PROP_HELP
     */
    public void setHelpString(String value) {
	if (value != null && !props.containsKey(PROP_HELP))
	    props.put(PROP_HELP, value);
    }

    /**
     * @see #PROP_HINT
     */
    public void setHintString(String value) {
	if (value != null && !props.containsKey(PROP_HINT))
	    props.put(PROP_HINT, value);
    }

    /**
     * For usage by de-serializers.
     * 
     * @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object)
     */
    public void setProperty(String propURI, Object value) {
	if (propURI == null || value == null || props.containsKey(propURI))
	    return;

	boolean knownProp = false;

	if (propURI.equals(PROP_CONTROL_LABEL)) {
	    if (value instanceof Label)
		knownProp = true;
	    else
		return;
	} else if (propURI.equals(PROP_HELP)) {
	    if (value instanceof String)
		knownProp = true;
	    else
		return;
	} else if (propURI.equals(PROP_HINT)) {
	    if (value instanceof String)
		knownProp = true;
	    else
		return;
	} else if (propURI.equals(PROP_PARENT_CONTROL)) {
	    if (value instanceof FormControl
		    || (value instanceof Form && this instanceof Group))
		knownProp = true;
	    else
		return;
	} else if (propURI.equals(PROP_REFERENCED_PPATH)) {
	    if (value instanceof PropertyPath)
		knownProp = true;
	    else
		return;
	} else if (propURI.equals(PROP_VALUE_RESTRICTION)) {
	    if (value instanceof MergedRestriction
		    && !props.containsKey(propURI))
		knownProp = true;
	    else
		return;
	}

	if (knownProp)
	    props.put(propURI, value);
	else
	    super.setProperty(propURI, value);
    }

    /**
     * Returns the text of the {@link Label} possibly associated with this
     * control, null otherwise.
     */
    public String toString() {
	Label l = getLabel();
	return (l == null) ? null : l.getText();
    }
}
