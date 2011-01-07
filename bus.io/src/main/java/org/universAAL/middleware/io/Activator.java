/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut fuer Graphische Datenverarbeitung 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either.ss or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.io;

import java.util.Dictionary;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.middleware.input.InputBus;
import org.universAAL.middleware.input.impl.InputBusImpl;
import org.universAAL.middleware.output.OutputBus;
import org.universAAL.middleware.output.impl.OutputBusImpl;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;
import org.universAAL.middleware.util.LogUtils;
import org.universAAL.middleware.util.ResourceComparator;

public class Activator implements BundleActivator {

	private static BundleContext context = null;
	private static MessageContentSerializer contentSerializer = null;
	private static boolean inputBusStarted = false, outputBusStarted = false;
	public static final Logger logger = LoggerFactory
			.getLogger(Activator.class);

	public static synchronized void assessContentSerialization(Resource content) {
		if (org.universAAL.middleware.util.Constants.debugMode()) {
			if (contentSerializer == null) {
				ServiceReference sr = context
						.getServiceReference(MessageContentSerializer.class
								.getName());
				if (sr == null)
					return;

				contentSerializer = (MessageContentSerializer) context
						.getService(sr);
			}

			LogUtils.logDebug(logger, "Activator", "assessContentSerialization", new Object[] {"Assessing message content serialization:"}, null);

			String str = contentSerializer.serialize(content);
			LogUtils.logDebug(logger, "Activator", "assessContentSerialization", new Object[] {"\n      1. serialization dump\n", str, "\n      2. deserialize & compare with the original resource\n"}, null);
			new ResourceComparator().printDiffs(content,
					(Resource) contentSerializer.deserialize(str));
		}
	}

	static Dictionary middlewareProps;
	static ServiceRegistration registration;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		Activator.context = context;

		// load classes exported by the i/o buses
		Class.forName("org.universAAL.middleware.input.InputEvent");
		Class.forName("org.universAAL.middleware.io.owl.AccessImpairment");
		Class.forName("org.universAAL.middleware.io.owl.DialogType");
		Class.forName("org.universAAL.middleware.io.owl.Gender");
		Class.forName("org.universAAL.middleware.io.owl.Modality");
		Class.forName("org.universAAL.middleware.io.owl.PrivacyLevel");
		Class.forName("org.universAAL.middleware.io.rdf.ChoiceItem");
		Class.forName("org.universAAL.middleware.io.rdf.ChoiceList");
		Class.forName("org.universAAL.middleware.io.rdf.Form");
		Class.forName("org.universAAL.middleware.io.rdf.Group");
		Class.forName("org.universAAL.middleware.io.rdf.InputField");
		Class.forName("org.universAAL.middleware.io.rdf.Label");
		Class.forName("org.universAAL.middleware.io.rdf.MediaObject");
		Class.forName("org.universAAL.middleware.io.rdf.Range");
		Class.forName("org.universAAL.middleware.io.rdf.Repeat");
		Class.forName("org.universAAL.middleware.io.rdf.Select");
		Class.forName("org.universAAL.middleware.io.rdf.Select1");
		Class.forName("org.universAAL.middleware.io.rdf.SimpleOutput");
		Class.forName("org.universAAL.middleware.io.rdf.SubdialogTrigger");
		Class.forName("org.universAAL.middleware.io.rdf.Submit");
		Class.forName("org.universAAL.middleware.io.rdf.TextArea");
		Class.forName("org.universAAL.middleware.output.OutputEvent");
		Class.forName("org.universAAL.middleware.output.OutputEventPattern");
	}

	public static void checkInputBus() {
		synchronized (context) {
			if (!inputBusStarted) {
				SodaPop sodapop = (SodaPop) context.getService(context
						.getServiceReference(SodaPop.class.getName()));
				context.registerService(InputBus.class.getName(),
						new InputBusImpl(sodapop), null);
				inputBusStarted = true;
			}
		}
	}

	public static void checkOutputBus() {
		synchronized (context) {
			if (!outputBusStarted) {
				SodaPop sodapop = (SodaPop) context.getService(context
						.getServiceReference(SodaPop.class.getName()));
				context.registerService(OutputBus.class.getName(),
						new OutputBusImpl(sodapop), null);
				outputBusStarted = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}
}
