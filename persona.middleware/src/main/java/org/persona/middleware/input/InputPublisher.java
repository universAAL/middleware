/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.persona.middleware.input;

import org.osgi.framework.BundleContext;
import org.persona.middleware.Activator;

import de.fhg.igd.ima.sodapop.Bus;
import de.fhg.igd.ima.sodapop.Publisher;
import de.fhg.igd.ima.sodapop.msg.Message;

/**
 * Provides the interface to be implemented by input publishers
 * together with shared code. Only instances of this class can 
 * publish input events.
 * 
 * @author mtazari
 */
public abstract class InputPublisher implements Publisher {
	private InputBus bus;
	private String myID;
	
	protected InputPublisher(BundleContext context) {
		Activator.checkInputBus();
		bus = (InputBus) context.getService(
				context.getServiceReference(InputBus.class.getName()));
		myID = bus.register(this);
	}

	public abstract void communicationChannelBroken();

	public final boolean eval(Message m) {
		return false;
	}

	public final void handleRequest(Message m) {
	}

	public final void busDyingOut(Bus b) {
		if (b == bus)
			communicationChannelBroken();
	}
	
	public final void publish(InputEvent e) {
		if (e != null) {
			bus.sendMessage(myID, e);
		}
	}
	
	public void close(){
		bus.unregister(myID, this);
	}
}
