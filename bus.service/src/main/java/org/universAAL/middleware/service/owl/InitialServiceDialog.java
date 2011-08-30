/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.service.owl;

import java.util.Hashtable;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public final class InitialServiceDialog extends UserInterfaceService {

	public static final String MY_URI;
	private static Hashtable isdClassLevelRestrictions = new Hashtable();
	static {
		MY_URI = uAAL_SERVICE_NAMESPACE + "InitialServiceDialog";
	}
	
	
	@Override
	public String getClassURI() {
	    return MY_URI;
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
	
	
	/**
	 * Returns the UIServiceDescription from an initial Dialog Description
	 * that upon call would lead to publishing an output event
	 * by the matching service component.
	 * 
	 * @param serviceClassURI the URI of the service class from an underlying
	 *                        ontology, e.g. the value of <code>Lighting.MY_URI
	 *                        </code> from the lighting example.
	 * @param vendor the URL of the partner home page that provides the UI
	 *               e.g. <code>"http://www.igd.fraunhofer.de"</code>
	 * 
	 * @param ServiceCaller instance is the parameter that will be used to call the service.
	 * 
	 * 
	 * 
	 
	 * @return The created service profile that can be used to register the service
	 *         with the service bus.
	 */
	
	
	public static final String getInitialDialogDescription(String serviceClassURI, String vendor, ServiceCaller theCaller) {
		return getUIServiceDescription(new InitialServiceDialog(), serviceClassURI, vendor, theCaller);
	}
	
	/**
	 * Returns the UIServiceDescription from an initial Dialog Description
	 * that upon call would lead to publishing an output event
	 * by the matching service component.
	 * 
	 * @param serviceClassURI the URI of the service class from an underlying
	 *                        ontology, e.g. the value of <code>Lighting.MY_URI
	 *                        </code> from the lighting example.
	 * 
	 * 
	 * @param ServiceCaller instance is the parameter that will be used to call the service.
	 * 
	 * 
	 * 
	 
	 * @return The created service profile that can be used to register the service
	 *         with the service bus.
	 */
	
	public static final UserInterfaceService[] getInitialDialogInfo(String serviceClassURI, ServiceCaller theCaller) {
		return getUIServiceInfo(new InitialServiceDialog(), serviceClassURI, theCaller);
	}
	
	/**
	 * Returns the UIServiceDescription from an initial Dialog Description
	 * that upon call would lead to publishing an output event
	 * by the matching service component.
	 * 
	 * @param serviceClassURI the URI of the service class from an underlying
	 *                        ontology, e.g. the value of <code>Lighting.MY_URI
	 *                        </code> from the lighting example.
	 *                        
	 * @param vendor the URL of the partner home page that provides the UI
	 *               e.g. <code>"http://www.igd.fraunhofer.de"</code>
	 * 
	 * @param Resource the user associated to the UI requested.  
	 * 
	 * 
	 * 
	 
	 * @return The created service profile that can be used to register the service
	 *         with the service bus.
	 */
	
	public static final ServiceRequest getInitialDialogRequest(String serviceClassURI, String vendor, Resource requestingUser) {
		return getUIServiceRequest(new InitialServiceDialog(), serviceClassURI, vendor, requestingUser);
	}
	
	/**
	 * Returns the UIServiceDescription from an initial Dialog Description
	 * that upon call would lead to publishing an output event
	 * by the matching service component.
	 * 
	 * @param serviceClassURI the URI of the service class from an underlying
	 *                        ontology, e.g. the value of <code>Lighting.MY_URI
	 *                        </code> from the lighting example.
	 * 
	 * 
	 * @param ServiceCaller instance is the parameter that will be used to call the service.
	 * 
	 * 
	 * 
	 
	 * @return The created service profile that can be used to register the service
	 *         with the service bus.
	 */
	
	public static boolean startInitialDialog(String serviceClassURI, String vendor, Resource requestingUser, ServiceCaller theCaller) {
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
	 * @see org.universAAL.middleware.service.owl.Service#getClassLevelRestrictions()
	 */
	protected Hashtable getClassLevelRestrictions() {
		return isdClassLevelRestrictions;
	}
}
