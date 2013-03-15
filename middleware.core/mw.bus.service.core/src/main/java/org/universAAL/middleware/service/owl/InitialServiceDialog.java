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
package org.universAAL.middleware.service.owl;

import java.util.Hashtable;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public final class InitialServiceDialog extends UserInterfaceService {

    public static final String MY_URI;
    private static Hashtable isdClassLevelRestrictions = new Hashtable();
    static {
	MY_URI = uAAL_SERVICE_NAMESPACE + "InitialServiceDialog";
    }

    /**
     * @see ManagedIndividual#getClassURI()
     */
    public String getClassURI() {
	return MY_URI;
    }

    /**
     * Creates and returns an appropriate {@link ServiceProfile} for an initial
     * dialog.
     * 
     * @see #createServiceProfile(UserInterfaceService, String, String, String,
     *      String)
     */
    public static ServiceProfile createInitialDialogProfile(
	    String serviceClassURI, String vendor, String description,
	    String startServiceURI) {
	return createServiceProfile(new InitialServiceDialog(startServiceURI),
		serviceClassURI, vendor, description, startServiceURI);
    }

    /**
     * @see #getUIServiceDescription(UserInterfaceService, String, String,
     *      ServiceCaller)
     */
    public static String getInitialDialogDescription(String serviceClassURI,
	    String vendor, ServiceCaller theCaller) {
	return getUIServiceDescription(new InitialServiceDialog(),
		serviceClassURI, vendor, theCaller);
    }

    /**
     * @see #getUIServiceInfo(UserInterfaceService, String, ServiceCaller)
     */
    public static UserInterfaceService[] getInitialDialogInfo(
	    String serviceClassURI, ServiceCaller theCaller) {
	return getUIServiceInfo(new InitialServiceDialog(), serviceClassURI,
		theCaller);
    }

    /**
     * @see #getUIServiceRequest(UserInterfaceService, String, String, Resource)
     */
    public static ServiceRequest getInitialDialogRequest(
	    String serviceClassURI, String vendor, Resource requestingUser) {
	return getUIServiceRequest(new InitialServiceDialog(), serviceClassURI,
		vendor, requestingUser);
    }

    /**
     * Start an initial dialog. This method will issue a service request on the
     * service bus to start a registered initial dialog service.
     * 
     * @param serviceClassURI
     *            the URI of the service class from an underlying ontology, e.g.
     *            the value of <code>Lighting.MY_URI</code> from the lighting
     *            example.
     * @param vendor
     *            the URL of the partner home page that provides the UI e.g.
     *            <code>"http://www.igd.fraunhofer.de"</code>
     * @param requestingUser
     *            the user that requested the UI service.
     * @param theCaller
     * @return true, if the call is successful, i.e. the initial dialog that was
     *         called returned {@link CallStatus#succeeded}
     */
    public static boolean startInitialDialog(String serviceClassURI,
	    String vendor, Resource requestingUser, ServiceCaller theCaller) {
	ServiceResponse resp = theCaller.call(getUIServiceRequest(
		new InitialServiceDialog(), serviceClassURI, vendor,
		requestingUser));
	return resp.getCallStatus() == CallStatus.succeeded;
    }

    /**
     * For exclusive use by serializers.
     */
    public InitialServiceDialog() {
	super();
    }

    /**
     * Create a new initial dialog service.
     * 
     * @param uri
     *            URI of this initial dialog service.
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
