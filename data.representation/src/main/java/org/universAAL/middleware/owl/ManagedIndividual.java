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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * The root of the whole manageable class hierarchy in uAAL.
 * <p>
 * Conventions to be followed by all subclasses in the class hierarchy rooted at
 * <code>ManagedIndividual</code> are:
 * <ol>
 * <li>They must define a <code>public static final</code> field of type
 * {@link java.lang.String} with the name <code>MY_URI</code> initialized by the
 * URI of the ontology class they represent.
 * <li>They must register to the uAAL ontology in a static code segment using
 * the protected static method {@link #register(Class)} below.</li>
 * <li><b>all the public and static methods</b> provided by the ancestors in the
 * class hierarchy that are not declared as final <b>should</b> be overridden;
 * in case of the direct subclasses of <code>ManagedIndividual</code>, they
 * <b>must</b> overwrite {@link #getRDFSComment()}, {@link #getRDFSLabel()},
 * {@link #getClassRestrictionsOnProperty(String)}, and
 * {@link #getStandardPropertyURIs()}, and may overwrite
 * {@link #getEnumerationMembers()} and {@link #getIndividualByURI(String)}.</li>
 * The latter two may be left out, if the default implementations by this class
 * (which always returns null) apply. All subclasses that enumerate their
 * members <b>must</b> overwrite them anyhow.
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
public abstract class ManagedIndividual extends Resource {

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

    /** The set of registered classes: from URI to class. */
    private static final Hashtable uriClassMap = new Hashtable(23);

    /** The set of registered classes: from class to URI. */
    private static final Hashtable classURIMap = new Hashtable(23);

    /** The constructor for (de-)serializers. */
    protected ManagedIndividual() {
	super();
	String classURI = getClassURI();
	if (classURI == null || uriClassMap.get(classURI) != this.getClass())
	    throw new RuntimeException(
		    "Missing class URI or class not registered!");
	addType(classURI, true);
    }

    /**
     * Creates an instance of ManagedIndividual with a given URI.
     * 
     * @param uri
     *            The URI.
     */
    protected ManagedIndividual(String uri) {
	super(uri);
	String classURI = getClassURI();
	if (classURI == null || uriClassMap.get(classURI) != this.getClass())
	    throw new RuntimeException(
		    "Missing class URI or class not registered!");
	addType(classURI, true);
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
	String classURI = getClassURI();
	if (classURI == null || uriClassMap.get(classURI) != this.getClass())
	    throw new RuntimeException(
		    "Missing class URI or class not registered!");
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

	Class clz1 = (Class) uriClassMap.get(supertypeURI);
	Class clz2 = (Class) uriClassMap.get(subtypeURI);
	return clz1 != null && clz2 != null && clz1.isAssignableFrom(clz2);
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

	Class clz = (Class) uriClassMap.get(typeURI);
	return (clz == null) ? TypeMapper.isCompatible(typeURI, TypeMapper
		.getDatatypeURI(value)) : clz
		.isAssignableFrom(value.getClass());
    }

    /**
     * Returns the restrictions that apply to the given property in the context
     * of this class. As this class has no properties, this implementation
     * returns null. Subclasses <b>must</b> overwrite this method.
     * 
     * @param propURI
     *            URI of the property.
     */
    public static Restriction getClassRestrictionsOnProperty(String propURI) {
	return null;
    }

    /**
     * Returns the restrictions that apply to the given property in the context
     * of the class with the given URI. This method creates a new instance of
     * the class with the given URI and calls the method
     * {@link org.universAAL.middleware.owl.ManagedIndividual#getClassRestrictionsOnProperty(String)}
     * for that object.
     * 
     * @param classURI
     *            URI of the class for which the restrictions apply.
     * @param propURI
     *            URI of the property.
     */
    public static final Restriction getClassRestrictionsOnProperty(
	    String classURI, String propURI) {
	try {
	    return (Restriction) getRegisteredClass(classURI).getMethod(
		    "getClassRestrictionsOnProperty",
		    new Class[] { String.class }).invoke(null,
		    new Object[] { propURI });
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Must return the list of all class members if they all are known and if
     * the class can guarantee that no other members can be created after a call
     * to this method. Subclasses that implement an enumeration <b>must</b>
     * overwrite this method.
     */
    public static ManagedIndividual[] getEnumerationMembers() {
	return null;
    }

    /**
     * Returns the list of all members of the class with the given URI if they
     * all are known to the class, guaranteeing that no other members can be
     * created after a call to this method. It returns null if no class with the
     * given URI is registered or the class can not guarantee the above
     * conditions.
     */
    public static final ManagedIndividual[] getEnumerationMembers(
	    String classURI) {
	try {
	    return (ManagedIndividual[]) getRegisteredClass(classURI)
		    .getMethod("getEnumerationMembers", null)
		    .invoke(null, null);
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Must return the distinguished instance with the given URI, if such an
     * instance can be identified by the class. Subclasses that have some
     * enumerated instances <b>must</b> overwrite this method.
     */
    public static ManagedIndividual getIndividualByURI(String instanceURI) {
	return null;
    }

    /**
     * Returns an instance of a registered subclass selected by the given class
     * URI. Only if the selected subclass has a distinguished instance with the
     * given URI, the returned instance will be well-formed, otherwise it is
     * just an "empty" one that must be "filled in" by setting its properties.
     */
    public static final ManagedIndividual getInstance(String classURI,
	    String instanceURI) {
	if (classURI == null)
	    return null;

	Class clz = (Class) uriClassMap.get(classURI);
	if (clz == null)
	    return null;

	try {
	    if (isAnonymousURI(instanceURI)) {
		try {
		    return (ManagedIndividual) clz.newInstance();
		} catch (Exception e1) {
		    return (ManagedIndividual) clz.getConstructor(
			    new Class[] { String.class }).newInstance(
			    new Object[] { instanceURI });
		}
	    } else {
		try {
		    Object tmp = clz.getMethod("getIndividualByURI",
			    new Class[] { String.class }).invoke(null,
			    new Object[] { instanceURI });
		    if (tmp instanceof ManagedIndividual)
			return (ManagedIndividual) tmp;
		} catch (Exception e1) {
		}
		return (ManagedIndividual) clz.getConstructor(
			new Class[] { String.class }).newInstance(
			new Object[] { instanceURI });
	    }
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * For a given set of URIs get the class that is most specialized, i.e. all
     * other classes are super classes of this class. The method can be used for
     * transformations to/from other representations, e.g. turtle, jena.
     * 
     * @param classURIs
     *            The set of URIs of classes.
     * @return The URI of the most specialized class.
     */
    public static final String getMostSpecializedClass(String[] classURIs) {
	if (classURIs == null)
	    return null;

	String result = null;
	Class tmp, clz = null;
	for (int i = 0; i < classURIs.length; i++) {
	    tmp = (Class) uriClassMap.get(classURIs[i]);
	    if (tmp != null)
		if (clz == null || clz.isAssignableFrom(tmp)) {
		    result = classURIs[i];
		    clz = tmp;
		}
	}

	return result;
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

	Class clz = (Class) uriClassMap.get(classURI);
	if (clz == null)
	    return null;

	ArrayList al = new ArrayList();
	while (true) {
	    clz = clz.getSuperclass();
	    if (clz == null)
		break;
	    else if (!Modifier.isAbstract(clz.getModifiers())) {
		classURI = (String) classURIMap.get(clz.getName());
		if (classURI != null)
		    al.add(classURI);
	    }
	}

	return al.isEmpty() ? null : (String[]) al
		.toArray(new String[al.size()]);
    }

    /**
     * Returns a human readable description on the essence of this ontology
     * class.
     */
    public static String getRDFSComment() {
	return "The root of the whole class hierarchy in the uAAL ontology.";
    }

    /**
     * Returns the value of the property <code>rdfs:comment</code> on the given
     * <code>owl:Class</code> from the underlying ontology, assumed to be a
     * subclass of <code>ManagedIndividual</code>.
     */
    public static final String getRDFSComment(String classNameOrURI) {
	try {
	    return (String) getRegisteredClass(classNameOrURI).getMethod(
		    "getRDFSComment", null).invoke(null, null);
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Returns a label with which this ontology class can be introduced to human
     * users.
     */
    public static String getRDFSLabel() {
	return "uAAL Ontology Root Class";
    }

    /**
     * Returns the value of the property <code>rdfs:label</code> of the given
     * <code>owl:Class</code> from the underlying ontology, assumed to be a
     * subclass of <code>ManagedIndividual</code>.
     */
    public static final String getRDFSLabel(String classNameOrURI) {
	try {
	    return (String) getRegisteredClass(classNameOrURI).getMethod(
		    "getRDFSLabel", null).invoke(null, null);
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Returns the {@link java.lang.Class} object that has been registered by
     * the given Java class name or URI. Returns null, if no class with the
     * given URI or name was registered before.
     */
    public static final Class getRegisteredClass(String classNameOrURI) {
	// make sure we have a URI
	classNameOrURI = getRegisteredClassURI(classNameOrURI);
	// return the class associated with the URI at hand
	return (classNameOrURI == null) ? null : (Class) uriClassMap
		.get(classNameOrURI);
    }

    /**
     * In case that the given parameter is the URI of a registered class, the
     * same will be returned; in case that the given parameter is the Java class
     * name of a registered class, the URI of the class will be returned;
     * otherwise the returned value will be <code>null</code>.
     */
    public static final String getRegisteredClassURI(String classNameOrURI) {
	if (classNameOrURI != null) {
	    String uri = (String) classURIMap.get(classNameOrURI);
	    if (uri != null)
		classNameOrURI = uri;
	}
	return classNameOrURI;
    }

    /**
     * Returns the standard list of URIs of the properties that instances of the
     * class are expected to have. As the root of the class hierarchy in the
     * uAAL ontology, ManagedIndividual itself has no standard properties, but
     * the subclasses must overwrite this method and return the proper list.
     */
    public static String[] getStandardPropertyURIs() {
	return new String[0];
    }

    /**
     * If the given <code>classNameOrURI</code> can be resolved to a registered
     * subclass that obeys the conventions of ManagedIndividual, this method
     * invokes the implementation of {@link #getStandardPropertyURIs()} by that
     * class in order to return the standard list of URIs of the properties that
     * instances of the class are expected to have.
     */
    public static final String[] getStandardPropertyURIs(String classNameOrURI) {
	try {
	    return (String[]) getRegisteredClass(classNameOrURI).getMethod(
		    "getStandardPropertyURIs", null).invoke(null, null);
	} catch (Exception e) {
	    return null;
	}
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
     * If the given <code>classNameOrURI</code> can be resolved to a registered
     * subclass that obeys the conventions of ManagedIndividual, this method can
     * answer if the class implements an enumeration.
     */
    public static final boolean isEnumerationClass(String classNameOrURI) {
	try {
	    return getRegisteredClass(classNameOrURI).getMethod(
		    "getEnumerationMembers", null).invoke(null, null) != null;
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Checks if a registered class with the given Java class name can be found.
     */
    public static final boolean isRegistedredClassName(String className) {
	return className != null && classURIMap.containsKey(className);
    }

    /**
     * Checks if a registered class with the given URI can be found.
     */
    public static final boolean isRegisteredClassURI(String uri) {
	return uri != null && uriClassMap.containsKey(uri);
    }

    /**
     * Register a new managed individual. All ontology classes have to call this
     * method.
     * 
     * @param clz
     *            The class to register.
     */
    protected static final void register(Class clz) {
	String msg = null;
	try {
	    String className = clz.getName();
	    String classURI = (String) classURIMap.get(className);
	    if (classURI == null) {
		classURI = (String) clz.getField("MY_URI").get(null);
		if (classURI == null)
		    msg = "Missing class URI!";
		else if (uriClassMap.containsKey(classURI))
		    if (uriClassMap.get(classURI) == clz) {
			// very strange that the two maps are not synch!
			classURIMap.put(className, classURI);
			return;
		    } else
			msg = "Another class already registered with the same URI (" + classURI + ") !";
	    } else if (classURI.equals(clz.getField("MY_URI").get(null))) {
		if (clz == uriClassMap.get(classURI))
		    // duplicate registration: everything consistent although a
		    // little bit strange
		    return;
		else
		    msg = "Another class already registered with the same URI (" + classURI + ")!";
	    } else
		msg = "Another class already registered with the same class name!";
	    if (msg == null)
		if (ManagedIndividual.class.isAssignableFrom(clz)
			|| clz.isInterface()) {
		    uriClassMap.put(classURI, clz);
		    classURIMap.put(className, classURI);
		    return;
		} else
		    msg = "Not a subclass!";
	} catch (Exception e) {
	    msg = "Missing class URI!";
	}
	throw new RuntimeException(msg);
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
     * Get a list of URIs of all standard properties for this class.
     */
    public String[] getClassStandardPropertyURIs() {
	try {
	    return (String[]) getClass().getMethod("getStandardPropertyURIs",
		    null).invoke(null, null);
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Get the class URI for this ManagedIndividual.
     * 
     * @return The class URI.
     */
    public String getClassURI() {
	return (String) classURIMap.get(this.getClass().getName());
	// try {
	// return (String) this.getClass().getField("MY_URI").get(null);
	// } catch (Exception e) {
	// return null;
	// }
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
	String[] propURIs = getClassStandardPropertyURIs();
	String classURI = getClassURI();
	for (int i = 0; i < propURIs.length; i++) {
	    Restriction r = getClassRestrictionsOnProperty(classURI,
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

	Restriction r = getClassRestrictionsOnProperty((String) classURIMap
		.get(this.getClass().getName()), propURI);
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
	    // to read it
	    // for checking the membership
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
		Restriction r = getClassRestrictionsOnProperty(getClassURI(),
			propPath[fromIndex]);
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
}
