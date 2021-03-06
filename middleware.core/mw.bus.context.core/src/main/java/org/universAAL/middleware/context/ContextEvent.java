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
package org.universAAL.middleware.context;

import org.universAAL.middleware.bus.model.matchable.Event;
import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ScopedResource;
import org.universAAL.middleware.util.ResourceUtil;

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
public class ContextEvent extends ScopedResource implements Event {
	public static final String CONTEXT_NAMESPACE = NAMESPACE_PREFIX + "Context.owl#";

	public static final String MY_URI = CONTEXT_NAMESPACE + "ContextEvent";

	public static final String CONTEXT_EVENT_URI_PREFIX = "urn:org.universAAL.middleware.context.rdf:ContextEvent#";

	// Class properties:
	//
	// the basic mandatory properties are that of an rdf:Statement, namely
	// rdf:subject,
	// rdf:predicate, and rdf:object
	public static final String LOCAL_NAME_SUBJECT = "subject";
	public static final String PROP_RDF_SUBJECT = RDF_NAMESPACE + LOCAL_NAME_SUBJECT;
	public static final String LOCAL_NAME_PREDICATE = "predicate";
	public static final String PROP_RDF_PREDICATE = RDF_NAMESPACE + LOCAL_NAME_PREDICATE;
	public static final String LOCAL_NAME_OBJECT = "object";
	public static final String PROP_RDF_OBJECT = RDF_NAMESPACE + LOCAL_NAME_OBJECT;

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
	public static final String PROP_CONTEXT_CONFIDENCE = CONTEXT_NAMESPACE + LOCAL_NAME_CONFIDENCE;

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
	 * PROP_CONTEXT_ACCURACY = CONTEXT_NAMESPACE + LOCAL_NAME_ACCURACY;
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
	public static final String PROP_CONTEXT_PROVIDER = CONTEXT_NAMESPACE + LOCAL_NAME_PROVIDER;

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
	public static final String PROP_CONTEXT_EXPIRATION_TIME = CONTEXT_NAMESPACE + LOCAL_NAME_EXPIRATION_TIME;

	/**
	 * A timestamp, as a Long value to be interpreted as the number of
	 * milliseconds from 01.01.1970, that will be set automatically as soon as a
	 * context provider builds an instance of ContextEvent using the
	 * {@link #ContextEvent(Resource, String)} constructor. Therefore, this
	 * timestamp can be interpreted as the reporting timestamp as opposed to
	 * {@link #PROP_CONTEXT_OCCURRENCE_TIMESTAMP}, which can be set in order to
	 * indicate that the actual time of occurrence for this event has been different 
	 * from its reporting time.
	 */
	public static final String LOCAL_NAME_TIMESTAMP = "hasTimestamp";
	public static final String PROP_CONTEXT_TIMESTAMP = CONTEXT_NAMESPACE + LOCAL_NAME_TIMESTAMP;
	
	/**
	 * Helpful for 'controllers' that have just made a change effect requested from them through 
	 * a service call received from the universAAL environment and now want to reflect this same change
	 * to the universAAL environment as a context event. In such a case, they can set this flag when
	 * creating a context event in order for the receivers of the context event to be able to 
	 * differentiate between external causes not in control of the universAAL environment and those
	 * caused in control of the universAAL environment.
	 */
	public static final String PROP_CONTEXT_REFLECTS_CHANGE_REQUEST = CONTEXT_NAMESPACE + "reflectsChangeRequest";
	
	/**
	 * As opposed to {@link #PROP_CONTEXT_TIMESTAMP} that is set automatically equal to the time of
	 * creating a certain instance of ContextEvent, this timestamp can be set by providing an additional
	 * parameter to the constructor to indicate that the change reported by this context event actually
	 * happened earlier at the specified time. Therefore, this parameter may remain unset; but if set,
	 * then it has to have a positive value smaller than the current time used for 
	 * {@link #PROP_CONTEXT_TIMESTAMP}. 
	 */
	public static final String PROP_CONTEXT_OCCURRENCE_TIMESTAMP = CONTEXT_NAMESPACE + "occurrenceTimestamp";
	
	/**
	 * When the occurrence time cannot be determined in "real time" (e.g., when the occurrence is detected
	 * by periodic check to see if a value has changed), this property can be used to indicate that the exact 
	 * occurrence time is not known but this is the best estimated occurrence time.
	 * <p><b><u>Recommendations:</u></b></p>
	 * <ul><li>When creating a context event is triggered based on "periodic check", this prop should be set
	 * equal to the average of the previous and current check times.</li>
	 * <li>When during the same "check" several changes are detected (e.g., when five motion sensors are checked
	 * among which two had a value change since the last check), all reported changes should have the same mean
	 * occurrence time in order to indicate that the precedence among them cannot be determined.</li></ul>
	 */
	public static final String PROP_CONTEXT_MEAN_OCCURRENCE_TIME = CONTEXT_NAMESPACE + "meanOccurrentTime";

	/**
	 * Constructs a CHe stub ContextEvent according to the parameters passed
	 *
	 * @param subjectURI
	 *            URI of the subject. Must not be null.
	 * @param subjectTypeURI
	 *            URI of the subject type. Must not be null.
	 * @param predicate
	 *            URI of the predicate. Must not be null.
	 * @param object
	 *            The object of the event. Must not be null.
	 * @return the ContextEvent.
	 */
	public static ContextEvent constructSimpleEvent(String subjectURI, String subjectTypeURI, String predicate,
			Object object) {
		if (subjectURI == null || subjectTypeURI == null || predicate == null || object == null)
			return null;

		Resource subject = null;
		if (OntologyManagement.getInstance().isRegisteredClass(subjectTypeURI, true))
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

	/**
	 * Construct a CHe stub ContextEvent inferring the object from the predicate
	 * which URI is present in the properties of the subject
	 *
	 * @param subject
	 *            The Resource representing the subject of the event. Must
	 *            include the property specified in the second parameter, and
	 *            must have a certain value
	 * @param predicate
	 *            The property of the subject that will be used as object in the
	 *            event
	 */
	public ContextEvent(Resource subject, String predicate) {
		super(CONTEXT_EVENT_URI_PREFIX, 8);

		if (subject == null || predicate == null)
			throw new NullPointerException("Invalid null value!");

		Object eventObject = subject.getProperty(predicate);
		if (eventObject == null)
			throw new RuntimeException("Event object not set!");

		addType(MY_URI, true);
		setRDFSubject(subject);
		setRDFPredicate(predicate);
		setRDFObject(eventObject);
		setTimestamp(new Long(System.currentTimeMillis()));
	}
	
	/**
	 * Similar to {@link #ContextEvent(Resource, String)} with the effect to set also one of
	 * {@link #PROP_CONTEXT_OCCURRENCE_TIMESTAMP} or {@link #PROP_CONTEXT_MEAN_OCCURRENCE_TIME}.
	 * These two properties may not exist simultaneously; therefore the constructor first checks
	 * `actualOccurrenceTime´ to see if it is a positive number less than the current time; if yes,
	 * it will set {@link #PROP_CONTEXT_OCCURRENCE_TIMESTAMP} with that value and will ignore
	 * `meanOccurrenceTime´. Otherwise `actualOccurrenceTime´  will be ignored and {@link 
	 * #PROP_CONTEXT_MEAN_OCCURRENCE_TIME} is set equal to `meanOccurrenceTime´
	 * if this parameters has a positive number less than the current time as value.
	 */
	public ContextEvent(Resource subject, String predicate, long actualOccurrenceTime, long meanOccurrenceTime) {
		this(subject, predicate);
		if (actualOccurrenceTime > 0  &&  actualOccurrenceTime < getTimestamp())
			props.put(PROP_CONTEXT_OCCURRENCE_TIMESTAMP, new Long(actualOccurrenceTime));
		else if (meanOccurrenceTime > 0  &&  meanOccurrenceTime < getTimestamp())
			props.put(PROP_CONTEXT_MEAN_OCCURRENCE_TIME, new Long(meanOccurrenceTime));
	}
	
	/**
	 * Similar to {@link #ContextEvent(Resource, String)} with the effect to set also
	 * {@link #PROP_CONTEXT_REFLECTS_CHANGE_REQUEST} to true.
	 */
	public ContextEvent(Resource subject, String predicate, boolean reflectsChangeRequest) {
		this(subject, predicate);
		if (reflectsChangeRequest)
			props.put(PROP_CONTEXT_REFLECTS_CHANGE_REQUEST, Boolean.TRUE);
	}
	
	/**
	 * Combines {@link #ContextEvent(Resource, String, boolean)} and {@link #ContextEvent(Resource, String, long, long)
	 * in one constructor..
	 */
	public ContextEvent(Resource subject, String predicate, boolean reflectsChangeRequest, long actualOccurrenceTime, long meanOccurrenceTime) {
		this(subject, predicate, actualOccurrenceTime, meanOccurrenceTime);
		if (reflectsChangeRequest)
			props.put(PROP_CONTEXT_REFLECTS_CHANGE_REQUEST, Boolean.TRUE);
	}

	/**
	 * Construct a CHe stub ContextEvent inferring the object from the predicate
	 * which URI is present in the properties of the subject
	 *
	 * @param subject
	 *            The Resource representing the subject of the event. Must
	 *            include the property specified in the second parameter, and
	 *            must have a certain value
	 * @param predicate
	 *            The property of the subject that will be used as object in the
	 *            event
	 */
	public ContextEvent(Resource subject, String predicate, Object object) {
		super(CONTEXT_EVENT_URI_PREFIX, 8);

		if (subject == null || predicate == null || object == null)
			throw new NullPointerException("Invalid null value!");

		if (!subject.setProperty(predicate, object))
			throw new RuntimeException("The property could not be set");

		addType(MY_URI, true);
		setRDFSubject(subject);
		setRDFPredicate(predicate);
		setRDFObject(object);
		setTimestamp(new Long(System.currentTimeMillis()));
	}

	/*
	 * deprecated public Rating getAccuracy() { return (Rating)
	 * getProperty(PROP_CONTEXT_ACCURACY); }
	 */

	/**
	 * Get the confidence of the event
	 *
	 * @return The confidence represented as a percentage (0 to 100)
	 */
	public Integer getConfidence() {
		return (Integer) getProperty(PROP_CONTEXT_CONFIDENCE);
	}

	/**
	 * Get the expiration time
	 *
	 * @return The amount of milliseconds after reception from which the
	 *         information in the event is no longer valid
	 */
	public Long getExpirationTime() {
		return (Long) getProperty(PROP_CONTEXT_EXPIRATION_TIME);
	}

	public int getPropSerializationType(String propURI) {
		return (PROP_RDF_SUBJECT.equals(propURI) || PROP_CONTEXT_PROVIDER.equals(propURI)) ? PROP_SERIALIZATION_REDUCED
				: PROP_SERIALIZATION_FULL;
	}

	/**
	 * Get the object of the event
	 *
	 * @return The object of the event (a Resource)
	 */
	public Object getRDFObject() {
		return getProperty(PROP_RDF_OBJECT);
	}

	/**
	 * Get the predicate of the event
	 *
	 * @return The URI of the predicate of the event
	 */
	public String getRDFPredicate() {
		Object o = getProperty(PROP_RDF_PREDICATE);
		return (o instanceof Resource) ? o.toString() : null;
	}

	/**
	 * Get the ContextProvider of the event
	 *
	 * @return The {@link org.universAAL.middleware.context.owl.ContextProvider}
	 *         representing the provider that originated the event
	 */
	public ContextProvider getProvider() {
		return (ContextProvider) props.get(PROP_CONTEXT_PROVIDER);
	}

	/**
	 * Get the subject of the event
	 *
	 * @return The {@link org.universAAL.middleware.rdf.Resource} that is the
	 *         subject to the event
	 */
	public Resource getRDFSubject() {
		return (Resource) getProperty(PROP_RDF_SUBJECT);
	}

	/**
	 * Get the type of the subject of the event
	 *
	 * @return The URI of the type of the subject to the event
	 */
	public String getSubjectTypeURI() {
		Resource subject = (Resource) getProperty(PROP_RDF_SUBJECT);
		return (subject == null) ? null : subject.getType();
	}

	/**
	 * Get the URI of the subject of the event
	 *
	 * @return The URI of the individual that is the subject to the event
	 */
	public String getSubjectURI() {
		Resource subject = (Resource) getProperty(PROP_RDF_SUBJECT);
		return (subject == null) ? null : subject.getURI();
	}

	/**
	 * Get the construction timestamp of the event.
	 *
	 * @return The timestamp, in UNIX format, associated with the event
	 */
	public Long getTimestamp() {
		return (Long) getProperty(PROP_CONTEXT_TIMESTAMP);
	}
	
	/**
	 * Returns the value set at the construction time for {@link #PROP_CONTEXT_OCCURRENCE_TIMESTAMP}
	 * or null if it was not set.
	 */
	public Long getActualOccurrenceTime() {
		return (Long) getProperty(PROP_CONTEXT_OCCURRENCE_TIMESTAMP);
	}
	
	/**
	 * Returns the value set at the construction time for {@link #PROP_CONTEXT_MEAN_OCCURRENCE_TIME}
	 * or null if it was not set.
	 */
	public Long getMeanOccurrenceTime() {
		return (Long) getProperty(PROP_CONTEXT_MEAN_OCCURRENCE_TIME);
	}
	
	public long getBestEstimatedOccurrenceTime() {
    Long l = getActualOccurrenceTime();
    if (l != null  &&  l.longValue() > 0)
      return l.longValue();
    
    l = getMeanOccurrenceTime();
    if (l != null  &&  l.longValue() > 0)
      return l.longValue();
      
    return getTimestamp().longValue();
	}
	
	/**
	 * Returns true iff the property {@link #PROP_CONTEXT_REFLECTS_CHANGE_REQUEST} has been set to true,
	 * otherwise false.
	 */
	public boolean reflectsChangeRequest() {
		return Boolean.TRUE == props.get(PROP_CONTEXT_REFLECTS_CHANGE_REQUEST);
	}

	public boolean isWellFormed() {
		return (getRDFSubject() != null && getRDFPredicate() != null && getRDFObject() != null
				&& getTimestamp() != null);
	}

	/*
	 * deprecated public void setAccuracy(Rating accuracy) { if (accuracy !=
	 * null && !props.containsKey(PROP_CONTEXT_ACCURACY))
	 * props.put(PROP_CONTEXT_ACCURACY, accuracy); }
	 */

	/**
	 * Set the confidence
	 *
	 * @param confidence
	 *            The confidence in percentage (0 to 100)
	 */
	public boolean setConfidence(Integer confidence) {
		if (confidence != null && confidence.intValue() >= 0 && confidence.intValue() <= 100
				&& !props.containsKey(PROP_CONTEXT_CONFIDENCE)) {
			props.put(PROP_CONTEXT_CONFIDENCE, confidence);
			return true;
		}
		return false;
	}

	/**
	 * Set the expiration time
	 *
	 * @param expirationTime
	 *            The amount of millisecond after which the event is not valid
	 *            afer reception
	 */
	public boolean setExpirationTime(Long expirationTime) {
		if (expirationTime != null && expirationTime.longValue() > 0
				&& !props.containsKey(PROP_CONTEXT_EXPIRATION_TIME)) {
			props.put(PROP_CONTEXT_EXPIRATION_TIME, expirationTime);
			return true;
		}
		return false;
	}

	/**
	 * Set the object
	 *
	 * @param o
	 */
	public boolean setRDFObject(Object o) {
		if (o != null && !props.containsKey(PROP_RDF_OBJECT)) {
			props.put(PROP_RDF_OBJECT, o);
			return true;
		}
		return false;
	}

	/**
	 * Set the predicate
	 *
	 * @param propURI
	 *            The URI of the predicate
	 */
	public boolean setRDFPredicate(String propURI) {
		if (propURI != null && propURI.lastIndexOf('#') > 0 && !props.containsKey(PROP_RDF_PREDICATE)) {
			props.put(PROP_RDF_PREDICATE, new Resource(propURI));
			return true;
		}
		return false;
	}

	/**
	 * Set the Context Provider
	 *
	 * @param src
	 */
	public boolean setProvider(ContextProvider src) {
		if (src != null && !props.containsKey(PROP_CONTEXT_PROVIDER)) {
			props.put(PROP_CONTEXT_PROVIDER, src);
			return true;
		}
		return false;
	}

	/**
	 * Set the subject
	 *
	 * @param subj
	 */
	public boolean setRDFSubject(Resource subj) {
		if (subj != null && !props.containsKey(PROP_RDF_SUBJECT)) {
			props.put(PROP_RDF_SUBJECT, subj);
			return true;
		}
		return false;
	}

	/**
	 * Sets {@link #PROP_CONTEXT_TIMESTAMP the original construction timestamp}.
	 *
	 * @param timestamp
	 *            The timestamp in UNIX format
	 */
	public boolean setTimestamp(Long timestamp) {
		if (timestamp != null && timestamp.longValue() > 0 && !props.containsKey(PROP_CONTEXT_TIMESTAMP)) {
			Long ots = getActualOccurrenceTime();
			if (ots == null  ||  ots.longValue() < timestamp.longValue()) {
				props.put(PROP_CONTEXT_TIMESTAMP, timestamp);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets {@link #PROP_CONTEXT_OCCURRENCE_TIMESTAMP the original occurrence timestamp}.
	 */
	public boolean setActualOccurrenceTime(Long ots) {
		if (ots != null && ots.longValue() > 0 
				&& !props.containsKey(PROP_CONTEXT_OCCURRENCE_TIMESTAMP)
				&& !props.containsKey(PROP_CONTEXT_MEAN_OCCURRENCE_TIME)) {
			Long ts = getTimestamp();
			if (ts == null  ||  ots.longValue() < ts.longValue()) {
				props.put(PROP_CONTEXT_OCCURRENCE_TIMESTAMP, ots);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets {@link #PROP_CONTEXT_MEAN_OCCURRENCE_TIME} with the provided value.
	 */
	public boolean setMeanOccurrenceTime(Long eot) {
		if (eot != null && eot.longValue() > 0 
				&& !props.containsKey(PROP_CONTEXT_OCCURRENCE_TIMESTAMP)
				&& !props.containsKey(PROP_CONTEXT_MEAN_OCCURRENCE_TIME)) {
			props.put(PROP_CONTEXT_MEAN_OCCURRENCE_TIME, eot);
			return true;
		}
		return false;
	}

	/**
	 * Set the involved user
	 *
	 * @param user
	 */
	public boolean setInvolvedUser(Resource user) {
		if (user != null && !props.containsKey(PROP_INVOLVED_HUMAN_USER)) {
			props.put(PROP_INVOLVED_HUMAN_USER, user);
			return true;
		}
		return false;
	}

	/**
	 * Get the involved user
	 *
	 * @return user
	 */
	public Resource getInvolvedUser() {
		return (Resource) props.get(PROP_INVOLVED_HUMAN_USER);
	}

	public boolean setProperty(String propURI, Object value) {
		if (propURI == null)
			return false;

		if (propURI.equals(PROP_RDF_OBJECT)) {
			return setRDFObject(value);
			/*
			 * deprecated } else if (value instanceof Rating) { if
			 * (propURI.equals(PROP_CONTEXT_ACCURACY)) setAccuracy((Rating)
			 * value);
			 */
		} else if (value instanceof ContextProvider) {
			if (propURI.equals(PROP_CONTEXT_PROVIDER))
				return setProvider((ContextProvider) value);
		} else if (value instanceof Resource) {
			if (propURI.equals(PROP_RDF_SUBJECT))
				return setRDFSubject((Resource) value);
			else if (propURI.equals(PROP_RDF_PREDICATE))
				return setRDFPredicate(((Resource) value).getURI());
			else if (propURI.equals(PROP_INVOLVED_HUMAN_USER))
				return setInvolvedUser((Resource) value);
		} else if (value instanceof String) {
			if (propURI.equals(PROP_RDF_PREDICATE))
				return setRDFPredicate((String) value);
		} else if (value instanceof Long) {
			if (propURI.equals(PROP_CONTEXT_TIMESTAMP))
				return setTimestamp((Long) value);
			else if (propURI.equals(PROP_CONTEXT_OCCURRENCE_TIMESTAMP))
				return setActualOccurrenceTime((Long) value);
			else if (propURI.equals(PROP_CONTEXT_MEAN_OCCURRENCE_TIME))
				return setMeanOccurrenceTime((Long) value);
			else if (propURI.equals(PROP_CONTEXT_EXPIRATION_TIME))
				return setExpirationTime((Long) value);
		} else if (value == Boolean.TRUE
				&&  PROP_CONTEXT_REFLECTS_CHANGE_REQUEST.equals(propURI)
				&&  !props.containsKey(propURI)) {
			props.put(PROP_CONTEXT_REFLECTS_CHANGE_REQUEST, Boolean.TRUE);
			return true;
		} else if (value instanceof Integer) {
			if (propURI.equals(PROP_CONTEXT_CONFIDENCE))
				return setConfidence((Integer) value);
		}
		return false;
	}

	/**
	 * @see Matchable#matches(Matchable)
	 *
	 *      Currently, this method always returns false.
	 */
	public boolean matches(Matchable subset) {
		return false;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("\n>>>>>>>>>>>>>>>>> ");
		ResourceUtil.addResource2SB(getRDFSubject(), sb);
		sb.append("->");
		ResourceUtil.addURI2SB(getRDFPredicate(), sb);
		sb.append(" = ");
		ResourceUtil.addObject2SB(getRDFObject(), sb);
		sb.append("\n");
		return sb.toString();
	}
}
