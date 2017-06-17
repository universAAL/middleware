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
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.universAAL.middleware.container.Attributes;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;

/**
 * @author amedrano
 * 
 */
public class POJOModuleContext implements ModuleContext {

	public enum LogLevel {
		ERROR, WARN, INFO, DEBUG, TRACE, NONE
	}

	private Logger logger;

	private Map<String, Object> attributeMap;

	private Set<File> configFiles;

	private ModuleActivator activator;

	private File configurationFolder;

	private File dataFolder;

	private Container container;

	public POJOModuleContext(ModuleActivator ma, POJOContainer container) {
		activator = ma;
		attributeMap = new HashMap<String, Object>();
		configFiles = new HashSet<File>();
		this.container = container;
		logger = Logger.getLogger(getClass());
		// Get all needed properties from container.
		loadUniversAALAttribute();
		configurationFolder = container.getConfigurationFolder();
		dataFolder = container.getDataFolder();
	}

	private void loadUniversAALAttribute() {
		Properties props = new Properties();
		try {
			props.load(this.getClass().getResourceAsStream(
					"attributes.properties"));
			setAttribute(Attributes.MIDDLEWARE_VERSION, props.getProperty(
					Attributes.MIDDLEWARE_VERSION,
					System.getProperty(Attributes.MIDDLEWARE_VERSION, "3.4.0")));
		} catch (IOException e) {
			logger.error("Unable to load default attributes set");
			setAttribute(Attributes.MIDDLEWARE_VERSION, "3.0.0");
		}
		setAttribute(Attributes.CONTAINER_NAME, "pojo");
		setAttribute(Attributes.CONTAINER_VERSION,
				getAttribute(Attributes.MIDDLEWARE_VERSION));

		setAttribute(Attributes.CONTAINER_PLATFORM_NAME, "java");
		setAttribute(Attributes.CONTAINER_PLATFORM_VERSION,
				System.getProperty("java.specification.version"));
		setAttribute(Attributes.CONTAINER_OS_NAME,
				System.getProperty("os.name"));
		setAttribute(Attributes.CONTAINER_OS_VERSION,
				System.getProperty("os.version"));
		setAttribute(Attributes.CONTAINER_OS_ARCHITECTURE,
				System.getProperty("os.arch"));
		setAttribute(Attributes.CONTAINER_EE_NAME,
				System.getProperty("java.vendor"));

		setAttribute(Attributes.CONTAINER_EE_VERSION,
				System.getProperty("java.version"));
	}

	/** {@inheritDoc} */
	public boolean canBeStarted(ModuleContext requester) {
		// If can be accessed then it can be started
		return true;
	}

	/** {@inheritDoc} */
	public boolean canBeStopped(ModuleContext requester) {
		// If can be accessed then it can be stopped
		return true;
	}

	/** {@inheritDoc} */
	public boolean canBeUninstalled(ModuleContext requester) {
		// can not uninstall things in POJO.
		return false;
	}

	/** {@inheritDoc} */
	public Object getAttribute(String attrName) {
		return attributeMap.get(attrName);
	}

	/** {@inheritDoc} */
	public Container getContainer() {
		return container;
	}

	/** {@inheritDoc} */
	public String getID() {
		return activator.getClass().getPackage().getName();
	}

	/** {@inheritDoc} */
	public File[] listConfigFiles(ModuleContext requester) {
		return configFiles.toArray(new File[configFiles.size()]);
	}

	/** {@inheritDoc} */
	public void logDebug(String tag, String message, Throwable t) {
		logger.debug(tag + ": " + message, t);
	}

	/** {@inheritDoc} */
	public void logError(String tag, String message, Throwable t) {
		logger.error(tag + ": " + message, t);
	}

	/** {@inheritDoc} */
	public void logInfo(String tag, String message, Throwable t) {
		logger.info(tag + ": " + message, t);
	}

	/** {@inheritDoc} */
	public void logWarn(String tag, String message, Throwable t) {
		logger.warn(tag + ": " + message, t);
	}

	/** {@inheritDoc} */
	public void logTrace(String tag, String message, Throwable t) {
		logger.trace(tag + ": " + message, t);
	}

	/** {@inheritDoc} */
	public boolean isLogErrorEnabled() {
		return Level.ERROR.isGreaterOrEqual(logger.getEffectiveLevel());
	}

	/** {@inheritDoc} */
	public boolean isLogWarnEnabled() {
		return Level.WARN.isGreaterOrEqual(logger.getEffectiveLevel());
	}

	/** {@inheritDoc} */
	public boolean isLogInfoEnabled() {
		return logger.isInfoEnabled();
	}

	/** {@inheritDoc} */
	public boolean isLogDebugEnabled() {
		return logger.isDebugEnabled();
	}

	/** {@inheritDoc} */
	public boolean isLogTraceEnabled() {
		return logger.isTraceEnabled();
	}

	/** {@inheritDoc} */
	public void registerConfigFile(Object[] configFileParams) {
		configFiles.add((File) configFileParams[0]);
	}

	/** {@inheritDoc} */
	public void setAttribute(String attrName, Object attrValue) {
		attributeMap.put(attrName, attrValue);
	}

	/** {@inheritDoc} */
	public boolean start(ModuleContext requester) {
		if (canBeStarted(requester)) {
			try {
				activator.start(this);
				return true;
			} catch (Exception e) {
				logger.error("Unable to start: "
						+ activator.getClass().getPackage().getName(), e);
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	public boolean stop(ModuleContext requester) {
		if (canBeStopped(requester)) {
			try {
				activator.stop(this);
				return true;
			} catch (Exception e) {
				logger.error("Unable to stop: "
						+ activator.getClass().getPackage().getName(), e);
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	public boolean uninstall(ModuleContext requester) {
		// in POJO one can stop the module but not unistall?
		return false;
	}

	/** {@inheritDoc} */
	public Object getProperty(String name) {

		Object value = getAttribute(name);
		if (value != null)
			return value;

		value = System.getProperty(name);
		if (value != null)
			return value;

		value = System.getenv(name);
		if (value != null)
			return value;

		return null;
	}

	/** {@inheritDoc} */
	public Object getProperty(String name, Object def) {
		Object value = getProperty(name);
		if (value == null)
			return def;
		return value;
	}

	public String getManifestEntry(String name) {
		// TODO
		return null;
	}

	public String getManifestEntry(String manifest, String name) {
		// TODO
		return null;
	}

	public File getConfigHome() {
		return new File(configurationFolder, getID());
	}

	public File getDataFolder() {
		return new File(dataFolder, getID());
	}
}
