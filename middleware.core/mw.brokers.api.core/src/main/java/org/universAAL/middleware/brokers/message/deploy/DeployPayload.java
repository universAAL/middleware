package org.universAAL.middleware.brokers.message.deploy;

import org.universAAL.middleware.brokers.message.Payload;
import org.universAAL.middleware.interfaces.mpa.UAPPCard;

/**
 * Payload for Deploy messages
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class DeployPayload extends Payload {

    private byte[] part;
    private static final long serialVersionUID = 4155680139788101950L;
    private UAPPCard uappCard;

    public DeployPayload(byte[] part, UAPPCard mpaCard) {

	this.part = part;
	this.uappCard = mpaCard;
    }

    public byte[] getPart() {
	return part;
    }

    public void setPart(byte[] part) {
	this.part = part;
    }

    public UAPPCard getuappCard() {
	return uappCard;
    }

    public void setuappCard(UAPPCard mpaCard) {
	this.uappCard = mpaCard;
    }

}
