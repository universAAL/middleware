package org.universAAL.middleware.api.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.api.SimpleServiceRegistrator;
import org.universAAL.middleware.api.annotation.Input;
import org.universAAL.middleware.api.annotation.Output;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.aapi.AapiServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * Basic class for wrapping ServiceCallee. It acts as a proxy for actual
 * implementation which was annotated by UniversAALService annotation. During
 * handleCall it invokes original service by reflection and create proper
 * ServiceResponse object.
 * 
 * @author dzmuda
 * 
 */
public class ServiceCalleeWrapper extends ServiceCallee {

    private ServiceProfile[] profiles;

    private Object wrappedObject;

    private Map<String, Method> annotatedMethods;
    private Map<String, List<String>> annotatedOutputNames;
    private Map<String, List<Output>> annotatedOutputs;
    private Map<String, List<String>> annotatedMethodsParametersNames;
    private Map<String, List<Input>> annotatedMethodsParameters;

    //private String namespace;
    //private String serviceURI;

    public ServiceCalleeWrapper(ModuleContext context,
	    ServiceProfile[] realizedServices, Object wrappedObject,
	    String namespace, String serviceURI,
	    Map<String, Method> annotatedMethods,
	    Map<String, List<String>> annotatedOutputNames,
	    Map<String, List<Output>> annotatedOutputs,
	    Map<String, List<String>> annotatedMethodsParametersNames,
	    Map<String, List<Input>> annotatedMethodsParameters) {
	super(context, realizedServices);

	this.profiles = realizedServices;
	this.wrappedObject = wrappedObject;
	this.annotatedMethods = annotatedMethods;
	this.annotatedOutputNames = annotatedOutputNames;
	this.annotatedOutputs = annotatedOutputs;
	this.annotatedMethodsParametersNames = annotatedMethodsParametersNames;
	this.annotatedMethodsParameters = annotatedMethodsParameters;
	//this.namespace = namespace;
	//this.serviceURI = serviceURI;
    }

    private ServiceCalleeWrapper(ModuleContext context,
	    ServiceProfile[] realizedServices) {
	super(context, realizedServices);
    }

    @Override
    public void communicationChannelBroken() {
    }

    /**
     * Handles the incoming ServiceCall and check if it fits to methods declared
     * by wrapped object. If so, it invoke such method with reflection and
     * create proper ServiceResponse
     * 
     * It is crucial to remember that Output in ServiceResponse has to be either
     * List or RDF object because TypeMapper.asLiteral does not support other
     * types. If output is an Array it is automatically converted into a List.
     * 
     * @see org.universAAL.middleware.service.ServiceCallee#handleCall(org.universAAL.middleware.service.ServiceCall)
     */
    public ServiceResponse handleCall(ServiceCall call) {
	if (call == null)
	    return null;

	String operation = call.getProcessURI();
	if (operation == null)
	    return null;

	Method m = null;

	AapiServiceResponse sr = new AapiServiceResponse(CallStatus.succeeded);
	try {
	    for (String s : annotatedMethods.keySet()) {
		if (operation.startsWith(s)) {
		    m = annotatedMethods.get(s);
		    List<String> methodParametersNames = annotatedMethodsParametersNames
			    .get(s);
		    Class<?>[] methodParametersTypes = m.getParameterTypes();
		    Object[] inputs = new Object[methodParametersNames.size()];
		    List<String> conversionErrors = new ArrayList<String>();
		    for (int i = 0; i < methodParametersNames.size(); i++) {
			Input inputAnnotation = annotatedMethodsParameters.get(
				s).get(i);
			Object input = null;
			input = call
				.getInputValue(methodParametersNames.get(i));

			if (input == null) {
			    conversionErrors.add(methodParametersNames.get(i)
				    + " not found in incoming ServiceRequest;");
			} else {
			    try {
				if (inputAnnotation.propertyPaths().length > 0) {
				    if (input instanceof Resource) {
					inputs[i] = input;
				    } else {
					inputs[i] = TypeMapper
						.getJavaInstance(
							input.toString(),
							TypeMapper
								.getDatatypeURI(methodParametersTypes[i]));
				    }
				} else {
				    inputs[i] = input;
				}

			    } catch (ClassCastException cce) {
				cce.printStackTrace();
				conversionErrors.add(methodParametersNames
					.get(i)
					+ " cannot be casted as "
					+ methodParametersTypes[i].getName()
					+ ";");
			    }
			}
		    }

		    if (conversionErrors.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < conversionErrors.size(); i++) {
			    sb.append(conversionErrors.get(i) + "\n");
			}
			return prepareErrorResponse("Error during ServiceRequest parameters lookup: \n"
				+ sb.toString());
		    }

		    Object retObj = m.invoke(wrappedObject, inputs);
		    // if method is void then skip adding output
		    if (!void.class.equals(m.getReturnType())) {
			List<String> outputs = annotatedOutputNames.get(s);

			if (outputs.size() > 1) {
			    // that means that the return object should be an
			    // object array and
			    // each of its elements are single output sequenced
			    if (retObj instanceof Object[]) {
				Object[] tempObjArray = (Object[]) retObj;
				if (tempObjArray.length != outputs.size()) {
				    return prepareErrorResponse("Different @Output annotations numbers than returned array lenght:\n"
					    + "@Output size: "
					    + outputs.size()
					    + " , returned array lenght : "
					    + tempObjArray.length);
				}
				for (int i = 0; i < outputs.size(); i++) {
				    sr
					    .addOutput(new ProcessOutput(
						    outputs.get(i),
						    convertArrayToList(tempObjArray[i])));
				}

			    } else {
				return prepareErrorResponse("Error during ServiceRequest outputs and results processing:\n"
					+ "If multiple @Output are provided then method should return Object[]");
			    }
			} else {
			    // we have only single output
			    // fixing issue with converting arrays into RDF
			    // (TypeMapper.asLiteral does not support it)
			    retObj = convertArrayToList(retObj);
			    String outVal = outputs.get(0);
			    sr.addOutput(new ProcessOutput(outVal, retObj));
			}

		    }
		    boolean allowUnbound = false;
		    for (Output output : annotatedOutputs.get(s)) {
			if (output.propertyPaths().length == 0) {
			    allowUnbound = true;
			    break;
			}
		    }
		    if (allowUnbound) {
			sr.allowUnboundOutput();
		    }
		}
	    }
	    return sr;
	} catch (Exception e) {
	    e.printStackTrace();
	    return prepareErrorResponse("Exception during wrapped handleCall:"
		    + e.getMessage());
	}
    }

    private ServiceResponse prepareErrorResponse(String message) {
	ServiceResponse invalidInput = new ServiceResponse(
		CallStatus.serviceSpecificFailure);
	invalidInput.addOutput(new ProcessOutput(
		ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, message));
	return invalidInput;
    }

    private Object convertArrayToList(Object retObj) {
	if (retObj instanceof Object[]) {
	    Object[] tempArray = (Object[]) retObj;
	    List<Object> list = new ArrayList<Object>();
	    for (Object o : tempArray) {
		list.add(o);
	    }
	    return list;
	}
	return retObj;
    }

    public void setProfiles(ServiceProfile[] profiles) {
	this.profiles = profiles;
    }

    public ServiceProfile[] getProfiles() {
	return profiles;
    }

    public static void main(String args[]) {
	SimpleServiceRegistrator ssr = new SimpleServiceRegistrator(null);
	for (Method m : ssr.getClass().getMethods()) {
	    System.out.println(m.getName() + "\t" + m.getReturnType());
	}
    }
}
