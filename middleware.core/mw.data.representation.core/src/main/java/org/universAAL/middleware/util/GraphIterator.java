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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.universAAL.middleware.rdf.Resource;

/**
 * Iterate over all Resources of an RDF graph.
 *
 * @author Carsten Stockloew
 */
public class GraphIterator implements Iterator {

	/**
	 * A class that links to a resource but overrides the equals-method to
	 * return true iff the objects are equal (instead of the URI as it is
	 * defined for {@link Resource}).
	 */
	public static class ObjectEqualsResource {
		public Resource r;

		public ObjectEqualsResource(Resource r) {
			this.r = r;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof ObjectEqualsResource))
				return false;
			return (r == ((ObjectEqualsResource) obj).r);
		}

		@Override
		public int hashCode() {
			return r.hashCode();
		}

		public ObjectEqualsResource set(Resource r) {
			this.r = r;
			return this;
		}
	}

	/**
	 * The set of resources that have been visited before. During iteration,
	 * this will be used to avoid endless loop in case of cycles.
	 */
	protected Set<ObjectEqualsResource> visitedResources = new HashSet<ObjectEqualsResource>();

	/**
	 * A dummy object for performance that is only used to call the
	 * contains-method of {@link #visitedResources}.
	 */
	protected ObjectEqualsResource idxdummy = new ObjectEqualsResource(null);

	/**
	 * The current information at a certain depth.
	 */
	protected StackElement se;

	/**
	 * The stack contains information for previous depth. When going one level
	 * deeper into the graph, the information about the current state, e.g. the
	 * enumerator of properties, is stored in the stack to be restored when all
	 * child nodes have been processed.
	 */
	protected Stack<StackElement> stack = new Stack<StackElement>();

	/**
	 * The next element as being returned by {@link #next()}. It can be
	 * calculated by {@link #next()} and by {@link #hasNext()}.
	 */
	protected GraphIteratorElement nextElement = null;

	/**
	 * Element to be stored in the stack.
	 */
	protected static class StackElement {
		/** The parent node. */
		private Resource nodeParent;

		/** The child node. Can be a {@link Resource}, a Literal, or a list. */
		private Object nodeChild = null;

		/** The depth, i.e. the distance to the root node. */
		private int depth;

		/**
		 * Enumerator for properties to iterate over all property URIs of a
		 * Resource.
		 */
		private Enumeration enumProp;

		/** The current property URI. */
		private String propURI;

		/**
		 * If the child node is a list, this variable holds the iterator over
		 * elements of the list.
		 */
		private Iterator lstIterator = null;

		/**
		 * If the child node is a list, this variable holds the index of the
		 * currently selected element of the list.
		 */
		private int lstIndex = 0;

		/**
		 * If the child node is a list, this variable holds the currently
		 * selected element of the list.
		 */
		private Object lstElement = null;
	}

	/**
	 * A specialized iterator to iterate only over instances of {@link Resource}
	 * .
	 */
	protected static class GraphIteratorResources extends GraphIterator {
		boolean first = true;

		GraphIteratorResources(Resource root) {
			super(root);
		}

		@Override
		protected void createNext() {
			if (first)
				return;

			// System.out.println("..createNext");
			// for (ObjectEqualsResource o : visitedResources) {
			// System.out.println("-- " + o.r);
			// }

			while (true) {
				super.createNext();
				if (nextElement == null)
					return;

				// System.out.println("next triple: " +
				// nextElement.getSubject().getURI() + " " +
				// nextElement.getPredicate() +
				// " " + nextElement.getObject());

				if (nextElement.getObject() instanceof Resource) {
					if (!visitedResources.contains(idxdummy.set((Resource) (nextElement.getObject()))))
						return;
					// else
					// System.out.println(" ++ already visited: " +
					// ((Resource)nextElement.getObject()).getURI());
				}
				// force createNext
				nextElement = null;
			}
		}

		@Override
		public boolean hasNext() {
			if (first)
				return true;
			return super.hasNext();
		}

		@Override
		public Object next() {
			if (first) {
				first = false;
				visitedResources.add(new ObjectEqualsResource(se.nodeParent));
				return se.nodeParent;
			} else {
				GraphIteratorElement el = (GraphIteratorElement) super.next();
				if (el == null)
					return null;
				Resource r = (Resource) el.getObject();
				// visitedResources.add(new ObjectEqualsResource(r));
				return r;
			}
		}
	}

	protected GraphIterator(Resource root) {
		stepDeeper(root);
	}

	/**
	 * Create a new Iterator that iterates over all triples of the graph.
	 *
	 * @param root
	 *            the {@link Resource} that is the root of the graph.
	 * @return an {@link Iterator} to iterate over elements of the graph.
	 */
	public static Iterator<GraphIteratorElement> getIterator(Resource root) {
		if (root == null)
			throw new NullPointerException("The argument of a GraphIterator can not be null.");
		return new GraphIterator(root);
	}

	/**
	 * Create a new Iterator that iterates over all Resources of the graph. This
	 * iterator differentiates between different Java objects. Thus, it might be
	 * that more than one resource with the same URI is returned if the Java
	 * objects are different.
	 *
	 * @param root
	 *            the {@link Resource} that is the root of the graph.
	 * @return an {@link Iterator} to iterate over elements of the graph.
	 */
	public static Iterator<Resource> getResourceIterator(Resource root) {
		if (root == null)
			throw new NullPointerException("The argument of a GraphIterator can not be null.");
		return new GraphIteratorResources(root);
	}

	// this is as a separate method so that it can be overwritten, e.g. to
	// provide a sorted list of properties
	protected Enumeration getPropertyEnumeration(Resource r) {
		return r.getPropertyURIs();
	}

	protected void stepDeeper(Resource root) {
		visitedResources.add(new ObjectEqualsResource(root));
		StackElement newSe = new StackElement();
		newSe.nodeParent = root;
		newSe.depth = 0;
		newSe.enumProp = getPropertyEnumeration(newSe.nodeParent);
		if (se != null) {
			// this is not the first level
			newSe.depth = se.depth + 1;
			stack.push(se);
		}
		se = newSe;
	}

	protected boolean stepHigher() {
		if (stack.isEmpty())
			return false;
		se = stack.pop();
		return true;
	}

	protected void createResult() {
		if (se.nodeChild instanceof List)
			nextElement = new GraphIteratorElement(se.nodeParent, se.propURI, se.lstElement, se.depth, true,
					se.lstIndex, (List) se.nodeChild);
		else
			nextElement = new GraphIteratorElement(se.nodeParent, se.propURI, se.nodeChild, se.depth, false, 0, null);
	}

	protected void createNext() {
		if (nextElement != null)
			return;

		// if the current element is a resource -> follow path
		Resource r = null;
		if (se.nodeChild instanceof Resource) {
			r = (Resource) se.nodeChild;
		} else if (se.lstElement instanceof Resource) {
			r = (Resource) se.lstElement;
		}
		if (r != null && !visitedResources.contains(idxdummy.set(r)))
			stepDeeper(r);

		while (true) {
			// increase iterator
			if (se.nodeChild instanceof List) {
				if (se.lstIterator.hasNext()) {
					// next property list element
					se.lstElement = se.lstIterator.next();
					se.lstIndex++;
				} else {
					// list is done -> next property
					se.lstElement = null;
					se.lstIndex = 0;
					se.lstIterator = null;
					se.nodeChild = null;
					continue;
				}
			} else {
				if (se.enumProp.hasMoreElements()) {
					se.propURI = (String) se.enumProp.nextElement();
					se.nodeChild = se.nodeParent.getProperty(se.propURI);
					if (se.nodeChild instanceof List) {
						se.lstIndex = 0;
						se.lstIterator = ((List) se.nodeChild).iterator();
					}
				} else {
					// resource is done -> stepHigher
					if (stepHigher())
						continue;
					// this case should only happen if next() is called if
					// hasNext() would return false
					return;
				}
			}

			// we are now at the next element, go on to the next valid element
			if (se.nodeChild instanceof Resource) {
				createResult();
				return;
			} else if (se.nodeChild instanceof List) {
				if (se.lstElement != null) {
					createResult();
					return;
				}
			} else if (se.nodeChild != null) {
				createResult();
				return;
			}
		}
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		createNext();
		return nextElement != null;
	}

	/**
	 * @see java.util.Iterator#next()
	 * @return the next element of the iterator. The type of the return value
	 *         depends on the type of the iterator.
	 */
	public Object next() {
		createNext();
		Object retVal = nextElement;
		nextElement = null;
		return retVal;
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException("Removing resources is not allowed in a GraphIterator");
	}
}
