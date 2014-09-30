package org.universAAL.middleware.service.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.MultiServiceResponse;
import org.universAAL.middleware.service.ServiceResponse;

import junit.framework.TestCase;

public class ServiceResponseTest extends TestCase {

    public static String NS = "http://ontology.igd.fhg.de/TestLampServer.owl#";
    public static String OUT1 = NS + "out1";
    public static String OUT2 = NS + "out2";

    Integer int1 = Integer.valueOf(1);
    Integer int2 = Integer.valueOf(2);
    Integer int3 = Integer.valueOf(3);
    Integer int4 = Integer.valueOf(4);

    public void testCallStatus() {
	ServiceResponse sr1 = new ServiceResponse(CallStatus.succeeded);
	assertTrue(sr1.getCallStatus() == CallStatus.succeeded);

	ServiceResponse sr2 = new ServiceResponse(CallStatus.denied);
	assertTrue(sr2.getCallStatus() == CallStatus.denied);

	MultiServiceResponse sr = new MultiServiceResponse();
	sr.addResponse(sr1);
	sr.addResponse(sr2);
	assertTrue(sr.getCallStatus() == CallStatus.succeeded);
	assertTrue(sr.hasNoSuccessCallStatus());
	assertTrue(sr.hasCallStatus(CallStatus.succeeded));
	assertTrue(sr.hasCallStatus(CallStatus.denied));
	assertFalse(sr.hasCallStatus(CallStatus.serviceSpecificFailure));

	sr = new MultiServiceResponse();
	sr.addResponse(sr2);
	assertTrue(sr.getCallStatus() == CallStatus.denied);
	sr.addResponse(sr1);
	assertTrue(sr.getCallStatus() == CallStatus.succeeded);
	assertTrue(sr.hasNoSuccessCallStatus());
	assertTrue(sr.hasCallStatus(CallStatus.succeeded));
	assertTrue(sr.hasCallStatus(CallStatus.denied));
	assertFalse(sr.hasCallStatus(CallStatus.serviceSpecificFailure));
    }

    public void testOutputs() {
	ServiceResponse sr1 = new ServiceResponse(CallStatus.succeeded);
	ServiceResponse sr2 = new ServiceResponse(CallStatus.denied);

	sr1.addOutput(OUT1, int1);
	sr1.addOutput(OUT2, int4);
	sr2.addOutput(OUT1, new ArrayList(Arrays.asList(int2, int3)));

	List<Object> lst;
	Map<String, List<Object>> map;

	map = sr1.getOutputsMap();
	assertTrue(map.size() == 2);
	lst = map.get(OUT1);
	assertTrue(lst.size() == 1);
	assertTrue(lst.contains(int1));
	lst = map.get(OUT2);
	assertTrue(lst.size() == 1);
	assertTrue(lst.contains(int4));

	MultiServiceResponse sr = new MultiServiceResponse();
	sr.addResponse(sr1);
	sr.addResponse(sr2);

	// combined output: out1
	lst = sr.getOutput(OUT1);
	assertTrue(lst.size() == 3);
	assertTrue(lst.contains(int1));
	assertTrue(lst.contains(int2));
	assertTrue(lst.contains(int3));

	// combined output: out1
	lst = sr.getOutput(OUT2);
	assertTrue(lst.size() == 1);
	assertTrue(lst.contains(int4));

	// map output
	map = sr.getOutputsMap();
	assertTrue(map.size() == 2);
	lst = map.get(OUT1);
	assertTrue(lst.size() == 3);
	assertTrue(lst.contains(int1));
	assertTrue(lst.contains(int2));
	assertTrue(lst.contains(int3));

	lst = map.get(OUT2);
	assertTrue(lst.size() == 1);
	assertTrue(lst.contains(int4));
    }
}
