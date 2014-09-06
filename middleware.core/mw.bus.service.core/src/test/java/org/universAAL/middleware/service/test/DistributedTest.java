package org.universAAL.middleware.service.test;

import java.util.List;

import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.test.ontology.Room;
import org.universAAL.middleware.service.test.util.ArrayListCallHandler;
import org.universAAL.middleware.service.test.util.ObjectCallHandler;
import org.universAAL.middleware.service.test.util.ProfileUtil;
import org.universAAL.middleware.service.test.util.RequestUtil;
import org.universAAL.middleware.service.test.util.ServiceBusTestCase;
import org.universAAL.middleware.service.test.util.TwoObjectCallHandler;

/**
 * Unit tests for testing a distributed service bus with 3 bus instances: a
 * coordinator (coord) and 2 nodes (node1 and node2).
 * 
 * Each unit test should have a scenario description that describes for all
 * three nodes which profiles are registered there. A '*' means that the caller
 * is on that node; the request of the caller is mentioned at the end.
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

    public void testSingleCoordGetLamps() {
	// scenario:
	// coord: getLamps *
	// node1: -
	// node2: -
	// *: getLamps
	reset();
	deployProfiles(COORD, ProfileUtil.create_getControlledLamps(true));
	setHandler(COORD, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));

	ServiceResponse sr = call(COORD, RequestUtil.getAllLampsRequest(true));
	checkResponse(sr);
    }

    public void testSingleDistributedGetLamps1() {
	// scenario:
	// coord: getLamps
	// node1: *
	// node2: -
	// *: getLamps
	reset();
	deployProfiles(COORD, ProfileUtil.create_getControlledLamps(true));
	setHandler(COORD, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));

	ServiceResponse sr = call(NODE1, RequestUtil.getAllLampsRequest(true));
	checkResponse(sr);
    }

    public void testSingleDistributedGetLamps2() {
	// scenario:
	// coord: *
	// node1: getLamps
	// node2: -
	// *: getLamps
	reset();
	deployProfiles(NODE1, ProfileUtil.create_getControlledLamps(true));
	setHandler(NODE1, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));

	ServiceResponse sr = call(COORD, RequestUtil.getAllLampsRequest(true));
	checkResponse(sr);
    }

    public void testSingleDistributedGetLamps3() {
	// scenario:
	// coord: -
	// node1: getLamps *
	// node2: -
	// *: getLamps
	reset();
	deployProfiles(NODE1, ProfileUtil.create_getControlledLamps(true));
	setHandler(NODE1, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));

	ServiceResponse sr = call(NODE1, RequestUtil.getAllLampsRequest(true));
	checkResponse(sr);
    }

    public void testSingleDistributedGetLamps4() {
	// scenario:
	// coord: -
	// node1: getLamps
	// node2: *
	// *: getLamps
	reset();
	deployProfiles(NODE1, ProfileUtil.create_getControlledLamps(true));
	setHandler(NODE1, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));

	ServiceResponse sr = call(NODE2, RequestUtil.getAllLampsRequest(true));
	checkResponse(sr);
    }

    public void testMultiCoordGetLamps1() {
	// scenario:
	// coord: getLamps1 getLamps2 *
	// node1: -
	// node2: -
	// *: getLamps
	reset();
	deployProfiles(COORD, 0, ProfileUtil.create_getControlledLamps(true, 0));
	setHandler(COORD, 0, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));
	deployProfiles(COORD, 1, ProfileUtil.create_getControlledLamps(true, 1));
	setHandler(COORD, 1, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp2));

	ServiceResponse sr = call(COORD, RequestUtil.getAllLampsRequest(true));
	checkResponse2(sr);
    }

    public void testMultiCoordGetLamps2() {
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

    public void testMultiDistributedGetLamps1() {
	// scenario:
	// coord: getLamps *
	// node1: getLamps
	// node2: -
	// *: getLamps
	reset();
	deployProfiles(COORD, 0, ProfileUtil.create_getControlledLamps(true));
	setHandler(COORD, 0, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));
	deployProfiles(NODE1, 1, ProfileUtil.create_getControlledLamps(true));
	setHandler(NODE1, 1, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp2));

	ServiceResponse sr = call(COORD, RequestUtil.getAllLampsRequest(true));
	checkResponse2(sr);
    }

    public void testMultiDistributedGetLamps2() {
	// scenario:
	// coord: -
	// node1: getLamps1 getLamps2
	// node2: *
	// *: getLamps
	// with ArrayListCallHandler
	reset();
	deployProfiles(NODE1, 0, ProfileUtil.create_getControlledLamps(true, 0));
	setHandler(NODE1, 0, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));
	deployProfiles(NODE1, 1, ProfileUtil.create_getControlledLamps(true, 1));
	setHandler(NODE1, 1, new ArrayListCallHandler(
		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp2));

	ServiceResponse sr = call(NODE2, RequestUtil.getAllLampsRequest(true));
	checkResponse2(sr);
    }

    public void testMultiDistributedGetLamps3() {
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

    public void testMultiDistributedGetLamps4() {
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

    public void testMultiDistributedGetLampsInfo() {
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
}
