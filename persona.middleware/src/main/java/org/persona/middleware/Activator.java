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

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.persona.middleware.context.ContextBus;
import org.persona.middleware.context.impl.ContextBusImpl;
import org.persona.middleware.input.InputBus;
import org.persona.middleware.input.impl.InputBusImpl;
import org.persona.middleware.output.OutputBus;
import org.persona.middleware.output.impl.OutputBusImpl;
import org.persona.middleware.service.ServiceBus;
import org.persona.middleware.service.impl.ServiceBusImpl;
import org.persona.middleware.util.ResourceComparator;
import org.persona.ontology.DialogType;
import org.persona.ontology.Gender;
import org.persona.ontology.InitialServiceDialog;
import org.persona.ontology.LevelRating;
import org.persona.ontology.Modality;
import org.persona.ontology.PrivacyLevel;
import org.persona.ontology.Rating;
import org.persona.ontology.context.ContextProvider;
import org.persona.ontology.expr.Complement;
import org.persona.ontology.expr.Enumeration;
import org.persona.ontology.expr.Intersection;
import org.persona.ontology.expr.OrderingRestriction;
import org.persona.ontology.expr.TypeURI;
import org.persona.ontology.expr.Union;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.igd.ima.sodapop.SodaPop;
import de.fhg.igd.ima.sodapop.msg.MessageContentSerializer;

public class Activator implements BundleActivator, ManagedService {
	// configuration parameters as property keys
	static final String PERSONA_AAL_SPACE_ID = "org.persona.middleware.peer.member_of";
	static final String PERSONA_CONF_ROOT_DIR = "bundles.configuration.location";
	static final String PERSONA_IS_COORDINATING_PEER = "org.persona.middleware.peer.is_coordinator";
	static final String PERSONA_IS_DEBUG_MODE = "org.persona.middleware.debugMode";
	
	private static BundleContext context = null;
	private static MessageContentSerializer contentSerializer = null;
	private static boolean contextBusStarted = false, serviceBusStarted = false,
			inputBusStarted = false, outputBusStarted = false;
	public static final Logger logger = LoggerFactory.getLogger(Activator.class);
	
	public static synchronized void assessContentSerialization(PResource content) {
		if ("true".equals(getMiddlewareProp(PERSONA_IS_DEBUG_MODE))) {
			if (contentSerializer == null) {
				ServiceReference sr = context.getServiceReference(MessageContentSerializer.class.getName());
				if (sr == null)
					return;
				
				contentSerializer = (MessageContentSerializer) context.getService(sr);
			}

			System.out.println("Assessing message content serialization; 1. serialization dump, 2. deserialize & compare with the original resource:");
			// System.out.println(new RuntimeException().getStackTrace()[1]);
			
			String str = contentSerializer.serialize(content);
			System.out.println(str);
			new ResourceComparator().printDiffs(content, (PResource) contentSerializer.deserialize(str));
		}
	}

	static Dictionary middlewareProps;
	static ServiceRegistration registration;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Activator.context = context;

		// load ontology classes exported by the middleware
		Class.forName(Complement.class.getName());
		Class.forName(ContextProvider.class.getName());
		Class.forName(DialogType.class.getName());
		Class.forName(Enumeration.class.getName());
		Class.forName(Gender.class.getName());
		Class.forName(InitialServiceDialog.class.getName());
		Class.forName(Intersection.class.getName());
		Class.forName(LevelRating.class.getName());
		Class.forName(Modality.class.getName());
		Class.forName(OrderingRestriction.class.getName());
		Class.forName(PrivacyLevel.class.getName());
		Class.forName(Rating.class.getName());
		Class.forName(TypeURI.class.getName());
		Class.forName(Union.class.getName());
		
		setDefaults();
		
		Dictionary props = new Hashtable(1);
		props.put(Constants.SERVICE_PID, "org.aal-persona.middleware.upper");
		registration = context.registerService(ManagedService.class.getName(), this, props);
		
		context.registerService(TypeMapper.class.getName(), TypeMapper.getTypeMapper(), null);
	}
	
	private void setDefaults() {
		middlewareProps = new Hashtable(4);
		middlewareProps.put(PERSONA_AAL_SPACE_ID,
				System.getProperty(PERSONA_AAL_SPACE_ID, "urn:org.persona.aal_space:test_environment"));
		middlewareProps.put(PERSONA_IS_COORDINATING_PEER,
				System.getProperty(PERSONA_IS_COORDINATING_PEER, "true"));
		middlewareProps.put(PERSONA_CONF_ROOT_DIR,
				System.getProperty(PERSONA_CONF_ROOT_DIR, System.getProperty("user.dir")));
		if ("true".equals(System.getProperty(PERSONA_IS_DEBUG_MODE)))
			middlewareProps.put(PERSONA_IS_DEBUG_MODE, "true");
	}
	
	static String getMiddlewareProp(String key) {
		return (key == null  ||  middlewareProps == null)? null : (String) middlewareProps.get(key);
	}
	
	public static void checkContextBus() {
		synchronized (context) {
			if (!contextBusStarted) {
				SodaPop sodapop = (SodaPop) context.getService(
						context.getServiceReference(SodaPop.class.getName()));
				context.registerService(ContextBus.class.getName(),
						new ContextBusImpl(sodapop), null);
				contextBusStarted = true;
			}
		}
	}
	
	public static void checkInputBus() {
		synchronized (context) {
			if (!inputBusStarted) {
				SodaPop sodapop = (SodaPop) context.getService(
						context.getServiceReference(SodaPop.class.getName()));
				context.registerService(InputBus.class.getName(),
						new InputBusImpl(sodapop), null);
				inputBusStarted = true;
			}
		}
	}
	
	public static void checkOutputBus() {
		synchronized (context) {
			if (!outputBusStarted) {
				SodaPop sodapop = (SodaPop) context.getService(
						context.getServiceReference(SodaPop.class.getName()));
				context.registerService(OutputBus.class.getName(),
						new OutputBusImpl(sodapop), null);
				outputBusStarted = true;
			}
		}
	}
	
	public static void checkServiceBus() {
		synchronized (context) {
			if (!serviceBusStarted) {
				SodaPop sodapop = (SodaPop) context.getService(
						context.getServiceReference(SodaPop.class.getName()));
				context.registerService(ServiceBus.class.getName(),
						new ServiceBusImpl(sodapop), null);
				serviceBusStarted = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

	public synchronized void updated(Dictionary properties) throws ConfigurationException {
		if (properties == null)
			setDefaults();
		else {
			Object val = properties.remove(PERSONA_AAL_SPACE_ID);
			if (val instanceof String)
				middlewareProps.put(PERSONA_AAL_SPACE_ID, val);

			val = properties.remove(PERSONA_CONF_ROOT_DIR);
			if (val instanceof String)
				middlewareProps.put(PERSONA_CONF_ROOT_DIR, val);

			val = properties.remove(PERSONA_IS_COORDINATING_PEER);
			if (val instanceof String)
				middlewareProps.put(PERSONA_IS_COORDINATING_PEER, val);

			val = properties.remove(PERSONA_IS_DEBUG_MODE);
			if (val instanceof String)
				middlewareProps.put(PERSONA_IS_DEBUG_MODE, val);
			
			// according to the documentation of ManagedService: "As a convention, it is
			// recommended that when a Managed Service is updated, it should copy all the
			// properties it does not recognize into the service registration properties.
			// This will allow the Configuration Admin service to set properties on
			// services which can then be used by other applications."
			registration.setProperties(properties);
		}
	}
}
