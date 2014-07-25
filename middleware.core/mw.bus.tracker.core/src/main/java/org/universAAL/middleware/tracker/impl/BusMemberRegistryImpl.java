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
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceBusFacade;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.util.IRegistryListener;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.middleware.ui.IUIBus;
import org.universAAL.middleware.ui.UIBusFacade;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIHandler;

public class BusMemberRegistryImpl implements IBusMemberRegistry {

    private List<IBusMemberRegistryListener> listeners;
    private List<IBusMemberRegistryListener> serviceBusListeners;
    private List<IBusMemberRegistryListener> contextBusListeners;
    private List<IBusMemberRegistryListener> uiBusListeners;

    private Map<String, BusMember> serviceBusMembers;
    private Map<String, BusMember> contextBusMembers;
    private Map<String, BusMember> uiBusMembers;

    private ModuleContext mc;
    private IRegistryListener serviceListener;
    private IRegistryListener contextListener;
    private IRegistryListener uiListener;

    public BusMemberRegistryImpl(ModuleContext mc) {
	this.mc = mc;
	listeners = new ArrayList<IBusMemberRegistryListener>();
	serviceBusListeners = new ArrayList<IBusMemberRegistryListener>();
	contextBusListeners = new ArrayList<IBusMemberRegistryListener>();
	uiBusListeners = new ArrayList<IBusMemberRegistryListener>();

	serviceBusMembers = new HashMap<String, BusMember>();
	contextBusMembers = new HashMap<String, BusMember>();
	uiBusMembers = new HashMap<String, BusMember>();

	addRegistryListeners();
    }

    public void addRegistryListeners() {

	if (serviceListener == null) {
	    addServiceBusListener();
	}

	if (contextListener == null) {
	    addContextListener();
	}

	if (uiListener == null) {
	    addUIListener();
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

    private void addServiceBusListener() {

	final ServiceBus serviceBus = ServiceBusFacade.fetchBus(mc);

	serviceListener = new IRegistryListener() {

	    public void busMemberRemoved(BusMember busMember) {
		if (busMember instanceof ServiceCallee) {
		    ServiceCallee callee = (ServiceCallee) busMember;
		    logInfo("%s",
			    "[BusMemberRemoved] ServiceCallee: "
				    + callee.getMyID());
		    removeBusMemberFromRegistry(callee.getMyID(), callee, BusType.Service);
		} else if (busMember instanceof ServiceCaller) {
		    ServiceCaller caller = (ServiceCaller) busMember;
		    logInfo("%s",
			    "[BusMemberRemoved] ServiceCaller: "
				    + caller.getMyID());
		    removeBusMemberFromRegistry(caller.getMyID(), caller, BusType.Service);
		}
	    }

	    public void busMemberAdded(BusMember busMember) {
		if (busMember instanceof ServiceCallee) {
		    ServiceCallee callee = (ServiceCallee) busMember;
		    logInfo("%s",
			    "[BusMemberAdded] ServiceCallee: "
				    + callee.getMyID());
		    addBusMemberToRegistry(callee.getMyID(), callee, BusType.Service);
		} else if (busMember instanceof ServiceCaller) {
		    ServiceCaller caller = (ServiceCaller) busMember;
		    logInfo("%s",
			    "[BusMemberAdded] ServiceCaller: "
				    + caller.getMyID());
		    addBusMemberToRegistry(caller.getMyID(), caller, BusType.Service);
		}
	    }

	    public void busCleared() {
		logInfo("%s", "[BusCleared] ServiceBus");
	    }
	};

	((AbstractBus) serviceBus).addRegistryListener(serviceListener);

    }

    private void addContextListener() {
	final ContextBus contextBus = ContextBusFacade.fetchBus(mc);

	contextListener = new IRegistryListener() {

	    public void busMemberRemoved(BusMember busMember) {
		if (busMember instanceof ContextPublisher) {
		    ContextPublisher publisher = (ContextPublisher) busMember;
		    logInfo("%s", "[BusMemberRemoved] ContextPublisher: "
			    + publisher.getMyID());
		    removeBusMemberFromRegistry(publisher.getMyID(), publisher, BusType.Context);
		} else if (busMember instanceof ContextSubscriber) {
		    ContextSubscriber subscriber = (ContextSubscriber) busMember;
		    logInfo("%s", "[BusMemberRemoved] ContextSubscriber: "
			    + subscriber.getMyID());
		    removeBusMemberFromRegistry(subscriber.getMyID(), subscriber, BusType.Context);
		}
	    }

	    public void busMemberAdded(BusMember busMember) {
		if (busMember instanceof ContextPublisher) {
		    ContextPublisher publisher = (ContextPublisher) busMember;
		    logInfo("%s", "[BusMemberAdded] ContextPublisher: "
			    + publisher.getMyID());
		    addBusMemberToRegistry(publisher.getMyID(), publisher, BusType.Context);
		} else if (busMember instanceof ContextSubscriber) {
		    ContextSubscriber subscriber = (ContextSubscriber) busMember;
		    logInfo("%s", "[BusMemberAdded] ContextSubscriber: "
			    + subscriber.getMyID());
		    addBusMemberToRegistry(subscriber.getMyID(), subscriber, BusType.Context);
		}
	    }

	    public void busCleared() {
		logInfo("%s", "[BusCleared] ContextBus");
	    }
	};

	((AbstractBus) contextBus).addRegistryListener(contextListener);
    }

    private void addUIListener() {
	final IUIBus uiBus = UIBusFacade.fetchBus(mc);

	uiListener = new IRegistryListener() {

	    public void busMemberRemoved(BusMember busMember) {
		if (busMember instanceof UICaller) {
		    UICaller caller = (UICaller) busMember;
		    logInfo("%s",
			    "[BusMemberRemoved] UICaller: " + caller.getMyID());
		    uiBusMembers.remove(caller.getMyID());
		    removeBusMemberFromRegistry(caller.getMyID(), caller, BusType.UI);
		} else if (busMember instanceof UIHandler) {
		    UIHandler handler = (UIHandler) busMember;
		    logInfo("%s",
			    "[BusMemberRemoved] UIHandler: "
				    + handler.getMyID());
		    removeBusMemberFromRegistry(handler.getMyID(), handler, BusType.UI);
		}
	    }

	    public void busMemberAdded(BusMember busMember) {
		if (busMember instanceof UICaller) {
		    UICaller caller = (UICaller) busMember;
		    logInfo("%s",
			    "[BusMemberAdded] UICaller: " + caller.getMyID());
		    addBusMemberToRegistry(caller.getMyID(), caller, BusType.UI);
		} else if (busMember instanceof UIHandler) {
		    UIHandler handler = (UIHandler) busMember;
		    logInfo("%s",
			    "[BusMemberAdded] UIHandler: " + handler.getMyID());
		    addBusMemberToRegistry(handler.getMyID(), handler, BusType.UI);
		}
	    }

	    public void busCleared() {
		logInfo("%s", "[BusCleared] IUIBus");
	    }
	};
	((AbstractBus) uiBus).addRegistryListener(uiListener);
    }

    private void removeBusMemberFromRegistry(String id,BusMember member, BusType type) {
	switch (type) {
	case Service:
	    serviceBusMembers.remove(id);
	    for (IBusMemberRegistryListener listener : serviceBusListeners) {
		listener.busMemberRemoved(member, BusType.Service);
	    }
	    break;
	case Context:
	    contextBusMembers.remove(id);
	    for (IBusMemberRegistryListener listener : contextBusListeners) {
		listener.busMemberRemoved(member, BusType.Context);
	    }
	    break;
	case UI:
	    uiBusMembers.remove(id);
	    for (IBusMemberRegistryListener listener : uiBusListeners) {
		listener.busMemberRemoved(member, BusType.UI);
	    }
	    break;
	}
	for (IBusMemberRegistryListener listener : listeners) {
	    listener.busMemberRemoved(member, type);
	}
    }
    
    private void addBusMemberToRegistry(String id, BusMember member,
	    BusType type) {
	switch (type) {
	case Service:
	    serviceBusMembers.put(id, member);
	    for (IBusMemberRegistryListener listener : serviceBusListeners) {
		listener.busMemberAdded(member, BusType.Service);
	    }
	    break;
	case Context:
	    contextBusMembers.put(id, member);
	    for (IBusMemberRegistryListener listener : contextBusListeners) {
		listener.busMemberAdded(member, BusType.Context);
	    }
	    break;
	case UI:
	    uiBusMembers.put(id, member);
	    for (IBusMemberRegistryListener listener : uiBusListeners) {
		listener.busMemberAdded(member, BusType.UI);
	    }
	    break;
	}
	for (IBusMemberRegistryListener listener : listeners) {
	    listener.busMemberAdded(member, type);
	}
    }

    public void addBusRegistryListener(IBusMemberRegistryListener listener,
	    BusType type, boolean notifyAboutPreviouslyRegisteredMembers) {
	switch (type) {
	case Service:
	    serviceBusListeners.add(listener);
	    if (notifyAboutPreviouslyRegisteredMembers) {
		for (BusMember member : serviceBusMembers.values()) {
		    listener.busMemberAdded(member, BusType.Service);
		}
	    }
	    break;
	case Context:
	    contextBusListeners.add(listener);
	    if (notifyAboutPreviouslyRegisteredMembers) {
		for (BusMember member : contextBusMembers.values()) {
		    listener.busMemberAdded(member, BusType.Context);
		}
	    }
	    break;
	case UI:
	    uiBusListeners.add(listener);
	    if (notifyAboutPreviouslyRegisteredMembers) {
		for (BusMember member : uiBusMembers.values()) {
		    listener.busMemberAdded(member, BusType.UI);
		}
	    }
	    break;
	}

    }

    public void addBusRegistryListener(IBusMemberRegistryListener listener,
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

    public void removeBusRegistryListener(IBusMemberRegistryListener listener) {
	listeners.remove(listener);
    }

    public void removeBusRegistryListener(IBusMemberRegistryListener listener,
	    BusType type) {
	switch (type) {
	case Service:
	    serviceBusListeners.remove(listener);
	    break;
	case Context:
	    contextBusListeners.remove(listener);
	    break;
	case UI:
	    uiBusListeners.remove(listener);
	    break;
	}
    }

    static void logInfo(String format, Object... args) {
	StackTraceElement callingMethod = Thread.currentThread()
		.getStackTrace()[2];
	LogUtils.logInfo(Activator.mc, BusMemberRegistryImpl.class,
		callingMethod.getMethodName(),
		new Object[] { String.format(format, args) }, null);
    }

}
