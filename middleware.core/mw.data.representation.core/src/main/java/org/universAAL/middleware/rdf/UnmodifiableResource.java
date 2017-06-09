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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;

/**
 * A Resource that can not be modified.
 *
 * @author Carsten Stockloew
 */
public final class UnmodifiableResource extends Resource {

	private Resource res;

	public UnmodifiableResource(Resource r) {
		res = r;
	}

	/**
	 * Get an unmodifiable version of the given object. If the parameter is a
	 * {@link Resource}, an {@link UnmodifiableResource} is returned. If the
	 * parameter is a {@link java.util.List}, an
	 * {@link UnmodifiableResourceList} is returned.
	 *
	 * @param o
	 *            The object for which an unmodifiable version should be
	 *            returned.
	 * @return The unmodifiable version.
	 */
	public static Object getUnmodifiable(Object o) {
		if (o instanceof Resource)
			return new UnmodifiableResource((Resource) o);
		else if (o instanceof List)
			return new UnmodifiableResourceList((List) o);

		return o;
	}

	@Override
	public boolean changeProperty(String propURI, Object value) {
		LogUtils.logDebug(SharedResources.moduleContext, UnmodifiableResource.class, "changeProperty",
				new String[] { "Can not change an unmodifiable resource." }, null);
		return false;
	}

	@Override
	public Object getProperty(String propURI) {
		Object o = res.getProperty(propURI);
		return getUnmodifiable(o);
	}

	@Override
	public boolean setProperty(String propURI, Object value) {
		LogUtils.logDebug(SharedResources.moduleContext, UnmodifiableResource.class, "setProperty",
				new String[] { "Can not change an unmodifiable resource." }, null);
		return false;
	}

	@Override
	public boolean setPropertyPath(String[] propPath, Object value, boolean force) {
		LogUtils.logDebug(SharedResources.moduleContext, UnmodifiableResource.class, "setPropertyPath",
				new String[] { "Can not change an unmodifiable resource." }, null);
		return false;
	}

	@Override
	public boolean setPropertyPath(String[] propPath, Object value) {
		LogUtils.logDebug(SharedResources.moduleContext, UnmodifiableResource.class, "setPropertyPath",
				new String[] { "Can not change an unmodifiable resource." }, null);
		return false;
	}

	@Override
	public boolean setPropertyPathFromOffset(String[] propPath, int fromIndex, Object value, boolean force) {
		LogUtils.logDebug(SharedResources.moduleContext, UnmodifiableResource.class, "setPropertyPathFromOffset",
				new String[] { "Can not change an unmodifiable resource." }, null);
		return false;
	}

	@Override
	public List asList() {
		return new UnmodifiableResourceList(res.asList());
	}

	@Override
	public void asList(List l) {
		ArrayList l2 = new ArrayList();
		res.asList(l2);
		for (int i = 0; i < l2.size(); i++) {
			Object o = l2.get(i);
			l.add(getUnmodifiable(o));
		}
	}

	@Override
	public Resource copy(boolean isXMLLitera) {
		return new UnmodifiableResource(res.copy(isXMLLitera));
	}

	@Override
	public Resource deepCopy() {
		return res.deepCopy();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof Resource))
			return false;
		if (getURI().equals(((Resource) other).getURI()))
			return true;
		return res.equals(other);
	}

	@Override
	public String getOrConstructLabel(String type) {
		return res.getOrConstructLabel(type);
	}

	@Override
	public int getPropSerializationType(String propURI) {
		return res.getPropSerializationType(propURI);
	}

	@Override
	public String getResourceComment() {
		return res.getResourceComment();
	}

	@Override
	public String getResourceLabel() {
		return res.getResourceLabel();
	}

	@Override
	public Object getStaticFieldValue(String fieldName, Object defaultValue) {
		// TODO correct?
		return res.getStaticFieldValue(fieldName, defaultValue);
	}

	@Override
	public int hashCode() {
		return res.hashCode();
	}

	@Override
	public boolean hasProperty(String propURI) {
		return res.hasProperty(propURI);
	}

	@Override
	public boolean isClosedCollection(String propURI) {
		return res.isClosedCollection(propURI);
	}

	@Override
	public boolean isWellFormed() {
		return res.isWellFormed();
	}

	@Override
	public boolean representsQualifiedURI() {
		return res.representsQualifiedURI();
	}

	@Override
	public boolean serializesAsXMLLiteral() {
		return res.serializesAsXMLLiteral();
	}

	@Override
	public void setResourceComment(String comment) {
	}

	@Override
	public void setResourceLabel(String label) {
	}

	@Override
	public String toString() {
		return res.toString();
	}

	@Override
	public String toStringRecursive() {
		return res.toStringRecursive();
	}

	@Override
	public String toStringRecursive(String prefix, boolean prefixAtStart, Hashtable visitedElements) {
		return res.toStringRecursive(prefix, prefixAtStart, visitedElements);
	}

	@Override
	public int numberOfProperties() {
		return res.numberOfProperties();
	}

	@Override
	public boolean isAnon() {
		return res.isAnon();
	}

	@Override
	public boolean hasQualifiedName() {
		return res.hasQualifiedName();
	}

	@Override
	public String getURI() {
		return res.getURI();
	}

	@Override
	public Enumeration getPropertyURIs() {
		return res.getPropertyURIs();
	}

	@Override
	public boolean addType(String typeURI, boolean blockFurtherTypes) {
		return false;
	}

	@Override
	public String getLocalName() {
		return res.getLocalName();
	}

	@Override
	public String getNamespace() {
		return res.getNamespace();
	}

	@Override
	public String getType() {
		return res.getType();
	}

	@Override
	public String[] getTypes() {
		return res.getTypes();
	}

	public Class getClassOfUnmodifiable() {
		return res.getClass();
	}

	public boolean instanceOf(Class c) {
		return c.isAssignableFrom(res.getClass());
	}
}
