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

package org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationEditionTypes;

import java.io.File;

import org.universAAL.middleware.mw.manager.configuration.core.interfaces.ConfigurableModule;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationDefinitionTypes.ConfigurationFile;

/**
 *  A configurable entity corresponding to a configuration file.
 * @author amedrano
 *
 */
public interface ConfigurationFileEditor extends ConfigurationFile, ConfigurableEntityEditor {

    /**
     * Pull the file, It will download if necessary, copying it to a temporal directory.
     * @return the {@link File} object pointing to the temporal file (it should be deleted by the caller, when finished).
     */
    File pullFile();
    
    /**
     * Push the file, it will copy the file, upload if necessary.
     * @param file the file to be pushed.
     * @return if the file was accepted by the {@link ConfigurableModule}.
     */
    boolean pushFile(File file);
}
