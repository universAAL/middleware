/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research 
	
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
package org.universAAL.middleware.container.osgi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.universAAL.middleware.container.utils.ModuleConfigHome;

/**
 * The base class for configuration files in universAAL. All configuration files
 * are contained in the root directory of the universAAL runtime (rundir). The
 * configuration files of a bundle are contained in a subdirectory with the name
 * of that bundle (Best Practice).
 * 
 * @author Carsten Stockloew
 */
public class BundleConfigHome extends ModuleConfigHome {

    /**
     * The root directory of the runtime configuration.
     */
    public static final String uAAL_CONF_ROOT_DIR = "bundles.configuration.location";

    /**
     * Constructor the create a new object for accessing configuration files.
     * The actual file name consists of the root directory of the universAAL
     * runtime, the given ID (which is by Best Practice the bundle name), and a
     * file name which is given to the methods
     * {@link #getConfFileAsStream(String)} or
     * {@link #getConfFileAsOutputStream(String)}.
     * 
     * @param id
     *            The ID of this module.
     */
    public BundleConfigHome(String id) {
	super(uAAL_CONF_ROOT_DIR, id);
    }

    /**
     * Get an InputStream for the given File.
     * 
     * @param f
     *            The file.
     * @return An InputStream for the file.
     * @throws IOException
     */
    public InputStream getConfFileAsStream(File f) throws IOException {
	return new FileInputStream(f);
    }

    /**
     * Get a {@link File} of the configuration file. The file name is created by
     * the home directory (as created in the constructor), the parameter given
     * to this method and the suffix ".properties".
     * 
     * @param nameWithoutExtension
     * @return
     */
    public File getPropFile(String nameWithoutExtension) {
	return new File(confHome, nameWithoutExtension + ".properties");
    }
}
