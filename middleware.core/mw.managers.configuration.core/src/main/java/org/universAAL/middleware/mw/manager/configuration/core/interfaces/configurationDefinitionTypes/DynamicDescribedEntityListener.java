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

package org.universAAL.middleware.mw.manager.configuration.core.interfaces.configurationDefinitionTypes;

import java.io.File;
import java.net.URL;

import org.universAAL.middleware.xsd.Base64Binary;

/**
 * Listener to be called when a {@link DynamicDescribedEntity} changes value.
 * @author amedrano
 *
 */
public interface DynamicDescribedEntityListener {
    
    /**
     * Call this method when the description of a Described Entity has changed,
     * so configuration editors can update it.
     * @param dentity
     */
    public void updatedDescription(DescribedEntity dentity);

    /**
     * Call this method when the entity has updated the value internally,
     * so persistent value can be stored and configuration editors informed on the new value.<BR>
     * In case of {@link ConfigurationParameter} type entities the validity of the new value will be checked against the 
     * restrictions.<br>
     * In case of {@link ConfigurationFile} type entities the value must be either:
     * 	<ul>
     * 	<li> a {@link File}
     * 	<li> an {@link URL}
     *  <li> a Base64 coded {@link String}
     *  <li> a byte[] containing the data of the file
     *  <li> a {@link Base64Binary}
     *  </ul>
     * @param deEntity
     * @param value
     */
    public void updatedValue(DescribedEntity deEntity, Object value);
}
