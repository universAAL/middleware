/*******************************************************************************
 * Copyright 2017 Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.container.pojo.layers;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.Attributes;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.pojo.dummyManagers.DummyAALSpaceManager;
import org.universAAL.middleware.container.pojo.dummyManagers.DummyCommunicationModule;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.serialization.MessageContentSerializer;

/**
 * @author amedrano
 *
 */
public class AbstractBusLayer implements ModuleActivator {

	private boolean isConnected = false;

	/**
	 * 
	 */
	public AbstractBusLayer(boolean isconnected) {
		this.isConnected = isconnected;
	}

	/** {@ inheritDoc} */
	public void start(ModuleContext mc) throws Exception {
		// init bus model
		// XXX init card correctly?
		final PeerCard myCard = new PeerCard(PeerRole.COORDINATOR,
				(String) mc.getAttribute(Attributes.CONTAINER_OS_ARCHITECTURE),
				(String) mc.getAttribute(Attributes.CONTAINER_PLATFORM_NAME));
		AALSpaceManager sm;
		CommunicationModule com;
		if (!isConnected) {
			sm = new DummyAALSpaceManager(myCard);
			com = new DummyCommunicationModule();
		} else {
			// TODO use real managers when set to do so.
			sm = null;
			com = null;
		}
		AbstractBus.initBrokerage(mc, sm, com);

		BusMessage.setThisPeer(sm.getMyPeerCard());

		MessageContentSerializer mcs = (MessageContentSerializer) mc
				.getContainer()
				.fetchSharedObject(
						mc,
						new Object[] { MessageContentSerializer.class.getName() });

		BusMessage.setMessageContentSerializer(mcs);

	}

	/** {@ inheritDoc} */
	public void stop(ModuleContext mc) throws Exception {

	}

}
