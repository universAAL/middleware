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

/**
 * A special case of {@link Label labels} accepting other choice
 * {@link ChoiceItem items} and lists as its children and hence serving as an
 * inner node in a hierarchy of possible choices. Selecting the label of a
 * choice list must make its children accessible.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 * @navassoc "" children "*" Label
 */
public class ChoiceList extends Label {
    public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE
	    + "ChoiceList";

    /**
     * Property for accessing the list of items and sublists contained in a
     * choice list.
     */
    public static final String PROP_CHILDREN = Form.uAAL_DIALOG_NAMESPACE
	    + "subchoices";

    private List children;

    /**
     * For use by de-serializers only.
     */
    public ChoiceList() {
	super();
    }

    /**
     * Constructs a new choice list.
     * 
     * @param labelText
     *            see {@link Label#Label(String, String)}
     * @param iconURL
     *            see {@link Label#Label(String, String)}
     */
    public ChoiceList(String labelText, String iconURL) {
	super(labelText, iconURL);
	children = new ArrayList();
	props.put(PROP_CHILDREN, children);
    }

    /**
     * Adds an item to this choice list.
     * 
     * @param item
     *            The item to be added to this choice list.
     */
    public void addChild(ChoiceItem item) {
	if (item != null)
	    children.add(item);
    }

    /**
     * Adds a sublist to this choice list.
     * 
     * @param sublist
     *            The sublist to be added to this choice list.
     */
    public void addChild(ChoiceList sublist) {
	if (sublist != null)
	    children.add(sublist);
    }

    /**
     * Returns the items and sublists contained in this choice list in the order
     * of their addition to the list.
     */
    public Label[] getChildren() {
	return (Label[]) children.toArray(new Label[children.size()]);
    }

    ChoiceItem findItem(String label) {
	ChoiceItem result = null;
	if (children != null)
	    for (Iterator i = children.iterator(); result == null
		    && i.hasNext();) {
		Object child = i.next();
		if (child instanceof ChoiceList)
		    result = ((ChoiceList) child).findItem(label);
		else if (child instanceof ChoiceItem
			&& child.toString().equals(label))
		    return (ChoiceItem) child;
	    }
	return result;
    }

    /**
     * @see org.universAAL.middleware.ui.rdf.Label#getMaxLength()
     */
    int getMaxLength() {
	int res = -1;
	for (Iterator i = children.iterator(); i.hasNext();) {
	    Object o = i.next();
	    if (o instanceof Label) {
		int aux = ((Label) o).getMaxLength();
		if (aux > res)
		    res = aux;
	    }
	}
	return res;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object)
     */
    public void setProperty(String propURI, Object value) {
	if (PROP_CHILDREN.equals(propURI))
	    if (value instanceof List && children == null) {
		for (Iterator i = ((List) value).iterator(); i.hasNext();) {
		    Object o = i.next();
		    if (!(o instanceof ChoiceItem)
			    && !(o instanceof ChoiceList))
			return;
		}
		children = (List) value;
		props.put(PROP_CHILDREN, children);
	    } else
		return;
	else
	    super.setProperty(propURI, value);
    }
}
