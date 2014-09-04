package org.universAAL.middleware.service.test.util;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.exception.CommunicationModuleException;
import org.universAAL.middleware.modules.listener.MessageListener;
import org.universAAL.middleware.service.impl.ServiceBusImpl;

public class MyCommunicationModule implements CommunicationModule {
    private Map<String, MessageListener> listeners = new HashMap<String, MessageListener>();
    private List<PeerCard> lstCards;
    private Iterator<PeerCard> it;
    private Map<String, String> mapReadableNodes;
    private boolean dbgout = false;

    MyCommunicationModule(List<PeerCard> lstCards,
	    Map<String, String> mapReadableNodes) {
	this.lstCards = lstCards;
	it = lstCards.iterator();
	this.mapReadableNodes = mapReadableNodes;
    }

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

    public void loadConfigurations(@SuppressWarnings("rawtypes") Dictionary arg0) {
    }

    public void addMessageListener(MessageListener listener, String channelName) {
	// we only get called for service bus, so the listener should be an
	// instance of ServiceBusImpl
	if (listener instanceof ServiceBusImpl) {
	    listeners.put(it.next().getPeerID(), listener);
	} else {
	    System.out
		    .println("ERROR: MyCommunicationModule.addMessageListener called with non-ServiceBusImpl");
	}
    }

    public MessageListener getListenerByNameAndType(String arg0,
	    @SuppressWarnings("rawtypes") Class arg1) {
	return null;
    }

    public boolean hasChannel(String arg0) {
	return true;
    }

    public void messageReceived(ChannelMessage arg0) {
    }

    public void removeMessageListener(MessageListener arg0, String arg1) {
    }

    public void send(ChannelMessage message, PeerCard receiver)
	    throws CommunicationModuleException {
	MessageListener listener = listeners.get(receiver.getPeerID());
	if (listener == null) {
	    System.out
		    .println("ERROR: MyCommunicationModule.send: listener is null for receiver "
			    + receiver.getPeerID());
	    return;
	}

	if (dbgout) {
	    String n1 = mapReadableNodes.get(message.getSender().getPeerID());
	    String n2 = mapReadableNodes.get(receiver.getPeerID());
	    String out = "\n\nSending message from -"
		    + n1
		    + "- to -"
		    + n2
		    + "-:\n\t"
		    + message.getContent().replace("\n\n", "\n")
			    .replace("\n", "\n\t") + "\n\n";

	    if (!out.contains("a <http://ontology.universAAL.org/uAAL.owl#Coordinator> ."))
		System.out.println(out);
	}
	listener.messageReceived(message);
    }

    public void send(ChannelMessage message, MessageListener arg1,
	    PeerCard receiver) throws CommunicationModuleException {
	// not needed
    }

    public void sendAll(ChannelMessage message)
	    throws CommunicationModuleException {
	// not needed
    }

    public void sendAll(ChannelMessage message, List<PeerCard> receivers)
	    throws CommunicationModuleException {
	// not needed
    }

    public void sendAll(ChannelMessage message, MessageListener arg1)
	    throws CommunicationModuleException {
	// get all PeerIDs except the sender of the channel message
	String sender = message.getSender().getPeerID();
	List<PeerCard> receivers = new ArrayList<PeerCard>();
	for (PeerCard p : lstCards) {
	    if (!p.getPeerID().equals(sender))
		receivers.add(p);
	}
	sendAll(message, receivers, null);
    }

    public void sendAll(ChannelMessage message, List<PeerCard> receivers,
	    MessageListener arg2) throws CommunicationModuleException {
	for (PeerCard p : receivers) {
	    send(message, p);
	}
    }
}
