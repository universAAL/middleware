package org.universAAL.middleware.rdf;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Interface for {@link Resource}; only temporary to ensure compatibility with
 * old versions. DO NOT USE! It will be removed shortly.
 * 
 * This interface was created for the renaming of ClassExpression to
 * TypeExpression and AbstractRestriction to PropertyRestriction.
 */
public interface IResource {

    /**
     * Set or add the type of this Resource. The type complies to rdf:type. A
     * Resource can have multiple types.
     * 
     * @param typeURI
     *            URI of the type.
     * @param blockFurtherTypes
     *            If true, no further types can be added.
     */
    public void addType(String typeURI, boolean blockFurtherTypes);

    /**
     * If this Resource represents an RDF, retrieve the elements as
     * {@link java.util.List}.
     * 
     * @return The list containing the elements of this RDF list.
     */
    public List asList();

    /**
     * If this Resource represents an RDF, retrieve the elements as
     * {@link java.util.List}.
     * 
     * @param l
     *            The list to store the elements of this RDF list.
     */
    public void asList(List l);

    /**
     * Change the value (RDF object) of the specified property (RDF predicate)
     * to the given object. If the value can't be set, it is ensured that the
     * original value is restored.
     * 
     * @param propURI
     *            The value has to be changed for this property.
     * @param value
     *            The new value.
     * @return true, if the value could be set.
     */
    public boolean changeProperty(String propURI, Object value);

    /** If this object is an XML Literal, create a copy of it. */
    public Resource copyAsXMLLiteral();

    /**
     * Create a deep copy of this Resource, i.e. create a new Resource for this
     * object (only a Resource, but not a derived class) and for the resources
     * of all properties.<br>
     * Currently, only resources are copies, but not list of resources.
     * 
     * @return The copied Resource.
     */
    // TODO: only resources are copies, but not list of resources.
    // TODO: will create an infinite loop for cycles.
    public Resource deepCopy();

    /** Determines if this Resource equals the specified Resource. */
    public boolean equals(Object other);

    /** Get the Resource comment. Convenient method to retrieve rdfs:comment. */
    public String getResourceComment();

    /** Get the Resource label. Convenient method to retrieve rdfs:label. */
    public String getResourceLabel();

    /**
     * If this resource has no original label, constructs one for it without
     * changing the resource itself.
     * 
     * @param type
     *            The optional type to be used instead of the return value of
     *            'getType()' when constructing a label
     * @return if there is an original label, that one is returned; otherwise a
     *         label constructed on-the-fly will be returned
     */
    public String getOrConstructLabel(String type);

    /**
     * Get the local name which is the part of the URI after the delimiter
     * ('#').
     * 
     * @see #getNamespace()
     */
    public String getLocalName();

    /**
     * Get the namespace of the URI which is the start of the URI including the
     * delimiter ('#'). It is the URI without the local name.
     * 
     * @see #getLocalName()
     */
    public String getNamespace();

    /** Get the RDF object for a specified property. */
    public Object getProperty(String propURI);

    /** Get all properties, i.e. all RDF predicates for this Resource. */
    public Enumeration getPropertyURIs();

    /**
     * Answers if the given property has to be considered when serializing this
     * individual in a minimized way, and if not ignore-able, whether its value
     * should be presented in its full form or can be reduced. The return value
     * must be one of {@link #PROP_SERIALIZATION_OPTIONAL},
     * {@link #PROP_SERIALIZATION_REDUCED}, or {@link #PROP_SERIALIZATION_FULL}.
     * It can be assumed that the given property is one of those returned by
     * {@link #getPropertyURIs()}. <br>
     * Decision criterion should be if the value of this property is absolutely
     * necessary when this resource is being sent to a remote node. If the
     * subclass rates it as unlikely that the receiver side would need this
     * info, the answer should be <code>PROP_SERIALIZATION_OPTIONAL</code> in
     * favor of lower communication traffic and higher performance even at risk
     * of a possible additional query on the receiver side for fetching this
     * info. With the same rationale, if a property should be included in the
     * process of serialization, it is preferable to include it in a reduced
     * form; in this case the return value should be
     * <code>PROP_SERIALIZATION_REDUCED</code>, otherwise
     * <code>PROP_SERIALIZATION_FULL</code> can be returned.
     * 
     * Subclasses should normally overwrite this method as this default
     * implementation returns always <code>PROP_SERIALIZATION_FULL</code>.
     */
    public int getPropSerializationType(String propURI);

    /**
     * Helper method to get the static field of the java class with the given
     * field name. If the field is not defined in this class, the given default
     * value is returned.
     * 
     * @param fieldName
     *            Name of the static field of the java class to retrieve.
     * @param defaultValue
     *            Default value, if the field could not be retrieved.
     * @return The value of the field.
     */
    public Object getStaticFieldValue(String fieldName, Object defaultValue);

    /**
     * Returns the URI of the first type added to the list of types of this
     * resource.
     */
    public String getType();

    /**
     * Returns the URIs of all known types of this resource.
     */
    public String[] getTypes();

    /** Get the URI. */
    public String getURI();

    /** Get the has code for this Resource, calculated from the URI. */
    public int hashCode();

    /** Determines if this Resource has the specified property. */
    public boolean hasProperty(String propURI);

    /**
     * Determines if this Resource has a qualified, i.e. the URI has a delimiter
     * ('#').
     */
    public boolean hasQualifiedName();

    /** Determines if this Resource has an anonymous URI. */
    public boolean isAnon();

    /**
     * Returns true if the value of the given property should be treated as an
     * rdf:List. Serializers can use this to determine if a multi-valued
     * property should be serialized using the concept of rdf:List or the
     * property should appear as often as the number of values assigned to the
     * property. The default behavior is that a property associated with an
     * instance of {@link java.util.List} is assumed to be a closed collection.
     * Subclasses can change this, if needed.
     */
    public boolean isClosedCollection(String propURI);

    /**
     * Returns true, if the state of the resource is valid, otherwise false.
     * <p>
     * Subclasses should overwrite this methods as the default implementation
     * returns always true.
     */
    public boolean isWellFormed();

    /**
     * Returns the number of properties, i.e. the number of RDF predicates for
     * this Resource.
     */
    public int numberOfProperties();

    public boolean representsQualifiedURI();

    /**
     * Resources to be serialized and parsed as rdf:XMLLiteral must overwrite
     * this method and return true. Serializers and parsers can use this as a
     * hint.
     */
    public boolean serializesAsXMLLiteral();

    /** Set the Resource comment. Convenient method to set rdfs:comment. */
    public void setResourceComment(String comment);

    /** Set the Resource label. Convenient method to set rdfs:label. */
    public void setResourceLabel(String label);

    /**
     * Adds a statement with this resource as the subject, the given
     * <code>propURI</code> as the predicate and the given value as the object.
     * Subclasses must override this in order to decide if the statement to be
     * added fits the general class constraints. If not, the call of this method
     * should be ignored. For each property only one single call may be made to
     * this method, unless subsequent calls to this method for setting the value
     * of the same property are treated as an update for an update-able
     * property. Multi-valued properties must be set using an instance of
     * {@link java.util.List}. The differentiation, if a such list should be
     * treated as an rdf:List, can be made with the help of
     * {@link #isClosedCollection(String)}. The default implementation here
     * accepts all property-value pairs blindly except for rdf:type which is
     * handled if the value is a type URI, a Resource or a java.util.List of
     * them.
     * <p>
     * Note: The setting of the property rdf:type is being handled by this class
     * via the final methods {@link #addType(String, boolean)},
     * {@link #getType()}, and {@link #getTypes()}. Although these methods give
     * the view of handling type URIs as strings, but in reality the types are
     * stored as direct instances of this class. So, the subclasses should
     * ignore calls for setting rdf:type; if not, then the subclass must pay
     * attention that the value should be a {@link List} of direct instances of
     * this class so that (1) the {@link #toString()} method returns just the
     * URI and (2) the serializers get no problems with the value. Also,
     * settings via subclasses may be overwritten by this class if a subsequent
     * call to {@link #addType(String, boolean)} is made.
     */
    public void setProperty(String propURI, Object value);

    /**
     * Set the given value at the end of the given property path, but does not
     * force the setting.
     * 
     * @see #setPropertyPathFromOffset(String[], int, Object, boolean)
     */
    public boolean setPropertyPath(String[] propPath, Object value);

    /**
     * Set the given value at the end of the given property path.
     * 
     * @see #setPropertyPathFromOffset(String[], int, Object, boolean)
     */
    public boolean setPropertyPath(String[] propPath, Object value,
	    boolean force);

    /**
     * Change or add the Resource at the end of the given property path to the
     * given value. This method starts from this Resource and follows the given
     * property path through the RDF graph. If a property from the path does not
     * yet exist, a new anonymous Resource is automatically created. At the end
     * of the property path, the given value is set as RDF object with the last
     * property from the path as RDF predicate.
     * 
     * @param propPath
     *            The set of properties defining the path through the RDF graph.
     * @param fromIndex
     *            The property path is evaluated from this index on; if
     *            'fromIndex' is greater than zero, then some entries at the
     *            beginning are just ignored.
     * @param value
     *            The value to set at the end of the property path
     * @param force
     *            Determines if setting the value has to be forced. If true,
     *            {@link #changeProperty(String, Object)} is called, otherwise
     *            {@link #setProperty(String, Object)} is called.
     * @return true, if the operation was successful.
     */
    // TODO: check if fromIndex > propPath.length (otherwise results in infinite
    // loop)
    public boolean setPropertyPathFromOffset(String[] propPath, int fromIndex,
	    Object value, boolean force);

    /** Get a String representation of this Resource; returns the URI. */
    public String toString();

    /**
     * Debug method: get a string of this RDF graph.
     * 
     * @return The graph as string.
     */
    public String toStringRecursive();

    /**
     * Debug method: get a string of this RDF graph.
     * 
     * @param prefix
     *            Indention string that every line starts with.
     * @param prefixAtStart
     *            True iff the first line should start with the prefix string.
     * @return The graph as string.
     */
    public String toStringRecursive(String prefix, boolean prefixAtStart,
	    Hashtable visitedElements);

}