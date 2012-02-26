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

import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * The default {@link Output} control containing info in form of plain text or
 * another simple type supported by
 * {@link org.universAAL.middleware.rdf.TypeMapper}.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class SimpleOutput extends Output {
    public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE
	    + "SimpleOutput";

    /**
     * For local storage of the contained info if this info is not accessible
     * from the form data.
     */
    public static final String PROP_CONTENT = Form.uAAL_DIALOG_NAMESPACE
	    + "theContent";

    /**
     * For exclusive use by de-serializers.
     */
    public SimpleOutput() {
	super();
    }

    /**
     * For exclusive use by applications.
     * 
     * @param parent
     *            The mandatory parent group as the direct container of this
     *            input field. See {@link FormControl#PROP_PARENT_CONTROL}.
     * @param label
     *            The optional {@link Label} to be associated with this input
     *            field. See {@link FormControl#PROP_CONTROL_LABEL}.
     * @param ref
     *            See {@link FormControl#PROP_REFERENCED_PPATH}; optional.
     * @param content
     *            The contained info. If the previous parameter (
     *            <code>ref</code>) is not null, it will be stored as part of
     *            the form data, otherwise locally using {@link #PROP_CONTENT}.
     */
    public SimpleOutput(Group parent, Label label, PropertyPath ref,
	    Object content) {
	super(MY_URI, parent, label, ref, (ref == null) ? null : content);
	boolean hasValue = hasValue();
	if (!isRepeatable() && content == null && !hasValue)
	    throw new IllegalArgumentException("Null content not allowed!");
	Object value = hasValue ? super.getValue() : null;
	if (TypeMapper.getDatatypeURI(content) == null) {
	    if (content != null
		    || (value != null && TypeMapper.getDatatypeURI(value) == null))
		throw new IllegalArgumentException("Given content not allowed!");
	} else if (!content.equals(value))
	    props.put(PROP_CONTENT, content);
    }

    /**
     * Returns the contained info as an instance of one of the classes supported
     * by {@link org.universAAL.middleware.rdf.TypeMapper}.
     */
    public Object getContent() {
	Object res = props.get(PROP_CONTENT);
	return (res == null) ? super.getValue() : res;
    }

    /**
     * Overrides {@link FormControl#getValue()} in order to additionally
     * consider the local storage using {@link #PROP_CONTENT}.
     */
    public Object getValue() {
	return getContent();
    }

    /**
     * For usage by de-serializers.
     */
    public void setProperty(String propURI, Object value) {
	if (PROP_CONTENT.equals(propURI)) {
	    if (!props.containsKey(propURI) && value != null)
		props.put(propURI, value);
	} else
	    super.setProperty(propURI, value);
    }
}
