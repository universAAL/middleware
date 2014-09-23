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
package org.universAAL.middleware.container.osgi.run;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;

public class uAALBundleExtender implements SynchronousBundleListener {

    private BundleContext bc;

    private Map bundleIdToActivatorExecutor = new HashMap();

    private String getClassNameFromEntry(String entryStr) {
	String[] entryParsed = entryStr.split("//");
	if (entryParsed.length == 2) {
	    String entryPath = entryParsed[1];
	    int index = entryPath.indexOf("/");
	    if (index >= 0) {
		String realEntryPath = entryPath.substring(index + 1);
		realEntryPath = realEntryPath.substring(0,
			realEntryPath.length() - 6);
		String className = realEntryPath.replace('/', '.');
		return className;
	    }
	}
	return null;
    }

    public uAALBundleExtender(BundleContext bc) {
	this.bc = bc;
    }

    private Class loadActivatorClass(Bundle bundle, String className)
	    throws Exception {
	if (className != null) {
	    Class c = bundle.loadClass(className);
	    Class[] ifaces = c.getInterfaces();
	    // boolean shouldLoad = false;
	    for (int i = 0; i < ifaces.length; i++) {
		if (ifaces[i] == ModuleActivator.class) {
		    return c;
		}
	    }
	}
	return null;
    }

    private String buildErrorMsg(ArrayList activators, Bundle bundle)
	    throws Exception {
	String newLine = System.getProperty("line.separator");
	StringBuilder msgBuilder = new StringBuilder();
	msgBuilder.append("Bundle ");
	msgBuilder.append(bundle.getSymbolicName());
	msgBuilder.append(" is ruined!! It has more than one ModuleActivator:");
	msgBuilder.append(newLine);
	for (int i = 0; i < activators.size(); i++) {
	    msgBuilder.append(" - ");
	    Class c = (Class) activators.get(i);
	    msgBuilder.append(c.getName());
	    msgBuilder.append(newLine);
	}
	return msgBuilder.toString();
    }

    private void doStart(final Bundle bundle) throws Exception {
	try {
	    Enumeration entries = bundle.findEntries("", "*Activator*.class",
		    true);
	    if (entries != null) {
		ArrayList activatorsClasses = new ArrayList();
		while (entries.hasMoreElements()) {
		    URL entryUrl = (URL) entries.nextElement();
		    Class activatorClass = loadActivatorClass(bundle,
			    getClassNameFromEntry(entryUrl.toString()));
		    if (activatorClass != null) {
			activatorsClasses.add(activatorClass);
		    }
		}
		if (!activatorsClasses.isEmpty()) {
		    ActivatorExecutor executor = null;
		    if (activatorsClasses.size() == 1) {
			Class c = (Class) activatorsClasses.get(0);
			ModuleActivator theActivator = (ModuleActivator) c
				.newInstance();
			executor = new ActivatorExecutor(theActivator, bundle);
		    } else {
			String errorMsg = buildErrorMsg(activatorsClasses,
				bundle);
			executor = new ActivatorExecutor(errorMsg, bundle);
		    }
		    executor.start();
		    bundleIdToActivatorExecutor.put(
			    new Long(bundle.getBundleId()), executor);
		}
	    }
	} catch (Exception t) {
	    new Thread(new Runnable() {
		public void run() {
		    try {
			bundle.stop();
		    } catch (Exception ex) {
			ex.printStackTrace();
		    }
		}
	    }).start();
	    throw t;
	}

    }

    private void doStop(Bundle bundle) throws Exception {
	if (bundle.getBundleId() != 0) {
	    ActivatorExecutor executor = (ActivatorExecutor) bundleIdToActivatorExecutor
		    .get(new Long(bundle.getBundleId()));
	    if (executor != null) {
		executor.stop();
		bundleIdToActivatorExecutor.remove(new Long(bundle
			.getBundleId()));
	    }
	}
    }

    public void bundleChanged(BundleEvent event) {
	Bundle bundle = event.getBundle();
	// ignore current bundle for context creation
	if (bundle.getBundleId() == bc.getBundle().getBundleId()) {
	    return;
	}
	try {
	    switch (event.getType()) {
	    case BundleEvent.STARTED: {
		doStart(bundle);
		break;
	    }
	    case BundleEvent.STOPPING: {
		doStop(bundle);
		break;
	    }
	    }
	} catch (Throwable t) {
	    t.printStackTrace();
	    throw new RuntimeException(t);
	}
    }

    private class ActivatorExecutor {
	private ModuleActivator activator;
	private ModuleContext mc;
	private Bundle b;
	private String errorMsg;
	private Logger logger;

	public ActivatorExecutor(ModuleActivator activator, Bundle b) {
	    this.activator = activator;
	    this.b = b;
	    this.mc = uAALBundleContainer.THE_CONTAINER
		    .registerModule(new Object[] { b.getBundleContext() });
	}

	public ActivatorExecutor(String errorMsg, Bundle b) {
	    this.errorMsg = errorMsg;
	    this.b = b;
	    this.logger = LoggerFactory.getLogger("org.universAAL."
		    + b.getSymbolicName());
	}

	private boolean logError() {
	    if (errorMsg != null) {
		logger.error(
			errorMsg,
			new IllegalStateException("Bundle "
				+ b.getSymbolicName()
				+ " has more than one ModuleActivator"));
		return true;
	    }
	    return false;
	}

	public void start() throws Exception {
	    if (!logError()) {
		activator.start(mc);
	    } else {
		new Thread(new Runnable() {
		    public void run() {
			try {
			    b.stop();
			} catch (Exception ex) {
			    ex.printStackTrace();
			}
		    }
		}).start();
	    }
	}

	public void stop() throws Exception {
	    if (errorMsg == null) {
		activator.stop(mc);
	    }
	}
    }
}
