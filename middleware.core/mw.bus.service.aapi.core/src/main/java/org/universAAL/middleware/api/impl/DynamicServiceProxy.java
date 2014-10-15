/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

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
package org.universAAL.middleware.api.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.universAAL.middleware.api.annotation.ChangeEffect;
import org.universAAL.middleware.api.annotation.Input;
import org.universAAL.middleware.api.annotation.Output;
import org.universAAL.middleware.api.annotation.ServiceOperation;
import org.universAAL.middleware.api.annotation.ServiceOperation.MatchMakingType;
import org.universAAL.middleware.api.exception.SimplifiedRegistrationException;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.aapi.AapiServiceRequest;
import org.universAAL.middleware.service.owl.Service;

public class DynamicServiceProxy implements InvocationHandler {
    private ServiceCaller caller;

    private AnnotationScanner scanner;

    private Class<?> serviceClass;

    public DynamicServiceProxy(ServiceCaller caller, Class<?> intefaceClazz)
	    throws SimplifiedRegistrationException, InstantiationException,
	    IllegalAccessException {
	this.caller = caller;
	scanner = new AnnotationScanner(intefaceClazz);
	scanner.scan();

	Class<?>[] rscClasses = scanner.getResourceClasses().value();
	Service service = null;
	for (int i = 0; i < rscClasses.length; i++) {
	    if (Service.class.isAssignableFrom(rscClasses[i])) {
		serviceClass = rscClasses[i];
		service = (Service) serviceClass.newInstance();
		break;
	    }
	}
	if (service == null) {
	    throw new SimplifiedRegistrationException(
		    "Cannot find subclass of Service in @ResourceClasses");
	}
    }

    private ServiceOperation.MatchMakingType inferMatchMakingType(
	    List<Output> outputs, List<Input> inputs,
	    List<ChangeEffect> changeEffects) {
	ServiceOperation.MatchMakingType inferredType = MatchMakingType.BY_URI;
	for (Output output : outputs) {
	    if (output.propertyPaths().length > 0) {
		inferredType = MatchMakingType.ONTOLOGICAL;
	    }
	}
	for (Input input : inputs) {
	    if (input.propertyPaths().length > 0) {
		inferredType = MatchMakingType.ONTOLOGICAL;
	    }
	}
	if (changeEffects.size() > 0) {
	    inferredType = MatchMakingType.ONTOLOGICAL;
	}
	return inferredType;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
	    throws Throwable {
	try {
	    ServiceOperation so = scanner.getMethodServiceOperation().get(
		    m.getName());

	    String serviceName = so.value();
	    if ("".equals(serviceName)) {
		serviceName = m.getName();
	    }
	    String serviceURI = AnnotationScanner.createServiceUri(
		    scanner.getNamespace(), scanner.getName(), serviceName);

	    List<Output> outputs = scanner.getMethodOutputs().get(serviceURI);
	    List<Input> inputs = scanner.getMethodInputs().get(serviceURI);
	    List<ChangeEffect> changeEffects = scanner.getMethodChangeEffects()
		    .get(serviceURI);

	    ServiceOperation.MatchMakingType matchMakingType = so.type();
	    if (matchMakingType == MatchMakingType.NOT_SPECIFIED) {
		matchMakingType = inferMatchMakingType(outputs, inputs,
			changeEffects);
	    }

	    AapiServiceRequest request = null;
	    switch (matchMakingType) {
	    case ONTOLOGICAL:
		request = new AapiServiceRequest(
			(Service) serviceClass.newInstance(), null);
		break;
	    case BY_URI:
		Constructor<?> serviceClassConstructor = serviceClass
			.getConstructor(String.class);
		Service serviceByUri = (Service) serviceClassConstructor
			.newInstance(serviceURI);
		request = new AapiServiceRequest(serviceByUri, null);
		break;
	    default:
		throw new IllegalArgumentException();
	    }
	    for (int i = 0; i < outputs.size(); i++) {
		Output output = outputs.get(i);
		if (output.propertyPaths().length > 0) {
		    request.addRequiredOutput("output" + i,
			    output.propertyPaths());
		}
	    }
	    for (int i = 0; i < inputs.size(); i++) {
		Input input = inputs.get(i);
		if (input.propertyPaths().length > 0) {
		    request.addValueFilter(input.propertyPaths(), args[i]);
		} else {
		    request.addInput(
			    AnnotationScanner.createParameterUri(
				    scanner.getNamespace(), null, input.name()),
			    args[i]);
		}
	    }
	    for (int i = 0; i < changeEffects.size(); i++) {
		ChangeEffect ceAnnotation = changeEffects.get(i);
		request.addChangeEffect(ceAnnotation.propertyPaths(),
			TypeMapper.getJavaInstance(ceAnnotation.value(),
				TypeMapper.getDatatypeURI(ceAnnotation
					.valueType())));
	    }
	    ServiceResponse response = caller.call(request);
	    if (!void.class.equals(m.getReturnType())) {
		if (outputs.size() > 1) {
		    Object[] retObj = new Object[outputs.size()];
		    for (int i = 0; i < outputs.size(); i++) {
			Output outputAnnotation = outputs.get(i);
			List<?> output = null;
			if (outputAnnotation.propertyPaths().length > 0) {
			    output = response.getOutput("output" + i, true);
			} else {
			    output = response.getOutput(AnnotationScanner
				    .createParameterUri(scanner.getNamespace(),
					    null, outputAnnotation.name()),
				    true);
			}
			if (output.size() == 1) {
			    retObj[i] = output.get(0);
			} else {
			    retObj[i] = output;
			}
		    }
		    return retObj;
		} else {
		    Output outputAnnotation = outputs.get(0);
		    // List<?> output = null;
		    Object resObj = null;
		    if (outputAnnotation.propertyPaths().length > 0) {
			resObj = response.getOutput("output0", true);
		    } else {
			resObj = response.getOutput(AnnotationScanner
				.createParameterUri(scanner.getNamespace(),
					null, outputAnnotation.name()), true);
		    }
		    if (resObj == null) {
			return null;
		    }
		    List<?> temp = ((List<?>) resObj);
		    if (Object[].class.isAssignableFrom(m.getReturnType())) {
			return temp.toArray((Object[]) Array.newInstance(m
				.getReturnType().getComponentType(), temp
				.size()));
		    } else if (List.class.isAssignableFrom(m.getReturnType())) {
			return temp;
		    } else {
			if (temp.size() == 0) {
			    return null;
			}
			return temp.get(0);
		    }
		}
	    }
	    return null;
	} catch (Exception e) {
	    throw e;
	}
    }

    public static Object newInstance(Class<?> interfaceClazz,
	    ServiceCaller caller) throws IllegalArgumentException,
	    SimplifiedRegistrationException, InstantiationException,
	    IllegalAccessException {
	return Proxy.newProxyInstance(interfaceClazz.getClassLoader(),
		new Class[] { interfaceClazz }, new DynamicServiceProxy(caller,
			interfaceClazz));
    }
}
