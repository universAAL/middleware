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
package org.persona.middleware;

import java.util.Hashtable;

import org.persona.middleware.context.ContextEvent;
import org.persona.middleware.context.ContextEventPattern;
import org.persona.middleware.dialog.ChoiceItem;
import org.persona.middleware.dialog.ChoiceList;
import org.persona.middleware.dialog.Form;
import org.persona.middleware.dialog.Group;
import org.persona.middleware.dialog.InputField;
import org.persona.middleware.dialog.Label;
import org.persona.middleware.dialog.MediaObject;
import org.persona.middleware.dialog.Range;
import org.persona.middleware.dialog.Select;
import org.persona.middleware.dialog.Select1;
import org.persona.middleware.dialog.SubdialogTrigger;
import org.persona.middleware.dialog.Submit;
import org.persona.middleware.dialog.TextArea;
import org.persona.middleware.dialog.SimpleOutput;
import org.persona.middleware.input.InputEvent;
import org.persona.middleware.output.OutputEvent;
import org.persona.middleware.output.OutputEventPattern;
import org.persona.middleware.service.AggregatingFilter;
import org.persona.middleware.service.PropertyPath;
import org.persona.middleware.service.ServiceCall;
import org.persona.middleware.service.ServiceRequest;
import org.persona.middleware.service.ServiceResponse;
import org.persona.middleware.service.impl.ServiceRealization;
import org.persona.middleware.service.process.ProcessParameter;
import org.persona.middleware.service.profile.ServiceProfile;

/**
 * @author mtazari
 *
 */
public class MiddlewareConstants {
	// bus names
	public static final String PERSONA_BUS_NAME_CONTEXT = "persona.bus.context";
	public static final String PERSONA_BUS_NAME_INPUT = "persona.bus.input";
	public static final String PERSONA_BUS_NAME_OUTPUT = "persona.bus.output";
	public static final String PERSONA_BUS_NAME_SERVICE = "persona.bus.service";
	
	// URIs of standard variables managed by the PERSONA middleware
	/**
	 * The URI of a standard variable managed by the PERSONA middleware indicating
	 * the current time.
	 */
	public static final String VAR_PERSONA_CURRENT_DATETIME =
		PResource.PERSONA_VOCABULARY_NAMESPACE + "currentDatetime";
	
	/**
	 * The URI of a standard variable managed by the PERSONA middleware indicating
	 * the software component currently accessing the middleware.
	 */
	public static final String VAR_PERSONA_ACCESSING_BUS_MEMBER =
		PResource.PERSONA_VOCABULARY_NAMESPACE + "theAccessingBusMember";
	
	/**
	 * The URI of a standard variable managed by the PERSONA middleware indicating
	 * the current human user as claimed by {@link #VAR_PERSONA_ACCESSING_BUS_MEMBER}.
	 */
	public static final String VAR_PERSONA_ACCESSING_HUMAN_USER =
		PResource.PERSONA_VOCABULARY_NAMESPACE + "theAccessingHumanUser";
	
	/**
	 * The URI of a standard variable managed by the PERSONA middleware indicating
	 * the profile of a service that is estimated to be appropriate for responding
	 * the current service request.
	 */
	public static final String VAR_PERSONA_SERVICE_TO_SELECT =
		PResource.PERSONA_VOCABULARY_NAMESPACE + "theServiceToSelect";
	
	public static final String PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX;
	
	private static Hashtable middlewareResources = new Hashtable();
	static {
		// init type mapping for middleware resources used in messages
		middlewareResources.put(ContextEvent.MY_URI, ContextEvent.class);
		middlewareResources.put(ContextEventPattern.MY_URI, ContextEventPattern.class);
		middlewareResources.put(AggregatingFilter.MY_URI, AggregatingFilter.class);
		middlewareResources.put(PropertyPath.TYPE_PROPERTY_PATH, PropertyPath.class);
		middlewareResources.put(ServiceCall.MY_URI, ServiceCall.class);
		middlewareResources.put(ServiceRequest.MY_URI, ServiceRequest.class);
		middlewareResources.put(ServiceRealization.MY_URI, ServiceRealization.class);
		middlewareResources.put(ServiceResponse.MY_URI, ServiceResponse.class);
		middlewareResources.put(ServiceProfile.MY_URI, ServiceProfile.class);
		middlewareResources.put(OutputEvent.MY_URI, OutputEvent.class);
		middlewareResources.put(OutputEventPattern.MY_URI, OutputEventPattern.class);
		middlewareResources.put(InputEvent.MY_URI, InputEvent.class);
		middlewareResources.put(Form.MY_URI, Form.class);
		middlewareResources.put(Label.MY_URI, Label.class);
		middlewareResources.put(ChoiceItem.MY_URI, ChoiceItem.class);
		middlewareResources.put(ChoiceList.MY_URI, ChoiceList.class);
		middlewareResources.put(Group.MY_URI, Group.class);
		middlewareResources.put(Submit.MY_URI, Submit.class);
		middlewareResources.put(SubdialogTrigger.MY_URI, SubdialogTrigger.class);
		middlewareResources.put(SimpleOutput.MY_URI, SimpleOutput.class);
		middlewareResources.put(MediaObject.MY_URI, MediaObject.class);
		middlewareResources.put(InputField.MY_URI, InputField.class);
		middlewareResources.put(TextArea.MY_URI, TextArea.class);
		middlewareResources.put(Select.MY_URI, Select.class);
		middlewareResources.put(Select1.MY_URI, Select1.class);
		middlewareResources.put(Range.MY_URI, Range.class);
		
		PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX = Activator.getMiddlewareProp(Activator.PERSONA_AAL_SPACE_ID) + "#";
	}
	
	public static boolean debugMode() {
		return "true".equals(Activator.getMiddlewareProp(Activator.PERSONA_IS_DEBUG_MODE));
	}
	
	public static boolean isCoordinatorInstance() {
		return "true".equals(Activator.getMiddlewareProp(Activator.PERSONA_IS_COORDINATING_PEER));
	}
	
	public static String getSpaceConfRoot() {
		return Activator.getMiddlewareProp(Activator.PERSONA_CONF_ROOT_DIR);
	}
	
	public static String extractPeerID(String busMemberURI) {
		if (busMemberURI == null
				||  !busMemberURI.startsWith(PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX))
			return null;
		int i = busMemberURI.lastIndexOf('_');
		if (i < PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length())
			return null;
		return busMemberURI.substring(PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX.length(), i);
	}
	
	public static PResource getResourceInstance(String classURI, String instanceURI) {
		if (classURI == null)
			return null;
		
		Class clz = (Class) middlewareResources.get(classURI);
		if (clz == null)
			return null;
		
		try {
			if (clz == ServiceCall.class  &&  ServiceCall.THIS_SERVICE_CALL.getURI().equals(instanceURI))
				return ServiceCall.THIS_SERVICE_CALL;
			if (PResource.isAnonymousURI(instanceURI))
				return (PResource) clz.newInstance();
			else
				return (PResource) clz.getConstructor(new Class[] {String.class})
						.newInstance(new Object[] {instanceURI});
		} catch (Exception e) {
			return null;
		}
	}
	
	public static PResource getAccessingComponentRef() {
		PResource result = new PResource();
		result.addType(ProcessParameter.TYPE_OWLS_VALUE_OF, true);
		result.setProperty(ProcessParameter.PROP_OWLS_VALUE_OF_THE_VAR,
				new PResource(VAR_PERSONA_ACCESSING_BUS_MEMBER));
		result.setProperty(ProcessParameter.PROP_OWLS_VALUE_FROM_PROCESS,
				ServiceCall.THIS_SERVICE_CALL);
		return result;
	}
	
	public static PResource getAccessingUserRef() {
		PResource result = new PResource();
		result.addType(ProcessParameter.TYPE_OWLS_VALUE_OF, true);
		result.setProperty(ProcessParameter.PROP_OWLS_VALUE_OF_THE_VAR,
				new PResource(VAR_PERSONA_ACCESSING_HUMAN_USER));
		result.setProperty(ProcessParameter.PROP_OWLS_VALUE_FROM_PROCESS,
				ServiceCall.THIS_SERVICE_CALL);
		return result;
	}
	
	public static PResource getCurrentDatetimeRef() {
		PResource result = new PResource();
		result.addType(ProcessParameter.TYPE_OWLS_VALUE_OF, true);
		result.setProperty(ProcessParameter.PROP_OWLS_VALUE_OF_THE_VAR,
				new PResource(VAR_PERSONA_CURRENT_DATETIME));
		result.setProperty(ProcessParameter.PROP_OWLS_VALUE_FROM_PROCESS,
				ServiceCall.THIS_SERVICE_CALL);
		return result;
	}
}
