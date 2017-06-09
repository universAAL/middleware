package org.universAAL.middleware.bus.model.test;

import org.universAAL.itests.IntegrationTest;

/**
 * Here developer's of this artifact should code their integration tests.
 * 
 * @author rotgier
 * 
 */
public class ArtifactIT extends IntegrationTest {
	public ArtifactIT() {

		// // Arguments for MW2.0
		setRunArguments("java.net.preferIPv4Stack", "true");
		setRunArguments("net.slp.port", "7000");
	}

	public void testComposite() {
		logAllBundles();
	}

}
