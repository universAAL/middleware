/*
	Copyright 2008-2010 CNR-ISTI, http://isti.cnr.it
	Institute of Information Science and Technologies 
	of the Italian National Research Council  
	
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
package org.universAAL.middleware.sodapop.impl;

import java.util.Vector;

/**
 * This class implements a thread-safe queue of PeerCommands. All the methods
 * are synchronized.
 * 
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */

public class CommandQueue {
    private Vector queue;
    private boolean running = true;

    public CommandQueue() {
	queue = new Vector();
    }

    /**
     * enqueue a command
     * 
     * @param Object
     *            cmd - command to enqueue
     * 
     */
    public synchronized void enqueue(Object cmd) {
	queue.add(cmd);
	if (queue.size() == 1) {
	    notify();
	}

    }

    /**
     * dequeue a command
     * 
     * @return Object - the dequeued command
     * 
     */
    public synchronized Object dequeue() {
	while (queue.size() == 0 && running) {
	    try {
		wait();
	    } catch (InterruptedException ignored) {
	    }
	}
	if (running)
	    return queue.remove(0);
	else
	    return null;
    }

    /**
     * close the queue
     * 
     */
    public synchronized void close() {
	running = false;
	notify();
    }

}
