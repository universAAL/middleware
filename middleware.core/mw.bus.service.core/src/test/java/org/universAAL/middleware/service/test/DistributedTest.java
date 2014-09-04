package org.universAAL.middleware.service.test;

import java.util.List;

import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.test.util.ArrayListCallHandler;
import org.universAAL.middleware.service.test.util.ProfileUtil;
import org.universAAL.middleware.service.test.util.RequestUtil;
import org.universAAL.middleware.service.test.util.ServiceBusTestCase;

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

    /**
     * Helper method to check if the response is valid and has exactly the
     * member 'lamp1'
     * 
     * @param sr
     */
    private void checkResponse(ServiceResponse sr) {
	assertTrue(sr != null);
	assertTrue(sr.getCallStatus() == CallStatus.succeeded);

	List<?> lampList = sr.getOutput(RequestUtil.OUTPUT_LIST_OF_LAMPS, true);
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

	List<?> lampList = sr.getOutput(RequestUtil.OUTPUT_LIST_OF_LAMPS, true);
	assertTrue(lampList.size() == 2);
	assertTrue(lampList.contains(lamp1));
	assertTrue(lampList.contains(lamp2));
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

//    public void testMultiCoordGetLamps() {
//	// scenario:
//	// coord: getLamps *
//	// node1: getLamps
//	// node2: -
//	// *: getLamps
//	reset();
//	deployProfiles(COORD, 0, ProfileUtil.create_getControlledLamps(true));
//	setHandler(COORD, 0, new ArrayListCallHandler(
//		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp1));
//	deployProfiles(COORD, 1, ProfileUtil.create_getControlledLamps(true));
//	setHandler(COORD, 1, new ArrayListCallHandler(
//		ProfileUtil.OUTPUT_CONTROLLED_LAMPS, lamp2));
//
//	ServiceResponse sr = call(COORD, RequestUtil.getAllLampsRequest(true));
//	checkResponse2(sr);
//    }
    
    public void testMultiDistributedGetLamps() {
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
}
