/*******************************************************************************
 * Copyright 2019 Universidad Politécnica de Madrid UPM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.middleware.util;

import org.universAAL.middleware.rdf.Resource;

/**
 * An Utility class to easily define Resources with their types and properties,
 * using simple linked methods, in one statement.
 * 
 * @author amedrano
 *
 */
public final class InlineResource extends Resource {

	/**
	 * Private constructor, use the static methods to initiate a statement.
	 * Constructor for anonymous Resource.
	 */
	private InlineResource() {
		super();
	}

	/**
	 * Private constructor, use the static methods to initiate a statement.
	 * Constructor for Named Resource.
	 */
	private InlineResource(String uri) {
		super(uri);
	}

	/**
	 * Initiate a Resource with URI.
	 * 
	 * @param uri The URI of the Resource
	 * @return The resource to continue defining stuff.
	 */
	public static InlineResource withURI(String uri) {
		InlineResource ir = new InlineResource(uri);
		return ir;
	}

	/**
	 * Initiate an anonymous Resource .
	 * 
	 * @return The resource to continue defining stuff.
	 */
	public static InlineResource anonymous() {
		InlineResource ir = new InlineResource();
		return ir;
	}

	/**
	 * Add a type to the resource.
	 * 
	 * @param typeURI The URI of the type of the Resource.
	 * @return The resource to continue defining stuff.
	 */
	public InlineResource subTypeOf(String typeURI) {
		addType(typeURI, false);
		return this;
	}

	/**
	 * Add a property value, it will add multivalue property if property already has
	 * a value.
	 * 
	 * @param prop_uri The URI of the property to add the value to.
	 * @param value    The Value to be added.
	 * @return The resource to continue defining stuff.
	 */
	public InlineResource withProperty(String prop_uri, Object value) {
		addToProperty(prop_uri, value);
		return this;
	}

	/**
	 * Specialize a InlineResource, this will return the Resource instanciated from
	 * the most specialized class universAAL can provide.
	 * 
	 * @return A specialized resource.
	 */
	public Resource specialize() {
		blockAddingTypes = true;
		return new Specializer().specialize(this);
	}

}
