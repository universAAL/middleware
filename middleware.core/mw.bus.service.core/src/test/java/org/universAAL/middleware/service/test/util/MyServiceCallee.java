package org.universAAL.middleware.service.test.util;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

public class MyServiceCallee extends ServiceCallee {
    private CallHandler handler = null;
    List<ServiceProfile[]> profiles = new ArrayList<ServiceProfile[]>();
    int node;
    int callee;

    protected MyServiceCallee(ModuleContext context,
	    ServiceProfile[] realizedServices, int node, int callee) {
	super(context, realizedServices);
	profiles.add(realizedServices);
	this.node = node;
	this.callee = callee;
    }

    @Override
    public void communicationChannelBroken() {
    }

    public void setHandler(CallHandler handler) {
	this.handler = handler;
    }

    @Override
    public ServiceResponse handleCall(ServiceCall call) {
	System.out.println(" -- Handler called: Node " + node + " Callee "
		+ callee);
	CallHandler handler = this.handler;
	if (handler != null)
	    return handler.handleCall(call);

	return null;
    }

    public void addProfiles(ServiceProfile[] p) {
	profiles.add(p);
	addNewServiceProfiles(p);

	System.out.println(" -- added new profile for Node " + node
		+ " Callee " + callee);
    }

    public void reset() {
	for (ServiceProfile[] p : profiles) {
	    removeMatchingProfiles(p);
	}
	profiles.clear();
    }
}
