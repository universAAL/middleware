package org.universAAL.middleware.container.test;

import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.osgi.run.Activator;

/**
 * Here developer's of this artifact should code their integration tests.
 * 
 * @author rotgier
 * 
 */
public class ArtifactIntegrationTest extends IntegrationTest {
    
    private class ShareObjectTestClass {
	
    }

    public void testComposite() {
	logAllBundles();
    }
    
    public void testAddRemoveSharedObject() {
   	ModuleContext mc = Activator.mc;
   	ShareObjectTestClass o = new ShareObjectTestClass();
   	uAALBundleContainer.THE_CONTAINER.shareObject(mc, o, new Object[]{o.getClass().getName()});
   	uAALBundleContainer.THE_CONTAINER.removeSharedObject(mc,o, new Object[]{o.getClass().getName()});
    }
    
    public void testRemoveSharedObjectTwice() {
   	ModuleContext mc = Activator.mc;
   	ShareObjectTestClass o = new ShareObjectTestClass();
   	uAALBundleContainer.THE_CONTAINER.shareObject(mc, o, new Object[]{o.getClass().getName()});
   	uAALBundleContainer.THE_CONTAINER.removeSharedObject(mc,o, new Object[]{o.getClass().getName()});
   	try {
   	    uAALBundleContainer.THE_CONTAINER.removeSharedObject(mc,o, new Object[]{o.getClass().getName()});
   	} catch (Exception e) {
   	   return;
   	}
   	fail();
    }
    
    public void testRemoveNonexistentSharedObject() {
   	ModuleContext mc = Activator.mc;  	
   	ShareObjectTestClass o = new ShareObjectTestClass();
   	try {
   	    uAALBundleContainer.THE_CONTAINER.removeSharedObject(mc,o, new Object[]{o.getClass().getName()});
   	} catch (Exception e) {
   	   return;
   	}
   	fail();
    }

    public void testCheckReturnedSharedObject() {
	ModuleContext mc = Activator.mc;
   	ShareObjectTestClass o = new ShareObjectTestClass();
   	uAALBundleContainer.THE_CONTAINER.shareObject(mc, o, new Object[]{o.getClass().getName()});
   	Object re = null;
   	
   	try {
   	    re = uAALBundleContainer.THE_CONTAINER.fetchSharedObject(mc, new Object[]{o.getClass().getName()});
   	    if (re == null) {
   		fail();
   	    }
   	    if (!o.getClass().isInstance(re)) {
   		fail();
   	    }
   	} catch(Exception e) {
   	    fail();
   	}
   	uAALBundleContainer.THE_CONTAINER.removeSharedObject(mc,o, new Object[]{o.getClass().getName()});
   	re = null;
   	re = uAALBundleContainer.THE_CONTAINER.fetchSharedObject(mc, new Object[]{o.getClass().getName()});
   	if (re != null) {
   	    fail();
   	}
   	if (o.getClass().isInstance(re)) {
   	    fail();
   	}
    }
}
