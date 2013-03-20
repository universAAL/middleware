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
package org.universAAL.middleware.managers.deploy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.deploymaneger.uapp.model.AalUapp;
import org.universAAL.middleware.deploymaneger.uapp.model.DeploymentUnit;
import org.universAAL.middleware.deploymaneger.uapp.model.DeploymentUnit.ContainerUnit.Karaf;
import org.universAAL.middleware.deploymaneger.uapp.model.ObjectFactory;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceStatus;
import org.universAAL.middleware.interfaces.mpa.UAPPCard;
import org.universAAL.middleware.interfaces.mpa.UAPPPartStatus;
import org.universAAL.middleware.interfaces.mpa.UAPPStatus;
import org.universAAL.middleware.interfaces.mpa.model.Bundle;
import org.universAAL.middleware.interfaces.mpa.model.Feature;
import org.universAAL.middleware.deploymaneger.uapp.model.Part;
import org.universAAL.middleware.interfaces.utils.Util;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.DeployManager;
import org.universAAL.middleware.managers.api.DeployManagerEventHandler;
import org.universAAL.middleware.managers.api.InstallationResults;
import org.universAAL.middleware.managers.api.UAPPPackage;
import org.universAAL.middleware.managers.deploy.util.Consts;

/**
 * The implementation of the DeployManager
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class DeployManagerImpl implements DeployManager,
	DeployManagerEventHandler, SharedObjectListener, AALSpaceListener {

    private AALSpaceEventHandler aalSpaceEventHandler;
    private AALSpaceManager aalSpaceManager;
    private ControlBroker controlBroker;
    private ModuleContext context;
    private boolean initialized = false;

    // Configuration param configured with default value
    private String uappSuffix = ".uapp";
    private String deployDir = "etc/deploy";
    private String APPLICATION_CONFIGURATION_PATH = "config";
    private String APPLICATION_BINARYPART_PATH = "bin";
    private static String TMP_DEPLOY_DIR = "tmp";

    // JAXB
    private JAXBContext jc;
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;

    // Application Registry. Local and trivial implementation
    // MPA ID, status
    private Map<String, UAPPStatus> registry;
    private boolean isDeployCoordinator = false;

    public DeployManagerImpl(ModuleContext context) {

	this.context = context;
	init();
	loadInstalledApplication();
	registry = new HashMap<String, UAPPStatus>();
	try {
	    jc = JAXBContext.newInstance(ObjectFactory.class);
	    unmarshaller = jc.createUnmarshaller();
	    marshaller = jc.createMarshaller();
	} catch (JAXBException e) {
	    LogUtils.logError(
		    context,
		    DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Error during creation of marshaller: " + e },
		    null);
	}

    }

    private void loadInstalledApplication() {
	// TODO Creating a way for storing the installed application

    }

    private void saveInstalledApplication() {
	// TODO Creating a way for saving the current list of installed
	// application
    }

    public boolean init() {
	if (!initialized) {

	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Initializing the DeployManager..." }, null);

	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "fetching the AALSpaceManager..." }, null);
	    Object[] aalManagers = context.getContainer()
		    .fetchSharedObject(
			    context,
			    new Object[] { AALSpaceManager.class.getName()
				    .toString() }, this);
	    if (aalManagers != null) {
		aalSpaceManager = (AALSpaceManager) aalManagers[0];
		aalSpaceManager.addAALSpaceListener(this);

		// check if I'm the deploy coordinator
		if (aalSpaceManager.getAALSpaceDescriptor() != null
			&& aalSpaceManager
				.getAALSpaceDescriptor()
				.getDeployManager()
				.getPeerID()
				.equals(aalSpaceManager.getmyPeerCard()
					.getPeerID())) {
		    isDeployCoordinator = true;
		}
	    } else {
		LogUtils.logDebug(context, DeployManagerImpl.class,
			"DeployManagerImpl",
			new Object[] { "No AALSpaceManagers found" }, null);
		initialized = false;
		return initialized;
	    }

	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "fetching the AALSpaceEventHandler..." },
		    null);
	    Object[] aalEventHandlers = context.getContainer()
		    .fetchSharedObject(
			    context,
			    new Object[] { AALSpaceManager.class.getName()
				    .toString() }, this);
	    if (aalEventHandlers != null) {
		aalSpaceEventHandler = (AALSpaceEventHandler) aalEventHandlers[0];

	    } else {
		LogUtils.logDebug(context, DeployManagerImpl.class,
			"DeployManagerImpl",
			new Object[] { "No AALSpaceEventHandler found" }, null);
		initialized = false;
		return initialized;
	    }

	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "fetching the ControlBroker..." }, null);
	    Object[] cBrokers = context.getContainer().fetchSharedObject(
		    context,
		    new Object[] { ControlBroker.class.getName().toString() },
		    this);
	    if (cBrokers != null) {
		LogUtils.logDebug(context, DeployManagerImpl.class,
			"DeployManagerImpl",
			new Object[] { "Found  ContextBrokers..." }, null);
		if (cBrokers[0] instanceof ControlBroker)
		    controlBroker = (ControlBroker) cBrokers[0];
		if (aalSpaceManager.getAALSpaceDescriptor() == null) {
		    initialized = false;
		    return initialized;
		}
	    } else {
		LogUtils.logDebug(context, DeployManagerImpl.class,
			"DeployManagerImpl",
			new Object[] { "No ContextBroker found" }, null);
		initialized = false;
		return initialized;
	    }

	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "DeployManager initialized" }, null);

	} else {
	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "DeployManager already initialized" }, null);
	}

	return initialized;
    }

    public InstallationResults requestToUninstall(String serviceId, String id) {
	throw new RuntimeException("Method not implemented yet");
	// TODO Creating the method
    }

    public InstallationResults requestToInstall(UAPPPackage application) {

	// checks
	// 1 - get the MPA file
	if (application == null) {
	    LogUtils.logWarn(
		    context,
		    DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "The application object is null...aborting" },
		    null);
	    return InstallationResults.UAPP_URI_INVALID;
	}

	final Map<PeerCard, Part> layout = application.getDeploy();
	String applicationFolderPAth = application.getFolder().toString()
		+ File.separatorChar + APPLICATION_CONFIGURATION_PATH;
	URI applicationConfigurationFolder = null;
	try {
	    applicationConfigurationFolder = new URI(applicationFolderPAth);
	} catch (URISyntaxException e1) {

	    LogUtils.logError(
		    context,
		    DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "The application configuration path is null...aborting: "
			    + e1.toString() }, null);
	    return InstallationResults.UAPP_URI_INVALID;
	}
	if (application == null || applicationConfigurationFolder == null
		|| layout == null) {
	    LogUtils.logWarn(
		    context,
		    DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "The deploy folder or layout are null...aborting" },
		    null);
	    return InstallationResults.UAPP_URI_INVALID;
	}
	// 2 - verify If I belong to an AALSpace
	if (aalSpaceManager.getAALSpaceDescriptor() == null)
	    return InstallationResults.NO_AALSPACE_JOINED;

	// 3 - verify if I'm the DeployCoordinator
	if (!aalSpaceManager.getAALSpaceDescriptor().getDeployManager()
		.getPeerID()
		.equals(aalSpaceManager.getmyPeerCard().getPeerID())) {
	    return InstallationResults.NOT_A_DEPLOYMANAGER;
	}

	// 4 - send event to the AAL Space
	aalSpaceEventHandler.mpaInstalling(aalSpaceManager
		.getAALSpaceDescriptor());

	/*
	 * File mpaFile = getMpaFile(deployFolder); if(mpaFile == null &&
	 * !mpaFile.isFile()){ LogUtils.logWarn(context,
	 * DeployManagerImpl.class,"DeployManagerImpl", new Object[]
	 * {"No MPA file found...aborting", null); return
	 * InstallationResults.FAILED; }
	 * 
	 * AalMpa mpa = null; try{ mpa =
	 * (AalMpa)unmarshaller.unmarshal(mpaFile); }catch (JAXBException e) {
	 * LogUtils.logError(context,
	 * DeployManagerImpl.class,"DeployManagerImpl", new Object[]
	 * {"Error while parsing the MPA file: "+e, null); return
	 * InstallationResults.FAILED; }
	 */

	File uappFile = Util
		.getFile(uappSuffix, applicationConfigurationFolder);
	AalUapp uapp = null;

	if (uappFile != null && uappFile.canRead()) {
	    try {
		uapp = (AalUapp) unmarshaller.unmarshal(uappFile);
	    } catch (JAXBException e) {
		LogUtils.logError(
			context,
			DeployManagerImpl.class,
			"DeployManagerImpl",
			new Object[] { "uAAP file cannot be parsed. Aborting..." },
			null);
		return InstallationResults.MPA_FILE_NOT_VALID;

	    }
	}
	UAPPCard uAPPCard = new UAPPCard(uapp.getApp().getName(), uapp.getApp()
		.getAppId(), uapp.getApp().getDescription());
	// adding an entry to the registry
	this.registry.put(uAPPCard.getId(), new UAPPStatus(uAPPCard));

	for (PeerCard peer : layout.keySet()) {
	    Part target = layout.get(peer);

	    // send the part to the target node
	    LogUtils.logInfo(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Sending request to install uAPP part to: "
			    + peer.getPeerID() }, null);
	    byte[] fileContent = createZippedPart(application.getFolder(),
		    target);
	    LogUtils.logDebug(
		    context,
		    DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "ZipFile created ready to sent it to the target node" },
		    null);
	    controlBroker.requestToInstallPart(fileContent, peer, uAPPCard);

	}

	/*
	 * AALSpaceDescriptor currentAALSpace = getCurrentAALSpace(); //check if
	 * I'm he DeployCoordinator //if(currentAALSpace != null &&
	 * currentAALSpace
	 * .getDeployManager().getPeerID().equals(aalSpaceManager.
	 * getmyPeerCard().getPeerID())){ LogUtils.logInfo(context,
	 * DeployManagerImpl.class,"DeployManagerImpl", new Object[]
	 * {"Installing MPA application", null); //manage the whole process
	 * //check the MPA against the AALSpace Writer writer = new
	 * StringWriter();
	 * 
	 * if(layout != null){ for(PeerCard peer: layout.keySet()){ try {
	 * //serialized a part as a string and send the request to install to
	 * the target node writer = new StringWriter();
	 * marshaller.marshal(layout.get(peer), writer);
	 * 
	 * } catch (JAXBException e) { LogUtils.logError(context,
	 * DeployManagerImpl.class,"DeployManagerImpl", new Object[]
	 * {"Error marshalling MPA part: "+e.toString(), null); }
	 * if(peer.getPeerID
	 * ().equals(aalSpaceManager.getmyPeerCard().getPeerID()))
	 * controlBroker.installArtefactLocally(writer.toString()); else{
	 * LogUtils.logInfo(context,
	 * DeployManagerImpl.class,"DeployManagerImpl", new Object[]
	 * {"Sending requesto to install MPA part to: "+peer.getPeerID(), null);
	 * controlBroker.requestToInstallPart(writer.toString(), peer); } } }
	 * //} //check the peer acting as DeployCoordinator and delegate the
	 * installation /* else if(currentAALSpace != null){
	 * LogUtils.logInfo(context,
	 * DeployManagerImpl.class,"DeployManagerImpl", new Object[]
	 * {"Sending request to install MPA to: "
	 * +currentAALSpace.getDeployManager().getPeerID(), null);
	 * controlBroker.requestToInstallMPA(multiPartApplication,
	 * currentAALSpace.getDeployManager()); return
	 * InstallationResults.DELEGATED; }else{ LogUtils.logWarn(context,
	 * DeployManagerImpl.class,"DeployManagerImpl", new Object[]
	 * {"No able to install a distibuted MPA without an AALSpace", null);
	 * return InstallationResults.NO_AALSPACE_JOINED; }
	 * 
	 * }else{ //TODO: installation return
	 * InstallationResults.LOCALLY_DELEGATED; }
	 */
	// 4 - send event to the AAL Space
	aalSpaceEventHandler.mpaInstalled(aalSpaceManager
		.getAALSpaceDescriptor());

	return InstallationResults.SUCCESS;
    }

    public void installationPartNotification(UAPPCard mpaCard, String partID,
	    PeerCard peer, UAPPPartStatus partStatus) {
	LogUtils.logDebug(context, DeployManagerImpl.class,
		"DeployManagerImpl", new Object[] { "Updating the MPA: "
			+ mpaCard.getId() }, null);
	if (mpaCard != null && peer != null && partStatus != null) {
	    UAPPStatus mpaStatus = registry.get(mpaCard.getId());
	    if (mpaStatus != null) {
		LogUtils.logDebug(
			context,
			DeployManagerImpl.class,
			"DeployManagerImpl",
			new Object[] { "Updating the MPA with data: " + partID
				+ " - " + peer.getPeerID() + " - "
				+ partStatus.toString() }, null);
		mpaStatus.updatePart(partID, peer.getPeerID(), partStatus);
	    } else {
		LogUtils.logWarn(
			context,
			DeployManagerImpl.class,
			"DeployManagerImpl",
			new Object[] { "Received a install part notification for an MPA unknows: "
				+ mpaCard.getId() + "...Aborting." }, null);

	    }
	}

    }

    private void addRegistryEntry(UAPPCard mpaCard, Map<PeerCard, Part> layout) {
	// add entry to the registry
	UAPPStatus mpaStatus = this.registry.put(mpaCard.getId(),
		new UAPPStatus(mpaCard));
	for (PeerCard peer : layout.keySet()) {
	    Part part = layout.get(peer);
	    mpaStatus.updatePart(part.getPartId(), peer.getPeerID(),
		    UAPPPartStatus.PART_PENDING);

	}
    }

    private File getMpaFile(URI deployFolder) {
	File deployFolderFile = new File(deployFolder);
	String[] mpaFiles = deployFolderFile.list(new FilenameFilter() {
	    public boolean accept(File dir, String name) {
		return (name.contains(uappSuffix));
	    }
	});
	if (mpaFiles.length < 0) {
	    return null;
	} else {
	    return new File(deployFolder.toString() + "/" + mpaFiles[0]);
	}
    }

    /**
     * Creates a zip file containing the artifacts to send
     * 
     * @param applicationFolder
     * @param part
     * @return
     */
    private byte[] createZippedPart(URI applicationFolder, Part part) {
	ZipOutputStream out = null;
	File zippedPart = null;
	byte[] buf = new byte[1024];
	try {
	    // create the zip file in a tmp dir
	    zippedPart = new File(deployDir + File.separatorChar
		    + TMP_DEPLOY_DIR + File.separatorChar + "part.zip");
	    out = new ZipOutputStream(new FileOutputStream(zippedPart));
	    // pit the part descriptor in the zip
	    File partDescription = new File(deployDir + File.separatorChar
		    + TMP_DEPLOY_DIR + File.separatorChar + "partDesc-uapp");
	    marshaller.marshal(part, partDescription);
	    FileInputStream in = new FileInputStream(partDescription);
	    out.putNextEntry(new ZipEntry(partDescription.getName()));
	    int len;
	    while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	    out.closeEntry();
	    in.close();
	} catch (FileNotFoundException e1) {
	    LogUtils.logError(
		    context,
		    DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Error while creating the zip file part. Aborting: "
			    + e1 }, null);
	    return null;
	} catch (JAXBException e) {
	    LogUtils.logError(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Error while creating the zip file part: "
			    + e }, null);
	    return null;
	} catch (IOException e) {
	    LogUtils.logError(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Error while creating the zip file part: "
			    + e }, null);
	    return null;
	}

	// zip the part folder
	// get the part id
	try {
	    String partID = part.getPartId();
	    String partFolderString = applicationFolder.toString()
		    + File.separatorChar + APPLICATION_BINARYPART_PATH
		    + File.separatorChar + partID +File.separatorChar;
	    File partFolder = new File(partFolderString);
	    BufferedInputStream inPartFile = null;
	    byte[] data = new byte[1000];
	    String[] partFiles = partFolder.list();
	    for (String fileName : partFiles) {
		inPartFile = new BufferedInputStream(new FileInputStream(
			partFolder.getPath() + File.separatorChar + fileName),
			1000);
		out.putNextEntry(new ZipEntry(fileName));
		int count;
		while ((count = inPartFile.read(data, 0, 1000)) != -1) {
		    out.write(data, 0, count);
		}
		out.closeEntry();
	    }
	    out.flush();
	    out.close();
	    inPartFile.close();
	} catch (FileNotFoundException e1) {
	    LogUtils.logError(
		    context,
		    DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Error while creating the zip file part. Aborting: "
			    + e1 }, null);
	    return null;
	} catch (IOException e) {
	    LogUtils.logError(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Error while creating the zip file part: "
			    + e }, null);
	    return null;
	}

	/*
	 * //put the artifacts in the zip file for (DeploymentUnit dUnit :
	 * part.getDeploymentUnit()) { if (dUnit.isSetContainerUnit() &&
	 * dUnit.getContainerUnit().isSetKaraf()) { Karaf karafDUnit =
	 * dUnit.getContainerUnit().getKaraf(); if (karafDUnit.isSetEmbedding()
	 * && karafDUnit.getFeatures().isSetRepositoryOrFeature()) { for
	 * (Serializable element : karafDUnit.getFeatures()
	 * .getRepositoryOrFeature()) { if (element instanceof Feature) { // get
	 * the feature Feature feature = (Feature) element; if
	 * (feature.isSetDetailsOrConfigOrConfigfile()) { for (Serializable
	 * element1 : feature .getDetailsOrConfigOrConfigfile()) { // get the
	 * bundles if (element1 instanceof Bundle) { Bundle bundle = (Bundle)
	 * element1; try { // add to the zip file only the // artefacts that has
	 * been locally // downloaded if (bundle.getValue().startsWith( "file"))
	 * { URI uri = new URI( bundle.getValue()); File file = new File(
	 * uri.getSchemeSpecificPart()); FileInputStream in = new
	 * FileInputStream( deployFolder.getPath() + "/" + file.getName());
	 * out.putNextEntry(new ZipEntry( file.getName())); int len; while ((len
	 * = in.read(buf)) > 0) { out.write(buf, 0, len); } out.closeEntry();
	 * in.close(); } } catch (URISyntaxException e) { LogUtils.logError(
	 * context, DeployManagerImpl.class, "DeployManagerImpl", new Object[] {
	 * "Error while creating the zip file part: " + e }, null); return null;
	 * } catch (IOException e) { LogUtils.logError( context,
	 * DeployManagerImpl.class, "DeployManagerImpl", new Object[] {
	 * "Error while creating the zip file part: " + e }, null); return null;
	 * } }
	 * 
	 * } } } } } } }
	 */
	try {
	    out.flush();
	    out.close();
	} catch (IOException e) {
	    LogUtils.logError(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Error while creating the zip file part: "
			    + e }, null);
	    return null;
	}
	try {
	    FileInputStream inZip = new FileInputStream(zippedPart);
	    byte[] fileContent = new byte[(int) zippedPart.length()];
	    inZip.read(fileContent);
	    return fileContent;
	} catch (FileNotFoundException e) {
	    LogUtils.logError(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Error while creating the zip file part: "
			    + e }, null);
	    return null;
	} catch (IOException e) {
	    LogUtils.logError(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Error while creating the zip file part: "
			    + e }, null);
	    return null;
	}

    }

    public void sharedObjectAdded(Object sharedObj, Object arg1) {
	if (sharedObj instanceof ControlBroker) {
	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "ControlBroker service added" }, null);
	    this.controlBroker = (ControlBroker) sharedObj;
	} else if (sharedObj instanceof AALSpaceManager) {
	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "AALSpaceManager service added" }, null);
	    aalSpaceManager = (AALSpaceManager) sharedObj;
	    aalSpaceManager.addAALSpaceListener(this);
	    if (aalSpaceManager.getAALSpaceDescriptor() != null
		    && aalSpaceManager
			    .getAALSpaceDescriptor()
			    .getDeployManager()
			    .getPeerID()
			    .equals(aalSpaceManager.getmyPeerCard().getPeerID())) {
		isDeployCoordinator = true;
	    }

	} else if (sharedObj instanceof AALSpaceEventHandler) {
	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "AALSpaceEventHandler service added" }, null);
	    aalSpaceEventHandler = (AALSpaceEventHandler) sharedObj;
	    aalSpaceManager.addAALSpaceListener(this);

	}

    }

    public void sharedObjectRemoved(Object sharedObj) {
	if (sharedObj instanceof ControlBroker) {
	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "ControlBroker service removed" }, null);
	    controlBroker = null;
	    initialized = false;
	} else if (sharedObj instanceof AALSpaceManager) {
	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "AALSpaceManager service removed" }, null);
	    aalSpaceManager = null;
	    initialized = false;
	} else if (sharedObj instanceof AALSpaceEventHandler) {
	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "AALSpaceEventHandler service removed" },
		    null);
	    aalSpaceEventHandler = null;
	    initialized = false;
	}

    }

    /**
     * Method for checking if the MPA can be installed on the AALSpace where the
     * MW resides.
     * 
     * @return boolean answer
     * 
     *         private boolean aalSpaceCheck(AalMpa mpa, AALSpaceDescriptor
     *         spaceDescriptor){ if(mpa.getApplicationProfile().getAalSpace() !=
     *         null){ AalSpace targetAalSpace =
     *         mpa.getApplicationProfile().getAalSpace();
     *         if(targetAalSpace.getTargetProfile()== null){
     *         LogUtils.logWarn(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[]
     *         {"MPA target profile is null but it is requirred...aborting",
     *         null); return false; //check if target profiles matches with the
     *         profile of my aal Space }else
     *         if(!targetAalSpace.getTargetProfile(
     *         ).getProfileId().value().equals
     *         (spaceDescriptor.getSpaceCard().getProfile())){
     *         LogUtils.logDebug(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[] {
     *         "MPA AAL Space profile does not match with the current AALSpace...trying with the alternative MPA profiles"
     *         , null); for(ProfileType alternativeProfile:
     *         mpa.getApplicationProfile
     *         ().getAalSpace().getAlternativeProfiles().getProfile()){
     *         if(alternativeProfile
     *         .getProfileId().value().equals(spaceDescriptor
     *         .getSpaceCard().getProfile())){ LogUtils.logInfo(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[]
     *         {"MPA alternative profile: "
     *         +alternativeProfile.getProfileId().value()+
     *         " matches with the AALSpace profile: "
     *         +spaceDescriptor.getSpaceCard().getProfile(), null); return true;
     *         } } LogUtils.logWarn(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[]
     *         {"MPA: "+mpa.getApp().getName()+
     *         " cannot be installed on the current AALSpace", null); return
     *         false; } }else{ LogUtils.logWarn(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[]
     *         {"MPA "+mpa.getApp().getName()+
     *         " target AALSpace is null...aborting", null); return false;
     * 
     *         } return true;
     * 
     *         }
     */

    /**
     * this method performs the checks related to the consistency of the MPA
     * 
     * @param mpa
     * @return
     * 
     *         private boolean mpaChecks(AalMpa mpa){ try{
     *         if(mpa.getApp().getAppId() == null ||
     *         mpa.getApp().getAppId().isEmpty()){ LogUtils.logWarn(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[]
     *         {"MPA application ID is null...aborting", null); return false; }
     *         else if(mpa.getApplicationPart().getPart().isEmpty()){
     *         LogUtils.logWarn(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[]
     *         {"MPA parts are empty...aborting", null); return false;
     * 
     *         } //TODO: Add check for the runtime support }catch
     *         (NullPointerException e) { LogUtils.logError(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[]
     *         {"Error during application checks", null); return false; } return
     *         true; }
     */

    /**
     * Method to find the set of target peers according to the multipart
     * applicatio
     * 
     * @param mpa
     *            the MPA
     * @return map of PeerCard of the target peers
     * 
     *         private Map<PeerCard, Part> makeMPALayout(AalMpa mpa){
     *         Map<PeerCard, Part> mpaLayout = new HashMap<PeerCard, Part>();
     *         Map<String, PeerCard> peers = new HashMap<String, PeerCard>();
     *         peers.putAll(aalSpaceManager.getPeers()); for(Part part :
     *         mpa.getApplicationPart().getPart()){ //check: deployment units
     *         for(String key: peers.keySet()){ PeerCard peer =
     *         aalSpaceManager.getPeers().get(key);
     *         if(checkDeployementUnit(part.getDeploymentUnit(), peer)){
     *         mpaLayout.put(peer, part); peers.remove(key); break; } } } return
     *         mpaLayout; }
     * 
     *         private boolean checkDeployementUnit(List<DeploymentUnit>
     *         depoyementUnits, PeerCard peer){ for(DeploymentUnit
     *         deployementUnit: depoyementUnits){ //check the existence of:
     *         osUnit if(deployementUnit.getOsUnit()!= null){
     *         if(deployementUnit.getOsUnit().value() == null ||
     *         deployementUnit.getOsUnit().value().isEmpty()){
     *         LogUtils.logWarn(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[] {
     *         "OSunit is present but not consistent. OSUnit is null or empty",
     *         null); return false; } }else if
     *         (deployementUnit.getPlatformUnit() != null){
     *         if(deployementUnit.getPlatformUnit().value() == null ||
     *         deployementUnit.getPlatformUnit().value().isEmpty()){
     *         LogUtils.logWarn(context,
     *         DeployManagerImpl.class,"DeployManagerImpl", new Object[] {
     *         "PlatformUnit is present but not consistent. Plaform is null or empty"
     *         , null); return false;
     * 
     *         } } } return true; }
     */

    public void aalSpaceJoined(AALSpaceDescriptor spaceDescriptor) {
	LogUtils.logDebug(
		context,
		DeployManagerImpl.class,
		"DeployManagerImpl",
		new Object[] { "Configure the ControlBroker for the reception of DeployMessage" },
		null);
	// if I'm also the deploy manager, set this property
	if (spaceDescriptor.getDeployManager().getPeerID()
		.equals(aalSpaceManager.getmyPeerCard().getPeerID())) {
	    isDeployCoordinator = true;
	}

    }

    public void aalSpaceLost(AALSpaceDescriptor spaceDescriptor) {
	// TODO Auto-generated method stub

    }

    public void dispose() {
	// remove me as listener
	context.getContainer().removeSharedObjectListener(this);
	// TODO Auto-generated method stub

    }

    public void loadConfigurations(Dictionary configurations) {
	LogUtils.logDebug(context, DeployManagerImpl.class,
		"DeployManagerImpl",
		new Object[] { "Updating DeployManager properties" }, null);
	if (configurations == null) {
	    LogUtils.logDebug(context, DeployManagerImpl.class,
		    "DeployManagerImpl",
		    new Object[] { "Properties are null. Aborting..." }, null);

	} else {
	    deployDir = (String) configurations.get(Consts.DEPLOY_DIR);
	    uappSuffix = (String) configurations.get(Consts.MPA_SUFFIX);
	}

    }

    public static void main(String[] args) throws FileNotFoundException {
	ZipOutputStream z = new ZipOutputStream(new FileOutputStream(new File(
		"prova")));
	if (z instanceof Serializable)
	    System.out.println("d");
    }

    public void newPeerJoined(PeerCard peer) {
	// TODO Auto-generated method stub

    }

    public void peerLost(PeerCard peer) {
	// TODO Auto-generated method stub

    }

    public boolean isDeployCoordinator() {
	return isDeployCoordinator;
    }

    public Map<String, UAPPStatus> getUAPPRegistry() {
	return registry;
    }

    public void aalSpaceStatusChanged(AALSpaceStatus status) {
	// TODO Auto-generated method stub

    }

}
