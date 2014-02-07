/*******************************************************************************
 * Copyright 2013 Universidad Politécnica de Madrid
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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

package org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationDefinitionTypes;

import java.io.File;
import java.net.URL;

import org.universAAL.middleware.mw.manager.configuration.core.interfaces.ConfigurableModule;

/**
 * A configuration entity that refers to a file.
 * Receive a {@link File} object in 
 * {@link ConfigurableModule#configurationChanged(org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.Scope, Object)}
 * as the value for the Scope define in this DescribedEntity.
 * <br>
 * It is recommended to use this Entity with caution, specially if the expected files are big. As they will be transmitted in a non-optimal way.
 * Don't use this entity for property files, use the {@link ConfigurationParameter} Entities for each expected line in the file.
 * @author amedrano
 *
 */
public interface ConfigurationFile extends DescribedEntity {

    /**
     * Reference to the default file to use when no file is defined.
     * @return
     */
    public URL getDefaultFileRef();
    
    /**
     * Help the Configuration Editor (interface and human user), provide comma separated extension filters for the file.
     * Eg: "*.*" for anything
     * Eg: "*.zip" for zip files only
     * Eg: "*.jpg,*.png" for jpg or png files
     * @return the comma separated extensions expected for this file.
     */
    public String getExtensionfilter();
}
