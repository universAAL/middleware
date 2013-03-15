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
package org.universAAL.middleware.service;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.Callee;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * This is an abstract class that the service callee members of the service bus
 * must derive from. According to the convention of the service bus regarding
 * the registration parameters the <li><code>ServiceCallee</code>-s may pass an
 * array of
 * {@link org.universAAL.middleware.service.owls.profile.ServiceProfile}s</li>
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 */
public abstract class ServiceCallee extends Callee {

    /**
     * The default constructor for this class.
     * 
     * @param context
     *            The OSGI bundle context where the ServiceBus is registered.
     *            Note that if no service bus is registered at the time of
     *            creation, this object will not be operational.
     * @param realizedServices
     *            The initial set of services that are realized by this callee.
     */
    protected ServiceCallee(ModuleContext context,
	    ServiceProfile[] realizedServices) {
	super(context, ServiceBusImpl.getServiceBusFetchParams());
	addNewServiceProfiles(realizedServices);
    }

    /**
     * Registers additional services to be provided by this
     * <code>ServiceCalee</code>.
     * 
     * @param realizedServices
     *            the new services.
     */
    protected final void addNewServiceProfiles(ServiceProfile[] realizedServices) {
	((ServiceBus) theBus).addNewServiceProfiles(busResourceURI,
		realizedServices);
    }

    /**
     * Removes a specified set of services that were previously provided by this
     * <code>ServiceCalee</code>.
     * 
     * @param realizedServices
     *            the services that need to be removed.
     */
    protected final void removeMatchingProfiles(
	    ServiceProfile[] realizedServices) {
	((ServiceBus) theBus).removeMatchingProfiles(busResourceURI,
		realizedServices);
    }

    /**
     * This abstract method is called for each member of the bus when the bus is
     * being stopped.
     */
    public abstract void communicationChannelBroken();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.sodapop.BusMember#busDyingOut(org.universAAL
     * .middleware.sodapop.Bus)
     */
    public final void busDyingOut(AbstractBus b) {
	if (b == theBus)
	    communicationChannelBroken();
    }

    /**
     * The actual service method of the <code>ServiceCallee</code>. It is called
     * by the bus whenever there is a call that need to be serviced by this
     * <code>ServiceCallee</code>.
     * 
     * @param call
     *            the call that needs to be serviced.
     * @return the result of the call execution.
     */
    public abstract ServiceResponse handleCall(ServiceCall call);

    /**
     * @see org.universAAL.middleware.bus.model.Callee#handleRequest(org.universAAL.middleware.bus.model.msg.Message)
     */
    public void handleRequest(BusMessage m) {
	if (m != null && m.getContent() instanceof ServiceCall) {
	    LogUtils.logInfo(owner, ServiceCallee.class, "handleRequest",
		    new Object[] { busResourceURI, " received service call:\n",
			    m.getContentAsString() }, null);
	    ServiceResponse sr = handleCall((ServiceCall) m.getContent());
	    if (sr == null)
		sr = new ServiceResponse(CallStatus.serviceSpecificFailure);
	    BusMessage reply = m.createReply(sr);
	    if (reply != null)
		((ServiceBus) theBus).brokerReply(busResourceURI, reply);
	}
    }

    /**
     * Unregisters this <code>ServiceCallee</code> from the bus.
     */
    public void close() {
	theBus.unregister(busResourceURI, this);
    }

    public String getMyID() {
	return busResourceURI;
    }
}
