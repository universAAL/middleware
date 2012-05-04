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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.osgi.run.Activator;

/**
 * An implementation of the concept of {@link Container} for OSGi.
 * 
 * @author mtazari
 * 
 */
public class uAALBundleContainer implements Container, ServiceListener {
    public static final uAALBundleContainer THE_CONTAINER = new uAALBundleContainer();

    private ArrayList listeners;

    private uAALBundleContainer() {
	listeners = new ArrayList();
    }

    /**
     * @see org.universAAL.middleware.container.Container#fetchSharedObject(ModuleContext,
     *      Object[], SharedObjectListener)
     * 
     * @param fetchParams
     *            <ul>
     *            <li>min length = 1, max used length is 2 (elements @ index 2+
     *            are ignored)</li>
     *            <li>type of both elements must be {@link java.lang.String}</li>
     *            <li>if element @ index 0 is null, there must be a not-null
     *            String object @ index 1</li>
     *            <li>if length = 1, then element @ index 0 must be not-null</li>
     *            <li>if length = 1, then
     *            {@link BundleContext#getServiceReference(String)} will be used
     *            to fetch a service object</li>
     *            <li>if length > 1, then
     *            {@link BundleContext#getServiceReferences(String, String)}
     *            will be used to fetch the first service object from the
     *            returned array</li>
     *            </ul>
     * 
     * @return if the given requester is an instance of
     *         {@link uAALBundleContext} and a {@link ServiceReference} has been
     *         found according to the explanations given for
     *         <code>fetchParams</code>, then the result of calling
     *         {@link BundleContext#getService(ServiceReference)} will be
     *         returned.
     */
    public Object fetchSharedObject(ModuleContext requester,
	    Object[] fetchParams) {
	if (requester instanceof uAALBundleContext && fetchParams != null
		&& fetchParams.length > 0)
	    if (fetchParams.length == 1) {
		if (fetchParams[0] instanceof String) {
		    return ((uAALBundleContext) requester)
			    .fetchObject((String) fetchParams[0]);
		}
	    } else if ((fetchParams[0] == null || fetchParams[0] instanceof String)
		    && (fetchParams[1] == null || fetchParams[1] instanceof String)) {
		Object[] result = ((uAALBundleContext) requester).fetchObject(
			(String) fetchParams[0], (String) fetchParams[1]);
		if (result != null)
		    return result[0];
	    }
	// problems with parameters
	return null;
    }

    public Object[] fetchSharedObject(ModuleContext requester,
	    Object[] fetchParams, SharedObjectListener listener) {
	if (requester instanceof uAALBundleContext && fetchParams != null
		&& fetchParams.length > 0)
	    if (fetchParams.length == 1) {
		if (fetchParams[0] instanceof String) {
		    if (listener != null)
			listeners.add(listener);
		    return ((uAALBundleContext) requester).fetchObject(
			    (String) fetchParams[0], null);
		}
	    } else if ((fetchParams[0] == null || fetchParams[0] instanceof String)
		    && (fetchParams[1] == null || fetchParams[1] instanceof String)) {
		if (listener != null)
		    listeners.add(listener);
		return ((uAALBundleContext) requester).fetchObject(
			(String) fetchParams[0], (String) fetchParams[1]);
	    }
	// problems with parameters => do not add the listener
	return null;
    }

    /**
     * @see org.universAAL.middleware.container.Container#installModule(org.universAAL.middleware.container.ModuleContext,
     *      java.lang.Object[])
     * 
     * @param installParams
     *            <ul>
     *            <li>min length = 1, max used length is 2 (elements @ index 2+
     *            are ignored)</li>
     *            <li>type of element @ index 0 must be {@link java.lang.String}
     *            (mandatory)</li>
     *            <li>if existing, type of element @ index 1 must be
     *            {@link java.io.InputStream}</li>
     *            <li>if length = 1, then
     *            {@link BundleContext#installBundle(String)} will be used to
     *            install the given bundle</li>
     *            <li>if length > 1, then
     *            {@link BundleContext#installBundle(String, java.io.InputStream)}
     *            will be used to install the given bundle</li>
     *            </ul>
     * 
     * @return a newly created instance of {@link uAALBundleContext} that wraps
     *         the newly installed bundle if the given requester is an instance
     *         of {@link uAALBundleContext}, the conditions on
     *         <code>installParams</code> hold, and the installation is
     *         successful, null otherwise.
     */
    public ModuleContext installModule(ModuleContext requester,
	    Object[] installParams) {
	if (!(requester instanceof uAALBundleContext))
	    return null;
	if (installParams == null || installParams.length == 0
		|| !(installParams[0] instanceof String))
	    return null;
	return (installParams.length == 1 || !(installParams[1] instanceof InputStream)) ? ((uAALBundleContext) requester)
		.installBundle((String) installParams[0])
		: ((uAALBundleContext) requester).installBundle(
			(String) installParams[0],
			(InputStream) installParams[1]);
    }

    /**
     * @see org.universAAL.middleware.container.Container#logListeners()
     */
    public Iterator logListeners() {
	return Activator.logListeners();
    }

    /**
     * @see org.universAAL.middleware.container.Container#registerModule(java.lang.Object[])
     * 
     * @param regParams
     *            element @ index 0 must exist and be an instance of
     *            {@link BundleContext}
     * 
     * @return an instance of {@link uAALBundleContext} if the conditions for
     *         <code>regParams</code> hold, null otherwise
     */
    public ModuleContext registerModule(Object[] regParams) {
	return (regParams != null && regParams.length > 0 && regParams[0] instanceof BundleContext) ? new uAALBundleContext(
		(BundleContext) regParams[0])
		: null;
    }

    /**
     * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
     */
    public void serviceChanged(ServiceEvent se) {
	if (se == null)
	    return;
	ServiceReference sr = se.getServiceReference();
	if (sr == null)
	    return;
	switch (se.getType()) {
	case ServiceEvent.REGISTERED:
	    BundleContext bc = sr.getBundle().getBundleContext();
	    for (Iterator i = listeners.iterator(); i.hasNext();)
		((SharedObjectListener) i.next()).sharedObjectAdded(bc
			.getService(sr), sr);
	    break;
	case ServiceEvent.UNREGISTERING:
	    for (Iterator i = listeners.iterator(); i.hasNext();)
		((SharedObjectListener) i.next()).sharedObjectRemoved(sr);
	    break;
	}
    }

    /**
     * @see org.universAAL.middleware.container.Container#shareObject(org.universAAL.middleware.container.ModuleContext,
     *      java.lang.Object, java.lang.Object[])
     * 
     * @param shareParams
     *            <ul>
     *            <li>min length = 1, no upper limit</li>
     *            <li>all elements (except for an optional last one - see next
     *            bullet) must be instances of {@link java.lang.String}; these
     *            strings specify the interfaces that the
     *            <code>objToShare</code> has implemented (cf.
     *            {@link org.osgi.framework.BundleContext#registerService(String[], Object, java.util.Dictionary)}
     *            ).</li>
     *            <li>optionally, a last element can be an instance of
     *            {@link java.util.Dictionary}; it can be used for
     *            property-based search of shared objects (cf.
     *            {@link org.osgi.framework.BundleContext#registerService(String[], Object, java.util.Dictionary)
     *            }
     *            ).</li>
     *            </ul>
     */
    public void shareObject(ModuleContext requester, Object objToShare,
	    Object[] shareParams) {
	if (!(requester instanceof uAALBundleContext) || objToShare == null
		|| shareParams == null || shareParams.length == 0) {
	    requester
		    .logWarn(this.getClass().getName() + "shareObject",
			    "Parameters passed to 'shareObject' do not satisfy the requirements of mw.container.osgi!",
			    null);
	    return;
	}

	int n = shareParams.length - 1;
	if (n == 0)
	    if (shareParams[0] instanceof String)
		((uAALBundleContext) requester).shareObject(
			(String) shareParams[0], objToShare, null);
	    else if (shareParams[0] instanceof Dictionary)
		((uAALBundleContext) requester).shareObject((String) null,
			objToShare, (Dictionary) shareParams[0]);
	    else
		requester
			.logWarn(this.getClass().getName() + "shareObject",
				"'shareParams' passed to 'shareObject' do not satisfy the requirements of mw.container.osgi!",
				null);
	else {
	    for (int i = 0; i < n; i++)
		if (!(shareParams[i] instanceof String)) {
		    requester
			    .logWarn(this.getClass().getName() + "shareObject",
				    "'shareParams' passed to 'shareObject' do not satisfy the requirements of mw.container.osgi!",
				    null);
		    return;
		}
	    if (shareParams[n] instanceof String)
		((uAALBundleContext) requester).shareObject(
			(String[]) shareParams, objToShare, null);
	    else if (shareParams[n] instanceof Dictionary)
		if (n == 1)
		    ((uAALBundleContext) requester).shareObject(
			    (String) shareParams[0], objToShare,
			    (Dictionary) shareParams[1]);
		else {
		    String[] xfaces = new String[n];
		    for (int i = 0; i < n; i++)
			xfaces[i] = (String) shareParams[i];
		    ((uAALBundleContext) requester).shareObject(xfaces,
			    objToShare, (Dictionary) shareParams[n]);
		}
	    else
		requester
			.logWarn(this.getClass().getName() + "shareObject",
				"'shareParams' passed to 'shareObject' do not satisfy the requirements of mw.container.osgi!",
				null);
	}
    }
}
