package org.universAAL.middleware.brokers.message.deploy;

import org.universAAL.middleware.interfaces.mpa.UAPPCard;
import org.universAAL.middleware.interfaces.mpa.UAPPPartStatus;

/**
 * Payload for deploy notifications
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class DeployNotificationPayload extends DeployPayload {

    private static final long serialVersionUID = -3622178451748007711L;
    private String mpaPartID;
    private UAPPPartStatus mpaPartStatus;

    public DeployNotificationPayload(byte[] part, UAPPCard mpaCard,
	    String partID, UAPPPartStatus mpaPartStatus) {
	super(part, mpaCard);
	this.mpaPartID = partID;
	this.mpaPartStatus = mpaPartStatus;
    }

    public String getPartID() {
	return mpaPartID;
    }

    public UAPPPartStatus getMpaPartStatus() {
	return mpaPartStatus;
    }

}
