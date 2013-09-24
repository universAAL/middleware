/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */
package org.universAAL.middleware.container;

/**
 * A set of attribute name that the {@link ModuleContext} has to provide through the
 * {@link ModuleContext#getAttribute(String)} method
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 * @since 2.0.1
 *
 */
public interface Attributes {

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the version of the middleware that is running on this container
     * @since 2.0.1
     */
    public final String MIDDLEWARE_VERSION = "org.universAAL.platform.version";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the name of the Container that is running uAAL (e.g.: Karaf, Android,
     * Felix, and so on)
     * @since 2.0.1
     */
    public final String CONTAINER_NAME = "org.universAAL.container.name";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the version of the Container that is running uAAL (e.g.: 2.3.0, 4.2.0,
     * and so on)
     * @since 2.0.1
     */
    public final String CONTAINER_VERSION = "org.universAAL.container.version";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the name of the Execution Environment where the Container
     * that is running on(e.g.: Felix, Eclipse, Android, Java, and so on)
     * @since 2.0.1
     */
    public final String CONTAINER_EE_NAME = "org.universAAL.container.ee.name";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the version of the Execution Environment where the Container
     * that is running on(e.g.: 2.3.1, 4.2.0, and so on)
     * @since 2.0.1
     */
    public final String CONTAINER_EE_VERSION = "org.universAAL.container.ee.version";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the architecture of the Execution Environment where the Container
     * that is running on(e.g.: x86, arm, and so on)
     * @since 2.0.1
     */
    public final String CONTAINER_EE_ARCHITECTURE = "org.universAAL.container.ee.architecture";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the name of the OS where Container that is running on (i.e.: for Windows
     * Vista it returns WindowsVista)
     * @since 2.0.1
     */
    public final String CONTAINER_OS_NAME = "org.universAAL.container.os.name";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the version of the OS where Container that is running on (i.e.: for
     * Windows Vista it returns 6.0.0)
     * @since 2.0.1
     */
    public final String CONTAINER_OS_VERSION = "org.universAAL.container.os.version";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the architecture of the OS where Container that is running on (i.e.: for
     * Windows Vista 32bit it returns x86)
     * @since 2.0.1
     */
    public final String CONTAINER_OS_ARCHITECTURE = "org.universAAL.container.os.architecture";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the version of platform where the Container is running on (i.e.: for
     * JavaVM-7 it returns java)
     * @since 2.0.1
     */
    public final String CONTAINER_PLATFORM_NAME = "org.universAAL.container.platform.name";

    /**
     * This is the name of the attribute of the uAAL container that represent
     * the version of platform where the Container is running on (i.e.: for
     * JavaVM-7 it returns 1.7.0)
     * @since 2.0.1
     */
    public final String CONTAINER_PLATFORM_VERSION = "org.universAAL.container.platform.version";


    /**
     * This is the prefix name that should be used by the attributes of the uAAL container
     * that provides more detail of the container
     * @since 2.0.1
     */
    public final String CONTAINER_EXTRA_INFO_PREFIX = "org.universAAL.container.extra";


}
