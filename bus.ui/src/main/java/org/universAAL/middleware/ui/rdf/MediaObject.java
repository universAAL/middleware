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

/**
 * An {@link Output} control for presenting media (content that goes beyond
 * plain text).
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
public class MediaObject extends Output {
	public static final String MY_URI = Form.uAAL_DIALOG_NAMESPACE
			+ "MediaObject";

	/**
	 * The Internet media type of the content borne by a media object.
	 * 
	 * @see http://en.wikipedia.org/wiki/Internet_media_type
	 */
	public static final String PROP_CONTENT_TYPE = Form.uAAL_DIALOG_NAMESPACE
			+ "contentType";

	/**
	 * The URL with which the media content can be retrieved.
	 */
	public static final String PROP_CONTENT_URL = Form.uAAL_DIALOG_NAMESPACE
			+ "contentURL";

	/**
	 * An optional hint for I/O handlers regarding the maximum horizontal size
	 * of a visualizable media in number of pixels.
	 */
	public static final String PROP_RESOLUTION_MAX_X = Form.uAAL_DIALOG_NAMESPACE
			+ "resMaxX";

	/**
	 * An optional hint for I/O handlers regarding the maximum vertical size of
	 * a visualizable media in number of pixels.
	 */
	public static final String PROP_RESOLUTION_MAX_Y = Form.uAAL_DIALOG_NAMESPACE
			+ "resMaxY";

	/**
	 * An optional hint for I/O handlers regarding the minimum horizontal size
	 * of a visualizable media in number of pixels.
	 */
	public static final String PROP_RESOLUTION_MIN_X = Form.uAAL_DIALOG_NAMESPACE
			+ "resMinX";

	/**
	 * An optional hint for I/O handlers regarding the minimum vertical size of
	 * a visualizable media in number of pixels.
	 */
	public static final String PROP_RESOLUTION_MIN_Y = Form.uAAL_DIALOG_NAMESPACE
			+ "resMinY";

	/**
	 * An optional hint for I/O handlers regarding the preferred horizontal size
	 * of a visualizable media in number of pixels.
	 */
	public static final String PROP_RESOLUTION_PREFERRED_X = Form.uAAL_DIALOG_NAMESPACE
			+ "resPreferredX";

	/**
	 * An optional hint for I/O handlers regarding the preferred vertical size
	 * of a visualizable media in number of pixels.
	 */
	public static final String PROP_RESOLUTION_PREFERRED_Y = Form.uAAL_DIALOG_NAMESPACE
			+ "resPreferredY";


	/**
	 * For exclusive use by de-serializers.
	 */
	public MediaObject() {
		super();
	}

	/**
	 * Constructs a new media object.
	 * 
	 * @param parent
	 *            The mandatory parent group as the direct container of this
	 *            media object. See {@link FormControl#PROP_PARENT_CONTROL}.
	 * @param label
	 *            The optional {@link Label} to be associated with this media
	 *            object. See {@link FormControl#PROP_CONTROL_LABEL}.
	 * @param contentType
	 *            See {@link #PROP_CONTENT_TYPE}; mandatory.
	 * @param contentURL
	 *            See {@link #PROP_CONTENT_URL}; mandatory.
	 */
	public MediaObject(Group parent, Label label, String contentType,
			String contentURL) {
		super(MY_URI, parent, label, null, null);
		props.put(PROP_CONTENT_TYPE, contentType);
		props.put(PROP_CONTENT_URL, contentURL);
	}

	/**
	 * @see #PROP_CONTENT_TYPE
	 */
	public String getContentType() {
		return (String) props.get(PROP_CONTENT_TYPE);
	}

	/**
	 * @see #PROP_CONTENT_URL
	 */
	public String getContentURL() {
		return (String) props.get(PROP_CONTENT_URL);
	}

	/**
	 * Overrides the default implementation in
	 * {@link FormControl#getMaxLength()} and returns always -1 as the intended
	 * number of characters does not apply to media objects.
	 */
	public int getMaxLength() {
		// not applicable
		return -1;
	}

	/**
	 * @see #PROP_RESOLUTION_MAX_X
	 */
	public int getResolutionMaxX() {
		Integer x = (Integer) props.get(PROP_RESOLUTION_MAX_X);
		return (x == null) ? -1 : x.intValue();
	}

	/**
	 * @see #PROP_RESOLUTION_MAX_Y
	 */
	public int getResolutionMaxY() {
		Integer x = (Integer) props.get(PROP_RESOLUTION_MAX_Y);
		return (x == null) ? -1 : x.intValue();
	}

	/**
	 * @see #PROP_RESOLUTION_MIN_X
	 */
	public int getResolutionMinX() {
		Integer x = (Integer) props.get(PROP_RESOLUTION_MIN_X);
		return (x == null) ? -1 : x.intValue();
	}

	/**
	 * @see #PROP_RESOLUTION_MIN_Y
	 */
	public int getResolutionMinY() {
		Integer x = (Integer) props.get(PROP_RESOLUTION_MIN_Y);
		return (x == null) ? -1 : x.intValue();
	}

	/**
	 * @see #PROP_RESOLUTION_PREFERRED_X
	 */
	public int getResolutionPreferredX() {
		Integer x = (Integer) props.get(PROP_RESOLUTION_PREFERRED_X);
		return (x == null) ? -1 : x.intValue();
	}

	/**
	 * @see #PROP_RESOLUTION_PREFERRED_Y
	 */
	public int getResolutionPreferredY() {
		Integer x = (Integer) props.get(PROP_RESOLUTION_PREFERRED_Y);
		return (x == null) ? -1 : x.intValue();
	}

	/**
	 * Sets the maximum size in number of pixels.
	 * 
	 * @param x
	 *            See {@link #PROP_RESOLUTION_MAX_X}
	 * @param y
	 *            See {@link #PROP_RESOLUTION_MAX_Y}
	 */
	public void setMaxResolution(int x, int y) {
		if (x > 0)
			props.put(PROP_RESOLUTION_MAX_X, new Integer(x));
		if (y > 0)
			props.put(PROP_RESOLUTION_MAX_Y, new Integer(y));
	}

	/**
	 * Sets the minimum size in number of pixels.
	 * 
	 * @param x
	 *            See {@link #PROP_RESOLUTION_MIN_X}
	 * @param y
	 *            See {@link #PROP_RESOLUTION_MIN_Y}
	 */
	public void setMinResolution(int x, int y) {
		if (x > 0)
			props.put(PROP_RESOLUTION_MIN_X, new Integer(x));
		if (y > 0)
			props.put(PROP_RESOLUTION_MIN_Y, new Integer(y));
	}

	/**
	 * Sets the preferred size in number of pixels.
	 * 
	 * @param x
	 *            See {@link #PROP_RESOLUTION_PREFERRED_X}
	 * @param y
	 *            See {@link #PROP_RESOLUTION_PREFERRED_Y}
	 */
	public void setPreferredResolution(int x, int y) {
		if (x > 0)
			props.put(PROP_RESOLUTION_PREFERRED_X, new Integer(x));
		if (y > 0)
			props.put(PROP_RESOLUTION_PREFERRED_Y, new Integer(y));
	}

	/**
	 * @see FormControl#setProperty(String, Object)
	 */
	public void setProperty(String propURI, Object value) {
		if (propURI == null || value == null || props.containsKey(propURI))
			return;

		if (PROP_CONTENT_TYPE.equals(propURI)
				|| PROP_CONTENT_URL.equals(propURI)) {
			if (value instanceof String)
				props.put(propURI, value);
		} else if (PROP_RESOLUTION_MAX_X.equals(propURI)
				|| PROP_RESOLUTION_MAX_Y.equals(propURI)
				|| PROP_RESOLUTION_MIN_X.equals(propURI)
				|| PROP_RESOLUTION_MIN_Y.equals(propURI)
				|| PROP_RESOLUTION_PREFERRED_X.equals(propURI)
				|| PROP_RESOLUTION_PREFERRED_Y.equals(propURI)) {
			if (value instanceof Integer && ((Integer) value).intValue() > 0)
				props.put(propURI, value);
		} else
			super.setProperty(propURI, value);
	}
}
