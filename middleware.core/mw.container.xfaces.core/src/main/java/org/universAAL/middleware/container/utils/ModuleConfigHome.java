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
package org.universAAL.middleware.container.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.universAAL.middleware.container.ModuleContext;

/**
 * The base class for configuration files in universAAL. All configuration files
 * are contained in the root directory of the universAAL runtime (rundir). The
 * configuration files of a bundle are contained in a subdirectory with the name
 * of that module (Best Practice).
 * 
 * Configuration files that follow the key-value format should use
 * {@link ModuleContext#registerConfigFile(Object[])} and the container-specific
 * mechanisms to get notified when the configuration changes.
 * 
 * @author Carsten Stockloew
 */
public class ModuleConfigHome {

    /**
     * The {@link java.io.File} containing the path for the configuration file
     * (the home directory of the configuration file).
     */
    protected File confHome;

    /**
     * Constructor the create a new object for accessing configuration files.
     * The actual file name consists of the root directory of the universAAL
     * runtime, the given ID (which is by Best Practice the module name), and a
     * file name which is given to the methods
     * {@link #getConfFileAsStream(File)} or
     * {@link #getConfFileAsStream(String)}.
     * 
     * @param confHome
     *            The home directory.
     * @param id
     *            The ID of this module.
     */
    public ModuleConfigHome(String confHome, String id) {
	this.confHome = new File(confHome, id);
    }

    /**
     * Returns the absolute pathname string of this abstract pathname.
     * 
     * @see File#getAbsolutePath()
     * @return the absolute pathname string
     */
    public String getAbsolutePath() {
	return confHome.getAbsolutePath();
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

    /**
     * Get an OutputStream for the file of the given file name in the home
     * directory of the configuration file.
     * 
     * @param filename
     *            The name of the file.
     * @return An OutputStream for the file.
     * @throws IOException
     */
    public OutputStream getConfFileAsOutputStream(String filename)
	    throws IOException {
	return new FileOutputStream(new File(confHome, filename));
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
