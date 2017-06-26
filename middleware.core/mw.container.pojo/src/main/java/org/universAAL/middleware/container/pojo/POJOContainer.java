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
package org.universAAL.middleware.container.pojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.LogListener;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;

/**
 * @author amedrano
 * @author Carsten Stockloew
 */
public final class POJOContainer implements Container {

	public static final String DATA_DIR_PROP = "org.universAAL.mw.container.pojo.datadir";
	public static final String CONF_DIR_PROP = "org.universAAL.mw.container.pojo.confdir";
	public static final String SYS_FILE = "system.properties";

	private List<SharedObjectListener> listeners;
	private List<LogListener> logListeners;

	private Map<String, Object> sharedObjectMap;

	private String configFolder = null;

	private Map<ModuleActivator, POJOModuleContext> modules;

	public POJOContainer() {
		listeners = new ArrayList<SharedObjectListener>();
		logListeners = new ArrayList<LogListener>();
		sharedObjectMap = new Hashtable<String, Object>();
		// load default system properties
		try {
			System.getProperties().load(
					getClass().getResourceAsStream(SYS_FILE));
		} catch (IOException e1) {
			Logger.getLogger(getClass()).error("File system error", e1);
		}
		reloadProperties();
	};

	private void reloadProperties() {

		File sysprop = new File(getConfigurationFolder(), SYS_FILE);
		// try load other system properties
		if (sysprop.exists()) {
			try {
				System.getProperties().load(new FileInputStream(sysprop));
			} catch (FileNotFoundException e) {
				Logger.getLogger(getClass()).info("System File Not found", e);
			} catch (IOException e) {
				Logger.getLogger(getClass()).error("File system error", e);
			}
		}
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
		return new Object[] { fetchSharedObject(requester, fetchParams) };
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
		return logListeners.iterator();
	}

	/** {@inheritDoc} */
	public ModuleContext registerModule(Object[] regParams) {
		ModuleActivator ma = (ModuleActivator) regParams[0];
		POJOModuleContext mc = new POJOModuleContext(ma, this);
		// keep track of modules
		modules.put(ma, mc);
		return mc;
	}

	/** {@inheritDoc} */
	public void shareObject(ModuleContext requester, Object objToShare,
			Object[] shareParams) {
		sharedObjectMap.put((String) shareParams[0], objToShare);
		// check if shared object is a LogListener for registering it
		if (objToShare instanceof LogListener) {
			registerLogListener((LogListener) objToShare);
		}
	}

	public void removeSharedObject(ModuleContext requester, Object objToRemove,
			Object[] shareParams) {
		sharedObjectMap.remove((String) shareParams[0]);
		// check if shared object is a LogListener for unregistering it
		if (objToRemove instanceof LogListener) {
			unregisterLogListener((LogListener) objToRemove);
		}
	}

	/** Register a LogListener */
	private void registerLogListener(LogListener listener) {
		logListeners.add(listener);
	}

	/** Remove a LogListener */
	private void unregisterLogListener(LogListener listener) {
		logListeners.remove(listener);
	}

	public void removeAllSharedObjects() {
		listeners.clear();
		sharedObjectMap.clear();
	}

	public void setConfigFolder(String newConfig) {
		configFolder = newConfig;
		// reload config
		reloadProperties();
		// XXX this will maintain previous properties which are not overridden
		// by the file.
	}

	/**
	 * @return
	 */
	public File getConfigurationFolder() {
		File cd;
		if (configFolder == null
				&& System.getProperties().contains(CONF_DIR_PROP)) {
			configFolder = System.getProperty(CONF_DIR_PROP);
		}
		if (configFolder == null) {
			cd = new File(System.getProperty(CONF_DIR_PROP, "./confdir/"));
		} else {
			cd = new File(configFolder);
		}
		if (!cd.exists()) {
			cd.mkdirs();
		}
		return cd;
	}

	/**
	 * @return
	 */
	public File getDataFolder() {
		File dd = new File(System.getProperty(DATA_DIR_PROP, "./datadir/"));
		if (!dd.exists()) {
			dd.mkdirs();
		}
		return dd;
	}
}
