package org.universAAL.middleware.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.universAAL.middleware.api.annotation.Cardinality;
import org.universAAL.middleware.api.annotation.ChangeEffect;
import org.universAAL.middleware.api.annotation.ChangeEffects;
import org.universAAL.middleware.api.annotation.Input;
import org.universAAL.middleware.api.annotation.Outputs;
import org.universAAL.middleware.api.annotation.OntologyClasses;
import org.universAAL.middleware.api.annotation.Output;
import org.universAAL.middleware.api.annotation.ServiceOperation;
import org.universAAL.middleware.api.annotation.UniversAALService;
import org.universAAL.middleware.api.exception.SimplifiedRegistrationException;
import org.universAAL.middleware.api.impl.AnnotationScanner;
import org.universAAL.middleware.api.impl.ServiceCalleeWrapper;
import org.universAAL.middleware.api.impl.SimplifiedApiService;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * Main class of Simpified API approach.
 * 
 * It is responsible for scanning provided object interfaces for occurences of
 * SimplifiedAPI annotations, generating ServiceProfiles, and creating
 * ServiceCallee wrappers.
 * 
 * In all methods if not all information was provided in @Input or @Output
 * annotations it generates default values:
 * 
 * @Input - default parameter name is "argX" where X is the number of parameter
 *        in method definition starting from 1.
 * @Output - default parameter name is "outputX" where X is the number of @Output
 *         annotation on method declaration starting from 1. Currently only
 *         single @Output can be provided and if its name is skipped a default
 *         value of "output1" is generated.
 * 
 * @author dzmuda
 * 
 */
public class SimpleServiceRegistrator {
    public ModuleContext mc;

    private List<ServiceCalleeWrapper> wrappers = new ArrayList<ServiceCalleeWrapper>();

    public SimpleServiceRegistrator(ModuleContext mc) {
	this.mc = mc;
    }

    /**
     * Method extracts MY_URI from given resource class.
     * 
     * @param resourceClass
     * @return
     * @throws SimplifiedRegistrationException
     */
    private String exctractUriFromResourceClass(OntologyClasses resourceClass)
	    throws SimplifiedRegistrationException {
	Class<?>[] classes = resourceClass.value();
	String myUri = "";
	for (Class<?> clazz : classes) {
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
	return myUri;
    }

    /**
     * Main method of Simplified API approach.
     * 
     * It scans provided object for occurrences of : 1. @UniversAALService and @ResourceClass
     * annotation on one of its interfaces; 2. Methods of founded interface for
     * occurrences of @ServiceOperation, @Output, and @ChangeEffect annotation;
     * 3. Parameters of methods for occurrences of @Input annotations.
     * 
     * For each founded method it generates a proper ServiceProfile and create a
     * ServiceCalleeWrapper instance.
     * 
     * If something goes wrong during processing a
     * SimplifiedRegistrationException, especially when: 1. No @UniversAALService
     * and @ResourceClass is found on object interfaces; 2. No @ServiceOperation
     * was provided for @UniversAALService annotated interface; 3. Duplicate
     * names of service was provided (@ServiceOperation); 4. No @Output
     * annotation was provided for method annotated with @ServiceOperation; 5.
     * Duplicate names was provided in @Input annotations of service method;
     * 
     * @param o
     *            - service implementation, which implements annotated
     *            UniversAAL interface
     * @throws SimplifiedRegistrationException
     */
    public void registerService(Object o)
	    throws SimplifiedRegistrationException {
	Class<?> annotatedInterface = null;
	OntologyClasses resourceClasses = null;
	Map<String, Method> annotatedMethods = new HashMap<String, Method>();
	Map<String, List<String>> annotatedOutputNames = new HashMap<String, List<String>>();
	Map<String, List<Output>> annotatedOutputs = new HashMap<String, List<Output>>();
	Map<String, List<String>> annotatedMethodsParametersNames = new HashMap<String, List<String>>();
	Map<String, List<Input>> annotatedMethodsParameters = new HashMap<String, List<Input>>();
	List<ServiceProfile> profiles = new ArrayList<ServiceProfile>();
	String namespace = null;
	String name = null;
	List<Class<?>> classes = new ArrayList<Class<?>>();
	classes.add(o.getClass());
	classes.addAll(Arrays.asList(o.getClass().getInterfaces()));
	for (Class<?> clazz : classes) {
	    annotatedInterface = null;
	    resourceClasses = null;
	    for (Annotation a : clazz.getAnnotations()) {
		if (a.annotationType().equals(UniversAALService.class)) {
		    annotatedInterface = clazz;
		    namespace = ((UniversAALService) a).namespace();
		    name = ((UniversAALService) a).name();
		    if ("".equals(name)) {
			name = clazz.getSimpleName();
		    }
		} else if (a.annotationType().equals(OntologyClasses.class)) {
		    resourceClasses = (OntologyClasses) a;
		}
	    }
	    if (annotatedInterface != null && resourceClasses != null) {
		break;
	    }
	}

	if (annotatedInterface == null) {
	    throw new SimplifiedRegistrationException(
		    "Cannot found @UniversAALService annotation in interfaces of provided service instance.");
	}

	if (resourceClasses == null) {
	    throw new SimplifiedRegistrationException(
		    "Cannot found @ResourceClass annotation in interfaces of provided service instance.");
	}
	// String myUri = namespace + name;
	String ontologyUri = exctractUriFromResourceClass(resourceClasses);
	registerOntology(namespace, name, ontologyUri);

	for (Method m : annotatedInterface.getMethods()) {
	    Cardinality defaultOutputCardinality = Cardinality.ONE_TO_ONE;
	    String serviceName = null;
	    List<Output> methodOutputAnnotations = new ArrayList<Output>();
	    List<String> outputs = new ArrayList<String>();
	    List<ChangeEffect> methodChangeEffects = new ArrayList<ChangeEffect>();
	    for (Annotation a : m.getAnnotations()) {
		if (a.annotationType().equals(ServiceOperation.class)) {
		    serviceName = ((ServiceOperation) a).value();
		    if ("".equals(serviceName)) {
			serviceName = m.getName();
		    }
		    if (annotatedMethods.containsKey(AnnotationScanner
			    .createServiceUri(namespace, name, serviceName))) {
			throw new SimplifiedRegistrationException(
				"Duplicate name definition for @ServiceOperation: "
					+ serviceName);
		    }
		    annotatedMethods.put(AnnotationScanner.createServiceUri(
			    namespace, name, serviceName), m);
		} else if (a.annotationType().equals(Output.class)
			|| a.annotationType().equals(Outputs.class)) {
		    Output[] temp = null;
		    if (a.annotationType().equals(Output.class)) {
			Class<?> returnType = m.getReturnType();
			if (returnType.isArray()) {
			    defaultOutputCardinality = Cardinality.MANY_TO_MANY;
			}
			if (returnType.isInstance(new ArrayList<Object>())) {
			    defaultOutputCardinality = Cardinality.MANY_TO_MANY;
			}
			temp = new Output[] { (Output) a };
		    } else {
			temp = ((Outputs) a).value();
		    }
		    for (int i = 0; i < temp.length; i++) {
			String assignedName = ((Output) temp[i]).name();
			String outputName = null;
			if ("".equals(assignedName)) {
			    outputName = namespace + "output"
				    + (outputs.size() + 1);
			} else {
			    outputName = namespace + assignedName;
			}
			outputs.add(outputName);
			methodOutputAnnotations.add((Output) temp[i]);
		    }
		} else if (a.annotationType().equals(ChangeEffect.class)) {
		    methodChangeEffects.add((ChangeEffect) a);
		} else if (a.annotationType().equals(ChangeEffects.class)) {
		    methodChangeEffects.addAll(Arrays
			    .asList(((ChangeEffects) a).value()));
		}
	    }

	    if (outputs.size() == 0) {
		Class<?> returnType = m.getReturnType();
		if (returnType != void.class) {
		    String methodName = m.getName();
		    if (methodName.startsWith("get") && methodName.length() > 3) {
			methodName = String.format("%s%s", Character
				.toLowerCase(methodName.charAt(3)), methodName
				.substring(4));
		    }
		    final String finalOutputName = namespace + methodName;
		    outputs.add(finalOutputName);
		    Output inferredOutput = new Output() {
			public Class<? extends Annotation> annotationType() {
			    return Output.class;
			}

			public String[] propertyPaths() {
			    return new String[0];
			}

			public String name() {
			    return finalOutputName;
			}

			public Class<?> filteringClass() {
			    return void.class;
			}

			public Cardinality cardinality() {
			    return Cardinality.NOT_SPECIFIED;
			}
		    };
		    methodOutputAnnotations.add(inferredOutput);
		}
	    }

	    if (serviceName != null) {
		annotatedOutputNames.put(AnnotationScanner.createServiceUri(
			namespace, name, serviceName), outputs);
		annotatedOutputs.put(AnnotationScanner.createServiceUri(
			namespace, name, serviceName), methodOutputAnnotations);
		List<String> methodParameterNames = new ArrayList<String>();

		Annotation[][] parameterAnnotations = m
			.getParameterAnnotations();
		if (parameterAnnotations.length != m.getParameterTypes().length) {
		    // Should never happen
		    throw new SimplifiedRegistrationException(
			    "Invalid number of @Input annotations for @ServiceOperation. Expected:  "
				    + m.getParameterTypes().length + " got "
				    + parameterAnnotations.length);
		}
		List<Input> methodParameterAnnotations = new ArrayList<Input>();
		Class<?>[] methodParameterTypes = m.getParameterTypes();
		for (int i = 0; i < parameterAnnotations.length; i++) {
		    String parameterName = "arg" + (i + 1);
		    for (int j = 0; j < parameterAnnotations[i].length; j++) {
			if (parameterAnnotations[i][j].annotationType().equals(
				Input.class)) {
			    Input input = (Input) parameterAnnotations[i][j];
			    methodParameterAnnotations.add(input);
			    String temp = input.name();
			    if (!"".equals(temp)) {
				parameterName = temp;
			    }
			    parameterName = namespace + parameterName;
			}
		    }
		    if (methodParameterNames.contains(parameterName)) {
			throw new SimplifiedRegistrationException(
				"Duplicate parameter name definition: '"
					+ parameterName
					+ "' for @ServiceOperation: "
					+ serviceName);
		    }
		    methodParameterNames.add(parameterName);
		}
		annotatedMethodsParametersNames.put(AnnotationScanner
			.createServiceUri(namespace, name, serviceName),
			methodParameterNames);
		annotatedMethodsParameters.put(AnnotationScanner
			.createServiceUri(namespace, name, serviceName),
			methodParameterAnnotations);
		profiles.add(createServiceProfileForMethod(namespace, name,
			serviceName, methodParameterAnnotations,
			methodParameterTypes, methodOutputAnnotations, m
				.getReturnType(), methodChangeEffects,
			ontologyUri, defaultOutputCardinality));
	    }
	}

	if (annotatedMethods.size() == 0) {
	    throw new SimplifiedRegistrationException(
		    "Cannot found @ServiceOperation annotation in provided interface methods.");
	}

	ServiceCalleeWrapper wrapper = new ServiceCalleeWrapper(mc, profiles
		.toArray(new ServiceProfile[0]), o, namespace, name,
		annotatedMethods, annotatedOutputNames, annotatedOutputs,
		annotatedMethodsParametersNames, annotatedMethodsParameters);
	wrappers.add(wrapper);
    }

    /**
     * Method is used for registering onthologies on the basis of provided
     * annotation.
     * 
     * Under the hood it scans class provided in @ResourceClass annotation for
     * occurences of "MY_URI" field and register it:
     * 
     * OntologyManagement.getInstance().register(new
     * SimpleOntology(namespace+serviceURI, myUri));
     * 
     * @param namespace
     *            - URI of whole UniversAAL service provider (@UniversAALService
     *            annotation - namespace)
     * @param name
     *            - name of service provider (@UniversAALService annotation -
     *            name)
     * @param resourceClass
     *            - @ResourceClass annotation that holds reference to class
     *            which should be scanned for "MY_URI" field and registered in
     *            OntologyManagement
     * @throws SimplifiedRegistrationException
     *             - thrown when provided clazz parameter does not have field
     *             MY_URI or it is not accessible.
     */
    private void registerOntology(String namespace, String name,
	    String ontologyUri) throws SimplifiedRegistrationException {
	OntologyManagement.getInstance().register(
		new SimpleOntology(namespace + name, ontologyUri));
    }

    /**
     * On the basis of information provided in annotation this method is
     * generating ServiceProfile. It is invoked for each method which is
     * annotated @ServiceOperation. It creates a SimplifiedApiService (extends
     * Service) and created Inputs, Outputs, and ChangeEffects. - for each @Input
     * parameter it calls createInput - for each @Output (currently supported
     * only single) parameter it calls addOutput - for each @ChangeEffect -
     * parameter it calls addChangeEffect
     * 
     * @param namespace
     *            - URI of whole UniversAAL service provider (@UniversAALService
     *            annotation)
     * @param name
     *            - name of service provider (@UniversAALService annotation -
     *            name)
     * @param serviceName
     *            - name of service method (@ServiceOperation annotation - name)
     * @param methodParametersAnnotations
     *            - parameter annotations (contains @Input annotations) of
     *            processed service method
     * @param methodOutputAnnotations
     *            - @Ouput annotation of processed service method
     * @return generated ServiceProfile
     * @throws SimplifiedRegistrationException
     */
    private ServiceProfile createServiceProfileForMethod(String namespace,
	    String name, String serviceName,
	    List<Input> methodParametersAnnotations,
	    Class<?>[] methodParameterTypes,
	    List<Output> methodOutputAnnotations, Class<?> methodOutputType,
	    List<ChangeEffect> methodChangeEffects, String ontologyUri,
	    Cardinality defaultOutputCardinality)
	    throws SimplifiedRegistrationException {
	SimplifiedApiService srv = SimplifiedApiService.createService(
		namespace, serviceName, ontologyUri);

	Class<?> filteringClass = null;
	Cardinality defaultInputCardinality = Cardinality.ONE_TO_ONE;

	String parameterURI = "";
	for (int i = 0; i < methodParametersAnnotations.size(); i++) {
	    Input input = methodParametersAnnotations.get(i);
	    if (input.propertyPaths().length > 0) {
		parameterURI = "arg" + (i + 1);
		if (!"".equals(input.name())) {
		    parameterURI = input.name();
		}
		parameterURI = namespace + parameterURI;
		if (input.filteringClass().equals(void.class)) {
		    filteringClass = methodParameterTypes[i];
		} else {
		    filteringClass = input.filteringClass();
		}
		srv.createInputWrapper(parameterURI, filteringClass,
			defaultInputCardinality, input.propertyPaths());
	    }
	}

	for (int i = 0; i < methodOutputAnnotations.size(); i++) {
	    Output output = methodOutputAnnotations.get(i);
	    if (output.propertyPaths().length > 0) {
		parameterURI = namespace + "output1";
		if (!"".equals(output.name())) {
		    parameterURI = namespace + output.name();
		}
		Cardinality card = output.cardinality();
		/*
		 * only if cardinality is not specified assume the one inferred
		 * from method signature
		 */
		if (card == Cardinality.NOT_SPECIFIED) {
		    card = defaultOutputCardinality;
		}
		filteringClass = methodOutputAnnotations.get(i)
			.filteringClass();
		if (filteringClass.equals(void.class)) {
		    if (methodOutputType.getComponentType() != null) {
			filteringClass = methodOutputType.getComponentType();
			// todo convert lists
		    } else {
			filteringClass = methodOutputType;
		    }
		}
		String[] propertyPaths = output.propertyPaths();
		srv.addOutputWrapper(parameterURI, filteringClass, card,
			propertyPaths);
	    }
	}
	for (int i = 0; i < methodChangeEffects.size(); i++) {
	    ChangeEffect ce = methodChangeEffects.get(i);
	    srv.addChangeEffectWrapper(ce.propertyPaths(), ce.value(), ce
		    .valueType());
	}
	return srv.getProfile();
    }

    public void unregisterAll() {
	for (ServiceCalleeWrapper wrapper : wrappers) {
	    wrapper.close();
	}
    }

}
