package org.universAAL.samples.lighting.client.test;

import org.osgi.framework.Constants;
import org.springframework.util.Assert;
import org.universAAL.itests.MiddlewareIntegrationTest;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ontology.phThing.Device;
import org.universAAL.samples.lighting.client.Activator;
import org.universAAL.samples.lighting.client.LightingConsumer;

/**
 * LightingTest is an example of JUnit OSGi integration test. The TestCase uses
 * LightingConsumer to verify if the lighting sample works correctly. Each
 * integration TestCase has to extend MiddlewareIntegrationTest provided in the
 * itests maven artifact. Thanks to using JUnit the TestCase will be executed
 * during each maven build. However please mind that the tests are by default
 * disabled in the main middleware pom file (mw.pom). To enable them an argument
 * "-DskipTests=false" has to be added to the "mvn" invocation in the command
 * line.
 * 
 * @author rotgier
 * 
 */
public class LightingTest extends MiddlewareIntegrationTest {

	/**
	 * Constructor of each integration TestCase has to call constructor of upper
	 * class providing path to the launch configuration and path to the
	 * configuration directory of the uAAL runtime. Launch configuration will be
	 * used to setup uAAL runtime for the purpose of TestCase. All bundles
	 * needed for the TestCase have to be included in the launch configuration.
	 */
	public LightingTest() {
		super("../../pom/launches/LightingExample_Complete_0_3_2.launch",
				"../../../itests/rundir/confadmin");
	}

	/**
	 * Helper method for logging.
	 * 
	 * @param msg
	 */
	private void LOGInfo(String format, Object ... args) {
	    	StackTraceElement callingMethod = Thread.currentThread().getStackTrace()[2];
	    	String logMsg = null;
	    	if (args != null) {
	    	    logMsg = String.format(format, args);
	    	} else {
	    	    logMsg = format;
	    	}
	    	LogUtils.logInfo(Activator.mc, LightingTest.class, callingMethod.getMethodName(), new Object [] {logMsg}, null);
	}


	/**
	 * Verifies that runtime platform has correctly started. It prints basic
	 * information about framework (vendor, version) and lists installed
	 * bundles.
	 * 
	 * @throws Exception
	 */
	public void testOsgiPlatformStarts() throws Exception {
		LOGInfo("FRAMEWORK_VENDOR %s", bundleContext.getProperty(Constants.FRAMEWORK_VENDOR));
		LOGInfo("FRAMEWORK_VERSION %s", bundleContext.getProperty(Constants.FRAMEWORK_VERSION));
		LOGInfo("FRAMEWORK_EXECUTIONENVIRONMENT %s", bundleContext.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT));

		LOGInfo("!!!!!!! Listing bundles in integration test !!!!!!!");
		for (int i = 0; i < bundleContext.getBundles().length; i++) {
			LOGInfo("name: " + bundleContext.getBundles()[i].getSymbolicName());
		}
		LOGInfo("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	/**
	 * Verifies the lighting sample with the use of LightingConsumer. Following
	 * operations are tested: getControlledLamps, turnOn, turnOff, dimToValue.
	 * 
	 * @throws Exception
	 */
	public void testLightingClient() throws Exception {
		LOGInfo("!!!!!!! Testing Lighting Client !!!!!!!");
		LOGInfo("!!!!!!! Getting controlled lamps and checking their amount !!!!!!!");

		/* There should be four lamps available. */
		Device[] controlledLamps = LightingConsumer.getControlledLamps();
		Assert.isTrue(controlledLamps.length == 4);

		int i = 0;
		for (Device lamp : controlledLamps) {
			LOGInfo("!!!!!!! Testing Lamp %s!!!!!!!", i);
			String lampUri = lamp.getURI();
			

			/* turnOn should end with success */
			LOGInfo("!!!!!!! Testing Lamp %s turnOn!!!!!!!", i);
			Assert.isTrue(LightingConsumer.turnOn(lampUri));

			/* when repeated turnOn should also end with success */
			LOGInfo("!!!!!!! Testing Lamp %s turnOn!!!!!!!", i);
			Assert.isTrue(LightingConsumer.turnOn(lampUri));

			/* turnOff should end with success */
			LOGInfo("!!!!!!! Testing Lamp %s turnOff!!!!!!!", i);
			Assert.isTrue(LightingConsumer.turnOff(lampUri));

			/* when repeated turnOff should also end with success */
			LOGInfo("!!!!!!! Testing Lamp %s turnOff!!!!!!!", i);
			Assert.isTrue(LightingConsumer.turnOff(lampUri));

			/* dimToValue with argument other than 0, 100 should fail */
			LOGInfo("!!!!!!! Testing Lamp %s dimToValue 45!!!!!!!", i);
			Assert.isTrue(!LightingConsumer.dimToValue(lampUri, 45));

			/* dimToValue with argument 100 should end with success */
			LOGInfo("!!!!!!! Testing Lamp %s dimToValue 100!!!!!!!", i);
			Assert.isTrue(LightingConsumer.dimToValue(lampUri, 100));

			/* dimToValue with argument 0 should end with success */
			LOGInfo("!!!!!!! Testing Lamp %s dimToValue 0!!!!!!!", i);
			Assert.isTrue(LightingConsumer.dimToValue(lampUri, 0));

			i++;
		}
	}

}
