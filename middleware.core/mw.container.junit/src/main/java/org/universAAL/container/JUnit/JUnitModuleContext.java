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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;

/**
 * @author amedrano
 * 
 */
public class JUnitModuleContext implements ModuleContext {

    private Logger logger;

    private Map<String, Object> attributeMap;

    private Set<File> configFiles;

    private ModuleActivator activator;

    private static Appender ca = new ConsoleAppender(new SimpleLayout());

    public JUnitModuleContext(ModuleActivator ma) {
	activator = ma;
	attributeMap = new HashMap<String, Object>();
	configFiles = new HashSet<File>();
	logger = LogManager.getLogger(activator.getClass().getPackage()
		.getName());
	logger.setLevel(Level.ALL);
	logger.addAppender(ca);
    }

    public JUnitModuleContext() {
	this(new ModuleActivator() {

	    public void stop(ModuleContext mc) throws Exception {
	    }

	    public void start(ModuleContext mc) throws Exception {
	    }
	});
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
	return JUnitContainer.getInstance();
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
	// TODO Auto-generated method stub
	return null;
    }

    public String getManifestEntry(String manifest, String name) {
	// TODO Auto-generated method stub
	return null;
    }
}
