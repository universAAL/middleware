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
package org.universAAL.middleware.io.rdf;

import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.rdf.Resource;

/**
 * The type of possible values for {@link FormControl#PROP_CONTROL_LABEL}. A
 * label reveals the intent of the corresponding form control for human users
 * primarily as a string. This string is stored using the standard property
 * rdfs:label (see {@link org.universAAL.middleware Resource#getResourceLabel()}
 * and {@link org.universAAL.middleware Resource#setResourceLabel(String)}).
 * 
 * @see http://www.w3.org/TR/rdf-schema/#ch_label
 * 
 * @author mtazari
 */
public class Label extends Resource {
    public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE + "Label";

    /**
     * The URL of media objects that can be used as an audio / visual icon
     * equivalent to the meaning borne by the text label.
     */
    public static final String PROP_ICON_URL = Form.uAAL_DIALOG_NAMESPACE
	    + "iconURL";

    static {
	addResourceClass(MY_URI, Label.class);
    }

    /**
     * For use by de-serializers only.
     */
    public Label() {
	super();
    }

    /**
     * Constructs a new label.
     * 
     * @param labelText
     *            Mandatory string bearing the intent of the corresponding form
     *            control for human users and stored using the standard property
     *            rdfs:label.
     * @param iconURL
     *            See {@link #PROP_ICON_URL}; optional.
     * 
     * @see http://www.w3.org/TR/rdf-schema/#ch_label
     */
    public Label(String labelText, String iconURL) {
	super();
	addType(getStaticFieldValue("MY_URI", MY_URI).toString(), true);
	setResourceLabel(labelText);
	if (iconURL != null)
	    props.put(PROP_ICON_URL, iconURL);
    }

    /**
     * @see #PROP_ICON_URL
     */
    public String getIconURL() {
	return (String) props.get(PROP_ICON_URL);
    }

    int getMaxLength() {
	String l = getText();
	return (l == null) ? -1 : l.length();
    }

    /**
     * Returns the label string.
     * 
     * @see org.universAAL.middleware Resource#getResourceLabel()
     */
    public String getText() {
	return getResourceLabel();
    }

    /**
     * @see org.universAAL.middleware Resource#setProperty(String, Object)
     */
    public void setProperty(String propURI, Object value) {
	if (PROP_ICON_URL.equals(propURI)) {
	    if (!props.containsKey(propURI) && value instanceof String)
		props.put(propURI, value);
	} else
	    super.setProperty(propURI, value);
    }

    /**
     * Overrides {@link org.universAAL.middleware Resource#toString()} in order
     * to return the label string; only if this is an empty string, the default
     * implementation is used.
     */
    public String toString() {
	String txt = getText();
	return StringUtils.isNullOrEmpty(txt) ? super.toString() : txt;
    }
}
