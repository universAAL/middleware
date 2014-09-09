package org.universAAL.middleware.service.test;

import java.util.List;

import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ProfileExistsException;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.service.test.ontology.Room;
import org.universAAL.middleware.service.test.util.ArrayListCallHandler;
import org.universAAL.middleware.service.test.util.CallHandler;
import org.universAAL.middleware.service.test.util.MyServiceCallee;
import org.universAAL.middleware.service.test.util.ObjectCallHandler;
import org.universAAL.middleware.service.test.util.ProfileUtil;
import org.universAAL.middleware.service.test.util.RequestUtil;
import org.universAAL.middleware.service.test.util.ResponseChecker;
import org.universAAL.middleware.service.test.util.ServiceBusTestCase;
import org.universAAL.middleware.service.test.util.TwoObjectCallHandler;

/**
 * Unit tests for testing a distributed service bus with 3 bus instances: a
 * coordinator (coord) and 2 nodes (node1 and node2).
 * 
 * Each unit test should have a scenario description that describes for all
 * three nodes which profiles are registered there. A '*' means that the caller
 * is on that node; the request of the caller is mentioned at the end. This
 * description can be omitted if all deployments are tested.
 * 
 * @author Carsten Stockloew
 * 
 */
public class DistributedTest extends ServiceBusTestCase {

    String lamp1 = "lamp1";
    String lamp2 = "lamp2";
    String lamp3 = "lamp3";

    Integer int11 = new Integer(11);
    Integer int22 = new Integer(22);

    /**
     * Helper method to check if the response is valid and has exactly the
     * member 'lamp1'
     * 
     * @param sr
     */
    private void checkResponse(ServiceResponse sr) {
	assertTrue(sr != null);
	assertTrue(sr.getCallStatus() == CallStatus.succeeded);

	List<?> lampList = sr.getOutput(RequestUtil.OUTPUT_LIST_OF_LAMPS);
	assertTrue(lampList.size() == 1);
	assertTrue(lampList.contains(lamp1));
    }

    /**
     * Helper method to check if the response is valid and has exactly the
     * members 'lamp1' and 'lamp2'
     * 
     * @param sr
     */
    private void checkResponse2(ServiceResponse sr) {
	assertTrue(sr != null);
	assertTrue(sr.getCallStatus() == CallStatus.succeeded);

	List<?> lampList = sr.getOutput(RequestUtil.OUTPUT_LIST_OF_LAMPS);
	assertTrue(lampList.size() == 2);
	assertTrue(lampList.contains(lamp1));
	assertTrue(lampList.contains(lamp2));
    }

    /**
     * Helper method to check if the response is valid and has exactly the
     * members 'lamp1', 'lamp2' abd 'lamp3'
     * 
     * @param sr
     */
    private void checkResponse3(ServiceResponse sr) {
	assertTrue(sr != null);
	assertTrue(sr.getCallStatus() == CallStatus.succeeded);

	List<?> lampList = sr.getOutput(RequestUtil.OUTPUT_LIST_OF_LAMPS);
	assertTrue(lampList.size() == 3);
	assertTrue(lampList.contains(lamp1));
	assertTrue(lampList.contains(lamp2));
	assertTrue(lampList.contains(lamp3));
    }

    public void testSingleGetLamps() {
	// scenario: getLamps, 1 callee, allDeployments
	ResponseChecker checker = new ResponseChecker() {
	    public void check(ServiceResponse sr) {
		checkResponse(sr);
	    }
	};
	testAllDeployments("SingleProfileGetLamps",
		ProfileUtil.create_getControlledLamps(true),
		new ArrayListCallHandler(ProfileUtil.OUTPUT_CONTROLLED_LAMPS,
			lamp1), RequestUtil.getAllLampsRequest(true), checker);
    }

    public void testMultiGetLamps() {
	// scenario: getLamps, 2 callees, allDeployments
	ServiceProfile profile1 = ProfileUtil
		.create_getControlledLamps(true, 0);
	ServiceProfile profile2 = ProfileUtil
		.create_getControlledLamps(true, 1);
	CallHandler handler1 = new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1);
	CallHandler handler2 = new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp2);
	ResponseChecker checker = new ResponseChecker() {
	    public void check(ServiceResponse sr) {
		checkResponse2(sr);
	    }
	};

	testAllDeployments("TwoProfilesGetLamps", profile1, handler1, profile2,
		handler2, RequestUtil.getAllLampsRequest(true), checker, true);
    }

    public void testMultiGetLampsSameProcess() {
	// scenario: getLamps, 2 callees, allDeployments
	// the profiles for the 2 callees is the same, i.e. the process URI, but
	// the callees are not on the same node
	ServiceProfile profile1 = ProfileUtil.create_getControlledLamps(true);
	ServiceProfile profile2 = ProfileUtil.create_getControlledLamps(true);
	CallHandler handler1 = new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1);
	CallHandler handler2 = new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp2);
	ResponseChecker checker = new ResponseChecker() {
	    public void check(ServiceResponse sr) {
		checkResponse2(sr);
	    }
	};

	testAllDeployments("TwoProfilesGetLamps", profile1, handler1, profile2,
		handler2, RequestUtil.getAllLampsRequest(true), checker, false);
    }

    public void testResponseAggCoord() {
	// test the aggregation of ServiceResponses
	// scenario:
	// coord: getLamps1 getLamps2 *
	// node1: -
	// node2: -
	// *: getLamps
	// with ArrayListCallHandler and ObjectCallHandler
	reset();
	deployProfiles(COORD, 0, ProfileUtil.create_getControlledLamps(true, 0));
	setHandler(COORD, 0, new ObjectCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));
	deployProfiles(COORD, 1, ProfileUtil.create_getControlledLamps(true, 1));
	setHandler(COORD, 1, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp2, lamp3));

	ServiceResponse sr = call(COORD, RequestUtil.getAllLampsRequest(true));
	checkResponse3(sr);
    }

    public void testResponseAggListObject1() {
	// test the aggregation of ServiceResponses
	// scenario:
	// coord: -
	// node1: getLamps1 getLamps2
	// node2: *
	// *: getLamps
	// with ArrayListCallHandler and ObjectCallHandler
	reset();
	deployProfiles(NODE1, 0, ProfileUtil.create_getControlledLamps(true, 0));
	setHandler(NODE1, 0, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1, lamp2));
	deployProfiles(NODE1, 1, ProfileUtil.create_getControlledLamps(true, 1));
	setHandler(NODE1, 1, new ObjectCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp3));

	ServiceResponse sr = call(NODE2, RequestUtil.getAllLampsRequest(true));
	checkResponse3(sr);
    }

    public void testResponseAggListObject2() {
	// test the aggregation of ServiceResponses
	// scenario:
	// coord: -
	// node1: getLamps1
	// node2: getLamps2 *
	// *: getLamps
	// with ArrayListCallHandler and ObjectCallHandler
	reset();
	deployProfiles(NODE1, 0, ProfileUtil.create_getControlledLamps(true, 0));
	setHandler(NODE1, 0, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1, lamp2));
	deployProfiles(NODE2, 1, ProfileUtil.create_getControlledLamps(true, 1));
	setHandler(NODE2, 1, new ObjectCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp3));

	ServiceResponse sr = call(NODE2, RequestUtil.getAllLampsRequest(true));
	checkResponse3(sr);
    }

    public void testResponseAgg2Outputs() {
	// test the aggregation of ServiceResponses with 2 outputs
	// scenario:
	// coord: -
	// node1: getLampInfo1
	// node2: getLampInfo2 *
	// *: getLampInfo
	reset();

	Room room1 = new Room("MyRoom1");
	Room room2 = new Room("MyRoom2");

	deployProfiles(NODE1, ProfileUtil.create_getLampInfo(true));
	setHandler(NODE1, new TwoObjectCallHandler(
		ProfileUtil.OUTPUT_LAMP_BRIGHTNESS, int11,
		ProfileUtil.OUTPUT_LAMP_LOCATION, room1));
	deployProfiles(NODE2, ProfileUtil.create_getLampInfo(true));
	setHandler(NODE2, new TwoObjectCallHandler(
		ProfileUtil.OUTPUT_LAMP_BRIGHTNESS, int22,
		ProfileUtil.OUTPUT_LAMP_LOCATION, room2));

	ServiceResponse sr = call(NODE2, RequestUtil.getLampInfoRequest(true));
	assertTrue(sr != null);
	assertTrue(sr.getCallStatus() == CallStatus.succeeded);

	List<?> lstBrightness = sr.getOutput(RequestUtil.OUTPUT_BRIGHTNESS);
	assertTrue(lstBrightness.size() == 2);
	assertTrue(lstBrightness.contains(int11));
	assertTrue(lstBrightness.contains(int22));

	List<?> lstLocation = sr.getOutput(RequestUtil.OUTPUT_LOCATION);
	assertTrue(lstLocation.size() == 2);
	assertTrue(lstLocation.contains(room1));
	assertTrue(lstLocation.contains(room2));
    }

    public void testSameProcessURI() {
	// tests to register 2 callees with the same profile (same process URI)
	// on the same node
	reset();
	int num = getNumRegisteredProfiles();
	MyServiceCallee c = coordCallee1;
	c.addProfiles(new ServiceProfile[] { ProfileUtil
		.create_getControlledLamps(true) });
	waitForProfileNumberChange(num);

	// test that adding the same profile will throw
	boolean hasThrown = false;
	try {
	    c.addProfiles(new ServiceProfile[] { ProfileUtil
		    .create_getControlledLamps(true) }, true);
	} catch (ProfileExistsException e) {
	    hasThrown = true;
	}
	assertTrue(hasThrown);

	// test that adding the same profile will not throw if we set
	// throwOnError to false
	hasThrown = false;
	try {
	    c.addProfiles(new ServiceProfile[] { ProfileUtil
		    .create_getControlledLamps(true) }, false);
	} catch (ProfileExistsException e) {
	    hasThrown = true;
	}
	assertFalse(hasThrown);
    }

    public void testInjectCoord() {
	reset();
	ServiceProfile profile = ProfileUtil.create_getControlledLamps(true);
	deployProfiles(COORD, profile);
	setHandler(COORD, new ObjectCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));

	ServiceCall call = new ServiceCall(profile.getProcessURI());
	//ServiceResponse sr = coordCaller.inject(call, coordCard);
    }
}
