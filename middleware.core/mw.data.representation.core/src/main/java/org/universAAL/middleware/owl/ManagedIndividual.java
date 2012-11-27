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
package org.universAAL.middleware.owl;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * The root of the whole manageable class hierarchy in uAAL.
 * <p>
 * Conventions to be followed by all subclasses in the class hierarchy rooted at
 * <code>ManagedIndividual</code> are:
 * <ol>
 * <li>They should define a <code>public static final</code> field of type
 * {@link java.lang.String} with the name <code>MY_URI</code> initialized by the
 * URI of the ontology class they represent.
 * <li>They MUST overwrite the method {@link #getClassURI()} which usually just
 * returns <code>MY_URI</code>.
 * <li>All sub classes MUST be registered by creating a sub class of
 * {@link Ontology} and registering this at the {@link OntologyManagement}.
 * <li>Instance methods that are serious candidates to be overwritten are
 * {@link #isClosedCollection(String)} and {@link #setProperty(String, Object)}.
 * Please read the comments below on these methods as well as comments provided
 * within {@link Resource}.
 * </ol>
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author cstockloew
 */
public abstract class ManagedIndividual extends FinalizedResource {

    /** URI namespace for OWL. */
    public static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";

    /** Definition of owl:Individual. */
    // TODO: there is no owl:Individual, use owl:NamedIndividual?
    public static final String TYPE_OWL_INDIVIDUAL = OWL_NAMESPACE
	    + "Individual";

    /** Definition of owl:Thing. */
    public static final String TYPE_OWL_THING = OWL_NAMESPACE + "Thing";

    /** The URI of the ontology class. Must be overwritten */
    public static final String MY_URI = uAAL_VOCABULARY_NAMESPACE
	    + "ManagedIndividual";

    /**
     * The ontological information about the class of this object. For
     * performance reasons, when the {@link OntClassInfo} is retrieved from
     * OntologyManagement by calling #getOntClassInfo() it is stored here to
     * accelerate further calls.
     */
    private OntClassInfo ontClassInfo = null;

    /** The constructor for (de-)serializers. */
    protected ManagedIndividual() {
	super();
	init();
    }

    /**
     * Creates an instance of ManagedIndividual with a given URI.
     * 
     * @param uri
     *            The URI.
     */
    protected ManagedIndividual(String uri) {
	super(uri);
	init();
    }

    /**
     * Creates an instance of ManagedIndividual with a URI that is created by
     * appending a unique ID to the given 'uriPrefix'. This constructor has a
     * pseudo parameter 'numProps' in order to make it distinct from the other
     * constructor that also takes a string. Later versions of ManagedIndividual
     * may decide to make some use of numProps in some way, however.
     * 
     * @param uriPrefix
     *            Prefix of the URI.
     * @param numProps
     *            Not used.
     * @see org.universAAL.middleware.rdf.Resource#Resource(String, int)
     */
    protected ManagedIndividual(String uriPrefix, int numProps) {
	super(uriPrefix, numProps);
	init();
    }

    /** Internal method to handle common parts of the constructors. */
    private void init() {
	String classURI = getClassURI();
	if (classURI == null)
	    throw new RuntimeException("Missing class URI!");
	if (!OntologyManagement.getInstance().isRegisteredClass(classURI, true))
	    throw new RuntimeException("Class not registered: " + classURI);

	addType(classURI, true);
    }

    /**
     * Assuming that the given parameters are the URIs of two registered classes
     * or datatypes, checks if the second type is derived from the first one.
     * 
     * @param supertypeURI
     *            The URI of the class that is assumed to be the super class of
     *            the second parameter.
     * @param subtypeURI
     *            The URI of the class that needs to be checked whether it is
     *            derived from the first parameter.
     * @return true, if the second type is derived from the first one.
     */
    public static final boolean checkCompatibility(String supertypeURI,
	    String subtypeURI) {
	if (supertypeURI == null || subtypeURI == null)
	    return false;

	if (supertypeURI.equals(subtypeURI)
		|| TypeMapper.isCompatible(supertypeURI, subtypeURI))
	    return true;

	OntClassInfo info = OntologyManagement.getInstance().getOntClassInfo(
		subtypeURI);
	if (info == null)
	    return false;
	return info.hasSuperClass(supertypeURI, true);
    }

    /**
     * Checks if the given value object is an instance of the type with the
     * given URI. Uses Java inheritance between registered classes and types for
     * checking compatibility.
     * 
     * @param typeURI
     *            The URI of the class that is assumed to be the super class of
     *            the second parameter.
     * @param value
     *            An instance of the class that needs to be checked whether it
     *            is derived from the first parameter.
     * @return true, if the second type is derived from the first one.
     */
    public static final boolean checkMembership(String typeURI, Object value) {
	if (typeURI == null)
	    return false;
	if (value == null)
	    return true;
	if (!(value instanceof ManagedIndividual)) {
	    return TypeMapper.isCompatible(typeURI, TypeMapper
		    .getDatatypeURI(value));
	    // return false;
	}

	// get class info for the object
	OntClassInfo info = ((ManagedIndividual) value).getOntClassInfo();
	if (info == null) // class not registered??
	    return false;

	// check class URI of the object
	if (typeURI.equals(info.getURI()))
	    return true;

	// check class URI of the super classes of the object
	return info.hasSuperClass(typeURI, true);
    }

    /**
     * Returns the restrictions that apply to the given property in the context
     * of the class with the given URI.
     * 
     * @param classURI
     *            URI of the class for which the restrictions apply.
     * @param propURI
     *            URI of the property.
     */
    public static final MergedRestriction getClassRestrictionsOnProperty(
	    String classURI, String propURI) {
	OntClassInfo info = OntologyManagement.getInstance().getOntClassInfo(
		classURI);
	if (info == null)
	    return null;

	return info.getRestrictionsOnProp(propURI);
    }

    /**
     * Returns an instance of a registered subclass selected by the given class
     * URI. Only if the selected subclass has a distinguished instance with the
     * given URI, the returned instance will be well-formed, otherwise it is
     * just an "empty" one that must be "filled in" by setting its properties.
     */
    public static final ManagedIndividual getInstance(String classURI,
	    String instanceURI) {

	Resource r = OntologyManagement.getInstance().getResource(classURI,
		instanceURI);
	if (r instanceof ManagedIndividual)
	    return (ManagedIndividual) r;
	return null;
    }

    public static Vector getNonAbstractSuperClasses(ManagedIndividual indi) {
	// TODO: change this!! (not very performant, maybe remove this method?)
	String[] res = ManagedIndividual.getNonabstractSuperClasses(indi
		.getClassURI());
	if (res == null)
	    return null;

	Vector resVec = new Vector();

	// in this method, we also include the given class (if it is
	// non-abstract)
	if (OntologyManagement.getInstance().isRegisteredClass(
		indi.getClassURI(), false))
	    resVec.add(indi.getClassURI());

	// add the super classes
	for (int i = 0; i < res.length; i++)
	    resVec.add(res[i]);

	return resVec;
    }

    /**
     * For a given class URI get the set of URIs for all super classes which are
     * instanceable (which are not abstract)
     * 
     * @param classURI
     *            The URI for which to get the super classes.
     * @return The set of URIs for all non-abstract super classes.
     */
    public static final String[] getNonabstractSuperClasses(String classURI) {
	if (classURI == null)
	    return null;

	OntClassInfo info = OntologyManagement.getInstance().getOntClassInfo(
		classURI);
	if (info == null)
	    return null;

	return info.getNamedSuperClasses(true, false);
    }

    /**
     * Get the class URI for a given object. If the object is an instance of
     * {@link java.util.List}, the class URI for the first object from this list
     * is returned.
     * 
     * @param o
     *            The object for which to return the class URI.
     * @return The class URI.
     */
    public static final String getTypeURI(Object o) {
	if (o instanceof List) {
	    if (((List) o).isEmpty())
		return null;
	    o = ((List) o).get(0);
	}

	return (o instanceof ManagedIndividual) ? ((ManagedIndividual) o)
		.getClassURI() : TypeMapper.getDatatypeURI(o);
    }

    /**
     * Checks if a registered class with the given URI can be found.
     */
    public static final boolean isRegisteredClassURI(String classURI) {
	LogUtils
		.logDebug(
			SharedResources.moduleContext,
			ManagedIndividual.class,
			"isRegisteredClassURI",
			new Object[] { "This method is deprecated, please use OntologyManagement.isRegisteredClass(classURI, true)." },
			null);
	return OntologyManagement.getInstance().isRegisteredClass(classURI,
		true);
    }

    /**
     * For a given {@link org.universAAL.middleware.rdf.Resource}, create a new
     * instance of ManagedIndividual with the given class URI and copy all
     * properties from the Resource object to this new ManagedIndividual.
     * 
     * @param classURI
     *            The class URI of the ManagedIndividual.
     * @param pr
     *            The Resource which needs to be copied.
     * @return A new instance of ManagedIndividual with all properties from the
     *         given Resource.
     */
    public static Resource toManagedIndividual(String classURI, Resource pr) {
	if (pr == null || classURI == null || pr instanceof ManagedIndividual)
	    return pr;
	ManagedIndividual mi = getInstance(classURI, pr.getURI());
	if (mi == null)
	    return pr;
	for (Enumeration e = pr.getPropertyURIs(); e.hasMoreElements();) {
	    String key = e.nextElement().toString();
	    Object value = pr.getProperty(key);
	    if (value instanceof Resource)
		value = toManagedIndividual(((Resource) value).getType(),
			(Resource) value);
	    mi.setProperty(key, value);
	}
	return mi;
    }

    /**
     * If this object is an XML Literal, create a copy of it.
     */
    public Resource copyAsXMLLiteral() {
	ManagedIndividual copy = getInstance(getClassURI(), uri);
	if (copy == null)
	    return super.copyAsXMLLiteral();
	copy.isXMLLiteral = true;
	for (Enumeration e = props.keys(); e.hasMoreElements();) {
	    Object key = e.nextElement();
	    copy.props.put(key, props.get(key));
	}
	return copy;
    }

    /**
     * Create a deep copy of this ManagedIndividual, i.e. create a new
     * ManagedIndividual for this object (according to the class URI of this
     * object) and for the resources of all properties.
     * 
     * @return The copied ManagedIndividual.
     * 
     * @see org.universAAL.middleware.rdf.Resource
     */
    // TODO: only resources are copies, but not list of resources.
    // TODO: will create an infinite loop for cycles.
    public Resource deepCopy() {
	ManagedIndividual copy = getInstance(getClassURI(), uri);
	if (copy == null)
	    return super.deepCopy();

	copy.isXMLLiteral = isXMLLiteral;
	copy.blockAddingTypes = blockAddingTypes;
	for (Enumeration e = props.keys(); e.hasMoreElements();) {
	    Object key = e.nextElement();
	    Object value = props.get(key);
	    if (value instanceof Resource)
		value = ((Resource) value).deepCopy();
	    copy.props.put(key, value);
	}
	return copy;
    }

    /**
     * Get the class URI for this ManagedIndividual. All non-abstract subclasses
     * MUST overwrite this method!
     * 
     * @return The class URI.
     */
    public String getClassURI() {
	return MY_URI;
    }

    /**
     * Answers if the given property has to be considered when serializing this
     * individual in a minimized way, and if not ignore-able, whether its value
     * should be presented in its full form or can be reduced. The return value
     * must be one of {@link #PROP_SERIALIZATION_OPTIONAL},
     * {@link #PROP_SERIALIZATION_REDUCED}, or {@link #PROP_SERIALIZATION_FULL}.
     * It can be assumed that the given property is one of those returned by
     * {@link #getPropertyURIs()}. Decision criterion should be if the value of
     * this property is absolutely necessary when this resource is being sent to
     * a remote node. If the subclass rates it as unlikely that the receiver
     * side would need this info, the answer should be
     * <code>PROP_SERIALIZATION_OPTIONAL</code> in favor of lower communication
     * traffic and higher performance even at risk of a possible additional
     * query on the receiver side for fetching this info. With the same
     * rationale, if a property should be included in the process of
     * serialization, it is preferable to include it in a reduced form; in this
     * case the return value should be <code>PROP_SERIALIZATION_REDUCED</code>,
     * otherwise <code>PROP_SERIALIZATION_FULL</code> can be returned.
     */
    public abstract int getPropSerializationType(String propURI);

    /**
     * @see org.universAAL.middleware.rdf.Resource#isWellFormed()
     */
    public boolean isWellFormed() {
	String[] propURIs = getOntClassInfo().getDeclaredProperties();
	String classURI = getClassURI();
	for (int i = 0; i < propURIs.length; i++) {
	    MergedRestriction r = getClassRestrictionsOnProperty(classURI,
		    propURIs[i]);
	    if (r != null && !r.hasMember(this, null))
		return false;
	}
	return true;
    }

    /**
     * The default implementation that will set a property iff it was not set
     * before and the given value complies with the restrictions defined by this
     * class of managed individuals for the given propURI.
     */
    public void setProperty(String propURI, Object value) {
	if (propURI == null || value == null || props.containsKey(propURI))
	    return;

	MergedRestriction r = OntologyManagement.getInstance().getOntClassInfo(
		getClassURI()).getRestrictionsOnProp(propURI);

	if (r == null)
	    super.setProperty(propURI, value);
	else {
	    if (r.getMaxCardinality() == 0)
		return;

	    if (value instanceof Resource
		    && !(value instanceof ManagedIndividual))
		value = toManagedIndividual(r.getPropTypeURI(),
			(Resource) value);

	    // we have to put the value first, because the Restriction 'r' needs
	    // to read it for checking the membership
	    props.put(propURI, value);
	    if (!r.hasMember(this, null))
		props.remove(propURI);
	}
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource
     */
    public boolean setPropertyPathFromOffset(String[] propPath, int fromIndex,
	    Object value, boolean force) {
	try {
	    if (fromIndex == propPath.length - 1) {
		if (force)
		    return changeProperty(propPath[fromIndex], value);
		setProperty(propPath[fromIndex], value);
		return props.get(propPath[fromIndex]) == value;
	    }

	    Object tmp = props.get(propPath[fromIndex]);
	    if (tmp == null) {
		if (value == null)
		    return true;
		MergedRestriction r = getClassRestrictionsOnProperty(
			getClassURI(), propPath[fromIndex]);
		if (r != null)
		    tmp = getInstance(r.getPropTypeURI(), null);
		if (tmp == null)
		    tmp = new Resource();
		props.put(propPath[fromIndex], tmp);
	    } else if (!(tmp instanceof Resource))
		return false;

	    return ((Resource) tmp).setPropertyPathFromOffset(propPath,
		    fromIndex + 1, value, force);
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Get the ontological information for this individual.
     */
    public OntClassInfo getOntClassInfo() {
	if (ontClassInfo == null) {
	    String classURI = getClassURI();
	    ontClassInfo = OntologyManagement.getInstance().getOntClassInfo(
		    classURI);
	}
	return ontClassInfo;
    }
}
