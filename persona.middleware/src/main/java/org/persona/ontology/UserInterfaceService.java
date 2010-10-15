/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.persona.ontology;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.persona.middleware.PResource;
import org.persona.middleware.TypeMapper;
import org.persona.middleware.service.CallStatus;
import org.persona.middleware.service.PropertyPath;
import org.persona.middleware.service.ServiceCaller;
import org.persona.middleware.service.ServiceRequest;
import org.persona.middleware.service.ServiceResponse;
import org.persona.middleware.service.process.ProcessOutput;
import org.persona.middleware.service.profile.ServiceProfile;
import org.persona.ontology.expr.Restriction;

/**
 * @author mtazari
 *
 */
public class UserInterfaceService extends Service {
	public static final String SERVICE_REQUEST_URI_PREFIX_INFO = PERSONA_SERVICE_NAMESPACE + "UIServiceInfo";
	public static final String SERVICE_REQUEST_URI_PREFIX_START = PERSONA_SERVICE_NAMESPACE + "UIServiceStart";
	public static final String OUTPUT_INSTANCE_INFO = PERSONA_SERVICE_NAMESPACE + "uiServiceInfo";

	public static final String MY_URI;
	public static final String PROP_CORRELATED_SERVICE_CLASS;
	public static final String PROP_DESCRIPTION;
	public static final String PROP_HAS_INFO_RETRIEVAL_PROCESS;
	public static final String PROP_HAS_VENDOR;
	private static Hashtable uisClassLevelRestrictions = new Hashtable();
	static {
		MY_URI = PERSONA_SERVICE_NAMESPACE + "UserInterfaceService";
		
		PROP_CORRELATED_SERVICE_CLASS = PERSONA_SERVICE_NAMESPACE + "correlatedServiceClass";
		PROP_DESCRIPTION = PERSONA_SERVICE_NAMESPACE + "description";
		PROP_HAS_INFO_RETRIEVAL_PROCESS = PERSONA_SERVICE_NAMESPACE + "infoRetrievalProcess";
		PROP_HAS_VENDOR = PERSONA_SERVICE_NAMESPACE + "vendor";
		
		uisClassLevelRestrictions.put(PROP_CORRELATED_SERVICE_CLASS, 
				Restriction.getAllValuesRestrictionWithCardinality(
						PROP_CORRELATED_SERVICE_CLASS, 
						TypeMapper.getDatatypeURI(PResource.class), 1, 1));
		uisClassLevelRestrictions.put(PROP_DESCRIPTION, 
				Restriction.getAllValuesRestrictionWithCardinality(
						PROP_DESCRIPTION, 
						TypeMapper.getDatatypeURI(String.class), 1, 1));
		uisClassLevelRestrictions.put(PROP_HAS_INFO_RETRIEVAL_PROCESS, 
				Restriction.getAllValuesRestrictionWithCardinality(
						PROP_HAS_INFO_RETRIEVAL_PROCESS, 
						TypeMapper.getDatatypeURI(PResource.class), 1, 1));
		uisClassLevelRestrictions.put(PROP_HAS_VENDOR, 
				Restriction.getAllValuesRestrictionWithCardinality(
						PROP_HAS_VENDOR, 
						TypeMapper.getDatatypeURI(PResource.class), 1, 1));
		
		register(UserInterfaceService.class);
	}
	
	/**
	 * Creates and returns an appropriate ServiceProfile for a UI
	 * service that upon call would lead to publishing an output event
	 * by the matching service component.
	 * 
	 * @param serviceClassURI the URI of the service class from an underlying
	 *                        ontology, e.g. the value of <code>Lighting.MY_URI
	 *                        </code> from the lighting example.
	 * @param vendor the URL of the partner home page that provides the UI
	 *               e.g. <code>"http://www.igd.fraunhofer.de"</code>
	 * @param description describes what the service does, e.g. <code>"The main screen
	 *                    of a lighting service component by Fraunhofer-IGD that allows
	 *                    human users to view and change the states of light sources
	 *                    found on a connected KNX bus. Special visualization techniques,
	 *                    such as usage of 3D scenes and moving the camera from the
	 *                    current viewpoint of the user all the way towards the location of
	 *                    a currently selected light source, lead to a unique user
	 *                    experience in controlling light sources."</code>
	 * @param startServiceURI is a URI that allows the service component to
	 *                        recognize that it should now publish an appropriate
	 *                        output event within the 'handleCall' method of
	 *                        the ServiceCallee subclass implemented by the
	 *                        service component.
	 * 
	 * @return The created service profile that can be used to register the service
	 *         with the service bus.
	 */
	public static final ServiceProfile createServiceProfile(String serviceClassURI,
			String vendor, String description,
			String startServiceURI) {
		return createServiceProfile(new UserInterfaceService(startServiceURI), serviceClassURI, vendor, description, startServiceURI);
	}
	
	protected static ServiceProfile createServiceProfile(UserInterfaceService uis, String serviceClassURI,
			String vendor, String description,
			String startServiceURI) {
		uis.setProperty(PROP_CORRELATED_SERVICE_CLASS,
				new PResource(serviceClassURI, true));
		uis.setProperty(PROP_DESCRIPTION, description);
		uis.setProperty(PROP_HAS_VENDOR, new PResource(vendor, true));
		return uis.myProfile;
	}
	
	public static Restriction getClassRestrictionsOnProperty(String propURI) {
		if (propURI != null) {
			Object o = uisClassLevelRestrictions.get(propURI);
			if (o instanceof Restriction)
				return (Restriction) o;
		}
		return Service.getClassRestrictionsOnProperty(propURI);
	}
	
	public static String getRDFSComment() {
		return "The class of all services starting an initial dialog correlated to a specific service class";
	}
	
	public static String getRDFSLabel() {
		return "Initial Service Dialog";
	}
	
	public static String[] getStandardPropertyURIs() {
		String[] inherited = Service.getStandardPropertyURIs();
		String[] toReturn = new String[inherited.length+4];
		int i = 0;
		while (i < inherited.length) {
			toReturn[i] = inherited[i];
			i++;
		}
		toReturn[i++] = PROP_CORRELATED_SERVICE_CLASS;
		toReturn[i++] = PROP_DESCRIPTION;
		toReturn[i++] = PROP_HAS_INFO_RETRIEVAL_PROCESS;
		toReturn[i] = PROP_HAS_VENDOR;
		return toReturn;
	}
	
	public static final String getUIServiceDescription(String serviceClassURI, String vendor, ServiceCaller theCaller) {
		return getUIServiceDescription(new UserInterfaceService(), serviceClassURI, vendor, theCaller);
	}
	
	protected static String getUIServiceDescription(UserInterfaceService requestedService, String serviceClassURI, String vendor, ServiceCaller theCaller) {
		requestedService.addInstanceLevelRestriction(
				Restriction.getFixedValueRestriction(PROP_CORRELATED_SERVICE_CLASS,
						new PResource(serviceClassURI, true)),
				new String[]{PROP_CORRELATED_SERVICE_CLASS});
		requestedService.addInstanceLevelRestriction(
				Restriction.getFixedValueRestriction(PROP_HAS_VENDOR,
						new PResource(vendor, true)),
				new String[]{PROP_HAS_VENDOR});
		ServiceRequest req = new ServiceRequest(SERVICE_REQUEST_URI_PREFIX_INFO, 5, requestedService, null);
		req.addSimpleOutputBinding(
				new ProcessOutput(OUTPUT_INSTANCE_INFO),
				new PropertyPath(null, true, new String[] {PROP_DESCRIPTION}));
		ServiceResponse resp = theCaller.call(req);
		if (resp.getCallStatus() == CallStatus.succeeded) {
			List outputs = resp.getOutputs();
			if (outputs != null)
				for (Iterator i=outputs.iterator(); i.hasNext();) {
					ProcessOutput output = (ProcessOutput) i.next();
					if (output.getURI().equals(OUTPUT_INSTANCE_INFO)) {
						Object o = output.getParameterValue();
						if (o instanceof String)
							return (String) o;
					}
				}
		}
		return null;
	}
	
	public static final UserInterfaceService[] getUIServiceInfo(String serviceClassURI, ServiceCaller theCaller) {
		return getUIServiceInfo(new UserInterfaceService(), serviceClassURI, theCaller);
	}
	
	protected static UserInterfaceService[] getUIServiceInfo(UserInterfaceService requestedService, String serviceClassURI, ServiceCaller theCaller) {
		requestedService.addInstanceLevelRestriction(
				Restriction.getFixedValueRestriction(PROP_CORRELATED_SERVICE_CLASS,
						new PResource(serviceClassURI, true)),
				new String[]{PROP_CORRELATED_SERVICE_CLASS});
		ServiceRequest req = new ServiceRequest(SERVICE_REQUEST_URI_PREFIX_INFO, 5, requestedService, null);
		req.addSimpleOutputBinding(
				new ProcessOutput(OUTPUT_INSTANCE_INFO),
				new PropertyPath(null, true, new String[] {MY_URI}));
		ServiceResponse resp = theCaller.call(req);
		if (resp.getCallStatus() == CallStatus.succeeded) {
			List outputs = resp.getOutputs();
			if (outputs != null)
				for (Iterator i=outputs.iterator(); i.hasNext();) {
					ProcessOutput output = (ProcessOutput) i.next();
					if (output.getURI().equals(OUTPUT_INSTANCE_INFO)) {
						Object o = output.getParameterValue();
						if (o instanceof List) {
							try {
								return (UserInterfaceService[])
								((List) o).toArray(new UserInterfaceService[((List) o).size()]);
							} catch (Exception e) {}
						}
					}
				}
		}
		return null;
	}
	
	public static final ServiceRequest getUIServiceRequest(String serviceClassURI, String vendor, PResource requestingUser) {
		return getUIServiceRequest(new UserInterfaceService(), serviceClassURI, vendor, requestingUser);
	}
	
	protected static ServiceRequest getUIServiceRequest(UserInterfaceService requestedService, String serviceClassURI, String vendor, PResource requestingUser) {
		requestedService.addInstanceLevelRestriction(
				Restriction.getFixedValueRestriction(PROP_CORRELATED_SERVICE_CLASS, new PResource(serviceClassURI, true)),
				new String[]{PROP_CORRELATED_SERVICE_CLASS});
		requestedService.addInstanceLevelRestriction(
				Restriction.getFixedValueRestriction(PROP_HAS_VENDOR, new PResource(vendor, true)),
				new String[]{PROP_HAS_VENDOR});
		return new ServiceRequest(SERVICE_REQUEST_URI_PREFIX_START, 5, requestedService, requestingUser);
	}
	
//	public static UserInterfaceService[] getVendorUIServices(String vendor, ServiceCaller theCaller) {
//		UserInterfaceService requestedService = new UserInterfaceService();
//		requestedService.instanceLevelRestrictions.put(PROP_HAS_VENDOR,
//				Restriction.getFixedValueRestriction(PROP_HAS_VENDOR, new PResource(vendor, true)));
//		ServiceRequest req = new ServiceRequest(SERVICE_REQUEST_URI_PREFIX_INFO, 5, requestedService, null);
//		req.addSimpleOutputBinding(
//				new ProcessOutput(OUTPUT_INSTANCE_INFO),
//				new PropertyPath(null, true, new String[] {MY_URI}));
//		ServiceResponse resp = theCaller.call(req);
//		if (resp.getCallStatus() == CallStatus.succeeded) {
//			List outputs = resp.getOutputs();
//			if (outputs != null)
//				for (Iterator i=outputs.iterator(); i.hasNext();) {
//					ProcessOutput output = (ProcessOutput) i.next();
//					if (output.getURI().equals(OUTPUT_INSTANCE_INFO)) {
//						Object o = output.getParameterValue();
//						if (o instanceof List) {
//							try {
//								return (UserInterfaceService[])
//								((List) o).toArray(new UserInterfaceService[((List) o).size()]);
//							} catch (Exception e) {}
//						}
//					}
//				}
//		}
//		return null;
//	}

	/**
	 * 
	 */
	protected UserInterfaceService() {
		super();
	}

	/**
	 * @param uri
	 */
	protected UserInterfaceService(String uri) {
		super(uri);
	}

	/**
	 * @see org.persona.ontology.Service#getClassLevelRestrictions()
	 */
	protected Hashtable getClassLevelRestrictions() {
		return uisClassLevelRestrictions;
	}

	/**
	 * @see org.persona.ontology.ManagedIndividual#getPropSerializationType(java.lang.String)
	 */
	public int getPropSerializationType(String propURI) {
		return PROP_SERIALIZATION_FULL;
	}
}
