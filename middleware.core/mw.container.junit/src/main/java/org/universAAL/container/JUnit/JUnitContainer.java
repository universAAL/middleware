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
package org.universAAL.container.JUnit;

import java.util.ArrayList;
import java.util.HashMap;
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
 *
 */
public class JUnitContainer implements Container {

	private static JUnitContainer instance = null;
	
	private List<SharedObjectListener> listeners;
	
	private Map<String, Object> sharedObjectMap;
	
//	private Map<String, POJOModuleContext> modules;
	
	private JUnitContainer(){
		listeners = new ArrayList<SharedObjectListener>();	
		sharedObjectMap = new HashMap<String, Object>();
	};
	
	public static JUnitContainer getInstance(){
		if (instance == null) {
			instance = new JUnitContainer();
		}
		return instance;
	}
	
	/** {@inheritDoc} */
	public Object fetchSharedObject(ModuleContext requester,
			Object[] fetchParams) {
		return sharedObjectMap.get(fetchParams[0]);
	}

	/** {@inheritDoc} */
	public Object[] fetchSharedObject(ModuleContext requester,
			Object[] fetchParams, SharedObjectListener listener) {
		synchronized (listeners) {
		    listeners.add(listener);
		}
		return (Object[]) fetchSharedObject(requester, fetchParams);
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
	public ModuleContext installModule(ModuleContext requester,
			Object[] installParams) {
		// no installing.
		return null;
	}

	/** {@inheritDoc} */
	public Iterator logListeners() {
		return new ArrayList<LogListener>().iterator();
	}

	/** {@inheritDoc} */
	public ModuleContext registerModule(Object[] regParams) {
		JUnitModuleContext mc = new JUnitModuleContext((ModuleActivator)regParams[0]); 
		return mc;
	}

	/** {@inheritDoc} */
	public void shareObject(ModuleContext requester, Object objToShare,
			Object[] shareParams) {
		sharedObjectMap.put((String) shareParams[0], objToShare);

	}

}
