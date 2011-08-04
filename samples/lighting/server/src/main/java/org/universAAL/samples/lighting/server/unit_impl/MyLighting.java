/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.universAAL.samples.lighting.server.unit_impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.samples.lighting.server.Activator;

/**
 * @author mtazari
 * 
 *         A simple model that is able to manage four virtual light sources
 * 
 */
public class MyLighting {
    private class Lamp {
	String loc;
	boolean isOn;

	Lamp(String loc, boolean isOn) {
	    this.loc = loc;
	    this.isOn = isOn;
	}
    }

    private Lamp[] myLampDB = new Lamp[] { new Lamp("loc1", false),
	    new Lamp("loc2", false), new Lamp("loc3", false),
	    new Lamp("loc4", false) };

    private ArrayList listeners = new ArrayList();

    public MyLighting() {
    }

    public void addListener(LampStateListener l) {
	listeners.add(l);
    }

    public int[] getLampIDs() {
	int[] ids = new int[myLampDB.length];
	for (int i = 0; i < myLampDB.length; i++)
	    ids[i] = i;
	return ids;
    }

    public String getLampLocation(int lampID) {
	return myLampDB[lampID].loc;
    }

    public boolean isOn(int lampID) {
	return myLampDB[lampID].isOn;
    }

    public void removeListener(LampStateListener l) {
	listeners.remove(l);
    }

    public void turnOff(int lampID) {
	if (myLampDB[lampID].isOn) {
	    myLampDB[lampID].isOn = false;
	    LogUtils.logInfo(Activator.mc, MyLighting.class, "turnOff",
		    new Object[] { "Lamp in ", myLampDB[lampID].loc,
			    " turned off!" }, null);
	    for (Iterator i = listeners.iterator(); i.hasNext();)
		((LampStateListener) i.next()).lampStateChanged(lampID,
			myLampDB[lampID].loc, false);
	}
    }

    public void turnOn(int lampID) {
	if (!myLampDB[lampID].isOn) {
	    myLampDB[lampID].isOn = true;
	    LogUtils.logInfo(Activator.mc, MyLighting.class, "turnOn",
		    new Object[] { "Lamp in ", myLampDB[lampID].loc,
			    " turned on!" }, null);
	    for (Iterator i = listeners.iterator(); i.hasNext();)
		((LampStateListener) i.next()).lampStateChanged(lampID,
			myLampDB[lampID].loc, true);
	}
    }
}
