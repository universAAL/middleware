package org.universAAL.middleware.interfaces.mpa;

/**
 * Wrapper for peer ID and uApp part status:
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class Pair {
    String peerID;
    UAPPPartStatus partStatus;

    public Pair(String peerID, UAPPPartStatus partStatus) {
	this.peerID = peerID;
	this.partStatus = partStatus;
    }

    public String getPeerID() {
	return peerID;
    }

    public UAPPPartStatus getPartStatus() {
	return partStatus;
    }

    public void setPeerID(String peerID) {
	this.peerID = peerID;
    }

    public void setPartStatus(UAPPPartStatus partStatus) {
	this.partStatus = partStatus;
    }

}