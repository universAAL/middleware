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

import org.persona.middleware.PResource;
import org.persona.middleware.service.CallStatus;
import org.persona.middleware.service.ServiceCaller;
import org.persona.middleware.service.ServiceRequest;
import org.persona.middleware.service.ServiceResponse;
import org.persona.middleware.service.profile.ServiceProfile;

/**
 * @author mtazari
 *
 */
public final class InitialServiceDialog extends UserInterfaceService {
	public static final String MY_URI;
	private static Hashtable isdClassLevelRestrictions = new Hashtable();
	static {
		MY_URI = PERSONA_SERVICE_NAMESPACE + "InitialServiceDialog";
		
		register(InitialServiceDialog.class);
	}
	
	/**
	 * Creates and returns an appropriate ServiceProfile for an initial
	 * dialog that upon call would lead to publishing an output event
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
	public static final ServiceProfile createInitialDialogProfile(String serviceClassURI,
			String vendor, String description,
			String startServiceURI) {
		return createServiceProfile(new InitialServiceDialog(startServiceURI), serviceClassURI, vendor, description, startServiceURI);
	}
	
	public static final String getInitialDialogDescription(String serviceClassURI, String vendor, ServiceCaller theCaller) {
		return getUIServiceDescription(new InitialServiceDialog(), serviceClassURI, vendor, theCaller);
	}
	
	public static final UserInterfaceService[] getInitialDialogInfo(String serviceClassURI, ServiceCaller theCaller) {
		return getUIServiceInfo(new InitialServiceDialog(), serviceClassURI, theCaller);
	}
	
	public static final ServiceRequest getInitialDialogRequest(String serviceClassURI, String vendor, PResource requestingUser) {
		return getUIServiceRequest(new InitialServiceDialog(), serviceClassURI, vendor, requestingUser);
	}
	
	public static String getRDFSComment() {
		return "The class of all services starting an initial dialog correlated to a specific service class";
	}
	
	public static String getRDFSLabel() {
		return "Initial Service Dialog";
	}
	
	public static boolean startInitialDialog(String serviceClassURI, String vendor, PResource requestingUser, ServiceCaller theCaller) {
		ServiceResponse resp = theCaller.call(getUIServiceRequest(new InitialServiceDialog(), serviceClassURI, vendor, requestingUser));
		return resp.getCallStatus() == CallStatus.succeeded;
	}

	/**
	 * 
	 */
	public InitialServiceDialog() {
		super();
	}

	/**
	 * @param uri
	 */
	public InitialServiceDialog(String uri) {
		super(uri);
	}

	/**
	 * @see org.persona.ontology.Service#getClassLevelRestrictions()
	 */
	protected Hashtable getClassLevelRestrictions() {
		return isdClassLevelRestrictions;
	}
}
