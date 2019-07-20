/*******************************************************************************
 * Copyright 2013 2011 Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.middleware.container.JUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.LogListener;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;

/**
 * @author amedrano
 * @author Carsten Stockloew
 */
public final class JUnitContainer implements Container {

	private static JUnitContainer instance = null;

	private Collection<SharedObjectListener> listeners;
	private Collection<LogListener> logListeners;

	private Map<String, Object> sharedObjectMap;

	// private Map<String, POJOModuleContext> modules;

	private JUnitContainer() {
		listeners = new HashSet<SharedObjectListener>();
		logListeners = new ArrayList<LogListener>();
		sharedObjectMap = new Hashtable<String, Object>();
	};

	public static JUnitContainer getInstance() {
		if (instance == null) {
			instance = new JUnitContainer();
		}
		return instance;
	}

	/** {@inheritDoc} */
	public Object fetchSharedObject(ModuleContext requester, Object[] fetchParams) {
		int lastXface = fetchParams.length;
//		if (fetchParams[lastXface] instanceof Filter) {
//			//TODO manage filter
//			lastXface--;
//		}
		for (int i = 0; i < lastXface; i++) {
			Object stored = sharedObjectMap.get(fetchParams[i]);
			if (stored instanceof List) {
				return ((List<Object>) stored).get(0);
			} else if (stored != null)
				return stored;
		}
		return null;
	}

	/** {@inheritDoc} */
	public Object[] fetchSharedObject(ModuleContext requester, Object[] fetchParams, SharedObjectListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}

		int lastXface = fetchParams.length;
//		if (fetchParams[lastXface] instanceof Filter) {
//			//TODO manage filter
//			lastXface--;
//		}
		
		HashSet<Object> result = new HashSet<Object>();
		for (int i = 0; i < lastXface; i++) {
			Object stored = sharedObjectMap.get(fetchParams[i]);
			if (stored instanceof List) {
				result.addAll((List<Object>) stored);
			} else if (stored != null)
				result.add(stored);
		}

		return result.toArray(new Object[result.size()]);
	}

	/** {@inheritDoc} */
	public void removeSharedObjectListener(SharedObjectListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}

	/** {@inheritDoc} */
	public ModuleContext installModule(ModuleContext requester, Object[] installParams) {
		// no installing.
		return null;
	}

	/** Register a LogListener */
	public void registerLogListener(LogListener listener) {
		logListeners.add(listener);
	}

	/** Remove a LogListener */
	public void unregisterLogListener(LogListener listener) {
		logListeners.remove(listener);
	}

	/** {@inheritDoc} */
	public Iterator logListeners() {
		return logListeners.iterator();
	}

	/** {@inheritDoc} */
	public ModuleContext registerModule(Object[] regParams) {
		String logname = "uAAL";
		if (regParams.length > 1){
			logname = (String) regParams[1];
		}
		JUnitModuleContext mc = new JUnitModuleContext((ModuleActivator) regParams[0], logname);
		return mc;
	}

	/** {@inheritDoc} */
	public void shareObject(ModuleContext requester, Object objToShare, Object[] shareParams) {
		int lastXface = shareParams.length - 1;
		if (shareParams[lastXface] instanceof Dictionary) {
			lastXface--;
		}
		for (int i = 0; i <= lastXface; i++) {
			if (!sharedObjectMap.containsKey(shareParams[i])) {
				sharedObjectMap.put((String) shareParams[i], objToShare);
			} else {
				//there is already an object => create list
				Object existing = sharedObjectMap.get(shareParams[i]);
				ArrayList<Object> list = new ArrayList<Object>();
				list.add(existing);
				list.add(objToShare);
				sharedObjectMap.put((String) shareParams[i], list);
			}
		}
		//TODO Manage Dictionary
		
		//notify listeners
		for (SharedObjectListener sharedObjectListener : listeners) {
			sharedObjectListener.sharedObjectAdded(objToShare, objToShare);
		}
	}

	public void removeSharedObject(ModuleContext requester, Object objToRemove, Object[] shareParams) {
		int lastXface = shareParams.length - 1;
		if (shareParams[lastXface] instanceof Dictionary) {
			lastXface--;
		}
		for (int i = 0; i <= lastXface; i++) {
			Object stored=sharedObjectMap.get((String) shareParams[i]);
			if (stored == null) {
				continue;
			}
			if (stored.equals(objToRemove)) {
				sharedObjectMap.remove((String) shareParams[i]);
				continue;
			}
			if (stored instanceof List) {
				((List<Object>)stored).remove(objToRemove);
			}
		}

		//notify listeners
		for (SharedObjectListener sharedObjectListener : listeners) {
			sharedObjectListener.sharedObjectRemoved(objToRemove);
		}
	}

	public void removeAllSharedObjects() {
		listeners.clear();
		sharedObjectMap.clear();
	}
}
