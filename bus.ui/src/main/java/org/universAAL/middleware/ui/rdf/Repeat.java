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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * A subclass of {@link Group} whose children are all of the same type. Hence it
 * allows only one child as a pseudo placeholder for all the repeatable form
 * controls of that single type. Consequently, calling
 * {@link FormControl#getValue()} on repeat controls returns always an instance
 * of {java.util.List} containing a list of objects with the same type. An
 * implication hereof is that also the initial value for repeat controls must be
 * a list.
 * <p>
 * As direct child of a repeat control, neither {@link Submit} nor repeat
 * controls are allowed. If this single pseudo placeholder is then a
 * {@link Group} control, then the children of that group play quasi the role of
 * columns in a virtual table whose rows are the entries of the list returned by
 * {@link FormControl#getValue()}. Otherwise, the Repeat object represents a one
 * column list of simple values.
 * <p>
 * As the single child control of a Repeat object is only a pseudo placeholder
 * control, its {@link FormControl#PROP_REFERENCED_PPATH} property must be null.
 * If this single child is a {@link Group} control, then the reference path for
 * all of the form controls in the subtree rooted at the pseudo child must have
 * a value relative to that repeat control and not relative to the root of the
 * form data, as it must be in all other cases.
 * <p>
 * This implementation uses a fixed selection model (confer Java standard
 * selection models like {@link javax.swing.ListSelectionModel} and
 * {@link javax.swing.tree.TreeSelectionModel}), where the single child control
 * of it represents always the selected list entry. A selection index points to
 * the corresponding entry in the list returned by {@link #getValue()}. If this
 * index is out of range (normally -1), then the data held by the single child
 * control is understood as candidate for being added to the list of values (see
 * {@link #addValue()}).
 * <p>
 * The above mentioned selection model does not provide any eventing mechanism
 * because the change of the selection occurs only through public methods of the
 * class (which means that there is no automatic change of selection) and also
 * it is assumed that instances of this class are not used in parallel so that
 * the component having the control over it is the only source of selection
 * change (by calling appropriate methods) that does not need to be notified.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class Repeat extends Group {
	/**
	 * Indicates if entries can be removed from the list of initial values
	 * associated with a repeat control.
	 */
	public static final String PROP_IS_DELETABLE = Form.uAAL_DIALOG_NAMESPACE
			+ "listEntriesDeletable";

	/**
	 * Indicates if entries in the list of initial values associated with a
	 * repeat control can be edited.
	 */
	public static final String PROP_IS_EDITABLE = Form.uAAL_DIALOG_NAMESPACE
			+ "listEntriesEditable";

	/**
	 * Indicates if new entries can be added to the list of initial values
	 * associated with a repeat control.
	 */
	public static final String PROP_IS_EXPANDABLE = Form.uAAL_DIALOG_NAMESPACE
			+ "listAcceptsNewEntries";

	/**
	 * The form control in the repeat that plays the role of a searchable column
	 * in the sense that the I/O handler should provide an additional input
	 * field where the user can enter text to be used to select a specific entry
	 * in the list of values associated with a repeat control.
	 */
	public static final String PROP_SEARCHABLE_FIELD = Form.uAAL_DIALOG_NAMESPACE
			+ "searchableField";

	private List values = null;
	private Object selection = null;
	private int selectionIndex = -1;
	private boolean submissionChecked = false;

	/**
	 * For exclusive use by de-serializers.
	 */
	public Repeat() {
		super();
	}

	/**
	 * Constructs a new repeat control.
	 * 
	 * @param parent
	 *            The mandatory parent group as the direct container of this
	 *            repeat control. See {@link FormControl#PROP_PARENT_CONTROL}.
	 * @param label
	 *            The optional {@link Label} to be associated with this input
	 *            field. See {@link FormControl#PROP_CONTROL_LABEL}.
	 * @param ref
	 *            See {@link FormControl#PROP_REFERENCED_PPATH}; mandatory.
	 * @param valueRestriction
	 *            See {@link Input#PROP_VALUE_RESTRICTION}. Because repeat
	 *            controls may contain input controls, you may specify here a
	 *            {@link org.universAAL.middleware.owl.Restriction} to let the
	 *            dialog package to derive the value restrictions for contained
	 *            input controls if the form data does not bear the required
	 *            info or if you want to define more restrictions compared to
	 *            that model-based restrictions.
	 * @param initialValue
	 *            A {@link java.util.List} to be used as the initial value for
	 *            this repeat.
	 */
	public Repeat(Group parent, Label label, PropertyPath ref,
		MergedRestriction valueRestriction, List initialValue) {
		super(MY_URI, parent, label, ref, valueRestriction, initialValue);

		if (getMaxCardinality() == 1)
			throw new IllegalArgumentException(
					"Wrong restrictions disallowing repeat!");

		values = initialValue;
	}

	void addChild(FormControl child) {
		if (child == null || child instanceof Submit || child instanceof Repeat)
			throw new IllegalArgumentException(
					"Only Input, Output, and Group instances are allowed!");

		List children = (List) props.get(PROP_CHILDREN);
		if (children == null) {
			children = new ArrayList();
			props.put(PROP_CHILDREN, children);
		} else if (children.size() > 0)
			throw new UnsupportedOperationException(
					"Not allowed to add more than one child to a Repeat object!");

		child.changeProperty(PROP_REFERENCED_PPATH, null);
		children.add(child);
	}

	/**
	 * Adds the value held by the single pseudo child control to the end of the
	 * list of values associated with this repeat control. Note: changes to the
	 * selection itself (achieved by calling
	 * {@link Input#storeUserInput(Object)} on child controls of a reapt) are
	 * only local until either this method or {@link #updateSelection()} is
	 * called.
	 * 
	 * @return true, if either there is no selection or the value differs from
	 *         the selected entry in the the list of values currently associated
	 *         with this repeat control. Otherwise it does not add the value and
	 *         returns false.
	 */
	public boolean addValue() {
		checkValues();

		if (selection != null && !values.contains(selection)
				&& (selectionIndex == -1 || valuesDiffer())) {
			selectionIndex = values.size();
			values.add(selection);
			if (selection instanceof Resource)
				selection = ((Resource) selection).deepCopy();
			submissionChecked = false;
			return true;
		}
		return false;
	}

	/**
	 * If applications have provided a list as initial data for this repeat
	 * control, they can forbid the addition of new values to that list by
	 * calling this method.
	 * 
	 * @see #PROP_IS_EXPANDABLE
	 */
	public void banEntryAddition() {
		props.put(PROP_IS_EXPANDABLE, Boolean.FALSE);
	}

	/**
	 * If applications have provided a list as initial data for this repeat
	 * control, they can forbid the deletion of values from that list by calling
	 * this method.
	 * 
	 * @see #PROP_IS_DELETABLE
	 */
	public void banEntryDeletion() {
		props.put(PROP_IS_DELETABLE, Boolean.FALSE);
	}

	/**
	 * If applications have provided a list as initial data for this repeat
	 * control, they can forbid the edition of existing values in that list by
	 * calling this method.
	 * 
	 * @see #PROP_IS_EDITABLE
	 */
	public void banEntryEdit() {
		props.put(PROP_IS_EDITABLE, Boolean.FALSE);
	}

	boolean checkSubmission() {
		if (!submissionChecked) {
			checkValues();

			try {
				submissionChecked = getFormObject().setValue(
						getReferencedPPath().getThePath(), values,
						(MergedRestriction) props.get(PROP_VALUE_RESTRICTION));
			} catch (Exception e) {
				submissionChecked = false;
			}
		}
		return submissionChecked;
	}

	private synchronized void checkValues() {
		if (values == null) {
			Object o = super.getValue();
			if (o instanceof List)
				values = (List) o;
			else {
				values = new ArrayList();
				if (o != null)
					values.add(o);
			}
		}
	}

	/**
	 * If the given form control is a column in this repeat control, this method
	 * returns the list of all values in that column in the order of their
	 * appearance in the list of values associated with this repeat control.
	 * Empty cells will add a null value to the returned list.
	 */
	public List getAllValues(FormControl fc) {
		return (fc == null) ? null : getAllValues(fc.getReferencedPPath());
	}

	List getAllValues(PropertyPath pp) {
		checkValues();

		String[] thePath = (pp == null) ? null : pp.getThePath();
		boolean multiColumn = thePath != null && thePath.length > 0;
		List result = new ArrayList(values.size());
		for (Iterator i = values.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o != null && multiColumn != (o instanceof Resource))
				return null;
			if (multiColumn)
				result.add(Form.getValue(thePath, (Resource) o));
			else
				result.add(o);
		}

		return result;
	}

	/**
	 * Returns the maximum number of values that can be associated with this
	 * repeat control. A negative integer is returned if there is no upper
	 * limit.
	 */
	public int getMaxCardinality() {
	    MergedRestriction r = getRestrictions();
		return (r == null) ? -1 : r.getMaxCardinality();
	}

	/**
	 * Returns the minimum number of values that must be associated with this
	 * repeat control. A non-positive integer is returned if there is no lower
	 * limit.
	 */
	public int getMinCardinality() {
	    MergedRestriction r = getRestrictions();
		return (r == null) ? -1 : r.getMinCardinality();
	}

	/**
	 * Returns the number of entries currently existing in the list of values
	 * associated with this repeat control.
	 */
	public int getNumberOfValues() {
		checkValues();

		return values.size();
	}

	MergedRestriction getPPathRestriction(String[] pp) {
	    MergedRestriction r = getRestrictions();

		if (r != null) {
			if (pp == null || pp.length == 0)
				return r.copyWithNewCardinality(1, 1);

			checkValues();

			if (values.isEmpty() || !(values.get(0) instanceof Resource))
				return Form.getPPathRestriction(pp, 0, r.getPropTypeURI());
		} else
			checkValues();

		return Form.getPPathRestriction(pp, (Resource) values.get(0));
	}

	/**
	 * @see #PROP_SEARCHABLE_FIELD
	 */
	public FormControl getSearchableField() {
		Object o = props.get(PROP_SEARCHABLE_FIELD);
		return (o instanceof FormControl) ? (FormControl) o : null;
	}

	/**
	 * Returns the index of the current selection in the list of values
	 * associated with this repeat control. If there is no selection, returns a
	 * negative number.
	 */
	public int getSelectionIndex() {
		return selectionIndex;
	}

	String getTypeURI(String[] pp) {
		MergedRestriction r = getPPathRestriction(pp);
		return (r == null) ? null : r.getPropTypeURI();
	}

	/**
	 * Returns the value that can be reached by the given path starting from the
	 * current selection.
	 */
	public Object getValue(String[] pp) {
		return (pp == null || pp.length == 0) ? selection
				: (selection instanceof Resource) ? Form.getValue(pp,
						(Resource) selection) : null;
	}

	boolean hasValue(String[] pp) {
		checkValues();

		if (values.isEmpty())
			return false;

		if (pp == null || pp.length == 0)
			return TypeMapper.getDatatypeURI(values.get(0)) != null;

		for (Iterator i = values.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof Resource
					&& Form.getValue(pp, (Resource) o) != null)
				return true;
		}

		return false;
	}

	/**
	 * @see #PROP_IS_EXPANDABLE
	 */
	public boolean listAcceptsNewEntries() {
		return !Boolean.FALSE.equals(props.get(PROP_IS_EXPANDABLE));
	}

	/**
	 * @see #PROP_IS_DELETABLE
	 */
	public boolean listEntriesDeletable() {
		return !Boolean.FALSE.equals(props.get(PROP_IS_DELETABLE));
	}

	/**
	 * @see #PROP_IS_EDITABLE
	 */
	public boolean listEntriesEditable() {
		return !Boolean.FALSE.equals(props.get(PROP_IS_EDITABLE));
	}

	/**
	 * If there is a selection that is not the last element in the list of
	 * values associated with this repeat control, its place will be exchanged
	 * with the next list element (the element whose index is equal to the
	 * selection index plus 1). Can be used for sorting the list of values
	 * associated with this repeat control.
	 */
	public void moveSelectionDown() {
		if (values != null && selectionIndex > -1
				&& selectionIndex < values.size() - 1) {
			int i = selectionIndex++;
			Object o = values.get(selectionIndex);
			values.set(selectionIndex, values.get(i));
			values.set(i, o);
		}
	}

	/**
	 * If there is a selection that is not the first element in the list of
	 * values associated with this repeat control (its index is greater than 0),
	 * its place will be exchanged with the previous list element (the element
	 * whose index is equal to the selection index minus 1). Can be used for
	 * sorting the list of values associated with this repeat control.
	 */
	public void moveSelectionUp() {
		if (selectionIndex > 0 && selectionIndex < values.size()) {
			int i = selectionIndex--;
			Object o = values.get(selectionIndex);
			values.set(selectionIndex, values.get(i));
			values.set(i, o);
		}
	}

	private Object newValue() {
		String type = getTypeURI();
		if (type == null) {
			List children = (List) props.get(PROP_CHILDREN);
			if (children == null || children.size() != 1)
				throw new IllegalStateException("Repeat object in wrong state!");
			if (children.get(0) instanceof Group)
				return new Resource();
		} else if (TypeMapper.isRegisteredDatatypeURI(type)) {
			Resource pr = ManagedIndividual.getInstance(type, null);
			if (pr == null) {
				pr = new Resource();
				pr.addType(type, false);
			}
			return pr;
		}
		return null;
	}

	/**
	 * Removes the current selection from the list of values associated with
	 * this repeat control. After this operation, there will be no selection and
	 * it must be set explicitly by calling {@link #setSelection(int)}.
	 * 
	 * @return true, if there was a selection; false, otherwise.
	 */
	public boolean removeSelection() {
		if (selectionIndex > -1 && selectionIndex < values.size()) {
			values.remove(selectionIndex);
			submissionChecked = false;
			selectionIndex = -1;
			selection = null;
			return true;
		}
		return false;
	}

	/**
	 * @see #PROP_SEARCHABLE_FIELD
	 */
	public void setSearchableField(FormControl fc) {
		if (fc != null)
			props.put(PROP_SEARCHABLE_FIELD, fc);
	}

	/**
	 * Changes the current selection to point to the element that has the given
	 * index (the parameter i) in the list of values associated with this
	 * repeat. If the given index is out of range, the effect will be equivalent
	 * to de-selecting any current selection.
	 */
	public void setSelection(int i) {
		checkValues();

		if (i < 0 || i >= values.size()) {
			selection = newValue();
			selectionIndex = -1;
		} else {
			selection = values.get(i);
			if (selection instanceof Resource)
				selection = ((Resource) selection).deepCopy();
			selectionIndex = i;
		}
	}

	boolean setValue(String[] pp, Object value, MergedRestriction valueRestrictions) {
		if (selection == null)
			selection = newValue();

		if (selection instanceof Resource)
			return Form.setValue((Resource) selection, pp, value,
					valueRestrictions);
		else if (pp == null || pp.length == 0) {
			if (value == null) {
				selection = null;
				selectionIndex = -1;
			} else {
				Object aux = props.get(PROP_REFERENCED_PPATH);
				String prop = (aux instanceof PropertyPath) ? ((PropertyPath) aux)
						.getLastPathElement()
						: null;
				if (prop == null || valueRestrictions != null)
					// a single input child does not need any value restrictions
					// such value restriction must be added to the Repeat object
					// itself
					// so, this case is a sign of inconsistent state
					// TODO: add a log entry!
					return false;
				valueRestrictions = getRestrictions();
				Resource dummy = new Resource();
				dummy.setProperty(prop, value);
				if (valueRestrictions != null
						&& !valueRestrictions.hasMemberIgnoreCardinality(dummy))
					return false;
				selection = value;
				return true;
			}
		}
		return false;
	}

	/**
	 * If there is a valid selection, the local changes to it will be reflected
	 * in the list of values associated with this repeat control. In this case,
	 * it returns true, otherwise null.
	 */
	public boolean updateSelection() {
		if (selection != null && selectionIndex > -1
				&& selectionIndex < values.size()) {
			values.set(selectionIndex, selection);
			if (selection instanceof Resource)
				selection = ((Resource) selection).deepCopy();
			submissionChecked = false;
			return true;
		}

		return false;
	}

	private boolean valuesDiffer() {
		if (selection instanceof Resource) {
			Object o = values.get(selectionIndex);
			for (Enumeration e = ((Resource) selection).getPropertyURIs(); e
					.hasMoreElements();) {
				String key = e.nextElement().toString();
				if (!((Resource) selection).getProperty(key).equals(
						((Resource) o).getProperty(key)))
					return true;
			}
			return false;
		} else
			return true;
	}
}
