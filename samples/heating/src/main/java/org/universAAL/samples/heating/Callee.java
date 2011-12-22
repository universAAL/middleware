package org.universAAL.samples.heating;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

public class Callee extends ServiceCallee {

	protected Callee(ModuleContext context) {
		super(context, ProvidedServiceTemp.profiles);
		// TODO Auto-generated constructor stub

		// With super we relate this call to our providedservicetemp
		// Here I register the services, the profiles.
	}

	public void communicationChannelBroken() {
		// TODO Auto-generated method stub

	}

	public ServiceResponse handleCall(ServiceCall call) {
		ServiceResponse response;
		if (call == null) {
			response = new ServiceResponse(CallStatus.serviceSpecificFailure);
			response.addOutput(new ProcessOutput(
					ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Null Call!"));
			return response;
		}

		String operation = call.getProcessURI();
		if (operation == null) {
			response = new ServiceResponse(CallStatus.serviceSpecificFailure);
			response.addOutput(new ProcessOutput(
					ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
					"Null Operation!"));
			return response;
		}

		// if the operation match with the service return a value.
		if (operation.startsWith(ProvidedServiceTemp.SERVICE_GET_VALUE)) {
			return getValue();
		} else {
			response = new ServiceResponse(CallStatus.serviceSpecificFailure);
			response.addOutput(new ProcessOutput(
					ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
					"Invlaid Operation!"));
			return response;
		}
	}

	private ServiceResponse getValue() {
		ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
		sr.addOutput(new ProcessOutput(ProvidedServiceTemp.OUTPUT_VALUE,
				new Float(80)));
		return sr;
	}

}