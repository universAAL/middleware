/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;

/**
 * A Resource that can not be modified.
 * 
 * @author Carsten Stockloew
 */
public class UnmodifiableResource extends Resource {

    /** A safe iterator that does not allow modifications. */
    private class SafeIterator implements Iterator {
	Iterator it;

	SafeIterator(Iterator it) {
	    this.it = it;
	}

	public boolean hasNext() {
	    return it.hasNext();
	}

	public Object next() {
	    return handleObject(it.next());
	}

	public void remove() {
	}
    }

    /** A safe iterator that does not allow modifications. */
    private class SafeListIterator extends SafeIterator implements ListIterator {
	ListIterator it;

	SafeListIterator(ListIterator it) {
	    super(it);
	    this.it = it;
	}

	public void add(Object arg0) {
	}

	public boolean hasPrevious() {
	    return it.hasPrevious();
	}

	public int nextIndex() {
	    return it.nextIndex();
	}

	public Object previous() {
	    return handleObject(it.previous());
	}

	public int previousIndex() {
	    return it.nextIndex();
	}

	public void set(Object arg0) {
	}
    }

    private class UnmodifiableResourceList implements List {
	List l;

	UnmodifiableResourceList(List l) {
	    this.l = l;
	}

	public boolean add(Object arg0) {
	    return false;
	}

	public void add(int arg0, Object arg1) {
	}

	public boolean addAll(Collection arg0) {
	    return false;
	}

	public boolean addAll(int arg0, Collection arg1) {
	    return false;
	}

	public void clear() {
	}

	public boolean contains(Object o) {
	    return l.contains(o);
	}

	public boolean containsAll(Collection arg0) {
	    return l.containsAll(arg0);
	}

	public Object get(int index) {
	    Object o = l.get(index);
	    if (o instanceof Resource)
		return new UnmodifiableResource((Resource) o);
	    return o;
	}

	public int indexOf(Object o) {
	    return l.indexOf(o);
	}

	public boolean isEmpty() {
	    return l.isEmpty();
	}

	public Iterator iterator() {
	    return new SafeIterator(l.iterator());
	}

	public int lastIndexOf(Object o) {
	    return l.lastIndexOf(o);
	}

	public ListIterator listIterator() {
	    return new SafeListIterator(l.listIterator());
	}

	public ListIterator listIterator(int index) {
	    return new SafeListIterator(l.listIterator(index));
	}

	public boolean remove(Object o) {
	    return false;
	}

	public Object remove(int index) {
	    return null;
	}

	public boolean removeAll(Collection arg0) {
	    return false;
	}

	public boolean retainAll(Collection arg0) {
	    return false;
	}

	public Object set(int arg0, Object arg1) {
	    return null;
	}

	public int size() {
	    return l.size();
	}

	public List subList(int fromIndex, int toIndex) {
	    return new UnmodifiableResourceList(l.subList(fromIndex, toIndex));
	}

	public Object[] toArray() {
	    Object[] o = l.toArray();
	    for (int i = 0; i < o.length; i++) {
		if (o[i] instanceof Resource)
		    o[i] = new UnmodifiableResource((Resource) o[i]);
	    }
	    return o;
	}

	public Object[] toArray(Object[] arg0) {
	    Object[] o = l.toArray(arg0);
	    for (int i = 0; i < o.length; i++) {
		if (o[i] instanceof Resource)
		    o[i] = new UnmodifiableResource((Resource) o[i]);
	    }
	    return o;
	}
    }

    Resource res;

    public UnmodifiableResource(Resource r) {
	res = r;
    }

    private Object handleObject(Object o) {
	if (o instanceof Resource)
	    return new UnmodifiableResource((Resource) o);
	else if (o instanceof List)
	    return new UnmodifiableResourceList((List) o);

	return o;
    }

    /** @see org.universAAL.middleware.rdf.Resource#changeProperty(String, Object) */
    public boolean changeProperty(String propURI, Object value) {
	LogUtils.logDebug(SharedResources.moduleContext,
		UnmodifiableResource.class, "changeProperty",
		new String[] { "Can not change an unmodifiable resource." },
		null);
	return false;
    }

    /** @see org.universAAL.middleware.rdf.Resource#getProperty(String) */
    public final Object getProperty(String propURI) {
	Object o = res.getProperty(propURI);
	return handleObject(o);
    }

    /** @see org.universAAL.middleware.rdf.Resource#setProperty(String, Object) */
    public void setProperty(String propURI, Object value) {
	LogUtils.logDebug(SharedResources.moduleContext,
		UnmodifiableResource.class, "setProperty",
		new String[] { "Can not change an unmodifiable resource." },
		null);
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#setPropertyPath(String[],
     *      Object, boolean)
     */
    public boolean setPropertyPath(String[] propPath, Object value,
	    boolean force) {
	LogUtils.logDebug(SharedResources.moduleContext,
		UnmodifiableResource.class, "setPropertyPath",
		new String[] { "Can not change an unmodifiable resource." },
		null);
	return false;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#setPropertyPath(String[],
     *      Object)
     */
    public boolean setPropertyPath(String[] propPath, Object value) {
	LogUtils.logDebug(SharedResources.moduleContext,
		UnmodifiableResource.class, "setPropertyPath",
		new String[] { "Can not change an unmodifiable resource." },
		null);
	return false;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#setPropertyPathFromOffset(String[],
     *      int, Object, boolean)
     */
    public boolean setPropertyPathFromOffset(String[] propPath, int fromIndex,
	    Object value, boolean force) {
	LogUtils.logDebug(SharedResources.moduleContext,
		UnmodifiableResource.class, "setPropertyPathFromOffset",
		new String[] { "Can not change an unmodifiable resource." },
		null);
	return false;
    }

    public List asList() {
	return new UnmodifiableResourceList(res.asList());
    }

    public void asList(List l) {
	ArrayList l2 = new ArrayList();
	res.asList(l2);
	for (int i = 0; i < l2.size(); i++) {
	    Object o = l2.get(i);
	    l.add(handleObject(o));
	}
    }

    public Resource copyAsXMLLiteral() {
	return new UnmodifiableResource(res.copyAsXMLLiteral());
    }

    public Resource deepCopy() {
	return res.deepCopy();
    }

    public boolean equals(Object other) {
	return res.equals(other);
    }

    public String getOrConstructLabel(String type) {
	return res.getOrConstructLabel(type);
    }

    public int getPropSerializationType(String propURI) {
	return res.getPropSerializationType(propURI);
    }

    public String getResourceComment() {
	return res.getResourceComment();
    }

    public String getResourceLabel() {
	return res.getResourceLabel();
    }

    public Object getStaticFieldValue(String fieldName, Object defaultValue) {
	// TODO correct?
	return res.getStaticFieldValue(fieldName, defaultValue);
    }

    public int hashCode() {
	return res.hashCode();
    }

    public boolean hasProperty(String propURI) {
	return res.hasProperty(propURI);
    }

    public boolean isClosedCollection(String propURI) {
	return res.isClosedCollection(propURI);
    }

    public boolean isWellFormed() {
	return res.isWellFormed();
    }

    public boolean representsQualifiedURI() {
	return res.representsQualifiedURI();
    }

    public boolean serializesAsXMLLiteral() {
	return res.serializesAsXMLLiteral();
    }

    public void setResourceComment(String comment) {
    }

    public void setResourceLabel(String label) {
    }

    public String toString() {
	return res.toString();
    }

    public String toStringRecursive() {
	return res.toStringRecursive();
    }

    public String toStringRecursive(String prefix, boolean prefixAtStart,
	    Hashtable visitedElements) {
	return res.toStringRecursive(prefix, prefixAtStart, visitedElements);
    }

    public int numberOfProperties() {
	return res.numberOfProperties();
    }

    /** @see org.universAAL.middleware.rdf.Resource#isAnon() */
    public boolean isAnon() {
	return res.isAnon();
    }

    /** @see org.universAAL.middleware.rdf.Resource#hasQualifiedName() */
    public final boolean hasQualifiedName() {
	return res.hasQualifiedName();
    }

    /** @see org.universAAL.middleware.rdf.Resource#getURI() */
    public final String getURI() {
	return res.getURI();
    }

    /** @see org.universAAL.middleware.rdf.Resource#getPropertyURIs() */
    public final Enumeration getPropertyURIs() {
	return res.getPropertyURIs();
    }

    /** @see org.universAAL.middleware.rdf.Resource#addType(String, boolean) */
    public final void addType(String typeURI, boolean blockFurtherTypes) {
    }

    /** @see org.universAAL.middleware.rdf.Resource#getLocalName() */
    public final String getLocalName() {
	return res.getLocalName();
    }

    /** @see org.universAAL.middleware.rdf.Resource#getNamespace() */
    public final String getNamespace() {
	return res.getNamespace();
    }

    /** @see org.universAAL.middleware.rdf.Resource#getType() */
    public final String getType() {
	return res.getType();
    }

    /** @see org.universAAL.middleware.rdf.Resource#getTypes() */
    public final String[] getTypes() {
	return res.getTypes();
    }
    
    public final Class getClassOfUnmodifiable() {
	return res.getClass();
    }
}
