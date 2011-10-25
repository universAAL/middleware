package org.universAAL.itests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

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
 * @author rotgier
 * 
 */
public class MiddlewareIntegrationTest extends
		AbstractConfigurableBundleCreatorTests {

	private String pathToLaunchFile;

	private String bundlesConfLocation;

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
		Resource[] bundleResources = new Resource[bundleList.size() + 3];
		bundleResources[0] = new UrlResource(
				"mvn:org.ops4j.pax.url/pax-url-mvn/1.3.5");
		bundleResources[1] = new UrlResource(
				"mvn:org.ops4j.pax.url/pax-url-wrap/1.3.5");
		bundleResources[2] = new UrlResource(
				"mvn:org.universAAL.middleware/itests/0.1.0-SNAPSHOT");
		for (int i = 0; i < bundleList.size(); i++) {
			bundleResources[i + 3] = new UrlResource(
					bundleList.get(i).bundleUrl);
		}
		return bundleResources;
	}

	/**
	 * This methods returns sorted list of bundles which will be started for the
	 * purpose of the TestCase. Method parses launch configuration provided in the
	 * constructor and extracts pax run arguments as well as JVM arguments. JVM
	 * arguments are then set by means of java.lang.System class. The
	 * "bundles.configuration.location" JVM argument provided in the launch
	 * configuration is ignored and the "bundlesConfLocation" property is used
	 * instead.
	 */
	protected Resource[] getTestBundles() {
		addProtocolHandlers();
		Resource[] bundleResources = null;
		try {
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
			return null;
		}
	}

}
