/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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
package org.universAAL.middleware.sodapop.impl.osgi;

import java.security.Security;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.acl.P2PConnector;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.impl.Codec;
import org.universAAL.middleware.sodapop.impl.SodaPopImpl;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class Activator implements BundleActivator {
    public static BundleConfigHome confHome;
    private SodaPopImpl g = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
	confHome = new BundleConfigHome(context.getBundle().getSymbolicName());
	g = new SodaPopImpl(uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context }),
		uAALBundleContainer.THE_CONTAINER, confHome.getAbsolutePath(),
		new Object[] { P2PConnector.class.getName() },
		new Object[] { MessageContentSerializer.class.getName() },
		new Object[] { SodaPop.class.getName() }, new Codec() {
		    public byte[] encode(byte[] data) {
			return data;
		    }

		    public byte[] decode(String data) {
			return data.getBytes();
		    }
		});
//	Security.addProvider(new BouncyCastleProvider());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
	g.stop();
//	Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }
}
