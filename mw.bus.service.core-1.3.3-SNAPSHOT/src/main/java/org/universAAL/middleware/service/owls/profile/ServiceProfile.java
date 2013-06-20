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
package org.universAAL.middleware.service.owls.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.bus.model.matchable.Matchable;
import org.universAAL.middleware.bus.model.matchable.Request;
import org.universAAL.middleware.bus.model.matchable.Requirement;
import org.universAAL.middleware.bus.model.matchable.UtilityAdvertisement;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.owl.OntClassInfo;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.rdf.FinalizedResource;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.process.ProcessInput;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.process.ProcessResult;

/**
 * A "registration parameter" as accepted by the service bus of the uAAL
 * middleware. It implements the concept of profile:Profile
 * (xmlns:profile="http://www.daml.org/services/owl-s/1.1/Profile.owl#"),
 * currently ignoring the following OWL-S properties: hasPrecondition,
 * contactInformation, serviceClassification, serviceProduct, and
 * serviceCategory.
 * <p>
 * {@link org.universAAL.middleware.service.ServiceCallee}s register to the
 * service bus by providing a set of instances of this class as registration
 * parameters, one for each "exported" operation that can be called by arbitrary
 * {@link org.universAAL.middleware.service.ServiceCaller}s. A such operation
 * has
 * <ul>
 * <li>an internal ID that has to be provided as a URI string; it will be stored
 * as the value for profile:has_process.</li>
 * </ul>
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class ServiceProfile extends FinalizedResource implements
	UtilityAdvertisement {
    public static final String OWLS_PROFILE_NAMESPACE = Service.OWLS_NAMESPACE_PREFIX
	    + "Profile.owl#";
    public static final String MY_URI = OWLS_PROFILE_NAMESPACE + "Profile";

    // list of OWL-S profile properties supported in uAAL
    /**
     * The OWL-S property profile:serviceName
     */
    public static final String PROP_OWLS_PROFILE_SERVICE_NAME = OWLS_PROFILE_NAMESPACE
	    + "serviceName";
    /**
     * The OWL-S property profile:textDescription
     */
    public static final String PROP_OWLS_PROFILE_TEXT_DESCRIPTION = OWLS_PROFILE_NAMESPACE
	    + "textDescription";
    /**
     * The OWL-S property profile:has_process
     */
    public static final String PROP_OWLS_PROFILE_HAS_PROCESS = OWLS_PROFILE_NAMESPACE
	    + "has_process";
    /**
     * The OWL-S property profile:hasInput
     */
    public static final String PROP_OWLS_PROFILE_HAS_INPUT = OWLS_PROFILE_NAMESPACE
	    + "hasInput";
    /**
     * The OWL-S property profile:hasOutput
     */
    public static final String PROP_OWLS_PROFILE_HAS_OUTPUT = OWLS_PROFILE_NAMESPACE
	    + "hasOutput";
    /**
     * The OWL-S property profile:hasResult
     */
    public static final String PROP_OWLS_PROFILE_HAS_RESULT = OWLS_PROFILE_NAMESPACE
	    + "hasResult";

    // list of OWL-S profile properties that shouldn't be used directly;
    // instead, sub-properties of them should be used
    /**
     * The OWL-S property profile:serviceParameter
     */
    public static final String PROP_OWLS_PROFILE_SERVICE_PARAMETER = OWLS_PROFILE_NAMESPACE
	    + "serviceParameter";
    /**
     * The OWL-S property profile:hasParameter
     */
    public static final String PROP_OWLS_PROFILE_HAS_PARAMETER = OWLS_PROFILE_NAMESPACE
	    + "hasParameter";

    // initial list of sub-properties of profile:serviceParameter in uAAL
    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * provided by the uAAL middleware for all registered services. The range of
     * this property is
     * {@link org.universAAL.middleware.service.owls.profile.QoSRating}.
     */
    public static final String PROP_uAAL_AVERAGE_QOS_RATING = uAAL_VOCABULARY_NAMESPACE
	    + "averageQoSRating";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * provided by the uAAL middleware for all registered services. The range of
     * this property is
     * {@link org.universAAL.middleware.service.owls.profile.ResponseTimeInMilliseconds}
     * .
     */
    public static final String PROP_uAAL_AVERAGE_RESPONSE_TIME = uAAL_VOCABULARY_NAMESPACE
	    + "averageResponseTime";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * to be provided by a registered service to indicate where the physical
     * node hosting the service resides. The range of this property is
     * {@link org.universAAL.middleware.service.owls.profile.SingleLocationParameter}
     * .
     */
    public static final String PROP_uAAL_HOST_LOCATION = uAAL_VOCABULARY_NAMESPACE
	    + "hostLocation";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * provided by the uAAL middleware for all registered services. The range of
     * this property is
     * {@link org.universAAL.middleware.service.owls.profile.QoSRating}.
     */
    public static final String PROP_uAAL_MAX_QOS_RATING = uAAL_VOCABULARY_NAMESPACE
	    + "maxQoSRating";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * provided by the uAAL middleware for all registered services. The range of
     * this property is
     * {@link org.universAAL.middleware.service.owls.profile.ResponseTimeInMilliseconds}
     * .
     */
    public static final String PROP_uAAL_MAX_RESPONSE_TIME = uAAL_VOCABULARY_NAMESPACE
	    + "maxResponseTime";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * provided by the uAAL middleware for all registered services. The range of
     * this property is
     * {@link org.universAAL.middleware.service.owls.profile.QoSRating}.
     */
    public static final String PROP_uAAL_MIN_QOS_RATING = uAAL_VOCABULARY_NAMESPACE
	    + "minQoSRating";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * provided by the uAAL middleware for all registered services. The range of
     * this property is
     * {@link org.universAAL.middleware.service.owls.profile.ResponseTimeInMilliseconds}
     * .
     */
    public static final String PROP_uAAL_MIN_RESPONSE_TIME = uAAL_VOCABULARY_NAMESPACE
	    + "minResponseTime";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * provided by the uAAL middleware for all registered services. The range of
     * this property is
     * {@link org.universAAL.middleware.service.owls.profile.NumberOfSamples}.
     */
    public static final String PROP_uAAL_NUMBER_OF_QOS_RATINGS = uAAL_VOCABULARY_NAMESPACE
	    + "numberOfQoSRatings";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * provided by the uAAL middleware for all registered services. The range of
     * this property is
     * {@link org.universAAL.middleware.service.owls.profile.NumberOfSamples}.
     */
    public static final String PROP_uAAL_NUMBER_OF_RESPONSE_TIME_MEASUREMENTS = uAAL_VOCABULARY_NAMESPACE
	    + "numberOfResponseTimeMeasurements";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * to be provided by a registered service to indicate after how many
     * milliseconds of waiting for a response from the service, the middleware
     * must send a timeout failure message to the caller. The range of this
     * property is
     * {@link org.universAAL.middleware.service.owls.profile.ResponseTimeInMilliseconds}
     * .
     */
    public static final String PROP_uAAL_RESPONSE_TIMEOUT = uAAL_VOCABULARY_NAMESPACE
	    + "responseTimeout";

    /**
     * A sub-property of profile:serviceParameter as a non-functional parameter
     * to be provided by a registered service to indicate in which physical area
     * it makes sense to utilize the service. The range of this property is
     * {@link org.universAAL.middleware.service.owls.profile.MultiLocationParameter}
     * ; the union of the given locations will be interpreted as the area
     * covered by the service.
     */
    public static final String PROP_uAAL_SPATIAL_COVERAGE = uAAL_VOCABULARY_NAMESPACE
	    + "spatialCoverage";

    // the repository for managing sub-properties of profile:serviceParameter
    private static Hashtable nonFunctionalParams = new Hashtable();
    static {
	nonFunctionalParams.put(PROP_uAAL_AVERAGE_QOS_RATING, QoSRating.class);
	nonFunctionalParams.put(PROP_uAAL_AVERAGE_RESPONSE_TIME,
		ResponseTimeInMilliseconds.class);
	nonFunctionalParams.put(PROP_uAAL_HOST_LOCATION,
		SingleLocationParameter.class);
	nonFunctionalParams.put(PROP_uAAL_MAX_QOS_RATING, QoSRating.class);
	nonFunctionalParams.put(PROP_uAAL_MAX_RESPONSE_TIME,
		ResponseTimeInMilliseconds.class);
	nonFunctionalParams.put(PROP_uAAL_MIN_QOS_RATING, QoSRating.class);
	nonFunctionalParams.put(PROP_uAAL_MIN_RESPONSE_TIME,
		ResponseTimeInMilliseconds.class);
	nonFunctionalParams.put(PROP_uAAL_NUMBER_OF_QOS_RATINGS,
		NumberOfSamples.class);
	nonFunctionalParams.put(PROP_uAAL_NUMBER_OF_RESPONSE_TIME_MEASUREMENTS,
		NumberOfSamples.class);
	nonFunctionalParams.put(PROP_uAAL_RESPONSE_TIMEOUT,
		ResponseTimeInMilliseconds.class);
	nonFunctionalParams.put(PROP_uAAL_SPATIAL_COVERAGE,
		MultiLocationParameter.class);
    }

    /**
     * Sub-properties of profile:serviceParameter can be made known to the uAAL
     * middleware by calling this method.
     * 
     * @param subPropertyOfServiceParameter
     *            The URI of the sub-property
     * @param subclassOfProfileParameter
     *            A subclass of {@link ProfileParameter} from which the values
     *            of the new property stem
     */
    public static void addNonFunctionalParameter(
	    String subPropertyOfServiceParameter,
	    Class subclassOfProfileParameter) {
	if (subPropertyOfServiceParameter != null
		&& subclassOfProfileParameter != null
		&& !nonFunctionalParams
			.containsKey(subPropertyOfServiceParameter)
		&& ProfileParameter.class
			.isAssignableFrom(subclassOfProfileParameter)) {
	    nonFunctionalParams.put(subPropertyOfServiceParameter,
		    subclassOfProfileParameter);
	}
    }

    /**
     * Only for use by deserializers.
     */
    public ServiceProfile() {
	super();
	addType(MY_URI, true);
    }

    public ServiceProfile(String uri) {
	super(uri);
	addType(MY_URI, true);
    }

    /**
     * The constructor to be used by
     * {@link org.universAAL.middleware.service.ServiceCallee}s. Effects,
     * inputs, outputs and output bindings must be added to the profile using
     * methods whose names starts with "add*". The rdfs:label and rdfs:comment
     * provided by the class of the given service will be used as
     * profile:serviceName and profile:textDescription respectively.
     * 
     * @param s
     *            The individual service whose profile is being constructed
     * @param processURI
     *            The URI that is used by the
     *            {@link org.universAAL.middleware.service.ServiceCallee}
     *            registering this profile as an internal ID for identifying the
     *            operation called
     */
    public ServiceProfile(Service s, String processURI) {
	super();
	if (s == null || processURI == null) {
	    throw new NullPointerException("Parameter null!");
	}
	addType(MY_URI, true);
	props.put(Service.PROP_OWLS_PRESENTED_BY, s);
	setProperty(PROP_OWLS_PROFILE_HAS_PROCESS, processURI);
	OntClassInfo oci = OntologyManagement.getInstance().getOntClassInfo(
		s.getClassURI());
	String aux = oci == null ? "" : oci.getResourceLabel();
	if (aux != null) {
	    props.put(PROP_OWLS_PROFILE_SERVICE_NAME, aux);
	}
	aux = oci == null ? "" : oci.getResourceComment();
	if (aux != null) {
	    props.put(PROP_OWLS_PROFILE_TEXT_DESCRIPTION, aux);
	}
    }

    /**
     * Declares that a call to the service described by this profile adds the
     * given <code>value</code> to the property reachable by the given
     * <code>ppath</code>. The property should normally be a multi-valued
     * property.
     */
    public void addAddEffect(String[] ppath, Object value) {
	if (ppath != null && value != null) {
	    theResult()
		    .addAddEffect(new PropertyPath(null, true, ppath), value);
	}
    }

    /**
     * Declares that a call to the service described by this profile changes the
     * value of the property reachable by the given <code>ppath</code> to the
     * given <code>value</code>.
     */
    public void addChangeEffect(String[] ppath, Object value) {
	if (ppath != null && value != null) {
	    theResult().addChangeEffect(new PropertyPath(null, true, ppath),
		    value);
	}
    }

    /**
     * Declares that the output parameter specified by <code>toParam</code> will
     * be an instance of the given <code>targetClass</code> as a result of
     * converting the actual value of a property reachable by the given property
     * path <code>sourceProp</code>.
     */
    public void addClassConversionOutputBinding(ProcessOutput toParam,
	    String[] sourceProp, TypeURI targetClass) {
	if (toParam != null && sourceProp != null && targetClass != null) {
	    theResult().addClassConversionOutputBinding(toParam,
		    new PropertyPath(null, true, sourceProp), targetClass);
	}
    }

    /**
     * Adds the given input parameter to the set of this service's input
     * parameters.
     */
    public void addInput(ProcessInput in) {
	if (in != null) {
	    inputs().add(in);
	}
    }

    /**
     * Declares that the output parameter specified by <code>toParam</code> will
     * be the translation of the actual value of a property reachable by the
     * given property path <code>sourceProp</code> into the given
     * <code>targetLang</code>.
     */
    public void addLangConversionOutputBinding(ProcessOutput toParam,
	    String[] sourceProp, String targetLang) {
	if (toParam != null && sourceProp != null && targetLang != null) {
	    theResult().addLangConversionOutputBinding(toParam,
		    new PropertyPath(null, true, sourceProp), targetLang);
	}
    }

    /**
     * Adds the given output parameter to the set of this service's output
     * parameters.
     */
    public void addOutput(ProcessOutput out) {
	if (out != null) {
	    outputs().add(out);
	}
    }

    /**
     * Declares that a call to the service described by this profile removes the
     * value of the property reachable by the given <code>ppath</code>.
     */
    public void addRemoveEffect(String[] ppath) {
	if (ppath != null) {
	    theResult().addRemoveEffect(new PropertyPath(null, true, ppath));
	}
    }

    /**
     * Declares that the output parameter specified by <code>toParam</code> will
     * reflect the value of a property reachable by the given property path
     * <code>sourceProp</code>.
     */
    public void addSimpleOutputBinding(ProcessOutput toParam,
	    String[] sourceProp) {
	if (toParam != null && sourceProp != null) {
	    theResult().addSimpleOutputBinding(toParam,
		    new PropertyPath(null, true, sourceProp));
	}
    }

    /**
     * Declares that the output parameter specified by <code>toParam</code> will
     * reflect the value of a property reachable by the given property path
     * <code>sourceProp</code> in terms of the given measurement unit
     * <code>targetUnit</code>.
     */
    public void addUnitConversionOutputBinding(ProcessOutput toParam,
	    String[] sourceProp, String targetUnit) {
	if (toParam != null && sourceProp != null && targetUnit != null) {
	    theResult().addUnitConversionOutputBinding(toParam,
		    new PropertyPath(null, true, sourceProp), targetUnit);
	}
    }

    /**
     * Returns the list of service effects; the main user of this method is the
     * service bus.
     */
    public Resource[] getEffects() {
	ProcessResult pr = (ProcessResult) props
		.get(PROP_OWLS_PROFILE_HAS_RESULT);
	List effects = pr == null ? null : pr.getEffects();
	return effects == null ? new Resource[0] : (Resource[]) effects
		.toArray(new Resource[effects.size()]);
    }

    /**
     * Returns the list of service input parameters; the main user of this
     * method is the service bus.
     */
    public Iterator getInputs() {
	List answer = (List) props.get(PROP_OWLS_PROFILE_HAS_INPUT);
	return answer == null ? new ArrayList(0).iterator() : answer.iterator();
    }

    public int getNumberOfInputs() {
	List answer = (List) props.get(PROP_OWLS_PROFILE_HAS_INPUT);
	return answer == null ? 0 : answer.size();
    }

    public int getNumberOfMandatoryInputs() {
	int result = 0;
	Iterator i = getInputs();
	while (i.hasNext()) {
	    if (((ProcessInput) i.next()).getMinCardinality() > 0) {
		result++;
	    }
	}
	return result;
    }

    /**
     * Returns the list of service output parameters; the main user of this
     * method is the service bus.
     */
    public Iterator getOutputs() {
	List answer = (List) props.get(PROP_OWLS_PROFILE_HAS_OUTPUT);
	return answer == null ? new ArrayList(0).iterator() : answer.iterator();
    }

    /**
     * Returns the list of declarations how the service output parameters are
     * bound; the main user of this method is the service bus.
     */
    public Resource[] getOutputBindings() {
	ProcessResult pr = (ProcessResult) props
		.get(PROP_OWLS_PROFILE_HAS_RESULT);
	List bindings = pr == null ? null : pr.getBindings();
	return bindings == null ? new Resource[0] : (Resource[]) bindings
		.toArray(new Resource[bindings.size()]);
    }

    /**
     * Returns the URI that serves as the internal ID of the service on the side
     * of the provider; the main user of this method is the service bus.
     */
    public String getProcessURI() {
	return ((Resource) props.get(PROP_OWLS_PROFILE_HAS_PROCESS)).getURI();
    }

    /**
     * Returns the name of the service; the main user of this method is the
     * service bus.
     */
    public String getServiceName() {
	return (String) props.get(PROP_OWLS_PROFILE_SERVICE_NAME);
    }

    /**
     * Returns the textual description of the service; the main user of this
     * method is the service bus.
     */
    public String getServiceDescription() {
	return (String) props.get(PROP_OWLS_PROFILE_TEXT_DESCRIPTION);
    }

    /**
     * Returns the individual from the service ontology that represents the
     * provided service; the main user of this method is the service bus.
     */
    public Service getTheService() {
	return (Service) props.get(Service.PROP_OWLS_PRESENTED_BY);
    }

    private List inputs() {
	List answer = (List) props.get(PROP_OWLS_PROFILE_HAS_INPUT);
	if (answer == null) {
	    answer = new ArrayList(5);
	    props.put(PROP_OWLS_PROFILE_HAS_INPUT, answer);
	}
	return answer;
    }

    /**
     * Checks if input, output and result is empty.
     * 
     * @return boolean
     */
    public boolean isEmpty() {
	return props.get(PROP_OWLS_PROFILE_HAS_INPUT) == null
		&& props.get(PROP_OWLS_PROFILE_HAS_OUTPUT) == null
		&& props.get(PROP_OWLS_PROFILE_HAS_RESULT) == null;
    }

    private List outputs() {
	List answer = (List) props.get(PROP_OWLS_PROFILE_HAS_OUTPUT);
	if (answer == null) {
	    answer = new ArrayList(5);
	    props.put(PROP_OWLS_PROFILE_HAS_OUTPUT, answer);
	}
	return answer;
    }

    private ProcessResult theResult() {
	ProcessResult pr = (ProcessResult) props
		.get(PROP_OWLS_PROFILE_HAS_RESULT);
	if (pr == null) {
	    pr = new ProcessResult();
	    props.put(PROP_OWLS_PROFILE_HAS_RESULT, pr);
	}
	return pr;
    }

    /**
     * @see org.universAAL.middleware.rdf.Resource#setProperty(java.lang.String,java.lang.Object)
     */
    @Override
    public boolean setProperty(String propURI, Object value) {
	// the only changeable props are the sub-properties of OWL-S
	// profile:serviceParameter
	if (propURI == null || value == null
		|| !(value instanceof ProfileParameter)
		&& props.containsKey(propURI)) {
	    return false;
	}
	if (Service.PROP_OWLS_PRESENTED_BY.equals(propURI)) {
	    if (value instanceof Service) {
		props.put(propURI, value);
		return true;
	    }
	} else if (PROP_OWLS_PROFILE_SERVICE_NAME.equals(propURI)
		|| PROP_OWLS_PROFILE_TEXT_DESCRIPTION.equals(propURI)) {
	    if (value instanceof String) {
		props.put(propURI, value);
		return true;
	    }
	} else if (PROP_OWLS_PROFILE_HAS_PROCESS.equals(propURI)) {
	    if (value instanceof String) {
		value = new Resource((String) value);
	    } else if (!(value instanceof Resource)
		    || ((Resource) value).numberOfProperties() != 0) {
		return false;
	    }
	    if (!((Resource) value).isAnon()
		    && Resource.isQualifiedName(((Resource) value).getURI())) {
		props.put(propURI, value);
		return true;
	    }
	} else if (PROP_OWLS_PROFILE_HAS_INPUT.equals(propURI)) {
	    value = ProcessInput.checkParameterList(value);
	    if (value == null) {
		return false;
	    }
	    props.put(propURI, value);
	    return true;
	} else if (PROP_OWLS_PROFILE_HAS_OUTPUT.equals(propURI)) {
	    value = ProcessOutput.checkParameterList(value);
	    if (value == null) {
		return false;
	    }
	    props.put(propURI, value);
	    return true;
	} else if (PROP_OWLS_PROFILE_HAS_RESULT.equals(propURI)) {
	    if (value instanceof ProcessResult) {
		if (!((ProcessResult) value).isWellFormed()) {
		    return false;
		}
	    } else if (value instanceof Resource) {
		value = ProcessResult.toResult((Resource) value);
		if (value == null) {
		    return false;
		}
	    } else {
		return false;
	    }
	    props.put(propURI, value);
	    return true;
	} else if (value instanceof ProfileParameter
		&& ((ProfileParameter) value).isWellFormed()) {
	    Class subclassOfProfileParameter = (Class) nonFunctionalParams
		    .get(propURI);
	    if (subclassOfProfileParameter != null
		    && subclassOfProfileParameter.isAssignableFrom(value
			    .getClass())) {
		props.put(propURI, value);
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns the serialized profile as String
     */
    public String serializeProfile() {
	return BusMessage.trySerializationAsContent(this);
    }

    /**
     * Returns the de-serialized profile
     */
    public static ServiceProfile deserializeProfile(String turtleSP) {

	Object o = BusMessage.deserializeAsContent(turtleSP);
	return o instanceof ServiceProfile ? (ServiceProfile) o : null;
    }

    /**
     * This method is called if the caller does not have any clue about what
     * type the Matchables have. Therefore here must an implementation of
     * matches(ServiceProfile) be given (as matching against another instance of
     * the same type must always be possible).
     * 
     * @param other
     *            the {@link Matchable} that this instance should be matched
     *            against
     * @return <tt>false</tt> if <tt>other</tt> is not an instance of
     *         {@link ServiceProfile},
     *         {@link #isMatchingServiceProfile(ServiceProfile)} else
     */
    public boolean matches(Matchable other) {
	if (other instanceof ServiceProfile) {
	    return isMatchingServiceProfile((ServiceProfile) other);
	} else {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    // TODO add a good matching algorithm
    private boolean isMatchingServiceProfile(ServiceProfile other) {
	boolean matches = true;
	for (Object current : Collections.list(getPropertyURIs())) {
	    String propertyURI = (String) current;
	    Object thisProperty = getProperty(propertyURI);
	    Object otherProperty = other.getProperty(propertyURI);

	    if (!thisProperty.equals(otherProperty)) {
		matches = false;
	    }
	}
	return matches;
    }

    /**
     * This method will only be called if d is not of type {@link Request}, so
     * the {@link ServiceProfile} will never match it and <tt>false</tt> is
     * returned.
     * 
     * @param d
     *            the requirement to be matched against
     * @return <tt>false</tt> as mentioned above
     */
    public boolean matches(Requirement d) {
	return false;
    }

    /**
     * Switches over the types of a possibly matching {@link Request}. Calls
     * appropriate methods for the different types.
     * 
     * @param r
     *            the request to be matched
     * @return <tt>true</tt>, if the Request matches, <tt>false</tt> if not
     */
    public boolean matches(Request r) {
	if (r instanceof ServiceRequest) {
	    return isMatchingServiceRequest((ServiceRequest) r);
	} else {
	    return false;
	}
    }

    private boolean isMatchingServiceRequest(ServiceRequest r) {
	// TODO method not implemented
	return false;
    }
}
