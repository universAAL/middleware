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

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.supply.LevelRating;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.ui.impl.UIBusImpl;

/**
 * The structural unit of forms that serves as a container for other form
 * controls.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @navassoc "" children * FormControl
 */
public class Group extends FormControl {
    public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE + "Group";

    /**
     * For each group, the list of form controls that have been created with
     * that group as their direct container (parent) in the order of their
     * creation.
     */
    public static final String PROP_CHILDREN = Form.uAAL_DIALOG_NAMESPACE
	    + "groupChildren";

    static final String STD_IO_CONTROLS = "ioControlsGroup";
    static final String STD_SUBMITS = "submitsGroup";
    static final String STD_STD_BUTTONS = "stdButtonsGroup";

    private int level = -1, numSubgroups = -1;
    private boolean hasInput = false, hasOutput = false;
    private LevelRating complexity;

    /**
     * For use by de-serializers only.
     */
    public Group() {
	super();
    }

    /**
     * Constructs a new group. The last three parameters will be ignored if the
     * parent is a {@link Repeat} control.
     * 
     * @param parent
     *            The mandatory parent group as the direct container of this
     *            group. See {@link FormControl#PROP_PARENT_CONTROL}.
     * @param label
     *            The optional {@link Label} to be associated with this group.
     *            See {@link FormControl#PROP_CONTROL_LABEL}.
     * @param ref
     *            See {@link FormControl#PROP_REFERENCED_PPATH}.
     * @param valueRestriction
     *            See {@link Input#PROP_VALUE_RESTRICTION}. Because Groups may
     *            contain input controls, you may specify here a
     *            {@link org.universAAL.middleware.owl.MergedRestriction} to let
     *            the dialog package to derive the value restrictions for any
     *            input control whose {@link FormControl#PROP_REFERENCED_PPATH}
     *            starts with the path given for <code>ref</code> (the previous
     *            parameter in this constructor). If <code>ref</code> is
     *            <code>null</code>, this parameter will be ignored.
     * @param initialValue
     *            A {@link org.universAAL.middleware.rdf.Resource} to be used as
     *            the initial value for this group. If this is specified, then
     *            the initial values of any other control whose
     *            {@link FormControl#PROP_REFERENCED_PPATH} starts with the path
     *            given for the above <code>ref</code> might be derivable from
     *            this value.
     */
    public Group(Group parent, Label label, PropertyPath ref,
	    MergedRestriction valueRestriction, Resource initialValue) {
	super(MY_URI, parent, label, ref, valueRestriction, initialValue);
    }

    protected Group(String typeURI, Group parent, Label label,
	    PropertyPath ref, MergedRestriction valueRestriction,
	    Object initialValue) {
	super(typeURI, parent, label, ref, valueRestriction, initialValue);
    }

    Group(String label, Form theForm) {
	super(MY_URI, theForm, new Label(label, null), null, null, null);
    }

    void addChild(FormControl child) {
	if (child == null)
	    return;

	List children = (List) props.get(PROP_CHILDREN);
	if (children == null) {
	    children = new ArrayList();
	    props.put(PROP_CHILDREN, children);
	} else {
	    Object o = props.get(PROP_PARENT_CONTROL);
	    if (o instanceof Form) {
		// this is the root group
		if (((Form) o).isStandardDialog()) {
		    if (children.size() == 3)
			// dialogs may have only 3 children that are created
			// automatically by Form.newDialog
			throw new UnsupportedOperationException(
				"Not allowed to add children to the root group!");
		} else if (children.size() == 2)
		    // all other dialogs may have only 2 children that are
		    // created
		    // automatically by Form.newSysMenu, Form.newSubdialog or
		    // Form.newMessage
		    throw new UnsupportedOperationException(
			    "Not allowed to add children to the root group!");
	    }
	}

	children.add(child);
    }

    private void addSubtree(List l, Submit s) {
	List children = (List) props.get(PROP_CHILDREN);
	if (children == null || children.isEmpty())
	    return;

	for (Iterator i = children.iterator(); i.hasNext();) {
	    FormControl fc = (FormControl) i.next();
	    if (fc instanceof Group && !(fc instanceof Repeat)) {
		l.add(fc);
		((Group) fc).addSubtree(l, s);
	    } else if (!(fc instanceof Submit)
		    || fc instanceof SubdialogTrigger)
		l.add(fc);
	}
    }

    private PropertyPath constructSubpath(String subprop) {
	PropertyPath pp = getReferencedPPath();
	if (pp == null)
	    return null;

	String[] subpath, path = pp.getThePath();
	if (path == null || path.length == 0)
	    return null;

	subpath = new String[path.length + 1];
	for (int i = 0; i < path.length; i++)
	    subpath[i] = path[i];
	subpath[path.length] = subprop;
	return new PropertyPath(null, false, subpath);
    }

    /**
     * Based on the knowledge about the type of this group (see
     * {@link #getTypeURI()}), this method generates a hierarchy of form
     * controls that reflects the exact structure known from the type of this
     * group. Info about the structure of the type of this group will be
     * available only if {@link #getTypeURI()} returns a subclass of
     * {@link org.universAAL.middleware.owl.ManagedIndividual ManagedIndividual}
     * ; then, the structural info will be retrieved based on
     * {@link org.universAAL.middleware.owl.OntClassInfo#getDeclaredProperties()
     * the standard properties of that class} and
     * {@link org.universAAL.middleware.owl.ManagedIndividual#getClassRestrictionsOnProperty(String,String)
     * the class restrictions on them}. Properties with insufficient
     * restrictions will be ignored. A property of type
     * {@link org.universAAL.middleware.owl.ManagedIndividual ManagedIndividual}
     * will create a subgroup; a property with a fixed set of values in its
     * range will create a {@link Select} or {@link Select1} control, depending
     * on possibly known cardinalities; all other properties will create an
     * {@link InputField}. All of the above may be packed in a {@link Repeat}
     * control, if the corresponding property allows more than one value.
     */
    public void doModelBasedExpansion() {
	String t = getTypeURI();
	if (t == null) {
	    LogUtils.logWarn(UIBusImpl.getModuleContext(), Group.class,
		    "doModelBasedExpansion",
		    new Object[] { "missing model ref!" }, null);
	    return;
	}

	String[] props = OntologyManagement.getInstance().getOntClassInfo(t)
		.getDeclaredProperties();
	if (props == null || props.length == 0) {
	    LogUtils.logWarn(UIBusImpl.getModuleContext(), Group.class,
		    "doModelBasedExpansion",
		    new Object[] { "not a ManagedIndividual!" }, null);
	    return;
	}

	for (int i = 0; i < props.length; i++) {
	    MergedRestriction r = ManagedIndividual
		    .getClassRestrictionsOnProperty(t, props[i]);
	    if (r == null) {
		LogUtils
			.logWarn(
				UIBusImpl.getModuleContext(),
				Group.class,
				"doModelBasedExpansion",
				new Object[] {
					"ignoring a property with unknown restrictions: ",
					props[i] }, null);
		continue;
	    }
	    Object[] values = r.getEnumeratedValues();
	    if (values == null || values.length == 0) {
		String tt = r.getPropTypeURI();
		if (tt == null)
		    LogUtils
			    .logWarn(
				    UIBusImpl.getModuleContext(),
				    Group.class,
				    "doModelBasedExpansion",
				    new Object[] {
					    "ignoring a property with insufficient restrictions: ",
					    props[i] }, null);
		else if (OntologyManagement.getInstance().isRegisteredClass(tt,
			true)) {
		    Group g = new Group(this, new Label(StringUtils
			    .deriveLabel(props[i]), null),
			    constructSubpath(props[i]), null, null);
		    g.doModelBasedExpansion();
		} else if (TypeMapper.isRegisteredDatatypeURI(tt))
		    new InputField(this, new Label(StringUtils
			    .deriveLabel(props[i]), null),
			    constructSubpath(props[i]), null, null);
		else
		    LogUtils.logWarn(UIBusImpl.getModuleContext(), Group.class,
			    "doModelBasedExpansion", new Object[] {
				    "ignoring a property with unknown type: ",
				    props[i] }, null);
	    } else {
		Select s = (r.getMinCardinality() > 1 || r.getMaxCardinality() != 1) ? new Select(
			this,
			new Label(StringUtils.deriveLabel(props[i]), null),
			constructSubpath(props[i]), null, null)
			: new Select1(this, new Label(StringUtils
				.deriveLabel(props[i]), null),
				constructSubpath(props[i]), null, null);
		s.generateChoices(values);
	    }
	}
    }

    /**
     * @see #PROP_CHILDREN
     */
    public FormControl[] getChildren() {
	List children = (List) props.get(PROP_CHILDREN);
	if (children == null)
	    return new FormControl[0];
	return (FormControl[]) children
		.toArray(new FormControl[children.size()]);
    }

    /**
     * This should help UI handlers to decide how to "render" a group. A group
     * with no subgroups is assumed to have no complexity. Up to 3 non-complex
     * subgroups cause low complexity. More than this or up to 3 subgroups with
     * low complexity cause meddle complexity. More than this or up to 3
     * subgroups with middle complexity cause high complexity. In all other
     * cases the group is assumed to be fully complex.
     * 
     * @return The complexity of this group as a value between
     *         {@link LevelRating#none} and {@link LevelRating#full}.
     */
    public LevelRating getComplexity() {
	if (level == -1)
	    getFormObject().finalizeGroupStructure();
	return complexity;
    }

    Output[] getFirstLevelOutputs() {
	List children = (List) props.get(PROP_CHILDREN);
	if (children == null || children.isEmpty())
	    return new Output[0];

	int subgroups = 0;
	Group subgroup = null;
	ArrayList al = new ArrayList();
	for (int i = 0; i < children.size(); i++)
	    if (children.get(i) instanceof Output)
		al.add(children.get(i));
	    else if (children.get(i) instanceof Group) {
		subgroups++;
		subgroup = (Group) children.get(i);
	    }

	if (subgroups == 1) {
	    children = (List) subgroup.props.get(PROP_CHILDREN);
	    for (int i = 0; i < children.size(); i++)
		if (children.get(i) instanceof Output)
		    al.add(children.get(i));
	}

	return (Output[]) al.toArray(new Output[al.size()]);
    }

    /**
     * Returns the level of this group in the hierarchical structure of a form.
     * The standard groups of a form (see the documentation of the class
     * {@link Form}) have the level 1. Their direct subgroups have the level 2,
     * and so forth.
     */
    public int getHierarchyLevel() {
	if (level == -1)
	    getFormObject().finalizeGroupStructure();
	return level;
    }

    /**
     * Overrides {@link FormControl#getMaxLength()} by returning always -1,
     * because no standard string representation of a group exists.
     */
    public int getMaxLength() {
	// not applicable
	return -1;
    }

    /**
     * Returns the number of direct subgroups in this group.
     */
    public int getNumberOfSubgroups() {
	if (level == -1)
	    getFormObject().finalizeGroupStructure();
	return numSubgroups;
    }

    MergedRestriction getPPathRestriction(String[] pp) {
	Object parent = props.get(PROP_PARENT_CONTROL);
	if (parent instanceof Group)
	    return ((Group) parent).getPPathRestriction(pp);
	else if (parent instanceof Form)
	    return ((Form) parent).getPPathRestriction(pp);
	else
	    return null;
    }

    FormControl[] getSubtree(Submit s) {
	ArrayList result = new ArrayList();
	addSubtree(result, s);
	return (FormControl[]) result.toArray(new FormControl[result.size()]);
    }

    String getTypeURI(String[] pp) {
	Object r = props.get(PROP_VALUE_RESTRICTION);
	Object mypp = props.get(PROP_REFERENCED_PPATH);
	if (r instanceof MergedRestriction
		&& mypp instanceof PropertyPath
		&& PropertyPath.pathHasPrefix(pp, ((PropertyPath) mypp)
			.getThePath())) {
	    String[] subpath = PropertyPath.getSubpath(pp,
		    ((PropertyPath) mypp).getThePath().length - 1);
	    MergedRestriction target = ((MergedRestriction) r)
		    .getRestrictionOnPath(subpath);
	    if (target != null) {
		String type = target.getPropTypeURI();
		if (type != null)
		    return type;
	    }
	}

	Object parent = props.get(PROP_PARENT_CONTROL);
	if (parent instanceof Group)
	    return ((Group) parent).getTypeURI(pp);
	else if (parent instanceof Form)
	    return ((Form) parent).getTypeURI(pp);
	else
	    return null;
    }

    Object getValue(String[] pp) {
	Object parent = props.get(PROP_PARENT_CONTROL);
	if (parent instanceof Group)
	    return ((Group) parent).getValue(pp);
	else if (parent instanceof Form)
	    return ((Form) parent).getValue(pp);
	else
	    return null;
    }

    /**
     * Checks if there are any input controls in the subtree rooted at this
     * group.
     */
    public boolean hasInput() {
	if (level == -1)
	    getFormObject().finalizeGroupStructure();
	return hasInput;
    }

    /**
     * Checks if there are any output controls in the subtree rooted at this
     * group.
     */
    public boolean hasOutput() {
	if (level == -1)
	    getFormObject().finalizeGroupStructure();
	return hasOutput;
    }

    boolean hasSubgroup(String label) {
	if (label == null)
	    return false;
	List children = (List) props.get(PROP_CHILDREN);
	if (children == null)
	    return false;
	for (Iterator i = children.iterator(); i.hasNext();) {
	    Object o = i.next();
	    if (o instanceof Group
		    && label.equals(((Group) o).getLabel().getText()))
		return true;
	}
	return false;
    }

    boolean hasValue(String[] pp) {
	Object parent = props.get(PROP_PARENT_CONTROL);
	if (parent instanceof Group)
	    return ((Group) parent).hasValue(pp);
	return (parent instanceof Form && ((Form) parent).getValue(pp) != null);
    }

    /**
     * Checks if this group is one of the standard groups in the form (see the
     * documentation of the class {@link Form}).
     */
    public boolean isRootGroup() {
	return getParentGroup().getProperty(PROP_PARENT_CONTROL) instanceof Form;
    }

    void setStructuralProps(int level) {
	this.level = level++;

	numSubgroups = 0;
	hasInput = hasOutput = false;
	int[] complexity = new int[] { 0, 0, 0, 0, 0 };

	List children = (List) props.get(PROP_CHILDREN);
	if (children != null)
	    for (Iterator i = children.iterator(); i.hasNext();) {
		Object o = i.next();
		if (o instanceof Group) {
		    ((Group) o).setStructuralProps(level);
		    complexity[((Group) o).getComplexity().ord()]++;
		    if (((Group) o).hasInput())
			hasInput = true;
		    if (((Group) o).hasOutput())
			hasOutput = true;
		    numSubgroups++;
		} else if (o instanceof Input)
		    hasInput = true;
		else if (o instanceof Output)
		    hasOutput = true;
	    }

	if (complexity[LevelRating.FULL] > 0
		|| complexity[LevelRating.HIGH] > 0
		|| complexity[LevelRating.MIDDLE] > 3)
	    this.complexity = LevelRating.full;
	else if (complexity[LevelRating.LOW] > 3
		|| complexity[LevelRating.MIDDLE] > 0)
	    this.complexity = LevelRating.high;
	else if (complexity[LevelRating.NONE] > 3
		|| complexity[LevelRating.LOW] > 0)
	    this.complexity = LevelRating.middle;
	else if (complexity[LevelRating.NONE] > 0)
	    this.complexity = LevelRating.low;
	else
	    this.complexity = LevelRating.none;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object)
     */
    public boolean setProperty(String propURI, Object value) {
	if (PROP_CHILDREN.equals(propURI)) {
	    if (props.containsKey(propURI))
		return false;
	    else if (value instanceof List) {
		for (Iterator i = ((List) value).iterator(); i.hasNext();)
		    if (!(i.next() instanceof FormControl))
			return false;
		props.put(propURI, value);
		return true;
	    } else if (value instanceof FormControl) {
		List l = new ArrayList(1);
		l.add(value);
		props.put(propURI, l);
		return true;
	    }
	    return false;
	} else
	    return super.setProperty(propURI, value);
    }

    boolean setValue(String[] pp, Object value,
	    MergedRestriction valueRestrictions) {
	Object parent = props.get(PROP_PARENT_CONTROL);
	if (parent instanceof Group)
	    return ((Group) parent).setValue(pp, value, valueRestrictions);
	else if (parent instanceof Form)
	    return ((Form) parent).setValue(pp, value, valueRestrictions);
	else
	    return false;
    }
}
