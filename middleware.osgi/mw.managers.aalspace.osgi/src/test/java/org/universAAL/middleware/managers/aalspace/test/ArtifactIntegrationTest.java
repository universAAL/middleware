package org.universAAL.middleware.managers.aalspace.test;

import org.universAAL.itests.IntegrationTest;

/**
 * Here developer's of this artifact should code their integration tests.
 * 
 * @author rotgier
 * 
 */
public class ArtifactIntegrationTest extends IntegrationTest {

    public ArtifactIntegrationTest() {
	setRunArguments("net.slp.port", "7000");
	setRunArguments("net.slp.multicastTimeouts", "500,750");
	setRunArguments("java.net.preferIPv4Stack", "true");
    }

    public void testComposite() {
	logAllBundles();
    }
}