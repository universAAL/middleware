package org.universAAL.middleware.api.impl;

import org.universAAL.middleware.api.annotation.Cardinality;
import org.universAAL.middleware.api.exception.SimplifiedRegistrationException;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.owl.Service;

/**
 * This is a simple helper class used for generating the profiles in the persona
 * way. As all methods for creating outputs,inputs, and change effects in
 * Service class are protected it was necessary to extend it.
 * 
 * @author dzmuda
 */
public class SimplifiedApiService extends Service {

    /**
     * This field is used only for purpose of createService factory method.
     */
    public static String temporalUri = null;

    /**
     * Factory method for creating instances of SimplifiedApiService. It is
     * needed due to early initialization (getClassURI) of upper classes.
     * 
     * @param namespace
     * @param methodUri
     * @param ontologyUri
     * @return
     */
    public synchronized static SimplifiedApiService createService(
	    String namespace, String methodUri, String ontologyUri) {
	temporalUri = ontologyUri;
	return new SimplifiedApiService(namespace, methodUri, ontologyUri);
    }

    public String MY_URI;

    private SimplifiedApiService(String namespace, String methodName,
	    String ontologyUri) {
	super(AnnotationScanner.createServiceUri(namespace, null, methodName));
	this.MY_URI = ontologyUri;
    }

    public String getClassURI() {
	/* If MY_URI was not initialized yet then use static temporalUri */
	if (MY_URI == null) {
	    return temporalUri;
	} else {
	    return MY_URI;
	}
    }

    /**
     * Wrapper method for creating the simpliest input.
     * 
     * @param baseURI
     *            - input parameter URI
     * @param clazz
     *            - input parameter class. It is scanned by reflection for
     *            MY_URI field.
     * @param multiple
     *            - parameter which determine if input should be created with
     *            minimum/maximum cardinality 0 or 1. If true they are 0 if
     *            false 1.
     * @param propertyPaths
     *            - string array of property paths for this output parameter
     * @throws SimplifiedRegistrationException
     *             - thrown when provided clazz parameter does not have field
     *             MY_URI or it is not accessible.
     */
    public void createInputWrapper(String baseURI, Class<?> clazz,
	    Cardinality card, String[] propertyPaths)
	    throws SimplifiedRegistrationException {
	int minCard, maxCard;
	switch (card) {
	case ONE_TO_ONE:
	    minCard = maxCard = 1;
	    break;
	case MANY_TO_MANY:
	    minCard = maxCard = 0;
	    break;
	default:
	    throw new IllegalArgumentException();
	}
	// this will return uri if this is one of the predefined types
	// otherwise check for MY_URI field
	String myUri = TypeMapper.getDatatypeURI(clazz);
	if (myUri == null || myUri.endsWith("anyURI")) {
	    try {
		Object value = clazz.getField("MY_URI").get(null);
		myUri = (String) value;
	    } catch (Exception e) {
		e.printStackTrace();
		throw new SimplifiedRegistrationException(
			"Exception during resolving MY_URI field:"
				+ e.getMessage());
	    }
	}
	if (propertyPaths.length == 0) {
	    // fix for bug in addFilteringInput when empty array is passed as
	    // parameter
	    createInput(baseURI, myUri, minCard, maxCard);
	} else {
	    addFilteringInput(baseURI, myUri, minCard, maxCard, propertyPaths);
	}

    }

    /**
     * Wrapper method for creating the simpliest output.
     * 
     * @param baseURI
     *            - output URI
     * @param clazz
     *            - output class. It is scanned by reflection for MY_URI field.
     * @param multiple
     *            - parameter which determine if input should be created with
     *            minimum/maximum cardinality 0 or 1. If true they are 0 if
     *            false 1.
     * @param propertyPaths
     *            - string array of property paths for this output parameter
     * @throws SimplifiedRegistrationException
     *             - thrown when provided clazz parameter does not have field
     *             MY_URI or it is not accessible.
     */
    public void addOutputWrapper(String baseURI, Class<?> clazz, Cardinality card,
	    String[] propertyPaths) throws SimplifiedRegistrationException {
	int minCard, maxCard;
	switch (card) {
	case ONE_TO_ONE:
	    minCard = maxCard = 1;
	    break;
	case MANY_TO_MANY:
	    minCard = maxCard = 0;
	    break;
	default:
	    throw new IllegalArgumentException();
	}
	// this will return uri if this is one of the predefined types
	// otherwise check for MY_URI field
	String myUri = TypeMapper.getDatatypeURI(clazz);
	if (myUri == null || myUri.endsWith("anyURI")) {
	    try {
		Object value = clazz.getField("MY_URI").get(null);
		myUri = (String) value;
	    } catch (Exception e) {
		e.printStackTrace();
		throw new SimplifiedRegistrationException(
			"Exception during resolving MY_URI field:"
				+ e.getMessage());
	    }
	}
	addOutput(baseURI, myUri, minCard, maxCard, propertyPaths);
    }

    /**
     * Wrapper method for ServiceProfile addChangeEffect
     * 
     * @param propPaths
     * @param value
     * @throws SimplifiedRegistrationException
     */
    public void addChangeEffectWrapper(String[] propPaths, String val,
	    Class<?> type) throws SimplifiedRegistrationException {
	Object value = null;
	// try{
	value = TypeMapper
		.getJavaInstance(val, TypeMapper.getDatatypeURI(type));
	// }catch(Exception e){
	// e.printStackTrace();
	// throw new
	// SimplifiedRegistrationException("Exception during addChangeEffectWrapper casting :"
	// + val + " as " + type.getCanonicalName() + ". " + e.getMessage());
	// }
	myProfile.addChangeEffect(propPaths, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.owl.ManagedIndividual#getPropSerializationType
     * (java.lang.String)
     */
    public int getPropSerializationType(String propURI) {
	return -1;
    }

    public boolean isWellFormed() {
	return true;
    }

}
