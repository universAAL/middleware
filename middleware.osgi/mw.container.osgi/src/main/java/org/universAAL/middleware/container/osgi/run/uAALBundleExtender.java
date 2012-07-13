package org.universAAL.middleware.container.osgi.run;

import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.uAALModuleActivator;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;

public class uAALBundleExtender implements SynchronousBundleListener {

    private BundleContext bc;

    private String getClassNameFromEntry(String entryStr) {
	String[] entryParsed = entryStr.split("//");
	if (entryParsed.length == 2) {
	    String entryPath = entryParsed[1];
	    int index = entryPath.indexOf("/");
	    if (index >= 0) {
		String realEntryPath = entryPath.substring(index + 1);
		realEntryPath = realEntryPath.substring(0, realEntryPath
			.length() - 6);
		String className = realEntryPath.replace('/', '.');
		return className;
	    }
	}
	return null;
    }

    public uAALBundleExtender(BundleContext bc) {
	this.bc = bc;
    }

    public void bundleChanged(BundleEvent event) {
	Bundle bundle = event.getBundle();

	// ignore current bundle for context creation
	if (bundle.getBundleId() == bc.getBundle().getBundleId()) {
	    return;
	}

	switch (event.getType()) {
	case BundleEvent.STARTED: {
	    final Bundle b = event.getBundle();
	    b.getSymbolicName();
	    Enumeration entries = b.findEntries("", "*Activator*.class", true);
	    if (entries != null) {
		while (entries.hasMoreElements()) {
		    URL entryUrl = (URL) entries.nextElement();
		    String className = getClassNameFromEntry(entryUrl
			    .toString());
		    if (className != null) {
			try {
			    Class c = b.loadClass(className);
			    Class[] ifaces = c.getInterfaces();
			    boolean shouldLoad = false;
			    for (int i = 0; i < ifaces.length; i++) {
				if (ifaces[i] == uAALModuleActivator.class) {
				    shouldLoad=true;
				    break;
				}
			    }
			    if (shouldLoad) {
				uAALModuleActivator theActivator = (uAALModuleActivator) c
					.newInstance();
				ModuleContext moduleContext = uAALBundleContainer.THE_CONTAINER
					.registerModule(new Object[] { b
						.getBundleContext() });
				theActivator.start(moduleContext);
				break;
			    }
			} catch (Throwable t) {
			    t.printStackTrace();
			    new Thread(new Runnable() {
				public void run() {
				    try {
					b.stop();
				    } catch (Exception ex) {
					ex.printStackTrace();
				    }
				}
			    }).start();
			    throw new RuntimeException(t);			    
			}
		    }
		}
	    }
	    break;
	}
	case BundleEvent.STOPPING: {
	    if (bundle.getBundleId() == 0) {
		// System bundle is shutting down; Special handling for
		// framework shutdown
		// shutdown();
	    } else {
	    }
	    break;
	}
	default:
	    break;
	}
    }
}
