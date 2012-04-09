/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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
package org.universAAL.middleware.sodapop;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

/**
 * 
 * Interface describing SodaPop layer (instance).
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public interface SodaPop {
    public MessageContentSerializer getContentSerializer(ModuleContext context);

    /**
     * 
     * @param name
     *            name of the local bus
     * @return bus instance
     */
    public AbstractBus getLocalBusByName(String name);

    /**
     * 
     * @return ID of the SodaPop instance
     */
    public String getID();

    /**
     * 
     * @param bus
     *            bus that leaves SodaPop
     */
    public void leave(AbstractBus bus);

    /**
     * 
     * @param bus
     *            bus that is added to the SodaPop
     */
    public void join(AbstractBus bus);

    // TODO add explanation for returning INT
    /**
     * 
     * @param bus
     *            bus on which message is to be propagated
     * @param msg
     *            message to propagate
     * @return
     */
    public int propagateMessage(AbstractBus bus, Message msg);
}
