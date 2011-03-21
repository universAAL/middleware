/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.middleware.context;

import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.Resource;

/**
 * Instances of this class can be used to exchange info about the state of
 * context elements using the model of RDF statements. The subject and the
 * predicate of the RDF statement together identify a context element uniquely
 * and the object of the RDF statement specifies its state at a time provided as
 * a timestamp. Other properties can be used to give more info about the
 * validity and provider of the statement, to name a few.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class ContextEvent extends Resource {
    public static final String uAAL_CONTEXT_NAMESPACE = uAAL_NAMESPACE_PREFIX
	    + "Context.owl#";

    public static final String MY_URI = uAAL_CONTEXT_NAMESPACE + "ContextEvent";
    static {
	addResourceClass(MY_URI, ContextEvent.class);
    }

    public static final String CONTEXT_EVENT_URI_PREFIX = "urn:org.universAAL.middleware.context.rdf:ContextEvent#";

    // Class properties:
    //
    // the basic mandatory properties are that of an rdf:Statement, namely
    // rdf:subject,
    // rdf:predicate, and rdf:object
    public static final String LOCAL_NAME_SUBJECT = "subject";
    public static final String PROP_RDF_SUBJECT = RDF_NAMESPACE
	    + LOCAL_NAME_SUBJECT;
    public static final String LOCAL_NAME_PREDICATE = "predicate";
    public static final String PROP_RDF_PREDICATE = RDF_NAMESPACE
	    + LOCAL_NAME_PREDICATE;
    public static final String LOCAL_NAME_OBJECT = "object";
    public static final String PROP_RDF_OBJECT = RDF_NAMESPACE
	    + LOCAL_NAME_OBJECT;

    // Indication of reliability: as a context event can be used for stating
    // both a measured
    // value and an inferred value, we have to define two properties:

    /**
     * The confidence of an inferred value or the probability for its
     * correctness as an integer between 0 and 100 to indicate the probability
     * in terms of percentage; e.g., a reasoner trying to find out what a person
     * is doing at a point in time may provide the percentage of reliability /
     * confidence of the reported activity based on its internal evaluation of
     * the used indications in the inference process. Any number not in range (<
     * 0 || > 100) should be interpreted as 'null'. Confidence is an optional
     * property.
     */
    public static final String LOCAL_NAME_CONFIDENCE = "hasConfidence";
    public static final String PROP_CONTEXT_CONFIDENCE = uAAL_CONTEXT_NAMESPACE
	    + LOCAL_NAME_CONFIDENCE;

    /*
     * Deprecated, because the accuracy of measurements can be stated in
     * instances of (subclasses of) {@link
     * org.universAAL.middleware.context.owl.DimensionMeasure} or {@link
     * org.universAAL.middleware.context.owl.MultiDimensionMeasure} using {@link
     * org.universAAL.middleware.context.owl.DimensionMeasure#errorUpperBound}
     * and / or {@link
     * org.universAAL.middleware.context.owl.MultiDimensionMeasure
     * #cumulativeErrorUpperBound}. public static final String
     * LOCAL_NAME_ACCURACY = "hasAccuracy"; public static final String
     * PROP_CONTEXT_ACCURACY = uAAL_CONTEXT_NAMESPACE + LOCAL_NAME_ACCURACY;
     */

    // no matter, which of the above types of reliability indications apply to
    // the context
    // event at hand, the specification of accuracy can be (1) a "one time"
    // value at the level
    // of the context provider (this is why the same properties are defined also
    // within the {@link ContextProvider} class), as in case of a GPS receiver
    // that for each
    // measured location guarantees that the reported value would be within a
    // radius of 3
    // meters from the expected exact value, or (2) a different value for each
    // context event,
    // as in case of a reasoner that each time derives the state of the relevant
    // context
    // element using several parameters with different confidence levels.

    /**
     * The provider of the contextual info. The middleware is supposed to fill
     * this field with the info provided by a corresponding ContextPublisher at
     * its registration time.
     */
    public static final String LOCAL_NAME_PROVIDER = "hasProvider";
    public static final String PROP_CONTEXT_PROVIDER = uAAL_CONTEXT_NAMESPACE
	    + LOCAL_NAME_PROVIDER;

    /**
     * An optional property of context events is its expiration time which says
     * until when the reported event can still be considered as valid; after
     * that point in time the value should be considered as unknown until a new
     * event reports a new value or confirms the old one.
     * <p>
     * When not set, values reported will remain valid until they are overridden
     * by a more up-to-date event.
     */
    public static final String LOCAL_NAME_EXPIRATION_TIME = "hasExpirationTime";
    public static final String PROP_CONTEXT_EXPIRATION_TIME = uAAL_CONTEXT_NAMESPACE
	    + LOCAL_NAME_EXPIRATION_TIME;

    /**
     * A timestamp, as a Long value to be interpreted as the number of
     * milliseconds from 01.01.1970, will be set automatically as soon as a
     * context provider builds an instance of ContextEvent using the
     * {@link #ContextEvent(Resource, String)} constructor. However, when the
     * middleware constructs a context event in the course of deserializing a
     * context event using the constructors inherited from Resource, then it can
     * set the timestamp 'manually'.
     */
    public static final String LOCAL_NAME_TIMESTAMP = "hasTimestamp";
    public static final String PROP_CONTEXT_TIMESTAMP = uAAL_CONTEXT_NAMESPACE
	    + LOCAL_NAME_TIMESTAMP;

    public static ContextEvent constructSimpleEvent(String subjectURI,
	    String subjectTypeURI, String predicate, Object object) {
	if (subjectURI == null || subjectTypeURI == null || predicate == null
		|| object == null)
	    return null;

	Resource subject = null;
	if (ManagedIndividual.isRegisteredClassURI(subjectTypeURI))
	    subject = ManagedIndividual.getInstance(subjectTypeURI, subjectURI);
	else {
	    subject = new Resource(subjectURI);
	    subject.addType(subjectTypeURI, false);
	}
	subject.setProperty(predicate, object);

	return new ContextEvent(subject, predicate);
    }

    /**
     * This constructor is for the exclusive usage by deserializers.
     */
    public ContextEvent(String uri) {
	super(uri);
	if (!uri.startsWith(CONTEXT_EVENT_URI_PREFIX))
	    throw new RuntimeException("Invalid instance URI!");
	addType(MY_URI, true);
    }

    public ContextEvent(Resource subject, String predicate) {
	super(CONTEXT_EVENT_URI_PREFIX, 8);

	if (subject == null || predicate == null)
	    throw new RuntimeException("Invalid null value!");

	Object eventObject = subject.getProperty(predicate);
	if (eventObject == null)
	    throw new RuntimeException("Event object not set!");

	addType(MY_URI, true);
	setRDFSubject(subject);
	setRDFPredicate(predicate);
	setRDFObject(eventObject);
	setTimestamp(new Long(System.currentTimeMillis()));
    }

    /*
     * deprecated public Rating getAccuracy() { return (Rating)
     * getProperty(PROP_CONTEXT_ACCURACY); }
     */

    public Integer getConfidence() {
	return (Integer) getProperty(PROP_CONTEXT_CONFIDENCE);
    }

    public Long getExpirationTime() {
	return (Long) getProperty(PROP_CONTEXT_EXPIRATION_TIME);
    }

    public int getPropSerializationType(String propURI) {
	return (PROP_RDF_SUBJECT.equals(propURI) || PROP_CONTEXT_PROVIDER
		.equals(propURI)) ? PROP_SERIALIZATION_REDUCED
		: PROP_SERIALIZATION_FULL;
    }

    public Object getRDFObject() {
	return getProperty(PROP_RDF_OBJECT);
    }

    public String getRDFPredicate() {
	Object o = getProperty(PROP_RDF_PREDICATE);
	return (o instanceof Resource) ? o.toString() : null;
    }

    public ContextProvider getProvider() {
	return (ContextProvider) props.get(PROP_CONTEXT_PROVIDER);
    }

    public Resource getRDFSubject() {
	return (Resource) getProperty(PROP_RDF_SUBJECT);
    }

    public String getSubjectTypeURI() {
	Resource subject = (Resource) getProperty(PROP_RDF_SUBJECT);
	return (subject == null) ? null : subject.getType();
    }

    public String getSubjectURI() {
	Resource subject = (Resource) getProperty(PROP_RDF_SUBJECT);
	return (subject == null) ? null : subject.getURI();
    }

    public Long getTimestamp() {
	return (Long) getProperty(PROP_CONTEXT_TIMESTAMP);
    }

    public boolean isWellFormed() {
	return (getRDFSubject() != null && getRDFPredicate() != null
		&& getRDFObject() != null && getTimestamp() != null);
    }

    /*
     * deprecated public void setAccuracy(Rating accuracy) { if (accuracy !=
     * null && !props.containsKey(PROP_CONTEXT_ACCURACY))
     * props.put(PROP_CONTEXT_ACCURACY, accuracy); }
     */

    public void setConfidence(Integer confidence) {
	if (confidence != null && confidence.intValue() >= 0
		&& confidence.intValue() <= 100
		&& !props.containsKey(PROP_CONTEXT_CONFIDENCE))
	    props.put(PROP_CONTEXT_CONFIDENCE, confidence);
    }

    public void setExpirationTime(Long expirationTime) {
	if (expirationTime != null && expirationTime.longValue() > 0
		&& !props.containsKey(PROP_CONTEXT_EXPIRATION_TIME))
	    props.put(PROP_CONTEXT_EXPIRATION_TIME, expirationTime);
    }

    public void setRDFObject(Object o) {
	if (o != null && !props.containsKey(PROP_RDF_OBJECT))
	    props.put(PROP_RDF_OBJECT, o);
    }

    public void setRDFPredicate(String propURI) {
	if (propURI != null && propURI.lastIndexOf('#') > 0
		&& !props.containsKey(PROP_RDF_PREDICATE))
	    props.put(PROP_RDF_PREDICATE, new Resource(propURI));
    }

    public void setProvider(ContextProvider src) {
	if (src != null && !props.containsKey(PROP_CONTEXT_PROVIDER))
	    props.put(PROP_CONTEXT_PROVIDER, src);
    }

    public void setRDFSubject(Resource subj) {
	if (subj != null && !props.containsKey(PROP_RDF_SUBJECT))
	    props.put(PROP_RDF_SUBJECT, subj);
    }

    public void setTimestamp(Long timestamp) {
	if (timestamp != null && timestamp.longValue() > 0
		&& !props.containsKey(PROP_CONTEXT_TIMESTAMP))
	    props.put(PROP_CONTEXT_TIMESTAMP, timestamp);
    }

    public void setProperty(String propURI, Object value) {
	if (propURI == null)
	    return;

	if (propURI.equals(PROP_RDF_OBJECT)) {
	    setRDFObject(value);
	    /*
	     * deprecated } else if (value instanceof Rating) { if
	     * (propURI.equals(PROP_CONTEXT_ACCURACY)) setAccuracy((Rating)
	     * value);
	     */
	} else if (value instanceof ContextProvider) {
	    if (propURI.equals(PROP_CONTEXT_PROVIDER))
		setProvider((ContextProvider) value);
	} else if (value instanceof Resource) {
	    if (propURI.equals(PROP_RDF_SUBJECT))
		setRDFSubject((Resource) value);
	    else if (propURI.equals(PROP_RDF_PREDICATE))
		setRDFPredicate(((Resource) value).getURI());
	} else if (value instanceof String) {
	    if (propURI.equals(PROP_RDF_PREDICATE))
		setRDFPredicate((String) value);
	} else if (value instanceof Long) {
	    if (propURI.equals(PROP_CONTEXT_TIMESTAMP))
		setTimestamp((Long) value);
	    else if (propURI.equals(PROP_CONTEXT_EXPIRATION_TIME))
		setExpirationTime((Long) value);
	} else if (value instanceof Integer) {
	    if (propURI.equals(PROP_CONTEXT_CONFIDENCE))
		setConfidence((Integer) value);
	}
    }
}
