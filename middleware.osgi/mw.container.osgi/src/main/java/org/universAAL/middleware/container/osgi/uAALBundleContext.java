/*
	Copyright 2011-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.container.osgi;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;

/**
 * An implementation of the concept of {@link ModuleContext} for OSGi.
 * 
 * @author mtazari
 * 
 */
public class uAALBundleContext implements ModuleContext {
    private BundleContext bundle;
    private Hashtable extension = new Hashtable();
    private Logger logger;
    private static BundleConfigHome servicesConfHome = new BundleConfigHome(
	    "services");
    private ArrayList confFiles = new ArrayList(2);

    uAALBundleContext(BundleContext bc) {
	bundle = bc;
	logger = LoggerFactory.getLogger("org.universAAL."
		+ bc.getBundle().getSymbolicName());
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#canBeStarted(org.universAAL.middleware.container.ModuleContext)
     */
    public boolean canBeStarted(ModuleContext requester) {
	// TODO check permissions
	return bundle.getBundle().getState() == Bundle.RESOLVED;
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#canBeStopped(org.universAAL.middleware.container.ModuleContext)
     */
    public boolean canBeStopped(ModuleContext requester) {
	// TODO check permissions
	return bundle.getBundle().getState() == Bundle.ACTIVE;
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#canBeUninstalled(org.universAAL.middleware.container.ModuleContext)
     */
    public boolean canBeUninstalled(ModuleContext requester) {
	// TODO check permissions
	int state = bundle.getBundle().getState();
	return state == Bundle.RESOLVED || state == Bundle.INSTALLED;
    }

    public Object fetchObject(String className) {
	ServiceReference sr = bundle.getServiceReference(className);
	return (sr == null) ? null : bundle.getService(sr);
    }

    public Object[] fetchObject(String className, String filter) {
	ServiceReference[] srs = null;
	try {
	    srs = bundle.getServiceReferences(className, filter);
	} catch (Exception e) {
	}
	if (srs == null || srs.length == 0)
	    return null;
	else {
	    Object[] result = new Object[srs.length];
	    for (int i = 0; i < srs.length; i++)
		result[i] = bundle.getService(srs[i]);
	    return result;
	}
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#getAttribute(java.lang.String)
     */
    public Object getAttribute(String attrName) {
	return (attrName == null) ? null : extension.get(attrName);
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#getContainer()
     */
    public Container getContainer() {
	return uAALBundleContainer.THE_CONTAINER;
    }

    public String getID() {
	return bundle.getBundle().getSymbolicName();
    }

    public uAALBundleContext installBundle(String location) {
	try {
	    Bundle b = bundle.installBundle(location);
	    return new uAALBundleContext(b.getBundleContext());
	} catch (Exception e) {
	    logError(this.getClass().getName() + "installBundle",
	    		"Exception while installing bundle at " + location, e);
	    return null;
	}
    }

    public uAALBundleContext installBundle(String location, InputStream is) {
	try {
	    Bundle b = bundle.installBundle(location, is);
	    return new uAALBundleContext(b.getBundleContext());
	} catch (Exception e) {
	    logError(this.getClass().getName() + "installBundle",
	    		"Exception while installing bundle at " + location, e);
	    return null;
	}
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#listConfigFiles(org.universAAL.middleware.container.ModuleContext)
     */
    public File[] listConfigFiles(ModuleContext requester) {
	// TODO check permissions
	int n = confFiles.size();
	File[] files = new File[n];
	for (int i = 0; i < n; i++)
	    files[i] = (File) ((Object[]) confFiles.get(i))[0];
	return files;
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#logDebug(java.lang.String,
     *      java.lang.Throwable)
     */
    public void logDebug(String tag, String message, Throwable t) {
	logger.debug(tag + message, t);
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#logError(java.lang.String,
     *      java.lang.Throwable)
     */
    public void logError(String tag, String message, Throwable t) {
	logger.error(tag + message, t);
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#logInfo(java.lang.String,
     *      java.lang.Throwable)
     */
    public void logInfo(String tag, String message, Throwable t) {
	logger.info(tag + message, t);
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#logWarn(java.lang.String,
     *      java.lang.Throwable)
     */
    public void logWarn(String tag, String message, Throwable t) {
	logger.warn(tag + message, t);
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#logTrace(java.lang.String,
     *      java.lang.Throwable)
     */
    public void logTrace(String tag, String message, Throwable t) {
	logger.trace(tag + message, t);
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#registerConfigFile(java.lang.Object[])
     */
    public void registerConfigFile(Object[] configFileParams) {
	// TODO define a convention for the array param
	// current assumption: 1st param @ index 0 is the
	// org.osgi.framework.Constants.SERVICE_PID
	// chosen for a org.osgi.service.cm.ManagedService (type = String)
	// possible extensions:
	// 2nd param @ index 1: help string describing the role of the property
	// file indirectly specified by the first first param @ index 0
	// 3rd param @ index 2: a hash-table with allowed properties as keys and
	// a help string about each property as value
	if (configFileParams != null && configFileParams.length > 0) {
	    configFileParams[0] = servicesConfHome
		    .getPropFile(configFileParams[0].toString());
	    confFiles.add(configFileParams);
	}
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttribute(String attrName, Object attrValue) {
	if (attrName != null && attrValue != null)
	    extension.put(attrName, attrValue);
    }

    public void shareObject(String xface, Object obj, Dictionary props) {
	bundle.registerService(xface, obj, props);
    }

    public void shareObject(String[] xface, Object obj, Dictionary props) {
	bundle.registerService(xface, obj, props);
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#start(org.universAAL.middleware.container.ModuleContext)
     */
    public boolean start(ModuleContext requester) {
	if (canBeStarted(requester)) {
	    try {
		bundle.getBundle().start();
		return true;
	    } catch (Exception e) {
		// TODO: log
	    }
	}
	return false;
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#stop(org.universAAL.middleware.container.ModuleContext)
     */
    public boolean stop(ModuleContext requester) {
	if (canBeStopped(requester)) {
	    try {
		bundle.getBundle().stop();
		return true;
	    } catch (Exception e) {
		// TODO: log
	    }
	}
	return false;
    }

    /**
     * @see org.universAAL.middleware.container.ModuleContext#uninstall(org.universAAL.middleware.container.ModuleContext)
     */
    public boolean uninstall(ModuleContext requester) {
	if (canBeUninstalled(requester)) {
	    try {
		bundle.getBundle().uninstall();
		return true;
	    } catch (Exception e) {
		// TODO: log
	    }
	}
	return false;
    }

}
