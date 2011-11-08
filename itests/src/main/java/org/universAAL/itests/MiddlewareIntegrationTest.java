package org.universAAL.itests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.osgi.test.platform.Platforms;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class has to be extended for the purpose of OSGi integration test
 * implementation. MiddlewareIntegrationTest extends class from Spring DM
 * framework and adds feature of setting up the TestCase with the use of Eclipse
 * launch configuration. Launch configuration provided in the constructor is
 * parsed and list of bundles as well as JVM arguments are extracted. Bundles
 * are sorted by the runtime level and then started in that order. JVM arguments
 * are set by means of java.lang.System class.
 * 
 * Comment about logging: org.universAAL.middleware.container.utils.LogUtils
 * cannot be used because BundleContext (and therefore ModuleContext) does not
 * yet exist for the bundle in which integration tests are to be launched. Thus
 * exceptions are simple printed out to screen.
 * 
 * @author rotgier
 * 
 */
public class MiddlewareIntegrationTest extends
	AbstractConfigurableBundleCreatorTests {

    private String pathToLaunchFile;

    private String bundlesConfLocation;

    private Object junitTestActivator;

    /**
     * Constructor can be invoked only by subclasses.
     * 
     * @param pathToLaunchFile
     *            path to the launch configuration which will be used for
     *            setting up the OSGi platform in which TestCase will be
     *            executed.
     * @param bundlesConfLocation
     *            path to the uAAL runtime configuration directory.
     */
    protected MiddlewareIntegrationTest(String pathToLaunchFile,
	    String bundlesConfLocation) {
	this.pathToLaunchFile = pathToLaunchFile;
	this.bundlesConfLocation = bundlesConfLocation;
    }

    /**
     * Helper class used for sorting bundles from the launch configuration by
     * the runlevel.
     * 
     * @author rotgier
     * 
     */
    class BundleFromLaunch {
	BundleFromLaunch(String bundleUrl, int runLevel) {
	    this.bundleUrl = bundleUrl;
	    this.runLevel = runLevel;
	}

	String bundleUrl;
	int runLevel;
    }

    /**
     * This method registers URL Handlers for the "mvn" and "wrap" protocols.
     * Because regular API (URL.setURLStreamHandlerFactory(
     * URLStreamHandlerFactory )) provides only setting new handlers and does
     * not provide adding. Registering Handlers is hacked with the use of Java
     * Reflection.
     */
    private void addProtocolHandlers() {
	try {
	    Field handlersField = URL.class.getDeclaredField("handlers");
	    handlersField.setAccessible(true);
	    Hashtable handlers = (Hashtable) handlersField.get(null);
	    handlers.put("mvn", new org.ops4j.pax.url.mvn.Handler());
	    handlers.put("wrap", new org.ops4j.pax.url.wrap.Handler());
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex);
	}
    }

    /**
     * This method informs Spring DM framework that Felix should be used as the
     * OSGi platform for the tests.
     */
    protected String getPlatformName() {
	return Platforms.FELIX;
    }

    /**
     * This method parses pax run arguments from the launch configuration,
     * extracts list of bundles and sorts it by the runlevel. Bundles are
     * wrapped in the org.springframework.core.io.Resource class as URLs with
     * "wrap" and "mvn" protocol.
     * 
     * 
     * @param paxArgs
     *            pax run arguments from the launch configurations provided as
     *            DOM NodeList
     * @return
     * @throws IOException
     */
    private Resource[] parsePaxArgs(NodeList paxArgs) throws IOException {
	List<BundleFromLaunch> bundleList = new ArrayList<BundleFromLaunch>();
	for (int i = 0; i < paxArgs.getLength(); i++) {
	    Node paxArgNode = paxArgs.item(i);
	    String nodeName = paxArgNode.getNodeName();
	    if (paxArgNode.getAttributes() != null) {
		Node valueAttrib = paxArgNode.getAttributes().getNamedItem(
			"value");
		if (valueAttrib != null) {
		    String paxArg = valueAttrib.getTextContent();
		    if (paxArg.startsWith("wrap") || paxArg.startsWith("mvn")) {
			String[] paxArgStrs = paxArg.split("@");
			String bundleUrlStr = paxArgStrs[0];
			String[] bundleUrlArr = bundleUrlStr.split("/");
			BundleContext bc = bundleContext;
			if (bundleUrlArr != null && bundleUrlArr.length == 3) {
			    if (bundleUrlArr[1].equals("smp.lighting.client")) {
				continue;
			    }
			}
			int runLevel = Integer.parseInt(paxArgStrs[1]);
			bundleList.add(new BundleFromLaunch(bundleUrlStr,
				runLevel));
		    }
		}
	    }

	}
	Collections.sort(bundleList, new Comparator<BundleFromLaunch>() {
	    public int compare(BundleFromLaunch o1, BundleFromLaunch o2) {
		if (o1.runLevel < o2.runLevel)
		    return -1;
		if (o1.runLevel > o2.runLevel)
		    return 1;
		return 0;
	    }

	});
	/*
	 * Beside bundles from the pax configuration the following three bundles
	 * have to be added.
	 */
	Resource[] bundleResources = new Resource[bundleList.size() + 4];
	bundleResources[0] = new UrlResource(
		"mvn:org.ops4j.pax.url/pax-url-mvn/1.3.5");
	bundleResources[1] = new UrlResource(
		"mvn:org.ops4j.pax.url/pax-url-wrap/1.3.5");
	bundleResources[2] = new UrlResource(
		"mvn:org.universAAL.middleware/itests/0.1.0-SNAPSHOT");
	bundleResources[3] = new UrlResource(
		"mvn:org.apache.commons/com.springsource.org.apache.commons.io/1.4.0");
	for (int i = 0; i < bundleList.size(); i++) {
	    bundleResources[i + 4] = new UrlResource(
		    bundleList.get(i).bundleUrl);
	}
	return bundleResources;
    }

    /**
     * Method postProcessBundleContext has to be overridden to wrap system
     * bundleContext into a fake one. Thanks to that installing bundle can be
     * intercepted and JunitTestActivator can be created and started.
     */
    @Override
    protected void postProcessBundleContext(BundleContext context)
	    throws Exception {
	BundleContext fakeBC = (BundleContext) Proxy.newProxyInstance(
		BundleContext.class.getClassLoader(),
		new Class[] { BundleContext.class }, new FakeBundleContext(
			context));
	super.postProcessBundleContext(fakeBC);
    }

    /**
     * This method copies contents of target/classes to target/test-classes.
     * Thanks to that regular classes of given bundle can be used for testing
     * without a need to load the bundle from maven repository. It is very
     * important because in the maven build cycle "test" precedes "install". If
     * this method will not be invoked, when bundle does not exist in the maven
     * repository there is a deadlock - bundle cannot be tested because it is
     * not in the repo and bundle cannot be installed in the repo because tests
     * fail.
     * 
     * Additionally method rewrites bundle manifest for purpose of adding
     * imports to packages related to itests bundle.
     * 
     * @throws IOException
     * 
     */
    private void prepareClassesToTests() throws Exception {
	FileUtils.copyDirectory(new File("./target/classes"), new File(
		"./target/test-classes"));
	Manifest bundleMf = new Manifest(new FileInputStream(
		"./target/classes/META-INF/MANIFEST.MF"));
	Attributes mainAttribs = bundleMf.getMainAttributes();
	mainAttribs.put(new Attributes.Name("Import-Package"), mainAttribs
		.getValue("Import-Package")
		+ ",org.universAAL.itests,org.springframework.util");
	bundleMf.write(new FileOutputStream(
		"./target/test-classes/META-INF/MANIFEST.MF"));
    }

    /**
     * This method returns sorted list of bundles which will be started for the
     * purpose of the TestCase. Method parses launch configuration provided in
     * the constructor and extracts pax run arguments as well as JVM arguments.
     * JVM arguments are then set by means of java.lang.System class. The
     * "bundles.configuration.location" JVM argument provided in the launch
     * configuration is ignored and the "bundlesConfLocation" property is used
     * instead.
     */
    protected Resource[] getTestBundles() {
	addProtocolHandlers();
	Resource[] bundleResources = null;
	try {
	    prepareClassesToTests();
	    Document doc = DocumentBuilderFactory.newInstance()
		    .newDocumentBuilder().parse(new File(pathToLaunchFile));
	    Node root = doc.getFirstChild();
	    NodeList nodes = root.getChildNodes();
	    for (int i = 0; i < nodes.getLength(); i++) {
		Node n = nodes.item(i);
		String name = n.getNodeName();
		if (name != null && name.equals("listAttribute")) {
		    NamedNodeMap attribs = n.getAttributes();
		    Node keyAttrib = attribs.getNamedItem("key");
		    String keyAttribStr = keyAttrib.getTextContent();
		    if (keyAttribStr
			    .equals("org.ops4j.pax.cursor.runArguments")) {
			NodeList paxArgs = n.getChildNodes();
			bundleResources = parsePaxArgs(paxArgs);
		    }
		}
		if (name != null && name.equals("stringAttribute")) {
		    NamedNodeMap attribs = n.getAttributes();
		    Node keyAttrib = attribs.getNamedItem("key");
		    String keyAttribStr = keyAttrib.getTextContent();
		    if (keyAttribStr
			    .equals("org.eclipse.jdt.launching.VM_ARGUMENTS")) {
			String vmArgs = attribs.getNamedItem("value")
				.getTextContent();
			for (String vmArg : vmArgs.split(" ")) {
			    vmArg = vmArg.trim();
			    if (!vmArg.startsWith("-D")) {
				throw new RuntimeException(String.format(
					"vmArg %s does not start with -D",
					vmArg));
			    }
			    vmArg = vmArg.substring(2);
			    String[] vmArgKeyValue = vmArg.split("=");
			    if (vmArgKeyValue[0]
				    .equals("bundles.configuration.location")) {
				System.setProperty(vmArgKeyValue[0],
					this.bundlesConfLocation);
			    } else {
				System.setProperty(vmArgKeyValue[0],
					vmArgKeyValue[1]);
			    }
			}
		    }
		}
	    }
	    return bundleResources;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex);
	}
    }

    /**
     * The class is used for intercepting installation of test bundle. When
     * interception occurs JunitTestActivator is created (using ClassLoader of
     * itests bundle) and it's start method is invoked.
     * 
     * @author rotgier
     * 
     */
    public class FakeBundleContext implements InvocationHandler {

	BundleContext systemBC;

	/**
	 * This flag is used to ensure that JunitTestActivator is created and
	 * started only once.
	 */
	private boolean initializedJunitTestActivator = false;

	public FakeBundleContext(BundleContext bc) {
	    this.systemBC = bc;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable {
	    Object ret = method.invoke(systemBC, args);
	    String mName = method.getName();
	    if ("installBundle".equals(mName)) {
		if (!initializedJunitTestActivator) {
		    Class junitTestActivatorClass = null;
		    for (Bundle b : systemBC.getBundles()) {
			if ("itests".equals(b.getSymbolicName())) {
			    junitTestActivatorClass = b
				    .loadClass("org.springframework.osgi.test.JUnitTestActivator");
			    junitTestActivator = junitTestActivatorClass
				    .newInstance();
			    Method activatorStartMethod = junitTestActivatorClass
				    .getMethod("start", BundleContext.class);
			    if (ret instanceof Bundle) {
				Bundle newBundle = (Bundle) ret;
				newBundle.start();
				BundleContext newBc = newBundle
					.getBundleContext();
				activatorStartMethod.invoke(junitTestActivator,
					newBc);
			    }
			    break;
			}
		    }
		    initializedJunitTestActivator = true;
		}
	    }
	    return ret;
	}

    }

}