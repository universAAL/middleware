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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.Repository;
import org.apache.karaf.features.FeaturesService.Option;
import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.connectors.DeployConnector;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.mpa.UAPPCard;
import org.universAAL.middleware.interfaces.mpa.UAPPPartStatus;
import org.universAAL.middleware.interfaces.mpa.model.DeploymentUnit;
import org.universAAL.middleware.interfaces.mpa.model.FeaturesRoot;
import org.universAAL.middleware.interfaces.mpa.model.ObjectFactory;
import org.universAAL.middleware.interfaces.mpa.model.Part;
import org.universAAL.middleware.interfaces.utils.Util;

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
    private FeaturesService karafFeatureService;
    private ControlBroker controlBroker;

    private boolean initialized = false;
    private static String UAPP_SUFFIX = ".uapp";
    private static String KAR_EXTENSION = "kar";
    private static String KAR_DEPLOY_DIR = "deploy";
    // JAXB
    private JAXBContext jc;
    private JAXBContext jcKaraf;
    private Unmarshaller unmarshaller;
    private Unmarshaller unmarshallerKaraf;
    private Marshaller marshaller;
    private Marshaller marshallerKaraf;

    public boolean init() {
	if (!initialized) {

	    Object[] cBrokers = context.getContainer().fetchSharedObject(
		    context,
		    new Object[] { ControlBroker.class.getName().toString() },
		    this);
	    if (cBrokers != null) {
		LogUtils.logDebug(context, KarafDeployConnector.class,
			"DeployManagerImpl",
			new Object[] { "Found  ContextBrokers..." }, null);
		if (cBrokers[0] instanceof ControlBroker)
		    controlBroker = (ControlBroker) cBrokers[0];
		else {
		    initialized = false;
		    return initialized;
		}
	    } else {
		LogUtils.logDebug(context, KarafDeployConnector.class,
			"DeployManagerImpl",
			new Object[] { "No ContextBroker found" }, null);
		initialized = false;
		return initialized;
	    }

	    LogUtils.logDebug(context, KarafDeployConnector.class,
		    "KarafDeployConnector",
		    new Object[] { "fetching the Karaf Feature Service..." },
		    null);
	    Object[] kFServices = context.getContainer()
		    .fetchSharedObject(
			    context,
			    new Object[] { FeaturesService.class.getName()
				    .toString() }, this);
	    if (kFServices != null) {
		LogUtils.logDebug(context, KarafDeployConnector.class,
			"KarafDeployConnector",
			new Object[] { "karafFeatureService found..." }, null);
		if (kFServices[0] instanceof FeaturesService) {
		    karafFeatureService = (FeaturesService) kFServices[0];
		}
	    } else {
		LogUtils.logWarn(context, KarafDeployConnector.class,
			"KarafDeployConnector",
			new Object[] { "karafFeatureService not found..." },
			null);
		initialized = false;
		return initialized;
	    }

	    initialized = true;
	}
	return initialized;

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

    public void sharedObjectAdded(Object arg0, Object arg1) {
	if (arg0 instanceof FeaturesService) {
	    LogUtils.logDebug(context, KarafDeployConnector.class,
		    "KarafDeployConnector",
		    new Object[] { "FeaturesService added" }, null);
	    this.karafFeatureService = (FeaturesService) arg0;
	} else if (arg0 instanceof ControlBroker) {
	    LogUtils.logDebug(context, KarafDeployConnector.class,
		    "DeployManagerImpl",
		    new Object[] { "ControlBroker service added" }, null);
	    this.controlBroker = (ControlBroker) arg0;

	}

    }

    public void sharedObjectRemoved(Object arg0) {
	// TODO Auto-generated method stub

    }

    public void installPart(File applicationFilePart, UAPPCard uAPPCard) {
	// String installationDir = applicationFilePart.getParent();
	Part applicationPart = null;
	if (init()) {
	    try {
		LogUtils.logInfo(context, KarafDeployConnector.class,
			"KarafDeployConnector",
			new Object[] { "Installing application part for uAAP:"
				+ uAPPCard.toString() }, null);
		ZipInputStream zipFile = new ZipInputStream(
			new FileInputStream(applicationFilePart));
		ZipEntry zipEntry = null;
		boolean end = false;
		while (!end) {
		    ZipEntry entry = zipFile.getNextEntry();
		    if (entry != null) {
			String outFilename = applicationFilePart.getParent()+File.separatorChar+entry.getName();
			OutputStream out = new FileOutputStream(outFilename);

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
		// check if I find a KAR archive
		File parentPartDir = applicationFilePart.getParentFile();
		if (parentPartDir.canRead()) {
		    File[] listFiles = parentPartDir.listFiles();
		    for (File file : listFiles) {
			if (file.getName().endsWith(KAR_EXTENSION)) {
			    // copy kar file in the deploy dir
			    file.renameTo(new File(KAR_DEPLOY_DIR+File.separatorChar+file.getName()));
			}
			LogUtils.logInfo(context, KarafDeployConnector.class,
				"KarafDeployConnector",
				new Object[] { "Application part installed for uAAP:"
					+ uAPPCard.toString() }, null);
		    }
		}

		/*
		 * 
		 * applicationPart = (Part) unmarshaller.unmarshal(Util.getFile(
		 * UAPP_SUFFIX, new File(".").toURI()));
		 * 
		 * StringWriter writer = null; for (DeploymentUnit dUnit :
		 * applicationPart.getDeploymentUnit()) { if
		 * (dUnit.isSetContainerUnit() &&
		 * dUnit.getContainerUnit().isSetKaraf() &&
		 * dUnit.getContainerUnit().getKaraf() .isSetFeatures()) {
		 * writer = new StringWriter(); // NamespacePrefixMapper m = new
		 * PreferredMapper(); // marshaller.setProperty(
		 * "com.sun.xml.internal.bind.namespacePrefixMapper", // m);
		 * 
		 * JAXBElement p = new JAXBElement<FeaturesRoot>( new QName(
		 * "http://karaf.apache.org/xmlns/features/v1.0.0", "features"),
		 * FeaturesRoot.class, dUnit .getContainerUnit().getKaraf()
		 * .getFeatures()); //
		 * marshaller.marshal(dUnit.getContainerUnit(
		 * ).getKaraf().getFeatures(), // writer); marshaller.marshal(p,
		 * writer);
		 * 
		 * } } // NamespacePrefixMapper m = new Prefer InputStream
		 * karafStream = new ByteArrayInputStream(writer
		 * .toString().getBytes("UTF-8")); //
		 * org.universAAL.middleware.connectors
		 * .deploy.karaf.core.model.FeaturesRoot // fRoot = //
		 * (org.universAAL
		 * .middleware.connectors.deploy.karaf.core.model.
		 * FeaturesRoot)unmarshallerKaraf.unmarshal(karafStream);
		 * 
		 * javax.xml.bind.JAXBElement stream = (JAXBElement)
		 * unmarshallerKaraf .unmarshal(karafStream); File localRepo =
		 * new File("locaRepo.xml"); marshallerKaraf.marshal(stream,
		 * localRepo);
		 * 
		 * karafFeatureService.addRepository(localRepo.toURI());
		 * localRepo.toString();
		 * 
		 * Repository[] karafRepositories = karafFeatureService
		 * .listRepositories(); for (Repository repo :
		 * karafRepositories) { if
		 * (repo.getURI().compareTo(localRepo.toURI()) == 0) { Feature[]
		 * featuresToInstall = repo.getFeatures(); // install all the
		 * features on the Karaf container List listOffeaturesToInstall
		 * = Arrays .asList(featuresToInstall); Set
		 * setOfFeaturesToInstall = new HashSet<Feature>(
		 * listOffeaturesToInstall);
		 * karafFeatureService.installFeatures( setOfFeaturesToInstall,
		 * EnumSet.noneOf(Option.class)); } }
		 */
		/* send the installation result to the Deploy Manager in the AAL
		controlBroker.notifyRequestToInstallPart(uAPPCard,
			applicationPart.getPartId(),
			UAPPPartStatus.PART_INSTALLED);*/

	    } catch (Exception e) {
		LogUtils.logError(
			context,
			KarafDeployConnector.class,
			"KarafDeployConnector",
			new Object[] { "Error during installation of uAPP: " + e },
			null);
		// send the installation result to the Deploy Manager in the AAL
		if (applicationPart != null)
		    controlBroker.notifyRequestToInstallPart(uAPPCard,
			    applicationPart.getPartId(),
			    UAPPPartStatus.PART_NOT_INSTALLED);
		else
		    controlBroker.notifyRequestToInstallPart(uAPPCard, "",
			    UAPPPartStatus.PART_NOT_INSTALLED);
	    }
	} else {
	    LogUtils.logInfo(
		    context,
		    KarafDeployConnector.class,
		    "KarafDeployConnector",
		    new Object[] { "Deploy Connector not initialized. Aborting..." },
		    null);
	    // send the installation result to the Deploy Manager in the AAL
	    if (applicationPart != null)
		controlBroker.notifyRequestToInstallPart(uAPPCard,
			applicationPart.getPartId(),
			UAPPPartStatus.PART_NOT_INSTALLED);
	    else
		controlBroker.notifyRequestToInstallPart(uAPPCard, "",
			UAPPPartStatus.PART_NOT_INSTALLED);

	}
	/*
	 * 
	 * 
	 * if(!serializedPart.isEmpty()){
	 * context.logInfo("Installing MPA application part", null); try {
	 * InputStream partStream = new
	 * ByteArrayInputStream(serializedPart.getBytes("UTF-8")); Part part=
	 * (Part) unmarshaller.unmarshal(partStream); for(DeploymentUnit
	 * deployementUnit: part.getDeploymentUnit()){
	 * if(deployementUnit.getContainerUnit() != null &&
	 * deployementUnit.getContainerUnit().getKaraf()!= null){ Karaf karaf =
	 * deployementUnit.getContainerUnit().getKaraf(); List<Serializable>
	 * list = karaf.getFeatures().getRepositoryOrFeature(); for(Serializable
	 * element: list){ if(element instanceof String){ //found a repository
	 * in the element add it to the Karaf container String repoUrl =
	 * (String)element; URI repository = new URI(repoUrl);
	 * karafFeatureService.addRepository(repository); Repository[]
	 * karafRepositories = karafFeatureService.listRepositories();
	 * for(Repository repo : karafRepositories){
	 * if(repo.getURI().compareTo(repository) ==0){ Feature[]
	 * featuresToInstall = repo.getFeatures(); //install all the features on
	 * the Karaf container List listOffeaturesToInstall =
	 * Arrays.asList(featuresToInstall); Set setOfFeaturesToInstall = new
	 * HashSet<Feature>(listOffeaturesToInstall);
	 * karafFeatureService.installFeatures(setOfFeaturesToInstall,
	 * EnumSet.noneOf(Option.class)); } } } } } } }catch (Exception e) {
	 * LogUtils.logError(context,
	 * KarafDeployConnector.class,"KarafDeployConnector", new Object[]
	 * {"Error during installation of MPA: "+e, null); }
	 * context.logInfo("MPA application part installed.", null); }
	 */
    }

    /*
     * private Set<Feature> adjustBundleURL(Set<Feature> featuresToInstall,
     * String installationDir){ Set<Feature> modifiedFeature = new
     * HashSet<Feature>(featuresToInstall);
     * 
     * for(Feature feature: featuresToInstall){ if(feature.getBundles().size() >
     * 0){ for(BundleInfo bundle: feature.getBundles()){ if(bundle != null &&
     * bundle.getLocation().startsWith("file:")){ String[] tmp =
     * bundle.getLocation().split("file:"); String bundleName =
     * tmp[1].substring(1); String newLocaion =
     * "file:"+File.separatorChar+installationDir+File.separatorChar+bundleName;
     * 
     * } }
     * 
     * } }
     * 
     * return null; }
     */

    public void uninstallPart(File applicationPart) {
	// TODO Auto-generated method stub

    }

    public void dispose() {
	context.getContainer().removeSharedObjectListener(this);

    }
}

/*
 * } class PreferredMapper extends NamespacePrefixMapper {
 * 
 * public String getPreferredPrefix(String namespaceUri, String suggestion,
 * boolean requirePrefix) {
 * if(namespaceUri.equals("http://karaf.apache.org/xmlns/features/v1.0.0"))
 * return ""; return "ns2"; } }
 */
