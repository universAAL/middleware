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
    private UAPPCard mpaCard;

    public DeployPayload(byte[] part, UAPPCard mpaCard) {

	this.part = part;
	this.mpaCard = mpaCard;
    }

    public byte[] getPart() {
	return part;
    }

    public void setPart(byte[] part) {
	this.part = part;
    }

    public UAPPCard getMpaCard() {
	return mpaCard;
    }

    public void setMpaCard(UAPPCard mpaCard) {
	this.mpaCard = mpaCard;
    }

}
