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
package org.universAAL.middleware.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.member.Callee;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.service.impl.ServiceRealization;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * This is an abstract class that the service callee members of the service bus
 * must derive from. According to the convention of the service bus regarding
 * the registration parameters the
 * <li><code>ServiceCallee</code>-s may pass an array of
 * {@link org.universAAL.middleware.service.owls.profile.ServiceProfile}s</li>
 *
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
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
	 * @throws NullPointerException
	 *             if realizedServices is null or one of the elements of that
	 *             array is null
	 */
	protected ServiceCallee(ModuleContext context, ServiceProfile[] realizedServices) {
		super(context, ServiceBusImpl.getServiceBusFetchParams());
		realizedServices = AccessControl.INSTANCE.checkPermission(owner, getURI(), realizedServices);
		addNewServiceProfiles(realizedServices);
	}

	/**
	 * The default constructor for this class.
	 *
	 * @param context
	 *            The OSGI bundle context where the ServiceBus is registered.
	 *            Note that if no service bus is registered at the time of
	 *            creation, this object will not be operational.
	 * @param realizedServices
	 *            The initial set of services that are realized by this callee.
	 * @param throwOnError
	 *            Determines if an Exception should be thrown in case of an
	 *            error. In that case, none of the profiles will be registered.
	 * @throws NullPointerException
	 *             if realizedServices is null or one of the elements of that
	 *             array is null
	 * @throws ProfileExistsException
	 *             if one of the profiles exists already.
	 * @throws SecurityException
	 *             if one of the profiles is not allowed to be registered by
	 *             this module.
	 */
	protected ServiceCallee(ModuleContext context, ServiceProfile[] realizedServices, boolean throwOnError) {
		super(context, ServiceBusImpl.getServiceBusFetchParams());
		realizedServices = AccessControl.INSTANCE.checkPermission(owner, getURI(), realizedServices);
		addNewServiceProfiles(realizedServices, throwOnError);
	}

	/**
	 * Registers additional services to be provided by this
	 * <code>ServiceCallee</code>.
	 *
	 * @param realizedServices
	 *            the new services.
	 * @throws NullPointerException
	 *             if realizedServices is null or one of the elements of that
	 *             array is null
	 */
	protected final void addNewServiceProfiles(ServiceProfile[] realizedServices) {
		try {
			addNewServiceProfiles(realizedServices, false);
		} catch (ProfileExistsException e) {
		}
	}

	/**
	 * Registers additional services to be provided by this
	 * <code>ServiceCallee</code>.
	 *
	 * @param realizedServices
	 *            the new services.
	 * @param throwOnError
	 *            Determines if an Exception should be thrown in case of an
	 *            error. In that case, none of the profiles will be registered.
	 * @throws NullPointerException
	 *             if realizedServices is null or one of the elements of that
	 *             array is null
	 * @throws ProfileExistsException
	 *             if one of the profiles exists already.
	 * @throws SecurityException
	 *             if one of the profiles is not allowed to be registered by
	 *             this module.
	 */
	protected final void addNewServiceProfiles(ServiceProfile[] realizedServices, boolean throwOnError) {
		ServiceProfile[] filteredServices = AccessControl.INSTANCE.checkPermission(owner, getURI(), realizedServices);
		if (throwOnError && filteredServices.length != realizedServices.length)
			throw new SecurityException("A profile is not allowed to be registered by this module.");
		((ServiceBus) theBus).addNewServiceProfiles(busResourceURI, filteredServices, throwOnError);
	}

	/**
	 * Removes a specified set of services that were previously provided by this
	 * <code>ServiceCallee</code>.
	 *
	 * @param realizedServices
	 *            the services that need to be removed.
	 */
	protected final void removeMatchingProfiles(ServiceProfile[] realizedServices) {
		((ServiceBus) theBus).removeMatchingProfiles(busResourceURI, realizedServices);
	}

	/**
	 * This abstract method is called for each member of the bus when the bus is
	 * being stopped.
	 */
	public abstract void communicationChannelBroken();

	/**
	 * @see BusMember#busDyingOut(AbstractBus)
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
	 * Handles a peer-to-peer request representing a {@link ServiceCall} coming
	 * from the bus.
	 *
	 * @param m
	 *            request message coming from the bus.
	 */
	public void handleRequest(BusMessage m) {
		if (m != null && m.getContent() instanceof ServiceCall) {
			ServiceCall sc = (ServiceCall) m.getContent();
			LogUtils.logInfo(owner, ServiceCallee.class, "handleRequest",
					new Object[] { busResourceURI, " received service call:\n", sc.toString() }, null);
			ServiceResponse sr = null;
			try {
				sr = handleCall(sc);
				LogUtils.logInfo(owner, ServiceCallee.class, "handleRequest",
						new Object[] { sc.getProcessURI(), " of ", busResourceURI, " returned:\n", sr.toString() }, null);
			} catch (Exception e) {
				sr = new ServiceResponse(CallStatus.serviceSpecificFailure);

				// get throwable as string
				Writer result = new StringWriter();
				PrintWriter printWriter = new PrintWriter(result);
				e.printStackTrace(printWriter);
				String ex = result.toString();

				sr.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, ex));

				LogUtils.logDebug(owner, ServiceCallee.class, "handleRequest",
						new Object[] { "An error occurred while executing the service callee." }, e);
			}
			if (sr == null)
				sr = new ServiceResponse(CallStatus.serviceSpecificFailure);
			sr.setProperty(ServiceRealization.SERVICE_PROVIDER, new Resource(busResourceURI));
			BusMessage reply = m.createReply(sr);
			if (reply != null)
				((ServiceBus) theBus).brokerReply(busResourceURI, reply);
		}
	}

	/**
	 * Get the ID of this bus member.
	 *
	 * @return the ID of this bus member.
	 */
	public String getMyID() {
		return busResourceURI;
	}
}
