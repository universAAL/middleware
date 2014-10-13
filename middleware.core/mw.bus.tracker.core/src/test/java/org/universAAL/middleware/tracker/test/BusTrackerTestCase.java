/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
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
package org.universAAL.middleware.tracker.test;

import org.universAAL.middleware.bus.junit.BusTestCase;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;
import org.universAAL.middleware.tracker.impl.Activator;

public class BusTrackerTestCase extends BusTestCase {

    // timeout is currently not used because the bus tracker works synchronously
    int timeout = 1; // unit is 10 ms
    static IBusMemberRegistry tracker;
    static boolean isInitialized = false;
    static BusListener listener = new BusListener();

    private static class BusListener implements IBusMemberRegistryListener {

	public boolean busMemberAddedDone;
	public boolean busMemberRemovedDone;
	public boolean regParamsAddedDone;
	public boolean regParamsRemovedDone;
	public boolean dups;
	public BusMember member;
	public BusType type;
	public String busMemberID;
	public Resource[] params;

	public void init() {
	    busMemberAddedDone = false;
	    busMemberRemovedDone = false;
	    regParamsAddedDone = false;
	    regParamsRemovedDone = false;
	    member = null;
	    type = null;
	    busMemberID = null;
	    params = null;
	    dups = false;
	}

	public synchronized void busMemberAdded(BusMember member, BusType type) {
	    System.out.println("  ---- bus member added: ");
	    System.out.println("       " + member.getURI() + "   "
		    + type.toString());
	    if (busMemberAddedDone == true)
		dups = true;
	    if (this.member != null)
		dups = true;
	    if (this.type != null)
		dups = true;
	    busMemberAddedDone = true;
	    this.member = member;
	    this.type = type;
	}

	public synchronized void busMemberRemoved(BusMember member, BusType type) {
	    System.out.println("  ---- bus member removed: ");
	    System.out.println("       " + member.getURI() + "   "
		    + type.toString());
	    if (busMemberRemovedDone == true)
		dups = true;
	    if (this.member != null)
		dups = true;
	    if (this.type != null)
		dups = true;
	    busMemberRemovedDone = true;
	    this.member = member;
	    this.type = type;
	}

	public synchronized void regParamsAdded(String busMemberID,
		Resource[] params) {
	    System.out.println("  ---- regParamsAdded: ");
	    System.out.println("       " + busMemberID);
	    if (regParamsAddedDone == true)
		dups = true;
	    if (this.busMemberID != null)
		dups = true;
	    if (this.params != null)
		dups = true;
	    regParamsAddedDone = true;
	    this.busMemberID = busMemberID;
	    this.params = params;
	}

	public synchronized void regParamsRemoved(String busMemberID,
		Resource[] params) {
	    System.out.println("  ---- regParamsRemoved: ");
	    System.out.println("       " + busMemberID);
	    if (regParamsRemovedDone == true)
		dups = true;
	    if (this.busMemberID != null)
		dups = true;
	    if (this.params != null)
		dups = true;
	    regParamsRemovedDone = true;
	    this.busMemberID = busMemberID;
	    this.params = params;
	}
    }

    class MyServiceCallee extends ServiceCallee {
	protected MyServiceCallee(ServiceProfile[] realizedServices) {
	    super(mc, realizedServices);
	}

	void addProfiles(ServiceProfile[] realizedServices) {
	    addNewServiceProfiles(realizedServices);
	}

	void addProfile(ServiceProfile realizedService) {
	    addProfiles(new ServiceProfile[] { realizedService });
	}

	void removeProfile(ServiceProfile[] realizedServices) {
	    removeMatchingProfiles(realizedServices);
	}

	void removeProfile(ServiceProfile realizedService) {
	    removeProfile(new ServiceProfile[] { realizedService });
	}

	@Override
	public void communicationChannelBroken() {
	}

	@Override
	public ServiceResponse handleCall(ServiceCall call) {
	    return null;
	}
    }

    interface Checker {
	boolean check();
    }

    @Override
    protected void setUp() throws Exception {
	if (isInitialized)
	    return;
	isInitialized = true;
	super.setUp();

	mc.setAttribute(AccessControl.PROP_MODE, "none");
	mc.setAttribute(AccessControl.PROP_MODE_UPDATE, "always");

	// init tracker
	org.universAAL.middleware.tracker.impl.Activator.fetchParams = new Object[] { IBusMemberRegistry.class
		.getName() };
	Activator a = new Activator();
	a.start(mc);

	// get tracker and add our listener
	tracker = (IBusMemberRegistry) mc.getContainer().fetchSharedObject(mc,
		IBusMemberRegistry.busRegistryShareParams);
    }

    /**
     * Check the answer asynchronously in a loop until all conditions are true
     * or a timeout occurs.
     * 
     * @param checker
     */
    private void checkAnswer(Checker checker) {
	int i = 0;
	while (!checker.check()) {
	    try {
		Thread.sleep(10);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    i++;
	    if (i > timeout) {
		System.out.println("-------------------------");
		new Exception("Error: a timeout occured")
			.printStackTrace(System.out);
		System.out.println("  current values:");
		System.out.println("     busMemberAddedDone:   "
			+ listener.busMemberAddedDone);
		System.out.println("     busMemberRemovedDone: "
			+ listener.busMemberRemovedDone);
		System.out.println("     regParamsAddedDone:   "
			+ listener.regParamsAddedDone);
		System.out.println("     regParamsRemovedDone: "
			+ listener.regParamsRemovedDone);
		System.out.println("     dups:        " + listener.dups);
		System.out.println("     member:      " + listener.member);
		System.out.println("     type:        " + listener.type);
		System.out.println("     busMemberID: " + listener.busMemberID);
		System.out.println("     params:      " + listener.params);

		assertTrue(false);
	    }
	}
    }

    private boolean checkOps(boolean busMemberAddedDone,
	    boolean busMemberRemovedDone, boolean regParamsAddedDone,
	    boolean regParamsRemovedDone) {
	if (!(listener.busMemberAddedDone == busMemberAddedDone))
	    return false;
	if (!(listener.busMemberRemovedDone == busMemberRemovedDone))
	    return false;
	if (!(listener.regParamsAddedDone == regParamsAddedDone))
	    return false;
	if (!(listener.regParamsRemovedDone == regParamsRemovedDone))
	    return false;
	return true;
    }

    public void testNotifyEmptyExisting() {
	final MyServiceCallee c = new MyServiceCallee(new ServiceProfile[0]);

	listener.init();
	tracker.addListener(listener, true);
	checkAnswer(new Checker() {
	    public boolean check() {
		if (!checkOps(true, false, true, false))
		    return false;
		if (!(listener.member == c))
		    return false;
		if (!(listener.type == BusType.Service))
		    return false;
		if (listener.dups)
		    return false;
		return true;
	    }
	});

	c.close();

	tracker.removeListener(listener);
    }

    public void testAddingParams() {
	tracker.addListener(listener, true);

	listener.init();
	final MyServiceCallee c = new MyServiceCallee(new ServiceProfile[0]);
	checkAnswer(new Checker() {
	    public boolean check() {
		if (!checkOps(true, false, true, false))
		    return false;
		if (!(listener.member == c))
		    return false;
		if (!(listener.type == BusType.Service))
		    return false;
		if (listener.dups)
		    return false;
		return true;
	    }
	});

	listener.init();
	c.addProfile(new ServiceProfile());
	checkAnswer(new Checker() {
	    public boolean check() {
		if (!checkOps(false, false, true, false))
		    return false;
		if (!(listener.type == null))
		    return false;
		if (listener.dups)
		    return false;
		return true;
	    }
	});

	listener.init();
	c.close();
	checkAnswer(new Checker() {
	    public boolean check() {
		if (!checkOps(false, true, false, false))
		    return false;
		if (!(listener.type == BusType.Service))
		    return false;
		if (listener.dups)
		    return false;
		return true;
	    }
	});

	tracker.removeListener(listener);
    }

    public void testRegParams() {
	class MyService extends Service {
	    public MyService(String uri) {
		super(uri);
	    }
	}
	
	final ServiceProfile p1 = new MyService("urn:myuri#1").getProfile();
	final ServiceProfile p2 = new MyService("urn:myuri#2").getProfile();
	final ServiceProfile p3 = new MyService("urn:myuri#3").getProfile();

	final MyServiceCallee c = new MyServiceCallee(new ServiceProfile[] {
		p1, p2 });

	listener.init();
	tracker.addListener(listener, true);
	checkAnswer(new Checker() {
	    public boolean check() {
		if (!checkOps(true, false, true, false))
		    return false;
		if (!(listener.member == c))
		    return false;
		if (!(listener.type == BusType.Service))
		    return false;
		if (listener.dups)
		    return false;
		return true;
	    }
	});

	listener.init();
	c.addProfile(p3);
	checkAnswer(new Checker() {
	    public boolean check() {
		if (!checkOps(false, false, true, false))
		    return false;
		if (!(listener.type == null))
		    return false;
		if (listener.dups)
		    return false;
		Resource[] params = listener.params;
		if (params.length != 1)
		    return false;
		if (params[0] != p3)
		    return false;
		return true;
	    }
	});

	listener.init();
	c.removeProfile(p2);
	checkAnswer(new Checker() {
	    public boolean check() {
		if (!checkOps(false, false, false, true))
		    return false;
		if (!(listener.type == null))
		    return false;
		if (listener.dups)
		    return false;
		Resource[] params = listener.params;
		if (params.length != 1)
		    return false;
		if (params[0] != p2)
		    return false;
		return true;
	    }
	});

	tracker.removeListener(listener);
	c.close();
    }
}
