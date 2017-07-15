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

package org.universAAL.middleware.container.pojo.dummyManagers;

import java.util.Dictionary;
import java.util.List;

import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.exception.CommunicationModuleException;
import org.universAAL.middleware.modules.listener.MessageListener;

/**
 * @author amedrano
 *
 */
public class DummyCommunicationModule implements CommunicationModule {
	public void dispose() {
	}

	public String getDescription() {
		return null;
	}

	public String getName() {
		return null;
	}

	public String getProvider() {
		return null;
	}

	public String getVersion() {
		return null;
	}

	public boolean init() {
		return false;
	}

	public void loadConfigurations(Dictionary arg0) {
	}

	public void addMessageListener(MessageListener arg0, String arg1) {
	}

	public MessageListener getListenerByNameAndType(String arg0, Class arg1) {
		return null;
	}

	public boolean hasChannel(String arg0) {
		return true;
	}

	public void messageReceived(ChannelMessage arg0) {
	}

	public void removeMessageListener(MessageListener arg0, String arg1) {
	}

	public void send(ChannelMessage arg0, PeerCard arg1)
			throws CommunicationModuleException {
	}

	public void send(ChannelMessage arg0, MessageListener arg1, PeerCard arg2)
			throws CommunicationModuleException {
	}

	public void sendAll(ChannelMessage arg0)
			throws CommunicationModuleException {
	}

	public void sendAll(ChannelMessage arg0, List<PeerCard> arg1)
			throws CommunicationModuleException {
	}

	public void sendAll(ChannelMessage arg0, MessageListener arg1)
			throws CommunicationModuleException {
	}

	public void sendAll(ChannelMessage arg0, List<PeerCard> arg1,
			MessageListener arg2) throws CommunicationModuleException {
	}
}