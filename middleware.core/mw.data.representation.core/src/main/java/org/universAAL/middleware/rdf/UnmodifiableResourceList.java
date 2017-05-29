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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A list of Resources that can not be modified.
 *
 * @author Carsten Stockloew
 */
public final class UnmodifiableResourceList implements List {

    /** A safe iterator that does not allow modifications. */
    private static class SafeIterator implements Iterator {
	private Iterator it;

	SafeIterator(Iterator it) {
	    this.it = it;
	}

	/** {@inheritDoc} */
	public boolean hasNext() {
	    return it.hasNext();
	}

	/** {@inheritDoc} */
	public Object next() {
	    return UnmodifiableResource.getUnmodifiable(it.next());
	}

	/** {@inheritDoc} */
	public void remove() {
	}
    }

    /** A safe iterator that does not allow modifications. */
    private static class SafeListIterator extends SafeIterator implements
	    ListIterator {
	private ListIterator it;

	SafeListIterator(ListIterator it) {
	    super(it);
	    this.it = it;
	}

	/** {@inheritDoc} */
	public void add(Object arg0) {
	}

	/** {@inheritDoc} */
	public boolean hasPrevious() {
	    return it.hasPrevious();
	}

	/** {@inheritDoc} */
	public int nextIndex() {
	    return it.nextIndex();
	}

	/** {@inheritDoc} */
	public Object previous() {
	    return UnmodifiableResource.getUnmodifiable(it.previous());
	}

	/** {@inheritDoc} */
	public int previousIndex() {
	    return it.nextIndex();
	}

	/** {@inheritDoc} */
	public void set(Object arg0) {
	}
    }

    private List l;

    public UnmodifiableResourceList(List l) {
	this.l = l;
    }

    public static SafeIterator getIterator(Iterator it) {
	return new SafeIterator(it);
    }

    /** {@inheritDoc} */
    public boolean add(Object arg0) {
	return false;
    }

    /** {@inheritDoc} */
    public void add(int arg0, Object arg1) {
    }

    /** {@inheritDoc} */
    public boolean addAll(Collection arg0) {
	return false;
    }

    /** {@inheritDoc} */
    public boolean addAll(int arg0, Collection arg1) {
	return false;
    }

    /** {@inheritDoc} */
    public void clear() {
    }

    /** {@inheritDoc} */
    public boolean contains(Object o) {
	return l.contains(o);
    }

    /** {@inheritDoc} */
    public boolean containsAll(Collection arg0) {
	return l.containsAll(arg0);
    }

    /** {@inheritDoc} */
    public Object get(int index) {
	Object o = l.get(index);
	if (o instanceof Resource)
	    return new UnmodifiableResource((Resource) o);
	return o;
    }

    /** {@inheritDoc} */
    public int indexOf(Object o) {
	return l.indexOf(o);
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
	return l.isEmpty();
    }

    /** {@inheritDoc} */
    public Iterator iterator() {
	return new SafeIterator(l.iterator());
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object o) {
	return l.lastIndexOf(o);
    }

    /** {@inheritDoc} */
    public ListIterator listIterator() {
	return new SafeListIterator(l.listIterator());
    }

    /** {@inheritDoc} */
    public ListIterator listIterator(int index) {
	return new SafeListIterator(l.listIterator(index));
    }

    /** {@inheritDoc} */
    public boolean remove(Object o) {
	return false;
    }

    /** {@inheritDoc} */
    public Object remove(int index) {
	return null;
    }

    /** {@inheritDoc} */
    public boolean removeAll(Collection arg0) {
	return false;
    }

    /** {@inheritDoc} */
    public boolean retainAll(Collection arg0) {
	return false;
    }

    /** {@inheritDoc} */
    public Object set(int arg0, Object arg1) {
	return null;
    }

    /** {@inheritDoc} */
    public int size() {
	return l.size();
    }

    /** {@inheritDoc} */
    public List subList(int fromIndex, int toIndex) {
	return new UnmodifiableResourceList(l.subList(fromIndex, toIndex));
    }

    /** {@inheritDoc} */
    public Object[] toArray() {
	Object[] o = l.toArray();
	for (int i = 0; i < o.length; i++) {
	    if (o[i] instanceof Resource)
		o[i] = new UnmodifiableResource((Resource) o[i]);
	}
	return o;
    }

    /** {@inheritDoc} */
    public Object[] toArray(Object[] arg0) {
	Object[] o = l.toArray(arg0);
	for (int i = 0; i < o.length; i++) {
	    if (o[i] instanceof Resource)
		o[i] = new UnmodifiableResource((Resource) o[i]);
	}
	return o;
    }
}
