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

import org.universAAL.middleware.owl.ManagedIndividual;

/**
 * Class to create instances for a {@link GenericOntology}.
 * 
 * @author Carsten Stockloew
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "This is implemented in Resource based on URI and props.")
public final class GenericManagedIndividual extends ManagedIndividual {

    private String classURI = null;

    public GenericManagedIndividual(String classURI, String instanceURI) {
	super(instanceURI);
	this.classURI = classURI;
	addType(classURI, true);
    }

    @Override
    public String getClassURI() {
	return classURI;
    }

    @Override
    public int getPropSerializationType(String propURI) {
	return PROP_SERIALIZATION_FULL;
    }
}
