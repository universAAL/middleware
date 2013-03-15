package org.universAAL.middleware.interfaces.mpa;

import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.interfaces.mpa.UAPPCard;
import org.universAAL.middleware.interfaces.mpa.UAPPPartStatus;

/**
 * Class describing the status of an uApp:<br>
 * uApp IDs, list of (parts, peers and status)
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class UAPPStatus {
    public UAPPCard mpaCard;
    // PartID, <PeerID,MPAPartStatus >
    Map<String, Pair> mpaParts;

    public UAPPStatus(UAPPCard mpaCard) {
	this.mpaCard = mpaCard;
	this.mpaParts = new HashMap<String, Pair>();
    }

    public UAPPCard getMpaCard() {
	return mpaCard;
    }

    public void updatePart(String partID, String peerID,
	    UAPPPartStatus partStatus) {
	if (mpaParts.containsKey(partID)) {
	    // update the status of an mpa part
	    Pair part = mpaParts.get(partID);
	    part.setPeerID(peerID);
	    part.setPartStatus(partStatus);
	} else {
	    // add a new part
	    Pair part = new Pair(peerID, partStatus);
	    mpaParts.put(partID, part);
	}
    }

    public Map<String, Pair> getMpaParts() {
	return mpaParts;
    }

}
