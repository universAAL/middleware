package org.universAAL.middleware.container.osgi;

import org.universAAL.middleware.container.Attributes;
import org.universAAL.middleware.container.ModuleContext;

/**
 * A set of extra attribute that the OSGi implementation of the {@link ModuleContext}
 * provides through the {@link ModuleContext#getAttribute(String)} method
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 * @since 2.0.1
 */
public interface OSGiAttributes {
    /**
     * This is the name of the attribute of the uAAL container that represent
     * the OSGi name where the Container that is running on(e.g.: Felix,
     * Eclipse, and so on)
     * @since 2.0.1
     */
    public final String OSGI_NAME = Attributes.CONTAINER_EXTRA_INFO_PREFIX + ".osgi.name";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the OSGi version where the Container that is running on(e.g.: 2.3.0,
     * 4.2.0, and so on)
     * @since 2.0.1
     */
    public final String OSGI_VERSION = Attributes.CONTAINER_EXTRA_INFO_PREFIX + ".osgi.version";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the architecture OSGi where the Container that is running on(e.g.: x86)
     * @since 2.0.1
     */
    public final String OSGI_ARCHITECTURE = Attributes.CONTAINER_EXTRA_INFO_PREFIX + ".osgi.architecture";


}
