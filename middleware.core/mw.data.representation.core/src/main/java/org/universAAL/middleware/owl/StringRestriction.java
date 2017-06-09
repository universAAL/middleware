/*
	Copyright 2017-2020 Carsten Stockloew

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

import org.universAAL.middleware.rdf.TypeMapper;

/**
 * A {@link TypeExpression} ({@link LengthRestriction}) that contains all
 * Strings with a given minimum and/or maximum length.
 *
 * @author Carsten Stockloew
 */
public final class StringRestriction extends LengthRestriction {

	/** URI of the data type. */
	public static final String DATATYPE_URI = TypeMapper.getDatatypeURI(String.class);

	/** Standard constructor. */
	public StringRestriction() {
		super(DATATYPE_URI);
	}

	@Override
	public TypeExpression copy() {
		return copyTo(new StringRestriction());
	}
}
