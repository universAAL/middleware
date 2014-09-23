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

import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.serialization.MessageContentSerializer;

/**
 * a Class that manages connections to universAAL modules.
 * 
 * @author amedrano
 * 
 */
public class SharedObjectConnector implements SharedObjectListener {

    private ModuleContext context;
    private AALSpaceManager aalSpaceManager;
    private MessageContentSerializer messageContentSerializer;
    private boolean stopping = false;
    private ControlBroker controlBroker;

    public ModuleContext getContext() {
	return context;
    }

    /**
     * @return the aalSpaceManager
     */
    public synchronized final AALSpaceManager getAalSpaceManager() {
	while (!stopping && aalSpaceManager == null) {
	    try {
		wait();
	    } catch (InterruptedException e) {
	    }
	}
	return aalSpaceManager;
    }

    /**
     * @return the controlBroker
     */
    public synchronized final ControlBroker getControlBroker() {
	while (!stopping && controlBroker == null) {
	    try {
		wait();
	    } catch (InterruptedException e) {
	    }
	}
	return controlBroker;
    }

    /**
     * @return the messageContentSerializer
     */
    public synchronized final MessageContentSerializer getMessageContentSerializer() {
	while (!stopping && messageContentSerializer == null) {
	    try {
		wait();
	    } catch (InterruptedException e) {
	    }
	}
	return messageContentSerializer;
    }

    /**
     * 
     */
    public SharedObjectConnector(ModuleContext mc) {
	context = mc;
	susbcribeFor(AALSpaceManager.class.getName());
	susbcribeFor(ControlBroker.class.getName());
	susbcribeFor(MessageContentSerializer.class.getName());
    }

    private synchronized void add(Object shr) {
	if (shr instanceof AALSpaceManager) {
	    aalSpaceManager = (AALSpaceManager) shr;
	    notifyAll();
	} else if (shr instanceof ControlBroker) {
	    controlBroker = (ControlBroker) shr;
	    notifyAll();
	} else if (shr instanceof MessageContentSerializer) {
	    messageContentSerializer = (MessageContentSerializer) shr;
	    notifyAll();
	}
    }

    private void susbcribeFor(String clazzName) {
	Object[] ref = context.getContainer().fetchSharedObject(context,
		new Object[] { clazzName }, this);
	if (ref != null && ref.length > 0) {
	    add(ref[0]);
	}
    }

    /** {@ inheritDoc} */
    public void sharedObjectAdded(Object sharedObj, Object removeHook) {
	if (!stopping) {
	    add(sharedObj);
	}
    }

    /** {@ inheritDoc} */
    public void sharedObjectRemoved(Object removeHook) {
	if (removeHook instanceof AALSpaceManager) {
	    aalSpaceManager = null;
	} else if (removeHook instanceof ControlBroker) {
	    controlBroker = null;
	} else if (removeHook instanceof MessageContentSerializer) {
	    messageContentSerializer = null;
	}
    }

    public void stop() {
	stopping = true;
    }
}
