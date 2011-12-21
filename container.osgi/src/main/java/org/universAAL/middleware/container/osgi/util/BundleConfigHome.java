/*	
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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

/**
 * The base class for configuration files in universAAL. All configuration files
 * are contained in the root directory of the universAAL runtime (rundir). The
 * configuration files of a bundle are contained in a subdirectory with the name
 * of that bundle (Best Practice).
 * 
 * @author climberg
 * @author Carsten Stockloew
 */
public class BundleConfigHome {

    /**
     * The root directory of the runtime configuration.
     */
    public static final String uAAL_CONF_ROOT_DIR = "bundles.configuration.location";

    /**
     * The {@link java.io.File} containing the path for the configuration file
     * (the home directory of the configuration file).
     */
    protected File confHome;

    /**
     * Constructor the create a new object for accessing configuration files.
     * 
     * @param id
     *            The ID for the configuration file. The actual file name
     *            consists of the root directory of the universAAL runtime, the
     *            given ID (which is by Best Practice the bundle name), and a
     *            file name which is given to the methods
     *            {@link #getConfFileAsStream(File)} or
     *            {@link #getConfFileAsStream(String)}.
     */
    public BundleConfigHome(String id) {
	confHome = new File(new File(System.getProperty(uAAL_CONF_ROOT_DIR,
		System.getProperty("user.dir"))), id);
    }

    public String getAbsolutePath() {
	return confHome.getAbsolutePath();
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
     * Get an InputStream for the file of the given file name in the home
     * directory of the configuration file.
     * 
     * @param filename
     *            The name of the file.
     * @return An InputStream for the file.
     * @throws IOException
     */
    public InputStream getConfFileAsStream(String filename) throws IOException {
	return new FileInputStream(new File(confHome, filename));
    }

    public File getPropFile(String nameWithoutExtension) {
	return new File(confHome, nameWithoutExtension + ".properties");
    }

    /**
     * List all files in the home directory of the configuration file.
     * 
     * @return The list of files.
     */
    public File[] listFiles() {
	return confHome.listFiles();
    }

}
