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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.middleware.container.Attributes;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.run.Activator;
import org.universAAL.middleware.container.utils.LogUtils;

/**
 * An implementation of the concept of {@link ModuleContext} for OSGi.
 *
 * @author mtazari
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 */
public class OSGiModuleContext implements ModuleContext {
	private BundleContext bundle;
	private Properties extension = new Properties();
	private static Properties staticExtension = new Properties() {
		{
			try {
				load(this.getClass().getResourceAsStream("attributes.properties"));
			} catch (IOException e) {
				put(Attributes.MIDDLEWARE_VERSION, "2.0.0+");
			}
		}
	};
	private Logger logger;

	/**
	 * The root directory of the runtime configuration.
	 */
	public static final String CONF_ROOT_PROP = "bundles.configuration.location";
	public static final String CONF_KARAF_PROP = "karaf.etc";
	public static final String WORK_KARAF_PROP = "bundles.configuration.location";
	public static final String WORK_ROOT_DEFAULT = "./data/";

	private static File servicesConfHome = new File(
			System.getProperty(CONF_ROOT_PROP, System.getProperty(CONF_KARAF_PROP, System.getProperty("user.dir"))),
			"services");

	private static Properties objectDefaults = new Properties() {
		{
			put("org.universAAL.middleware.serialization.MessageContentSerializer",
					"(&(objectClass=org.universAAL.middleware.serialization.MessageContentSerializer) "
							+ "(|(Content-Type=text/turtle) (Content-Type=application/x-turtle)))");
			put("org.universAAL.middleware.serialization.MessageContentSerializerEx",
					"(&(objectClass=org.universAAL.middleware.serialization.MessageContentSerializerEx) "
							+ "(|(Content-Type=text/turtle) (Content-Type=application/x-turtle)))");
		}
	};

	private static Properties attributeMap = new Properties() {
		{

			put(Attributes.CONTAINER_PLATFORM_VERSION, "java.specification.version");

			boolean isKaraf = null != System.getProperty("karaf.home");
			if (isKaraf) {
				put(Attributes.CONTAINER_VERSION, System.getProperty("karaf.version"));
			} else {
				put(Attributes.CONTAINER_VERSION, Constants.FRAMEWORK_VERSION);
			}

			put(Attributes.CONTAINER_OS_NAME, Constants.FRAMEWORK_OS_NAME);
			put(Attributes.CONTAINER_OS_VERSION, Constants.FRAMEWORK_OS_VERSION);
			put(Attributes.CONTAINER_OS_ARCHITECTURE, Constants.FRAMEWORK_PROCESSOR);
			put(Attributes.CONTAINER_EE_NAME, Constants.FRAMEWORK_VENDOR);
			put(Attributes.CONTAINER_EE_VERSION, Constants.FRAMEWORK_VERSION);
			put(Attributes.CONTAINER_EE_ARCHITECTURE, Constants.FRAMEWORK_PROCESSOR);
			put(OSGiAttributes.OSGI_NAME, Constants.FRAMEWORK_VENDOR);
			put(OSGiAttributes.OSGI_VERSION, Constants.FRAMEWORK_VERSION);
			put(OSGiAttributes.OSGI_ARCHITECTURE, Constants.FRAMEWORK_PROCESSOR);
		}
	};
	private ArrayList confFiles = new ArrayList(2);
	private Hashtable<String, ServiceRegistration> sharedObjects = new Hashtable<String, ServiceRegistration>();

	OSGiModuleContext(BundleContext bc) {
		bundle = bc;
		logger = LoggerFactory.getLogger("org.universAAL." + bc.getBundle().getSymbolicName());
	}

	private static String defaultFilter(String interf) {
		String df = System.getProperty(interf + ".defaultFilter");
		return df == null ? objectDefaults.getProperty(interf) : df;
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

		ServiceReference sr;
		String defFilter = OSGiModuleContext.defaultFilter(className);
		if (defFilter != null) {
			try {
				ServiceReference[] srs = bundle.getAllServiceReferences(className, defFilter);
				sr = srs[0];
			} catch (Exception e) {
				LogUtils.logWarn(Activator.mc, getClass(), "fetchObject",
						new Object[] { "unable to retrieve default, results may vary" }, e);
				sr = bundle.getServiceReference(className);
			}
		} else {
			sr = bundle.getServiceReference(className);
		}
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
			ArrayList<Object> aresult = new ArrayList<Object>();

			// Add the results
			for (int i = 0; i < srs.length; i++) {
				Object o = bundle.getService(srs[i]);
				if (!aresult.contains(o))
					aresult.add(o);
			}

			// find the defaults
			ArrayList<Object> dobject = new ArrayList<Object>();
			String df = OSGiModuleContext.defaultFilter(className);
			if (df != null) {
				try {
					ServiceReference[] dsrs = bundle.getAllServiceReferences(className, df);
					for (int i = 0; i < dsrs.length; i++) {
						dobject.add(bundle.getService(dsrs[i]));
					}
				} catch (InvalidSyntaxException e) {
					LogUtils.logWarn(Activator.mc, getClass(), "fetchObject",
							new Object[] { "unable to retrieve default, results may vary" }, e);
				}
			}
			// move defaults to first
			for (Object o : dobject) {
				if (aresult.contains(o)) {
					aresult.remove(o);
					aresult.add(0, o);
				}
			}

			return aresult.toArray(new Object[aresult.size()]);
		}
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attrName) {
		if (attrName == null)
			return null;

		if (attrName == Attributes.CONTAINER_NAME)
			if (null != System.getProperty("karaf.home")) {
				return "karaf";
			} else {
				return "osgi";
			}

		if (Attributes.CONTAINER_PLATFORM_NAME == attrName)
			return "java";

		if (attributeMap.contains(attrName)) {
			attrName = attributeMap.getProperty(attrName);
		}

		return (extension.contains(attrName)) ? extension.get(attrName)
				: OSGiModuleContext.staticExtension.contains(attrName) ? OSGiModuleContext.staticExtension.get(attrName)
						: bundle.getProperty(attrName);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#getContainer()
	 */
	public Container getContainer() {
		return OSGiContainer.THE_CONTAINER;
	}

	public String getID() {
		return bundle.getBundle().getSymbolicName();
	}

	public OSGiModuleContext installBundle(String location) {
		try {
			Bundle b = bundle.installBundle(location);
			return new OSGiModuleContext(b.getBundleContext());
		} catch (Exception e) {
			logError(this.getClass().getName() + "installBundle", "Exception while installing bundle at " + location,
					e);
			return null;
		}
	}

	public OSGiModuleContext installBundle(String location, InputStream is) {
		try {
			Bundle b = bundle.installBundle(location, is);
			return new OSGiModuleContext(b.getBundleContext());
		} catch (Exception e) {
			logError(this.getClass().getName() + "installBundle", "Exception while installing bundle at " + location,
					e);
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
		logger.debug(tag + ": " + message, t);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#logError(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logError(String tag, String message, Throwable t) {
		logger.error(tag + ": " + message, t);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#logInfo(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logInfo(String tag, String message, Throwable t) {
		logger.info(tag + ": " + message, t);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#logWarn(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logWarn(String tag, String message, Throwable t) {
		logger.warn(tag + ": " + message, t);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#logTrace(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logTrace(String tag, String message, Throwable t) {
		logger.trace(tag + ": " + message, t);
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
			configFileParams[0] = new File(servicesConfHome, configFileParams[0].toString() + ".properties");
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
		ServiceRegistration sr = bundle.registerService(xface, obj, props);
		sharedObjects.put(xface, sr);
	}

	public void shareObject(String[] xface, Object obj, Dictionary props) {
		ServiceRegistration sr = bundle.registerService(xface, obj, props);
		for (String xf : xface) {
			sharedObjects.put(xf, sr);
		}
	}

	public void removeSharedObject(String[] xface, Object obj, Dictionary props) {
		for (String xf : xface) {
			sharedObjects.get(xf).unregister();
			sharedObjects.remove(xf);
		}
	}

	public void removeSharedObject(String xface, Object obj, Dictionary props) {
		sharedObjects.get(xface).unregister();
		sharedObjects.remove(xface);
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

	private String[] getBundleList() {
		Bundle[] bundles = bundle.getBundles();
		String[] values = new String[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			values[i] = bundles[i].getSymbolicName() + "-" + bundles[i].getHeaders(Constants.BUNDLE_VERSION);
		}
		return values;
	}

	public Object getProperty(String name) {
		return getProperty(name, null);
	}

	public Object getProperty(String name, Object def) {
		Object value = null;

		value = getAttribute(name);
		if (value != null)
			return value;

		value = bundle.getProperty(name);
		if (value != null)
			return value;

		value = System.getProperty(name);
		if (value != null)
			return value;

		value = System.getenv(name);
		if (value != null)
			return value;

		return def;
	}

	public String getManifestEntry(String name) {
		return (String) bundle.getBundle().getHeaders().get(name);
	}

	public String getManifestEntry(String manifest, String name) {
		URL url = bundle.getBundle().getEntry(manifest);
		if (url == null)
			return null;
		Manifest man;
		try {
			man = new Manifest(url.openStream());
		} catch (IOException e) {
			// e.printStackTrace();
			return null;
		}
		return man.getMainAttributes().getValue(name);
	}

	public File getConfigHome() {
		return new File(
				System.getProperty(CONF_ROOT_PROP, System.getProperty(CONF_KARAF_PROP, System.getProperty("user.dir"))),
				getID());
	}

	public File getDataFolder() {
		// XXX maybe set another system property to point to the main data
		// folder
		return new File(System.getProperty(WORK_KARAF_PROP, WORK_ROOT_DEFAULT), getID());
	}

	/** {@inheritDoc} */
	public boolean isLogErrorEnabled() {
		return logger.isErrorEnabled();
	}

	/** {@inheritDoc} */
	public boolean isLogWarnEnabled() {
		return logger.isWarnEnabled();
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
}
