/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.util;

import java.util.List;
import org.universAAL.middleware.rdf.Resource;

public final class GraphIteratorElement {
    private Resource subject = null;
    private String predicate = null;
    private Object object = null;
    private int depth = 0;
    private boolean isList = false;
    private int listIndex = 0;
    private List theList = null;

    public GraphIteratorElement(Resource subject, String predicate, Object object, int depth,
	    boolean isList, int listIndex, List theList) {
	this.subject = subject;
	this.predicate = predicate;
	this.object = object;
	this.depth = depth;
	this.isList = isList;
	this.listIndex = listIndex;
	this.theList = theList;
    }
    
    public List getList() {
	return theList;
    }

    public boolean isList() {
	return isList;
    }

    public int getListIndex() {
	return listIndex;
    }

    public int getDepth() {
	return depth;
    }

    public Resource getSubject() {
	return subject;
    }

    public String getPredicate() {
	return predicate;
    }

    public Object getObject() {
	return object;
    }
}
