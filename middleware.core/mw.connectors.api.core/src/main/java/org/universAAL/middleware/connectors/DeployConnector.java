package org.universAAL.middleware.connectors;

import java.io.File;

import org.universAAL.middleware.interfaces.mpa.UAPPCard;

/**
 * Interface for the deploy connectors.
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public interface DeployConnector extends Connector {

    /**
     * This method allows to install a part on the running container
     * 
     * @param serializedPart
     *            The part to be installed
     * @param mpaCard
     *            The MPA application to install
     */
    public void installPart(File applicationPart, UAPPCard mpaCard);

    /**
     * This method allows to install a part on the running container
     * 
     * @param part
     *            Part as binay file
     */
    public void uninstallPart(File applicationPart);

}
