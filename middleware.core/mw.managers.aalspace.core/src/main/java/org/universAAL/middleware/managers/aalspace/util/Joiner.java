/*	
	Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
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
package org.universAAL.middleware.managers.aalspace.util;

import java.util.Iterator;

import org.universAAL.middleware.managers.aalspace.AALSpaceManagerImpl;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

/**
 * This thread inspects the set of discovered AALSpaces and tries to join to the
 * default one
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class Joiner implements Runnable {

    private AALSpaceManagerImpl spaceManager;
    private boolean stop = false;
    private ModuleContext context;

    public Joiner(AALSpaceManagerImpl spaceManager, ModuleContext context) {
	this.spaceManager = spaceManager;
	this.context = context;
    }

    public void setStop() {
	this.stop = true;
    }

    public void run() {

	if (spaceManager.getAALSpaceDescriptor() == null) {

	    // look for an element of the set that matches wit the default
	    // AALSpace
	    if (!spaceManager.getAALSpaces().isEmpty()) {

		LogUtils.logDebug(
			context,
			Joiner.class,
			"Joiner",
			new Object[] { "AALSpaces found looking for the default one" },
			null);
		Iterator<AALSpaceCard> iterator = spaceManager.getAALSpaces()
			.iterator();
		while (iterator.hasNext()) {
		    AALSpaceCard candidate = iterator.next();
		    /*
		     * check if to join to the AAL Space found a)ID matches with
		     * the default configuration the one in the default
		     * configuration no default b)no default configuration
		     * found, join toi the first one
		     */
		    if ((spaceManager.getAalSpaceDefaultConfiguration() != null && candidate
			    .getSpaceID().equals(
				    spaceManager
					    .getAalSpaceDefaultConfiguration()
					    .getSpaceDescriptor().getSpaceId()))
			    || (spaceManager.getAalSpaceDefaultConfiguration() == null)) {
			if (candidate.getRetry() == 0)
			    spaceManager.getAALSpaces().remove(candidate);
			else {
			    candidate.setRetry(candidate.getRetry() - 1);
			    spaceManager.join(candidate);
			    /*
			     * //FIX Timeout is too small when trying to joining to Android peer,
			     * 		in fact it fires before that sender thread actually send the data.
			     * 		In particular, the problem is that the packet is sent on a different
			     * 		Thread and that Thread is not scheduled in time.
			     * ///SOLUTION increased timeout from 3s to 30s, but we should calculate the real
			     * 		timer that is timeout should fire after that data is actually sent
			     */
			    synchronized (spaceManager.getPendingAALSpace()) {
				try {
				    spaceManager.getPendingAALSpace().wait(
					    spaceManager
						    .getWaitAfterJoinRequest());
				} catch (Exception e) {
				    e.printStackTrace();
				}
			    }			    
			    // notify or timeout expired. To clean up the
			    // pending join
			    if (spaceManager.getAALSpaceDescriptor() == null)
				spaceManager.cleanUpJoinRequest();
			}

		    }
		}

		// check if now I'm part of an AAL Space , if yes no operations
		// if not and I am a Coordinator I run the initialization of an
		// AALSpace
		if (spaceManager.getAALSpaceDescriptor() == null
			&& spaceManager.getMyPeerCard().isCoordinator()) {
		    spaceManager.initAALSpace(spaceManager
			    .getAalSpaceDefaultConfiguration());
		}
	    }

	    // check if now I'm part of an AAL Space , if yes no operations
	    // if not and I am a Coordinator I run the initialization of an
	    // AALSpace
	    if (spaceManager.getAALSpaceDescriptor() == null
		    && spaceManager.getMyPeerCard().isCoordinator()) {
		spaceManager.initAALSpace(spaceManager
			.getAalSpaceDefaultConfiguration());
	    }

	}
    }
}
