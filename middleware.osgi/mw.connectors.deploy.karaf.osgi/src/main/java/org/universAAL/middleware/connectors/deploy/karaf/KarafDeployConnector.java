/*
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
package org.universAAL.middleware.connectors.deploy.karaf;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.brokers.control.ExceptionUtils;
import org.universAAL.middleware.connectors.DeployConnector;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.mpa.UAPPCard;
import org.universAAL.middleware.interfaces.mpa.UAPPPartStatus;
import org.universAAL.middleware.interfaces.mpa.model.ObjectFactory;
import org.universAAL.middleware.interfaces.mpa.model.Part;

//import com.sun.xml.bind.marshaller.NamespacePrefixMapper;;

/**
 * Implementation of the deploy Connector for the Karaf OSGi implementation
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class KarafDeployConnector implements DeployConnector,
	SharedObjectListener {

    private String description;
    private String name;
    private String provider;
    private String version;

    private ModuleContext context;

    // Karaf services for installing artefacts
    private ControlBroker controlBroker = null;

    private boolean initialized = false;
    private final static String UAPP_SUFFIX = ".uapp";
    private final static String KAR_EXTENSION = "kar";
    private final static String KAR_DEPLOY_DIR = System.getProperty(
	    "org.universeAAL.connector.karaf.deploydir", "deploy");
    private static final String JAR_EXTENSION = "jar";

    // JAXB
    private JAXBContext jc;
    private JAXBContext jcKaraf;
    private Unmarshaller unmarshaller;
    private Unmarshaller unmarshallerKaraf;
    private Marshaller marshaller;
    private Marshaller marshallerKaraf;
    private Properties registry;
    private BundleConfigHome moduleConfigHome;

    private ControlBroker getControlBroker() {
	synchronized (this) {
	    if (controlBroker != null) {
		return controlBroker;
	    }
	    Object[] cBrokers = context.getContainer().fetchSharedObject(
		    context,
		    new Object[] { ControlBroker.class.getName().toString() },
		    this);
	    if (cBrokers != null) {
		LogUtils.logDebug(context, KarafDeployConnector.class,
			"DeployManagerImpl",
			new Object[] { "Found  ContextBrokers..." }, null);
		if (cBrokers[0] instanceof ControlBroker) {
		    controlBroker = (ControlBroker) cBrokers[0];
		} else {
		    return null;
		}
	    }
	    return controlBroker;
	}
    }

    public boolean init() {
	return getControlBroker() != null;
    }

    public KarafDeployConnector(ModuleContext context) {
	this.context = context;
	moduleConfigHome = new BundleConfigHome("mw.connectors.deploy.karaf");
	try {
	    jc = JAXBContext.newInstance(ObjectFactory.class);

	    jcKaraf = JAXBContext
		    .newInstance(org.universAAL.middleware.connectors.deploy.karaf.model.ObjectFactory.class);
	    unmarshallerKaraf = jcKaraf.createUnmarshaller();
	    unmarshaller = jc.createUnmarshaller();
	    marshaller = jc.createMarshaller();
	    marshallerKaraf = jcKaraf.createMarshaller();
	} catch (JAXBException e) {
	    LogUtils.logError(
		    context,
		    KarafDeployConnector.class,
		    "KarafDeployConnector",
		    new Object[] { "Error during Deploy Karaf parser intialization: "
			    + e.toString() }, null);
	}
    }

    public String getDescription() {
	return this.description;
    }

    public String getName() {
	return this.name;
    }

    public String getProvider() {
	return this.provider;
    }

    public String getVersion() {
	return this.version;
    }

    public void loadConfigurations(Dictionary configurations) {
	LogUtils.logDebug(context, KarafDeployConnector.class,
		"KarafDeployConnector",
		new Object[] { "updating Karaf Deploy Connector properties" },
		null);
	if (configurations == null) {
	    LogUtils.logDebug(
		    context,
		    KarafDeployConnector.class,
		    "KarafDeployConnector",
		    new Object[] { "Karaf Deploy Connector properties are null" },
		    null);
	    return;
	}
	try {
	    this.name = (String) configurations
		    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_NAME);
	    this.version = (String) configurations
		    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_VERSION);
	    this.description = (String) configurations
		    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_DESCRIPTION);
	    this.provider = (String) configurations
		    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_PROVIDER);

	} catch (NullPointerException e) {
	    LogUtils.logError(
		    context,
		    KarafDeployConnector.class,
		    "KarafDeployConnector",
		    new Object[] { "Error during Karaf Deploy properties update" },
		    null);
	} catch (Exception e) {
	    LogUtils.logError(
		    context,
		    KarafDeployConnector.class,
		    "KarafDeployConnector",
		    new Object[] { "Error during Karaf Deploy properties update" },
		    null);
	}
	LogUtils.logDebug(context, KarafDeployConnector.class,
		"KarafDeployConnector",
		new Object[] { "Error during Karaf Deploy properties update" },
		null);
    }

    public void sharedObjectAdded(Object service, Object arg1) {
	if (service instanceof ControlBroker) {
	    LogUtils.logDebug(context, KarafDeployConnector.class,
		    "DeployManagerImpl",
		    new Object[] { "ControlBroker service added" }, null);
	    synchronized (this) {
		this.controlBroker = (ControlBroker) service;
	    }
	}

    }

    public void sharedObjectRemoved(Object service) {
	synchronized (this) {
	    if (controlBroker == service) {
		controlBroker = null;
	    }
	}

    }

    private void unzipTo(File zip, File dir) throws IOException {
	dir.mkdirs();
	ZipInputStream zipFile = new ZipInputStream(new FileInputStream(zip));
	ZipEntry zipEntry = null;
	boolean end = false;
	while (!end) {
	    ZipEntry entry = zipFile.getNextEntry();
	    if (entry == null) {
		end = true;
		continue;
	    }
	    if (entry.isDirectory()) {
		if (new File(dir, entry.getName()).mkdirs() == false) {
		    throw new IOException(
			    "ZipEntry was a directory but unable to create subfolders "
				    + entry.getName() + " inside "
				    + dir.getAbsolutePath());
		}
		continue;
	    }
	    final int dirIndex = entry.getName()
		    .lastIndexOf(File.separatorChar);
	    while (dirIndex > 0) {
		final String dirName = entry.getName().substring(0, dirIndex);
		File parentDir = new File(dir, dirName);
		if (parentDir.exists() && parentDir.isDirectory()) {
		    break;
		}
		boolean created = parentDir.mkdirs();
		if (created) {
		    break;
		}
		LogUtils.logInfo(
			context,
			KarafDeployConnector.class,
			"unzipTo",
			new Object[] { "Unable to create subfolders ",
				entry.getName(), " into ",
				dir.getAbsolutePath(),
				" with mkdirs() method, trying different approch." },
			null);

		StringTokenizer dirs = new StringTokenizer(dirName,
			File.separator);
		File parent = dir;
		dir.mkdirs();
		created = true;
		while (dirs.hasMoreTokens()) {
		    String curDir = dirs.nextToken();
		    parent = new File(parent, curDir);
		    if (parent.exists()) {
			parent.delete();
			LogUtils.logInfo(
				context,
				KarafDeployConnector.class,
				"unzipTo",
				new Object[] { curDir,
					" exists and is not a directory, it may be a leftover, we are deleting it" },
				null);
		    }
		    if (parent.mkdir() == false
			    && !(parent.exists() && parent.isDirectory())) {
			created = false;
			LogUtils.logInfo(context, KarafDeployConnector.class,
				"unzipTo", new Object[] {
					"Unable to create subfolders ", curDir,
					" into ", parent.getAbsolutePath(), },
				null);
			break;
		    }
		}
		if (created == false) {
		    throw new IOException("Unable to create subfolders "
			    + dirName + " inside " + dir.getAbsolutePath());
		}
		break;
	    }
	    OutputStream out = null;
	    out = new FileOutputStream(new File(dir, entry.getName()));

	    // Transfer bytes from the ZIP file to the output file
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = zipFile.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	    out.close();
	}

    }

    private String getInstalledKarafFile(UAPPCard card) throws IOException {
	registry = getInstallationRegistry();
	String key = card.getServiceId() + ":" + card.getId() + ":"
		+ card.getPartId();
	return registry.getProperty(key);
    }

    private void updateInstalltionRegistry(UAPPCard card, String karFile)
	    throws IOException {
	registry = getInstallationRegistry();
	String key = card.getServiceId() + ":" + card.getId() + ":"
		+ card.getPartId();
	if (karFile == null) {
	    registry.remove(key);
	} else {
	    registry.setProperty(key, karFile);
	}
	OutputStream fos = moduleConfigHome
		.getConfFileAsOutputStream("deploy.registry");
	registry.store(
		fos,
		"universAAL Installation registry, format is serviceId:appId:partId=<path-to-karaf-file>");
	fos.flush();
	fos.close();
    }

    private Properties getInstallationRegistry() throws IOException {
	if (registry == null) {
	    registry = new Properties();
	    // TODO Using BundleConfigHome
	    InputStream in = moduleConfigHome
		    .getConfFileAsStream("deploy.registry");
	    registry.load(in);
	}
	return registry;
    }

    public void installPart(File zipfile, UAPPCard card) {

	final String name = Thread.currentThread().getName();
	Thread.currentThread().setName("DeployManager[KarafConnector]");

	UAPPPartStatus result = UAPPPartStatus.PART_NOT_INSTALLED;
	try {
	    result = m_installPart(zipfile, card);
	} catch (Exception ex) {
	    if (zipfile == null)
		zipfile = new File("NULL_ZIP_FILE");
	    LogUtils.logError(context, KarafDeployConnector.class,
		    "installPart", new Object[] { "Error installing a "
			    + zipfile.getAbsolutePath() + " due to "
			    + ExceptionUtils.stackTraceAsString(ex) }, ex);
	}
	synchronized (this) {
	    final ControlBroker broker = getControlBroker();
	    if (broker != null) {
		broker.notifyRequestToInstallPart(card, card.getPartId(),
			result);
	    }
	}

	Thread.currentThread().setName(name);
    }

    public UAPPPartStatus m_installPart(File zipfile, UAPPCard card) {
	final String METHOD = "installPart";
	try {
	    LogUtils.logInfo(context, KarafDeployConnector.class, METHOD,
		    new Object[] { "Installing application part for uAAP:"
			    + card.toString() }, null);
	    File parentPartDir = File.createTempFile("part-", "-unzip");
	    parentPartDir.delete();
	    parentPartDir.mkdirs();
	    unzipTo(zipfile, parentPartDir);
	    // check if I find a KAR archive
	    File[] listFiles = parentPartDir.listFiles();
	    File karFile = verifyValidZipContent(listFiles);
	    if (karFile == null) {
		LogUtils.logError(context, KarafDeployConnector.class, METHOD,
			new Object[] { "No valid part for installation" }, null);
		return UAPPPartStatus.PART_MISSING_NEEDED_FILES;
	    }
	    String uniquePrefix = installFile(karFile);
	    installConfiguration(parentPartDir);
	    if (uniquePrefix == null)
		return UAPPPartStatus.PART_NOT_INSTALLED;

	    updateInstalltionRegistry(card, uniquePrefix);
	    return UAPPPartStatus.PART_INSTALLED;
	} catch (Exception e) {
	    LogUtils.logError(context, KarafDeployConnector.class, METHOD,
		    new Object[] { "Error during installation of uAPP: " + e },
		    e);
	    return UAPPPartStatus.PART_NOT_INSTALLED;
	}
    }

    private void installConfiguration(File parentPartDir) {
	final String METHOD = "installConfiguration";
	File[] configs = parentPartDir.listFiles(new FileFilter() {

	    public boolean accept(File pathname) {
		return pathname.isDirectory()
			&& "config".equals(pathname.getName());
	    }
	});
	if (configs == null || configs.length == 0) {
	    LogUtils.logInfo(context, KarafDeployConnector.class, METHOD,
		    "No configuration for this part");
	    return;
	}
	if (configs.length > 1) {
	    LogUtils.logWarn(context, KarafDeployConnector.class, METHOD,
		    "Too many config folders: " + Arrays.toString(configs)
			    + " we are going to install only " + configs[0]);
	    for (int i = 1; i < configs.length; i++) {
		if (configs[i].delete() == false) {
		    configs[i].deleteOnExit();
		}
	    }
	}

	final File configDir = configs[0];
	File dstFolder = new File(moduleConfigHome.getAbsolutePath());
	dstFolder = dstFolder.getParentFile();
	File[] configFiles = configDir.listFiles();
	for (int i = 0; i < configFiles.length; i++) {
	    final File src = configFiles[i];
	    final File dst = new File(dstFolder, configFiles[i].getName());
	    if (dst.exists()) {
		LogUtils.logWarn(
			context,
			KarafDeployConnector.class,
			METHOD,
			new Object[] { "Configuration  ", src.getName(),
				" alrady exists so is not going to be installed " },
			null);
		continue;
	    }
	    if (src.renameTo(dst) == false) {
		LogUtils.logWarn(
			context,
			KarafDeployConnector.class,
			METHOD,
			new Object[] {
				"Unable to install the configuration ",
				src.getName(),
				" by moving: ",
				src.getAbsolutePath() + " -> "
					+ dst.getAbsolutePath() }, null);
	    }
	}
	configDir.delete();

    }

    private File verifyValidZipContent(File[] listFiles) {
	final String METHOD = "verifyValidZipContent";
	int karFiles = 0;
	File validKarafFile = null;
	for (File file : listFiles) {
	    String name = file.getName();
	    if (name.endsWith(KAR_EXTENSION) == false
		    && name.endsWith(JAR_EXTENSION) == false) {
		LogUtils.logWarn(
			context,
			KarafDeployConnector.class,
			METHOD,
			new Object[] { "The part contain unexpected file that will be ignored "
				+ file.getAbsolutePath() }, null);
		continue;
	    } else if (name.endsWith(KAR_EXTENSION) == false
		    && name.endsWith(JAR_EXTENSION) == true) {
		continue;
	    } else if (name.endsWith(KAR_EXTENSION) == true
		    && name.endsWith(JAR_EXTENSION) == false) {
		karFiles++;
	    }

	    final String jarname = extractExentsion(name, KAR_EXTENSION) + "."
		    + JAR_EXTENSION;
	    File jarFile = new File(file.getParent(), jarname);
	    if (jarFile.exists() == true) {
		validKarafFile = file;
	    } else {
		LogUtils.logWarn(
			context,
			KarafDeployConnector.class,
			METHOD,
			new Object[] { "The part has karaf file "
				+ file.getAbsolutePath()
				+ " but it miss the twin JAR file " + jarname },
			null);
	    }
	}
	if (karFiles > 1 && validKarafFile != null) {
	    LogUtils.logWarn(
		    context,
		    KarafDeployConnector.class,
		    METHOD,
		    new Object[] { "The part contains too many karaf file only "
			    + validKarafFile.getAbsolutePath()
			    + " and the twin JAR file will be installed" },
		    null);
	} else if (karFiles > 1 && validKarafFile == null) {
	    LogUtils.logWarn(
		    context,
		    KarafDeployConnector.class,
		    METHOD,
		    new Object[] { "The part contains too many karaf file and none of them with a twin JAR so nothing will be installed" },
		    null);
	} else if (karFiles == 0) {
	    LogUtils.logError(
		    context,
		    KarafDeployConnector.class,
		    METHOD,
		    new Object[] { "The part contains no karaf file nothing will be installed" },
		    null);
	}
	return validKarafFile;
    }

    private String installFile(File file) {
	final String METHOD = "installFile";
	String fileName = file.getName();
	fileName = extractExentsion(fileName, KAR_EXTENSION);

	String uniquePrefix = fileName + "-" + System.currentTimeMillis();
	String karfile = uniquePrefix + "." + KAR_EXTENSION;
	String jarfile = uniquePrefix + "." + JAR_EXTENSION;
	// copy kar file in the deploy dir
	File deployFolder = new File(KAR_DEPLOY_DIR);
	boolean result = file.renameTo(new File(deployFolder, karfile));
	if (result == false) {
	    LogUtils.logError(context, KarafDeployConnector.class, METHOD,
		    new Object[] { "Error during KAR installation of file ",
			    file, " as ", uniquePrefix, ".", KAR_EXTENSION,
			    " in folder ", deployFolder.getAbsolutePath() },
		    null);
	    return null;
	}

	LogUtils.logInfo(context, KarafDeployConnector.class, METHOD,
		new Object[] { "Application part installed for uAAP:" }, null);
	File jar = new File(file.getParent(), fileName + "." + JAR_EXTENSION);
	result = jar.renameTo(new File(KAR_DEPLOY_DIR, jarfile));
	if (result == false) {
	    LogUtils.logError(context, KarafDeployConnector.class, METHOD,
		    new Object[] { "Error during JAR installation of file ",
			    jar, " as ", uniquePrefix, ".", JAR_EXTENSION,
			    " in folder ", deployFolder.getAbsolutePath() },
		    null);
	    return null;
	}
	return uniquePrefix;
    }

    private String extractExentsion(String fn, String ext) {
	return fn = fn.substring(0, fn.lastIndexOf("." + ext));
    }

    public void dispose() {
	context.getContainer().removeSharedObjectListener(this);

    }

    public void uninstallPart(UAPPCard card) {
	final String name = Thread.currentThread().getName();
	Thread.currentThread().setName("DeployManager[KarafConnector]");

	UAPPPartStatus result = UAPPPartStatus.PART_NOT_INSTALLED;
	try {
	    result = m_uninstallPart(card);
	} catch (Exception ex) {
	    LogUtils.logError(
		    context,
		    KarafDeployConnector.class,
		    "installPart",
		    new Object[] { "Error UNinstalling a " + card.getName()
			    + "/" + card.getServiceId() + ":"
			    + card.getPartId() + " due to "
			    + ExceptionUtils.stackTraceAsString(ex) }, ex);
	}
	synchronized (this) {
	    final ControlBroker broker = getControlBroker();
	    if (broker != null) {
		broker.notifyRequestToInstallPart(card, card.getPartId(),
			result);
	    }
	}

	Thread.currentThread().setName(name);
    }

    private UAPPPartStatus m_uninstallPart(UAPPCard card) throws IOException {
	String uniquePrefix = getInstalledKarafFile(card);
	if (uniquePrefix == null) {
	    return UAPPPartStatus.PART_NOT_INSTALLED;
	}
	File karafFile = new File(KAR_DEPLOY_DIR, uniquePrefix + "."
		+ KAR_EXTENSION);
	if (karafFile.delete() == false) {
	    return UAPPPartStatus.PART_NOT_UNINSTALLED;
	}
	File jarFile = new File(KAR_DEPLOY_DIR, uniquePrefix + "."
		+ JAR_EXTENSION);
	if (jarFile.delete() == false) {
	    return UAPPPartStatus.PART_NOT_UNINSTALLED;
	}
	updateInstalltionRegistry(card, null);
	return UAPPPartStatus.PART_UNINSTALLED;
    }
}
