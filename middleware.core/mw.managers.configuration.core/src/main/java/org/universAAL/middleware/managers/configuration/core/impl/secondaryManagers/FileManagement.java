/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers;

import java.io.File;
import java.net.URL;

import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.ConfigurationFile;

/**
 * An interface for Containers to provide certain files.
 * 
 * @author amedrano
 * 
 */
public interface FileManagement {

    /**
     * check, and store if not, a URL file in the cache.
     * 
     * @param url
     * @return
     */
    public File cache(URL url);

    /**
     * The master file of the configurator editor, where all the entitites will
     * be stored.
     * 
     * @return
     */
    public File getMasterFile();

    /**
     * Generate a File path for a specific identificator. These files are where
     * {@link ConfigurationFile ConfigurationFiles} will be stored.
     * 
     * @param id
     * @return
     */
    public File getLocalFile(String id);

    /**
     * Create a temporal file, used when users pull the file before copying it
     * else where.
     * 
     * @return
     */
    public File getTemporalFile();

}
