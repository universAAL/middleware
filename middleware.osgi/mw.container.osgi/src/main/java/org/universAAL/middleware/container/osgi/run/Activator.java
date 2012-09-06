/*
	Copyright 2011-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 

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
package org.universAAL.middleware.container.osgi.run;

import java.util.ArrayList;
import java.util.Iterator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.LogListener;

/**
 * @author mtazari
 * 
 */
public class Activator implements BundleActivator, ServiceListener {
    private BundleContext context;
    private static ArrayList logListeners = new ArrayList(2);

    public static Iterator logListeners() {
	return logListeners.iterator();
    }

    public void serviceChanged(ServiceEvent se) {
	Object service = context.getService(se.getServiceReference());
	if (service instanceof LogListener) {
	    if (se.getType() == ServiceEvent.REGISTERED)
		logListeners.add(service);
	    else if (se.getType() == ServiceEvent.UNREGISTERING)
		logListeners.remove(service);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext arg0) throws Exception {
	context = arg0;
	context.addServiceListener(this);
	context.addBundleListener(new uAALBundleExtender(context));
	try {
	    ServiceReference sr[] = context.getServiceReferences(
		    LogListener.class.getName(), null);
	    if (sr == null)
		return;
	    for (int i = 0; i < sr.length; i++) {
		LogListener l = (LogListener) context.getService(sr[i]);
		if (l != null)
		    logListeners.add(l);
	    }
	} catch (InvalidSyntaxException e) {
	    e.printStackTrace();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext arg0) throws Exception {
	// TODO Auto-generated method stub

    }
}
