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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class UnmodifiableResourceList implements List {

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
	    return UnmodifiableResource.getUnmodifiable(it.next());
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
	    return UnmodifiableResource.getUnmodifiable(it.previous());
	}

	public int previousIndex() {
	    return it.nextIndex();
	}

	public void set(Object arg0) {
	}
    }

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
