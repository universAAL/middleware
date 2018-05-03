/*
        Copyright 2011-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
        Fraunhofer-Gesellschaft - Institute for Computer Graphics Research

        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council


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
import java.util.Collections;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.LogListener;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.osgi.run.Activator;

/**
 * An implementation of the concept of {@link Container} for OSGi.
 *
 * @author mtazari
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 *
 */
public final class OSGiContainer implements Container, ServiceListener {
	public static final OSGiContainer THE_CONTAINER = new OSGiContainer();

	private List<SharedObjectListener> listeners;

	private OSGiContainer() {
		listeners = Collections.synchronizedList(new ArrayList<SharedObjectListener>());
	}

	/**
	 * @see org.universAAL.middleware.container.Container#fetchSharedObject(ModuleContext,
	 *      Object[], SharedObjectListener)
	 *
	 * @param fetchParams
	 *            <ul>
	 *            <li>min length = 1, max used length is 2 (elements @ index 2+
	 *            are ignored)</li>
	 *            <li>type of both elements must be
	 *            {@link java.lang.String}</li>
	 *            <li>if element @ index 0 is null, there must be a not-null
	 *            String object @ index 1</li>
	 *            <li>if length = 1, then element @ index 0 must be
	 *            not-null</li>
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
	 *         {@link OSGiModuleContext} and a {@link ServiceReference} has been
	 *         found according to the explanations given for
	 *         <code>fetchParams</code>, then the result of calling
	 *         {@link BundleContext#getService(ServiceReference)} will be
	 *         returned.
	 */
	public Object fetchSharedObject(ModuleContext requester, Object[] fetchParams) {
		if (requester instanceof OSGiModuleContext && fetchParams != null && fetchParams.length > 0)
			if (fetchParams.length == 1) {
				if (fetchParams[0] instanceof String) {
					return ((OSGiModuleContext) requester).fetchObject((String) fetchParams[0]);
				}
			} else if ((fetchParams[0] == null || fetchParams[0] instanceof String)
					&& (fetchParams[1] == null || fetchParams[1] instanceof String)) {
				Object[] result = ((OSGiModuleContext) requester).fetchObject((String) fetchParams[0],
						(String) fetchParams[1]);
				if (result != null)
					return result[0];
			}
		// problems with parameters
		return null;
	}

	public Object[] fetchSharedObject(ModuleContext requester, Object[] fetchParams, SharedObjectListener listener) {
		if (requester instanceof OSGiModuleContext && fetchParams != null && fetchParams.length > 0)
			if (fetchParams.length == 1) {
				if (fetchParams[0] instanceof String) {
					if (listener != null && !listeners.contains(listener)) {
						synchronized (listeners) {
							listeners.add(listener);
						}

					}
					return ((OSGiModuleContext) requester).fetchObject((String) fetchParams[0], null);
				}
			} else if ((fetchParams[0] == null || fetchParams[0] instanceof String)
					&& (fetchParams[1] == null || fetchParams[1] instanceof String)) {
				if (listener != null && !listeners.contains(listener))
					synchronized (listeners) {
						listeners.add(listener);
					}
				return ((OSGiModuleContext) requester).fetchObject((String) fetchParams[0], (String) fetchParams[1]);
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
	 * @return a newly created instance of {@link OSGiModuleContext} that wraps
	 *         the newly installed bundle if the given requester is an instance
	 *         of {@link OSGiModuleContext}, the conditions on
	 *         <code>installParams</code> hold, and the installation is
	 *         successful, null otherwise.
	 */
	public ModuleContext installModule(ModuleContext requester, Object[] installParams) {
		if (!(requester instanceof OSGiModuleContext))
			return null;
		if (installParams == null || installParams.length == 0 || !(installParams[0] instanceof String))
			return null;
		return (installParams.length == 1 || !(installParams[1] instanceof InputStream))
				? ((OSGiModuleContext) requester).installBundle((String) installParams[0])
				: ((OSGiModuleContext) requester).installBundle((String) installParams[0],
						(InputStream) installParams[1]);
	}

	/**
	 * @see org.universAAL.middleware.container.Container#logListeners()
	 */
	public Iterator<LogListener> logListeners() {
		return Activator.logListeners();
	}

	/**
	 * @see org.universAAL.middleware.container.Container#registerModule(java.lang.Object[])
	 *
	 * @param regParams
	 *            element @ index 0 must exist and be an instance of
	 *            {@link BundleContext}
	 *
	 * @return an instance of {@link OSGiModuleContext} if the conditions for
	 *         <code>regParams</code> hold, null otherwise
	 */
	public ModuleContext registerModule(Object[] regParams) {
		BundleContext bContext = null;
		if (regParams != null && regParams.length > 0 && regParams[0] instanceof BundleContext) {
			bContext = (BundleContext) regParams[0];
			// register me as ServiceListener
			bContext.addServiceListener(THE_CONTAINER);
		}
		return (regParams != null && regParams.length > 0 && regParams[0] instanceof BundleContext)
				? new OSGiModuleContext((BundleContext) regParams[0]) : null;
	}

	/**
	 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent se) {
		// NOTE: Not handling ServiceEvent.MODIFIED event
		if (se == null || se.getType() == ServiceEvent.MODIFIED)
			return;
		ServiceReference sr = se.getServiceReference();
		if (sr == null)
			return;
		final BundleContext bc = sr.getBundle().getBundleContext();
		final ArrayList<SharedObjectListener> listenersLocalCopy;
		synchronized (listeners) {
			listenersLocalCopy = new ArrayList<SharedObjectListener>(listeners);
		}
		switch (se.getType()) {
		case ServiceEvent.REGISTERED:
			for (Iterator<SharedObjectListener> i = listenersLocalCopy.iterator(); i.hasNext();)
				i.next().sharedObjectAdded(bc.getService(sr), sr);
			break;
		case ServiceEvent.UNREGISTERING:
			for (Iterator<SharedObjectListener> i = listenersLocalCopy.iterator(); i.hasNext();) {
				SharedObjectListener sol = i.next();
				if (sol != null && bc != null) {
					sol.sharedObjectRemoved(bc.getService(sr));
				}
			}
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
	 *            {@link org.osgi.framework.BundleContext#registerService(String[], Object, java.util.Dictionary) }
	 *            ).</li>
	 *            </ul>
	 */
	public void shareObject(ModuleContext requester, Object objToShare, Object[] shareParams) {
		if (!(requester instanceof OSGiModuleContext) || objToShare == null || shareParams == null
				|| shareParams.length == 0) {
			requester.logWarn(this.getClass().getName() + "shareObject",
					"Parameters passed to 'shareObject' do not satisfy the requirements of mw.container.osgi!", null);
			return;
		}

		int n = shareParams.length - 1;
		if (n == 0)
			if (shareParams[0] instanceof String)
				((OSGiModuleContext) requester).shareObject((String) shareParams[0], objToShare, null);
			else if (shareParams[0] instanceof Dictionary)
				((OSGiModuleContext) requester).shareObject((String) null, objToShare,
						(Dictionary<?, ?>) shareParams[0]);
			else
				requester.logWarn(this.getClass().getName() + "shareObject",
						"'shareParams' passed to 'shareObject' do not satisfy the requirements of mw.container.osgi!",
						null);
		else {
			for (int i = 0; i < n; i++)
				if (!(shareParams[i] instanceof String)) {
					requester.logWarn(this.getClass().getName() + "shareObject",
							"'shareParams' passed to 'shareObject' do not satisfy the requirements of mw.container.osgi!",
							null);
					return;
				}
			if (shareParams[n] instanceof String)
				((OSGiModuleContext) requester).shareObject((String[]) shareParams, objToShare, null);
			else if (shareParams[n] instanceof Dictionary)
				if (n == 1)
					((OSGiModuleContext) requester).shareObject((String) shareParams[0], objToShare,
							(Dictionary<?, ?>) shareParams[1]);
				else {
					String[] xfaces = new String[n];
					for (int i = 0; i < n; i++)
						xfaces[i] = (String) shareParams[i];
					((OSGiModuleContext) requester).shareObject(xfaces, objToShare, (Dictionary<?, ?>) shareParams[n]);
				}
			else
				requester.logWarn(this.getClass().getName() + "shareObject",
						"'shareParams' passed to 'shareObject' do not satisfy the requirements of mw.container.osgi!",
						null);
		}
	}

	public void removeSharedObject(ModuleContext requester, Object objToRemove, Object[] shareParams) {
		if (!(requester instanceof OSGiModuleContext) || objToRemove == null || shareParams == null
				|| shareParams.length == 0) {
			requester.logWarn(this.getClass().getName() + "removeSharedObject",
					"Parameters passed to 'removeSharedObject' do not satisfy the requirements of mw.container.osgi!",
					null);
			return;
		}

		int n = shareParams.length - 1;
		if (n == 0)
			if (shareParams[0] instanceof String)
				((OSGiModuleContext) requester).removeSharedObject((String) shareParams[0], objToRemove, null);
			else if (shareParams[0] instanceof Dictionary)
				((OSGiModuleContext) requester).removeSharedObject((String) null, objToRemove,
						(Dictionary<?, ?>) shareParams[0]);
			else
				requester.logWarn(this.getClass().getName() + "removeSharedObject",
						"'shareParams' passed to 'removeSharedObject' do not satisfy the requirements of mw.container.osgi!",
						null);
		else {
			for (int i = 0; i < n; i++)
				if (!(shareParams[i] instanceof String)) {
					requester.logWarn(this.getClass().getName() + "removeSharedObject",
							"'shareParams' passed to 'removeSharedObject' do not satisfy the requirements of mw.container.osgi!",
							null);
					return;
				}
			if (shareParams[n] instanceof String)
				((OSGiModuleContext) requester).removeSharedObject((String[]) shareParams, objToRemove, null);
			else if (shareParams[n] instanceof Dictionary)
				if (n == 1)
					((OSGiModuleContext) requester).removeSharedObject((String) shareParams[0], objToRemove,
							(Dictionary<?, ?>) shareParams[1]);
				else {
					String[] xfaces = new String[n];
					for (int i = 0; i < n; i++)
						xfaces[i] = (String) shareParams[i];
					((OSGiModuleContext) requester).removeSharedObject(xfaces, objToRemove,
							(Dictionary<?, ?>) shareParams[n]);
				}
			else
				requester.logWarn(this.getClass().getName() + "removeSharedObject",
						"'shareParams' passed to 'removeSharedObject' do not satisfy the requirements of mw.container.osgi!",
						null);
		}
	}

	public void removeSharedObjectListener(SharedObjectListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}
}
