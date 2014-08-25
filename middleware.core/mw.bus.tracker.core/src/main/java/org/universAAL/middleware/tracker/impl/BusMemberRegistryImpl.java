/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

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
package org.universAAL.middleware.tracker.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.ContextBusFacade;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceBusFacade;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.util.IRegistryListener;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.middleware.ui.IUIBus;
import org.universAAL.middleware.ui.UIBusFacade;

//TODO: synchronize map and listener for concurrent access
public class BusMemberRegistryImpl implements IBusMemberRegistry {

    /**
     * The list of listeners that we have to notify about changes.
     */
    private List<IBusMemberRegistryListener> listeners;

    private Map<String, BusMember> serviceBusMembers;
    private Map<String, BusMember> contextBusMembers;
    private Map<String, BusMember> uiBusMembers;

    private ModuleContext mc;

    /**
     * The listener that we implement to get notified about changes in the
     * service bus.
     */
    private IRegistryListener serviceListener;

    /**
     * The listener that we implement to get notified about changes in the
     * context bus.
     */
    private IRegistryListener contextListener;

    /**
     * The listener that we implement to get notified about changes in the UI
     * bus.
     */
    private IRegistryListener uiListener;

    private class TypedRegistryListener implements IRegistryListener {
	BusType type;

	TypedRegistryListener(BusType type) {
	    this.type = type;
	}

	private void log(String action, BusMember busMember) {
	    log(action, busMember.getClass().getSimpleName(),
		    busMember.getURI());
	}

	private void log(String action, String busMemberClass,
		String busMemberURI) {
	    logInfo("%s", "[BusMember" + action + "] " + busMemberClass + ": "
		    + busMemberURI);
	}

	public void busMemberAdded(BusMember busMember) {
	    log("Added", busMember);
	    addBusMemberToRegistry(busMember.getURI(), busMember, type);
	    for (IBusMemberRegistryListener listener : listeners) {
		listener.busMemberAdded(busMember, type);
	    }
	}

	public void busMemberRemoved(BusMember busMember) {
	    log("Removed", busMember);
	    removeBusMemberFromRegistry(busMember.getURI(), busMember, type);
	    for (IBusMemberRegistryListener listener : listeners) {
		listener.busMemberRemoved(busMember, type);
	    }
	}

	public void busCleared() {
	    Map<String, BusMember> busMembers = null;
	    switch (type) {
	    case Service:
		busMembers = serviceBusMembers;
		break;
	    case Context:
		busMembers = contextBusMembers;
		break;
	    case UI:
		busMembers = uiBusMembers;
		break;
	    }
	    if (busMembers == null)
		return;
	    for (BusMember member : busMembers.values()) {
		for (IBusMemberRegistryListener listener : listeners) {
		    listener.busMemberRemoved(member, type);
		}
	    }
	    busMembers.clear();
	}

	public void regParamsAdded(String busMemberID, Resource[] params) {
	    log("regParamsAdded", "", busMemberID);
	    for (IBusMemberRegistryListener listener : listeners) {
		listener.regParamsAdded(busMemberID, params);
	    }
	}

	public void regParamsRemoved(String busMemberID, Resource[] params) {
	    log("regParamsRemoved", "", busMemberID);
	    for (IBusMemberRegistryListener listener : listeners) {
		listener.regParamsRemoved(busMemberID, params);
	    }
	}
    }

    public BusMemberRegistryImpl(ModuleContext mc) {
	this.mc = mc;
	listeners = new ArrayList<IBusMemberRegistryListener>();

	serviceBusMembers = new HashMap<String, BusMember>();
	contextBusMembers = new HashMap<String, BusMember>();
	uiBusMembers = new HashMap<String, BusMember>();

	if (serviceListener == null) {
	    addServiceBusListener();
	}
	if (contextListener == null) {
	    addContextBusListener();
	}
	if (uiListener == null) {
	    addUIBusListener();
	}
    }

    public void removeRegistryListeners() {
	if (serviceListener != null) {
	    ServiceBus serviceBus = ServiceBusFacade.fetchBus(mc);
	    ((AbstractBus) serviceBus).removeRegistryListener(serviceListener);
	}

	if (contextListener != null) {
	    ContextBus serviceBus = ContextBusFacade.fetchBus(mc);
	    ((AbstractBus) serviceBus).removeRegistryListener(contextListener);
	}

	if (uiListener != null) {
	    IUIBus uiBus = UIBusFacade.fetchBus(mc);
	    ((AbstractBus) uiBus).removeRegistryListener(uiListener);
	}
    }

    private IRegistryListener createBusListener(AbstractBus bus, BusType type) {
	IRegistryListener listener = new TypedRegistryListener(type);
	bus.addRegistryListener(listener);
	return listener;
    }

    private void addServiceBusListener() {
	final ServiceBus serviceBus = ServiceBusFacade.fetchBus(mc);
	serviceListener = createBusListener((AbstractBus) serviceBus,
		BusType.Service);
    }

    private void addContextBusListener() {
	final ContextBus contextBus = ContextBusFacade.fetchBus(mc);
	contextListener = createBusListener((AbstractBus) contextBus,
		BusType.Context);
    }

    private void addUIBusListener() {
	final IUIBus uiBus = UIBusFacade.fetchBus(mc);
	uiListener = createBusListener((AbstractBus) uiBus, BusType.UI);
    }

    private void removeBusMemberFromRegistry(String id, BusMember member,
	    BusType type) {
	switch (type) {
	case Service:
	    serviceBusMembers.remove(id);
	    break;
	case Context:
	    contextBusMembers.remove(id);
	    break;
	case UI:
	    uiBusMembers.remove(id);
	    break;
	}
    }

    private void addBusMemberToRegistry(String id, BusMember member,
	    BusType type) {
	switch (type) {
	case Service:
	    serviceBusMembers.put(id, member);
	    break;
	case Context:
	    contextBusMembers.put(id, member);
	    break;
	case UI:
	    uiBusMembers.put(id, member);
	    break;
	}
    }

    public void addListener(IBusMemberRegistryListener listener,
	    boolean notifyAboutPreviouslyRegisteredMembers) {
	listeners.add(listener);
	if (notifyAboutPreviouslyRegisteredMembers) {
	    for (BusMember member : serviceBusMembers.values()) {
		listener.busMemberAdded(member, BusType.Service);
	    }
	    for (BusMember member : contextBusMembers.values()) {
		listener.busMemberAdded(member, BusType.Context);
	    }
	    for (BusMember member : uiBusMembers.values()) {
		listener.busMemberAdded(member, BusType.UI);
	    }
	}
    }

    public void removeListener(IBusMemberRegistryListener listener) {
	listeners.remove(listener);
    }

    void logInfo(String format, Object... args) {
	StackTraceElement callingMethod = Thread.currentThread()
		.getStackTrace()[2];
	LogUtils.logInfo(mc, BusMemberRegistryImpl.class,
		callingMethod.getMethodName(),
		new Object[] { String.format(format, args) }, null);
    }
}
