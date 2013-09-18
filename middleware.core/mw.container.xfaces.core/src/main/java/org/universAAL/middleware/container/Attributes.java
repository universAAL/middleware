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
 * A set of attribute that the {@link ModuleContext} has to provide through the
 * {@link ModuleContext#getAttribute(String)} method
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 * 
 */
public interface Attributes {

    /**
     * The version of the middleware that is running on this container
     */
    public final String MIDDLEWARE_VERSION = "org.universAAL.platform.version";

    /**
     * The name of the Container that is running uAAL (e.g.: Karaf, Android,
     * Felix, and so on)
     */
    public final String CONTAINER_NAME = "org.universAAL.container.name";

    /**
     * The version of the Container that is running uAAL (e.g.: 2.3.0, 4.2.0,
     * and so on)
     */
    public final String CONTAINER_VERSION = "org.universAAL.container.version";

    /**
     * The OSGi name where the Container that is running on(e.g.: Felix,
     * Eclipse, and so on) <b>NOTE</b> it should be set only when uAAL is
     * running on OSGi
     */
    public final String OSGI_NAME = "org.universAAL.container.osgi.name";

    /**
     * The OSGi version where the Container that is running on(e.g.: 2.3.0,
     * 4.2.0, and so on) <b>NOTE</b> it should be set only when uAAL is running
     * on OSGi
     */
    public final String OSGI_VERSION = "org.universAAL.container.osgi.version";

    /**
     * The architecture OSGi where the Container that is running on(e.g.: x86)
     * <b>NOTE</b> it should be set only when uAAL is running on OSGi
     */
    public final String OSGI_ARCHITECTURE = "org.universAAL.container.osgi.architecture";

    /**
     * The Android name where the Container that is running on(e.g.: Google,
     * Samnsung, and so on) <b>NOTE</b> it should be set only when uAAL is
     * running on Android
     */
    public final String ANDROID_NAME = "org.universAAL.container.android.name";

    /**
     * The Android version where the Container that is running on (e.g.: 2.3.1,
     * 4.2.0, and so on) <b>NOTE</b> it should be set only when uAAL is running
     * on Android
     */
    public final String ANDROID_VERSION = "org.universAAL.container.android.version";

    /**
     * The architecture Android where the Container that is running on(e.g.:
     * ARM7, ARM9) <b>NOTE</b> it should be set only when uAAL is running on
     * Android
     */
    public final String ANDROID_ARCHITECTURE = "org.universAAL.container.android.architecture";

    /**
     * The name of the OS where Container that is running on (i.e.: for Windows
     * Vista it returns WindowsVista)
     */
    public final String CONTAINER_OS_NAME = "org.universAAL.container.os.name";

    /**
     * The version of the OS where Container that is running on (i.e.: for
     * Windows Vista it returns 6.0.0)
     */
    public final String CONTAINER_OS_VERSION = "org.universAAL.container.os.version";

    /**
     * The architecture of the OS where Container that is running on (i.e.: for
     * Windows Vista 32bit it returns x86)
     */
    public final String CONTAINER_OS_ARCHITECTURE = "org.universAAL.container.os.architecture";

    /**
     * The version of platform where the Container is running on (i.e.: for
     * JavaVM-7 it returns java)
     */
    public final String CONTAINER_PLATFORM_NAME = "org.universAAL.container.platform.name";

    /**
     * The version of platform where the Container is running on (i.e.: for
     * JavaVM-7 it returns 1.7.0)
     */
    public final String CONTAINER_PLATFORM_VERSION = "org.universAAL.container.platform.version";

}
