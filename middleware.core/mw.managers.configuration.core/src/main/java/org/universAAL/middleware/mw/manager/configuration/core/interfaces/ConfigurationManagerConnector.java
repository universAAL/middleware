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

package org.universAAL.middleware.mw.manager.configuration.core.interfaces;

import org.universAAL.middleware.brokers.message.configuration.ConfigurationMessage;

/**
 * Contector Methods to recieve messages from control bus.
 * @author amedrano
 *
 */
public interface ConfigurationManagerConnector {
    
    /**
     * Called when a propagation message is received.
     * @param message
     */
    public void processPropagation(ConfigurationMessage message);
    
    /**
     * Called when a request message is received.
     * @param message
     */
    public void processRequest(ConfigurationMessage message);
    
    /**
     * Called when a response message is received.
     * @param message
     */
    public void processResponse(ConfigurationMessage message);

}
