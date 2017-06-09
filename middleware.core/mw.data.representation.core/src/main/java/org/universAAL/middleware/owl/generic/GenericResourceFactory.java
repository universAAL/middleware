/*
	Copyright 2016-2020 Carsten Stockloew

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
package org.universAAL.middleware.owl.generic;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;

/**
 * The factory for a {@link GenericOntology}.
 *
 * @author Carsten Stockloew
 */
public final class GenericResourceFactory implements ResourceFactory {

	/** {@inheritDoc} */
	public Resource createInstance(String classURI, String instanceURI, int factoryIndex) {
		return new GenericManagedIndividual(classURI, instanceURI);
	}
}
