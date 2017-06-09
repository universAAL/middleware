/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research

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
package org.universAAL.middleware.rdf;

import java.util.Enumeration;

/**
 * This class finalizes some of the methods of {@link Resource} so that they
 * cannot be overwritten by subclasses.
 *
 * @author Carsten Stockloew
 */
public class FinalizedResource extends Resource {

	/** @see org.universAAL.middleware.rdf.Resource#Resource() */
	protected FinalizedResource() {
		super();
	}

	/** @see org.universAAL.middleware.rdf.Resource#Resource(boolean) */
	public FinalizedResource(boolean isXMLLiteral) {
		super(isXMLLiteral);
	}

	/** @see org.universAAL.middleware.rdf.Resource#Resource(String) */
	protected FinalizedResource(String uri) {
		super(uri);
	}

	/** @see org.universAAL.middleware.rdf.Resource#Resource(String, boolean) */
	public FinalizedResource(String uri, boolean isXMLLiteral) {
		super(uri, isXMLLiteral);
	}

	/** @see org.universAAL.middleware.rdf.Resource#Resource(String, int) */
	protected FinalizedResource(String uriPrefix, int numProps) {
		super(uriPrefix, numProps);
	}

	@Override
	public final Object getProperty(String propURI) {
		return super.getProperty(propURI);
	}

	@Override
	public final int numberOfProperties() {
		return super.numberOfProperties();
	}

	@Override
	public final boolean isAnon() {
		return super.isAnon();
	}

	@Override
	public final boolean hasQualifiedName() {
		return super.hasQualifiedName();
	}

	@Override
	public final String getURI() {
		return super.getURI();
	}

	@Override
	public final Enumeration getPropertyURIs() {
		return super.getPropertyURIs();
	}

	@Override
	public final boolean addType(String typeURI, boolean blockFurtherTypes) {
		return super.addType(typeURI, blockFurtherTypes);
	}

	@Override
	public final String getLocalName() {
		return super.getLocalName();
	}

	@Override
	public final String getNamespace() {
		return super.getNamespace();
	}

	@Override
	public final String getType() {
		return super.getType();
	}

	@Override
	public final String[] getTypes() {
		return super.getTypes();
	}
}
