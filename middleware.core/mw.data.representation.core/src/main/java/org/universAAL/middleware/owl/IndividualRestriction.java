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
package org.universAAL.middleware.owl;

public final class IndividualRestriction extends BoundedValueRestriction {

    public static final String DATATYPE_URI = ComparableIndividual.MY_URI;

    public IndividualRestriction() {
	super(DATATYPE_URI);
    }

    public IndividualRestriction(ComparableIndividual min,
	    boolean minInclusive, ComparableIndividual max, boolean maxInclusive) {
	super(ComparableIndividual.MY_URI, min, minInclusive, max, maxInclusive);
    }

    protected Comparable getNext(Comparable c) {
	return ((ComparableIndividual) c).getNext();
    }

    protected Comparable getPrevious(Comparable c) {
	return ((ComparableIndividual) c).getPrevious();
    }

    /** @see org.universAAL.middleware.owl.TypeExpression#copy() */
    public TypeExpression copy() {
	return copyTo(new IndividualRestriction());
    }
}
