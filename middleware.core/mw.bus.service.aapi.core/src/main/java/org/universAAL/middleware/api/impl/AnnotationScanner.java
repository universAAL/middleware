package org.universAAL.middleware.api.impl;

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
import org.universAAL.middleware.api.annotation.Output;
import org.universAAL.middleware.api.annotation.Outputs;
import org.universAAL.middleware.api.annotation.OntologyClasses;
import org.universAAL.middleware.api.annotation.ServiceOperation;
import org.universAAL.middleware.api.annotation.UniversAALService;
import org.universAAL.middleware.api.exception.SimplifiedRegistrationException;

public class AnnotationScanner {
    private UniversAALService serviceAnnotation;
    private OntologyClasses resourceClasses;
    private Map<String, ServiceOperation> methodServiceOperation = new HashMap<String, ServiceOperation>();
    private Map<String, List<Output>> methodOutputs = new HashMap<String, List<Output>>();
    private Map<String, List<Input>> methodInputs = new HashMap<String, List<Input>>();
    private Map<String, List<ChangeEffect>> methodChangeEffects = new HashMap<String, List<ChangeEffect>>();
    private Map<String, List<String>> annotatedMethodsParametersNames = new HashMap<String, List<String>>();

    private String namespace;
    private String name;

    private Class scannedClazz;

    public AnnotationScanner(Class scannedClazz) {
	this.scannedClazz = scannedClazz;
    }

    public static String createServiceUri(String namespace, String serviceName,
	    String methodName) {
	return namespace + methodName;
    }

    public static String createParameterUri(String namespace,
	    String serviceName, String parameterUri) {
	return namespace + parameterUri;
    }

    public void scan() throws SimplifiedRegistrationException {
	for (Annotation a : scannedClazz.getAnnotations()) {
	    if (a.annotationType().equals(UniversAALService.class)) {
		serviceAnnotation = (UniversAALService) a;
		namespace = ((UniversAALService) a).namespace();
		name = ((UniversAALService) a).name();
		if ("".equals(name)) {
		    name = scannedClazz.getSimpleName();
		}
	    } else if (a.annotationType().equals(OntologyClasses.class)) {
		resourceClasses = (OntologyClasses) a;
	    }
	}

	for (Method m : scannedClazz.getMethods()) {
	    List<Output> methodOutputAnnotations = new ArrayList<Output>();
	    List<Input> methodInputsAnnotations = new ArrayList<Input>();
	    List<ChangeEffect> methodChangeEffectsAnnotations = new ArrayList<ChangeEffect>();
	    String serviceName = "";
	    for (Annotation a : m.getAnnotations()) {
		if (a.annotationType().equals(ServiceOperation.class)) {
		    methodServiceOperation.put(m.getName(),
			    (ServiceOperation) a);
		    serviceName = ((ServiceOperation) a).value();
		    if ("".equals(serviceName)) {
			serviceName = m.getName();
		    }
		} else if (a.annotationType().equals(Output.class)
			|| a.annotationType().equals(Outputs.class)) {
		    Output[] temp = null;
		    if (a.annotationType().equals(Output.class)) {
			temp = new Output[] { (Output) a };
		    } else {
			temp = ((Outputs) a).value();
		    }
		    for (int i = 0; i < temp.length; i++) {
			methodOutputAnnotations.add((Output) temp[i]);
		    }
		} else if (a.annotationType().equals(ChangeEffect.class)) {
		    methodChangeEffectsAnnotations.add((ChangeEffect) a);
		} else if (a.annotationType().equals(ChangeEffects.class)) {
		    methodChangeEffectsAnnotations.addAll(Arrays
			    .asList(((ChangeEffects) a).value()));
		}
	    }
	    if (methodOutputAnnotations.isEmpty()) {
		Class returnType = m.getReturnType();
		if (returnType != void.class) {
		    String name = m.getName();
		    String outputName = null;
		    if (name.startsWith("get") && name.length() > 3) {
			outputName = String
				.format("%s%s", Character.toLowerCase(name
					.charAt(3)), name.substring(4));
		    }
		    final String finalOutputName = outputName;
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

			public Class filteringClass() {
			    return void.class;
			}

			public Cardinality cardinality() {
			    return Cardinality.NOT_SPECIFIED;
			}
		    };
		    methodOutputAnnotations.add(inferredOutput);
		}
	    }
	    methodOutputs.put(createServiceUri(namespace, name, serviceName),
		    methodOutputAnnotations);
	    methodChangeEffects.put(createServiceUri(namespace, name,
		    serviceName), methodChangeEffectsAnnotations);
	    Annotation[][] parameterAnnotations = m.getParameterAnnotations();
	    for (int i = 0; i < parameterAnnotations.length; i++) {
		Input input = null;
		for (int j = 0; j < parameterAnnotations[i].length; j++) {
		    if (parameterAnnotations[i][j].annotationType().equals(
			    Input.class)) {
			input = (Input) parameterAnnotations[i][j];
			break;
		    }
		}
		if (input == null) {
		    throw new SimplifiedRegistrationException(
			    "Does not found @input annotation for " + i
				    + " parameter of " + m.getName()
				    + " method!");
		}
		methodInputsAnnotations.add(input);
	    }
	    methodInputs.put(createServiceUri(namespace, name, serviceName),
		    methodInputsAnnotations);
	}
    }

    public UniversAALService getServiceAnnotation() {
	return serviceAnnotation;
    }

    public void setServiceAnnotation(UniversAALService serviceAnnotation) {
	this.serviceAnnotation = serviceAnnotation;
    }

    public OntologyClasses getResourceClasses() {
	return resourceClasses;
    }

    public void setResourceClasses(OntologyClasses resourceClasses) {
	this.resourceClasses = resourceClasses;
    }

    public Map<String, ServiceOperation> getMethodServiceOperation() {
	return methodServiceOperation;
    }

    public void setMethodServiceOperation(
	    Map<String, ServiceOperation> methodServiceOperation) {
	this.methodServiceOperation = methodServiceOperation;
    }

    public Map<String, List<Output>> getMethodOutputs() {
	return methodOutputs;
    }

    public void setMethodOutputs(Map<String, List<Output>> methodOutputs) {
	this.methodOutputs = methodOutputs;
    }

    public Map<String, List<Input>> getMethodInputs() {
	return methodInputs;
    }

    public void setMethodInputs(Map<String, List<Input>> methodInputs) {
	this.methodInputs = methodInputs;
    }

    public Map<String, List<ChangeEffect>> getMethodChangeEffects() {
	return methodChangeEffects;
    }

    public void setMethodChangeEffects(
	    Map<String, List<ChangeEffect>> methodChangeEffects) {
	this.methodChangeEffects = methodChangeEffects;
    }

    public Class getScannedClazz() {
	return scannedClazz;
    }

    public void setScannedClazz(Class scannedClazz) {
	this.scannedClazz = scannedClazz;
    }

    public void setNamespace(String namespace) {
	this.namespace = namespace;
    }

    public String getNamespace() {
	return namespace;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }
}
