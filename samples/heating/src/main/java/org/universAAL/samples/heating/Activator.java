package org.universAAL.samples.heating;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.ontology.weather.TempSensor;

public class Activator implements BundleActivator {

	CSubscriber cs; // We create a subscriber

	ContextPublisher cp; // We create a publisher

	private ServiceCaller caller; // to call a service

	Callee servicecallee; // to be called (the service)

    static ModuleContext mc;

    public void start(final BundleContext context) throws Exception {
    	mc = uAALBundleContainer.THE_CONTAINER
    			.registerModule(new Object[] { context });
		// TODO Auto-generated method stub

		System.out.println("*****************************************");
		System.out.println("-------TEMPERATURE SENSOR------------");
		System.out.println("*****************************************");

		// THIS IS A PEDAGOGICAL EXAMPLE ABOUT A PERSONA BUNDLE CREATION
		// IT IS NOT A HEATING SERVICE AS THE TITLE SAYS

		// THIS BUNDLE HAVE 2 INDEPENDENT PARTS (THAT CAN BE CONSIDERED LIKE 2
		// DIFERENT BUNDLES)

		// **********************************************************
		// FIRST PART: ¿What we do in this part?
		// **********************************************************
		// 1 WE CREATE A SENSOR, A PUBLISHER, AND A SUBSCRIBER.
		// 2 THE SENSOR WILL PUBLISH A EVENT BY MEANS OF A
		// PUBLISHER AND THIS EVENT WILL BE COLLECTED BY A SUBCRIBER.
		// THE SUBSCRIBER CLASS IS: CSubscriber.java (inside
		// org.universAAL.samples.heating)

		// BUS CONTEXT

		System.out
				.println(" I CREATE A SUBSCRIBER TO RECEIVE TEMPERATURES FROM SENSORS");

		cs = new CSubscriber(mc,
				new ContextEventPattern[] { new ContextEventPattern() });

		// I create a context publisher(cp) that will publish temperatures

		// In order to define the context publisher ontology I create a context
		// provider
		ContextProvider cpinfo = new ContextProvider(
				"http://ontology.tsbtecnologias.es/Test.owl#TestContextProvider");
		cpinfo.setType(ContextProviderType.gauge);
		// context publisher = (context provider, bundle context)
		cp = new DefaultContextPublisher(mc, cpinfo);

		// I create a sensor to measure the temperature inside the house.
		System.out.println("I CREATE A TEMPERATURE SENSOR");
		TempSensor ts = new TempSensor();
		ts.setMeasuredValue(38); // I fix the temperature to 38 degrees because
		// we are not goingo to measure the
		// temperature for real.

		// I publish the actual temperature so the subscriber got something to
		// catch

		System.out
				.println("I CREATE 3 EVENTS TO PUBLISH THE ACTUAL TEMPERATURE");

		ContextEvent ev1 = new ContextEvent(ts, TempSensor.PROP_MEASURED_VALUE);
		ContextEvent ev2 = new ContextEvent(ts, TempSensor.PROP_MEASURED_VALUE);
		ContextEvent ev3 = new ContextEvent(ts, TempSensor.PROP_MEASURED_VALUE);

		System.out.println("I PUBLISH EVENTS");
		cp.publish(ev1);
		cp.publish(ev2);
		cp.publish(ev3);

		// //////////////////////////////////////////////////////////////////////////////
		// **********************************************************
		// SECOND PART: ¿What we do in this part?
		// **********************************************************
		// 1 CREATES A SERVICE ONTOLOGY (CLASS OF SERVICE) IN ServiceDevice.java
		// 2 CREATES A SERVICE (A INSTANCE OF THE SERVICE ONTOLOGY)IN
		// ProvidedServiceTemp.java
		// 3 A CALLEE, THAT IS, A CLASS THAT PERMITS THE SERVICE TO BE CALLED
		// 4 THIS ACTIVATOR (below) CALLS A SERVICE(BY MEANS OF A CALLER) AND
		// PROCESS THE ANSWER OF THIS SERVICE (SERVICE RESPONSE).

		// SERVICE BUS

		servicecallee = new Callee(mc);

		// I create a default caller and i pass him the actual context
		caller = new DefaultServiceCaller(mc);

		// I create a object service response

		ServiceResponse sr;

		// I create a object service request

		ServiceRequest req = new ServiceRequest(new ServiceDevice(null), null);

		// I configure the request for the call.
		req.addTypeFilter(new String[] { ServiceDevice.PROPERTY_CONTROLS },
				TempSensor.MY_URI);

		// output_temp id of the uri.

		req.addRequiredOutput(ProvidedServiceTemp.SERVER_NAMESPACE
				+ "output_temp",
				new String[] { ServiceDevice.PROPERTY_CONTROLS,
						TempSensor.PROP_MEASURED_VALUE });

		// I call the service

		sr = caller.call(req);

		// answer process
		// what I do is to break down the "answer" response, process it.

		if (sr.getCallStatus() == CallStatus.succeeded) {
			System.out
					.println(sr.getOutput(ProvidedServiceTemp.SERVER_NAMESPACE
							+ "output_temp", true));
		} else {
			System.out.println("error" + sr.getCallStatus());
		}

	}

	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
