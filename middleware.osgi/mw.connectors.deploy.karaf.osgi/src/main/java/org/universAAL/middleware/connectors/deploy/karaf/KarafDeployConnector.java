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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.connectors.DeployConnector;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
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
    private final static String UAAL_DEPLOY_DIR = System.getProperty(
            "org.universeAAL.deploy.connector.deploydir", "uAAL"
                    + File.pathSeparator + "deploy");
    private final static String UAAL_TMP_DIR = System.getProperty(
            "org.universeAAL.deploy.connector.tmpdir", "uAAL"
                    + File.pathSeparator + "tmp");
    private static final String JAR_EXTENSION = "jar";

    // JAXB
    private JAXBContext jc;
    private JAXBContext jcKaraf;
    private Unmarshaller unmarshaller;
    private Unmarshaller unmarshallerKaraf;
    private Marshaller marshaller;
    private Marshaller marshallerKaraf;
    private Properties registry;

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
            if (entry != null) {
                OutputStream out = null;
                out = new FileOutputStream(new File(dir, entry.getName()));

                // Transfer bytes from the ZIP file to the output file
                byte[] buf = new byte[1024];
                int len;
                while ((len = zipFile.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
            } else {
                end = true;
            }
        }

    }


    private String getInstalledKarafFile(UAPPCard card) throws IOException{
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
        if ( karFile == null ) {
            registry.remove(key);
        } else {
            registry.setProperty(key, karFile);
        }
        FileOutputStream fos = new FileOutputStream(new File(UAAL_DEPLOY_DIR,
                "deploy.registry"));
        registry.store(fos, "universAAL Installation registry, format is serviceId:appId:partId=<path-to-karaf-file>");
        fos.flush();
        fos.close();
    }

    private Properties getInstallationRegistry() throws IOException {
        if (registry == null) {
            registry = new Properties();
            registry.load(new FileInputStream(new File(UAAL_DEPLOY_DIR,
                    "deploy.registry")));
        }
        return registry;
    }

    public void installPart(File zipfile, UAPPCard card) {
        UAPPPartStatus result = UAPPPartStatus.PART_NOT_INSTALLED;
        try {
            result = m_installPart(zipfile, card);
        } catch (Exception ex) {

        }
        synchronized (this) {
            final ControlBroker broker = getControlBroker();
            if (broker != null) {
                broker.notifyRequestToInstallPart(card, card.getPartId(),
                        result);
            }
        }
    }

    public UAPPPartStatus m_installPart(File zipfile, UAPPCard card) {
        final String METHOD = "installPart";
        Part applicationPart = null;
        try {
            LogUtils.logInfo(context, KarafDeployConnector.class, METHOD,
                    new Object[] { "Installing application part for uAAP:"
                            + card.toString() }, null);
            File parentPartDir = new File(UAAL_TMP_DIR);
            unzipTo(zipfile, parentPartDir);
            // check if I find a KAR archive
            File[] listFiles = parentPartDir.listFiles();
            for (File file : listFiles) {
                String name = file.getName();
                if ( name.endsWith(KAR_EXTENSION) == false)
                    continue;
                String uniquePrefix = installFile(file);
                if ( uniquePrefix == null )
                    return UAPPPartStatus.PART_NOT_INSTALLED;

                updateInstalltionRegistry(card, uniquePrefix);
            }
        } catch (Exception e) {
            LogUtils.logError(context, KarafDeployConnector.class, METHOD,
                    new Object[] { "Error during installation of uAPP: " + e },
                    e);
            return UAPPPartStatus.PART_NOT_INSTALLED;
        }
        return UAPPPartStatus.PART_INSTALLED;
    }

    private String installFile(File file) {
        final String METHOD = "installFile";
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."+KAR_EXTENSION));
        String uniquePrefix = name + System.currentTimeMillis();
        String karfile = uniquePrefix
                + "." + KAR_EXTENSION;
        String jarfile = uniquePrefix
                + "." + JAR_EXTENSION;
        // copy kar file in the deploy dir
        boolean result = file.renameTo(new File(KAR_DEPLOY_DIR,
                karfile));
        if (result == false) {
            LogUtils.logError(context, KarafDeployConnector.class,
                    METHOD,
                    new Object[] { "Error during KAR installation of file "
                            + file + " as " + uniquePrefix + "." + KAR_EXTENSION  }, null);
            return null;
        }

        LogUtils.logInfo(context, KarafDeployConnector.class, METHOD,
                new Object[] { "Application part installed for uAAP:"
                        }, null);
        File jar = new File( file.getAbsolutePath(), fileName + "." + JAR_EXTENSION );
        result = jar.renameTo(new File(KAR_DEPLOY_DIR,
                jarfile));
        if (result == false) {
            LogUtils.logError(context, KarafDeployConnector.class,
                    METHOD,
                    new Object[] { "Error during JAR installation of file "
                            + jar + " as " + uniquePrefix + "." + JAR_EXTENSION  }, null);
            return null;
        }
        return uniquePrefix;
    }

    public void dispose() {
        context.getContainer().removeSharedObjectListener(this);

    }

    public void uninstallPart(UAPPCard card) {
        UAPPPartStatus result = UAPPPartStatus.PART_NOT_INSTALLED;
        try {
            result = m_uninstallPart(card);
        } catch (Exception ex) {

        }
        synchronized (this) {
            final ControlBroker broker = getControlBroker();
            if (broker != null) {
                broker.notifyRequestToInstallPart(card, card.getPartId(),
                        result);
            }
        }
    }

    private UAPPPartStatus m_uninstallPart(UAPPCard card) throws IOException {
        String uniquePrefix = getInstalledKarafFile(card);
        if ( uniquePrefix == null ) {
            return UAPPPartStatus.PART_NOT_INSTALLED;
        }
        File karafFile = new File(KAR_DEPLOY_DIR, uniquePrefix + "." + KAR_EXTENSION);
        if ( karafFile.delete() == false ) {
            return UAPPPartStatus.PART_NOT_UNINSTALLED;
        }
        File jarFile = new File(KAR_DEPLOY_DIR, uniquePrefix + "." + JAR_EXTENSION);
        if ( jarFile.delete() == false ) {
            return UAPPPartStatus.PART_NOT_UNINSTALLED;
        }
        updateInstalltionRegistry(card, null);
        return UAPPPartStatus.PART_UNINSTALLED;
    }
}
