package org.universAAL.middleware.modules.communication;

import org.universAAL.middleware.connectors.CommunicationConnector;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.modules.listener.MessageListener;

public class BroadcastExecutor implements Runnable {

    private ChannelMessage message;
    private CommunicationConnector communicationConnector;
    private MessageListener listener;

    public BroadcastExecutor(ChannelMessage message,
	    CommunicationConnector communicationConnector,
	    MessageListener listener) {
	this.message = message;
	this.communicationConnector = communicationConnector;
	this.listener = listener;
    }

    public void run() {
	try {
	    communicationConnector.multicast(message);
	} catch (CommunicationConnectorException e) {
	    listener.handleSendError(message, e);
	}
    }
}
