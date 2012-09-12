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
public class UnmodifiableResource extends Resource {

    Resource res;

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
	return getUnmodifiable(o);
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
	    l.add(getUnmodifiable(o));
	}
    }

    public Resource copyAsXMLLiteral() {
	return new UnmodifiableResource(res.copyAsXMLLiteral());
    }

    public Resource deepCopy() {
	return res.deepCopy();
    }

    public boolean equals(Object other) {
	if (this == other)
	    return true;
	if (getURI().equals(((Resource) other).getURI()))
	    return true;
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

    public final boolean instanceOf(Class c) {
	return c.isAssignableFrom(res.getClass());
    }
}
