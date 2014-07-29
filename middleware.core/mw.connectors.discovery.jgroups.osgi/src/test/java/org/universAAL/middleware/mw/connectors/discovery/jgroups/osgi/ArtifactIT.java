package org.universAAL.middleware.mw.connectors.discovery.jgroups.osgi;

import org.universAAL.itests.IntegrationTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ArtifactIT extends IntegrationTest {

    public ArtifactIT() {
	setRunArguments("net.slp.port", "7000");
	setRunArguments("net.slp.multicastTimeouts", "500,750");
	setRunArguments("java.net.preferIPv4Stack", "true");
    }

    public void testComposite() {
	logAllBundles();
    }
}
